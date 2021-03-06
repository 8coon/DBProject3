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
public class Posts {

    private JdbcTemplate jdbc;
    private Users users;
    private Forums forums;


    @Autowired
    public Posts(JdbcTemplate jdbc, Users users, Forums forums) {
        this.jdbc = jdbc;
        this.users = users;
        this.forums = forums;
    }


    public List<PostData> create(List<PostData> posts, ThreadData thread) {
        String created = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"));

        if (posts.size() > 0 && posts.get(0).getCreated() != null) {
            created = posts.get(0).getCreated();
        }

        Connection conn = null;

        try {
            conn = this.jdbc.getDataSource().getConnection();

            PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO Posts " +
                            "(author, forum, thread, message, created, isEdited, parent, path, id)" +
                            " VALUES " +
                            "(?, ?, ?, ?, ?::TIMESTAMPTZ, ?, ?, " +
                            "   (SELECT path FROM Posts AS P WHERE P.id = ? LIMIT 1) || ?::INT, ?)",
                    Statement.NO_GENERATED_KEYS
            );

            for (PostData post: posts) {
                if (post.getParent() > 0) {
                    try {
                        int parentThread = this.jdbc.queryForObject(
                                "SELECT thread FROM Posts WHERE id = ? LIMIT 1",
                                Integer.class, post.getParent()
                        );

                        if (parentThread != thread.getId()) {
                            throw new SQLException();
                        }
                    } catch (EmptyResultDataAccessException e) {
                        throw new SQLException();
                    }
                }

                post.setId(this.jdbc.queryForObject("SELECT nextVal('Posts_id_seq')", Integer.class));
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
                ps.setInt(8, post.getParent());
                ps.setInt(9, post.getId());
                ps.setInt(10, post.getId());

                ps.addBatch();
            }

            ps.executeBatch();
            this.forums.incStat(thread.getForum(), posts.size(), 0);

            for (PostData post: posts) {
                this.forums.addMember(post.getForum(), post.getAuthor());
            }

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

            throw new DataIntegrityViolationException("");
        }
    }


    public List<PostData> flat(int threadId, int offset, int limit, boolean desc) {
        String order = (desc ? "DESC" : "ASC");

        return this.jdbc.query(
                "SELECT author, forum, thread, message, created, isEdited, parent, id " +
                        " FROM Posts WHERE thread = ? " +
                        "ORDER BY" +
                        " id " + order +
                        " LIMIT ? OFFSET ?",
                new PostData(),
                threadId, limit, offset
        );
    }


    public List<PostData> tree(int threadId, int offset, int limit, boolean desc) {
        String order = (desc ? "DESC" : "ASC");

        return this.jdbc.query(
                "SELECT author, forum, thread, message, created, isEdited, parent, id, path " +
                        " FROM Posts WHERE thread = ? " +
                        "ORDER BY" +
                        "   path " + order +
                        " LIMIT ? OFFSET ?",
                new PostData(),
                threadId, limit, offset
        );
    }


    public List<PostData> parentTree(int threadId, int offset, int limit, boolean desc) {
        String order = (desc ? "DESC" : "ASC");

        return this.jdbc.query(
                "WITH Roots AS (" +
                            "SELECT path FROM Posts WHERE thread = ? AND parent = 0 " +
                            "ORDER BY" +
                            " id " + order +
                            " LIMIT ? OFFSET ?" +
                        ") SELECT " +
                        " Posts.id, Posts.author, Posts.forum, Posts.created, Posts.message, Posts.thread, " +
                        "   Posts.parent, Posts.isEdited " +
                        "FROM Posts JOIN Roots ON Roots.path <@ Posts.path WHERE thread = ?" +
                        " ORDER BY " +
                        "   Posts.path " + order,
                new PostData(),
                threadId, limit, offset, threadId
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

            if (relation.equalsIgnoreCase("user")) {
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


    public PostData set(int id, PostData post) {
        PostData oldPost = this.jdbc.queryForObject(
                "SELECT  author, forum, thread, message, created, isEdited, parent, id" +
                        " FROM Posts WHERE id = ? LIMIT 1",
                new PostData(),
                id
        );

        if (post.getMessage() == null || oldPost.getMessage().equals(post.getMessage())) {
            return oldPost;
        }

        return this.jdbc.queryForObject(
                "UPDATE Posts SET message = ?, isEdited = ? WHERE id = ? RETURNING *",
                new PostData(),
                post.getMessage(), true, id
        );
    }


}
