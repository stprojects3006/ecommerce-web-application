package com.queue_it.connector.integrationconfig;

import com.queue_it.connector.IHttpRequest;

public final class CookieValidatorHelper {

    public static boolean evaluate(TriggerPart triggerPart, IHttpRequest request) {
        return ComparisonOperatorHelper.evaluate(triggerPart.Operator,
                triggerPart.IsNegative,
                triggerPart.IsIgnoreCase,
                request.getCookieValue(triggerPart.CookieName),
                triggerPart.ValueToCompare,
                triggerPart.ValuesToCompare);
    }
}