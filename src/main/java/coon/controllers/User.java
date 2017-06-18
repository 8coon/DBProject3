package coon.controllers;


import coon.models.Users;
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
@RequestMapping("/api/user")
public class User {

    private Users users;


    @Autowired
    public User(Users users) {
        this.users = users;
    }


    @PostMapping("/{nickname}/create")
    public ResponseEntity<?> create(
            @PathVariable String nickname,
            @RequestBody UserData user
    ) {
        user.setNickname(nickname);
        List<UserData> found = this.users.all(nickname, user.getEmail());

        if (found.size() == 0) {
            return new ResponseEntity<>(
                    this.users.create(user),
                    HttpStatus.CREATED
            );
        }

        return new ResponseEntity<>(
                found,
                HttpStatus.CONFLICT
        );
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
    public ResponseEntity<?> getProfile(
            @PathVariable String nickname,
            @RequestBody UserData user
    ) {
        List<UserData> found = this.users.all(null, user.getEmail());
        boolean conflict = false;

        if (found.size() > 0) {
            for (UserData foundUser: found) {
                if (!foundUser.getNickname().equalsIgnoreCase(nickname)) {
                    conflict = true;
                    break;
                }
            }
        }

        if (conflict) {
            return new ResponseEntity<>(
                    found,
                    HttpStatus.CONFLICT
            );
        }

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
