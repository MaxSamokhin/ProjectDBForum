package ru.max.forumDb.forum;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.max.forumDb.thread.ThreadModel;
import ru.max.forumDb.user.UserModel;
import ru.max.forumDb.user.UserService;

import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;

@Service
@Transactional
public class ForumService {

    private final JdbcTemplate jdbcTmp;
    private final UserService userService;

    public ForumService(JdbcTemplate template, UserService userService) {
        this.jdbcTmp = template;
        this.userService = userService;
    }

    public ForumModel createForum(String title, String slug, String nickname) {

        UserModel user = userService.getUserInfo(nickname);

        ForumModel forum = new ForumModel(title, user.getNickname(), slug);
        forum.setPosts(0);
        forum.setThreads(0);

        final String sql = "insert into Forum (title,slug, user_id, posts, threads) values (?, ?, ?, ?, ?) ;";
//        final String sqlFindIdUser = "select id from Users where lower(nickname) = lower(?) ;";
//
//
//        int id = jdbcTmp.queryForObject(sqlFindIdUser, Integer.class, forum.getUser());
//        System.out.println(forum.getTitle() + " " + forum.getSlug()+ " " + id+ " " + forum.getPosts()+ " " + forum.getThreads());

        jdbcTmp.update(sql, forum.getTitle(), forum.getSlug(), user.getId(), forum.getPosts(), forum.getThreads());

        return forum;
    }

    public Object findBySlug(String slug) {
        final String sql = "select Forum.id, Forum.title, Forum.slug, Users.nickname, Forum.posts, Forum.threads " +
                "from  Forum " +
                "join Users on Forum.user_id = Users.id " +
                "where slug = ?::citext;";

        return jdbcTmp.queryForObject(sql, (rs, rowNum) -> new ForumModel(
                rs.getLong("id"),
                rs.getString("title"),
                rs.getString("slug"),
                rs.getString("nickname"),
                rs.getInt("posts"),
                rs.getInt("threads")), slug);
    }

    public ForumModel getInfoAboutForum(String slug) {
        final String sql = "select Forum.id, Forum.title, Forum.slug, Users.nickname, Forum.posts, Forum.threads " +
                "from Forum join Users on Forum.user_id = Users.id " +
                "where Forum.slug = ?::citext";

        return jdbcTmp.queryForObject(sql, (rs, rowNum) -> new ForumModel(
                rs.getLong("id"),
                rs.getString("title"),
                rs.getString("slug"),
                rs.getString("nickname"),
                rs.getInt("posts"),
                rs.getInt("threads")), slug);
    }

    public ThreadModel createThread(ThreadModel threadUpdrate, String slug) {

        final String sqlGetForum = "select Forum.id, Forum.title, Forum.slug, Users.nickname, Forum.posts, Forum.threads " +
                "from Forum join Users on Forum.user_id = Users.id " +
                "where Forum.slug = ?::citext";

        System.out.println("111111");

        ForumModel forum = jdbcTmp.queryForObject(sqlGetForum, (rs, rowNum) -> new ForumModel(
                rs.getLong("id"),
                rs.getString("title"),
                rs.getString("slug"),
                rs.getString("nickname"),
                rs.getInt("posts"),
                rs.getInt("threads")), slug);
//        ForumModel forum = getInfoAboutForum(slug);

        if (forum == null) {
            throw new EmptyResultDataAccessException(1);
        }

        final String sqlGetUser = "select id, nickname, fullname, about, email from Users where nickname = ?::citext ;";

        System.out.println("222222");

        UserModel user = jdbcTmp.queryForObject(sqlGetUser, (rs, rowNum) -> new UserModel(
                rs.getInt("id"),
                rs.getString("nickname"),
                rs.getString("email"),
                rs.getString("fullname"),
                rs.getString("about")
        ), threadUpdrate.getAuthor());

//        UserModel user = userService.getUserInfo(threadUpdrate.getAuthor());

//        String sql = "select id from Thread where lower(title)= lower(?) or lower(slug) = lower(?)";

//        if (jdbcTmp.queryForObject(sql, Integer.class, threadUpdrate.getTitle()) == null) {
//            throw new DuplicateKeyException("error");
//        }

        String sqlInsertIntoThread = "insert into thread (title, author_id, forum_id, message, created, slug) " +
                "values (?,?,?,?,?,?)  RETURNING id;";


        String sqlUpdate = "UPDATE Forum SET threads = threads + 1 WHERE slug = ?::citext";

        System.out.println("33333333");
        System.out.println(threadUpdrate.getTitle() + " ");
        System.out.println(user.getId() + " " + forum.getId() + " ");
        System.out.println(threadUpdrate.getMessage() + " ");
        System.out.println(threadUpdrate.getCreated() + " ");
        System.out.println("slug: " + threadUpdrate.getSlug());

//        jdbcTmp.update(sqlInsertIntoThread, threadUpdrate.getTitle(), user.getId(), forum.getId(), threadUpdrate.getMessage(), threadUpdrate.getCreated(), threadUpdrate.getSlug());

        threadUpdrate.setForum(forum.getSlug());

        threadUpdrate.setId(jdbcTmp.queryForObject(sqlInsertIntoThread, Integer.class, threadUpdrate.getTitle(), user.getId(),
                forum.getId(), threadUpdrate.getMessage(), threadUpdrate.getCreated(), threadUpdrate.getSlug()));

        System.out.println("4444444");

        jdbcTmp.update(sqlUpdate, forum.getSlug());

        System.out.println("5555555");
//        int  thread_id = jdbcTmp.queryForObject(sql, Integer.class, threadUpdrate.getTitle(), threadUpdrate.getSlug());

        return threadUpdrate;
    }


