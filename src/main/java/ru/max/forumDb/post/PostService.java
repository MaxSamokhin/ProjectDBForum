package ru.max.forumDb.post;

import org.json.JSONObject;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.max.forumDb.Message;
import ru.max.forumDb.forum.ForumModel;
import ru.max.forumDb.thread.ThreadModel;
import ru.max.forumDb.user.UserModel;

import java.sql.Timestamp;
import java.util.Arrays;

@Service
@Transactional
public class PostService {
    private final JdbcTemplate jdbcTmp;

    public PostService(JdbcTemplate jdbcTmp) {
        this.jdbcTmp = jdbcTmp;
    }

    public JSONObject getInfoPosts(int id, String related) {
        JSONObject jsonObject = new JSONObject();

        String sqlFindPostById = "select Posts.id, Posts.parent, Users.nickname, Posts.message, Posts.is_edited, Forum.slug, " +
                "Posts.thread_id, Posts.created from Posts " +
                "join Users on Posts.author = Users.id " +
                "join Forum on Posts.forum_id = Forum.id " +
                "where Posts.id=?;";
        String sqlFindUserById = "select distinct Users.id, Users.nickname, Users.fullname, Users.email, Users.about " +
                "from Posts join Users on Posts.author = Users.id " +
                "where Posts.id=?;";
        String sqlFindForumById = "select Forum.id, Forum.title, Forum.slug, Users.nickname, Forum.posts, Forum.threads " +
                "from Posts join Forum on Posts.forum_id = Forum.id " +
                "join Users on Forum.user_id = Users.id " +
                "where Posts.id=?;";
        String sqlFindThreadById = "select Thread.id, Thread.title, Users.nickname, Forum.slug as f_slug, Thread.message, " +
                "Thread.votes, Thread.slug as t_slug, Thread.created from Thread " +
                "join Users on Thread.author_id = Users.id " +
                "join Forum on Thread.forum_id = Forum.id " +
                "join Posts on Posts.thread_id = Thread.id " +
                "where Posts.id=?;";

        PostModel post = jdbcTmp.queryForObject(sqlFindPostById, (rs, rowNum) -> new PostModel(
                rs.getInt("id"),
                rs.getInt("parent"),
                rs.getString("nickname"),
                rs.getString("message"),
                rs.getBoolean("is_edited"),
                rs.getString("slug"),
                rs.getInt("thread_id"),
                rs.getTimestamp("created")), id);

        jsonObject.put("post", post.getJson());

        System.out.println("i am work 1");

        if (related != null && related.contains("user")) {
            UserModel user = jdbcTmp.queryForObject(sqlFindUserById, (rs, rowNum) -> new UserModel(
                    rs.getInt("id"),
                    rs.getString("nickname"),
                    rs.getString("email"),
                    rs.getString("fullname"),
                    rs.getString("about")), id);

            jsonObject.put("author", user.getJsonWithoutId());
        }

        System.out.println("i am work 2");

        if (related != null && related.contains("thread")) {
            ThreadModel thread = jdbcTmp.queryForObject(sqlFindThreadById, (rs, rowNum) -> new ThreadModel(
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("nickname"),
                    rs.getString("f_slug"),
                    rs.getString("message"),
                    rs.getInt("votes"),
                    rs.getString("t_slug"),
                    rs.getTimestamp("created")), id);

            jsonObject.put("thread", thread.getJson());
        }

        if (related != null &&  related.contains("forum")) {
            ForumModel forum = jdbcTmp.queryForObject(sqlFindForumById, (rs, rowNum) -> new ForumModel(
                    rs.getLong("id"),
                    rs.getString("title"),
                    rs.getString("slug"),
                    rs.getString("nickname"),
                    rs.getInt("posts"),
                    rs.getInt("threads")), id);

            jsonObject.put("forum", forum.getJson());
        }

        return jsonObject;
    }

    public PostModel update(int id, Message msg) {

        String sqlFindPostById = "select Posts.id, Posts.parent, Users.nickname, Posts.message, Posts.is_edited, Forum.slug, " +
                "Posts.thread_id, Posts.created from Posts " +
                "join Users on Posts.author = Users.id " +
                "join Forum on Posts.forum_id = Forum.id " +
                "where Posts.id=?;";

        String sqlUpdatePost = "update Posts set ( message, is_edited ) = (?, ?) " +
                "where Posts.id=?";

        PostModel post = jdbcTmp.queryForObject(sqlFindPostById, (rs, rowNum) -> new PostModel(
                rs.getInt("id"),
                rs.getInt("parent"),
                rs.getString("nickname"),
                rs.getString("message"),
                rs.getBoolean("is_edited"),
                rs.getString("slug"),
                rs.getInt("thread_id"),
                rs.getTimestamp("created")), id);

        if(!post.getMessage().equals(msg.getMessage())) {

            if (msg.getMessage() != null) {
                post.setEdited(true);
                post.setMessage(msg.getMessage());
            }

            msg.setMessage(post.getMessage());

            jdbcTmp.update(sqlUpdatePost, msg.getMessage(),post.isEdited(), id);
        }

        System.out.println("time:" + post.getCreated());

        return post;
    }
}
