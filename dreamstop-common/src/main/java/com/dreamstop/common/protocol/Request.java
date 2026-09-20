package com.dreamstop.common.protocol;

import com.dreamstop.common.model.RequestType;
import java.io.Serializable;

public class Request implements Serializable {
    private static final long serialVersionUID = 1L;

    private RequestType type;
    private String token;
    private String payloadJson;

    public Request() {
    }

    public Request(RequestType type) {
        this(type, null, null);
    }

    public Request(RequestType type, String payloadJson) {
        this(type, null, payloadJson);
    }

    public Request(RequestType type, String token, String payloadJson) {
        this.type = type;
        this.token = token;
        this.payloadJson = payloadJson;
    }

    public static <T> Request of(RequestType type, T payloadObject) {
        return of(type, null, payloadObject);
    }

    public static <T> Request of(RequestType type, String token, T payloadObject) {
        String json = payloadObject != null ? JsonUtils.toJson(payloadObject) : null;
        return new Request(type, token, json);
    }

    public RequestType getType() {
        return type;
    }

    public void setType(RequestType type) {
        this.type = type;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getPayloadJson() {
        return payloadJson;
    }

    public void setPayloadJson(String payloadJson) {
        this.payloadJson = payloadJson;
    }

    public <T> T getPayloadAs(Class<T> targetClass) {
        if (payloadJson == null || payloadJson.trim().isEmpty()) {
            return null;
        }
        return JsonUtils.fromJson(payloadJson, targetClass);
    }
}
