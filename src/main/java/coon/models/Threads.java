package coon.models;


import coon.models.data.ForumData;
import coon.models.data.ThreadData;
import coon.models.data.UserData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;


@Repository
@Transactional
public class Threads {

    private JdbcTemplate jdbc;
    private Users users;
    private Forums forums;


    @Autowired
    public Threads(JdbcTemplate jdbc, Users users, Forums forums) {
        this.jdbc = jdbc;
        this.users = users;
        this.forums = forums;
    }


    public ThreadData create(ThreadData thread, ForumData forum, UserData author) {
        thread.setAuthor(author.getNickname());
        thread.setForum(forum.getSlug());

        if (thread.getCreated() == null) {
            thread.setCreated(LocalDateTime.now().format(
                    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            ));
        }

        return this.jdbc.queryForObject(
                "INSERT INTO Threads" +
                        "(author, created, forum, message, title, slug)" +
                        " VALUES (?, ?::TIMESTAMPTZ, ?, ?, ?, ?)" +
                        "RETURNING *",
                new ThreadData(),
                thread.getAuthor(), thread.getCreated(), thread.getForum(), thread.getMessage(), thread.getTitle(),
                thread.getSlug()
        );
    }


    public ThreadData get(String slug) {
        return this.jdbc.queryForObject(
                "SELECT * FROM Threads WHERE lower(slug) = lower(?) LIMIT 1",
                new ThreadData(),
                slug
        );
    }


    public List<ThreadData> all(String forum, String since, int limit, boolean desc) {
        return this.jdbc.query(
                "SELECT * FROM Threads " +
                        "WHERE lower(forum) = lower(?) AND " +
                        (since != null
                                ?
                                    (desc
                                            ?
                                                "created <= ?::TIMESTAMPTZ ORDER BY created DESC"
                                            :
                                                "created >= ?::TIMESTAMPTZ ORDER BY created ASC"
                                    )
                                :
                                    (desc
                                            ?
                                                "?::TEXT IS NULL ORDER BY created DESC"
                                            :
                                                "?::TEXT IS NULL ORDER BY created ASC"
                                    )
                        ) +
                        " LIMIT ?",
                new ThreadData(),
                forum, since, limit
        );
    }


}
