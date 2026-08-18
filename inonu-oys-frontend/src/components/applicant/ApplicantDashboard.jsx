import React, { useState } from 'react';
import api from '../../api';
import './ApplicantDashboard.css';

function ApplicantDashboard({ user, applicationData, onLogout, onStartApplication }) {
  const application = applicationData;
  const [downloading, setDownloading] = useState(false);
  const [resultDownloading, setResultDownloading] = useState(false);
  const [documentLoading, setDocumentLoading] = useState('');

  const statusLabels = {
    SUBMITTED: 'Başvurunuz Alındı',
    PENDING_EVALUATION: 'Jüri Değerlendirmesinde',
    COMPLETED: 'Değerlendirme Tamamlandı',
    REJECTED: 'Başvuru Reddedildi',
  };

  const placementLabels = {
    PRINCIPAL: 'Asıl Olarak Yerleştiniz',
    RESERVE: 'Yedek Aday',
    UNSUCCESSFUL: 'Yerleşemediniz',
  };

  const examTypeLabels = {
    GROUP: 'Toplu sınav',
    INDIVIDUAL: 'Bireysel sınav',
    TRACK: 'Parkur / zamana karşı sınav',
  };

  const resultDocumentReady = Boolean(application?.resultPublished && application?.placementStatus);

  const formatDate = (value) => value
    ? new Intl.DateTimeFormat('tr-TR', { dateStyle: 'long' }).format(new Date(value))
    : '-';

  const handleDownload = async (url, setLoading) => {
    if (!application?.id) {
      alert('Başvuru bilgileri bulunamadı.');
      return;
    }
    try {
      setLoading(true);
      const response = await api.get(url, { responseType: 'blob' });
      const file = new Blob([response.data], { type: 'application/pdf' });
      const fileURL = URL.createObjectURL(file);
      window.open(fileURL, '_blank', 'noopener,noreferrer');
      window.setTimeout(() => URL.revokeObjectURL(fileURL), 60000);
    } catch {
      alert('Belge açılırken bir hata oluştu. Lütfen oturumunuzu kontrol edin veya daha sonra tekrar deneyin.');
    } finally {
      setLoading(false);
    }
  };

  const handleResultDownload = () => {
    if (!resultDocumentReady) {
      alert('Sonuç belgesi henüz yayımlanmadı. Admin yerleştirme sonuçlarını yayımladıktan sonra indirilebilir.');
      return;
    }
    handleDownload(`/applications/${application.id}/result-document`, setResultDownloading);
  };

  const openUploadedDocument = async (path, label) => {
    if (!path) return;
    try {
      setDocumentLoading(label);
      const { data } = await api.get(`/files/${path}`, { responseType: 'blob' });
      const url = URL.createObjectURL(data);
      window.open(url, '_blank', 'noopener,noreferrer');
      window.setTimeout(() => URL.revokeObjectURL(url), 60000);
    } catch {
      alert('Yüklediğiniz belge açılamadı.');
    } finally {
      setDocumentLoading('');
    }
  };

  const documentButton = (path, label) => (
    <button
      type="button"
      className={path ? 'applicant-doc-button' : 'applicant-doc-button missing'}
      disabled={!path || documentLoading === label}
      onClick={() => openUploadedDocument(path, label)}
    >
      <span>{label}</span>
      <small>{path ? (documentLoading === label ? 'Açılıyor...' : 'Görüntüle') : 'Yüklenmedi'}</small>
    </button>
  );

  return (
    <div className="applicant-page">
      <nav className="applicant-navbar">
        <div className="navbar-brand">
          <h2>İNÖNÜ ÜNİVERSİTESİ</h2>
          <p>Özel Yetenek Sınavı Aday Paneli</p>
        </div>
        <div className="user-info">
          <span className="user-badge">Aday Öğrenci</span>
          <span>{user.firstName} {user.lastName}</span>
          <button onClick={onLogout} className="applicant-btn danger">Güvenli Çıkış</button>
        </div>
      </nav>

      <main className="applicant-content">
        <section className="applicant-hero">
          <div>
            <span className="applicant-kicker">Aday Paneli</span>
            <h1>Merhaba, {user.firstName}</h1>
            <p>Başvuru durumunuzu, belgelerinizi ve sonuç bilgilerinizi bu ekrandan takip edebilirsiniz.</p>
          </div>
          <div className="applicant-profile-card">
            <small>Profil</small>
            <strong>{user.firstName} {user.lastName}</strong>
            <span>T.C. {user.username}</span>
          </div>
        </section>

        {application ? (
          <>
            {application.resultPublished && (
              <section className={`applicant-result-banner result-${String(application.placementStatus).toLowerCase()}`}>
                <span>Sonuç Açıklandı</span>
                <h2>{placementLabels[application.placementStatus]}</h2>
                <p>
                  Bölüm sıralamanız <strong>{application.placementRank}</strong>,
                  yerleştirme puanınız <strong>{application.finalPlacementScore}</strong>.
                </p>
              </section>
            )}

            <section className="applicant-grid">
              <div className="applicant-card status-card">
                <span className={`applicant-status status-${String(application.status).toLowerCase()}`}>
                  {statusLabels[application.status] || application.status}
                </span>
                <h3>{application.programName}</h3>
                <p>Başvurunuz sistemde kayıtlı. Değerlendirme sürecindeki gelişmeler burada güncellenir.</p>
                <div className="applicant-score-grid">
                  <div><small>TYT</small><strong>{application.tytScore ?? '-'}</strong></div>
                  <div><small>OBP</small><strong>{application.obp ?? '-'}</strong></div>
                  <div><small>Jüri Ort.</small><strong>{application.oyspScore ?? 'Bekleniyor'}</strong></div>
                </div>
              </div>

              <div className="applicant-card">
                <h3>Resmi Belgeler</h3>
                <p>Sınav giriş ve sonuç belgelerinizi buradan açabilirsiniz.</p>
                <div className="applicant-action-list">
                  <button className="applicant-action-card blue" onClick={() => handleDownload(`/applications/${application.id}/exam-document`, setDownloading)} disabled={downloading}>
                    <span>Sınava Giriş Belgesi</span>
                    <small>{downloading ? 'Hazırlanıyor...' : 'Görüntüle / İndir'}</small>
                  </button>
                  <button className="applicant-action-card amber" onClick={handleResultDownload} disabled={!resultDocumentReady || resultDownloading}>
                    <span>Sınav Sonuç Belgesi</span>
                    <small>{resultDocumentReady ? (resultDownloading ? 'Hazırlanıyor...' : 'Görüntüle / İndir') : 'Sonuçlar yayımlanınca açılır'}</small>
                  </button>
                </div>
              </div>
            </section>

            <section className="applicant-card applicant-jury-card">
              <div className="applicant-section-header">
                <div>
                  <h3>Jüri Atamalarınız</h3>
                  <p>
                    {application.juryAssignments?.length
                      ? 'Bölüm admini tarafından onaylanan jüri atamalarınız aşağıda listelenir.'
                      : 'Jüri atamanız bölüm admini tarafından hazırlanıyor.'}
                  </p>
                </div>
                <span className={application.juryAssignments?.length ? 'exam-chip published' : 'exam-chip'}>
                  {application.juryAssignments?.length ? 'Atandı' : 'Bekleniyor'}
                </span>
              </div>
              <div className="applicant-doc-grid">
                {application.juryAssignments?.length ? application.juryAssignments.map((assignment) => (
                  <div className="applicant-doc-button" key={assignment.id || assignment.juryId}>
                    <span>{assignment.juryFullName}</span>
                    <small>{assignment.matchedAreas || 'Onaylı jüri eşleşmesi'}</small>
                  </div>
                )) : (
                  <div className="applicant-doc-button missing">
                    <span>Henüz jüri ataması yok</span>
                    <small>Onaylandığında burada görünecek</small>
                  </div>
                )}
              </div>
            </section>

            <section className="applicant-card applicant-exam-card">
              <span className={application.examSchedulePublished ? 'exam-chip published' : 'exam-chip'}>
                {application.examSchedulePublished ? 'Sınav Programı Açıklandı' : 'Sınav Programı Bekleniyor'}
              </span>
              <h3>Sınav Giriş Bilgileriniz</h3>
              {application.examSchedulePublished ? (
                <div className="applicant-exam-layout">
                  <div className="applicant-exam-primary">
                    <small>Sınav Tarihi ve Saati</small>
                    <strong>{formatDate(application.examDate)}</strong>
                    <span>{application.appointmentStartTime || application.examStartTime}{application.appointmentEndTime ? ` - ${application.appointmentEndTime}` : (application.examEndTime ? ` - ${application.examEndTime}` : '')}</span>
                  </div>
                  <div className="applicant-exam-grid">
                    <p><span>Sınav Tipi</span>{examTypeLabels[application.examSessionType] || application.examSessionType}</p>
                    <p><span>Bina / Yer</span>{application.examLocation || '-'}</p>
                    <p><span>Salon / Oda</span>{application.examRoom || '-'}</p>
                    <p><span>Aday Sırası</span>{application.examOrder ?? '-'}</p>
                  </div>
                </div>
              ) : (
                <p>Sınav tarihiniz, saatiniz, binanız, salonunuz ve aday sıranız otomatik program yayımlandığında burada görünecek.</p>
              )}
            </section>

            <section className="applicant-card">
              <div className="applicant-section-header">
                <div>
                  <h3>Yüklediğiniz Belgeler</h3>
                  <p>Başvuru sırasında sisteme eklediğiniz evrakları buradan kontrol edebilirsiniz.</p>
                </div>
              </div>
              <div className="applicant-doc-grid">
                {documentButton(application.osymDocPath, 'ÖSYM Sonuç Belgesi')}
                {documentButton(application.diplomaDocPath, 'Diploma / Mezuniyet')}
                {documentButton(application.healthDocPath, 'Sağlık Raporu')}
                {documentButton(application.photoDocPath, 'Fotoğraf')}
                {documentButton(application.nationalDocPath, 'Milli Sporcu Belgesi')}
                {documentButton(application.disabledDocPath, 'Engelli Raporu')}
              </div>
            </section>
          </>
        ) : (
          <section className="applicant-start-card">
            <span>Henüz Başvuru Yok</span>
            <h2>Özel yetenek sınavı başvurunuzu başlatın</h2>
            <p>TYT ve OBP bilgilerinizi girip gerekli belgeleri yükleyerek başvurunuzu tamamlayabilirsiniz.</p>
            <button className="applicant-btn primary" onClick={onStartApplication}>Başvuru Formunu Doldur</button>
          </section>
        )}
      </main>
    </div>
  );
}

export default ApplicantDashboard;
