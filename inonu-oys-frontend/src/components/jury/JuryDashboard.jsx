import React, { useState, useEffect, useCallback } from 'react';
import api from '../../api';
import Notification from '../Notification';
import './JuryDashboard.css';

const statusLabel = {
  SUBMITTED: 'Başvuru Alındı',
  PENDING_EVALUATION: 'Değerlendirilecek',
  COMPLETED: 'Tamamlandı',
  REJECTED: 'Reddedildi',
};

const evaluationProfiles = [
  {
    match: ['Müzik Öğretmenliği', 'Müzik Bilimleri', 'Müzikoloji'],
    criteria: ['İşitme / kulak', 'Ritim', 'Ses / şan', 'Enstrüman veya performans', 'Müzikal ifade'],
  },
  {
    match: ['Beden', 'Antrenörlük', 'Spor Yöneticiliği', 'Engellilerde'],
    criteria: ['Sprint', 'Slalom', 'Engel geçme', 'Denge / takla', 'Top kontrolü ve hedef atış'],
  },
  {
    match: ['Resim'],
    criteria: ['Çizim becerisi', 'Gözlem', 'Oran-orantı', 'Kompozisyon', 'Yaratıcılık'],
  },
  {
    match: ['Seramik'],
    criteria: ['Şekillendirme', 'Üç boyutlu algı', 'Hacim', 'Tasarım', 'Yaratıcılık'],
  },
];

const getEvaluationCriteria = (programName = '') =>
  evaluationProfiles.find((profile) => profile.match.some((key) => programName.includes(key)))?.criteria
  || ['Yetenek', 'Yaratıcılık', 'Estetik bakış', 'Alan bilgisi', 'Potansiyel'];

const parsePerformancePreferences = (value) => {
  if (!value) return null;
  try {
    return JSON.parse(value);
  } catch {
    return null;
  }
};

const parseJurySpecialties = (value) => {
  if (!value) return [];
  try {
    const parsed = JSON.parse(value);
    return Array.isArray(parsed) ? parsed : [];
  } catch {
    return [];
  }
};

const jurySpecialtyOptions = [
  {
    scope: 'MUSIC',
    group: 'Müzik - Enstrüman',
    items: ['Bağlama', 'Gitar', 'Piyano', 'Keman', 'Ud', 'Ney', 'Flüt', 'Klarnet', 'Viyolonsel', 'Kanun', 'Perküsyon']
      .map((detail) => `Enstrüman performansı: ${detail}`),
  },
  {
    scope: 'MUSIC',
    group: 'Müzik - Vokal / İşitme',
    items: [
      'Şan / vokal performansı: Türk halk müziği',
      'Şan / vokal performansı: Türk sanat müziği',
      'Şan / vokal performansı: Klasik batı müziği',
      'Şan / vokal performansı: Popüler müzik',
      'Şan / vokal performansı: Caz',
      'Ritim tekrarı',
      'Melodi tekrarı',
      'İşitme / kulak sınavı',
      'Solfej',
      'Doğaçlama / farklı performans',
    ],
  },
  {
    scope: 'SPORT',
    group: 'Spor - Ortak Parkur',
    items: ['Ortak Parkur Performansı', 'Sprint', 'Slalom', 'Engel geçme', 'Takla', 'Denge tahtası', 'Sağlık topu taşıma', 'Basketbol topu sürme', 'Futbol topuyla slalom', 'Hedefe top atma', 'Koordinasyon', 'Çeviklik'],
  },
  {
    scope: 'ART',
    group: 'Resim - Çizim Sınavı',
    items: ['Çizim Sınavı', 'Desen çizimi', 'Gözlem çizimi', 'Oran-orantı', 'Kompozisyon', 'Işık-gölge', 'Yaratıcı yorum'],
  },
  {
    scope: 'CERAMIC',
    group: 'Seramik',
    items: ['Şekillendirme', 'Üç boyutlu algı', 'Hacim kurgusu', 'Yüzey tasarımı', 'Modelleme', 'Yaratıcı form'],
  },
];

