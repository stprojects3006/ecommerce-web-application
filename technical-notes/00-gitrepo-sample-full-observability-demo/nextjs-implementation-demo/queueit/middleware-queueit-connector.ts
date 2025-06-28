import { KnownUser } from "@queue-it/connector-javascript";
import type { NextRequest } from "next/server";
import { NextResponse } from "next/server";
import integrationsConfig from "./config/integrationconfiguration.json";
import NextjsHttpContextProvider from "./NextjsHttpContextProvider";

const getInlineIntegrationConfigString = async () => {
  return JSON.stringify(integrationsConfig);
};

/** QUEUE-IT SECRETS & SETTINGS FROM .env */
const QueueIT_Settings = {
  QUEUEIT_CUSTOMER_ID: process.env.QUEUEIT_CUSTOMER_ID as string,
  QUEUEIT_SECRET_KEY: process.env.QUEUEIT_SECRET_KEY as string,
  QUEUEIT_API_KEY: process.env.QUEUEIT_API_KEY as string,
  QUEUEIT_ENQT_ENABLED:
    parseInt(process.env.QUEUEIT_ENQT_ENABLED as string) === 1,
  QUEUEIT_ENQT_VALIDITY_TIME: parseInt(
    process.env.QUEUEIT_ENQT_VALIDITY_TIME as string
  ),
  QUEUEIT_ENQT_KEY_ENABLED:
    parseInt(process.env.QUEUEIT_ENQT_KEY_ENABLED as string) === 1,
  QUEUEIT_REQ_BODY_ENABLED:
    parseInt(process.env.QUEUEIT_REQ_BODY_ENABLED as string) === 1,
};

async function HandleNextjsRequest(
  request: NextRequest,
  response: NextResponse
) {
  try {
    var integrationsConfigString = await getInlineIntegrationConfigString();

    const customerId = QueueIT_Settings.QUEUEIT_CUSTOMER_ID;
    const secretKey = QueueIT_Settings.QUEUEIT_SECRET_KEY;
    const apiKey = QueueIT_Settings.QUEUEIT_API_KEY;
    const enqueueTokenValidityTime =
      QueueIT_Settings.QUEUEIT_ENQT_VALIDITY_TIME;
    const enqueueTokenEnabled = QueueIT_Settings.QUEUEIT_ENQT_ENABLED;
    const enqueueTokenKeyEnabled = QueueIT_Settings.QUEUEIT_ENQT_KEY_ENABLED;
    const requestBodyCheckEnabled = QueueIT_Settings.QUEUEIT_REQ_BODY_ENABLED;

    const settings = {
      customerId,
      secretKey,
      enqueueTokenEnabled,
      enqueueTokenValidityTime,
      enqueueTokenKeyEnabled,
    };

    const requestBodyString: string = requestBodyCheckEnabled
      ? await request.text()
      : "";

    var httpContextProvider = new NextjsHttpContextProvider(
      request,
      response,
      requestBodyString
    );

    if (settings.enqueueTokenEnabled) {
      httpContextProvider.setEnqueueTokenProvider(
        settings.customerId,
        settings.secretKey,
        settings.enqueueTokenValidityTime,
        request.ip || "",
        enqueueTokenKeyEnabled
      );
    }

    var requestUrl = httpContextProvider._httpRequest.getAbsoluteUri();
    const queueitToken = request.nextUrl.searchParams.get(
      KnownUser.QueueITTokenKey
    ) as string;
    request.nextUrl.searchParams.delete(KnownUser.QueueITTokenKey);
    var requestUrlWithoutToken =
      httpContextProvider._httpRequest.getAbsoluteUri();
    // The requestUrlWithoutToken is used to match Triggers and as the Target url (where to return the users to).
    // It is therefor important that this is exactly the url of the users browsers. So, if your webserver is
    // behind e.g. a load balancer that modifies the host name or port, reformat requestUrlWithoutToken before proceeding.

    var validationResult = await KnownUser.validateRequestByIntegrationConfig(
      requestUrlWithoutToken,
      queueitToken,
      integrationsConfigString,
      customerId,
      secretKey,
      httpContextProvider,
      apiKey
    );

    if (validationResult.doRedirect()) {
      // Adding no cache headers to prevent browsers to cache requests
      response.headers.append(
        "Cache-Control",
        "no-cache, no-store, must-revalidate, max-age=0"
      );
      response.headers.append("Pragma", "no-cache");
      response.headers.append("Expires", "Fri, 01 Jan 1990 00:00:00 GMT");

      if (validationResult.isAjaxResult) {
        // In case of ajax call send the user to the queue by sending a custom queue-it header and redirecting user to queue from javascript
        var headerName = validationResult.getAjaxQueueRedirectHeaderKey();
        response.headers.append(
          headerName,
          validationResult.getAjaxRedirectUrl()
        );
        response.headers.append("Access-Control-Expose-Headers", headerName);

        // Render page
        return response;
      } else {
        // Send the user to the queue - either because hash was missing or because is was invalid
        const redirectResponse = NextResponse.redirect(
          validationResult.redirectUrl
        );
        redirectResponse.headers.append(
          "Cache-Control",
          "no-cache, no-store, must-revalidate, max-age=0"
        );
        redirectResponse.headers.append("Pragma", "no-cache");
        redirectResponse.headers.append(
          "Expires",
          "Fri, 01 Jan 1990 00:00:00 GMT"
        );

        return redirectResponse;
      }
    } else {
      // Request can continue - we remove queueittoken form querystring parameter to avoid sharing of user specific token
      if (
        requestUrl !== requestUrlWithoutToken &&
        validationResult.actionType === "Queue"
      ) {
        var responseHeaders = new Headers(response.headers);
        responseHeaders.set("location", requestUrlWithoutToken);

        const res = NextResponse.next({
          headers: responseHeaders,
          status: 302,
        });

        return res;
      } else {
        // No change - Continue
        return response;
      }
    }
  } catch (e) {
    // There was an error validating the request
    // Use your own logging framework to log the Exception
    console.log("Queue-it connector error:", e);

    // In any case let the user continue
    return NextResponse.next();
  }
}

export default HandleNextjsRequest;
