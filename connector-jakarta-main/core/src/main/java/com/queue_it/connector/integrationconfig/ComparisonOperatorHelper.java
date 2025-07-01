package com.queue_it.connector.integrationconfig;

public final class ComparisonOperatorHelper {

    public static boolean evaluate(
            String opt,
            boolean isNegative,
            boolean isIgnoreCase,
            String value,
            String valueToCompare,
            String[] valuesToCompare) {

        value = (value != null) ? value : "";
        valueToCompare = (valueToCompare != null) ? valueToCompare : "";
        valuesToCompare = (valuesToCompare != null) ? valuesToCompare : new String[0];

        if (ComparisonOperatorType.EQUALS.equals(opt)) {
            return equals(value, valueToCompare, isNegative, isIgnoreCase);
        } else if (ComparisonOperatorType.CONTAINS.equals(opt)) {
            return contains(value, valueToCompare, isNegative, isIgnoreCase);
        } else if (ComparisonOperatorType.EQUALS_ANY.equals(opt)) {
            return equalsAny(value, valuesToCompare, isNegative, isIgnoreCase);
        } else if (ComparisonOperatorType.CONTAINS_ANY.equals(opt)) {
            return containsAny(value, valuesToCompare, isNegative, isIgnoreCase);
        } else {
            return false;
        }
    }

    private static boolean contains(String value, String valueToCompare, boolean isNegative, boolean ignoreCase) {
        if (valueToCompare.equals("*") && value != null && !value.isEmpty()) {
            return true;
        }
        boolean evaluation;
        if (ignoreCase) {
            evaluation = value.toUpperCase().contains(valueToCompare.toUpperCase());
        } else {
            evaluation = value.contains(valueToCompare);
        }
        if (isNegative) {
            return !evaluation;
        } else {
            return evaluation;
        }
    }

    private static boolean equals(String value, String valueToCompare, boolean isNegative, boolean ignoreCase) {
        boolean evaluation;

        if (ignoreCase) {
            evaluation = value.equalsIgnoreCase(valueToCompare);
        } else {
            evaluation = value.equals(valueToCompare);
        }

        if (isNegative) {
            return !evaluation;
        } else {
            return evaluation;
        }
    }

    private static boolean equalsAny(String value, String[] valuesToCompare, boolean isNegative, boolean isIgnoreCase) {
        for (String valueToCompare : valuesToCompare) {
            if (equals(value, valueToCompare, false, isIgnoreCase)) {
                return !isNegative;
            }
        }
        return isNegative;
    }

    private static boolean containsAny(String value, String[] valuesToCompare, boolean isNegative,
            boolean isIgnoreCase) {
        for (String valueToCompare : valuesToCompare) {
            if (contains(value, valueToCompare, false, isIgnoreCase)) {
                return !isNegative;
            }
        }
        return isNegative;
    }
}