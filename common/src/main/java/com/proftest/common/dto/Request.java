package com.proftest.common.dto;

import com.proftest.common.protocol.RequestType;

import java.io.Serializable;

public class Request implements Serializable {

    private static final long serialVersionUID = 1L;

    private RequestType type;
    private String token;
    private String payload;

    public Request() {}

    public Request(RequestType type, String payload) {
        this.type = type;
        this.payload = payload;
    }

    public Request(RequestType type, String token, String payload) {
        this.type = type;
        this.token = token;
        this.payload = payload;
    }

    public RequestType getType() { return type; }
    public void setType(RequestType type) { this.type = type; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }

    @Override
    public String toString() {
        return "Request{type=" + type + ", token=" + token + "}";
    }
}
