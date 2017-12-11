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

    public ForumService(JdbcTemplate template) {
        this.jdbcTmp = template;
    }

    ForumModel createForum(String title, String slug, String nickname) {

        UserModel user = UserService.getUserInfo(nickname);

        ForumModel forum = new ForumModel(title, user.getNickname(), slug);
        forum.setPosts(0);
        forum.setThreads(0);

        jdbcTmp.update(ForumRequest.insertIntoForum, forum.getTitle(), forum.getSlug(),
                user.getId(), forum.getPosts(), forum.getThreads());

        return forum;
    }

    public ForumModel getInfoAboutForum(String slug) {

        return jdbcTmp.queryForObject(ForumRequest.findForumBySlug, MAPPER_FORUM, slug);
    }

    ThreadModel createThread(ThreadModel threadUpdrate, String slug) {

        ForumModel forum = jdbcTmp.queryForObject(ForumRequest.findForumBySlug, MAPPER_FORUM, slug);
        if (forum == null) {
            throw new EmptyResultDataAccessException(1);
        }

        UserModel user = UserService.getUserInfo(threadUpdrate.getAuthor());

        threadUpdrate.setForum(forum.getSlug());

        threadUpdrate.setId(jdbcTmp.queryForObject(ForumRequest.insertIntoThread, Integer.class, threadUpdrate.getTitle(), user.getId(),
                forum.getId(), threadUpdrate.getMessage(), threadUpdrate.getCreated(), threadUpdrate.getSlug()));

        jdbcTmp.update(ForumRequest.insertIntoForumUsers, user.getId(), forum.getId());
        jdbcTmp.update(ForumRequest.updateForum, forum.getSlug());

        return threadUpdrate;
    }

    private static final RowMapper<ForumModel> MAPPER_FORUM = (rs, rowNum) -> new ForumModel(
            rs.getLong("id"),
            rs.getString("title"),
            rs.getString("slug"),
            rs.getString("nickname"),
            rs.getInt("posts"),
            rs.getInt("threads")
    );

}
