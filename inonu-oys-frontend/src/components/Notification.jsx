import React, { useEffect } from 'react';
import './Notification.css';

function Notification({ message, type, onClose }) {
  useEffect(() => {
    if (message) {
      const timer = setTimeout(() => {
        onClose();
      }, 4000); // 4 saniye sonra kapanacak

      return () => clearTimeout(timer);
    }
  }, [message, onClose]);

  if (!message) {
    return null;
  }

  return (
    // Bu konteyner, bildirimlerin sağ üst köşede birikmesini sağlar
    <div className="notification-container">
      <div className={`notification-box notification-${type}`}>
        <div className="notification-icon">
          {type === 'success' ? '✓' : '✕'}
        </div>
        <div className="notification-content">
          <h4>{type === 'success' ? 'Başarılı!' : 'Uyarı'}</h4>
          <p>{message}</p>
        </div>
        <button onClick={onClose} className="notification-close-btn">&times;</button>
      </div>
    </div>
  );
}

export default Notification;
