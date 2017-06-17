package coon.controllers;


import coon.models.Forums;
import coon.models.data.ForumData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


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
        return new ResponseEntity<>(
                this.forums.create(forum),
                HttpStatus.CREATED
        );
    }

}
