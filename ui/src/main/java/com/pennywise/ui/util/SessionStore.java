package com.pennywise.ui.util;

import java.util.prefs.Preferences;

/**
 * Thread-safe session store. volatile ensures writes from background threads
 * are immediately visible to all other threads (JavaFX thread + OkHttp
 * threads).
 */
public class SessionStore {

    private static final Preferences PREFS = Preferences.userNodeForPackage(SessionStore.class);
    private static final String TOKEN_KEY = "token";
    private static final String EMAIL_KEY = "email";
    private static final String NAME_KEY = "name";
    private static final String INC_CATS_KEY = "incomeCategories";
    private static final String EXP_CATS_KEY = "expenseCategories";

    private static volatile String token = PREFS.get(TOKEN_KEY, null);
    private static volatile String email = PREFS.get(EMAIL_KEY, null);
    private static volatile String name = PREFS.get(NAME_KEY, null);

    public static String getToken() {
        return token;
    }

    public static void setToken(String t) {
        token = t;
        sync(TOKEN_KEY, t);
    }

    public static String getEmail() {
        return email;
    }

    public static void setEmail(String e) {
        email = e;
        sync(EMAIL_KEY, e);
    }

    public static String getName() {
        return name;
    }

    public static void setName(String n) {
        name = n;
        sync(NAME_KEY, n);
    }

    public static String getInitials() {
        String src = (name != null && !name.isEmpty()) ? name
                : (email != null ? email : "U");
        return src.substring(0, Math.min(2, src.length())).toUpperCase();
    }

    public static boolean isLoggedIn() {
        return token != null && !token.isEmpty();
    }

    public static void clear() {
        token = null;
        email = null;
        name = null;
        PREFS.remove(TOKEN_KEY);
        PREFS.remove(EMAIL_KEY);
        PREFS.remove(NAME_KEY);
    }

    public static java.util.List<String> getIncomeCategories(java.util.List<String> defaults) {
        return loadList(INC_CATS_KEY, defaults);
    }

    public static java.util.List<String> getExpenseCategories(java.util.List<String> defaults) {
        return loadList(EXP_CATS_KEY, defaults);
    }

    public static void setIncomeCategories(java.util.List<String> categories) {
        saveList(INC_CATS_KEY, categories);
    }

    public static void setExpenseCategories(java.util.List<String> categories) {
        saveList(EXP_CATS_KEY, categories);
    }

    public static String getScopedData(String key) {
        return PREFS.get(scopedKey(key), null);
    }

    public static void setScopedData(String key, String value) {
        String scopedKey = scopedKey(key);
        if (value == null || value.isBlank()) {
            PREFS.remove(scopedKey);
        } else {
            PREFS.put(scopedKey, value);
        }
    }

    public static void clearScopedData(String key) {
        PREFS.remove(scopedKey(key));
    }

    private static java.util.List<String> loadList(String key, java.util.List<String> defaults) {
        String raw = PREFS.get(scopedKey(key), null);
        if (raw == null || raw.isBlank()) {
            return new java.util.ArrayList<>(defaults);
        }
        java.util.List<String> items = new java.util.ArrayList<>();
        for (String item : raw.split("\\n")) {
            String trimmed = item.trim();
            if (!trimmed.isEmpty() && !items.contains(trimmed)) {
                items.add(trimmed);
            }
        }
        return items.isEmpty() ? new java.util.ArrayList<>(defaults) : items;
    }

    private static void saveList(String key, java.util.List<String> categories) {
        if (categories == null || categories.isEmpty()) {
            PREFS.remove(scopedKey(key));
            return;
        }
        PREFS.put(scopedKey(key), String.join("\n", categories));
    }

    private static String scopedKey(String key) {
        String scope = email != null && !email.isBlank() ? email.trim().toLowerCase() : "global";
        return scope + "." + key;
    }

    private static void sync(String key, String value) {
        if (value == null || value.isEmpty()) {
            PREFS.remove(key);
        } else {
            PREFS.put(key, value);
        }
    }
}
