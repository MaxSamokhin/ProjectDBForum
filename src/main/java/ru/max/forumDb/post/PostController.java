package ru.max.forumDb.post;

import org.json.JSONObject;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.max.forumDb.Error;
import ru.max.forumDb.Message;

@RestController
@RequestMapping(path = "api/post")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping("/{id}/details")
    public ResponseEntity<?> getInfoThread(@PathVariable(value = "id") int id,  @RequestParam( name = "related", required = false ) String related ) {
        try {

            JSONObject infoPost = postService.getInfoPosts(id, related);

            return ResponseEntity.status(HttpStatus.OK).body(infoPost.toString());

        } catch (EmptyResultDataAccessException e) {
            Error error = new Error("message", "Can't find details");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error.getJsonError().toString()); // 404
        }
    }

    @PostMapping("/{id}/details")
    public ResponseEntity<?> editedMessage(@PathVariable(value = "id") int id, @RequestBody Message msg) {
        try {

            PostModel post = postService.update(id, msg);
            return ResponseEntity.status(HttpStatus.OK).body(post.getJson().toString());

        } catch (EmptyResultDataAccessException e) {
            Error error = new Error("message", "Can't find id msg");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error.getJsonError().toString()); // 404
        }
    }
}
