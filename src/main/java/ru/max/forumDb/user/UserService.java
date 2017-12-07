package ru.max.forumDb.user;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private static JdbcTemplate jdbcTmp = null;

    public UserService(JdbcTemplate template) {
        jdbcTmp = template;
    }

    UserModel createUser(String nickname, String fullname, String email, String about) {
        UserModel user = new UserModel(nickname, fullname, email, about);
        jdbcTmp.update(UserRequest.insertUser, user.getNickname(), user.getFullName(), user.getEmail(), user.getAbout());
        return user;
    }

    List<UserModel> findUsers(String nickname, String email) {
        return jdbcTmp.query(UserRequest.selectByNicknameOrEmail, MAPPER_USER_WITHOUT_ID, nickname, email);
    }

    public static UserModel getUserInfo(String nickname) {
        return jdbcTmp.queryForObject(UserRequest.selectByNickname, MAPPER_USER, nickname);
    }

    UserModel correctUserProfile(String nickname, UpdateDataUser updateData) {
        final UserModel upUser = getUserInfo(nickname);

        if (updateData.getAbout() != null) {
            upUser.setAbout(updateData.getAbout());
        }

        if (updateData.getEmail() != null) {
            upUser.setEmail(updateData.getEmail());
        }

        if (updateData.getFullname() != null) {
            upUser.setFullname(updateData.getFullname());
        }

        return jdbcTmp.queryForObject(UserRequest.updateByNickname, MAPPER_USER_WITHOUT_ID,
                upUser.getFullname(), upUser.getAbout(), upUser.getEmail(), nickname);
    }


    public List<UserModel> getUsersForum(String slug, int limit, String since, boolean desc) {

        String sqlSort = !desc ? "asc" : "desc";
        String sign = !desc ? ">" : "<";

        Long forId = jdbcTmp.queryForObject(UserRequest.selectIdForumBySlug, Long.class, slug);

        return jdbcTmp.query(UserRequest.getUsers(since,sign,sqlSort), MAPPER_USER, forId, limit);
    }


    private static final RowMapper<UserModel> MAPPER_USER = (rs, rowNum) -> new UserModel(
            rs.getInt("id"),
            rs.getString("nickname"),
            rs.getString("email"),
            rs.getString("fullname"),
            rs.getString("about")
    );

    private static final RowMapper<UserModel> MAPPER_USER_WITHOUT_ID = (rs, rowNum) -> new UserModel(
            rs.getString("nickname"),
            rs.getString("fullname"),
            rs.getString("email"),
            rs.getString("about")
    );

}
