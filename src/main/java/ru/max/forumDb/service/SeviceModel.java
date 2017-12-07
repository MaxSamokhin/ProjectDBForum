package ru.max.forumDb.service;

import org.json.JSONObject;

public class SeviceModel {

    private int user;
    private int forum;
    private int thread;
    private int post;


    public SeviceModel(int user, int forum, int thread, int post) {
        this.user = user;
        this.forum = forum;
        this.thread = thread;
        this.post = post;
    }

    public JSONObject getJson() {
        final JSONObject jsonObject = new JSONObject();
        jsonObject.put("user", user);
        jsonObject.put("forum", forum);
        jsonObject.put("thread", thread);
        jsonObject.put("post", post);

        return jsonObject;
    }

    public int getUser() {
        return user;
    }

    public void setUser(int user) {
        this.user = user;
    }

    public int getForum() {
        return forum;
    }

    public void setForum(int forum) {
        this.forum = forum;
    }

    public int getThread() {
        return thread;
    }

    public void setThread(int thread) {
        this.thread = thread;
    }

    public int getPost() {
        return post;
    }

    public void setPost(int post) {
        this.post = post;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SeviceModel that = (SeviceModel) o;

        if (user != that.user) return false;
        if (forum != that.forum) return false;
        if (thread != that.thread) return false;
        return post == that.post;
    }

    @Override
    public int hashCode() {
        int result = user;
        result = 31 * result + forum;
        result = 31 * result + thread;
        result = 31 * result + post;
        return result;
    }
}
