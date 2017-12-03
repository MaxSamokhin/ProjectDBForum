package ru.max.forumDb.forum;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import ru.max.forumDb.thread.ThreadModel;
import ru.max.forumDb.user.UserModel;
import ru.max.forumDb.user.UserService;


@Service
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

        final String sql = "insert into Forum (title,slug, user_id, posts, threads) values (?, ?::citext, ?, ?, ?) ;";
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

        return jdbcTmp.queryForObject(sql, MAPPER_FORUM, slug);
    }

    public ForumModel getInfoAboutForum(String slug) {
        final String sql = "select Forum.id, Forum.title, Forum.slug, Users.nickname, Forum.posts, Forum.threads " +
                "from Forum join Users on Forum.user_id = Users.id " +
                "where Forum.slug = ?::citext";

        return jdbcTmp.queryForObject(sql, MAPPER_FORUM, slug);
    }

    public ThreadModel createThread(ThreadModel threadUpdrate, String slug) {

        final String sqlGetForum = "select Forum.id, Forum.title, Forum.slug, Users.nickname, Forum.posts, Forum.threads " +
                "from Forum join Users on Forum.user_id = Users.id " +
                "where Forum.slug = ?::citext";


        ForumModel forum = jdbcTmp.queryForObject(sqlGetForum, MAPPER_FORUM, slug);
//        ForumModel forum = getInfoAboutForum(slug);

        if (forum == null) {
            throw new EmptyResultDataAccessException(1);
        }

//        final String sqlGetUser = "select id, nickname, fullname, about, email from Users where nickname = ?::citext ;";
//        UserModel user = jdbcTmp.queryForObject(sqlGetUser, MAPPER_USER, threadUpdrate.getAuthor());
        UserModel user = UserService.getUserInfo(threadUpdrate.getAuthor());

//        UserModel user = userService.getUserInfo(threadUpdrate.getAuthor());

//        String sql = "select id from Thread where lower(title)= lower(?) or lower(slug) = lower(?)";

//        if (jdbcTmp.queryForObject(sql, Integer.class, threadUpdrate.getTitle()) == null) {
//            throw new DuplicateKeyException("error");
//        }

        String sqlInsertIntoThread = "insert into thread (title, author_id, forum_id, message, created, slug) " +
                "values (?,?,?,?,?,?::citext)  RETURNING id;";


        String sqlUpdate = "UPDATE Forum SET threads = threads + 1 WHERE slug = ?::citext";

//        jdbcTmp.update(sqlInsertIntoThread, threadUpdrate.getTitle(), user.getId(), forum.getId(), threadUpdrate.getMessage(), threadUpdrate.getCreated(), threadUpdrate.getSlug());

        threadUpdrate.setForum(forum.getSlug());

        threadUpdrate.setId(jdbcTmp.queryForObject(sqlInsertIntoThread, Integer.class, threadUpdrate.getTitle(), user.getId(),
                forum.getId(), threadUpdrate.getMessage(), threadUpdrate.getCreated(), threadUpdrate.getSlug()));


        jdbcTmp.update(sqlUpdate, forum.getSlug());

//        int  thread_id = jdbcTmp.queryForObject(sql, Integer.class, threadUpdrate.getTitle(), threadUpdrate.getSlug());

        return threadUpdrate;
    }


    public static final RowMapper<ForumModel> MAPPER_FORUM = (rs, rowNum) -> new ForumModel(
            rs.getLong("id"),
            rs.getString("title"),
            rs.getString("slug"),
            rs.getString("nickname"),
            rs.getInt("posts"),
            rs.getInt("threads")
    );

}
