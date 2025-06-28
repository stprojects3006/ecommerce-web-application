# Queue-it Server Side Javascript Connector Implementation Sample for NextJs Applications

## Introduction

This demo contains the sample code to demonstrate the correct way to implement the Queue-it server side javascript SDK in a NextJS application.

## How to use this demo project?

```bash
git clone https://github.com/queueit/nextjs-implementation-demo.git
cd nextjs-implementation-demo
npm install
curl --request GET https://[your-customer-id].queue-it.net/status/integrationconfig/secure/[your-customer-id] --header "api-key: [your-api-key]" --header "Host: queue-it.net" > ./queueit/config/integrationconfiguration.json
```

Rename the `.env.template` file to `.env` in the root folder of the project and fill out the missing values.

```bash
npm run build
npm run start
```

## How to integrate into an existing NextJs project

- install dependencies

```bash
npm install @queue-it/connector-javascript jssha text-encoding
```

- copy the entire `/queueit` folder to your project

- add the content of the `.env.template` file to your existing `.env` file in your project and fill out the missing values

- get your queue-it integration configuration json file from the Queue-it API. Substitute `your-customer-id` and `your-api-key` and use the below call and save it as `queueit/config/integrationconfiguration.json`.

```bash
curl --request GET https://[your-customer-id].queue-it.net/status/integrationconfig/secure/[your-customer-id] --header "api-key: [your-api-key]" --header "Host: queue-it.net" > ./queueit/config/integrationconfiguration.json
```

- edit your middleware.ts file to run the Queue-it connector

```typescript
// Import the Queue-it connector
import HandleNextjsRequest from "./queueit/middleware-queueit-connector";

export const middleware = async (request: NextRequest) => {
  // Create the response
  let response = NextResponse.next();

  // AddQueue-it logic inside the middleware method
  response = await HandleNextjsRequest(request, response);

  // Other custom logic goes here

  return response;
};
```

- build & run

## Note about NextJs versions

This demo and the included Queue-it code was written and tested for NextJs v13 and above.
For version 12 there is a separate branch called `nextjsv12`.
Versions before that are not supported.

## Roadmap

- Extend the demo example to consume protected API resource with hybrid integration (ajax intercept)
