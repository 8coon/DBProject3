package coon.models.data;


import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;


public class PostQueryData {

    @JsonProperty("marker")
    public String marker;

    @JsonProperty("posts")
    public List<PostData> posts;


    public PostQueryData(int marker, List<PostData> posts) {
        this.marker = String.valueOf(marker);
        this.posts = posts;
    }

}
