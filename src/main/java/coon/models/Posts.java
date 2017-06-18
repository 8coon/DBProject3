package coon.models;


import coon.Application;
import coon.models.data.*;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


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
                this.forums.addMember(thread.getForum(), post.getAuthor());
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

            ForumData forum = this.forums.get(thread.getForum());
            forum.setPosts(forum.getPosts() + posts.size());
            this.forums.set(forum);

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


    public PostDetailsData details(int id, String[] related) {
        StringBuilder joins = new StringBuilder();
        List<String> selects = new ArrayList<>();

        for (String relation: related) {
            if (relation.equalsIgnoreCase("post")) {
                selects.add(" Posts.author AS Posts_author," +
                        " Posts.forum AS Posts_forum, " +
                        " Posts.thread AS Posts_thread," +
                        " Posts.id AS Posts_id, " +
                        " Posts.message AS Posts_message, " +
                        " Posts.created AS Posts_created," +
                        " Posts.isEdited AS Posts_isEdited," +
                        " Posts.parent AS Posts_parent "
                );

                continue;
            }

            if (relation.equalsIgnoreCase("author")) {
                selects.add(" Users.nickname AS author_nickname," +
                        " Users.fullname AS author_fullname, " +
                        " Users.email AS author_email, " +
                        " Users.about AS author_about "
                );

                joins.append(" JOIN Users ON (lower(Users.nickname) = lower(Posts.author)) ");
                continue;
            }

            if (relation.equalsIgnoreCase("thread")) {
                selects.add(" Threads.author AS Threads_author," +
                        " Threads.forum AS Threads_forum, " +
                        " Threads.slug AS Threads_slug," +
                        " Threads.id AS Threads_id, " +
                        " Threads.message AS Threads_message, " +
                        " Threads.created AS Threads_created, " +
                        " Threads.title AS Threads_title, " +
                        " Threads.votes AS Threads_votes "
                );

                joins.append(" JOIN Threads ON (Threads.id = thread) ");
                continue;
            }

            if (relation.equalsIgnoreCase("forum")) {
                selects.add(" Forums.slug AS Forums_slug," +
                        " Forums.author AS Forums_author, " +
                        " Forums.title AS Forums_title, " +
                        " Forums.posts AS Forums_posts, " +
                        " Forums.threads AS Forums_threads "
                );

                joins.append(" JOIN Forums ON (lower(Forums.slug) = lower(Posts.forum)) ");
            }
        }

        return this.jdbc.queryForObject(
                "SELECT " + String.join(", ", selects)
                        + " FROM Posts " + joins.toString() + " WHERE Posts.id = ? LIMIT 1",
                new PostDetailsData(related),
                id
        );
    }


}
