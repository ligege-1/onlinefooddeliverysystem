package com.example.onlinefooddeliverysystem.data;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AddressManager {
    private static final String PREF_NAME = "address_store";
    private static final String CAMPUS_PREFIX = "成都理工大学宜宾校区";
    private static final AddressManager INSTANCE = new AddressManager();

    private final String defaultAddress = CAMPUS_PREFIX + " 南门";

    private AddressManager() {
    }

    public static AddressManager getInstance() {
        return INSTANCE;
    }

    public String getCampusPrefix() {
        return CAMPUS_PREFIX;
    }

    public String getDefaultAddress() {
        return defaultAddress;
    }

    public List<String> getAddresses(Context context) {
        ArrayList<String> allAddresses = new ArrayList<>();
        allAddresses.add(defaultAddress);
        allAddresses.addAll(getCustomAddresses(context));
        return allAddresses;
    }

    public List<String> getCustomAddresses(Context context) {
        String saved = prefs(context).getString(customAddressKey(context), "");
        ArrayList<String> addresses = new ArrayList<>();
        if (saved.trim().isEmpty()) {
            return addresses;
        }
        String[] parts = saved.split("\n");
        for (String part : parts) {
            String safePart = part.trim();
            if (!safePart.isEmpty() && !addresses.contains(safePart)) {
                addresses.add(safePart);
            }
        }
        return addresses;
    }

    public String getCurrentAddress(Context context) {
        String saved = prefs(context).getString(currentAddressKey(context), "");
        return saved.trim().isEmpty() ? defaultAddress : saved.trim();
    }

    public void setCurrentAddress(Context context, String address) {
        if (address != null && !address.trim().isEmpty()) {
            prefs(context).edit().putString(currentAddressKey(context), address.trim()).apply();
        }
    }

    public String getShortAddress(Context context) {
        return getCurrentAddress(context).replaceFirst("^" + CAMPUS_PREFIX + "\\s*", "").trim();
    }

    public String buildCustomAddress(String detail) {
        String safeDetail = detail == null ? "" : detail.trim();
        if (safeDetail.isEmpty()) {
            return "";
        }
        if (safeDetail.startsWith(CAMPUS_PREFIX)) {
            return safeDetail;
        }
        return CAMPUS_PREFIX + " " + safeDetail;
    }

    public String buildDormAddress(String building, String room) {
        String safeBuilding = normalizeBuilding(building);
        String safeRoom = room == null ? "" : room.trim();
        if (safeBuilding.isEmpty() || safeRoom.isEmpty()) {
            return "";
        }
        return CAMPUS_PREFIX + " " + safeBuilding + " " + safeRoom;
    }

    public boolean addCustomAddress(Context context, String address) {
        String safeAddress = address == null ? "" : address.trim();
        if (safeAddress.isEmpty()) {
            return false;
        }

        ArrayList<String> customAddresses = new ArrayList<>(getCustomAddresses(context));
        if (!customAddresses.contains(safeAddress) && !defaultAddress.equals(safeAddress)) {
            customAddresses.add(safeAddress);
        }

        prefs(context).edit()
                .putString(customAddressKey(context), joinAddresses(customAddresses))
                .putString(currentAddressKey(context), safeAddress)
                .apply();
        return true;
    }

    public boolean isValidBuilding(String building) {
        String normalized = normalizeBuilding(building);
        return normalized.matches("B[1-5]");
    }

    public boolean isValidRoom(String room) {
        if (room == null || !room.trim().matches("\\d{4}")) {
            return false;
        }
        int value = Integer.parseInt(room.trim());
        for (int base = 1000; base <= 6000; base += 1000) {
            if (value >= base && value <= base + 136) {
                return true;
            }
        }
        return false;
    }

    private SharedPreferences prefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    private String currentAddressKey(Context context) {
        return "current_address_" + UserManager.getInstance().getCurrentUserStorageKey(context);
    }

    private String customAddressKey(Context context) {
        return "custom_addresses_" + UserManager.getInstance().getCurrentUserStorageKey(context);
    }

    private String joinAddresses(List<String> addresses) {
        StringBuilder builder = new StringBuilder();
        for (String address : addresses) {
            if (builder.length() > 0) {
                builder.append('\n');
            }
            builder.append(address);
        }
        return builder.toString();
    }

    private String normalizeBuilding(String building) {
        if (building == null) {
            return "";
        }
        return building.trim().toUpperCase(Locale.ROOT);
    }
}
