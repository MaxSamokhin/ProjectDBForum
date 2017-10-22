package ru.max.forumDb.user;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UserService {

    private final JdbcTemplate jdbcTmp;

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

        return jdbcTmp.query(sql, (rs, rowNum) -> new UserModel(
                rs.getString("nickname"),
                rs.getString("fullname"),
                rs.getString("email"),
                rs.getString("about")
        ), nickname, email);
    }

    public UserModel getUserInfo(String nickname) {
        final String sql = "select id, nickname, fullname, about, email from Users where nickname = ?::citext ;";
        return jdbcTmp.queryForObject(sql, (rs, rowNum) -> new UserModel(
                rs.getInt("id"),
                rs.getString("nickname"),
                rs.getString("email"),
                rs.getString("fullname"),
                rs.getString("about")
        ), nickname);
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


        return jdbcTmp.queryForObject(sql, (rs, rowNum) -> new UserModel(
                rs.getString("nickname"),
                rs.getString("fullname"),
                rs.getString("email"),
                rs.getString("about")
        ), upUser.getFullname(), upUser.getAbout(), upUser.getEmail(), nickname);
    }
}
