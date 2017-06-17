package coon.models;


import coon.models.data.ForumData;
import coon.models.data.UserData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;


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

}
