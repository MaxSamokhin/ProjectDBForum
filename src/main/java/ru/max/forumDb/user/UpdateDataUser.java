package ru.max.forumDb.user;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.Nullable;

public class UpdateDataUser {

    private String fullname;
    private String about;
    private String email;

    @JsonCreator
    public UpdateDataUser(@JsonProperty("fullname") String fullname,
                     @JsonProperty("email") String email,
                     @JsonProperty("about") String about) {
        this.fullname = fullname;
        this.email = email;
        this.fullname = fullname;
        this.about = about;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

}
