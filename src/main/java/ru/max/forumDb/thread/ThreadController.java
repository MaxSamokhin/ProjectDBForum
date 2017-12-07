package ru.max.forumDb.thread;

import org.json.JSONArray;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.max.forumDb.Error;
import ru.max.forumDb.forum.ForumService;
import ru.max.forumDb.post.PostModel;
import ru.max.forumDb.post.PostService;
import ru.max.forumDb.vote.VoteModel;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping(path = "api/thread")
public class ThreadController {

    private final ThreadService threadService;
    private final PostService postService;

    public ThreadController(ThreadService threadService, PostService postService) {
        this.threadService = threadService;
        this.postService = postService;
    }

    @PostMapping("/{slug_or_id}/create")
    public ResponseEntity<?> createNewPost(@PathVariable(value = "slug_or_id") String slugOrId,
                                           @RequestBody List<PostModel> listPosts) {
        try {

            Boolean isId = true;
            int id = 0;
            try {
                id = Integer.parseInt(slugOrId);
            } catch (Exception e) {
            }

            ThreadModel thread = threadService.getThreadIdOrSlug(id, slugOrId);

            threadService.createPosts(thread, listPosts);

            final JSONArray result = new JSONArray();
            for (PostModel pst : listPosts) {
                result.put(pst.getJson());
            }

            return ResponseEntity.status(HttpStatus.CREATED).body(result.toString());  // 201

        } catch (EmptyResultDataAccessException e) {
            Error error = new Error("message", "Can't find forum with slug1");

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error.getJsonError().toString()); // 404

        } catch (DuplicateKeyException | SQLException e) {

            Error error = new Error("message", "Can't find forum with slug2");

            return ResponseEntity.status(HttpStatus.CONFLICT).body(error.getJsonError().toString()); //409

        }
    }

    @GetMapping("{slug_or_id}/details")
    public ResponseEntity<?> getInfoThread(@PathVariable(value = "slug_or_id") String slugOrId) {

        try {

            Boolean isId = true;
            int id = 0;
            try {
                id = Integer.parseInt(slugOrId);
            } catch (Exception e) {
            }

            ThreadModel thread = threadService.getThreadIdOrSlug(id, slugOrId);
            return ResponseEntity.status(HttpStatus.OK).body(thread.getJson().toString());

        } catch (EmptyResultDataAccessException e) {
            Error error = new Error("message", "Can't find forum with slug");

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error.getJsonError().toString()); // 404

        }
    }

    @PostMapping("{slug_or_id}/details")
    public ResponseEntity<?> updateThread(@PathVariable(value = "slug_or_id") String slugOrId,
                                          @RequestBody ThreadModel threadInfo) {

//        Boolean isId = true;
//        int id = 0;
//        String slug = "";
//        try {
//            id = Integer.parseInt(slugOrId);
//        } catch (Exception e) {
//            slug = slugOrId;
//            isId = false;
//        }
        try {
//            ThreadModel thread;
            Boolean isId = true;
            int id = 0;
            try {
                id = Integer.parseInt(slugOrId);
            } catch (Exception e) {
            }

            ThreadModel thread = threadService.getThreadIdOrSlug(id, slugOrId);
            ThreadModel resThread = threadService.updateInfoThread(thread, threadInfo);
            return ResponseEntity.status(HttpStatus.OK).body(resThread.getJson().toString());
//            if (isId) {
//                thread = forumService.getThreadById(id);
//                ThreadModel resThread = threadService.updateInfoThread(thread, threadInfo);
//                return ResponseEntity.status(HttpStatus.OK).body(resThread);
//            } else {
//                thread = forumService.getThread(slug);
//                ThreadModel resThread = threadService.updateInfoThread(thread, threadInfo);
//                return ResponseEntity.status(HttpStatus.OK).body(resThread);
//            }

        } catch (EmptyResultDataAccessException e) {
            Error error = new Error("message", "Can't find forum with slug");

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error.getJsonError().toString()); // 404

        }
    }

    @GetMapping("{slug_or_id}/posts")
    public ResponseEntity<?> getMessageThread(@PathVariable(value = "slug_or_id") String slugOrId,
                                              @RequestParam(name = "limit", required = false, defaultValue = "15") Integer limit,
                                              @RequestParam(value = "since", required = false, defaultValue = "-1") int since,
                                              @RequestParam(value = "sort", required = false, defaultValue = "flat") String sort,
                                              @RequestParam(value = "desc", required = false, defaultValue = "false") boolean desc) {
        try {

            Boolean isId = true;
            int id = 0;
            try {
                id = Integer.parseInt(slugOrId);
            } catch (Exception e) {
            }

            ThreadModel thread = threadService.getThreadIdOrSlug(id, slugOrId);

            List<PostModel> posts = null;

            if (Objects.equals(sort, "flat")) {

                posts = postService.getPostsFlat(thread, limit, since, desc);

                final JSONArray result = new JSONArray();
                for (PostModel pst : posts) {
                    result.put(pst.getJson());
                }

                return ResponseEntity.status(HttpStatus.OK).body(result.toString());
            }

            if (Objects.equals(sort, "tree")) {

                posts = postService.getPostsTree(thread, limit, since, desc);

                final JSONArray result = new JSONArray();
                for (PostModel pst : posts) {
                    result.put(pst.getJson());
                }

                return ResponseEntity.status(HttpStatus.OK).body(result.toString());
            }
            if (Objects.equals(sort, "parent_tree")) {

                posts = postService.getPostsParentTree(thread, limit, since, desc);

                final JSONArray result = new JSONArray();
                for (PostModel pst : posts) {
                    result.put(pst.getJson());
                }

                return ResponseEntity.status(HttpStatus.OK).body(result.toString());
            }

            return ResponseEntity.status(HttpStatus.OK).body(":(");

        } catch (EmptyResultDataAccessException e) {
            Error error = new Error("message", "Can't find forum with slug");

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error.getJsonError().toString()); // 404

        }
    }

    @PostMapping("{slug_or_id}/vote")
    public ResponseEntity<?> voteThreads(@PathVariable(value = "slug_or_id") String slugOrId,
                                         @RequestBody VoteModel vote) {
        try {

            Boolean isId = true;
            int id = 0;
            try {
                id = Integer.parseInt(slugOrId);
            } catch (Exception e) {
            }

            ThreadModel thread = threadService.getThreadIdOrSlug(id, slugOrId);
            ThreadModel votedThread = threadService.addVote(thread, vote);

            return ResponseEntity.status(HttpStatus.OK).body(votedThread.getJson().toString());

        } catch (EmptyResultDataAccessException e) {
            Error error = new Error("message", "Can't find forum with slug");

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error.getJsonError().toString()); // 404

        }
    }
}
