package coon.controllers;


import coon.models.Forums;
import coon.models.data.ForumData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/forum")
public class Forum {

    private Forums forums;


    @Autowired
    public Forum(Forums forums) {
        this.forums = forums;
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

}
