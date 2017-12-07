package ru.max.forumDb.user;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UpdateDataUser that = (UpdateDataUser) o;

        if (fullname != null ? !fullname.equals(that.fullname) : that.fullname != null) return false;
        if (about != null ? !about.equals(that.about) : that.about != null) return false;
        return email != null ? email.equals(that.email) : that.email == null;
    }

    @Override
    public int hashCode() {
        int result = fullname != null ? fullname.hashCode() : 0;
        result = 31 * result + (about != null ? about.hashCode() : 0);
        result = 31 * result + (email != null ? email.hashCode() : 0);
        return result;
    }
}
