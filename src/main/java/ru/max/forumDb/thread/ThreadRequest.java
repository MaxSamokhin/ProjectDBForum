package ru.max.forumDb.thread;

import org.jetbrains.annotations.Contract;

class ThreadRequest {

    static final String getPostByIdAndThreadId ="select Posts.id, Posts.parent, Posts.nickname, Posts.message, " +
            " Posts.is_edited, Forum.slug, Posts.thread_id, Posts.created " +
            " from Forum " +
            " join Posts on Posts.forum_id = Forum.id " +
            " where Posts.id=? and Posts.thread_id=?";

    static final String insertIntoPosts = "insert into Posts " +
            " (id, parent, author_id, message, forum_id, thread_id, created, path, nickname ) " +
            " values (?, ?," +
            " (select Users.id " +
            " from Users " +
            " where Users.nickname=?::citext)" +
            " ,?,?,?,?, " +
            " (select path " +
            " from posts " +
            " where id = ?) || ?, ?::citext)";

    static final String nextVal = "SELECT nextval('posts_id_seq')";

    static final String updateForum = "update forum " +
            "set posts = posts + ? " +
            "where slug = ?::citext ;";

    static final String updateThread = "update thread " +
            " set title=?, message=? " +
            " where id=?";

    static final String selectVoteUser = "select users.nickname, vote.voice " +
            "from vote " +
            "join users on users.id=vote.user_id " +
            "where nickname = ?::citext AND vote.thread_id = ?;";

    static final String insertVote = "insert into vote " +
            "(user_id, thread_id, voice) " +
            "values " +
            "((select id from users where nickname = ?::citext), ?, ?);";

    static final String updateThreadVote = "update thread " +
            "set votes = (select sum(voice) " +
            " from vote " +
            " where thread_id = ?)" +
            " where thread.id=? " +
            " returning votes;";

    static final String updateVote = "update vote " +
            " set voice=? " +
            " where user_id = (select id from users" +
            " where nickname = ?::citext) " +
            " and thread_id=?;";

    static final String selectThreadByIdOrSlug ="select Thread.id,Thread.title, Users.nickname, Forum.slug as f_slug," +
            " Thread.message,Thread.votes, Thread.slug as t_slug, Thread.created " +
            "from Thread " +
            "join Forum on Thread.forum_id = Forum.id " +
            "join Users on Thread.author_id = Users.id " +
            "where Thread.id = ? or Thread.slug=?::citext;";


    static final String selectThreadBySlug ="select Thread.id,Thread.title, Users.nickname, Forum.slug as f_slug," +
            "Thread.message,Thread.votes, Thread.slug as t_slug, Thread.created " +
            "from Thread " +
            "join Forum on Thread.forum_id = Forum.id " +
            "join Users on Thread.author_id = Users.id " +
            "where Thread.slug = ?::citext;";


    static final String findForumBySlug= "select Forum.id " +
            "from Forum " +
            "where Forum.slug = ?::citext;";

    static final String insertIntoForumUsers = "insert into forum_user (user_id, forum_id) " +
            " values (?, ?)";

    @Contract(pure = true)
    static String getThreadsForum(String since, String sign, String sqlSort, int limit) {
        String sql = "select distinct Thread.id, Thread.title, Users.nickname, Forum.slug as f_slug, Thread.message, Thread.votes, Thread.slug as t_slug, Thread.created " +
                "from Thread " +
                "join Forum on Thread.forum_id = Forum.id " +
                "join Users on Thread.author_id = Users.id " +
                "where Forum.id = ? ";

        if (since != null) {
            sql += " and Thread.created " +sign + "' " + since + "' ";
        }

        sql += " order by Thread.created " + sqlSort;

        if (limit != -1) {
            sql += " limit " + limit + ";";
        }

        return sql;
    }
}
