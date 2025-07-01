package com.queue_it.connector;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

import com.queue_it.connector.helpers.HashHelper;
import com.queue_it.connector.helpers.Utils;
import com.queue_it.connector.models.StateInfo;

public class UserInQueueStateCookieRepository implements IUserInQueueStateRepository {

    private static final String QUEUEIT_DATA_KEY = "QueueITAccepted-SDFrts345E-V3";
    private static final String QUEUE_ID_KEY = "QueueId";
    private static final String HASH_KEY = "Hash";
    private static final String ISSUETIME_KEY = "IssueTime";
    private static final String REDIRECT_TYPE_KEY = "RedirectType";
    private static final String EVENT_ID_KEY = "EventId";
    private static final String FIXED_COOKIE_VALIDITY_MINUTES_KEY = "FixedValidityMins";
    private static final String HASHED_IP_KEY = "Hip";

    public IConnectorContextProvider connectorContextProvider;

    public static String getCookieKey(String eventId) {
        return QUEUEIT_DATA_KEY + "_" + eventId;
    }

    public UserInQueueStateCookieRepository(IConnectorContextProvider connectorContextProvider) {
        this.connectorContextProvider = connectorContextProvider;
    }

    @Override
    public void store(
            String eventId,
            String queueId,
            Integer fixedCookieValidityMinutes,
            String cookieDomain,
            Boolean isCookieHttpOnly,
            Boolean isCookieSecure,
            String redirectType,
            String hashedIP,
            String secretKey) throws Exception {

        String cookieKey = getCookieKey(eventId);

        String cookieValue = createCookieValue(eventId, queueId, fixedCookieValidityMinutes, redirectType, hashedIP,
                secretKey);

        this.connectorContextProvider.getHttpResponse().setCookie(cookieKey, cookieValue, 24 * 60 * 60, cookieDomain,
                isCookieHttpOnly, isCookieSecure);
    }

    private String createCookieValue(String eventId, String queueId, Integer fixedCookieValidityMinutes,
            String redirectType, String hashedIP, String secretKey) throws Exception {

        String issueTime = Long.toString(System.currentTimeMillis() / 1000L);
        String fixedValidityStr = fixedCookieValidityMinutes != null ? String.valueOf(fixedCookieValidityMinutes) : "";

        String valueToHash = eventId
                + ((queueId == null || queueId.trim().isEmpty()) ? "" : queueId)
                + fixedValidityStr
                + redirectType
                + issueTime;

        String hashedValue = HashHelper.generateSHA256Hash(secretKey, valueToHash);
        return EVENT_ID_KEY + "=" + eventId + "&"
                + ((queueId == null || queueId.trim().isEmpty()) ? "" : (QUEUE_ID_KEY + "=" + queueId + "&"))
                + (fixedCookieValidityMinutes != null
                        ? (FIXED_COOKIE_VALIDITY_MINUTES_KEY + "=" + fixedValidityStr + "&")
                        : "")
                + REDIRECT_TYPE_KEY + "=" + redirectType
                + "&" + ISSUETIME_KEY + "=" + issueTime + "&" + HASH_KEY + "=" + hashedValue
                + (hashedIP != null ? "&" + HASHED_IP_KEY + "=" + hashedIP : "");
    }

    private Boolean isCookieValid(
            String secretKey,
            HashMap<String, String> cookieNameValueMap,
            String eventId,
            int cookieValidityMinutes,
            boolean validateTime) throws UnsupportedEncodingException, NoSuchAlgorithmException, InvalidKeyException {
        try {
            if (!cookieNameValueMap.containsKey(EVENT_ID_KEY)) {
                return false;
            }
            if (!cookieNameValueMap.containsKey(REDIRECT_TYPE_KEY)) {
                return false;
            }
            if (!cookieNameValueMap.containsKey(ISSUETIME_KEY)) {
                return false;
            }
            if (!cookieNameValueMap.containsKey(HASH_KEY)) {
                return false;
            }

            String fixedCookieValidityMinutes = "";
            if (cookieNameValueMap.containsKey(FIXED_COOKIE_VALIDITY_MINUTES_KEY)) {
                fixedCookieValidityMinutes = cookieNameValueMap.get(FIXED_COOKIE_VALIDITY_MINUTES_KEY);
            }

            String queueId = "";
            if (cookieNameValueMap.containsKey(QUEUE_ID_KEY)) {
                queueId = cookieNameValueMap.get(QUEUE_ID_KEY);
            }

            String stringToHash = cookieNameValueMap.get(EVENT_ID_KEY)
                    + ((queueId == null || queueId.trim().isEmpty()) ? "" : queueId)
                    + fixedCookieValidityMinutes
                    + cookieNameValueMap.get(REDIRECT_TYPE_KEY)
                    + cookieNameValueMap.get(ISSUETIME_KEY);

            String hashValue = HashHelper.generateSHA256Hash(secretKey, stringToHash);

            if (!hashValue.equals(cookieNameValueMap.get(HASH_KEY))) {
                return false;
            }
            if (!eventId.equalsIgnoreCase(cookieNameValueMap.get(EVENT_ID_KEY))) {
                return false;
            }

            if (validateTime) {
                int validity = cookieValidityMinutes;
                if (!Utils.isNullOrWhiteSpace(fixedCookieValidityMinutes)) {
                    validity = Integer.parseInt(fixedCookieValidityMinutes);
                }

                long expirationTime = Long.parseLong(cookieNameValueMap.get(ISSUETIME_KEY)) + (validity * 60);
                if (expirationTime < (System.currentTimeMillis() / 1000L)) {
                    return false;
                }
            }

            String clientIp = connectorContextProvider.getHttpRequest().getUserHostAddress();
            String cookieHashedIp = cookieNameValueMap.get(HASHED_IP_KEY);

            if (cookieHashedIp != null && clientIp != null) {
                String hashedClientIp = connectorContextProvider.getCryptoProvider().generateSHA256Hash(secretKey,
                        clientIp);

                if (!cookieHashedIp.equals(hashedClientIp)) {
                    return false;
                }
            }
            return true;

        } catch (NumberFormatException ex) {
        }
        return true;
    }