function JuryDashboard({ user, onLogout }) {
  const [applications, setApplications] = useState([]);
  const [departments, setDepartments] = useState([]);
  const [examSessions, setExamSessions] = useState([]);
  const [inactiveSlots, setInactiveSlots] = useState([]);
  const [inactiveForm, setInactiveForm] = useState({ departmentId: '', inactiveDate: '', startTime: '', endTime: '', reason: '' });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  
  const [selectedApp, setSelectedApp] = useState(null);
  const [talentScore, setTalentScore] = useState('');
  const [criteriaScores, setCriteriaScores] = useState({});
  const [scoreComment, setScoreComment] = useState('');
  const [activeList, setActiveList] = useState('pending');
  const [activeSection, setActiveSection] = useState('candidates');
  const [evaluationTab, setEvaluationTab] = useState('info');
  const [jurySpecialties, setJurySpecialties] = useState(parseJurySpecialties(user?.jurySpecialties));
  
  const [notification, setNotification] = useState({ message: '', type: '' });

  const fetchJuryApplications = useCallback(async () => {
    if (!user?.id) return;
    try {
      setLoading(true);
      setError('');
      const [applicationResponse, departmentResponse, inactiveResponse, sessionResponse] = await Promise.all([
        api.get('/jury/applications'),
        api.get('/departments/all'),
        api.get('/jury/inactive-slots'),
        api.get('/exam-sessions/my'),
      ]);
      setApplications(applicationResponse.data || []);
      setDepartments(departmentResponse.data || []);
      setInactiveSlots(inactiveResponse.data || []);
      setExamSessions(sessionResponse.data || []);
    } catch {
      setError('Size atanan başvurular yüklenemedi.');
    } finally {
      setLoading(false);
    }
  }, [user]);

  useEffect(() => {
    fetchJuryApplications();
  }, [fetchJuryApplications]);

  useEffect(() => {
    setJurySpecialties(parseJurySpecialties(user?.jurySpecialties));
  }, [user?.jurySpecialties]);

  const handleSelectApp = (app) => {
    setSelectedApp(app);
    setEvaluationTab('info');
    setTalentScore(app.currentJuryScore ?? '');
    setCriteriaScores({});
    setScoreComment('');
  };

  const handleSaveScore = async () => {
    const missingCriteria = selectedCriteria.filter((criterion) =>
      criteriaScores[criterion] === undefined || criteriaScores[criterion] === '');
    if (selectedApp?.currentJuryScore == null && missingCriteria.length) {
      setNotification({ message: 'Lütfen tüm değerlendirme kriterlerine 0-100 arası puan girin.', type: 'error' });
      return;
    }
    if (!selectedApp || talentScore === '' || parseFloat(talentScore) < 0 || parseFloat(talentScore) > 100) {
      setNotification({ message: 'Lütfen 0-100 arası geçerli bir puan girin.', type: 'error' });
      return;
    }
    if (!window.confirm(`${selectedApp.applicantFullName} için ${talentScore} puanı kilitlenecek. Onaylıyor musunuz?`)) {
      return;
    }
    try {
      const payload = {
        applicationId: selectedApp.id,
        score: parseFloat(talentScore),
        comment: scoreComment,
        criteriaScores: JSON.stringify(criteriaScores),
      };
      await api.post('/jury/score', payload);
      setNotification({ message: 'Puanınız bu aday için başarıyla kaydedildi.', type: 'success' });
      setSelectedApp(null);
      fetchJuryApplications();
    } catch (err) {
      console.error("Puan kaydetme hatası:", err);
      if (err.response) {
        const msg = typeof err.response.data === 'object' 
          ? (err.response.data.message || `Sunucu Hatası (${err.response.status})`)
          : err.response.data;
        setNotification({ message: msg, type: 'error' });
      } else {
        setNotification({ message: 'Sunucuya ulaşılamadı!', type: 'error' });
      }
    }
  };

  const renderDocumentLink = (docPath, docName) => {
    if (!docPath) return <span className="jury-doc-missing">{docName} yüklenmedi</span>;
    return <button type="button" onClick={async () => {
      try {
        const { data } = await api.get(`/files/${docPath}`, { responseType: 'blob' });
        const url = URL.createObjectURL(data);
        window.open(url, '_blank', 'noopener,noreferrer');
        window.setTimeout(() => URL.revokeObjectURL(url), 60000);
      } catch {
        setNotification({ message: 'Belge açılamadı.', type: 'error' });
      }
    }} className="jury-doc-button">{docName}</button>;
  };

  const handleCreateInactiveSlot = async (event) => {
    event.preventDefault();
    if (!inactiveForm.departmentId || !inactiveForm.inactiveDate) {
      setNotification({ message: 'Pasiflik için bölüm ve tarih seçin.', type: 'error' });
      return;
    }
    try {
      const payload = {
        departmentId: Number(inactiveForm.departmentId),
        inactiveDate: inactiveForm.inactiveDate,
        startTime: inactiveForm.startTime || null,
        endTime: inactiveForm.endTime || null,
        reason: inactiveForm.reason,
      };
      const { data } = await api.post('/jury/inactive-slots', payload);
      setInactiveSlots((items) => [...items, data]);
      setInactiveForm({ departmentId: '', inactiveDate: '', startTime: '', endTime: '', reason: '' });
      setNotification({ message: 'Pasiflik bildiriminiz kaydedildi.', type: 'success' });
    } catch (err) {
      setNotification({ message: err.response?.data || 'Pasiflik bildirimi kaydedilemedi.', type: 'error' });
    }
  };

  const specialtyScopeForDepartment = (departmentName = '') => {
    if (['Beden', 'Antrenörlük', 'Spor Yöneticiliği', 'Engellilerde'].some((key) => departmentName.includes(key))) return 'SPORT';
    if (['Müzik', 'Müzikoloji'].some((key) => departmentName.includes(key))) return 'MUSIC';
    if (departmentName.includes('Resim')) return 'ART';
    if (departmentName.includes('Seramik')) return 'CERAMIC';
    return 'GENERAL';
  };

  const getOwnSpecialtyGroups = () => {
    const scopes = new Set(assignedDepartments.map((department) => specialtyScopeForDepartment(department.name)));
    if (!scopes.size) {
      if (user?.juryField === 'SPOR') scopes.add('SPORT');
      if (user?.juryField === 'MUSIC') scopes.add('MUSIC');
      if (user?.juryField === 'ART') scopes.add('ART');
      if (user?.juryField === 'CERAMIC') scopes.add('CERAMIC');
      if (user?.juryField === 'GSF') {
        scopes.add('MUSIC');
        scopes.add('ART');
        scopes.add('CERAMIC');
      }
    }
    return jurySpecialtyOptions.filter((group) => scopes.has(group.scope));
  };

  const toggleOwnSpecialty = async (specialty) => {
    const next = jurySpecialties.includes(specialty)
      ? jurySpecialties.filter((item) => item !== specialty)
      : [...jurySpecialties, specialty];
    try {
      setJurySpecialties(next);
      await api.patch('/jury/specialties', {
        jurySpecialties: JSON.stringify([...new Set(next)].sort()),
      });
      setNotification({ message: 'Uzmanlık alanlarınız güncellendi.', type: 'success' });
    } catch (err) {
      setJurySpecialties(jurySpecialties);
      setNotification({ message: err.response?.data || 'Uzmanlık alanları güncellenemedi.', type: 'error' });
    }
  };

  const pendingApplications = applications.filter((app) => app.currentJuryScore == null);
  const scoredApplications = applications.filter((app) => app.currentJuryScore != null);
  const visibleApplications = activeList === 'pending' ? pendingApplications : scoredApplications;
  const assignedDepartments = departments.filter((department) => user?.assignedDepartmentIds?.includes(department.id));
  const formatDateTime = (value) => value
    ? new Intl.DateTimeFormat('tr-TR', { dateStyle: 'short', timeStyle: 'short' }).format(new Date(value))
    : '-';
  const selectedCriteria = selectedApp ? getEvaluationCriteria(selectedApp.programName) : [];
  const selectedPerformancePreferences = selectedApp ? parsePerformancePreferences(selectedApp.performancePreferences) : null;
  const filledCriteriaScores = selectedCriteria
    .map((criterion) => criteriaScores[criterion])
    .filter((score) => score !== undefined && score !== '')
    .map((score) => Number(score))
    .filter((score) => !Number.isNaN(score));
  const calculatedCriteriaScore = filledCriteriaScores.length === selectedCriteria.length && selectedCriteria.length
    ? Math.round((filledCriteriaScores.reduce((sum, score) => sum + score, 0) / selectedCriteria.length) * 100) / 100
    : '';

  const updateCriteriaScore = (criterion, value) => {
    const normalizedValue = value === '' ? '' : Math.max(0, Math.min(100, Number(value)));
    setCriteriaScores((items) => ({ ...items, [criterion]: normalizedValue }));
    const nextScores = { ...criteriaScores, [criterion]: normalizedValue };
    const values = selectedCriteria
      .map((item) => nextScores[item])
      .filter((score) => score !== undefined && score !== '')
      .map((score) => Number(score))
      .filter((score) => !Number.isNaN(score));
    if (values.length === selectedCriteria.length && selectedCriteria.length) {
      const average = Math.round((values.reduce((sum, score) => sum + score, 0) / selectedCriteria.length) * 100) / 100;
      setTalentScore(String(average));
    }
  };

  return (
    <div className="admin-container">
      <Notification 
        message={notification.message} 
        type={notification.type} 
        onClose={() => setNotification({ message: '', type: '' })} 
      />
      <nav className="navbar">
        <div className="navbar-brand">
          <h2>İNÖNÜ ÜNİVERSİTESİ</h2>
          <p>Jüri Değerlendirme Paneli</p>
        </div>
        <div className="user-info">
          <span className="user-badge">Jüri Üyesi</span>
          <span>Hoş geldiniz, <strong>{user?.firstName} {user?.lastName}</strong></span>
          <button onClick={onLogout} className="jury-btn jury-btn-danger">Sistemden Çıkış</button>
        </div>
      </nav>

      <main className="admin-content">
        {loading ? <div className="admin-loading-text">Başvurular yükleniyor...</div> : error ? <div className="error-msg">{error}</div> : (
          <>
          <section className="jury-top-switcher">
            <button className={activeSection === 'candidates' ? 'active' : ''} onClick={() => setActiveSection('candidates')}>Adaylar <span>{pendingApplications.length}</span></button>
            <button className={activeSection === 'sessions' ? 'active' : ''} onClick={() => setActiveSection('sessions')}>Sinav Gorevlerim <span>{examSessions.length}</span></button>
            <button className={activeSection === 'specialties' ? 'active' : ''} onClick={() => setActiveSection('specialties')}>Uzmanliklarim <span>{jurySpecialties.length}</span></button>
            <button className={activeSection === 'inactive' ? 'active' : ''} onClick={() => setActiveSection('inactive')}>Pasiflik <span>{inactiveSlots.length}</span></button>
          </section>
          <section className="jury-profile-panel">
            <div>
              <small>Jüri Profili</small>
              <h3>{user?.firstName} {user?.lastName}</h3>
              <p>T.C. {user?.username}</p>
            </div>
            <div className="jury-workload">
              <span><strong>{pendingApplications.length}</strong> değerlendirilecek</span>
              <span><strong>{scoredApplications.length}</strong> değerlendirilmiş</span>
            </div>
            <div className="jury-assigned-departments">
              <small>Atanan Bölümler</small>
              <div>
                {assignedDepartments.length
                  ? assignedDepartments.map((department) => <span key={department.id}>{department.name}</span>)
                  : <span>Atanmış bölüm bulunmuyor</span>}
              </div>
            </div>
          </section>
          {activeSection === 'specialties' && <section className="jury-profile-panel jury-specialty-panel">
            <div className="jury-specialty-copy">
              <small>Uzmanlıklarım</small>
              <h3>Değerlendirebileceğiniz alanları seçin</h3>
              <p>Seçimleriniz aday-jüri eşleşmesinde kullanılır. Bölümünüze uygun alanları istediğiniz zaman güncelleyebilirsiniz.</p>
              <span>{jurySpecialties.length} uzmanlık seçili</span>
            </div>
            <div className="jury-specialty-editor">
              {getOwnSpecialtyGroups().length ? getOwnSpecialtyGroups().map((group) => (
                <div className="jury-specialty-group" key={group.group}>
                  <strong>{group.group}</strong>
                  <div className="jury-specialty-chip-grid">
                    {group.items.map((specialty) => {
                      const active = jurySpecialties.includes(specialty);
                      return (
                        <button
                          type="button"
                          key={specialty}
                          className={`jury-specialty-chip ${active ? 'active' : ''}`}
                          onClick={() => toggleOwnSpecialty(specialty)}
                        >
                          {active ? '✓ ' : '+ '}{specialty}
                        </button>
                      );
                    })}
                  </div>
                </div>
              )) : <p>Uzmanlık seçebilmek için önce bölüm atamanız yapılmalı.</p>}
            </div>
          </section>}
          {activeSection === 'sessions' && <section className="jury-profile-panel">
            <div>
              <small>Sınav Görevlerim</small>
              <h3>Atandığınız oturumlar</h3>
              <p>Asıl veya yedek görevleriniz burada görünür.</p>
            </div>
            <div className="jury-session-list">
              {examSessions.length
                ? examSessions.map((session) => {
                    const ownAssignment = (session.juries || []).find((jury) => jury.juryId === user.id);
                    return <span key={session.id}>
                      {session.departmentName} · {session.examDate} {session.startTime}-{session.endTime} · {session.room} · {ownAssignment?.replacement ? 'Yedek' : 'Asıl'}
                    </span>;
                  })
                : <span>Henüz sınav görevi atanmadı</span>}
            </div>
          </section>}
          {activeSection === 'inactive' && <section className="jury-profile-panel">
            <div>
              <small>Sınav Günü Pasiflik</small>
              <h3>Müsait olmadığınız zamanı bildirin</h3>
              <p>Saat girmezseniz tüm gün pasif sayılırsınız.</p>
            </div>
            <form className="jury-inactive-form" onSubmit={handleCreateInactiveSlot}>
              <select value={inactiveForm.departmentId} onChange={(e) => setInactiveForm({ ...inactiveForm, departmentId: e.target.value })} required>
                <option value="">Bölüm seçin</option>
                {assignedDepartments.map((department) => <option key={department.id} value={department.id}>{department.name}</option>)}
              </select>
              <input type="date" value={inactiveForm.inactiveDate} onChange={(e) => setInactiveForm({ ...inactiveForm, inactiveDate: e.target.value })} required />
              <input type="time" value={inactiveForm.startTime} onChange={(e) => setInactiveForm({ ...inactiveForm, startTime: e.target.value })} />
              <input type="time" value={inactiveForm.endTime} onChange={(e) => setInactiveForm({ ...inactiveForm, endTime: e.target.value })} />
              <input placeholder="Sebep (opsiyonel)" value={inactiveForm.reason} onChange={(e) => setInactiveForm({ ...inactiveForm, reason: e.target.value })} />
              <button className="jury-btn jury-btn-primary" type="submit">Pasife Al</button>
            </form>
            <div className="jury-assigned-departments">
              <small>Kayıtlı Pasiflikler</small>
              <div>
                {inactiveSlots.length
                  ? inactiveSlots.map((slot) => <span key={slot.id}>{slot.departmentName} · {slot.inactiveDate}{slot.startTime ? ` ${slot.startTime}-${slot.endTime}` : ' tüm gün'}</span>)
                  : <span>Pasiflik bildirimi yok</span>}
              </div>
            </div>
          </section>}
          {activeSection === 'candidates' && selectedApp ? (
            <div className="jury-evaluation-page">
              <div className="jury-evaluation-header">
                <div>
                  <small>Aday Değerlendirme</small>
                  <h3>{selectedApp.applicantFullName}</h3>
                  <p>{selectedApp.programName} · T.C. {selectedApp.applicantUsername}</p>
                </div>
                <button className="jury-btn jury-btn-secondary" onClick={() => setSelectedApp(null)}>Aday Listesine Dön</button>
              </div>
              <div className="jury-evaluation-tabs">
                <button className={evaluationTab === 'info' ? 'active' : ''} onClick={() => setEvaluationTab('info')}>Aday Bilgileri</button>
                <button className={evaluationTab === 'documents' ? 'active' : ''} onClick={() => setEvaluationTab('documents')}>Belgeler</button>
                <button className={evaluationTab === 'score' ? 'active' : ''} onClick={() => setEvaluationTab('score')}>Değerlendirme</button>
                <button className={evaluationTab === 'assignment' ? 'active' : ''} onClick={() => setEvaluationTab('assignment')}>Jüri Eşleşmesi</button>
              </div>
              <div className="jury-evaluation-content">
                {evaluationTab === 'info' && (
                  <div className="jury-candidate-summary">
                    <small>Aday Profili</small>
                    <h5>{selectedApp.applicantFullName}</h5>
                    <div className="jury-profile-grid">
                      <p><span>T.C.</span>{selectedApp.applicantUsername}</p>
                      <p><span>Bölüm</span>{selectedApp.programName}</p>
                      <p><span>TYT</span>{selectedApp.tytScore ?? '-'}</p>
                      <p><span>OBP</span>{selectedApp.obp ?? '-'}</p>
                      <p><span>Milli Sporcu</span>{selectedApp.isNational ? 'Evet' : 'Hayır'}</p>
                      <p><span>Engelli Aday</span>{selectedApp.isDisabled ? 'Evet' : 'Hayır'}</p>
                    </div>
                    <div className="jury-doc-section">
                      <h6>Adayın Performans Tercihleri</h6>
                      {selectedPerformancePreferences?.selections?.length ? (
                        <div className="jury-preference-list">
                          {selectedPerformancePreferences.selections.map((selection) => {
                            const label = typeof selection === 'string' ? selection : selection.type;
                            const detail = typeof selection === 'string' ? '' : [selection.detail, selection.otherDetail].filter(Boolean).join(' / ');
                            return <span key={`${label}-${detail}`}>{detail ? `${label}: ${detail}` : label}</span>;
                          })}
                          {selectedPerformancePreferences.customPerformance && <span>Not: {selectedPerformancePreferences.customPerformance}</span>}
                        </div>
                      ) : (
                        <p className="jury-locked-note">Bu aday için performans tercihi kaydı bulunmuyor.</p>
                      )}
                    </div>
                  </div>
                )}
                {evaluationTab === 'documents' && (
                  <div className="jury-doc-section">
                    <h6>Aday Belgeleri</h6>
                    <div className="jury-doc-grid">
                      {renderDocumentLink(selectedApp.osymDocPath, 'ÖSYM Sonuç')}
                      {renderDocumentLink(selectedApp.diplomaDocPath, 'Diploma')}
                      {renderDocumentLink(selectedApp.healthDocPath, 'Sağlık Raporu')}
                      {renderDocumentLink(selectedApp.photoDocPath, 'Fotoğraf')}
                      {renderDocumentLink(selectedApp.nationalDocPath, 'Milli Sporcu Belgesi')}
                      {renderDocumentLink(selectedApp.disabledDocPath, 'Engelli Raporu')}
                    </div>
                  </div>
                )}
                {evaluationTab === 'assignment' && (
                  <div className="jury-doc-section jury-assignment-note">
                    <h6>Onaylı Jüri Eşleşmesi</h6>
                    {selectedApp.juryAssignments?.length ? (
                      <div className="jury-preference-list">
                        {selectedApp.juryAssignments.map((assignment) => (
                          <span key={assignment.id || assignment.juryId}>
                            {assignment.juryFullName} · {assignment.matchedAreas || 'Onaylı eşleşme'}
                          </span>
                        ))}
                      </div>
                    ) : (
                      <p className="jury-locked-note">Bu aday için onaylı jüri eşleşmesi bulunmuyor.</p>
                    )}
                  </div>
                )}
                {evaluationTab === 'score' && (
                  <div className="jury-score-box">
                    <h6>Bölüm Bazlı Değerlendirme Kriterleri</h6>
                    <p className="subtitle">{selectedApp.currentJuryScore == null ? 'Her kriteri 0-100 arasında puanlayın. Toplam puan otomatik ortalama olarak hesaplanır.' : `Puanlama tarihi: ${formatDateTime(selectedApp.currentJuryScoredAt)}`}</p>
                    {selectedApp.currentJuryScore == null && (
                      <div className="jury-criteria-grid">
                        {selectedCriteria.map((criterion) => (
                          <label key={criterion}>
                            <span>{criterion}</span>
                            <input type="number" min="0" max="100" step="0.01" value={criteriaScores[criterion] ?? ''} onChange={(e) => updateCriteriaScore(criterion, e.target.value)} />
                          </label>
                        ))}
                      </div>
                    )}
                    {calculatedCriteriaScore !== '' && selectedApp.currentJuryScore == null && (
                      <p className="jury-calculated-score">Hesaplanan toplam puan: <strong>{calculatedCriteriaScore}</strong></p>
                    )}
                    <textarea className="jury-comment-input" placeholder="Kısa jüri notu (opsiyonel)" value={scoreComment} disabled={selectedApp.currentJuryScore != null} onChange={(e) => setScoreComment(e.target.value)} />
                    <h6>Toplam Yetenek Sınavı Puanı</h6>
                    <input type="number" className="jury-score-input" min="0" max="100" step="0.01" placeholder="Puanı Girin" value={talentScore} disabled={selectedApp.currentJuryScore != null} onChange={(e) => setTalentScore(e.target.value)} />
                    {selectedApp.currentJuryScore == null ? (
                      <button className="jury-btn jury-btn-success jury-submit-score" onClick={handleSaveScore}>Puanı Onayla ve Kilitle</button>
                    ) : (
                      <p className="jury-locked-note">Bu aday için puanınız kilitlenmiş. Değişiklik gerekiyorsa admin ile görüşülmelidir.</p>
                    )}
                  </div>
                )}
              </div>
            </div>
          ) : activeSection === 'candidates' && <div className="admin-content-grid jury-list-only">
            <div className="admin-main-panel">
              <div className="panel-header-row">
                <h3>{activeList === 'pending' ? 'Değerlendirilecek Adaylar' : 'Değerlendirilmiş Adaylar'}</h3>
                <button className="jury-btn jury-btn-secondary" onClick={fetchJuryApplications}>Yenile</button>
              </div>
              <div className="jury-list-tabs">
                <button className={activeList === 'pending' ? 'active' : ''} onClick={() => { setActiveList('pending'); setSelectedApp(null); }}>
                  Değerlendirilecek <span>{pendingApplications.length}</span>
                </button>
                <button className={activeList === 'scored' ? 'active' : ''} onClick={() => { setActiveList('scored'); setSelectedApp(null); }}>
                  Değerlendirilmiş <span>{scoredApplications.length}</span>
                </button>
              </div>
              <div className="table-responsive-wrapper">
                <table className="admin-table">
                  <thead><tr><th>Aday</th><th>Bölüm</th><th>Durum</th><th>İşlem</th></tr></thead>
                  <tbody>
                    {visibleApplications.map(app => (
                      <tr key={app.id} className={selectedApp?.id === app.id ? 'row-selected jury-clickable-row' : 'jury-clickable-row'} onClick={() => handleSelectApp(app)}>
                        <td>
                          <div className="jury-person-cell">
                            <span>{app.applicantFullName}</span>
                            <small>T.C. {app.applicantUsername}</small>
                          </div>
                        </td>
                        <td><span className="jury-program-chip">{app.programName}</span></td>
                        <td>
                          <span className={`jury-status status-${String(app.status).toLowerCase()}`}>{app.currentJuryScore != null ? `Puanınız: ${app.currentJuryScore}` : (statusLabel[app.status] || app.status)}</span>
                        </td>
                        <td className="jury-table-actions"><button className="jury-btn jury-btn-primary" onClick={(event) => { event.stopPropagation(); handleSelectApp(app); }}>{app.currentJuryScore == null ? 'Değerlendir' : 'Görüntüle'}</button></td>
                      </tr>
                    ))}
                  </tbody>
                </table>
                {visibleApplications.length === 0 && <div className="admin-empty-text">{activeList === 'pending' ? 'Değerlendirilecek aday bulunmuyor.' : 'Henüz değerlendirdiğiniz aday yok.'}</div>}
              </div>
            </div>
            <div className="admin-sidebar-panel">
              {selectedApp ? (
                <div className="detail-card animated-fade">
                  <div className="detail-header">
                    <h4>Aday Değerlendirme</h4>
                    <button className="btn-close-detail" onClick={() => setSelectedApp(null)}>✕</button>
                  </div>
                  <div className="detail-body">
                    <div className="jury-candidate-summary">
                      <small>Aday Profili</small>
                      <h5>{selectedApp.applicantFullName}</h5>
                      <div className="jury-profile-grid">
                        <p><span>T.C.</span>{selectedApp.applicantUsername}</p>
                        <p><span>Bölüm</span>{selectedApp.programName}</p>
                        <p><span>TYT</span>{selectedApp.tytScore ?? '-'}</p>
                        <p><span>OBP</span>{selectedApp.obp ?? '-'}</p>
                        <p><span>Milli Sporcu</span>{selectedApp.isNational ? 'Evet' : 'Hayır'}</p>
                        <p><span>Engelli Aday</span>{selectedApp.isDisabled ? 'Evet' : 'Hayır'}</p>
                      </div>
                    </div>
                    <div className="jury-doc-section">
                      <h6>Adayın Performans Tercihleri</h6>
                      {selectedPerformancePreferences?.selections?.length ? (
                        <div className="jury-preference-list">
                          {selectedPerformancePreferences.selections.map((selection) => {
                            const label = typeof selection === 'string' ? selection : selection.type;
                            const detail = typeof selection === 'string' ? '' : [selection.detail, selection.otherDetail].filter(Boolean).join(' / ');
                            return <span key={`${label}-${detail}`}>{detail ? `${label}: ${detail}` : label}</span>;
                          })}
                          {selectedPerformancePreferences.customPerformance && <span>Not: {selectedPerformancePreferences.customPerformance}</span>}
                        </div>
                      ) : (
                        <p className="jury-locked-note">Bu aday için performans tercihi kaydı bulunmuyor.</p>
                      )}
                    </div>
                    <div className="jury-doc-section jury-assignment-note">
                      <h6>Onaylı Jüri Eşleşmesi</h6>
                      {selectedApp.juryAssignments?.length ? (
                        <div className="jury-preference-list">
                          {selectedApp.juryAssignments.map((assignment) => (
                            <span key={assignment.id || assignment.juryId}>
                              {assignment.juryFullName} · {assignment.matchedAreas || 'Onaylı eşleşme'}
                            </span>
                          ))}
                        </div>
                      ) : (
                        <p className="jury-locked-note">Bu aday için onaylı jüri eşleşmesi bulunmuyor.</p>
                      )}
                    </div>
                    <div className="jury-doc-section">
                      <h6>Aday Belgeleri</h6>
                      <div className="jury-doc-grid">
                        {renderDocumentLink(selectedApp.osymDocPath, 'ÖSYM Sonuç')}
                        {renderDocumentLink(selectedApp.diplomaDocPath, 'Diploma')}
                        {renderDocumentLink(selectedApp.healthDocPath, 'Sağlık Raporu')}
                        {renderDocumentLink(selectedApp.photoDocPath, 'Fotoğraf')}
                        {renderDocumentLink(selectedApp.nationalDocPath, 'Milli Sporcu Belgesi')}
                        {renderDocumentLink(selectedApp.disabledDocPath, 'Engelli Raporu')}
                      </div>
                    </div>
                    <div className="jury-score-box">
                      <h6>Bölüm Bazlı Değerlendirme Kriterleri</h6>
                      <p className="subtitle">{selectedApp.currentJuryScore == null ? 'Her kriteri 0-100 arasında puanlayın. Toplam puan otomatik ortalama olarak hesaplanır.' : `Puanlama tarihi: ${formatDateTime(selectedApp.currentJuryScoredAt)}`}</p>
                      {selectedApp.currentJuryScore == null && (
                        <div className="jury-criteria-grid">
                          {selectedCriteria.map((criterion) => (
                            <label key={criterion}>
                              <span>{criterion}</span>
                              <input
                                type="number"
                                min="0"
                                max="100"
                                step="0.01"
                                value={criteriaScores[criterion] ?? ''}
                                onChange={(e) => updateCriteriaScore(criterion, e.target.value)}
                              />
                            </label>
                          ))}
                        </div>
                      )}
                      {calculatedCriteriaScore !== '' && selectedApp.currentJuryScore == null && (
                        <p className="jury-calculated-score">Hesaplanan toplam puan: <strong>{calculatedCriteriaScore}</strong></p>
                      )}
                      <textarea
                        className="jury-comment-input"
                        placeholder="Kısa jüri notu (opsiyonel)"
                        value={scoreComment}
                        disabled={selectedApp.currentJuryScore != null}
                        onChange={(e) => setScoreComment(e.target.value)}
                      />
                      <h6>Toplam Yetenek Sınavı Puanı</h6>
                      <input 
                        type="number" 
                        className="jury-score-input"
                        min="0"
                        max="100"
                        step="0.01"
                        placeholder="Puanı Girin"
                        value={talentScore}
                        disabled={selectedApp.currentJuryScore != null}
                        onChange={(e) => setTalentScore(e.target.value)}
                      />
                      {selectedApp.currentJuryScore == null ? (
                        <button className="jury-btn jury-btn-success jury-submit-score" onClick={handleSaveScore}>Puanı Onayla ve Kilitle</button>
                      ) : (
                        <p className="jury-locked-note">Bu aday için puanınız kilitlenmiş. Değişiklik gerekiyorsa admin ile görüşülmelidir.</p>
                      )}
                    </div>
                  </div>
                </div>
              ) : (
                <div className="sidebar-placeholder"><p>Adayı değerlendirmek ve puan girmek için sol listeden “Değerlendir” butonuna basın.</p></div>
              )}
            </div>
          </div>}
          </>
        )}
      </main>
    </div>
  );
}

export default JuryDashboard;
