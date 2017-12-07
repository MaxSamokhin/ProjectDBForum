package ru.max.forumDb.post;

import org.json.JSONObject;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import ru.max.forumDb.Message;
import ru.max.forumDb.forum.ForumModel;
import ru.max.forumDb.thread.ThreadModel;
import ru.max.forumDb.user.UserModel;

import java.util.List;

@Service
public class PostService {
    private final JdbcTemplate jdbcTmp;

    public PostService(JdbcTemplate jdbcTmp) {
        this.jdbcTmp = jdbcTmp;
    }

    JSONObject getInfoPosts(int id, String related) {

        JSONObject jsonObject = new JSONObject();
        PostModel post = jdbcTmp.queryForObject(PostRequest.findPostById, MAPPER_POST, id);
        jsonObject.put("post", post.getJson());

        if (related != null && related.contains("user")) {
            UserModel user = jdbcTmp.queryForObject(PostRequest.findUserById, MAPPER_USER, id);
            jsonObject.put("author", user.getJsonWithoutId());
        }

        if (related != null && related.contains("thread")) {
            ThreadModel thread = jdbcTmp.queryForObject(PostRequest.findThreadById, MAPPER_THREAD, id);
            jsonObject.put("thread", thread.getJson());
        }

        if (related != null && related.contains("forum")) {
            ForumModel forum = jdbcTmp.queryForObject(PostRequest.findForumById, MAPPER_FORUM, id);

            jsonObject.put("forum", forum.getJson());
        }

        return jsonObject;
    }

    public PostModel update(int id, Message msg) {

         PostModel post = jdbcTmp.queryForObject(PostRequest.findPostById, MAPPER_POST, id);

        if (!post.getMessage().equals(msg.getMessage())) {

            if (msg.getMessage() != null) {
                post.setEdited(true);
                post.setMessage(msg.getMessage());
            }

            msg.setMessage(post.getMessage());

            jdbcTmp.update(PostRequest.updatePost, msg.getMessage(), post.isEdited(), id);
        }

        return post;
    }


    public List<PostModel> getPostsParentTree(ThreadModel thread, int limit, int since, boolean desc) {

        String sqlSort = !desc ? "asc" : "desc";
        String sign = !desc ? ">" : "<";

        String sql_new2 = "select Posts.id, Posts.parent, Posts.nickname, Posts.message, Posts.is_edited, " +
                " Forum.slug, Posts.thread_id, Posts.created from Posts " +
                " join Forum on Posts.forum_id  = Forum.id " +
                "  where thread_id = ? ";

        boolean flag = true;

        if (since != -1) {
            sql_new2 += " and path[1] in (select id " +
                    "from Posts where thread_id = ? and parent = 0 and " +
                    " path " + sign + " (select path " +
                    " from Posts" +
                    " where id = ?) " +
                    " order by path " + sqlSort + ", thread_id " + sqlSort + "  limit ? ) ";
        } else if (limit != -1) {
            sql_new2 += " and path[1] in (select id  from Posts where   thread_id = ? and parent = 0" +
                    " order by path " + sqlSort + ", thread_id " + sqlSort + " limit ? ) ";
            flag = false;
        }

        sql_new2 += "order by path "+sqlSort+" ,thread_id "+sqlSort+" ;";

        if (flag) {
            return jdbcTmp.query(sql_new2, MAPPER_POST, thread.getId(), thread.getId(), since, limit);
        }
        return jdbcTmp.query(sql_new2, MAPPER_POST, thread.getId(), thread.getId(), limit);

    }


    public List<PostModel> getPostsFlat(ThreadModel thread, int limit, int since, boolean desc) {

        String sqlSort = !desc ? "asc" : "desc";
        String sign = !desc ? ">" : "<";

        return jdbcTmp.query(PostRequest.flatSort(since,sign,sqlSort), MAPPER_POST, thread.getId(), limit);

    }


    public List<PostModel> getPostsTree(ThreadModel thread, int limit, int since, boolean desc) {

        String sqlSort = !desc ? "asc" : "desc";
        String sign = !desc ? ">" : "<";

        return jdbcTmp.query(PostRequest.treeSort(since,sign,sqlSort), MAPPER_POST, thread.getId(), limit);
    }


    private static final RowMapper<PostModel> MAPPER_POST = (rs, rowNum) -> new PostModel(
            rs.getInt("id"),
            rs.getInt("parent"),
            rs.getString("nickname"),
            rs.getString("message"),
            rs.getBoolean("is_edited"),
            rs.getString("slug"),
            rs.getInt("thread_id"),
            rs.getTimestamp("created")
    );


    private static final RowMapper<UserModel> MAPPER_USER = (rs, rowNum) -> new UserModel(
            rs.getInt("id"),
            rs.getString("nickname"),
            rs.getString("email"),
            rs.getString("fullname"),
            rs.getString("about")
    );

    private static final RowMapper<ThreadModel> MAPPER_THREAD = (rs, rowNum) -> new ThreadModel(
            rs.getInt("id"),
            rs.getString("title"),
            rs.getString("nickname"),
            rs.getString("f_slug"),
            rs.getString("message"),
            rs.getInt("votes"),
            rs.getString("t_slug"),
            rs.getTimestamp("created")
    );

    private static final RowMapper<ForumModel> MAPPER_FORUM = (rs, rowNum) -> new ForumModel(
            rs.getLong("id"),
            rs.getString("title"),
            rs.getString("slug"),
            rs.getString("nickname"),
            rs.getInt("posts"),
            rs.getInt("threads")
    );
}
