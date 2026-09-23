package com.dreamstop.common.protocol;

import com.dreamstop.common.model.ResponseStatus;
import java.io.Serializable;

/**
 * Represents a response sent from the server back to the client.
 * Contains the response status (SUCCESS, ERROR, etc.), a status message, and
 * optional data in JSON.
 */
public class Response implements Serializable {
    private static final long serialVersionUID = 1L;

    private ResponseStatus status;
    private String message;
    private String dataJson;

    public Response() {
    }

    public Response(ResponseStatus status, String message, String dataJson) {
        this.status = status;
        this.message = message;
        this.dataJson = dataJson;
    }

    public static Response success(String message) {
        return new Response(ResponseStatus.SUCCESS, message, null);
    }

    public static <T> Response success(T dataObject, String message) {
        String json = dataObject != null ? JsonUtils.toJson(dataObject) : null;
        return new Response(ResponseStatus.SUCCESS, message, json);
    }

    public static Response error(String message) {
        return new Response(ResponseStatus.ERROR, message, null);
    }

    public static Response notFound(String message) {
        return new Response(ResponseStatus.NOT_FOUND, message, null);
    }

    public static Response unauthorized(String message) {
        return new Response(ResponseStatus.UNAUTHORIZED, message, null);
    }

    public static Response badRequest(String message) {
        return new Response(ResponseStatus.BAD_REQUEST, message, null);
    }

    public static Response conflict(String message) {
        return new Response(ResponseStatus.CONFLICT, message, null);
    }

    public boolean isSuccess() {
        return ResponseStatus.SUCCESS.equals(this.status);
    }

    public ResponseStatus getStatus() {
        return status;
    }

    public void setStatus(ResponseStatus status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDataJson() {
        return dataJson;
    }

    public void setDataJson(String dataJson) {
        this.dataJson = dataJson;
    }

    // Deserializes the response data JSON into the target object class
    public <T> T getDataAs(Class<T> targetClass) {
        if (dataJson == null || dataJson.trim().isEmpty()) {
            return null;
        }
        return JsonUtils.fromJson(dataJson, targetClass);
    }

    @Override
    public String toString() {
        return "Response{" +
                "status=" + status +
                ", message='" + message + '\'' +
                ", hasData=" + (dataJson != null && !dataJson.isBlank()) +
                '}';
    }
}
