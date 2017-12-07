package ru.max.forumDb.user;

import org.json.JSONArray;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import ru.max.forumDb.Error;

import java.util.List;

@RestController
@RequestMapping(path = "api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Создание нового пользователя в базе данных.
    @PostMapping("/{nickname}/create")
    public ResponseEntity<?> createUser(@PathVariable(value = "nickname") String nickname, @RequestBody UserModel user) {
        try {
            final UserModel newUser = userService.createUser(nickname, user.getFullName(), user.getEmail(), user.getAbout());

            return ResponseEntity.status(HttpStatus.CREATED).body(newUser.getJsonWithoutId().toString());  // 201

        } catch (DuplicateKeyException e) {

            final List<UserModel> oldUsers = userService.findUsers(nickname, user.getEmail());

            final JSONArray result = new JSONArray();
            for (UserModel users : oldUsers) {
                result.put(users.getJsonWithoutId());
            }

            return ResponseEntity.status(HttpStatus.CONFLICT).body(result.toString()); // 409
        }
    }

    // Получение информации о пользователе форума по его имени.
    @GetMapping("/{nickname}/profile")
    public ResponseEntity<?> getUserInfo(@PathVariable(value = "nickname") String nickname) {
        try {
            final UserModel user = UserService.getUserInfo(nickname);

            return ResponseEntity.status(HttpStatus.OK).body(user.getJsonWithoutId().toString());

        } catch (EmptyResultDataAccessException e) {
            Error error = new Error("message", "Can't find user with nickname " + nickname);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error.getJsonError().toString()); // 404
        }
    }

    // Изменение информации в профиле пользователя.
    @PostMapping("/{nickname}/profile")
    public ResponseEntity<?> correctProfile(@PathVariable(value = "nickname") String nickname, @RequestBody UpdateDataUser updateData) {
        try {
            final UserModel user = userService.correctUserProfile(nickname, updateData);

            return ResponseEntity.status(HttpStatus.OK).body(user.getJsonWithoutId().toString());

        } catch (EmptyResultDataAccessException e) {
            Error error = new Error("message", "Can't find user with nickname " + nickname);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error.getJsonError().toString()); // 404

        } catch (DuplicateKeyException e) {
            Error error = new Error("message", "Can't find user with nickname " + nickname);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error.getJsonError().toString()); // 409
        }
    }
}
