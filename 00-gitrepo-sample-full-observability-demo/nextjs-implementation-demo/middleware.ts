import type { NextRequest } from "next/server";
import { NextResponse } from "next/server";
import HandleNextjsRequest from "./queueit/middleware-queueit-connector";

/**
 * Middleware
 *
 * @param request
 * @returns
 */
export const middleware = async (request: NextRequest) => {
  console.log("[Middleware] path: ", request.nextUrl.pathname);

  // Create the response
  let response = NextResponse.next();

  // Queue-it logic here
  response = await HandleNextjsRequest(request, response);

  // Other custom logic here
  // do something custom here

  return response;
};

/**
 * Requests for paths matched here will be processed by the middleware function
 */
export const config = {
  matcher: [
    /*
     * Match all request paths except for the ones starting with:
     * - api (API routes - we do NOT want these to be redirected to the WR)
     * - _next/static (static files - these should never be redirected to the WR)
     * - favicon.ico (favicon file)
     */
    "/((?!api|_next/static|favicon.ico).*)",
  ],
};
