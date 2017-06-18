package coon.controllers;


import coon.models.Posts;
import coon.models.data.PostData;
import coon.models.data.PostDetailsData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;


@RestController
@RequestMapping("/api/service")
public class Service {

    private JdbcTemplate jdbc;


    @Autowired
    public Service(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }


    @GetMapping("/status")
    public ResponseEntity<?> status() {
        int forums = this.jdbc.queryForObject("SELECT count(*) FROM Forums;", Integer.class);
        int posts = this.jdbc.queryForObject("SELECT count(*) FROM Posts;", Integer.class);
        int threads = this.jdbc.queryForObject("SELECT count(*) FROM Threads;", Integer.class);
        int users = this.jdbc.queryForObject("SELECT count(*) FROM Users;", Integer.class);

        return new ResponseEntity<>(
                new Object() {
                    public final int forum = forums;
                    public final int post = posts;
                    public final int thread = threads;
                    public final int user = users;
                },
                HttpStatus.OK
        );
    }


    @PostMapping("/clear")
    public void clear() {
        try {
            String sql = String.join("\n", Files.readAllLines(
                    Paths.get("src/main/resources/schema.sql")
            ));

            this.jdbc.execute(sql);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
