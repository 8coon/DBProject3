package coon.controllers;


import coon.models.Forums;
import coon.models.Posts;
import coon.models.Threads;
import coon.models.Users;
import coon.models.data.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


@RestController
@Transactional
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

        boolean foundPost = false;

        for (String rel: related) {
            if (rel.equalsIgnoreCase("post")) {
                foundPost = true;
                break;
            }
        }

        if (!foundPost) {
            List<String> newRel = new ArrayList<>(Arrays.asList(related));
            newRel.add("post");
            related = newRel.toArray(new String[] {""});
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


    @PostMapping("/{id}/details")
    public ResponseEntity<PostData> set(
            @PathVariable("id") int id,
            @RequestBody PostData post
    ) {
        try {
            return new ResponseEntity<>(
                    this.posts.set(id, post),
                    HttpStatus.OK
            );
        } catch (EmptyResultDataAccessException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

}
