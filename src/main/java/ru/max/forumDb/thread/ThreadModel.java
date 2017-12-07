package ru.max.forumDb.thread;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;

public class ThreadModel {

    private int id;
    private String title;
    private String author;  // author_id
    private String forum;   // forum_id
    private String message;
    private int votes;
    private String slug;
    private Timestamp created;


    @JsonCreator
    public ThreadModel( @JsonProperty("author") String author,
                        @JsonProperty("created") Timestamp created,
                        @JsonProperty("slug") String slug,
                        @JsonProperty("message") String message,
                        @JsonProperty("title") String title) {
        this.title = title;
        this.created = created == null ? Timestamp.valueOf(ZonedDateTime.now().toLocalDateTime()) : created;
        this.author = author;
        this.message = message;
        this.slug = slug;

    }

    public ThreadModel(String title, String author, String forum, String message, int votes, Timestamp created) {
        this.title = title;
        this.author = author;
        this.forum = forum;
        this.message = message;
        this.votes = votes;
        this.created = created;
    }

    public ThreadModel(int id, String title, String author, String forum, String message, int votes, String slug, Timestamp created) {
        this.id = id;
        this.slug = slug;
        this.title = title;
        this.author = author;
        this.forum = forum;
        this.message = message;
        this.votes = votes;
        this.created = created;
    }

    @JsonIgnore
    public JSONObject getJson() {
        final JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", id);
        jsonObject.put("title", title);
        jsonObject.put("author", author);
        jsonObject.put("forum", forum);
        jsonObject.put("votes", votes);
        jsonObject.put("slug", slug);
        jsonObject.put("created", created.toInstant().toString());
        jsonObject.put("message", message);

        return jsonObject;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getForum() {
        return forum;
    }

    public void setForum(String forum) {
        this.forum = forum;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getVotes() {
        return votes;
    }

    public void setVotes(int votes) {
        this.votes = votes;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    @JsonIgnore
    public Timestamp getCreated() {
        return created;
    }

    public void setCreated(Timestamp created) {
        this.created = created;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ThreadModel that = (ThreadModel) o;

        if (id != that.id) return false;
        if (votes != that.votes) return false;
        if (title != null ? !title.equals(that.title) : that.title != null) return false;
        if (author != null ? !author.equals(that.author) : that.author != null) return false;
        if (forum != null ? !forum.equals(that.forum) : that.forum != null) return false;
        if (message != null ? !message.equals(that.message) : that.message != null) return false;
        if (slug != null ? !slug.equals(that.slug) : that.slug != null) return false;
        return created != null ? created.equals(that.created) : that.created == null;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (author != null ? author.hashCode() : 0);
        result = 31 * result + (forum != null ? forum.hashCode() : 0);
        result = 31 * result + (message != null ? message.hashCode() : 0);
        result = 31 * result + votes;
        result = 31 * result + (slug != null ? slug.hashCode() : 0);
        result = 31 * result + (created != null ? created.hashCode() : 0);
        return result;
    }
}
