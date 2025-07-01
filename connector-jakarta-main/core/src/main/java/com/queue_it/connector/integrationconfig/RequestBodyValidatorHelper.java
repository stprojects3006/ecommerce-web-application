package com.queue_it.connector.integrationconfig;

public final class RequestBodyValidatorHelper {

    public static boolean evaluate(TriggerPart triggerPart, String requestBodyAsString) {
        return ComparisonOperatorHelper.evaluate(triggerPart.Operator,
                triggerPart.IsNegative,
                triggerPart.IsIgnoreCase,
                (requestBodyAsString != null && requestBodyAsString != "" ? requestBodyAsString : ""),
                triggerPart.ValueToCompare,
                triggerPart.ValuesToCompare);
    }
}