package coon.controllers;


import coon.models.Users;
import coon.models.data.UserData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/user")
public class User {

    private Users users;


    @Autowired
    public User(Users users) {
        this.users = users;
    }


    @PostMapping("/{nickname}/create")
    public ResponseEntity<UserData> create(
            @PathVariable String nickname,
            @RequestBody UserData user
    ) {
        user.setNickname(nickname);

        try {
            return new ResponseEntity<>(
                    this.users.create(user),
                    HttpStatus.OK
            );
        } catch (DuplicateKeyException e) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
    }


    @GetMapping("/{nickname}/profile")
    public ResponseEntity<UserData> getProfile(
            @PathVariable String nickname
    ) {
        try {
            return new ResponseEntity<>(
                    this.users.get(nickname),
                    HttpStatus.OK
            );
        } catch (EmptyResultDataAccessException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }


    @PostMapping("/{nickname}/profile")
    public ResponseEntity<UserData> getProfile(
            @PathVariable String nickname,
            @RequestBody UserData user
    ) {
        try {
            return new ResponseEntity<>(
                    this.users.set(nickname, user),
                    HttpStatus.OK
            );
        } catch (EmptyResultDataAccessException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

}
