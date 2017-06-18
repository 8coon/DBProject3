package coon.models;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;


public class VoiceData implements RowMapper<VoiceData> {


    private String author;
    private int voice;


    @JsonCreator
    public VoiceData(
            @JsonProperty("nickname") String author,
            @JsonProperty("voice") int voice
    ) {
        this.author = author;
        this.voice = voice;
    }

    public VoiceData() {
    }


    @Override
    public VoiceData mapRow(ResultSet resultSet, int i) throws SQLException {
        return new VoiceData(
                resultSet.getString("author"),
                resultSet.getInt("voice")
        );
    }


    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public int getVoice() {
        return voice;
    }

    public void setVoice(int voice) {
        this.voice = voice;
    }

}
