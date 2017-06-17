package coon.models.data;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;


public class ThreadData extends Data implements RowMapper<ThreadData> {


    private String author;
    private String created;
    private String forum;
    private int id;
    private String message;
    private String title;
    private String slug;


    @JsonCreator
    public ThreadData(
            @JsonProperty("author") String author,
            @JsonProperty("created") String created,
            @JsonProperty("forum") String forum,
            @JsonProperty("id") int id,
            @JsonProperty("message") String message,
            @JsonProperty("title") String title,
            @JsonProperty("slug") String slug
    ) {
        this.author = author;
        this.created = created;
        this.forum = forum;
        this.id = id;
        this.message = message;
        this.title = title;
        this.slug = slug;
    }

    public ThreadData() {
    }


    @Override
    public ThreadData mapRow(ResultSet resultSet, int i) throws SQLException {
        return new ThreadData(
                resultSet.getString("author"),
                LocalDateTime.ofInstant(
                        resultSet.getTimestamp("created").toInstant(),
                        ZoneOffset.ofHours(0)
                ).format(
                        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                ),
                resultSet.getString("forum"),
                resultSet.getInt("id"),
                resultSet.getString("message"),
                resultSet.getString("title"),
                resultSet.getString("slug")
        );
    }


    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public String getForum() {
        return forum;
    }

    public void setForum(String forum) {
        this.forum = forum;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }


}
