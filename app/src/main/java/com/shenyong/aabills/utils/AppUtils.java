package com.shenyong.aabills.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageManager;

import com.sddy.utils.ArrayUtils;
import com.shenyong.aabills.AABilsApp;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class AppUtils {
    public static String getVersionName() {
        String versionName = "";
        Context context = AABilsApp.getInstance();
        try {
            versionName = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionName;
    }

    public static int getVersionCode() {
        int versionCodee = 0;
        Context context = AABilsApp.getInstance();
        try {
            versionCodee = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionCodee;
    }

    public static boolean isServiceRunning(Context context, String name) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> serviceInfos = am.getRunningServices(100);
        if (ArrayUtils.isEmpty(serviceInfos)) {
            return false;
        }
        boolean running = false;
        for (ActivityManager.RunningServiceInfo info : serviceInfos) {
            running = info.service.getClassName().equals(name);
            if (running) {
                break;
            }
        }

        return running;
    }

    /**
     * Regex which matches any of the sequences that we need to fix up after
     * URLEncoder.encode().
     */
    private static final Pattern ENCODED_CHARACTERS_PATTERN;
    static {
        StringBuilder pattern = new StringBuilder();

        pattern
                .append(Pattern.quote("+"))
                .append("|")
                .append(Pattern.quote("*"))
                .append("|")
                .append(Pattern.quote("%7E"))
                .append("|")
                .append(Pattern.quote("%2F"));

        ENCODED_CHARACTERS_PATTERN = Pattern.compile(pattern.toString());
    }

    public static String urlEncode(final String value, final boolean path) {
        if (value == null) {
            return "";
        }

        try {
            String encoded = URLEncoder.encode(value, StandardCharsets.UTF_8.name());

            Matcher matcher = ENCODED_CHARACTERS_PATTERN.matcher(encoded);
            StringBuffer buffer = new StringBuffer(encoded.length());

            while (matcher.find()) {
                String replacement = matcher.group(0);

                if ("+".equals(replacement)) {
                    replacement = "%20";
                } else if ("*".equals(replacement)) {
                    replacement = "%2A";
                } else if ("%7E".equals(replacement)) {
                    replacement = "~";
                } else if (path && "%2F".equals(replacement)) {
                    replacement = "/";
                }

                matcher.appendReplacement(buffer, replacement);
            }

            matcher.appendTail(buffer);
            return buffer.toString();

        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }
    }
}
