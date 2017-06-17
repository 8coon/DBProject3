package coon.models.data;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;


public class UserData extends Data implements RowMapper<UserData> {


    private String nickname;
    private String fullName;
    private String email;
    private String about;


    @JsonCreator
    public UserData(
            @JsonProperty("nickname") String nickname,
            @JsonProperty("fullname") String fullName,
            @JsonProperty("email") String email,
            @JsonProperty("about") String about
    ) {
        this.nickname = nickname;
        this.fullName = fullName;
        this.email = email;
        this.about = about;
    }


    private UserData() {
    }


    public static UserData Map() {
        return new UserData();
    }


    @Override
    public UserData mapRow(ResultSet resultSet, int i) throws SQLException {
        return new UserData(
                resultSet.getString("nickname"),
                resultSet.getString("fullname"),
                resultSet.getString("email"),
                resultSet.getString("about")
        );
    }


    public UserData merge(UserData other) {
        return new UserData(
                this.nickname,
                Data.value(other.getFullName(), this.fullName),
                Data.value(other.getEmail(), this.email),
                Data.value(other.getAbout(), this.about)
        );
    }


    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }
}
