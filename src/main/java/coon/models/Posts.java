package coon.models;


import coon.Application;
import coon.models.data.ForumData;
import coon.models.data.PostData;
import coon.models.data.ThreadData;
import coon.models.data.UserData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;


@Repository
@Transactional
public class Posts {

    private JdbcTemplate jdbc;
    private Users users;
    private Forums forums;
    private Threads threads;


    @Autowired
    public Posts(JdbcTemplate jdbc, Users users, Forums forums, Threads threads) {
        this.jdbc = jdbc;
        this.users = users;
        this.forums = forums;
        this.threads = threads;

        if (Application.triggered.compareAndSet(true, true)) {
            return;
        }

        this.jdbc.execute(
                "CREATE OR REPLACE FUNCTION onPostInsert() RETURNS trigger AS $func$\n" +
                        "BEGIN\n" +
                        "\n" +
                        "  IF NEW.parent > 0 THEN\n" +
                        "    NEW.path = (SELECT path FROM Posts WHERE id = NEW.parent LIMIT 1) || NEW.id;\n" +
                        "  ELSE\n" +
                        "    NEW.path = array[]::INT[] || NEW.id;\n" +
                        "  END IF;\n" +
                        "\n" +
                        "  RETURN NEW;\n" +
                        "\n" +
                        "END;\n" +
                        "$func$ LANGUAGE plpgsql;\n" +
                        "\n" +
                        "\n" +
                        "DROP TRIGGER IF EXISTS postCheck ON Posts;\n" +
                        "CREATE TRIGGER postCheck BEFORE INSERT OR UPDATE ON Posts" +
                        "   FOR EACH ROW EXECUTE PROCEDURE onPostInsert();"
        );
    }


    public List<PostData> create(List<PostData> posts, ThreadData thread) {
        String created = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"));
        Connection conn = null;

        try {
            conn = this.jdbc.getDataSource().getConnection();

            PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO Posts " +
                            "(author, forum, thread, message, created, isEdited, parent)" +
                            " VALUES " +
                            "(?, ?, ?, ?, ?::TIMESTAMPTZ, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );

            for (PostData post: posts) {
                post.setAuthor(this.users.get(post.getAuthor()).getNickname());
                post.setForum(thread.getForum());
                post.setThread(thread.getId());
                post.setCreated(created);

                ps.setString(1, post.getAuthor());
                ps.setString(2, post.getForum());
                ps.setInt(3, post.getThread());
                ps.setString(4, post.getMessage());
                ps.setString(5, post.getCreated());
                ps.setBoolean(6, post.isEdited());
                ps.setInt(7, post.getParent());

                ps.addBatch();
            }

            ps.executeBatch();
            ResultSet ids = ps.getGeneratedKeys();

            while (ids.next()) {
                posts.get(ids.getRow() - 1).setId(ids.getInt("id"));
            }

            ids.close();
            conn.close();
            return posts;

        } catch (SQLException e1) {
            if (conn != null) {
                try {
                    conn.close();
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }

            e1.printStackTrace();
        }

        return null;
    }


    public List<PostData> flat(ThreadData thread, int offset, int limit, boolean desc) {
        String order = (desc ? "DESC" : "ASC");

        return this.jdbc.query(
                "SELECT * FROM Posts WHERE thread = ? " +
                        "ORDER BY" +
                        "   created " + order +
                        " , id " + order +
                        " LIMIT ? OFFSET ?",
                new PostData(),
                thread.getId(), limit, offset
        );
    }


    public List<PostData> tree(ThreadData thread, int offset, int limit, boolean desc) {
        String order = (desc ? "DESC" : "ASC");

        return this.jdbc.query(
                "SELECT * FROM Posts WHERE thread = ? " +
                        "ORDER BY" +
                        "   path " + order +
                        " , created " + order +
                        " , id " + order +
                        " LIMIT ? OFFSET ?",
                new PostData(),
                thread.getId(), limit, offset
        );
    }


    public List<PostData> parentTree(ThreadData thread, int offset, int limit, boolean desc) {
        String order = (desc ? "DESC" : "ASC");

        return this.jdbc.query(
                "WITH Roots AS (" +
                            "SELECT id FROM Posts WHERE thread = ? AND parent = 0 " +
                            "ORDER BY" +
                            " created " + order +
                            " , id " + order +
                            " LIMIT ? OFFSET ?" +
                        ") SELECT * FROM Posts WHERE thread = ? AND Posts.path[1] IN (SELECT id FROM Roots)" +
                        "ORDER BY" +
                        "   path " + order +
                        " , created " + order +
                        " , id " + order,
                new PostData(),
                thread.getId(), limit, offset, thread.getId()
        );
    }


}
