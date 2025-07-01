package com.queue_it.connector.integrationconfig;

import java.net.MalformedURLException;
import java.net.URL;

public final class UrlValidatorHelper {

    public static boolean evaluate(TriggerPart triggerPart, String url) {
        return ComparisonOperatorHelper.evaluate(
                triggerPart.Operator,
                triggerPart.IsNegative,
                triggerPart.IsIgnoreCase,
                getUrlPart(triggerPart, url),
                triggerPart.ValueToCompare,
                triggerPart.ValuesToCompare);
    }

    private static String getUrlPart(TriggerPart triggerPart, String url) {
        if (UrlPartType.PAGE_URL.equals(triggerPart.UrlPart)) {
            return url;
        }

        try {
            URL oUrl = new URL(url);

            if (UrlPartType.PAGE_PATH.equals(triggerPart.UrlPart)) {
                return oUrl.getPath();
            } else if (UrlPartType.HOST_NAME.equals(triggerPart.UrlPart)) {
                return oUrl.getHost();
            } else {
                return "";
            }
        } catch (MalformedURLException ex) {
            return "";
        }
    }
}