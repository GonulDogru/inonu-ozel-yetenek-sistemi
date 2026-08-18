import React, { useEffect, useState } from 'react';
import api from '../api';

const defaultSystemSettings = {
  applicationsOpen: true,
  applicationStartDate: '',
  applicationEndDate: '',
  minTytScore: 150,
  requireObp: false,
  requireOsymDocument: true,
  requireDiplomaDocument: true,
  requireHealthDocument: true,
  requirePhotoDocument: true,
  requireNationalDocument: true,
  requireDisabledDocument: true,
};

const normalizeText = (value = '') =>
  String(value)
    .toLocaleLowerCase('tr-TR')
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .replace(/ı/g, 'i');

const hasAnyKeyword = (value, keywords) => {
  const normalizedValue = normalizeText(value);
  return keywords.some((keyword) => normalizedValue.includes(normalizeText(keyword)));
};

const isSportDepartment = (department) =>
  hasAnyKeyword(department?.name, ['Beden', 'Antrenörlük', 'Spor Yöneticiliği', 'Engellilerde']);

const isDrawingOnlyDepartment = (department) =>
  hasAnyKeyword(department?.name, ['Resim']);

const automaticPerformanceFor = (department) => {
  if (isSportDepartment(department)) {
    return {
      departmentName: department?.name || '',
      mode: 'AUTO',
      examFormat: 'Ortak Parkur Performansı',
      selections: [
        {
          type: 'Ortak Parkur Performansı',
          detail: 'Sprint, slalom, engel geçme, takla, denge, sağlık topu taşıma, basketbol topu sürme, futbol slalom ve hedefe top atma kriterleriyle değerlendirilir.',
          otherDetail: '',
        },
      ],
      legacySelections: ['Ortak Parkur Performansı'],
    };
  }
  if (isDrawingOnlyDepartment(department)) {
    return {
      departmentName: department?.name || '',
      mode: 'AUTO',
      examFormat: 'Çizim Sınavı',
      selections: [
        {
          type: 'Çizim Sınavı',
          detail: 'Çizim becerisi, gözlem, oran-orantı, kompozisyon, ışık-gölge ve yaratıcılık kriterleriyle değerlendirilir.',
          otherDetail: '',
        },
      ],
      legacySelections: ['Çizim Sınavı'],
    };
  }
  return null;
};

const performanceProfiles = [
  {
    match: ['Müzik Öğretmenliği', 'Müzik Bilimleri', 'Müzikoloji', 'Müzik'],
    title: 'Müzik Performans Tercihleri',
    description: 'Sınavda göstermek istediğiniz 3 performansı seçin ve her seçim için detay belirtin.',
    options: [
      { label: 'Enstrüman performansı', detailLabel: 'Çalacağınız enstrüman', details: ['Bağlama', 'Gitar', 'Piyano', 'Keman', 'Ud', 'Ney', 'Flüt', 'Klarnet', 'Viyolonsel', 'Kanun', 'Perküsyon', 'Diğer'] },
      { label: 'Şan / vokal performansı', detailLabel: 'Vokal alanı', details: ['Türk halk müziği', 'Türk sanat müziği', 'Klasik batı müziği', 'Popüler müzik', 'Caz', 'Diğer'] },
      { label: 'Ritim tekrarı', detailLabel: 'Ritim düzeyi', details: ['Temel ritim', 'Orta düzey ritim', 'Karma ritim'] },
      { label: 'Melodi tekrarı', detailLabel: 'Melodi düzeyi', details: ['Tek ses', 'İki ses', 'Kısa ezgi', 'Uzun ezgi'] },
      { label: 'İşitme / kulak sınavı', detailLabel: 'İşitme alanı', details: ['Tek ses işitme', 'Çift ses işitme', 'Akor işitme', 'Ezgi işitme'] },
      { label: 'Solfej', detailLabel: 'Solfej düzeyi', details: ['Başlangıç', 'Orta', 'İleri'] },
      { label: 'Doğaçlama / farklı performans', detailLabel: 'Performans türü', details: ['Kendi beste/yorum', 'Doğaçlama vokal', 'Doğaçlama enstrüman', 'Diğer'] },
    ],
  },
  {
    match: ['Seramik'],
    title: 'Seramik Uygulama Alanları',
    description: 'Seramik alanında değerlendirilebilecek 3 uygulama başlığını seçin.',
    options: ['Şekillendirme', 'Üç boyutlu algı', 'Hacim kurgusu', 'Yüzey tasarımı', 'Modelleme', 'Yaratıcı form'].map((label) => ({ label })),
  },
];

