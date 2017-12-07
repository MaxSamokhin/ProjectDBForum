package ru.max.forumDb.user;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

public class UserModel {

    private int id;
    private String nickname;
    private String email;
    private String fullname;
    private String about;

    public UserModel(int id, String nickname, String email, String fullname, String about) {
        this.id = id;
        this.nickname = nickname;
        this.email = email;
        this.fullname = fullname;
        this.about = about;
    }

    public JSONObject getJsonWithoutId() {
        final JSONObject jsonObject = new JSONObject();
        jsonObject.put("nickname", nickname);
        jsonObject.put("fullname", fullname);
        jsonObject.put("about", about);
        jsonObject.put("email", email);

        return jsonObject;
    }

    @JsonCreator
    public UserModel(@JsonProperty("nickname") @Nullable String nickname,
                     @JsonProperty("fullname") String fullname,
                     @JsonProperty("email") String email,
                     @JsonProperty("about") String about) {
        this.nickname = nickname;
        this.email = email;
        this.fullname = fullname;
        this.about = about;
    }

    public String getNickname() {
        return nickname;
    }

    public String getEmail() {
        return email;
    }

    public String getFullName() {
        return fullname;
    }

    public String getAbout() {
        return about;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserModel userModel = (UserModel) o;

        if (id != userModel.id) return false;
        if (nickname != null ? !nickname.equals(userModel.nickname) : userModel.nickname != null) return false;
        if (email != null ? !email.equals(userModel.email) : userModel.email != null) return false;
        if (fullname != null ? !fullname.equals(userModel.fullname) : userModel.fullname != null) return false;
        return about != null ? about.equals(userModel.about) : userModel.about == null;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (nickname != null ? nickname.hashCode() : 0);
        result = 31 * result + (email != null ? email.hashCode() : 0);
        result = 31 * result + (fullname != null ? fullname.hashCode() : 0);
        result = 31 * result + (about != null ? about.hashCode() : 0);
        return result;
    }
}
