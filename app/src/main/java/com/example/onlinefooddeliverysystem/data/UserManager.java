package com.example.onlinefooddeliverysystem.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.onlinefooddeliverysystem.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class UserManager {
    private static final String PREF_NAME = "user_session";
    private static final String KEY_LOGGED_IN = "logged_in";
    private static final String KEY_CURRENT_USER = "current_user";
    private static final String KEY_ACCOUNT_LIST = "account_list";

    private static final String LEGACY_KEY_USERNAME = "username";
    private static final String LEGACY_KEY_PASSWORD = "password";
    private static final String LEGACY_KEY_AVATAR = "avatar";

    public static final String AVATAR_TOM = "tom";
    public static final String AVATAR_XIAOXIN = "xiaoxin";
    public static final String AVATAR_JERRY = "jerry";
    public static final String AVATAR_PIG = "pig";
    public static final String AVATAR_DEFAULT = "default";

    private static final UserManager INSTANCE = new UserManager();

    private UserManager() {
    }

    public static UserManager getInstance() {
        return INSTANCE;
    }

    public boolean isLoggedIn(Context context) {
        migrateLegacyAccountIfNeeded(context);
        return prefs(context).getBoolean(KEY_LOGGED_IN, false) && !getCurrentUsername(context).isEmpty();
    }

    public boolean hasAccount(Context context) {
        migrateLegacyAccountIfNeeded(context);
        return !getAccountUsernames(context).isEmpty();
    }

    public boolean accountExists(Context context, String username) {
        migrateLegacyAccountIfNeeded(context);
        String safeUsername = normalizeUsername(username);
        if (safeUsername.isEmpty()) {
            return false;
        }
        return getAccountUsernames(context).contains(safeUsername);
    }

    public String getUsername(Context context) {
        migrateLegacyAccountIfNeeded(context);
        return getCurrentUsername(context);
    }

    public String getAvatarKey(Context context) {
        migrateLegacyAccountIfNeeded(context);
        String currentUsername = getCurrentUsername(context);
        if (currentUsername.isEmpty()) {
            return AVATAR_DEFAULT;
        }
        return prefs(context).getString(accountAvatarKey(currentUsername), AVATAR_DEFAULT);
    }

    public int getAvatarResId(Context context) {
        return getAvatarResIdByKey(getAvatarKey(context));
    }

    public int getAvatarResIdByKey(String avatarKey) {
        if (AVATAR_DEFAULT.equals(avatarKey)) {
            return R.drawable.avatar_default;
        }
        if (AVATAR_TOM.equals(avatarKey)) {
            return R.drawable.avatar_tom;
        }
        if (AVATAR_XIAOXIN.equals(avatarKey)) {
            return R.drawable.avatar_xiaoxin;
        }
        if (AVATAR_JERRY.equals(avatarKey)) {
            return R.drawable.avatar_jerry;
        }
        return R.drawable.avatar_pig;
    }

    public void updateAvatar(Context context, String avatarKey) {
        migrateLegacyAccountIfNeeded(context);
        String currentUsername = getCurrentUsername(context);
        if (currentUsername.isEmpty()) {
            return;
        }
        prefs(context).edit().putString(accountAvatarKey(currentUsername), avatarKey).apply();
    }

    public boolean register(Context context, String username, String password) {
        migrateLegacyAccountIfNeeded(context);
        String safeUsername = normalizeUsername(username);
        String safePassword = password == null ? "" : password.trim();
        if (safeUsername.isEmpty() || safePassword.isEmpty()) {
            return false;
        }
        if (accountExists(context, safeUsername)) {
            return false;
        }

        ArrayList<String> usernames = new ArrayList<>(getAccountUsernames(context));
        usernames.add(safeUsername);

        prefs(context).edit()
                .putString(KEY_ACCOUNT_LIST, joinAccounts(usernames))
                .putString(accountPasswordKey(safeUsername), safePassword)
                .putString(accountAvatarKey(safeUsername), AVATAR_DEFAULT)
                .apply();
        return true;
    }

    public boolean login(Context context, String username, String password) {
        migrateLegacyAccountIfNeeded(context);
        String safeUsername = normalizeUsername(username);
        String safePassword = password == null ? "" : password.trim();
        if (safeUsername.isEmpty() || safePassword.isEmpty()) {
            return false;
        }

        SharedPreferences preferences = prefs(context);
        String savedPassword = preferences.getString(accountPasswordKey(safeUsername), "");
        boolean success = safePassword.equals(savedPassword);
        if (success) {
            preferences.edit()
                    .putBoolean(KEY_LOGGED_IN, true)
                    .putString(KEY_CURRENT_USER, safeUsername)
                    .apply();
        }
        return success;
    }

    public void logout(Context context) {
        prefs(context).edit().putBoolean(KEY_LOGGED_IN, false).apply();
    }

    public String getCurrentUserStorageKey(Context context) {
        migrateLegacyAccountIfNeeded(context);
        String currentUsername = getCurrentUsername(context);
        return currentUsername.isEmpty() ? "guest" : sanitizeKey(currentUsername);
    }

    private void migrateLegacyAccountIfNeeded(Context context) {
        SharedPreferences preferences = prefs(context);
        String legacyUsername = normalizeUsername(preferences.getString(LEGACY_KEY_USERNAME, ""));
        String legacyPassword = preferences.getString(LEGACY_KEY_PASSWORD, "");
        boolean hasLegacyAccount = !legacyUsername.isEmpty() && !legacyPassword.trim().isEmpty();
        boolean alreadyMigrated = !preferences.getString(KEY_ACCOUNT_LIST, "").trim().isEmpty();
        if (!hasLegacyAccount || alreadyMigrated) {
            return;
        }

        String legacyAvatar = preferences.getString(LEGACY_KEY_AVATAR, AVATAR_DEFAULT);
        preferences.edit()
                .putString(KEY_ACCOUNT_LIST, legacyUsername)
                .putString(KEY_CURRENT_USER, legacyUsername)
                .putString(accountPasswordKey(legacyUsername), legacyPassword.trim())
                .putString(accountAvatarKey(legacyUsername), legacyAvatar.isEmpty() ? AVATAR_DEFAULT : legacyAvatar)
                .apply();
    }

    private List<String> getAccountUsernames(Context context) {
        String saved = prefs(context).getString(KEY_ACCOUNT_LIST, "");
        ArrayList<String> usernames = new ArrayList<>();
        if (saved.trim().isEmpty()) {
            return usernames;
        }
        String[] parts = saved.split("\n");
        for (String part : parts) {
            String safePart = normalizeUsername(part);
            if (!safePart.isEmpty() && !usernames.contains(safePart)) {
                usernames.add(safePart);
            }
        }
        return usernames;
    }

    private String getCurrentUsername(Context context) {
        return normalizeUsername(prefs(context).getString(KEY_CURRENT_USER, ""));
    }

    private String accountPasswordKey(String username) {
        return "password_" + sanitizeKey(username);
    }

    private String accountAvatarKey(String username) {
        return "avatar_" + sanitizeKey(username);
    }

    private String sanitizeKey(String username) {
        return normalizeUsername(username).toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_\\-]", "_");
    }

    private String normalizeUsername(String username) {
        return username == null ? "" : username.trim();
    }

    private String joinAccounts(List<String> usernames) {
        StringBuilder builder = new StringBuilder();
        for (String username : usernames) {
            if (builder.length() > 0) {
                builder.append('\n');
            }
            builder.append(username);
        }
        return builder.toString();
    }

    private SharedPreferences prefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }
}
