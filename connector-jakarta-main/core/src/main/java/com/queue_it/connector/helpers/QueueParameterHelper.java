package com.queue_it.connector.helpers;

public class QueueParameterHelper {

    public static final String TimeStampKey = "ts";
    public static final String ExtendableCookieKey = "ce";
    public static final String CookieValidityMinutesKey = "cv";
    public static final String HashKey = "h";
    public static final String HashedIpKey = "hip";
    public static final String QueueIdKey = "q";
    public static final String RedirectTypeKey = "rt";
    public static final String EventIdKey = "e";
    public static final String KeyValueSeparatorChar = "_";
    public static final String KeyValueSeparatorGroupChar = "~";

    public static QueueUrlParams extractQueueParams(String queueitToken) {

        if (Utils.isNullOrWhiteSpace(queueitToken)) {
            return null;
        }
        QueueUrlParams result = new QueueUrlParams();
        result.setQueueITToken(queueitToken);

        String[] paramList = queueitToken.split(KeyValueSeparatorGroupChar);
        for (String paramKeyValue : paramList) {
            String[] keyValueArr = paramKeyValue.split(KeyValueSeparatorChar);

            if (keyValueArr.length != 2)
                continue;

            if (HashKey.equals(keyValueArr[0])) {
                result.setHashCode(keyValueArr[1]);
            } else if (TimeStampKey.equals(keyValueArr[0])) {
                if (Utils.isLong(keyValueArr[1])) {
                    result.setTimeStamp(Long.parseLong(keyValueArr[1]));
                }
            } else if (CookieValidityMinutesKey.equals(keyValueArr[0])) {
                if (Utils.isInteger(keyValueArr[1])) {
                    result.setCookieValidityMinutes(Integer.parseInt(keyValueArr[1]));
                }
            } else if (EventIdKey.equals(keyValueArr[0])) {
                result.setEventId(keyValueArr[1]);
            } else if (QueueIdKey.equals(keyValueArr[0])) {
                result.setQueueId(keyValueArr[1]);
            } else if (ExtendableCookieKey.equals(keyValueArr[0])) {
                result.setExtendableCookie(Boolean.parseBoolean(keyValueArr[1]));
            } else if (RedirectTypeKey.equals(keyValueArr[0])) {
                result.setRedirectType(keyValueArr[1]);
            } else if (HashedIpKey.equals(keyValueArr[0])) {
                result.setHashedIp(keyValueArr[1]);
            }
        }
        String queueITTokenWithoutHash = result.getQueueITToken()
                .replace(KeyValueSeparatorGroupChar + HashKey + KeyValueSeparatorChar + result.getHashCode(), "");
        result.setQueueITTokenWithoutHash(queueITTokenWithoutHash);
        return result;
    }
}