package ru.max.forumDb.thread;


import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
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


    public ThreadService(JdbcTemplate jdbcTmp, ForumService forumService) {
        this.jdbcTmp = jdbcTmp;
        this.forumService = forumService;
    }

    void createPosts(ThreadModel thread, List<PostModel> listPosts) throws SQLException {
        Timestamp nowTime = Timestamp.valueOf(ZonedDateTime.now().toLocalDateTime());

        int threadId = thread.getId();


        try (Connection connection = jdbcTmp.getDataSource().getConnection()) {
            connection.setAutoCommit(false);

            ForumModel forum = (ForumModel) forumService.getInfoAboutForum(thread.getForum());

            try (PreparedStatement preparedStatement = connection.prepareStatement(ThreadRequest.insertIntoPosts,
                    Statement.NO_GENERATED_KEYS)) {

                for (PostModel post : listPosts) {

                    if (post.getParent() != 0) {
                        try {
                            final List<PostModel> posts = jdbcTmp.query(ThreadRequest.getPostByIdAndThreadId,
                                    MAPPER_POST, post.getParent(), thread.getId());
                            if (posts.isEmpty()) {
                                throw new SQLException();
                            }
                        } catch (Exception e) {
                            throw new SQLException();
                        }
                    }

                    UserModel user = UserService.getUserInfo(post.getAuthor());
                    post.setId(jdbcTmp.queryForObject(ThreadRequest.nextVal, Integer.class));

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

                String sqlUpdate = ThreadRequest.updateForum;
                jdbcTmp.update(sqlUpdate, listPosts.size(), thread.getForum());


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

    ThreadModel updateInfoThread(ThreadModel thread, ThreadModel threadInfo) {

        if (threadInfo.getTitle() == null) {
            threadInfo.setTitle(thread.getTitle());
        }

        if (threadInfo.getMessage() == null) {
            threadInfo.setMessage(thread.getMessage());
        }

        jdbcTmp.update(ThreadRequest.updateThread, threadInfo.getTitle(), threadInfo.getMessage(), thread.getId());

        thread.setTitle(threadInfo.getTitle());
        thread.setMessage(threadInfo.getMessage());
        return thread;
    }

    // поправить !!!!!!!!!
    ThreadModel addVote(ThreadModel thread, VoteModel vote) {

        UserModel user = UserService.getUserInfo(vote.getNickname());

        VoteModel realVote = null;

        try {
            realVote = jdbcTmp.queryForObject(ThreadRequest.selectVoteUser, (rs, rowNum) -> new VoteModel(
                    rs.getString("nickname"),
                    rs.getInt("voice")), vote.getNickname(), thread.getId()
            );
        } catch (EmptyResultDataAccessException e) {
//            throw new EmptyResultDataAccessException(1);
        }

        if (realVote == null) {

            jdbcTmp.update(ThreadRequest.insertVote, vote.getNickname(), thread.getId(), vote.getVoice());
            int votes = jdbcTmp.queryForObject(ThreadRequest.updateThreadVote,
                    Integer.class, thread.getId(), thread.getId());

            thread.setVotes(votes);

        } else {

            jdbcTmp.update(ThreadRequest.updateVote, vote.getVoice(), vote.getNickname(), thread.getId());
            int votes = jdbcTmp.queryForObject(ThreadRequest.updateThreadVote,
                    Integer.class, thread.getId(), thread.getId());

            thread.setVotes(votes);
        }

        return thread;
    }

    ThreadModel getThreadIdOrSlug(int id, String slugOrId) {
        return jdbcTmp.queryForObject(ThreadRequest.selectThreadByIdOrSlug, MAPPER_THREAD, id, slugOrId);
    }

    public ThreadModel getThread(String slug) {
        return jdbcTmp.queryForObject(ThreadRequest.selectThreadBySlug, MAPPER_THREAD, slug);
    }

    public List<ThreadModel> getThreadsForum(String slug, int limit, String since, boolean desc) {

        String sqlSort = !desc ? "asc" : "desc";
        String sign = !desc ? ">=" : "<=";

        Long forId = jdbcTmp.queryForObject(ThreadRequest.findForumBySlug, Long.class, slug);
        return jdbcTmp.query(ThreadRequest.getThreadsForum(since, sign, sqlSort, limit), MAPPER_THREAD, forId);
    }

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
}


