package ru.max.forumDb.vote;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

public class VoteModel {

    private String nickname;
    private int voice;

    @JsonCreator
    public VoteModel(@JsonProperty("nickname") @Nullable String nickname,
                     @JsonProperty("voice") int voice) {
        this.nickname = nickname;
        this.voice = voice;
    }

//    public VoteModel(String nickname, String voice) {
//        this.nickname = nickname;
//        this.voice = voice;
//    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public int getVoice() {
        return voice;
    }

    public void setVoice(int voice) {
        this.voice = voice;
    }

    public JSONObject getJson() {
        final JSONObject jsonObject = new JSONObject();
        jsonObject.put("nickname", nickname);
        jsonObject.put("voice", voice);

        return jsonObject;
    }


}
