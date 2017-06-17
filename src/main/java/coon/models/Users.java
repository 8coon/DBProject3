package coon.models;


import coon.models.data.UserData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;


@Repository
@Transactional
public class Users {

    private JdbcTemplate jdbc;


    @Autowired
    public Users(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }


    public UserData create(UserData in) {
        this.jdbc.update(
                "INSERT INTO Users (nickname, fullname, email, about) VALUES (?, ?, ?, ?)",
                in.getNickname(), in.getFullName(), in.getEmail(), in.getAbout()
        );
        return in;
    }


    public UserData get(String nickname) {
        return this.jdbc.queryForObject(
                "SELECT * FROM Users WHERE lower(nickname) = lower(?) LIMIT 1",
                UserData.Map(),
                nickname
        );
    }


    public UserData set(String nickname, UserData user) {
        UserData newUser = this.get(nickname).merge(user);

        return this.jdbc.queryForObject(
                "UPDATE Users SET fullname = ?, email = ?, about = ? WHERE lower(nickname) = lower(?)" +
                        "RETURNING *",
                UserData.Map(),
                newUser.getFullName(), newUser.getEmail(), newUser.getAbout(), newUser.getNickname()
        );
    }

}
