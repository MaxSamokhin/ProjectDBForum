package ru.max.forumDb.post;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.time.ZonedDateTime;

public class PostModel {

    private int id;
    private Integer parent;
    private String nickname;
    private String message;
    private boolean isEdited;
    private String forum;  // forum_id
    private int threadId;
    private Timestamp created;

    @JsonCreator
    public PostModel(@JsonProperty("parent") int parent,
                      @JsonProperty("author") String nickname,
                      @JsonProperty("message") String message) {
        this.parent = parent;
        this.nickname = nickname;
        this.message = message;
    }

    public PostModel(int id, int parent, String nickname, String message, boolean isEdited, String forum, int threadId, Timestamp created) {
        this.id = id;
        this.parent = parent;
        this.nickname = nickname;
        this.message = message;
        this.isEdited = isEdited;
        this.forum = forum;
        this.threadId = threadId;

        this.created = created == null ? Timestamp.valueOf(ZonedDateTime.now().toLocalDateTime()) : created;
    }

    public JSONObject getJson() {
        final JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", id);
        jsonObject.put("parent", parent);
        jsonObject.put("author", nickname);
        jsonObject.put("message", message);
        jsonObject.put("isEdited", isEdited);
        jsonObject.put("forum", forum);
        jsonObject.put("created", created.toInstant().toString());
        jsonObject.put("thread", threadId);

        return jsonObject;
    }

    public int getId() {
        return id;
    }

    public Integer getParent() {
        return parent;
    }

    public String getAuthor() {
        return nickname;
    }

    public String getMessage() {
        return message;
    }

    public boolean isEdited() {
        return isEdited;
    }

    public String getForum() {
        return forum;
    }

    public int getThreadId() {
        return threadId;
    }

    @JsonIgnore
    public Timestamp getCreated() {
        return created;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setParent(int parent) {
        this.parent = parent;
    }

    public void setAuthor(String nickname) {
        this.nickname = nickname;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setEdited(boolean edited) {
        isEdited = edited;
    }

    public void setForum(String forum) {
        this.forum = forum;
    }

    public void setThreadId(int threadId) {
        this.threadId = threadId;
    }

    public void setCreated(Timestamp created) {
        this.created = created;
    }

}
