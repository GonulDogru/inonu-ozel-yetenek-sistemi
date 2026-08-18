import React, { useState, useEffect } from 'react';
import api from './api';
import './App.css';

import ApplicationForm from './components/ApplicationForm';
import AdminDashboard from './components/admin/AdminDashboard';
import JuryDashboard from './components/jury/JuryDashboard';
import ApplicantDashboard from './components/applicant/ApplicantDashboard';

function App() {
  const [isLogin, setIsLogin] = useState(true);
  const [user, setUser] = useState(() => {
    try {
      return JSON.parse(localStorage.getItem('user')) || null;
    } catch {
      return null;
    }
  });
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');
  const [error, setError] = useState('');
  
  const [isApplying, setIsApplying] = useState(false);
  const [applicationData, setApplicationData] = useState(null);
  const [isStatusChecking, setIsStatusChecking] = useState(false);

  const checkApplicationStatus = async (currentUsername) => {
    setIsStatusChecking(true);
    try {
      const response = await api.get(`/applications/by-username/${currentUsername}`);
      setApplicationData(response.data || null);
    } catch (err) {
      setApplicationData(null);
      if (err.response?.status !== 404) {
        console.error("Başvuru durumu sorgulanırken hata:", err);
      }
    } finally {
      setIsStatusChecking(false);
    }
  };

  useEffect(() => {
    if (user && user.role === 'APPLICANT') {
      checkApplicationStatus(user.username);
    }
  }, [user]);

  const handleLoginSubmit = async (e) => {
    e.preventDefault();
    setError('');

    if (username.length !== 11 || !/^\d+$/.test(username)) {
        setError('T.C. Kimlik Numarası 11 haneli bir sayı olmalıdır.');
        return;
    }

    if (!password) {
      setError('Lütfen şifrenizi girin.');
      return;
    }

    try {
      if (isLogin) {
        const response = await api.post('/auth/login', { username, password });
        localStorage.setItem('token', response.data.token);
        localStorage.setItem('user', JSON.stringify(response.data.user));
        setUser(response.data.user);
      } else {
        if (!firstName || !lastName) {
          setError('Lütfen ad ve soyad alanlarını doldurun!');
          return;
        }
        const registerData = { username, password, firstName, lastName, role: 'APPLICANT' };
        await api.post('/auth/register', registerData);
        setError('');
        alert('Kaydınız başarıyla oluşturuldu! Şimdi giriş yapabilirsiniz.');
        setIsLogin(true);
      }
    } catch (err) {
      console.error(err);
      setError(err.response?.data?.message || err.response?.data || 'İşlem başarısız!');
    }
  };

  const handleLogout = () => {
    setUser(null);
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    setUsername('');
    setPassword('');
    setError('');
    setIsApplying(false);
    setApplicationData(null);
  };

  const handleApplicationSuccess = () => {
    setIsApplying(false);
    checkApplicationStatus(user.username);
  };

  if (user && user.role === 'APPLICANT' && isStatusChecking) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh', fontFamily: 'sans-serif', color: '#162447' }}>
        <h3>Sistem Bilgileri Doğrulanıyor, Lütfen Bekleyin...</h3>
      </div>
    );
  }

  if (user) {
    switch (user.role) {
      case 'ADMIN':
      case 'SUPER_ADMIN':
      case 'DEPARTMENT_ADMIN':
        return <AdminDashboard user={user} onLogout={handleLogout} />;
      case 'JURY':
        return <JuryDashboard user={user} onLogout={handleLogout} />;
      case 'APPLICANT':
        if (isApplying) {
          return <ApplicationForm user={user} onBackToDashboard={handleApplicationSuccess} />;
        }
        return <ApplicantDashboard 
                  user={user} 
                  applicationData={applicationData} 
                  onLogout={handleLogout} 
                  onStartApplication={() => setIsApplying(true)} 
               />;
      default:
        handleLogout();
        return null;
    }
  }

  return (
    <div className="auth-container">
      <div className="auth-card">
        <h2>İnönü Üniversitesi</h2>
        <p>Özel Yetenek Sınavı Otomasyonu</p>
        {error && <div className="error-msg">{error}</div>}
        <form onSubmit={handleLoginSubmit}>
          {!isLogin && (
            <>
              <div className="form-group"><label>Ad</label><input type="text" value={firstName} onChange={(e) => setFirstName(e.target.value)} placeholder="Adınızı girin" /></div>
              <div className="form-group"><label>Soyad</label><input type="text" value={lastName} onChange={(e) => setLastName(e.target.value)} placeholder="Soyadınızı girin" /></div>
            </>
          )}
          <div className="form-group">
            <label>T.C. Kimlik No</label>
            <input 
              type="text" 
              value={username} 
              onChange={(e) => {
                const val = e.target.value;
                if (/^\d*$/.test(val) && val.length <= 11) {
                  setUsername(val);
                }
              }} 
              placeholder="T.C. Kimlik numaranızı girin" 
            />
          </div>
          <div className="form-group"><label>Şifre</label><input type="password" value={password} onChange={(e) => setPassword(e.target.value)} placeholder="Şifrenizi girin" /></div>
          <button type="submit" className="btn-primary">{isLogin ? 'Giriş Yap' : 'Kayıt Ol'}</button>
        </form>
        <div className="toggle-auth">
          {isLogin ? (<p>Aday kaydınız yok mu? <span onClick={() => { setIsLogin(false); setError(''); }}>Hesap Oluştur</span></p>) : (<p>Zaten kayıtlı mısınız? <span onClick={() => { setIsLogin(true); setError(''); }}>Giriş Yap</span></p>)}
        </div>
      </div>
    </div>
  );
}

export default App;
