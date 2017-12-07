package ru.max.forumDb.post;

class PostRequest {

    static final String findPostById = "select Posts.id, Posts.parent, Posts.nickname, Posts.message, Posts.is_edited, Forum.slug, " +
            "Posts.thread_id, Posts.created from Posts " +
            "join Forum on Posts.forum_id = Forum.id " +
            "where Posts.id=?;";

    static final String findUserById = "select distinct Users.id, Users.nickname, Users.fullname, Users.email, Users.about " +
            "from Posts join Users on Posts.author_id = Users.id " +
            "where Posts.id=?;";

    static final String findForumById = "select Forum.id, Forum.title, Forum.slug, Users.nickname, Forum.posts, Forum.threads " +
            "from Posts " +
            "join Forum on Posts.forum_id = Forum.id " +
            "join Users on Forum.user_id = Users.id " +
            "where Posts.id=?;";

    static final String findThreadById = "select Thread.id, Thread.title, Users.nickname, Forum.slug as f_slug, Thread.message, " +
            "Thread.votes, Thread.slug as t_slug, Thread.created from Thread " +
            "join Forum on Thread.forum_id = Forum.id " +
            "join Users on Thread.author_id = Users.id " +
            "join Posts on Posts.thread_id = Thread.id " +
            "where Posts.id=?;";

    static final String updatePost = "update Posts " +
            " set ( message, is_edited ) = (?, ?) " +
            " where Posts.id=?";

    static String flatSort(int since, String sign, String sqlSort) {
        String sql = "select Posts.id, Posts.parent, Posts.nickname, Posts.message, Posts.is_edited, Forum.slug, " +
                "Posts.thread_id, Posts.created from Forum " +
                " join Posts on Posts.forum_id = Forum.id ";

        if (since != -1) {
            sql += " where (Posts.thread_id = ? and Posts.id " + sign + " " + since + " ) ";
        } else {
            sql += "where Posts.thread_id = ? ";
        }

        sql += "order by  Posts.created " + sqlSort + ",  Posts.id  " + sqlSort + " limit ? ;";

        return sql;
    }

    static String treeSort(int since, String sign, String sqlSort) {

        String sql = "select Posts.id, Posts.parent, Posts.nickname, Posts.message, Posts.is_edited, Forum.slug, " +
                "Posts.thread_id, Posts.created from Posts " +
                " join Forum on Posts.forum_id = Forum.id " +
                " where  Posts.thread_id = ? ";

        if (since != -1) {
            sql += " and Posts.path " + sign + " (select Posts.path from Posts where Posts.id = " + since + ") ";
        }
        sql += "order by  Posts.path " + sqlSort + ", Posts.id " + sqlSort + " limit ? ;";

        return sql;
    }

}
