import React, { useState, useEffect } from 'react';
import QueueItService from '../../api-service/queueit.service';
import useQueueIt from '../../hooks/useQueueIt';
import QueueItProtection from '../../components/queueit/QueueItProtection';

const TestQueueItPage = () => {
  const [testResults, setTestResults] = useState([]);
  const [isTesting, setIsTesting] = useState(false);
  const [eventId, setEventId] = useState('test-event-2024');
  
  const {
    isLoading,
    error,
    validateQueueToken,
    cancelQueueSession,
    extendQueueCookie,
    getQueueStatus,
    checkHealth,
    getQueueToken,
    clearError
  } = useQueueIt(eventId);

  const addTestResult = (testName, success, details = '') => {
    setTestResults(prev => [...prev, {
      id: Date.now(),
      name: testName,
      success,
      details,
      timestamp: new Date().toLocaleTimeString()
    }]);
  };

  const runBackendTests = async () => {
    setIsTesting(true);
    setTestResults([]);

    try {
      // Test 1: Health Check
      try {
        const health = await QueueItService.checkHealth();
        addTestResult('Health Check', true, JSON.stringify(health, null, 2));
      } catch (error) {
        addTestResult('Health Check', false, error.message);
      }

      // Test 2: Status Check
      try {
        const status = await QueueItService.getQueueStatus();
        addTestResult('Status Check', true, JSON.stringify(status, null, 2));
      } catch (error) {
        addTestResult('Status Check', false, error.message);
      }

      // Test 3: Token Validation
      try {
        const result = await QueueItService.validateQueueToken(
          'test-token-123',
          'http://localhost:3000/test-page',
          'http://localhost:3000/test-page'
        );
        addTestResult('Token Validation', true, JSON.stringify(result, null, 2));
      } catch (error) {
        addTestResult('Token Validation', false, error.message);
      }

      // Test 4: Cookie Extension
      try {
        const result = await QueueItService.extendQueueCookie(
          'test-event-2024',
          'test-queue-123',
          {
            cookieValidityMinutes: 30,
            isCookieHttpOnly: true,
            isCookieSecure: false
          }
        );
        addTestResult('Cookie Extension', true, JSON.stringify(result, null, 2));
      } catch (error) {
        addTestResult('Cookie Extension', false, error.message);
      }

    } catch (error) {
      addTestResult('Test Suite', false, error.message);
    } finally {
      setIsTesting(false);
    }
  };

  const runHookTests = async () => {
    setIsTesting(true);
    setTestResults([]);

    try {
      // Test 1: Hook Health Check
      try {
        const health = await checkHealth();
        addTestResult('Hook Health Check', true, JSON.stringify(health, null, 2));
      } catch (error) {
        addTestResult('Hook Health Check', false, error.message);
      }

      // Test 2: Hook Status Check
      try {
        const status = await getQueueStatus();
        addTestResult('Hook Status Check', true, JSON.stringify(status, null, 2));
      } catch (error) {
        addTestResult('Hook Status Check', false, error.message);
      }

      // Test 3: Get Queue Token
      try {
        const token = getQueueToken();
        addTestResult('Get Queue Token', true, `Token: ${token || 'None'}`);
      } catch (error) {
        addTestResult('Get Queue Token', false, error.message);
      }

    } catch (error) {
      addTestResult('Hook Test Suite', false, error.message);
    } finally {
      setIsTesting(false);
    }
  };

  const clearResults = () => {
    setTestResults([]);
    clearError();
  };

  return (
    <div className="test-queueit-page">
      <div className="test-header">
        <h1>🧪 Queue-it Integration Test Page</h1>
        <p>Test the Queue-it integration functionality</p>
      </div>

      <div className="test-controls">
        <div className="control-group">
          <label htmlFor="eventId">Event ID:</label>
          <input
            id="eventId"
            type="text"
            value={eventId}
            onChange={(e) => setEventId(e.target.value)}
            placeholder="Enter Queue-it event ID"
          />
        </div>

        <div className="button-group">
          <button 
            onClick={runBackendTests}
            disabled={isTesting}
            className="test-button primary"
          >
            {isTesting ? 'Running Tests...' : 'Test Backend APIs'}
          </button>

          <button 
            onClick={runHookTests}
            disabled={isTesting}
            className="test-button secondary"
          >
            {isTesting ? 'Running Tests...' : 'Test React Hook'}
          </button>

          <button 
            onClick={clearResults}
            className="test-button clear"
          >
            Clear Results
          </button>
        </div>
      </div>

      <div className="test-status">
        <h3>Current Status</h3>
        <div className="status-grid">
          <div className="status-item">
            <strong>Event ID:</strong> {eventId}
          </div>
          <div className="status-item">
            <strong>Queue Token:</strong> {getQueueToken() || 'None'}
          </div>
          <div className="status-item">
            <strong>Loading:</strong> {isLoading ? 'Yes' : 'No'}
          </div>
          {error && (
            <div className="status-item error">
              <strong>Error:</strong> {error}
            </div>
          )}
        </div>
      </div>

      <div className="test-results">
        <h3>Test Results ({testResults.length})</h3>
        {testResults.length === 0 ? (
          <p className="no-results">No test results yet. Run a test to see results.</p>
        ) : (
          <div className="results-list">
            {testResults.map(result => (
              <div key={result.id} className={`result-item ${result.success ? 'success' : 'error'}`}>
                <div className="result-header">
                  <span className="result-status">
                    {result.success ? '✅' : '❌'}
                  </span>
                  <span className="result-name">{result.name}</span>
                  <span className="result-time">{result.timestamp}</span>
                </div>
                {result.details && (
                  <div className="result-details">
                    <pre>{result.details}</pre>
                  </div>
                )}
              </div>
            ))}
          </div>
        )}
      </div>

      <div className="protection-demo">
        <h3>QueueItProtection Component Demo</h3>
        <QueueItProtection
          eventId={eventId}
          fallbackUrl="/queue"
          onAccessGranted={(result) => {
            addTestResult('Protection Access Granted', true, JSON.stringify(result, null, 2));
          }}
          onAccessDenied={(reason) => {
            addTestResult('Protection Access Denied', false, reason);
          }}
        >
          <div className="protected-content">
            <h4>🎉 Protected Content</h4>
            <p>This content is protected by Queue-it. If you can see this, the protection is working!</p>
            <div className="protected-actions">
              <button 
                onClick={() => validateQueueToken('test-token', window.location.href)}
                disabled={isLoading}
                className="action-button"
              >
                Test Token Validation
              </button>
              <button 
                onClick={() => extendQueueCookie('test-queue', { cookieValidityMinutes: 30 })}
                disabled={isLoading}
                className="action-button"
              >
                Test Cookie Extension
              </button>
            </div>
          </div>
        </QueueItProtection>
      </div>

      <style jsx>{`
        .test-queueit-page {
          max-width: 1200px;
          margin: 0 auto;
          padding: 20px;
          font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
        }

        .test-header {
          text-align: center;
          margin-bottom: 30px;
          padding: 20px;
          background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
          color: white;
          border-radius: 10px;
        }

        .test-header h1 {
          margin: 0 0 10px 0;
          font-size: 2.5em;
        }

        .test-controls {
          background: #f8f9fa;
          padding: 20px;
          border-radius: 8px;
          margin-bottom: 20px;
        }

        .control-group {
          margin-bottom: 15px;
        }

        .control-group label {
          display: block;
          margin-bottom: 5px;
          font-weight: 600;
        }

        .control-group input {
          width: 100%;
          padding: 10px;
          border: 1px solid #ddd;
          border-radius: 4px;
          font-size: 16px;
        }

        .button-group {
          display: flex;
          gap: 10px;
          flex-wrap: wrap;
        }

        .test-button {
          padding: 12px 20px;
          border: none;
          border-radius: 6px;
          font-size: 14px;
          font-weight: 600;
          cursor: pointer;
          transition: all 0.2s;
        }

        .test-button.primary {
          background: #007bff;
          color: white;
        }

        .test-button.primary:hover:not(:disabled) {
          background: #0056b3;
        }

        .test-button.secondary {
          background: #6c757d;
          color: white;
        }

        .test-button.secondary:hover:not(:disabled) {
          background: #545b62;
        }

        .test-button.clear {
          background: #dc3545;
          color: white;
        }

        .test-button.clear:hover {
          background: #c82333;
        }

        .test-button:disabled {
          opacity: 0.6;
          cursor: not-allowed;
        }

        .test-status {
          background: white;
          border: 1px solid #ddd;
          border-radius: 8px;
          padding: 20px;
          margin-bottom: 20px;
        }

        .status-grid {
          display: grid;
          grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
          gap: 15px;
          margin-top: 15px;
        }

        .status-item {
          padding: 10px;
          background: #f8f9fa;
          border-radius: 4px;
          border-left: 4px solid #007bff;
        }

        .status-item.error {
          border-left-color: #dc3545;
          background: #f8d7da;
          color: #721c24;
        }

        .test-results {
          background: white;
          border: 1px solid #ddd;
          border-radius: 8px;
          padding: 20px;
          margin-bottom: 20px;
        }

        .no-results {
          text-align: center;
          color: #6c757d;
          font-style: italic;
        }

        .results-list {
          max-height: 400px;
          overflow-y: auto;
        }

        .result-item {
          border: 1px solid #ddd;
          border-radius: 6px;
          margin-bottom: 10px;
          overflow: hidden;
        }

        .result-item.success {
          border-left: 4px solid #28a745;
        }

        .result-item.error {
          border-left: 4px solid #dc3545;
        }

        .result-header {
          display: flex;
          align-items: center;
          padding: 12px 15px;
          background: #f8f9fa;
          gap: 10px;
        }

        .result-status {
          font-size: 18px;
        }

        .result-name {
          font-weight: 600;
          flex: 1;
        }

        .result-time {
          color: #6c757d;
          font-size: 12px;
        }

        .result-details {
          padding: 15px;
          background: white;
        }

        .result-details pre {
          margin: 0;
          white-space: pre-wrap;
          word-wrap: break-word;
          font-size: 12px;
          color: #495057;
        }

        .protection-demo {
          background: white;
          border: 1px solid #ddd;
          border-radius: 8px;
          padding: 20px;
        }

        .protected-content {
          background: #d4edda;
          border: 1px solid #c3e6cb;
          border-radius: 6px;
          padding: 20px;
          text-align: center;
        }

        .protected-actions {
          display: flex;
          gap: 10px;
          justify-content: center;
          margin-top: 15px;
        }

        .action-button {
          padding: 8px 16px;
          background: #28a745;
          color: white;
          border: none;
          border-radius: 4px;
          cursor: pointer;
          font-size: 14px;
        }

        .action-button:hover:not(:disabled) {
          background: #218838;
        }

        .action-button:disabled {
          opacity: 0.6;
          cursor: not-allowed;
        }
      `}</style>
    </div>
  );
};

export default TestQueueItPage; 