import { ICryptoProvider } from "@queue-it/connector-javascript";
import jsSHA from "jssha";

export class NextjsCryptoProvider implements ICryptoProvider {
  public constructor() {}
  public getSha256Hash(secretKey: string, stringToHash: string) {
    const jws = new jsSHA("SHA-256", "TEXT", {
      hmacKey: { value: secretKey, format: "TEXT" },
    });
    jws.update(stringToHash);
    return jws.getHash("HEX");
  }
}
