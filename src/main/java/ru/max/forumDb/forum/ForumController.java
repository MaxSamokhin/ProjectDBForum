package ru.max.forumDb.forum;

import org.json.JSONArray;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.max.forumDb.Error;
import ru.max.forumDb.thread.ThreadModel;
import ru.max.forumDb.thread.ThreadService;
import ru.max.forumDb.user.UserModel;
import ru.max.forumDb.user.UserService;

import java.util.List;

@RestController
@RequestMapping(path = "api/forum")
public class ForumController {

    private final ForumService forumService;
    private final UserService userService;
    private final ThreadService threadService;

    public ForumController(ForumService forumService, UserService userService, ThreadService threadService) {
        this.forumService = forumService;
        this.userService = userService;
        this.threadService = threadService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createForum(@RequestBody ForumModel data) {
        try {
            ForumModel forum = forumService.createForum(data.getTitle(), data.getSlug(), data.getUser());

            return ResponseEntity.status(HttpStatus.CREATED).body(forum.getJson().toString());  // 201

        } catch (DuplicateKeyException e) {

            ForumModel forum = (ForumModel) forumService.getInfoAboutForum(data.getSlug());

            return ResponseEntity.status(HttpStatus.CONFLICT).body(forum.getJson().toString());

        } catch (DataAccessException e) {
            Error error = new Error("message", "Can't find user with id "); // !!!!

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error.getJsonError().toString());
        }
    }

    @GetMapping("/{slug}/details")
    public ResponseEntity<?> getInfoAboutForum(@PathVariable(value = "slug") String slug) {
        try {
            ForumModel forum = forumService.getInfoAboutForum(slug);

            return ResponseEntity.status(HttpStatus.OK).body(forum.getJson().toString());

        } catch (EmptyResultDataAccessException e) {
            Error error = new Error("message", "Can't find forum with slug " + slug);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error.getJsonError().toString()); // 404
        }
    }

    @PostMapping("/{slug}/create")
    public ResponseEntity<?> createThread(@PathVariable(value = "slug") String slug,
                                          @RequestBody ThreadModel thread) {
        try {
            ThreadModel newThread = forumService.createThread(thread, slug);

            return ResponseEntity.status(HttpStatus.CREATED).body(newThread.getJson().toString());

        } catch (DuplicateKeyException e) {

            ThreadModel newThread = threadService.getThread(thread.getSlug());

            return ResponseEntity.status(HttpStatus.CONFLICT).body(newThread.getJson().toString());

        } catch (EmptyResultDataAccessException e) {
            Error error = new Error("message", "Can't find user forum or thread");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error.getJsonError().toString()); // 404
        }
    }

    @GetMapping("/{slug}/users")
    public ResponseEntity<?> getUsersSort(@PathVariable(value = "slug") String slug,
                                          @RequestParam(name = "limit", required = false, defaultValue = "15") int limit,
                                          @RequestParam(value = "since", required = false) String since,
                                          @RequestParam(value = "desc", required = false, defaultValue = "false") boolean desc) {

        try {

            final List<UserModel> users = userService.getUsersForum(slug, limit, since, desc);

            final JSONArray result = new JSONArray();
            for (UserModel user : users) {
                result.put(user.getJsonWithoutId());
            }

            return ResponseEntity.status(HttpStatus.OK).body(result.toString());

        } catch (EmptyResultDataAccessException e) {
            Error error = new Error("message", "Can't find forum");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error.getJsonError().toString()); // 404
        }
    }

    @GetMapping("/{slug}/threads")
    public ResponseEntity<?> getThreadsForum(@PathVariable(value = "slug") String slug,
                                             @RequestParam(name = "limit", required = false, defaultValue = "-1") int limit,
                                             @RequestParam(value = "since", required = false) String since,
                                             @RequestParam(value = "desc", required = false, defaultValue = "false") boolean desc) {
        try {
            final List<ThreadModel> threads = threadService.getThreadsForum(slug, limit, since, desc);

            final JSONArray result = new JSONArray();
            for (ThreadModel th : threads) {
                result.put(th.getJson());
            }

            return ResponseEntity.status(HttpStatus.OK).body(result.toString());

        } catch (EmptyResultDataAccessException e) {
            Error error = new Error("message", "Can't find forum");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error.getJsonError().toString()); // 404
        }
    }

}


