package coon.controllers;


import coon.models.*;
import coon.models.data.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@Transactional
@RequestMapping("/api/thread")
public class Thread {

    private Threads threads;
    private Posts posts;
    private Users users;


    @Autowired
    public Thread(Threads threads, Posts posts, Users users) {
        this.threads = threads;
        this.posts = posts;
        this.users = users;
    }


    @PostMapping("/{slug_or_id}/create")
    public ResponseEntity<List<PostData>> createPosts(
            @PathVariable("slug_or_id") String slugOrId,
            @RequestBody List<PostData> posts
    ) {
        ThreadData thread;

        try {
            thread = this.threads.resolve(slugOrId);
        } catch (EmptyResultDataAccessException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        try {
            return new ResponseEntity<>(
                    this.posts.create(posts, thread),
                    HttpStatus.CREATED
            );
        } catch (DataIntegrityViolationException e) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        } catch (EmptyResultDataAccessException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }


    @PostMapping("/{slug_or_id}/vote")
    public ResponseEntity<ThreadData> vote(
            @PathVariable("slug_or_id") String slugOrId,
            @RequestBody VoiceData voice
    ) {
        ThreadData thread;
        UserData author;

        try {
            thread = this.threads.resolve(slugOrId);
            author = this.users.get(voice.getAuthor());
        } catch (EmptyResultDataAccessException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        this.threads.vote(author, thread, voice.getVoice());

        return new ResponseEntity<>(
                thread,
                HttpStatus.OK
        );
    }


    @GetMapping("/{slug_or_id}/details")
    public ResponseEntity<ThreadData> get(
            @PathVariable("slug_or_id") String slugOrId
    ) {
        try {
            return new ResponseEntity<>(
                    this.threads.resolve(slugOrId),
                    HttpStatus.OK
            );
        } catch (EmptyResultDataAccessException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }


    @GetMapping("/{slug_or_id}/posts")
    public ResponseEntity<?> posts(
            @PathVariable("slug_or_id") String slugOrId,
            @RequestParam(name = "limit", defaultValue = "100", required = false) int limit,
            @RequestParam(name = "marker", defaultValue = "0", required = false) int offset,
            @RequestParam(name = "desc", defaultValue = "false", required = false) boolean desc,
            @RequestParam(name = "sort", defaultValue = "flat", required = false) String sort
    ) {
        int threadId;

        try {
            threadId = this.threads.fastResolve(slugOrId);
        } catch (EmptyResultDataAccessException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        List<PostData> posts;
        int marker = 0;

        if (sort.equalsIgnoreCase("flat")) {
            posts = this.posts.flat(threadId, offset, limit, desc);
            marker = posts.size();
        } else if (sort.equalsIgnoreCase("tree")) {
            posts = this.posts.tree(threadId, offset, limit, desc);
            marker = posts.size();
        } else {
            posts = this.posts.parentTree(threadId, offset, limit, desc);

            for (PostData post: posts) {
                if (post.getParent() == 0) {
                    marker++;
                }
            }
        }

        if (posts.size() == 0) {
            if (!this.threads.exists(threadId)) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        }

        return new ResponseEntity<>(
                new PostQueryData(offset + marker, posts),
                HttpStatus.OK
        );
    }


    @PostMapping("/{slug_or_id}/details")
    public ResponseEntity<ThreadData> set(
            @PathVariable("slug_or_id") String slugOrId,
            @RequestBody ThreadData data
    ) {
        ThreadData thread;

        try {
            thread = this.threads.resolve(slugOrId);
        } catch (EmptyResultDataAccessException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(
                this.threads.set(thread, data),
                HttpStatus.OK
        );
    }

}