    public static HashMap<String, String> getCookieNameValueMap(String cookieValue) {
        HashMap<String, String> result = new HashMap<String, String>();
        String[] cookieNameValues = cookieValue.split("&");

        for (int i = 0; i < cookieNameValues.length; ++i) {
            String[] arr = cookieNameValues[i].split("=");
            if (arr.length == 2) {
                result.put(arr[0], arr[1]);
            }
        }
        return result;
    }

    /**
     *
     * @param eventId
     * @param cookieValidityMinutes
     * @param secretKey
     * @param validateTime
     * @return
     */
    @Override
    public StateInfo getState(String eventId, int cookieValidityMinutes, String secretKey, boolean validateTime) {
        try {
            String cookieKey = getCookieKey(eventId);
            String cookieValue = this.connectorContextProvider.getHttpRequest().getCookieValue(cookieKey);

            if (cookieValue == null) {
                return new StateInfo(false, false, null, null, null, null);
            }
            HashMap<String, String> cookieNameValueMap = UserInQueueStateCookieRepository
                    .getCookieNameValueMap(cookieValue);

            if (Boolean.FALSE.equals(isCookieValid(secretKey, cookieNameValueMap, eventId, cookieValidityMinutes, validateTime))) {
                return new StateInfo(true, false, null, null, null, null);
            }

            return new StateInfo(true, true, cookieNameValueMap.get(QUEUE_ID_KEY),
                    cookieNameValueMap.get(FIXED_COOKIE_VALIDITY_MINUTES_KEY),
                    cookieNameValueMap.get(REDIRECT_TYPE_KEY),
                    cookieNameValueMap.get(HASHED_IP_KEY));
        } catch (NumberFormatException ex) {
        } catch (Exception ex) {
        }
        return new StateInfo(true, false, null, null, null, null);
    }

    @Override
    public void cancelQueueCookie(
            String eventId,
            String cookieDomain,
            Boolean isCookieHttpOnly,
            Boolean isCookieSecure) {
        String cookieKey = getCookieKey(eventId);
        this.connectorContextProvider.getHttpResponse().setCookie(cookieKey, "", 0, cookieDomain, false,
                false);
    }

    @Override
    public void reissueQueueCookie(
            String eventId,
            int cookieValidityMinutes,
            String cookieDomain,
            Boolean isCookieHttpOnly, Boolean isCookieSecure, String secretKey) {
        try {
            String cookieKey = getCookieKey(eventId);
            String cookieValueOld = this.connectorContextProvider.getHttpRequest().getCookieValue(cookieKey);
            if (cookieValueOld == null) {
                return;
            }
            HashMap<String, String> cookieNameValueMap = getCookieNameValueMap(cookieValueOld);
            if (!isCookieValid(secretKey, cookieNameValueMap, eventId, cookieValidityMinutes, true)) {
                return;
            }
            Integer fixedCookieValidityMinutes = null;
            if (cookieNameValueMap.containsKey(FIXED_COOKIE_VALIDITY_MINUTES_KEY)) {
                fixedCookieValidityMinutes = Integer.valueOf(cookieNameValueMap.get(FIXED_COOKIE_VALIDITY_MINUTES_KEY));
            }

            String cookieValue = createCookieValue(eventId, cookieNameValueMap.get(QUEUE_ID_KEY),
                    fixedCookieValidityMinutes, cookieNameValueMap.get(REDIRECT_TYPE_KEY),
                    cookieNameValueMap.get(HASHED_IP_KEY), secretKey);

            this.connectorContextProvider.getHttpResponse().setCookie(cookieKey, cookieValue, 24 * 60 * 60,
                    cookieDomain, isCookieHttpOnly,
                    isCookieSecure);

        } catch (Exception ex) {
        }
    }
}
