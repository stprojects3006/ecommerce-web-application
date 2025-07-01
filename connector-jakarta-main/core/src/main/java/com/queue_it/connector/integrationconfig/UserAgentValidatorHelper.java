package com.queue_it.connector.integrationconfig;

public final class UserAgentValidatorHelper {

    public static boolean evaluate(TriggerPart triggerPart, String userAgent) {
        return ComparisonOperatorHelper.evaluate(triggerPart.Operator,
                triggerPart.IsNegative,
                triggerPart.IsIgnoreCase,
                userAgent,
                triggerPart.ValueToCompare,
                triggerPart.ValuesToCompare);
    }
}