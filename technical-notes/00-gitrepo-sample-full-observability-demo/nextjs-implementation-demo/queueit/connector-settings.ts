type ConnectorSettings = {
  customerId: string;
  secretKey: string;
  apiKey: string;
  isEnqueueTokenEnabled: boolean;
  enqueueTokenKeyEnabled: boolean;
  enqueueTokenValidityTime: number;
  isEnqueueTokenKeyEnabled: boolean;
};

export default ConnectorSettings;
