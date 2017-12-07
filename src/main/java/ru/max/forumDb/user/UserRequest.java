package ru.max.forumDb.user;

import org.jetbrains.annotations.Contract;

class UserRequest {

    static final String insertUser = "insert into Users (nickname, fullname, email, about) " +
            "values (?::citext, ?, ?::citext, ?)";

    static final String selectByNicknameOrEmail = "select nickname, fullname, email, about " +
            "from Users " +
            "where nickname = ?::citext or email = ?::citext ";

    static final String selectByNickname = "select id, nickname, fullname, about, email " +
            "from Users " +
            "where nickname = ?::citext ;";

    static final String updateByNickname = "update Users " +
            "set fullname = ?, about = ?, email = ? " +
            "where nickname = ?::citext returning *;";

    static final String selectIdForumBySlug = "select Forum.id " +
            "from Forum " +
            "where Forum.slug = ?::citext ;";


    @Contract(pure = true)
    static String getUsers(String since, String sign, String sqlSort) {
        String sql = "select id, nickname, fullname, about, email from users " +
                "where users.id in (select user_id from forum_user where forum_user.forum_id= ?) ";

        if (since != null) {
            sql += " and nickname" + sign + " '" + since + "'::citext";
        }

        sql += " order by nickname " + sqlSort + " limit ?;"; // убрать collate

        return sql;
    }
}
