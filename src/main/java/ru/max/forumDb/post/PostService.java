package ru.max.forumDb.post;

import org.json.JSONObject;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.max.forumDb.Message;
import ru.max.forumDb.forum.ForumModel;
import ru.max.forumDb.thread.ThreadModel;
import ru.max.forumDb.user.UserModel;

import java.util.List;

@Transactional
@Service
public class PostService {
    private final JdbcTemplate jdbcTmp;

    public PostService(JdbcTemplate jdbcTmp) {
        this.jdbcTmp = jdbcTmp;
    }

    public JSONObject getInfoPosts(int id, String related) {
        JSONObject jsonObject = new JSONObject();

        String sqlFindPostById = "select Posts.id, Posts.parent, Posts.nickname, Posts.message, Posts.is_edited, Forum.slug, " +
                "Posts.thread_id, Posts.created from Posts " +
                "join Forum on Posts.forum_id = Forum.id " +
                "where Posts.id=?;";
        String sqlFindUserById = "select distinct Users.id, Users.nickname, Users.fullname, Users.email, Users.about " +
                "from Posts join Users on Posts.author_id = Users.id " +
                "where Posts.id=?;";
        String sqlFindForumById = "select Forum.id, Forum.title, Forum.slug, Users.nickname, Forum.posts, Forum.threads " +
                "from Posts " +
                "join Forum on Posts.forum_id = Forum.id " +
                "join Users on Forum.user_id = Users.id " +
                "where Posts.id=?;";
        String sqlFindThreadById = "select Thread.id, Thread.title, Users.nickname, Forum.slug as f_slug, Thread.message, " +
                "Thread.votes, Thread.slug as t_slug, Thread.created from Thread " +
                "join Forum on Thread.forum_id = Forum.id " +
                "join Users on Thread.author_id = Users.id " +
                "join Posts on Posts.thread_id = Thread.id " +
                "where Posts.id=?;";

        PostModel post = jdbcTmp.queryForObject(sqlFindPostById, MAPPER_POST, id);

        jsonObject.put("post", post.getJson());

        if (related != null && related.contains("user")) {
            UserModel user = jdbcTmp.queryForObject(sqlFindUserById, MAPPER_USER, id);
            jsonObject.put("author", user.getJsonWithoutId());
        }

        if (related != null && related.contains("thread")) {
            ThreadModel thread = jdbcTmp.queryForObject(sqlFindThreadById, MAPPER_THREAD, id);
            jsonObject.put("thread", thread.getJson());
        }

        if (related != null &&  related.contains("forum")) {
            ForumModel forum = jdbcTmp.queryForObject(sqlFindForumById, MAPPER_FORUM, id);

            jsonObject.put("forum", forum.getJson());
        }

        return jsonObject;
    }

    public PostModel update(int id, Message msg) {

        String sqlFindPostById = "select Posts.id, Posts.parent, Posts.nickname, Posts.message, Posts.is_edited, Forum.slug, " +
                "Posts.thread_id, Posts.created from Posts " +
                "join Forum on Posts.forum_id = Forum.id " +
                "where Posts.id=?;";

        String sqlUpdatePost = "update Posts set ( message, is_edited ) = (?, ?) " +
                "where Posts.id=?";

        PostModel post = jdbcTmp.queryForObject(sqlFindPostById, MAPPER_POST, id);

        if(!post.getMessage().equals(msg.getMessage())) {

            if (msg.getMessage() != null) {
                post.setEdited(true);
                post.setMessage(msg.getMessage());
            }

            msg.setMessage(post.getMessage());

            jdbcTmp.update(sqlUpdatePost, msg.getMessage(),post.isEdited(), id);
        }

        return post;
    }

//
//    @Override
//    public List<Post> getPostsParentTree(final Thread thread, final Boolean desc, final List<Integer> parents) {
//        final String SQL = "SELECT p.id, parent_id, f.slug, thread_id, __nickname, is_edited, p.message, p.created " +
//                "FROM posts p " +
//                "JOIN forums f ON (f.id = p.forum_id) " +
//                "WHERE p.__path[1] = ? AND p.thread_id = ? " +
//                "ORDER BY __path " + (desc ? "DESC" : "ASC") + ", p.id " + (desc ? "DESC" : "ASC") + ";";
//
//        List<Post> result = new ArrayList<>();
//        for (Integer parent : parents) {
//            result.addAll(template.query(SQL, POST_MAPPER, parent, thread.getId()));
//        }
//        return result;
//    }

