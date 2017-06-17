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


    @JsonCreator
    public ForumData(
            @JsonProperty("slug") String slug,
            @JsonProperty("title") String title,
            @JsonProperty("user") String author
    ) {
        this.slug = slug;
        this.title = title;
        this.author = author;
    }

    private ForumData(){
    }

    public ForumData Map() {
        return new ForumData();
    }



    @Override
    public ForumData mapRow(ResultSet resultSet, int i) throws SQLException {
        return null;
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


}
