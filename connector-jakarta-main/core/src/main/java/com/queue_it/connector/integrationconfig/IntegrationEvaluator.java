package com.queue_it.connector.integrationconfig;

import com.queue_it.connector.IHttpRequest;

interface IIntegrationEvaluator {

    IntegrationConfigModel getMatchedIntegrationConfig(
            CustomerIntegration customerIntegration,
            String currentPageUrl,
            IHttpRequest request) throws Exception;
}

public class IntegrationEvaluator implements IIntegrationEvaluator {

    @Override
    public IntegrationConfigModel getMatchedIntegrationConfig(
            CustomerIntegration customerIntegration,
            String currentPageUrl,
            IHttpRequest request) throws Exception {

        if (request == null) {
            throw new Exception("request is null");
        }

        for (IntegrationConfigModel integration : customerIntegration.Integrations) {
            for (TriggerModel trigger : integration.Triggers) {
                if (evaluateTrigger(trigger, currentPageUrl, request)) {
                    return integration;
                }
            }
        }
        return null;
    }

    private boolean evaluateTrigger(
            TriggerModel trigger,
            String currentPageUrl,
            IHttpRequest request) {
        if (trigger.LogicalOperator.equals(LogicalOperatorType.OR)) {
            for (TriggerPart part : trigger.TriggerParts) {
                if (evaluateTriggerPart(part, currentPageUrl, request)) {
                    return true;
                }
            }
            return false;
        } else {
            for (TriggerPart part : trigger.TriggerParts) {
                if (!evaluateTriggerPart(part, currentPageUrl, request)) {
                    return false;
                }
            }
            return true;
        }
    }

    private boolean evaluateTriggerPart(TriggerPart triggerPart, String currentPageUrl, IHttpRequest request) {
        if (ValidatorType.URL_VALIDATOR.equals(triggerPart.ValidatorType)) {
            return UrlValidatorHelper.evaluate(triggerPart, currentPageUrl);
        } else if (ValidatorType.COOKIE_VALIDATOR.equals(triggerPart.ValidatorType)) {
            return CookieValidatorHelper.evaluate(triggerPart, request);
        } else if (ValidatorType.USERAGENT_VALIDATOR.equals(triggerPart.ValidatorType)) {
            return UserAgentValidatorHelper.evaluate(triggerPart, request.getUserAgent());
        } else if (ValidatorType.HTTPHEADER_VALIDATOR.equals(triggerPart.ValidatorType)) {
            return HttpHeaderValidatorHelper.evaluate(triggerPart, request.getHeader(triggerPart.HttpHeaderName));
        } else if (ValidatorType.REQUESTBODY_VALIDATOR.equals(triggerPart.ValidatorType)) {
            return RequestBodyValidatorHelper.evaluate(triggerPart, request.getRequestBodyAsString());
        } else {
            return false;
        }
    }
}
