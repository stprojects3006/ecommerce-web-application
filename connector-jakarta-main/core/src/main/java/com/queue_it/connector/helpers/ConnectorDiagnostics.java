package com.queue_it.connector.helpers;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import com.queue_it.connector.ICryptoProvider;
import com.queue_it.connector.RequestValidationResult;

public class ConnectorDiagnostics {
    public static ConnectorDiagnostics verify(String customerId, String secretKey, String queueitToken,
            ICryptoProvider cryptoProvider) throws InvalidKeyException, UnsupportedEncodingException, NoSuchAlgorithmException {

        ConnectorDiagnostics diagnostics = new ConnectorDiagnostics();

        QueueUrlParams qParams = QueueParameterHelper.extractQueueParams(queueitToken);

        if (qParams == null) {
            return diagnostics;
        }

        if (qParams.getRedirectType() == null) {
            return diagnostics;
        }

        if (!"debug".equals(qParams.getRedirectType())) {
            return diagnostics;
        }

        if (Utils.isNullOrWhiteSpace(customerId) || Utils.isNullOrWhiteSpace(secretKey)) {
            diagnostics.setStateWithSetupError();
            return diagnostics;
        }

        String calculatedHash = cryptoProvider.generateSHA256Hash(secretKey, qParams.getQueueITTokenWithoutHash());

        if (!calculatedHash.toUpperCase().equals(qParams.getHashCode().toUpperCase())) {
            diagnostics.setStateWithTokenError(customerId, "hash");
            return diagnostics;
        }

        if (qParams.getTimeStamp() < System.currentTimeMillis() / 1000L) {
            diagnostics.setStateWithTokenError(customerId, "timestamp");
            return diagnostics;
        }

        diagnostics.isEnabled = true;

        return diagnostics;
    }

    public boolean isEnabled;
    public boolean hasError;

    public RequestValidationResult validationResult;

    public boolean getIsEnabled() {
        return this.isEnabled;
    }

    public void setIsEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    public boolean getHasError() {
        return this.hasError;
    }

    public void setHasError(boolean hasError) {
        this.hasError = hasError;
    }

    public RequestValidationResult getValidationResult() {
        return this.validationResult;
    }

    public void setValidationResult(RequestValidationResult validationResult) {
        this.validationResult = validationResult;
    }

    private void setStateWithTokenError(String customerId, String errorCode) {
        hasError = true;
        String redirectUrl = "https://" + customerId + ".api2.queue-it.net/" + customerId
                + "/diagnostics/connector/error/?code=" + errorCode;

        validationResult = new RequestValidationResult("ConnectorDiagnosticsRedirect", null, null, redirectUrl, null,
                null);
    }

    private void setStateWithSetupError() {
        hasError = true;
        validationResult = new RequestValidationResult("ConnectorDiagnosticsRedirect", null, null,
                "https://api2.queue-it.net/diagnostics/connector/error/?code=setup", null, null);
    }
}