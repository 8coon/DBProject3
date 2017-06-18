package coon.controllers;


import coon.models.Forums;
import coon.models.Posts;
import coon.models.Threads;
import coon.models.Users;
import coon.models.data.ForumData;
import coon.models.data.PostDetailsData;
import coon.models.data.ThreadData;
import coon.models.data.UserData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/post")
public class Post {

    private Posts posts;


    @Autowired
    public Post(Posts posts) {
        this.posts = posts;
    }


    @GetMapping("/{id}/details")
    public ResponseEntity<PostDetailsData> users(
            @PathVariable("id") int id,
            @RequestParam(name = "related", required = false) String[] related
    ) {
        if (related == null) {
            related = new String[] { "post" };
        }

        try {
            return new ResponseEntity<>(
                    this.posts.details(id, related),
                    HttpStatus.OK
            );
        } catch (EmptyResultDataAccessException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

}
