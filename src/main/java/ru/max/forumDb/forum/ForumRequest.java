package ru.max.forumDb.forum;

class ForumRequest {

    static final  String insertIntoForum = "insert into Forum " +
            " (title,slug, user_id, posts, threads) " +
            " values (?, ?::citext, ?, ?, ?) ;";

    static final String findForumBySlug = "select Forum.id, Forum.title, Forum.slug," +
            " Users.nickname, Forum.posts, Forum.threads " +
            " from Forum join Users on Forum.user_id = Users.id " +
            " where Forum.slug = ?::citext";


    static final String insertIntoThread = "insert into thread " +
            " (title, author_id, forum_id, message, created, slug) " +
            " values (?,?,?,?,?,?::citext)  " +
            " returning id;";

    static final String updateForum = "update Forum " +
            " set threads = threads + 1 " +
            " where slug = ?::citext";

}
