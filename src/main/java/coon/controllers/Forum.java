package coon.controllers;


import coon.models.Forums;
import coon.models.Threads;
import coon.models.Users;
import coon.models.data.ForumData;
import coon.models.data.ThreadData;
import coon.models.data.UserData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@Transactional
@RequestMapping("/api/forum")
public class Forum {

    private Forums forums;
    private Users users;
    private Threads threads;


    @Autowired
    public Forum(Forums forums, Users users, Threads threads) {
        this.forums = forums;
        this.users = users;
        this.threads = threads;
    }


    @PostMapping("/create")
    public ResponseEntity<ForumData> create(
            @RequestBody ForumData forum
    ) {
        ForumData found;

        try {
            found = this.forums.get(forum.getSlug());
        } catch (EmptyResultDataAccessException e1) {

            try {
                return new ResponseEntity<>(
                        this.forums.create(forum),
                        HttpStatus.CREATED
                );
            } catch (EmptyResultDataAccessException e2) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        }

        return new ResponseEntity<>(
                found,
                HttpStatus.CONFLICT
        );
    }


    @GetMapping("/{slug}/details")
    public ResponseEntity<ForumData> details(
            @PathVariable("slug") String slug
    ) {
        try {
            return new ResponseEntity<>(
                    this.forums.get(slug),
                    HttpStatus.OK
            );
        } catch (EmptyResultDataAccessException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }


    @PostMapping("/{slug}/create")
    public ResponseEntity<ThreadData> createThread(
            @PathVariable("slug") String slug,
            @RequestBody ThreadData thread
    ) {
        ForumData forum;
        UserData author;

        try {
            forum = this.forums.get(slug);
            author = this.users.get(thread.getAuthor());
        } catch (EmptyResultDataAccessException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        ThreadData found;

        try {
            found = this.threads.withSlug(thread.getSlug());
        } catch (EmptyResultDataAccessException e) {
            return new ResponseEntity<>(
                    this.threads.create(thread, forum, author),
                    HttpStatus.CREATED
            );
        }

        return new ResponseEntity<>(
                found,
                HttpStatus.CONFLICT
        );
    }


    @GetMapping("/{slug}/threads")
    public ResponseEntity<List<ThreadData>> threads(
            @PathVariable("slug") String slug,
            @RequestParam(name = "limit", defaultValue = "100", required = false) int limit,
            @RequestParam(name = "since", required = false) String since,
            @RequestParam(name = "desc", defaultValue = "false", required = false) boolean desc
    ) {
        try {
            this.forums.get(slug);
        } catch (EmptyResultDataAccessException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(
                this.threads.all(slug, since, limit, desc),
                HttpStatus.OK
        );
    }


    @GetMapping("/{slug}/users")
    public ResponseEntity<List<UserData>> users(
            @PathVariable("slug") String slug,
            @RequestParam(name = "limit", defaultValue = "100", required = false) int limit,
            @RequestParam(name = "since", required = false) String since,
            @RequestParam(name = "desc", defaultValue = "false", required = false) boolean desc
    ) {
        try {
            this.forums.get(slug);
        } catch (EmptyResultDataAccessException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(
                this.forums.members(slug, since, limit, desc),
                HttpStatus.OK
        );
    }

}
