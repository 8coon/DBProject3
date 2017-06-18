package coon.models;


import coon.controllers.Forum;
import coon.models.data.ForumData;
import coon.models.data.UserData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Repository
@Transactional
public class Forums {

    private JdbcTemplate jdbc;
    private Users users;


    @Autowired
    public Forums(JdbcTemplate jdbc, Users users) {
        this.jdbc = jdbc;
        this.users = users;
    }


    public ForumData create(ForumData forum) {
        UserData user = this.users.get(forum.getAuthor());
        forum.setAuthor(user.getNickname());

        this.jdbc.update(
                "INSERT INTO Forums (slug, title, author) VALUES (?, ?, ?)",
                forum.getSlug(), forum.getTitle(), forum.getAuthor()
        );

        return forum;
    }


    public ForumData get(String slug) {
        return this.jdbc.queryForObject(
                "SELECT * FROM Forums WHERE lower(slug) = lower(?) LIMIT 1",
                new ForumData(),
                slug
        );
    }


    public ForumData set(ForumData forum) {
        return this.jdbc.queryForObject(
                "UPDATE Forums SET posts = ?, threads = ? WHERE lower(slug) = lower(?) RETURNING *",
                new ForumData(),
                forum.getPosts(), forum.getThreads(), forum.getSlug()
        );
    }


    public void addMember(String forum, String author) {
        try {
            this.jdbc.queryForObject(
                    "SELECT author FROM Members WHERE lower(forum) = lower(?) AND lower(author) = lower(?) " +
                            "LIMIT 1",
                    String.class,
                    forum, author
            );
        } catch (EmptyResultDataAccessException e) {
            UserData user = this.users.get(author);

            this.jdbc.update(
                    "INSERT INTO Members (forum, author, fullname, email, about) VALUES (?, ?, ?, ?, ?)",
                    forum, user.getNickname(), user.getFullName(), user.getEmail(), user.getAbout()
            );
        }
    }


    public List<UserData> members(String forum, String since, int limit, boolean desc) {
        String order = (desc ? "DESC" : "ASC");

        return this.jdbc.query(
                "SELECT *, author AS nickname FROM Members WHERE " +
                        "lower(forum) = lower(?) " +
                        (since != null ? "AND lower(author) " + (desc ? "<" : ">") + " lower(?)"
                                       : "AND ?::TEXT IS NULL")+
                        " ORDER BY lower(author) " + order + " LIMIT ?",
                new UserData(),
                forum, since, limit
        );
    }

}
