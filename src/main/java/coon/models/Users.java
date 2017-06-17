package coon.models;


import coon.models.data.UserData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;


@Repository
@Transactional
public class Users {

    private JdbcTemplate jdbc;


    @Autowired
    public Users(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }


    public UserData create(UserData user) {
        this.jdbc.update(
                "INSERT INTO Users (nickname, fullname, email, about) VALUES (?, ?, ?, ?)",
                user.getNickname(), user.getFullName(), user.getEmail(), user.getAbout()
        );

        return user;
    }


    public UserData get(String nickname) {
        return this.jdbc.queryForObject(
                "SELECT * FROM Users WHERE lower(nickname) = lower(?) LIMIT 1",
                UserData.Map(),
                nickname
        );
    }


    public List<UserData> all(String nickname, String email) {
        if (email == null) {
            if (nickname == null) {
                return new ArrayList<>();
            }

            return this.jdbc.query(
                    "SELECT * FROM Users WHERE lower(nickname) = lower(?)",
                    UserData.Map(),
                    nickname
            );
        }

        if (nickname == null) {
            return this.jdbc.query(
                    "SELECT * FROM Users WHERE lower(email) = lower(?)",
                    UserData.Map(),
                    email
            );
        }

        return this.jdbc.query(
                "SELECT * FROM Users WHERE lower(email) = lower(?) OR lower(nickname) = lower(?)",
                UserData.Map(),
                email, nickname
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
