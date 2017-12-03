package ru.max.forumDb.user;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private static JdbcTemplate jdbcTmp = null;

    public UserService(JdbcTemplate template) {
        this.jdbcTmp = template;
    }

    public UserModel createUser(String nickname, String fullname, String email, String about) {
        UserModel user = new UserModel(nickname, fullname, email, about);
        jdbcTmp.update("insert into Users (nickname, fullname, email, about) values (?, ?, ?, ?)",
                user.getNickname(), user.getFullName(), user.getEmail(), user.getAbout());

        return user;
    }

    public List<UserModel> findUsers(String nickname, String email) {
        final String sql = "select nickname, fullname, email, about from Users where nickname = ?::citext or email = ?::citext ";

        return jdbcTmp.query(sql, MAPPER_USER_WITHOUT_ID, nickname, email);
    }

    public static UserModel getUserInfo(String nickname) {
        final String sql = "select id, nickname, fullname, about, email from Users where nickname = ?::citext ;";
        return jdbcTmp.queryForObject(sql, MAPPER_USER, nickname);
    }

    public UserModel correctUserProfile(String nickname, UpdateDataUser updateData) {
        final String sql = "update Users set fullname = ?, about = ?, email = ? where nickname = ?::citext returning *;";

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


        return jdbcTmp.queryForObject(sql, MAPPER_USER_WITHOUT_ID,
                upUser.getFullname(), upUser.getAbout(), upUser.getEmail(), nickname);
    }


    public List<UserModel> getUsersForum(String slug, int limit, String since, boolean desc) {

        String sqlSort = !desc ? "asc" : "desc";
        String sign = !desc ? ">" : "<";

        final String sqlFindForumId = "select Forum.id from Forum where Forum.slug = ?::citext ;";

        String sql = "select id, nickname, fullname, about, email from users " +
                "where users.id in (select user_id from forum_user where forum_id= ?) ";

        if (since != null) {
            sql += " and lower( nickname COLLATE \"ucs_basic\" )" + sign + " lower('" + since + "') COLLATE \"ucs_basic\" ";  // ::citext
        }

        sql += " order by nickname::citext COLLATE \"ucs_basic\" " + sqlSort + " limit ?;";

        Long forId = jdbcTmp.queryForObject(sqlFindForumId, Long.class, slug);

        return jdbcTmp.query(sql, MAPPER_USER, forId, limit);
    }


    public static final RowMapper<UserModel> MAPPER_USER = (rs, rowNum) -> new UserModel(
            rs.getInt("id"),
            rs.getString("nickname"),
            rs.getString("email"),
            rs.getString("fullname"),
            rs.getString("about")
    );

    public static final RowMapper<UserModel> MAPPER_USER_WITHOUT_ID = (rs, rowNum) -> new UserModel(
            rs.getString("nickname"),
            rs.getString("fullname"),
            rs.getString("email"),
            rs.getString("about")
    );

}