    public ThreadModel getThread(String slug) {
        final String sqlFindThread = "select Thread.id,Thread.title, Users.nickname as u_name, Forum.slug as forum_slug,Thread.message,Thread.votes, Thread.slug, Thread.created " +
                "from Thread " +
                "join Users on Thread.author_id = Users.id " +
                "join Forum on Thread.forum_id = Forum.id " +
                "where Thread.slug = ?::citext;";

        return jdbcTmp.queryForObject(sqlFindThread, (rs, rowNum) -> new ThreadModel(
                rs.getInt("id"),
                rs.getString("title"),
                rs.getString("u_name"),
                rs.getString("forum_slug"),
                rs.getString("message"),
                rs.getInt("votes"),
                rs.getString("slug"),
                rs.getTimestamp("created")), slug);
    }

    public ThreadModel getThreadById(int id) {
        final String sqlFindThread = "select Thread.id,Thread.title, Users.nickname as u_name, Forum.slug as forum_slug,Thread.message,Thread.votes, Thread.slug, Thread.created " +
                "from Thread " +
                "join Users on Thread.author_id = Users.id " +
                "join Forum on Thread.forum_id = Forum.id " +
                "where Thread.id = ?::citext;";

        return jdbcTmp.queryForObject(sqlFindThread, (rs, rowNum) -> new ThreadModel(
                rs.getInt("id"),
                rs.getString("title"),
                rs.getString("u_name"),
                rs.getString("forum_slug"),
                rs.getString("message"),
                rs.getInt("votes"),
                rs.getString("slug"),
                rs.getTimestamp("created")), id);
    }

    public List<UserModel> getUsersForum(String slug, int limit, String since, boolean desc) {

        String sqlSort = !desc ? "asc" : "desc";
        String sign = !desc ? ">" : "<";

        final String sqlFindForumId = "select Forum.id from Forum where Forum.slug = ?::citext ;";

        String sql = " select * from ( " +
                " select distinct Users.id, Users.nickname, Users.fullname, Users.email, Users.about " +
                " from Users join Posts on ( Posts.author= Users.id and Posts.forum_id = ? )" +
                " union " +
                " select distinct Users.id, Users.nickname, Users.fullname, Users.email, Users.about " +
                "from Users join Thread on ( Thread.author_id = Users.id and Thread.forum_id=? ) ) as res_user";


        if (since != null) {
            sql += " where lower( res_user.nickname COLLATE \"ucs_basic\" )" + sign + " lower('" + since + "') COLLATE \"ucs_basic\" ";  // ::citext
        }

        sql += " order by res_user.nickname::citext COLLATE \"ucs_basic\" " + sqlSort + " limit ?;";

        Long forId = jdbcTmp.queryForObject(sqlFindForumId, Long.class, slug);

        return jdbcTmp.query(sql, (rs, rowNum) -> new UserModel(
                rs.getInt("id"),
                rs.getString("nickname"),
                rs.getString("email"),
                rs.getString("fullname"),
                rs.getString("about")), forId, forId, limit);
    }

    public List<ThreadModel> getThreadsForum(String slug, int limit, String since, boolean desc) {
        final String sqlFindForumId = "select Forum.id from Forum where lower(Forum.slug) = lower(?)";

        String sql = "select distinct Thread.id, Thread.title, Users.nickname, Forum.slug as f_slug, Thread.message, Thread.votes, Thread.slug as t_slug, Thread.created as time_create " +
                "from Thread join Users on Thread.author_id = Users.id " +
                "join Forum on Thread.forum_id = Forum.id " +
                "where Forum.id = ? ";

        if (since != null) {
            if (desc) {
                sql += " and Thread.created <= '" + since + "' ";
            } else {
                sql += " and Thread.created >= '" + since + "' ";
            }
        }

        sql += desc ? " order by Thread.created DESC " : " order by Thread.created  ASC ";

        if (limit != -1) {
            sql += " limit " + limit + ";";
        }

        Long forId = jdbcTmp.queryForObject(sqlFindForumId, Long.class, slug);
        System.out.println(forId);
        return jdbcTmp.query(sql, (rs, rowNum) -> new ThreadModel(
                rs.getInt("id"),
                rs.getString("title"),
                rs.getString("nickname"),
                rs.getString("f_slug"),
                rs.getString("message"),
                rs.getInt("votes"),
                rs.getString("t_slug"),
                rs.getTimestamp("time_create")), forId);
    }

}
