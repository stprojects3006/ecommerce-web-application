package com.queue_it.connector.integrationconfig;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.URISyntaxException;

import org.junit.Test;

import com.queue_it.connector.IHttpRequest;
import com.queue_it.connector.IHttpResponse;
import com.queue_it.connector.integrationconfig.ComparisonOperatorType;
import com.queue_it.connector.integrationconfig.CookieValidatorHelper;
import com.queue_it.connector.integrationconfig.TriggerPart;
import com.queue_it.connector.HttpRequest;
import com.queue_it.connector.HttpResponse;
import com.queue_it.connector.mocks.ConnectorContextProviderMock;
import com.queue_it.connector.mocks.KnownUserRequestWrapperMock;

public class CookieValidatorHelperTest {

    @Test
    public void Evaluate_Test() throws URISyntaxException {

        // Arrange
        ConnectorContextProviderMock connectorContextProviderMock = new ConnectorContextProviderMock();

        HttpServletRequestMock httpServletRequestMock = new HttpServletRequestMock();
        KnownUserRequestWrapperMock knownUserRequestWrapperMock = new KnownUserRequestWrapperMock(
                httpServletRequestMock);
        connectorContextProviderMock.httpRequest = new HttpRequest(knownUserRequestWrapperMock);

        HttpServletResponseMock httpServletResponseMock = new HttpServletResponseMock(httpServletRequestMock);
        connectorContextProviderMock.httpResponse = new HttpResponse(httpServletResponseMock);

        IHttpRequest request = connectorContextProviderMock.getHttpRequest();
        IHttpResponse response = connectorContextProviderMock.getHttpResponse();

        // Act
        TriggerPart triggerPart = new TriggerPart();
        triggerPart.CookieName = "c1";
        triggerPart.Operator = ComparisonOperatorType.CONTAINS;
        triggerPart.ValueToCompare = "1";

        // Assert
        assertFalse(CookieValidatorHelper.evaluate(triggerPart, request));

        // Act
        response.setCookie("c5", "5", null, null, null, null);
        response.setCookie("c1", "1", null, null, null, null);
        response.setCookie("c2", "test", null, null, null, null);

        // Assert
        assertTrue(CookieValidatorHelper.evaluate(triggerPart, request));

        // Act
        triggerPart.ValueToCompare = "5";

        // Assert
        assertFalse(CookieValidatorHelper.evaluate(triggerPart, request));

        // Act
        triggerPart.ValueToCompare = "Test";
        triggerPart.IsIgnoreCase = true;
        triggerPart.CookieName = "c2";

        // Assert
        assertTrue(CookieValidatorHelper.evaluate(triggerPart, request));

        // Act
        triggerPart.ValueToCompare = "Test";
        triggerPart.IsIgnoreCase = true;
        triggerPart.IsNegative = true;
        triggerPart.CookieName = "c2";

        // Assert
        assertFalse(CookieValidatorHelper.evaluate(triggerPart, request));
    }
}