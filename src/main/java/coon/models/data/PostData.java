package coon.models.data;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;


public class PostData extends Data implements RowMapper<PostData> {


    private String author;
    private String forum;
    private int thread;
    private int id;
    private String message;
    private String created;
    private boolean isEdited;
    private int parent;


    @JsonCreator
    public PostData(
            @JsonProperty("author") String author,
            @JsonProperty("forum") String forum,
            @JsonProperty("thread") int thread,
            @JsonProperty("id") int id,
            @JsonProperty("message") String message,
            @JsonProperty("created") String created,
            @JsonProperty("isEdited") boolean isEdited,
            @JsonProperty("parent") int parent
    ) {
        this.author = author;
        this.forum = forum;
        this.thread = thread;
        this.id = id;
        this.message = message;
        this.created = created;
        this.isEdited = isEdited;
        this.parent = parent;
    }

    public PostData() {
    }


    @Override
    public PostData mapRow(ResultSet resultSet, int i) throws SQLException {
        return new PostData(
                resultSet.getString("author"),
                resultSet.getString("forum"),
                resultSet.getInt("thread"),
                resultSet.getInt("id"),
                resultSet.getString("message"),
                LocalDateTime.ofInstant(
                        resultSet.getTimestamp("created").toInstant(),
                        ZoneOffset.ofHours(0)
                ).format(
                        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                ),
                resultSet.getBoolean("isEdited"),
                resultSet.getInt("parent")
        );
    }


    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getForum() {
        return forum;
    }

    public void setForum(String forum) {
        this.forum = forum;
    }

    public int getThread() {
        return thread;
    }

    public void setThread(int thread) {
        this.thread = thread;
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

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    @JsonProperty("isEdited")
    public boolean isEdited() {
        return isEdited;
    }

    public void setEdited(boolean edited) {
        isEdited = edited;
    }

    public int getParent() {
        return parent;
    }

    public void setParent(int parent) {
        this.parent = parent;
    }

}
