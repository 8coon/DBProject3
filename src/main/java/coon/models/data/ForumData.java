package coon.models.data;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;


public class ForumData extends Data implements RowMapper<ForumData> {


    private String slug;
    private String title;
    private String author;
    private int posts;
    private int threads;


    @JsonCreator
    public ForumData(
            @JsonProperty("slug") String slug,
            @JsonProperty("title") String title,
            @JsonProperty("user") String author,
            @JsonProperty("posts") int posts,
            @JsonProperty("threads") int threads
    ) {
        this.slug = slug;
        this.title = title;
        this.author = author;
        this.posts = posts;
        this.threads = threads;
    }

    public ForumData() {
    }


    @Override
    public ForumData mapRow(ResultSet resultSet, int i) throws SQLException {
        return new ForumData(
                resultSet.getString("slug"),
                resultSet.getString("title"),
                resultSet.getString("author"),
                resultSet.getInt("posts"),
                resultSet.getInt("threads")
        );
    }


    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @JsonProperty("user")
    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }


    @JsonProperty("posts")
    public int getPosts() {
        return posts;
    }

    public void setPosts(int posts) {
        this.posts = posts;
    }

    @JsonProperty("threads")
    public int getThreads() {
        return threads;
    }

    public void setThreads(int threads) {
        this.threads = threads;
    }
}
