package com.proftest.common.dto;

import com.proftest.common.protocol.ResponseStatus;

import java.io.Serializable;

public class Response implements Serializable {

    private static final long serialVersionUID = 1L;

    private ResponseStatus status;
    private String message;
    private String payload;

    public Response() {}

    public Response(ResponseStatus status, String message, String payload) {
        this.status = status;
        this.message = message;
        this.payload = payload;
    }

    public static Response ok(String payload) {
        return new Response(ResponseStatus.SUCCESS, "OK", payload);
    }

    public static Response ok(String message, String payload) {
        return new Response(ResponseStatus.SUCCESS, message, payload);
    }

    public static Response error(String message) {
        return new Response(ResponseStatus.ERROR, message, null);
    }

    public static Response unauthorized(String message) {
        return new Response(ResponseStatus.UNAUTHORIZED, message, null);
    }

    public static Response forbidden() {
        return new Response(ResponseStatus.FORBIDDEN, "Access denied", null);
    }

    public static Response notFound(String message) {
        return new Response(ResponseStatus.NOT_FOUND, message, null);
    }

    public static Response conflict(String message) {
        return new Response(ResponseStatus.CONFLICT, message, null);
    }

    public boolean isSuccess() {
        return ResponseStatus.SUCCESS.equals(status);
    }

    public ResponseStatus getStatus() { return status; }
    public void setStatus(ResponseStatus status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }

    @Override
    public String toString() {
        return "Response{status=" + status + ", message=" + message + "}";
    }
}
