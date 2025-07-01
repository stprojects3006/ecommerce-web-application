package com.queue_it.connector.helpers;

import java.net.URLEncoder;
import java.util.AbstractCollection;
import java.util.Iterator;

public class Utils {

    public static boolean isNullOrWhiteSpace(String str) {
        return str == null || str.trim().length() == 0;
    }

    public static boolean isInteger(String value) {
        try {
            Integer.valueOf(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isLong(String value) {
        try {
            Long.valueOf(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static String join(String delimiter, AbstractCollection<String> s) {
        if (s == null || s.isEmpty()) {
            return "";
        }
        Iterator<String> iter = s.iterator();
        StringBuilder builder = new StringBuilder(iter.next());
        while (iter.hasNext()) {
            builder.append(delimiter).append(iter.next());
        }
        return builder.toString();
    }

    public static String encodeUrl(String arg) {
        try {
            String value = URLEncoder.encode(arg, "UTF-8").replaceAll("\\+", "%20").replaceAll("&", "\\%26")
                    .replaceAll("!", "\\%21").replaceAll("'", "\\%27").replaceAll("\\(", "\\%28")
                    .replaceAll("~", "\\%7E").replaceAll("\\|", "\\%7C").replaceAll("\\)", "\\%29")
                    .replaceAll("\\*", "\\%2A");
            return value;
        } catch (Exception e) {
            return arg;
        }
    }
    
    public static StringBuilder getQueryStringStringBuilder(String sdkVersion, String customerId, String eventId,
            int configVersion,
            String actionName,
            String culture, String layoutName, String enqueueToken) throws Exception {

        StringBuilder queryStringBuilder = new StringBuilder("c=");
        queryStringBuilder.append(Utils.encodeUrl(customerId));

        queryStringBuilder.append("&");
        queryStringBuilder.append("e=");
        queryStringBuilder.append(Utils.encodeUrl(eventId));

        queryStringBuilder.append("&");
        queryStringBuilder.append("ver=");
        queryStringBuilder.append(Utils.encodeUrl(sdkVersion));

        queryStringBuilder.append("&");
        queryStringBuilder.append("cver=");
        queryStringBuilder.append(Utils.encodeUrl(String.valueOf(configVersion)));

        queryStringBuilder.append("&");
        queryStringBuilder.append("man=");
        queryStringBuilder.append(Utils.encodeUrl(actionName));

        if (!Utils.isNullOrWhiteSpace(culture)) {
            queryStringBuilder.append("&");
            queryStringBuilder.append("cid=");
            queryStringBuilder.append(Utils.encodeUrl(culture));
        }

        if (!Utils.isNullOrWhiteSpace(layoutName)) {
            queryStringBuilder.append("&");
            queryStringBuilder.append("l=");
            queryStringBuilder.append(Utils.encodeUrl(layoutName));
        }

        if (!isNullOrWhiteSpace(enqueueToken)) {
            queryStringBuilder.append("&");
            queryStringBuilder.append("enqueuetoken=");
            queryStringBuilder.append(Utils.encodeUrl(enqueueToken));
        }

        return queryStringBuilder;
    }
}

