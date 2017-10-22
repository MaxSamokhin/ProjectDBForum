package ru.max.forumDb;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.json.JSONObject;

public class Error {

    private String typeError;
    private String message;

    public Error(String type, String msg) {
        message = msg;
        typeError = type;
    }

    public JSONObject getJsonError() {
        final JSONObject jsonObject = new JSONObject();
        jsonObject.put(typeError,message);

        return jsonObject;
    }
}
