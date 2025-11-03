package com.safe_jeonse.server.util;

public final class LnbrUtils {

    private LnbrUtils() {}

    public static String padTo4(String value) {
        if (value == null) return "0000";
        String v = value.trim();
        if (v.isEmpty() || "null".equalsIgnoreCase(v)) return "0000";
        v = v.replaceAll("\\D", "");
        if (v.isEmpty()) return "0000";
        if (v.length() > 4) v = v.substring(v.length() - 4);
        int pad = 4 - v.length();
        if (pad <= 0) return v;
        StringBuilder sb = new StringBuilder(4);
        for (int i = 0; i < pad; i++) sb.append('0');
        sb.append(v);
        return sb.toString();
    }
}