    public List<PostModel> getPostsParentTree(ThreadModel thread, int limit, int since, boolean desc) {

        String sqlSort = !desc ? "asc" : "desc";
        String sign = !desc ? ">" : "<";

        String sub = "with sub as (select path from posts where thread_id=? and parent=0 ";

        if (since != -1) {
            sub += "and path " + sign + " (select Posts.path from Posts where Posts.id = " + since + ") ";
        }
        sub += "order by  Posts.id " + sqlSort + " limit ?)";

        String sql = sub + "select Posts.id, Posts.parent, Posts.nickname, Posts.message, Posts.is_edited," +
                " Forum.slug, Posts.thread_id, Posts.created from Posts " +
                " join Forum on Posts.forum_id = Forum.id " +
                " join sub on sub.path <@ Posts.path " +
                "order by Posts.path " + sqlSort;

        return jdbcTmp.query(sql, MAPPER_POST, thread.getId(), limit);
    }


    public List<PostModel> getPostsFlat(ThreadModel thread, int limit, int since, boolean desc) {

        String sqlSort = !desc ? "asc" : "desc";
        String sign = !desc ? ">" : "<";

        String sql = "select Posts.id, Posts.parent, Posts.nickname, Posts.message, Posts.is_edited, Forum.slug, " +
                "Posts.thread_id, Posts.created from Forum " +
                " join Posts on Posts.forum_id = Forum.id ";
        if (since != -1) {
            sql += " where (Posts.thread_id = ? and Posts.id " + sign + " " + since + " ) ";
        } else {
            sql += "where Posts.thread_id = ? ";
        }
        sql += "order by  Posts.created " + sqlSort + ",  Posts.id  " + sqlSort + " limit ? ;";

        return jdbcTmp.query(sql, MAPPER_POST, thread.getId(), limit);

    }


    public List<PostModel> getPostsTree(ThreadModel thread, int limit, int since, boolean desc) {

        String sqlSort = !desc ? "asc" : "desc";
        String sign = !desc ? ">" : "<";

        String sql = "select Posts.id, Posts.parent, Posts.nickname, Posts.message, Posts.is_edited, Forum.slug, " +
                "Posts.thread_id, Posts.created from Posts " +
                " join Forum on Posts.forum_id = Forum.id " +
                " where  Posts.thread_id = ? ";

        if (since != -1) {
            sql += " and Posts.path " + sign + " (select Posts.path from Posts where Posts.id = " + since + ") ";
        }
        sql += "order by  Posts.path " + sqlSort + ", Posts.id " + sqlSort + " limit ? ;";

        return jdbcTmp.query(sql, MAPPER_POST, thread.getId(), limit);
    }



    public static final RowMapper<PostModel> MAPPER_POST = (rs, rowNum) -> new PostModel(
            rs.getInt("id"),
            rs.getInt("parent"),
            rs.getString("nickname"),
            rs.getString("message"),
            rs.getBoolean("is_edited"),
            rs.getString("slug"),
            rs.getInt("thread_id"),
            rs.getTimestamp("created")
    );


    public static final RowMapper<UserModel> MAPPER_USER = (rs, rowNum) -> new UserModel(
            rs.getInt("id"),
            rs.getString("nickname"),
            rs.getString("email"),
            rs.getString("fullname"),
            rs.getString("about")
    );

    public static final RowMapper<ThreadModel> MAPPER_THREAD = (rs, rowNum) -> new ThreadModel(
            rs.getInt("id"),
            rs.getString("title"),
            rs.getString("nickname"),
            rs.getString("f_slug"),
            rs.getString("message"),
            rs.getInt("votes"),
            rs.getString("t_slug"),
            rs.getTimestamp("created")
    );

    public static final RowMapper<ForumModel> MAPPER_FORUM = (rs, rowNum) -> new ForumModel(
            rs.getLong("id"),
            rs.getString("title"),
            rs.getString("slug"),
            rs.getString("nickname"),
            rs.getInt("posts"),
            rs.getInt("threads")
    );
}
