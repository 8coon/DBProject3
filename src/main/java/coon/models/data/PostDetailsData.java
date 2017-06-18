package coon.models.data;


import com.fasterxml.jackson.annotation.JsonProperty;
import coon.controllers.Post;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class PostDetailsData implements RowMapper<PostDetailsData> {

    @JsonProperty("post")
    public PostData post;

    @JsonProperty("author")
    public UserData author;

    @JsonProperty("thread")
    public ThreadData thread;

    @JsonProperty("forum")
    public ForumData forum;

    private String[] related;


    public PostDetailsData(PostData post, UserData author, ThreadData thread, ForumData forum) {
        this.post = post;
        this.author = author;
        this.thread = thread;
        this.forum = forum;
    }


    public PostDetailsData() {
    }


    public PostDetailsData(String[] related) {
        this.related = related;
    }


    @Override
    public PostDetailsData mapRow(ResultSet resultSet, int i) throws SQLException {
        PostDetailsData details = new PostDetailsData();

        for (String relation: this.related) {
            if (relation.equalsIgnoreCase("post")) {
                details.post = new PostData(
                        resultSet.getString("posts_author"),
                        resultSet.getString("posts_forum"),
                        resultSet.getInt("posts_thread"),
                        resultSet.getInt("posts_id"),
                        resultSet.getString("posts_message"),
                        LocalDateTime.ofInstant(
                                resultSet.getTimestamp("posts_created").toInstant(),
                                ZoneOffset.ofHours(0)
                        ).format(
                                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                        ),
                        resultSet.getBoolean("posts_isEdited"),
                        resultSet.getInt("posts_parent")
                );

                continue;
            }

            if (relation.equalsIgnoreCase("author")) {
                details.author = new UserData(
                        resultSet.getString("author_nickname"),
                        resultSet.getString("author_fullname"),
                        resultSet.getString("author_email"),
                        resultSet.getString("author_about")
                );

                continue;
            }

            if (relation.equalsIgnoreCase("thread")) {
                details.thread = new ThreadData(
                        resultSet.getString("threads_author"),
                        LocalDateTime.ofInstant(
                                resultSet.getTimestamp("threads_created").toInstant(),
                                ZoneOffset.ofHours(0)
                        ).format(
                                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                        ),
                        resultSet.getString("threads_forum"),
                        resultSet.getInt("threads_id"),
                        resultSet.getString("threads_message"),
                        resultSet.getString("threads_title"),
                        resultSet.getString("threads_slug"),
                        resultSet.getInt("threads_votes")
                );

                continue;
            }

            if (relation.equalsIgnoreCase("forum")) {
                details.forum = new ForumData(
                        resultSet.getString("forums_slug"),
                        resultSet.getString("forums_title"),
                        resultSet.getString("forums_author"),
                        resultSet.getInt("forums_posts"),
                        resultSet.getInt("forums_threads")
                );
            }
        }

        return details;
    }

}
