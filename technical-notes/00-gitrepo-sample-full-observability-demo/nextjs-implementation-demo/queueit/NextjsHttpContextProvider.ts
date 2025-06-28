import {
    DefaultEnqueueTokenProvider,
    IConnectorContextProvider,
    ICryptoProvider,
    IEnqueueTokenProvider,
    IHttpRequest,
    IHttpResponse,
} from "@queue-it/connector-javascript";
import { Payload, Token } from "@queue-it/queue-token";
import { NextRequest, NextResponse } from "next/server";
import { NextjsCryptoProvider } from "./nextjsCryptoProvider";

export default class NextjsHttpContextProvider implements IConnectorContextProvider {
    _httpRequest: IHttpRequest;
    _httpResponse: IHttpResponse;
    _outputCookie: string | undefined;
    _enqueueTokenProvider: IEnqueueTokenProvider | undefined;
    _cryptoProvider: ICryptoProvider;

    isError: boolean | undefined;

    constructor(public request: NextRequest, public response: NextResponse, bodyString: string) {
        this._httpRequest = new RequestProvider(request, bodyString);
        this._httpResponse = new ResponseProvider(response);
        this._cryptoProvider = new NextjsCryptoProvider();
    }

    public getHttpRequest() {
        return this._httpRequest;
    }

    public getHttpResponse() {
        return this._httpResponse;
    }

    public setOutputCookie(setCookie: string) {
        this._outputCookie = setCookie;
    }

    public getOutputCookie() {
        return this._outputCookie;
    }

    public setEnqueueTokenProvider(
        customerId: string,
        secretKey: string,
        validityTime: number,
        clientIp: string,
        withKey: boolean
    ) {
        this._enqueueTokenProvider = new DefaultEnqueueTokenProvider(
            customerId,
            secretKey,
            validityTime,
            clientIp,
            withKey,
            Token,
            Payload
        );
    }

    public getEnqueueTokenProvider() {
        return this._enqueueTokenProvider || null;
    }
    public getCryptoProvider() {
        return this._cryptoProvider;
    }
}

class RequestProvider implements IHttpRequest {
    _parsedCookieDic!: Record<string, string>;

    constructor(private req: NextRequest, private bodyString: string) {}

    public getUserAgent() {
        return this.getHeader("user-agent");
    }

    public getHeader(name: string) {
        if (name.toLowerCase() == "x-queueit-clientip") {
            return this.getUserHostAddress();
        }
        var headerValue = this.req.headers.get(name);

        if (!headerValue) return "";

        return headerValue;
    }

    public getAbsoluteUri() {
        return this.req.nextUrl.toString();
    }

    public getUserHostAddress(): any {
        return this.req.ip;
    }

    public getCookieValue(cookieKey: string): string {
        /* Nextjs Version 13+ */
        return this.req.cookies.get(cookieKey)?.value ?? "THERE IS NO COOKIE AVAILABLE";
    }

    public getRequestBodyAsString() {
        return this.bodyString;
    }
}

class ResponseProvider implements IHttpResponse {
    constructor(private res: NextResponse) {}

    public setCookie(
        cookieName: string,
        cookieValue: string,
        domain: string,
        expiration: number,
        httpOnly: boolean,
        isSecure: boolean
    ) {
        // expiration is in secs, but Date needs it in milisecs
        let expirationDate = new Date(expiration * 1000);

        /* Nextjs Version 13+ */
        this.res.cookies.set({
            name: cookieName,
            value: cookieValue,
            domain: domain,
            path: "/",
            expires: expirationDate,
            secure: isSecure,
            sameSite: false,
            httpOnly: httpOnly,
        });
    }
}
