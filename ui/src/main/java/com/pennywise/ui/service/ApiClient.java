package com.pennywise.ui.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.pennywise.ui.util.SessionStore;
import okhttp3.*;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class ApiClient {

    private static final String BASE = "http://localhost:8080/api";
    private static final MediaType JSON = MediaType.get("application/json");
    private static final OkHttpClient http = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();
    public static final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule()).disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    private static volatile String authToken;

    public static void setAuthToken(String token) {
        authToken = token;
    }

    public static void clearAuthToken() {
        authToken = null;
    }

    private static String tokenOrSession() {
        String tok = authToken;
        return (tok != null && !tok.isEmpty()) ? tok : SessionStore.getToken();
    }

    private static String requireAuthToken() throws IOException {
        String tok = tokenOrSession();
        if (tok == null || tok.isEmpty()) {
            throw new IOException("Not authenticated - please log in again");
        }
        return tok;
    }

    public static JsonNode get(String path) throws IOException {
        String tok = requireAuthToken();
        return exec(new Request.Builder().url(BASE + path).header("Authorization", "Bearer " + tok).get().build());
    }

    public static JsonNode post(String path, Object body) throws IOException {
        String tok = requireAuthToken();
        return exec(new Request.Builder().url(BASE + path).header("Authorization", "Bearer " + tok).post(RequestBody.create(mapper.writeValueAsString(body), JSON)).build());
    }

    public static JsonNode postNoAuth(String path, Object body) throws IOException {
        return exec(new Request.Builder().url(BASE + path).post(RequestBody.create(mapper.writeValueAsString(body), JSON)).build());
    }

    public static JsonNode patch(String path, Object body) throws IOException {
        return exec(new Request.Builder().url(BASE + path).header("Authorization", "Bearer " + requireAuthToken()).patch(RequestBody.create(mapper.writeValueAsString(body), JSON)).build());
    }

    public static void delete(String path) throws IOException {
        try (Response r = http.newCall(new Request.Builder().url(BASE + path).header("Authorization", "Bearer " + requireAuthToken()).delete().build()).execute()) {
            if (!r.isSuccessful()) {
                throw new IOException("HTTP " + r.code());
            }
        }
    }

    private static JsonNode exec(Request req) throws IOException {
        try (Response r = http.newCall(req).execute()) {
            String b = r.body() != null ? r.body().string() : "{}";
            if (!r.isSuccessful()) {
                throw new IOException("HTTP " + r.code() + ": " + b);
            }
            return mapper.readTree(b);
        }
    }
}
