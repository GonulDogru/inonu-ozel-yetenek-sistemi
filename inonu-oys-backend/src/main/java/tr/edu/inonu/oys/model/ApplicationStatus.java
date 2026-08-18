package tr.edu.inonu.oys.model;

public enum ApplicationStatus {
    SUBMITTED,          // Aday tarafından ilk kez gönderildi, yönetici onayı bekliyor
    PENDING_EVALUATION, // Yönetici tarafından onaylandı, jüri değerlendirmesi bekliyor
    REJECTED,           // Yönetici tarafından reddedildi
    COMPLETED,          // Jüri değerlendirmesi tamamlandı, puanı hesaplandı
    CANCELLED           // Aday tarafından iptal edildi (isteğe bağlı)
}
