package ru.max.forumDb.service;

class ServiceRequest {

    static final String truncateAll = "truncate Forum cascade; " +
            "truncate Posts cascade; " +
            "truncate Thread cascade; " +
            "truncate Users cascade; " +
            "truncate Vote cascade; " +
            "truncate Forum_User cascade; ";

    static final String countUsers = "select count(*) from Users";
    static final String countForum = "select count(*) from Forum";
    static final String countThread = "select count(*) from Thread";
    static final String countPosts = "select count(*) from Posts";

}
