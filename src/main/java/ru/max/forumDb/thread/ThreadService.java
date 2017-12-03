package ru.max.forumDb.thread;


import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Service;

import ru.max.forumDb.forum.ForumModel;
import ru.max.forumDb.forum.ForumService;
import ru.max.forumDb.post.PostModel;
import ru.max.forumDb.user.UserModel;
import ru.max.forumDb.user.UserService;
import ru.max.forumDb.vote.VoteModel;

import java.sql.*;
import java.time.ZonedDateTime;
import java.util.List;

@Service
public class ThreadService {

    private final JdbcTemplate jdbcTmp;
    private final ForumService forumService;
    private final UserService userService;

    public ThreadService(JdbcTemplate jdbcTmp, ForumService forumService, UserService userService) {
        this.jdbcTmp = jdbcTmp;
        this.forumService = forumService;
        this.userService = userService;
    }

    public void createPosts(ThreadModel thread, List<PostModel> listPosts) throws SQLException {
        String sql = "insert into Posts (id, parent, author_id, message, forum_id, thread_id, created, path, nickname ) " +
                "values (?, ?,(select Users.id from Users where Users.nickname=?::citext),?,?,?,?, (SELECT path FROM posts WHERE id = ?) || ?, ?::citext)";

        Timestamp nowTime = Timestamp.valueOf(ZonedDateTime.now().toLocalDateTime());

        int threadId = thread.getId();


        try (Connection connection = jdbcTmp.getDataSource().getConnection()) {
            connection.setAutoCommit(false);

//            Connection connection = DataSourceUtils.getConnection(jdbcTmp.getDataSource());

            ForumModel forum = (ForumModel) forumService.findBySlug(thread.getForum());  // ??

//            PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            try (PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.NO_GENERATED_KEYS)) {

                for (PostModel post : listPosts) {

                    if (post.getParent() != 0) {
                        try {
                            final List<PostModel> posts = jdbcTmp.query(
                                    "select Posts.id, Posts.parent, Posts.nickname, Posts.message, " +
                                            " Posts.is_edited, Forum.slug, Posts.thread_id, Posts.created " +
                                            " from Forum " +
                                            " join Posts on Posts.forum_id = Forum.id " +
                                            " where Posts.id=? and Posts.thread_id=?", MAPPER_POST, post.getParent(), thread.getId());
                            if (posts.isEmpty()) {
                                throw new SQLException();
                            }
                        } catch (Exception e) {
                            throw new SQLException();
                        }
                    }

                    UserModel user = UserService.getUserInfo(post.getAuthor()); // ???
                    post.setId(jdbcTmp.queryForObject("SELECT nextval('posts_id_seq')", Integer.class));

                    post.setForum(thread.getForum());
                    post.setThreadId(thread.getId());
                    post.setCreated(nowTime);

                    preparedStatement.setInt(1, post.getId());
                    preparedStatement.setInt(2, post.getParent());
                    preparedStatement.setString(3, post.getAuthor());
                    preparedStatement.setString(4, post.getMessage());
                    preparedStatement.setLong(5, forum.getId());
                    preparedStatement.setInt(6, threadId);
                    preparedStatement.setTimestamp(7, post.getCreated());

                    if (post.getParent() != 0) {
                        preparedStatement.setInt(8, post.getParent());
                    } else {
                        preparedStatement.setInt(8, post.getId());
                    }
                    preparedStatement.setInt(9, post.getId());
                    preparedStatement.setString(10, post.getAuthor());

                    preparedStatement.addBatch();
                }
                preparedStatement.executeBatch();
                connection.commit();

                String sqlUpdrate = "UPDATE forum SET posts = posts + ? WHERE slug = ?::citext;";
                jdbcTmp.update(sqlUpdrate, listPosts.size(), thread.getForum());


            } catch (DataIntegrityViolationException e) {
                connection.rollback();
                throw new DataIntegrityViolationException("error");
            } catch (SQLException e) {
                connection.rollback();
                throw new SQLException();
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (DataIntegrityViolationException e) {
            throw new DataIntegrityViolationException("error");
        } catch (SQLException e) {
            throw new SQLException();
        }
    }


    public ThreadModel updateInfoThread(ThreadModel thread, ThreadModel threadInfo) {

        if (threadInfo.getTitle() == null) {
            threadInfo.setTitle(thread.getTitle());
        }

        if (threadInfo.getMessage() == null) {
            threadInfo.setMessage(thread.getMessage());
        }

        String sql = "update thread set title=?, message=? where id=?";

        jdbcTmp.update(sql, threadInfo.getTitle(), threadInfo.getMessage(), thread.getId());

        thread.setTitle(threadInfo.getTitle());
        thread.setMessage(threadInfo.getMessage());
        return thread;
    }


    // поправить !!!!!!!!!
    public ThreadModel addVote(ThreadModel thread, VoteModel vote) {

//        final String threadQuery = "UPDATE thread SET votes = votes + ? WHERE id = ? RETURNING *";

        UserModel user = userService.getUserInfo(vote.getNickname());

        String sqlGetVote = "SELECT users.nickname, vote.voice " +
                "FROM vote " +
                "JOIN users on users.id=vote.user_id " +
                "WHERE nickname = ?::citext AND vote.thread_id = ?;";

        VoteModel realVote = null;

        try {
            realVote = jdbcTmp.queryForObject(sqlGetVote, (rs, rowNum) -> new VoteModel(
                    rs.getString("nickname"),
                    rs.getInt("voice")), vote.getNickname(), thread.getId()
            );
        } catch (EmptyResultDataAccessException e) {
//            throw new EmptyResultDataAccessException(1);
        }

        if (realVote == null) {

            String sqlInsert = "INSERT INTO vote (user_id, thread_id, voice) " +
                    "VALUES ((SELECT id FROM users WHERE nickname = ?::citext), ?, ?);";

            jdbcTmp.update(sqlInsert, vote.getNickname(), thread.getId(), vote.getVoice());

            String sqlUpdrate = "UPDATE thread SET votes = (SELECT SUM(voice) FROM vote " +
                    "WHERE thread_id = ?) where thread.id=? RETURNING votes;";

            int votes = jdbcTmp.queryForObject(sqlUpdrate, Integer.class, thread.getId(), thread.getId());

            thread.setVotes(votes);

        } else {

            String sqlUpdrateVote = "UPDATE vote SET voice=? WHERE user_id=(SELECT id FROM users WHERE nickname = ?::citext) " +
                    " and thread_id=?;";

            jdbcTmp.update(sqlUpdrateVote, vote.getVoice(), vote.getNickname(), thread.getId());

            String sqlGetVotes = "UPDATE thread SET votes = (SELECT SUM(voice) FROM vote " +
                    "WHERE thread_id = ?) where thread.id=? RETURNING votes;";

            int votes = jdbcTmp.queryForObject(sqlGetVotes, Integer.class, thread.getId(), thread.getId());

            thread.setVotes(votes);
        }

        return thread;
    }

    public ThreadModel getThreadIdOrSlug(int id, String slugOrId) {
        final String sqlFindThread = "select Thread.id,Thread.title, Users.nickname, Forum.slug as f_slug,Thread.message,Thread.votes, Thread.slug as t_slug, Thread.created " +
                "from Thread " +
                "join Forum on Thread.forum_id = Forum.id " +
                "join Users on Thread.author_id = Users.id " +
                "where Thread.id = ? or Thread.slug=?::citext;";

        return jdbcTmp.queryForObject(sqlFindThread, MAPPER_THREAD, id, slugOrId);
    }

    public ThreadModel getThread(String slug) {
        final String sqlFindThread = "select Thread.id,Thread.title, Users.nickname, Forum.slug as f_slug,Thread.message,Thread.votes, Thread.slug as t_slug, Thread.created " +
                "from Thread " +
                "join Forum on Thread.forum_id = Forum.id " +
                "join Users on Thread.author_id = Users.id " +
                "where Thread.slug = ?::citext;";

        return jdbcTmp.queryForObject(sqlFindThread, MAPPER_THREAD, slug);
    }

    public ThreadModel getThreadById(int id) {
        final String sqlFindThread = "select Thread.id,Thread.title, Users.nickname, Forum.slug as f_slug,Thread.message,Thread.votes, Thread.slug as t_slug, Thread.created " +
                "from Thread " +
                "join Forum on Thread.forum_id = Forum.id " +
                "join Users on Thread.author_id = Users.id " +
                "where Thread.id = ?;";

        return jdbcTmp.queryForObject(sqlFindThread, MAPPER_THREAD, id);
    }

    public List<ThreadModel> getThreadsForum(String slug, int limit, String since, boolean desc) {
        final String sqlFindForumId = "select Forum.id from Forum where Forum.slug = ?::citext;";

        String sql = "select distinct Thread.id, Thread.title, Users.nickname, Forum.slug as f_slug, Thread.message, Thread.votes, Thread.slug as t_slug, Thread.created " +
                "from Thread " +
                "join Forum on Thread.forum_id = Forum.id " +
                "join Users on Thread.author_id = Users.id " +
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
        return jdbcTmp.query(sql, MAPPER_THREAD, forId);
    }

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
}