function ApplicationForm({ user, onBackToDashboard }) {
  const [step, setStep] = useState(1);
  const [tytScore, setTytScore] = useState('');
  const [obp, setObp] = useState('');
  const [faculty, setFaculty] = useState('');
  const [selectedDepartmentId, setSelectedDepartmentId] = useState(null);
  const [departments, setDepartments] = useState([]);
  const [performanceSelections, setPerformanceSelections] = useState([]);
  const [performanceDetails, setPerformanceDetails] = useState({});
  const [isNationalAthlete, setIsNationalAthlete] = useState(false);
  const [isDisabledAthlete, setIsDisabledAthlete] = useState(false);
  const [files, setFiles] = useState({
    osymDoc: null,
    diplomaDoc: null,
    healthDoc: null,
    photoDoc: null,
    nationalDoc: null,
    disabledDoc: null,
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [systemSettings, setSystemSettings] = useState(defaultSystemSettings);

  useEffect(() => {
    Promise.all([
      api.get('/departments/all'),
      api.get('/system-settings'),
    ])
      .then(([departmentResponse, settingsResponse]) => {
        setDepartments(departmentResponse.data || []);
        setSystemSettings({ ...defaultSystemSettings, ...(settingsResponse.data || {}) });
      })
      .catch(() => setError('Başvuru ayarları veya bölüm listesi yüklenemedi.'));
  }, []);

  const selectedDepartment = departments.find((department) => department.id === Number(selectedDepartmentId));
  const performanceProfile = selectedDepartment
    ? performanceProfiles.find((profile) => profile.match.some((key) => hasAnyKeyword(selectedDepartment.name, [key])))
    : null;
  const automaticPerformance = selectedDepartment ? automaticPerformanceFor(selectedDepartment) : null;
  const selectedPerformanceOptions = performanceProfile
    ? performanceProfile.options.filter((option) => performanceSelections.includes(option.label))
    : [];
  const minTytScore = Number(systemSettings.minTytScore || 150);
  const today = new Date().toISOString().slice(0, 10);
  const applicationBlockedMessage = !systemSettings.applicationsOpen
    ? 'Başvurular şu anda Super Admin tarafından kapatılmış.'
    : systemSettings.applicationStartDate && today < systemSettings.applicationStartDate
      ? `Başvurular ${systemSettings.applicationStartDate} tarihinde başlayacak.`
      : systemSettings.applicationEndDate && today > systemSettings.applicationEndDate
        ? `Başvuru dönemi ${systemSettings.applicationEndDate} tarihinde sona ermiş.`
        : '';

  const documentRequirementText = (required) => required ? 'Zorunlu' : 'İsteğe bağlı';

  const resetPerformanceChoices = () => {
    setPerformanceSelections([]);
    setPerformanceDetails({});
  };

  const handleHeaderBack = () => {
    if (step > 1) {
      prevStep();
      return;
    }
    onBackToDashboard();
  };

  const handleFileChange = (e, fieldName) => {
    if (e.target.files && e.target.files[0]) {
      setFiles((prev) => ({ ...prev, [fieldName]: e.target.files[0] }));
    }
  };

  const requiredDocumentMissing = () => {
    const checks = [
      ['requireOsymDocument', 'ÖSYM sonuç belgesi', files.osymDoc],
      ['requireDiplomaDocument', 'diploma / mezuniyet belgesi', files.diplomaDoc],
      ['requireHealthDocument', 'sağlık raporu', files.healthDoc],
      ['requirePhotoDocument', 'biyometrik fotoğraf', files.photoDoc],
    ];
    const missing = checks.find(([settingField, , file]) => systemSettings[settingField] && !file);
    if (missing) return `${missing[1]} yüklenmelidir.`;
    if (isNationalAthlete && systemSettings.requireNationalDocument && !files.nationalDoc) {
      return 'Milli sporcu belgenizi yüklemeniz zorunludur.';
    }
    if (isDisabledAthlete && systemSettings.requireDisabledDocument && !files.disabledDoc) {
      return 'Engelli sağlık kurulu raporunuzu yüklemeniz zorunludur.';
    }
    return '';
  };

  const togglePerformanceSelection = (option) => {
    setPerformanceSelections((items) => {
      const label = option.label;
      if (items.includes(label)) {
        setPerformanceDetails((details) => {
          const next = { ...details };
          delete next[label];
          delete next[`${label}Other`];
          return next;
        });
        return items.filter((item) => item !== label);
      }
      if (items.length >= 3) {
        setError('En fazla 3 performans tercihi seçebilirsiniz.');
        return items;
      }
      setError('');
      return [...items, label];
    });
  };

  const updatePerformanceDetail = (label, value) => {
    setPerformanceDetails((details) => ({ ...details, [label]: value }));
  };

  const updatePerformanceOtherDetail = (label, value) => {
    setPerformanceDetails((details) => ({ ...details, [`${label}Other`]: value }));
  };

  const nextStep = () => {
    setError('');
    if (applicationBlockedMessage) {
      setError(applicationBlockedMessage);
      return;
    }
    if (step === 1) {
      if (!tytScore || parseFloat(tytScore) < minTytScore || parseFloat(tytScore) > 500) {
        setError(`Lütfen geçerli bir TYT puanı girin (Taban puan: ${minTytScore}).`);
        return;
      }
      if (systemSettings.requireObp && obp === '') {
        setError('OBP bilgisi zorunludur.');
        return;
      }
      if (obp !== '' && (parseFloat(obp) < 0 || parseFloat(obp) > 500)) {
        setError('Lütfen 0-500 arasında geçerli bir OBP girin.');
        return;
      }
    }
    if (step === 2 && (!faculty || !selectedDepartmentId)) {
      setError('Lütfen önce bir fakülte ve bölüm tercihi yapın.');
      return;
    }
    if (step === 3 && performanceProfile) {
      if (performanceSelections.length !== 3) {
        setError('Lütfen sınavda göstermek istediğiniz 3 performans/uygulama alanını seçin.');
        return;
      }
      const missingDetail = selectedPerformanceOptions.find((option) => option.details?.length && !performanceDetails[option.label]);
      if (missingDetail) {
        setError(`${missingDetail.label} için ${missingDetail.detailLabel || 'detay'} seçin.`);
        return;
      }
      const missingOtherDetail = selectedPerformanceOptions.find((option) =>
        performanceDetails[option.label] === 'Diğer' && !String(performanceDetails[`${option.label}Other`] || '').trim());
      if (missingOtherDetail) {
        setError(`${missingOtherDetail.label} için "Diğer" açıklaması girin.`);
        return;
      }
    }
    setStep((prev) => prev + 1);
  };

  const prevStep = () => {
    setError('');
    setStep((prev) => Math.max(1, prev - 1));
  };

  const handleFormSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    if (applicationBlockedMessage) {
      setError(applicationBlockedMessage);
      setLoading(false);
      return;
    }

    const missingDocumentMessage = requiredDocumentMissing();
    if (missingDocumentMessage) {
      setError(missingDocumentMessage);
      setLoading(false);
      return;
    }

    const formData = new FormData();
    formData.append('tytScore', tytScore);
    if (obp !== '') formData.append('obp', obp);
    formData.append('faculty', faculty);
    formData.append('departmentId', selectedDepartmentId);
    formData.append('performancePreferences', JSON.stringify(automaticPerformance || {
      departmentName: selectedDepartment?.name || '',
      selections: selectedPerformanceOptions.map((option) => ({
        type: option.label,
        detail: performanceDetails[option.label] || '',
        otherDetail: performanceDetails[`${option.label}Other`] || '',
      })),
      legacySelections: performanceSelections,
    }));
    formData.append('isNational', isNationalAthlete);
    formData.append('isDisabled', isDisabledAthlete);
    if (files.osymDoc) formData.append('osymDoc', files.osymDoc);
    if (files.diplomaDoc) formData.append('diplomaDoc', files.diplomaDoc);
    if (files.healthDoc) formData.append('healthDoc', files.healthDoc);
    if (files.photoDoc) formData.append('photoDoc', files.photoDoc);
    if (files.nationalDoc) formData.append('nationalDoc', files.nationalDoc);
    if (files.disabledDoc) formData.append('disabledDoc', files.disabledDoc);

    try {
      await api.post('/applications/apply', formData, { headers: { 'Content-Type': 'multipart/form-data' } });
      alert('Başvurunuz başarıyla sisteme kaydedildi ve ön değerlendirmeye alındı!');
      onBackToDashboard();
    } catch (err) {
      if (err.response) {
        setError(typeof err.response.data === 'object'
          ? (err.response.data.message || `Başvuru sırasında hata oluştu (Kod: ${err.response.status})`)
          : err.response.data);
      } else {
        setError('Sunucu bağlantısı kurulamadı! Lütfen backend projesini kontrol edin.');
      }
    } finally {
      setLoading(false);
    }
  };

  const openDepartments = departments.filter((department) => department.talentAdmissionEnabled !== false);
  const filteredDepartments = openDepartments.filter((department) => faculty === 'SPOR'
    ? hasAnyKeyword(department.name, ['Beden', 'Antrenörlük', 'Spor Yöneticiliği', 'Engellilerde'])
    : hasAnyKeyword(department.name, ['Resim', 'Müzik', 'Müzikoloji', 'Seramik']));

  return (
    <div className="form-page-container">
      <div className="form-header-card">
        <button className="btn-back-minimal" onClick={handleHeaderBack}>{step > 1 ? '← Önceki Adım' : '← Panele Dön'}</button>
        <h2>Özel Yetenek Sınavı Başvuru Formu</h2>
        <p>Hoş geldiniz, <strong style={{ color: '#fff' }}>{user.firstName} {user.lastName}</strong>. Lütfen adımları takip ederek başvurunuzu tamamlayın.</p>
        <div className="form-stepper">
          <div className={`step-pill ${step >= 1 ? 'active' : ''}`}>1. Akademik Bilgiler</div>
          <div className="step-line"></div>
          <div className={`step-pill ${step >= 2 ? 'active' : ''}`}>2. Bölüm Tercihi</div>
          <div className="step-line"></div>
          <div className={`step-pill ${step >= 3 ? 'active' : ''}`}>3. Performans Tercihleri</div>
          <div className="step-line"></div>
          <div className={`step-pill ${step >= 4 ? 'active' : ''}`}>4. Evrak Yükleme</div>
        </div>
      </div>

      <div className="form-body-card">
        {error && <div className="error-msg" style={{ marginBottom: '1.5rem' }}>{error}</div>}
        {applicationBlockedMessage && <div className="error-msg" style={{ marginBottom: '1.5rem' }}>{applicationBlockedMessage}</div>}

        {step === 1 && (
          <div className="step-content fade-in">
            <h3>1. TYT ve OBP Bilgileri</h3>
            <div className="form-group-custom">
              <label>2026 TYT Giriş Puanı</label>
              <input type="number" step="0.001" value={tytScore} onChange={(e) => setTytScore(e.target.value)} placeholder="Örn: 245.670" />
              <small>ÖSYM sonuç belgenizdeki TYT puanınızı girin. Güncel taban puan: {minTytScore}</small>
            </div>
            <div className="form-group-custom">
              <label>Ortaöğretim Başarı Puanı (OBP) {systemSettings.requireObp ? '(Zorunlu)' : '(İsteğe bağlı)'}</label>
              <input type="number" min="0" max="500" step="0.01" value={obp} onChange={(e) => setObp(e.target.value)} placeholder="Örn: 425.50" />
              <small>OBP bilginizi TYT puanı gibi manuel girin. Diploma/mezuniyet belgesi başvuru kontrolünde doğrulama için kullanılır.</small>
            </div>
            <div className="toggle-container-grid">
              <div className="toggle-box">
                <div><h4>Milli Sporcu Statüsü</h4><p>Milli sporcu kontenjanından yararlanmak istiyor musunuz?</p></div>
                <label className="switch"><input type="checkbox" checked={isNationalAthlete} onChange={(e) => setIsNationalAthlete(e.target.checked)} /><span className="slider round"></span></label>
              </div>
              <div className="toggle-box">
                <div><h4>Engelli Aday Statüsü</h4><p>Engelli aday kontenjan kural ve avantajlarından yararlanmak istiyor musunuz?</p></div>
                <label className="switch"><input type="checkbox" checked={isDisabledAthlete} onChange={(e) => setIsDisabledAthlete(e.target.checked)} /><span className="slider round"></span></label>
              </div>
            </div>
            <div className="form-navigation"><button className="btn-next-step" onClick={nextStep} disabled={Boolean(applicationBlockedMessage)}>İleri: Bölüm Tercihi →</button></div>
          </div>
        )}

        {step === 2 && (
          <div className="step-content fade-in">
            <h3>2. Fakülte ve Bölüm Tercih Modülü</h3>
            <p className="subtitle">Lütfen yetenek sınavına girmek istediğiniz ana fakülteyi seçerek altındaki branşı belirleyin.</p>
            <div className="faculty-grid">
              <div className={`faculty-card ${faculty === 'SPOR' ? 'selected' : ''}`} onClick={() => { setFaculty('SPOR'); setSelectedDepartmentId(null); resetPerformanceChoices(); }}>
                <div className="icon-badge">🏃</div><h4>Spor Bilimleri Fakültesi</h4><p>Beden Eğitimi, Antrenörlük, Yöneticilik ve Egzersiz bölümleri.</p>
              </div>
              <div className={`faculty-card ${faculty === 'GSF' ? 'selected' : ''}`} onClick={() => { setFaculty('GSF'); setSelectedDepartmentId(null); resetPerformanceChoices(); }}>
                <div className="icon-badge">🎨</div><h4>Güzel Sanatlar ve Tasarım Fakültesi</h4><p>Resim, Müzikoloji ve Seramik sanat dalları.</p>
              </div>
            </div>
            {faculty && (
              <div className="program-select-area fade-in">
                <h4>Hangi bölüm için sınava gireceksiniz?</h4>
                <div className="program-options-grid">
                  {filteredDepartments.map((department) => (
                    <div key={department.id} className={`program-option-item ${selectedDepartmentId === department.id ? 'active' : ''}`} onClick={() => { setSelectedDepartmentId(department.id); resetPerformanceChoices(); }}>
                      {department.name}
                    </div>
                  ))}
                </div>
              </div>
            )}
            <div className="form-navigation"><button className="btn-prev-step" onClick={prevStep}>← Geri Dön</button><button className="btn-next-step" onClick={nextStep}>İleri: Performans Tercihleri →</button></div>
          </div>
        )}

        {step === 3 && (
          <div className="step-content fade-in">
            <h3>3. Performans / Uygulama Tercihleri</h3>
            {performanceProfile ? (
              <>
                <p className="subtitle">{performanceProfile.description}</p>
                <div className="program-options-grid">
                  {performanceProfile.options.map((option) => (
                    <div key={option.label} className={`program-option-item ${performanceSelections.includes(option.label) ? 'active' : ''}`} onClick={() => togglePerformanceSelection(option)}>{option.label}</div>
                  ))}
                </div>
                <small>{performanceSelections.length}/3 seçim yapıldı.</small>
                {selectedPerformanceOptions.length > 0 && (
                  <div className="performance-detail-panel">
                    <h4>Seçilen performansların detayları</h4>
                    {selectedPerformanceOptions.map((option) => (
                      <div className="performance-detail-row" key={`detail-${option.label}`}>
                        <div><strong>{option.label}</strong><small>{option.detailLabel || 'Detay gerekmiyor'}</small></div>
                        {option.details?.length ? (
                          <div className="performance-detail-fields">
                            <select value={performanceDetails[option.label] || ''} onChange={(e) => updatePerformanceDetail(option.label, e.target.value)}>
                              <option value="">Seçin</option>
                              {option.details.map((detail) => <option key={detail} value={detail}>{detail}</option>)}
                            </select>
                            {performanceDetails[option.label] === 'Diğer' && <input value={performanceDetails[`${option.label}Other`] || ''} onChange={(e) => updatePerformanceOtherDetail(option.label, e.target.value)} placeholder="Lütfen belirtin" />}
                          </div>
                        ) : <span className="file-success-badge">Detay gerekmiyor</span>}
                      </div>
                    ))}
                  </div>
                )}
              </>
            ) : automaticPerformance ? (
              <div className="performance-detail-panel">
                <h4>{automaticPerformance.examFormat}</h4>
                <p className="subtitle">
                  Bu bölümde adaydan ayrıca performans seçimi alınmaz. Başvurunuz otomatik olarak
                  <strong> {automaticPerformance.examFormat}</strong> kapsamında kaydedilecek.
                </p>
                {automaticPerformance.selections.map((selection) => (
                  <div className="performance-detail-row" key={selection.type}>
                    <div>
                      <strong>{selection.type}</strong>
                      <small>{selection.detail}</small>
                    </div>
                    <span className="file-success-badge">Otomatik</span>
                  </div>
                ))}
              </div>
            ) : <p className="subtitle">Bu bölüm için özel tercih listesi tanımlı değil. Bir sonraki adıma geçebilirsiniz.</p>}
            <div className="form-navigation"><button className="btn-prev-step" onClick={prevStep}>← Geri Dön</button><button className="btn-next-step" onClick={nextStep}>İleri: Evrakları Yükle →</button></div>
          </div>
        )}

        {step === 4 && (
          <form onSubmit={handleFormSubmit} className="step-content fade-in">
            <h3>4. Güvenli Evrak ve Belge Transferi</h3>
            <p className="subtitle">Yüklenecek tüm belgelerin e-devlet onaylı veya taranmış resmi PDF/Görsel formatında olması şarttır.</p>
            <div className="file-upload-grid">
              <div className="upload-box-item"><label>Barkodlu ÖSYM Sonuç Belgesi ({documentRequirementText(systemSettings.requireOsymDocument)})</label><input type="file" accept=".pdf,.png,.jpg,.jpeg" onChange={(e) => handleFileChange(e, 'osymDoc')} />{files.osymDoc && <div className="file-success-badge">✓ {files.osymDoc.name}</div>}</div>
              <div className="upload-box-item"><label>Lise Diploması Örneği ({documentRequirementText(systemSettings.requireDiplomaDocument)})</label><input type="file" accept=".pdf,.png,.jpg,.jpeg" onChange={(e) => handleFileChange(e, 'diplomaDoc')} />{files.diplomaDoc && <div className="file-success-badge">✓ {files.diplomaDoc.name}</div>}</div>
              <div className="upload-box-item"><label>Resmi Heyet/Sağlık Raporu ({documentRequirementText(systemSettings.requireHealthDocument)})</label><input type="file" accept=".pdf,.png,.jpg,.jpeg" onChange={(e) => handleFileChange(e, 'healthDoc')} />{files.healthDoc && <div className="file-success-badge">✓ {files.healthDoc.name}</div>}</div>
              <div className="upload-box-item"><label>Biyometrik Vesikalık Fotoğraf ({documentRequirementText(systemSettings.requirePhotoDocument)})</label><input type="file" accept="image/*" onChange={(e) => handleFileChange(e, 'photoDoc')} />{files.photoDoc && <div className="file-success-badge">✓ {files.photoDoc.name}</div>}</div>
              {isNationalAthlete && <div className="upload-box-item conditional-item animated-pulse"><label style={{ color: '#2980b9' }}>🏅 SGM Onaylı Milli Sporculuk Belgesi ({documentRequirementText(systemSettings.requireNationalDocument)})</label><input type="file" accept=".pdf,.png,.jpg,.jpeg" onChange={(e) => handleFileChange(e, 'nationalDoc')} />{files.nationalDoc && <div className="file-success-badge">✓ {files.nationalDoc.name}</div>}</div>}
              {isDisabledAthlete && <div className="upload-box-item conditional-item animated-pulse"><label style={{ color: '#8e44ad' }}>♿ Sürekli Engelli Sağlık Kurulu Raporu ({documentRequirementText(systemSettings.requireDisabledDocument)})</label><input type="file" accept=".pdf,.png,.jpg,.jpeg" onChange={(e) => handleFileChange(e, 'disabledDoc')} />{files.disabledDoc && <div className="file-success-badge">✓ {files.disabledDoc.name}</div>}</div>}
            </div>
            <div className="form-navigation" style={{ marginTop: '2.5rem' }}>
              <button type="button" className="btn-prev-step" onClick={prevStep} disabled={loading}>← Geri Dön</button>
              <button type="submit" className="btn-submit-final" disabled={loading || Boolean(applicationBlockedMessage)}>{loading ? 'Başvurunuz gönderiliyor...' : 'Başvuruyu resmi olarak tamamla 🚀'}</button>
            </div>
          </form>
        )}
      </div>
    </div>
  );
}

export default ApplicationForm;
