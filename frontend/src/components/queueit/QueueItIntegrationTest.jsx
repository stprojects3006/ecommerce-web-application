import React, { useState } from 'react';
import QueueItService from '../../api-service/queueit.service';

const QueueItIntegrationTest = () => {
  const [eventId, setEventId] = useState('flash-sale-2024');
  const [queueitToken, setQueueitToken] = useState('');
  const [response, setResponse] = useState(null);
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(false);

  const handleQueueUser = async () => {
    setLoading(true);
    setError(null);
    setResponse(null);
    try {
      const res = await QueueItService.queueUser({ eventId, queueitToken });
      setResponse(res);
    } catch (err) {
      setError(err.message || 'Error');
    }
    setLoading(false);
  };

  // Stubs for future endpoints
  const handleSimulateEvent = async () => {
    setError('Not implemented');
  };
  const handleGetSessionInfo = async () => {
    setError('Not implemented');
  };
  const handleResetTestState = async () => {
    setError('Not implemented');
  };

  return (
    <div style={{border: '1px solid #ccc', padding: 20, margin: 20}}>
      <h3>Queue-it Integration Test</h3>
      <div>
        <label>Event ID: </label>
        <input value={eventId} onChange={e => setEventId(e.target.value)} />
      </div>
      <div>
        <label>Queue-it Token: </label>
        <input value={queueitToken} onChange={e => setQueueitToken(e.target.value)} />
      </div>
      <button onClick={handleQueueUser} disabled={loading}>Test /api/queueit/queue</button>
      <button onClick={handleSimulateEvent} disabled>Simulate Event (stub)</button>
      <button onClick={handleGetSessionInfo} disabled>Get Session Info (stub)</button>
      <button onClick={handleResetTestState} disabled>Reset Test State (stub)</button>
      {loading && <div>Loading...</div>}
      {response && <pre style={{background: '#eee', padding: 10}}>{JSON.stringify(response, null, 2)}</pre>}
      {error && <div style={{color: 'red'}}>{error}</div>}
    </div>
  );
};

export default QueueItIntegrationTest; 