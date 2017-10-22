package ru.max.forumDb.forum;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

public class ForumModel {

    private Long id;
    private String title;
    private String slug;
    private String user;  // user_id
    private int posts;
    private int threads;

    @JsonCreator
    public ForumModel(@JsonProperty("title") String title,
                      @JsonProperty("user") String user,
                      @JsonProperty("slug") String slug) {
        this.title = title;
        this.slug = slug;
        this.user = user;
    }

    public ForumModel(Long id, String title, String slug, String user, int posts, int threads) {
        this.id = id;
        this.title = title;
        this.slug = slug;
        this.user = user;
        this.posts = posts;
        this.threads = threads;
    }

    public JSONObject getJson() {
        final JSONObject jsonObject = new JSONObject();
        jsonObject.put("title", title);
        jsonObject.put("user", user);
        jsonObject.put("slug", slug);
        jsonObject.put("posts", posts);
        jsonObject.put("threads", threads);
        return jsonObject;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getSlug() {
        return slug;
    }

    public String getUser() {
        return user;
    }

    public int getPosts() {
        return posts;
    }

    public int getThreads() {
        return threads;
    }


    public void setId(Long id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setPosts(int posts) {
        this.posts = posts;
    }

    public void setThreads(int threads) {
        this.threads = threads;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ForumModel that = (ForumModel) o;

        if (posts != that.posts) return false;
        if (threads != that.threads) return false;
        if (!id.equals(that.id)) return false;
        if (!title.equals(that.title)) return false;
        if (!slug.equals(that.slug)) return false;
        return user.equals(that.user);
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + title.hashCode();
        result = 31 * result + slug.hashCode();
        result = 31 * result + user.hashCode();
        result = 31 * result + posts;
        result = 31 * result + threads;
        return result;
    }

}
