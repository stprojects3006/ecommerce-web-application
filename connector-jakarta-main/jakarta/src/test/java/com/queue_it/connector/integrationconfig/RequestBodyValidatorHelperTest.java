package com.queue_it.connector.integrationconfig;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.util.HashMap;

import org.junit.Test;

import com.queue_it.connector.IHttpRequest;
import com.queue_it.connector.mocks.KnownUserRequestWrapperMock;

class HttpRequestMock implements IHttpRequest {

    @Override
    public String getUserAgent() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getUserAgent'");
    }

    @Override
    public HashMap<String, String> getHeaders() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getHeaders'");
    }

    @Override
    public URI getUri() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getUrl'");
    }

    @Override
    public String getUserHostAddress() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getUserHostAddress'");
    }

    @Override
    public String getCookieValue(String cookieKey) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getCookieValue'");
    }

    @Override
    public String getRequestBodyAsString() {
        return "test body";

    }

    @Override
    public String getHeader(String headerValue) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getHeader'");
    }

}

public class RequestBodyValidatorHelperTest {
    @Test
    public void Evaluate_Test() {
        TriggerPart triggerPart = new TriggerPart();
        triggerPart.Operator = ComparisonOperatorType.CONTAINS;
        triggerPart.ValueToCompare = "test body";

        HttpServletRequestMock requestMock = new HttpServletRequestMock();
        KnownUserRequestWrapperMock wrappedRequest = new KnownUserRequestWrapperMock(requestMock);

        assertFalse(RequestBodyValidatorHelper.evaluate(triggerPart, wrappedRequest.GetRequestBodyAsString()));

        wrappedRequest.SetRequestBodyAsString("test body");

        assertTrue(RequestBodyValidatorHelper.evaluate(triggerPart, wrappedRequest.GetRequestBodyAsString()));

        triggerPart.ValueToCompare = "ZZZ";
        assertFalse(RequestBodyValidatorHelper.evaluate(triggerPart, wrappedRequest.GetRequestBodyAsString()));

        triggerPart.ValueToCompare = "Test";
        triggerPart.IsIgnoreCase = true;
        assertTrue(RequestBodyValidatorHelper.evaluate(triggerPart, wrappedRequest.GetRequestBodyAsString()));

        triggerPart.ValueToCompare = "Test";
        triggerPart.IsIgnoreCase = true;
        triggerPart.IsNegative = true;
        assertFalse(RequestBodyValidatorHelper.evaluate(triggerPart, wrappedRequest.GetRequestBodyAsString()));

        triggerPart.ValueToCompare = "Test";
        triggerPart.IsIgnoreCase = true;
        triggerPart.IsNegative = true;
        assertFalse(RequestBodyValidatorHelper.evaluate(triggerPart, wrappedRequest.GetRequestBodyAsString()));
    }
}
