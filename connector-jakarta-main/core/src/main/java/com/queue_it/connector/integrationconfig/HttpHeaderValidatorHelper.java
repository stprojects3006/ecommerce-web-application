package com.queue_it.connector.integrationconfig;

public final class HttpHeaderValidatorHelper {

    public static boolean evaluate(TriggerPart triggerPart, String headerValue) {
        return ComparisonOperatorHelper.evaluate(triggerPart.Operator,
                triggerPart.IsNegative,
                triggerPart.IsIgnoreCase,
                headerValue,
                triggerPart.ValueToCompare,
                triggerPart.ValuesToCompare);
    }
}