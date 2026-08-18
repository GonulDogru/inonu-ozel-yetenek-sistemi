import React, { useCallback, useEffect, useState } from 'react';
import api from '../../api';
import './AdminDashboard.css';

const emptyJury = { username: '', password: '', firstName: '', lastName: '', juryField: 'SPOR' };
const emptySession = {
  departmentId: '',
  classroomId: '',
  classroomIds: [],
  sessionType: 'GROUP',
  examDate: '',
  startTime: '',
  endTime: '',
  location: '',
  room: '',
  candidateIntervalMinutes: 10,
  published: false,
};
const emptyClassroom = { departmentId: '', name: '', capacity: 1, building: '', active: true };

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
    items: [
      'Ortak Parkur Performansı',
      'Sprint',
      'Slalom',
      'Engel geçme',
      'Takla',
      'Denge tahtası',
      'Sağlık topu taşıma',
      'Basketbol topu sürme',
      'Futbol topuyla slalom',
      'Hedefe top atma',
      'Koordinasyon',
      'Çeviklik',
    ],
  },
  {
    scope: 'ART',
    group: 'Resim - Çizim Sınavı',
    items: [
      'Çizim Sınavı',
      'Desen çizimi',
      'Gözlem çizimi',
      'Oran-orantı',
      'Kompozisyon',
      'Işık-gölge',
      'Yaratıcı yorum',
    ],
  },
  {
    scope: 'CERAMIC',
    group: 'Seramik',
    items: [
      'Şekillendirme',
      'Üç boyutlu algı',
      'Hacim kurgusu',
      'Yüzey tasarımı',
      'Modelleme',
      'Yaratıcı form',
    ],
  },
];
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
  examDocumentEnabled: true,
  resultDocumentEnabled: true,
};

const statusLabel = {
  SUBMITTED: 'Başvuru Alındı',
  PENDING_EVALUATION: 'Değerlendirmede',
  COMPLETED: 'Tamamlandı',
  REJECTED: 'Reddedildi',
};

const auditActionLabel = {
  USER_CREATED: 'Kullanıcı oluşturuldu',
  USER_ACTIVATED: 'Kullanıcı aktifleştirildi',
  USER_DEACTIVATED: 'Kullanıcı pasife alındı',
  APPLICATION_STATUS_CHANGED: 'Başvuru durumu değişti',
  DEPARTMENT_SETTINGS_UPDATED: 'Bölüm ayarları güncellendi',
  CLASSROOM_CREATED: 'Salon oluşturuldu',
  CLASSROOM_ACTIVATED: 'Salon aktifleştirildi/güncellendi',
  CLASSROOM_DEACTIVATED: 'Salon pasife alındı/güncellendi',
  EXAM_SESSION_CREATED: 'Sınav oturumu oluşturuldu',
  EXAM_SESSION_UPDATED: 'Sınav oturumu güncellendi',
  EXAM_CANDIDATES_ASSIGNED: 'Adaylar oturuma yerleştirildi',
  EXAM_SESSION_PUBLISHED: 'Sınav oturumu yayımlandı',
  EXAM_AUTO_SCHEDULED: 'Otomatik sınav planlandı',
  PLACEMENT_PUBLISHED: 'Sonuçlar yayımlandı',
  SYSTEM_SETTINGS_UPDATED: 'Sistem ayarları güncellendi',
  JURY_DEPARTMENT_ASSIGNED: 'Jüri bölüm ataması yapıldı',
  JURY_DEPARTMENT_REMOVED: 'Jüri bölüm ataması kaldırıldı',
  JURY_INACTIVE_SLOT_CREATED: 'Jüri pasif zamanı ekledi',
  ADMIN_DEPARTMENT_ASSIGNED: 'Admin bölüm yetkisi verildi',
  ADMIN_DEPARTMENT_REMOVED: 'Admin bölüm yetkisi kaldırıldı',
};

const auditTargetLabel = {
  USER: 'Kullanıcı/Jüri',
  APPLICATION: 'Başvuru',
  DEPARTMENT: 'Bölüm',
  CLASSROOM: 'Salon',
  EXAM_SESSION: 'Sınav Oturumu',
  JURY: 'Jüri',
  SYSTEM_SETTINGS: 'Sistem Ayarları',
};

function AdminDashboard({ user, onLogout }) {
  const isDepartmentAdmin = user.role === 'DEPARTMENT_ADMIN';
  const isSuperAdmin = user.role === 'SUPER_ADMIN';
  const assignedDepartmentIds = user.assignedDepartmentIds || [];
  const canUseDepartment = (departmentId) => !isDepartmentAdmin || assignedDepartmentIds.includes(Number(departmentId));
  const filterDepartments = (items) => isDepartmentAdmin ? items.filter((item) => canUseDepartment(item.id)) : items;
  const filterDepartmentData = (items) => isDepartmentAdmin ? items.filter((item) => canUseDepartment(item.departmentId)) : items;
  const adminBadge = user.role === 'DEPARTMENT_ADMIN' ? 'Departman Admini' : 'Super Admin';

  const [activeTab, setActiveTab] = useState('overview');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [applications, setApplications] = useState([]);
  const [selectedApp, setSelectedApp] = useState(null);
  const [selectedApplicationIds, setSelectedApplicationIds] = useState([]);
  const [candidateJuryAssignments, setCandidateJuryAssignments] = useState([]);
  const [candidateJurySelection, setCandidateJurySelection] = useState({});
  const [assignmentLoading, setAssignmentLoading] = useState(false);
  const [juryScores, setJuryScores] = useState([]);
  const [juryMembers, setJuryMembers] = useState([]);
  const [adminUsers, setAdminUsers] = useState([]);
  const [systemSettings, setSystemSettings] = useState(defaultSystemSettings);
  const [selectedAdminUser, setSelectedAdminUser] = useState(null);
  const [departments, setDepartments] = useState([]);
  const [selectedJury, setSelectedJury] = useState(null);
  const [juryView, setJuryView] = useState('list');
  const [juryForm, setJuryForm] = useState(emptyJury);
  const [juryCreateAssignment, setJuryCreateAssignment] = useState({ departmentId: '', assignmentRole: 'PRIMARY' });
  const [placementResults, setPlacementResults] = useState([]);
  const [examSessions, setExamSessions] = useState([]);
  const [examView, setExamView] = useState('list');
  const [sessionForm, setSessionForm] = useState(emptySession);
  const [classrooms, setClassrooms] = useState([]);
  const [classroomView, setClassroomView] = useState('list');
  const [classroomForm, setClassroomForm] = useState(emptyClassroom);
  const [editingClassroomId, setEditingClassroomId] = useState(null);
  const [applicationFilters, setApplicationFilters] = useState({ query: '', status: '', departmentId: '', juryAssignment: '' });
  const [juryFilters, setJuryFilters] = useState({ query: '', field: '', departmentId: '', assignmentRole: '', active: '' });
  const [classroomFilters, setClassroomFilters] = useState({ query: '', departmentId: '', active: '' });
  const [examFilters, setExamFilters] = useState({ query: '', departmentId: '', sessionType: '', published: '' });
  const [placementFilters, setPlacementFilters] = useState({ query: '', examType: '', quotaState: '' });
  const [adminFilters, setAdminFilters] = useState({ query: '', role: '', departmentId: '', active: '' });
  const [auditLogs, setAuditLogs] = useState([]);
  const [auditFilters, setAuditFilters] = useState({ query: '', action: '', targetType: '', limit: 100 });

  const getAssignmentRole = (jury, departmentId) =>
    jury.assignedDepartmentRoles?.[departmentId] || jury.assignedDepartmentRoles?.[String(departmentId)] || 'PRIMARY';

  const getDepartmentName = (departmentId) =>
    departments.find((department) => department.id === Number(departmentId))?.name || 'Bölüm';

  const getJuryAssignmentItems = (jury) =>
    (jury.assignedDepartmentIds || [])
      .filter((departmentId) => !isDepartmentAdmin || canUseDepartment(departmentId))
      .map((departmentId) => ({
      departmentId,
      departmentName: getDepartmentName(departmentId),
      role: getAssignmentRole(jury, departmentId),
    }));

  const getUserDepartmentItems = (managedUser) =>
    (managedUser.assignedDepartmentIds || []).map((departmentId) => ({
      departmentId,
      departmentName: getDepartmentName(departmentId),
    }));

  const parseJurySpecialties = (jury) => {
    try {
      const parsed = JSON.parse(jury?.jurySpecialties || '[]');
      return Array.isArray(parsed) ? parsed : [];
    } catch {
      return [];
    }
  };

  const stringifyJurySpecialties = (items) => JSON.stringify([...new Set(items)].sort());

  const specialtyScopeForDepartment = (departmentName = '') => {
    if (['Beden', 'Antrenörlük', 'Spor Yöneticiliği', 'Engellilerde'].some((key) => departmentName.includes(key))) return 'SPORT';
    if (['Müzik', 'Müzikoloji'].some((key) => departmentName.includes(key))) return 'MUSIC';
    if (departmentName.includes('Resim')) return 'ART';
    if (departmentName.includes('Seramik')) return 'CERAMIC';
    return 'GENERAL';
  };

  const getJurySpecialtyGroups = (jury) => {
    const scopes = new Set(getJuryAssignmentItems(jury).map((assignment) => specialtyScopeForDepartment(assignment.departmentName)));
    if (!scopes.size) {
      if (jury?.juryField === 'SPOR') scopes.add('SPORT');
      if (jury?.juryField === 'MUSIC') scopes.add('MUSIC');
      if (jury?.juryField === 'ART') scopes.add('ART');
      if (jury?.juryField === 'CERAMIC') scopes.add('CERAMIC');
      if (jury?.juryField === 'GSF') {
        scopes.add('MUSIC');
        scopes.add('ART');
        scopes.add('CERAMIC');
      }
    }
    return jurySpecialtyOptions.filter((group) => scopes.has(group.scope));
  };

  const expectedJuryFieldForDepartment = (departmentName = '') =>
    specialtyScopeForDepartment(departmentName) === 'SPORT' ? 'SPOR' : specialtyScopeForDepartment(departmentName);

  const juryFieldLabel = (field) => ({
    SPOR: 'Spor',
    MUSIC: 'Müzik',
    ART: 'Resim',
    CERAMIC: 'Seramik',
    GSF: 'Güzel Sanatlar',
  }[field] || field || '-');

  const jurySpecialtyScopeMatches = (jury, departmentName = '') => {
    const scope = specialtyScopeForDepartment(departmentName);
    if (scope === 'GENERAL') return true;
    const specialties = normalize(parseJurySpecialties(jury).join(' '));
    if (!specialties) return false;
    const hasAny = (items) => items.some((item) => specialties.includes(normalize(item)));
    if (scope === 'MUSIC') return hasAny(['enstrüman', 'şan', 'vokal', 'ritim', 'melodi', 'işitme', 'kulak', 'bağlama', 'gitar', 'piyano', 'keman']);
    if (scope === 'ART') return hasAny(['çizim', 'desen', 'gözlem', 'kompozisyon', 'oran']);
    if (scope === 'CERAMIC') return hasAny(['seramik', 'şekillendirme', 'modelleme', 'hacim', 'tasarım']);
    if (scope === 'SPORT') return hasAny(['ortak parkur', 'sprint', 'slalom', 'engel', 'denge', 'parkur']);
    return true;
  };

  const juryOwnScope = (jury) => {
    const text = normalize(`${jury.firstName || ''} ${jury.lastName || ''} ${parseJurySpecialties(jury).join(' ')}`);
    const hasAny = (items) => items.some((item) => text.includes(normalize(item)));
    if (hasAny(['beden', 'antrenör', 'antrenor', 'spor', 'parkur', 'sprint', 'slalom', 'engel', 'denge'])) return 'SPORT';
    if (hasAny(['müzik', 'muzik', 'müzikoloji', 'muzikoloji', 'enstrüman', 'enstruman', 'şan', 'san', 'vokal', 'ritim', 'melodi', 'işitme', 'isitme', 'kulak', 'bağlama', 'baglama', 'gitar', 'piyano', 'keman'])) return 'MUSIC';
    if (hasAny(['resim', 'çizim', 'cizim', 'desen', 'gözlem', 'gozlem', 'kompozisyon', 'oran'])) return 'ART';
    if (hasAny(['seramik', 'seramik', 'şekillendirme', 'sekillendirme', 'modelleme', 'hacim', 'tasarım', 'tasarim'])) return 'CERAMIC';
    if (jury.juryField === 'SPOR') return 'SPORT';
    if (jury.juryField === 'MUSIC') return 'MUSIC';
    if (jury.juryField === 'ART') return 'ART';
    if (jury.juryField === 'CERAMIC') return 'CERAMIC';
    return 'UNKNOWN';
  };

  const juriesForApplication = (application) => {
    const departmentName = application.programName || getDepartmentName(application.departmentId);
    const expectedField = expectedJuryFieldForDepartment(departmentName);
    return juryMembers.filter((jury) =>
      jury.active !== false
      && jury.juryField === expectedField
      && jurySpecialtyScopeMatches(jury, departmentName)
      && (jury.assignedDepartmentIds || []).map(Number).includes(Number(application.departmentId)));
  };

  const filterJuriesByAdminScope = (items, scopedDepartments = departments) => {
    if (!isDepartmentAdmin) return items;
    const scopedDepartmentIds = scopedDepartments.map((department) => Number(department.id));
    const scopedDepartmentScopes = scopedDepartments.map((department) => specialtyScopeForDepartment(department.name));
    return items.filter((jury) => scopedDepartments.some((department) =>
      (jury.assignedDepartmentIds || []).map(Number).includes(Number(department.id))
      && jury.juryField === expectedJuryFieldForDepartment(department.name))
      && scopedDepartmentScopes.includes(juryOwnScope(jury))
      && (jury.assignedDepartmentIds || []).map(Number).some((id) => scopedDepartmentIds.includes(id)));
  };

  const normalize = (value) => String(value || '').toLocaleLowerCase('tr-TR');
  const textMatches = (haystack, query) => !query || normalize(haystack).includes(normalize(query));
  const formatAuditDate = (value) => value
    ? new Date(value).toLocaleString('tr-TR', { dateStyle: 'short', timeStyle: 'short' })
    : '-';

  const loadClassrooms = useCallback(async (departmentItems) => {
    const results = await Promise.allSettled(
      departmentItems.map((department) => api.get(`/classrooms/department/${department.id}`))
    );
    const loadedClassrooms = results
      .filter((result) => result.status === 'fulfilled')
      .flatMap((result) => result.value.data || []);
    const failedCount = results.filter((result) => result.status === 'rejected').length;
    setClassrooms(loadedClassrooms);
    if (failedCount > 0) {
      setError(`${failedCount} bölümün salon verisi yüklenemedi. Diğer veriler gösteriliyor.`);
    }
  }, []);

  const fetchData = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      if (activeTab === 'overview') {
        const [applicationList, juryList, departmentList, sessionList] = await Promise.all([
          api.get('/applications/all'),
          api.get('/users/jury'),
          api.get('/departments/all'),
          api.get('/exam-sessions'),
        ]);
        const scopedDepartments = filterDepartments(departmentList.data || []);
        setApplications(filterDepartmentData(applicationList.data || []));
        setJuryMembers(filterJuriesByAdminScope(juryList.data || [], scopedDepartments));
        setDepartments(scopedDepartments);
        setExamSessions(filterDepartmentData(sessionList.data || []));
        await loadClassrooms(scopedDepartments);
        setSelectedApp(null);
        setSelectedApplicationIds([]);
      } else if (activeTab === 'students') {
        const { data } = await api.get('/applications/all');
        setApplications(filterDepartmentData(data || []));
        setSelectedApp(null);
        setSelectedApplicationIds([]);
      } else if (activeTab === 'jury') {
        const [juries, departmentList] = await Promise.all([
          api.get('/users/jury'),
          api.get('/departments/all'),
        ]);
        const scopedDepartments = filterDepartments(departmentList.data || []);
        setJuryMembers(filterJuriesByAdminScope(juries.data || [], scopedDepartments));
        setDepartments(scopedDepartments);
        if (scopedDepartments.length && !juryCreateAssignment.departmentId) {
          setJuryCreateAssignment((item) => ({ ...item, departmentId: String(scopedDepartments[0].id) }));
        }
      } else if (activeTab === 'admins') {
        const [adminList, departmentList] = await Promise.all([
          api.get('/users/admins'),
          api.get('/departments/all'),
        ]);
        const scopedDepartments = filterDepartments(departmentList.data || []);
        setAdminUsers(adminList.data || []);
        setDepartments(scopedDepartments);
        setSelectedAdminUser(null);
      } else if (activeTab === 'settings') {
        const { data } = await api.get('/system-settings');
        setSystemSettings({ ...defaultSystemSettings, ...(data || {}) });
      } else if (activeTab === 'placement') {
        const { data } = await api.get('/departments/all');
        setDepartments(filterDepartments(data || []));
      } else if (activeTab === 'exam') {
        const [departmentList, sessionList] = await Promise.all([
          api.get('/departments/all'),
          api.get('/exam-sessions'),
        ]);
        const scopedDepartments = filterDepartments(departmentList.data || []);
        setDepartments(scopedDepartments);
        setExamSessions(filterDepartmentData(sessionList.data || []));
        await loadClassrooms(scopedDepartments);
      } else if (activeTab === 'classrooms') {
        const { data } = await api.get('/departments/all');
        const scopedDepartments = filterDepartments(data || []);
        setDepartments(scopedDepartments);
        await loadClassrooms(scopedDepartments);
      } else if (activeTab === 'audit') {
        const { data } = await api.get('/audit-logs', {
          params: {
            query: auditFilters.query || undefined,
            action: auditFilters.action || undefined,
            targetType: auditFilters.targetType || undefined,
            limit: auditFilters.limit || 100,
          },
        });
        setAuditLogs(data || []);
      }
    } catch (err) {
      if ([401, 403].includes(err.response?.status)) {
        setError('Oturum yetkisi geçersiz görünüyor. Lütfen tekrar giriş yapın.');
        onLogout();
        return;
      }
      setError(err.response?.data || 'Veriler yüklenirken bir hata oluştu.');
    } finally {
      setLoading(false);
    }
  }, [activeTab, isDepartmentAdmin, assignedDepartmentIds.join(','), loadClassrooms, auditFilters.query, auditFilters.action, auditFilters.targetType, auditFilters.limit]);

  useEffect(() => {
    fetchData();
  }, [fetchData]);

  const selectApplication = async (application) => {
    setSelectedApp(application);
    setJuryScores([]);
    setCandidateJuryAssignments([]);
    if (application.status === 'COMPLETED') {
      const { data } = await api.get(`/applications/${application.id}/scores`);
      setJuryScores(data || []);
    }
    try {
      const { data } = await api.get(`/candidate-jury-assignments/application/${application.id}`);
      setCandidateJuryAssignments(data || []);
      setCandidateJurySelection(Object.fromEntries((data || []).slice(0, 3).map((assignment, index) => [index, String(assignment.juryId)])));
    } catch {
      setCandidateJuryAssignments([]);
      setCandidateJurySelection({});
    }
  };

  const suggestCandidateJuries = async (applicationId) => {
    try {
      setAssignmentLoading(true);
      const { data } = await api.post(`/candidate-jury-assignments/application/${applicationId}/suggest`);
      setCandidateJuryAssignments(data || []);
      setCandidateJurySelection(Object.fromEntries((data || []).slice(0, 3).map((assignment, index) => [index, String(assignment.juryId)])));
    } catch (err) {
      setError(err.response?.data || 'Jüri önerileri oluşturulamadı.');
    } finally {
      setAssignmentLoading(false);
    }
  };

  const approveCandidateJuries = async (applicationId) => {
    const juryIds = [0, 1, 2].map((index) => Number(candidateJurySelection[index])).filter(Boolean);
    if (juryIds.length !== 3) {
      setError('Onaylamak için önce 3 jüri önerisi oluşturulmalı.');
      return;
    }
    if (new Set(juryIds).size !== 3) {
      setError('Aynı jüri birden fazla seçilemez. Lütfen 3 farklı jüri seçin.');
      return;
    }
    if (!window.confirm('Bu aday için 3 jüri onaylansın mı?')) return;
    try {
      setAssignmentLoading(true);
      const { data } = await api.post('/candidate-jury-assignments/approve', { applicationId, juryIds });
      setCandidateJuryAssignments(data || []);
      setCandidateJurySelection(Object.fromEntries((data || []).slice(0, 3).map((assignment, index) => [index, String(assignment.juryId)])));
      setApplications((items) => items.map((item) =>
        item.id === applicationId ? { ...item, juryAssignments: data || [] } : item));
      setSelectedApp((item) => item?.id === applicationId ? { ...item, juryAssignments: data || [] } : item);
    } catch (err) {
      setError(err.response?.data || 'Jüri eşleşmeleri onaylanamadı.');
    } finally {
      setAssignmentLoading(false);
    }
  };

  const updateStatus = async (status) => {
    try {
      await api.patch(`/applications/${selectedApp.id}/status`, { status });
      setApplications((items) => items.map((item) =>
        item.id === selectedApp.id ? { ...item, status } : item));
      setSelectedApp((item) => ({ ...item, status }));
      setSelectedApplicationIds((ids) => ids.filter((id) => id !== selectedApp.id));
    } catch (err) {
      setError(err.response?.data || 'Durum güncellenemedi.');
    }
  };

  const createJury = async (event) => {
    event.preventDefault();
    setError('');
    try {
      const departmentId = juryCreateAssignment.departmentId || (isDepartmentAdmin ? departments[0]?.id : '');
      const departmentName = getDepartmentName(departmentId);
      const juryField = departmentId ? expectedJuryFieldForDepartment(departmentName) : juryForm.juryField;
      const { data } = await api.post('/users/create', { ...juryForm, juryField, role: 'JURY' });
      let createdJury = data;
      if (departmentId) {
        await api.post('/jury/assign', {
          juryId: data.id,
          departmentId: Number(departmentId),
          assignmentRole: juryCreateAssignment.assignmentRole,
        });
        createdJury = {
          ...data,
          assignedDepartmentIds: [...new Set([...(data.assignedDepartmentIds || []).map(Number), Number(departmentId)])],
          assignedDepartmentRoles: {
            ...(data.assignedDepartmentRoles || {}),
            [departmentId]: juryCreateAssignment.assignmentRole,
          },
        };
      }
      setJuryMembers((items) => [...items, createdJury]);
      setSelectedJury(createdJury);
      setJuryView('list');
      setJuryForm(emptyJury);
      setJuryCreateAssignment((item) => ({ ...item, assignmentRole: 'PRIMARY' }));
    } catch (err) {
      setError(err.response?.data || 'Jüri oluşturulamadı.');
    }
  };

  const deleteJury = async (juryId) => {
    if (!window.confirm('Bu jüri pasife alınsın mı?')) return;
    try {
      await api.delete(`/users/${juryId}`);
      setJuryMembers((items) => items.map((item) => item.id === juryId ? { ...item, active: false } : item));
      if (selectedJury?.id === juryId) setSelectedJury((item) => ({ ...item, active: false }));
    } catch (err) {
      setError(err.response?.data || 'Jüri pasife alınamadı.');
    }
  };

  const toggleJuryActive = async (jury) => {
    const active = jury.active === false;
    try {
      const { data } = await api.patch(`/users/${jury.id}/active`, { active });
      setJuryMembers((items) => items.map((item) => item.id === jury.id ? data : item));
      if (selectedJury?.id === jury.id) setSelectedJury(data);
    } catch (err) {
      setError(err.response?.data || 'Jüri durumu güncellenemedi.');
    }
  };

  const changeAssignment = async (departmentId, assigned, assignmentRole = 'PRIMARY') => {
    if (!selectedJury) return;
    if (selectedJury.active === false) {
      setError('Pasif jüriye bölüm ataması yapılamaz. Önce jüriyi aktifleştirin.');
      return;
    }
    try {
      await api.post(`/jury/${assigned ? 'remove' : 'assign'}`, {
        juryId: selectedJury.id,
        departmentId,
        assignmentRole,
      });
      const currentIds = selectedJury.assignedDepartmentIds || [];
      const ids = assigned
        ? currentIds.filter((id) => Number(id) !== Number(departmentId))
        : [...new Set([...currentIds.map(Number), Number(departmentId)])];
      const roles = { ...(selectedJury.assignedDepartmentRoles || {}) };
      if (assigned) {
        delete roles[departmentId];
        delete roles[String(departmentId)];
      } else {
        roles[departmentId] = assignmentRole;
      }
      const updated = { ...selectedJury, assignedDepartmentIds: ids, assignedDepartmentRoles: roles };
      setSelectedJury(updated);
      setJuryMembers((items) => items.map((item) => item.id === updated.id ? updated : item));
    } catch (err) {
      setError(err.response?.data || 'Bölüm ataması güncellenemedi.');
    }
  };

  const toggleJurySpecialty = async (jury, specialty) => {
    const current = parseJurySpecialties(jury);
    const next = current.includes(specialty)
      ? current.filter((item) => item !== specialty)
      : [...current, specialty];
    try {
      const { data } = await api.patch(`/users/${jury.id}/jury-specialties`, {
        jurySpecialties: stringifyJurySpecialties(next),
      });
      setSelectedJury(data);
      setJuryMembers((items) => items.map((item) => item.id === data.id ? data : item));
    } catch (err) {
      setError(err.response?.data || 'Jüri uzmanlık alanları güncellenemedi.');
    }
  };

  const toggleAdminActive = async (managedUser) => {
    const active = managedUser.active === false;
    try {
      const { data } = await api.patch(`/users/${managedUser.id}/active`, { active });
      setAdminUsers((items) => items.map((item) => item.id === managedUser.id ? data : item));
      if (selectedAdminUser?.id === managedUser.id) setSelectedAdminUser(data);
    } catch (err) {
      setError(err.response?.data || 'Admin durumu güncellenemedi.');
    }
  };

  const changeAdminDepartment = async (departmentId, assigned) => {
    if (!selectedAdminUser) return;
    try {
      const { data } = await api.patch(`/users/${selectedAdminUser.id}/departments`, {
        departmentId,
        assigned: !assigned,
      });
      setSelectedAdminUser(data);
      setAdminUsers((items) => items.map((item) => item.id === data.id ? data : item));
      setError('');
    } catch (err) {
      setError(err.response?.data || 'Admin bölüm yetkisi güncellenemedi.');
    }
  };

  const renderAdminControlPanel = (managedUser) => (
    <div className="jury-inline-panel admin-control-panel">
      <div className="jury-inline-header">
        <div>
          <span className="panel-eyebrow">Admin yetki kontrolü</span>
          <h4>{managedUser.firstName} {managedUser.lastName}</h4>
          <p>T.C. {managedUser.username} · {managedUser.role === 'DEPARTMENT_ADMIN' ? 'Bölüm Admini' : 'Admin'} · {managedUser.active === false ? 'Pasif' : 'Aktif'}</p>
        </div>
        <button className="admin-btn admin-btn-secondary" type="button" onClick={() => setSelectedAdminUser(null)}>Kapat</button>
      </div>
      <div className="jury-inline-summary">
        {getUserDepartmentItems(managedUser).length ? getUserDepartmentItems(managedUser).map((assignment) => (
          <span key={`admin-summary-${assignment.departmentId}`} className="jury-assignment-chip primary">
            {assignment.departmentName}
          </span>
        )) : <span className="jury-assignment-chip empty">Henüz bölüm yetkisi yok</span>}
      </div>
      <div className="department-assignment-grid jury-inline-grid">
        {departments.map((department) => {
          const assigned = (managedUser.assignedDepartmentIds || []).map(Number).includes(Number(department.id));
          return (
            <div key={department.id} className={`department-card ${assigned ? 'assigned' : ''}`}>
              <div>
                <h5>{department.name}</h5>
                <p>{assigned ? 'Bu admin bu bölümü yönetebilir.' : 'Bu bölüm için yetkisi yok.'}</p>
              </div>
              <button className={`admin-btn ${assigned ? 'admin-btn-danger' : 'admin-btn-success'}`} type="button" onClick={() => changeAdminDepartment(department.id, assigned)}>
                {assigned ? 'Yetkiyi Kaldır' : 'Yetki Ver'}
              </button>
            </div>
          );
        })}
      </div>
    </div>
  );

  const renderJuryAssignmentPanel = (jury) => (
    <div className="jury-inline-panel">
      <div className="jury-inline-header">
        <div>
          <span className="panel-eyebrow">Jüri atama yönetimi</span>
          <h4>{jury.firstName} {jury.lastName}</h4>
          <p>T.C. {jury.username} · {jury.juryField} · {jury.active === false ? 'Pasif' : 'Aktif'}</p>
        </div>
        <button className="admin-btn admin-btn-secondary" type="button" onClick={() => setSelectedJury(null)}>Kapat</button>
      </div>
      {jury.active === false && (
        <div className="admin-inline-warning">
          Bu jüri pasif durumda. Aktifleştirilmeden yeni bölüm ataması yapılamaz ve otomatik sınav planına dahil edilmez.
        </div>
      )}
      <div className="jury-inline-summary">
        {getJuryAssignmentItems(jury).length ? getJuryAssignmentItems(jury).map((assignment) => (
          <span key={`summary-${assignment.departmentId}`} className={`jury-assignment-chip ${assignment.role === 'BACKUP' ? 'backup' : 'primary'}`}>
            {assignment.departmentName} · {assignment.role === 'BACKUP' ? 'Yedek' : 'Asıl'}
          </span>
        )) : <span className="jury-assignment-chip empty">Henüz bölüm ataması yok</span>}
      </div>
      <div className="jury-specialty-panel">
        <div className="jury-specialty-header">
          <div>
            <span className="panel-eyebrow">Uzmanlık eşleşmesi</span>
            <h4>Jürinin değerlendirebileceği alanlar</h4>
            <p>Adayın performans seçimiyle aynı alanlar işaretlenirse sistem eşleşmeyi önceliklendirir.</p>
          </div>
          <small>{parseJurySpecialties(jury).length} alan seçili</small>
        </div>
        {getJurySpecialtyGroups(jury).map((group) => (
          <div className="jury-specialty-group" key={group.group}>
            <strong>{group.group}</strong>
            <div className="jury-specialty-chip-grid">
              {group.items.map((specialty) => {
                const active = parseJurySpecialties(jury).includes(specialty);
                return (
                  <button
                    type="button"
                    key={specialty}
                    className={`jury-specialty-chip ${active ? 'active' : ''}`}
                    onClick={() => toggleJurySpecialty(jury, specialty)}
                  >
                    {active ? '✓ ' : '+ '}{specialty}
                  </button>
                );
              })}
            </div>
          </div>
        ))}
      </div>
      <div className="department-assignment-grid jury-inline-grid">
        {departments.map((department) => {
          const assigned = (jury.assignedDepartmentIds || []).map(Number).includes(Number(department.id));
          const role = getAssignmentRole(jury, department.id);
          return <div key={department.id} className={`department-assignment-button ${assigned ? 'assigned' : ''}`}>
            <span>{assigned ? '✓' : '+'}</span>
            <strong>{department.name}</strong>
            <small>{assigned ? (role === 'BACKUP' ? 'Yedek jüri' : 'Asıl jüri') : 'Atanmamış'}</small>
            <div className="department-assignment-actions">
              <button type="button" onClick={() => changeAssignment(department.id, false, 'PRIMARY')}>Asıl yap</button>
              <button type="button" onClick={() => changeAssignment(department.id, false, 'BACKUP')}>Yedek yap</button>
              {assigned && <button type="button" onClick={() => changeAssignment(department.id, true)}>Kaldır</button>}
            </div>
          </div>;
        })}
      </div>
    </div>
  );

  const renderApplicationDetailPanel = (application) => (
    <div className="jury-inline-panel application-inline-panel">
      <div className="jury-inline-header">
        <div>
          <span className="panel-eyebrow">Aday inceleme</span>
          <h4>{application.applicantFullName}</h4>
          <p>T.C. {application.applicantUsername} · {application.programName}</p>
        </div>
        <button className="admin-btn admin-btn-secondary" type="button" onClick={() => setSelectedApp(null)}>Kapat</button>
      </div>
      <div className="application-detail-grid">
        <div className="application-info-card"><span>Bölüm</span><strong>{application.programName}</strong></div>
        <div className="application-info-card"><span>TYT</span><strong>{application.tytScore}</strong></div>
        <div className="application-info-card"><span>OBP</span><strong>{application.obp ?? 'Öğrenci tarafından girilmemiş'}</strong></div>
        <div className="application-info-card"><span>Durum</span><strong>{statusLabel[application.status] || application.status}</strong></div>
      </div>
      <p className="admin-help-text compact">OBP öğrenci tarafından başvuru sırasında beyan edilir; admin kontrolü resmi belgeler üzerinden yapılır.</p>
      <div className="evrak-links-grid">
        {documentLink(application.osymDocPath, 'ÖSYM')}
        {documentLink(application.diplomaDocPath, 'Diploma')}
        {documentLink(application.healthDocPath, 'Sağlık')}
        {documentLink(application.photoDocPath, 'Fotoğraf')}
        {documentLink(application.nationalDocPath, 'Milli Sporcu')}
        {documentLink(application.disabledDocPath, 'Engelli Raporu')}
      </div>
      {application.status === 'SUBMITTED' && (
        <div className="admin-actions-section">
          <button className="admin-btn admin-btn-success" onClick={() => updateStatus('PENDING_EVALUATION')}>Değerlendirmeye Gönder</button>
          <button className="admin-btn admin-btn-danger" onClick={() => updateStatus('REJECTED')}>Reddet</button>
        </div>
      )}
      <div className="candidate-jury-assignment-panel">
        <div className="jury-specialty-header">
          <div>
            <span className="panel-eyebrow">Aday-Jüri Eşleşmesi</span>
            <h4>Otomatik jüri önerileri</h4>
            <p>Adayın bölümü ve performans bilgisine göre 3 jüri önerilir. Onaylandıktan sonra jüri ekranına düşer.</p>
          </div>
          <div className="department-assignment-actions">
            <button className="admin-btn admin-btn-secondary" type="button" disabled={assignmentLoading} onClick={() => suggestCandidateJuries(application.id)}>
              {assignmentLoading ? 'Hazırlanıyor...' : 'Önerileri Oluştur'}
            </button>
            <button className="admin-btn admin-btn-success" type="button" disabled={assignmentLoading || candidateJuryAssignments.length < 3} onClick={() => approveCandidateJuries(application.id)}>
              Eşleşmeleri Onayla
            </button>
          </div>
        </div>
        <div className="department-assignment-grid jury-inline-grid">
          {candidateJuryAssignments.length ? candidateJuryAssignments.slice(0, 3).map((assignment, index) => {
            const selectedElsewhere = new Set(Object.entries(candidateJurySelection)
              .filter(([selectionIndex]) => Number(selectionIndex) !== index)
              .map(([, juryId]) => String(juryId))
              .filter(Boolean));
            return (
            <div key={assignment.id || `${assignment.applicationId}-${assignment.juryId}`} className={`department-card ${assignment.status === 'APPROVED' ? 'assigned' : ''}`}>
              <div>
                <h5>{assignment.juryFullName}</h5>
                <p>{assignment.matchedAreas || 'Genel bölüm jüri eşleşmesi'}</p>
                <small>Skor: {assignment.matchScore} · {assignment.status === 'APPROVED' ? 'Onaylandı' : 'Öneri'}</small>
              </div>
              <select
                value={candidateJurySelection[index] || ''}
                onChange={(event) => setCandidateJurySelection((items) => ({ ...items, [index]: event.target.value }))}
              >
                <option value="">Jüri seçin</option>
                {juriesForApplication(application).map((jury) => (
                  <option key={jury.id} value={jury.id} disabled={selectedElsewhere.has(String(jury.id))}>
                    {jury.firstName} {jury.lastName}{selectedElsewhere.has(String(jury.id)) ? ' (seçildi)' : ''}
                  </option>
                ))}
              </select>
            </div>
          );
          }) : <div className="admin-empty-text">Henüz jüri önerisi oluşturulmadı.</div>}
        </div>
      </div>
      {application.status === 'COMPLETED' && (
        <div className="score-breakdown">
          <h4>Jüri Puanları</h4>
          {juryScores.map((score) => <p key={score.juryFullName}>{score.juryFullName}: <strong>{score.score}</strong></p>)}
          <p>Ortalama: <strong>{application.oyspScore}</strong></p>
        </div>
      )}
    </div>
  );

  const updateDepartmentField = (departmentId, field, value) => {
    setDepartments((items) => items.map((item) =>
      item.id === departmentId ? { ...item, [field]: value } : item));
  };

  const saveDepartmentSettings = async (department) => {
    try {
      const { data } = await api.put(`/departments/${department.id}/settings`, {
        code: department.code || '',
        examType: department.examType || 'INDIVIDUAL',
        quota: Number(department.quota),
        baseScoreRequirement: Number(department.baseScoreRequirement),
        trimScores: Boolean(department.trimScores),
        defaultCandidateIntervalMinutes: department.defaultCandidateIntervalMinutes ? Number(department.defaultCandidateIntervalMinutes) : null,
        defaultSessionDurationMinutes: department.defaultSessionDurationMinutes ? Number(department.defaultSessionDurationMinutes) : null,
        defaultBreakMinutes: department.defaultBreakMinutes ? Number(department.defaultBreakMinutes) : 0,
        requiredPrimaryJuryCount: department.requiredPrimaryJuryCount ? Number(department.requiredPrimaryJuryCount) : 3,
        requiredBackupJuryCount: department.requiredBackupJuryCount ? Number(department.requiredBackupJuryCount) : 1,
        jurySelfInactiveDeadlineHours: department.jurySelfInactiveDeadlineHours ? Number(department.jurySelfInactiveDeadlineHours) : 24,
      });
      setDepartments((items) => items.map((item) => item.id === data.id ? data : item));
      setError('');
    } catch (err) {
      setError(err.response?.data || 'Bölüm ayarları kaydedilemedi.');
    }
  };

  const updateSystemSetting = (field, value) => {
    setSystemSettings((settings) => ({ ...settings, [field]: value }));
  };

  const saveSystemSettings = async () => {
    try {
      const payload = {
        ...systemSettings,
        applicationStartDate: systemSettings.applicationStartDate || null,
        applicationEndDate: systemSettings.applicationEndDate || null,
        minTytScore: Number(systemSettings.minTytScore || 0),
      };
      const { data } = await api.put('/system-settings', payload);
      setSystemSettings({ ...defaultSystemSettings, ...(data || {}) });
      setError('Sistem ayarları kaydedildi.');
    } catch (err) {
      setError(err.response?.data || 'Sistem ayarları kaydedilemedi.');
    }
  };

  const publishPlacement = async (departmentId) => {
    if (!window.confirm('Bu bölümün sonuçları hesaplanıp adaylara yayımlansın mı?')) return;
    try {
      const { data } = await api.post(`/placements/departments/${departmentId}/publish`);
      setPlacementResults(data || []);
      setError('');
    } catch (err) {
      setError(err.response?.data || 'Yerleştirme sonuçları yayımlanamadı.');
    }
  };

  const createExamSession = async (event) => {
    event.preventDefault();
    const selectedClassroom = classrooms.find((classroom) => classroom.id === Number(sessionForm.classroomId));
    if (!selectedClassroom) {
      setError('Oturum oluşturmak için aktif salon seçin.');
      return;
    }
    try {
      const payload = {
        ...sessionForm,
        departmentId: Number(sessionForm.departmentId),
        location: selectedClassroom.building || selectedClassroom.departmentName || '',
        room: selectedClassroom.name,
        candidateIntervalMinutes: sessionForm.sessionType === 'INDIVIDUAL'
          ? Number(sessionForm.candidateIntervalMinutes)
          : null,
      };
      const { data } = await api.post('/exam-sessions', payload);
      setExamSessions((items) => [...items, data]);
      setSessionForm(emptySession);
      setExamView('list');
      setError('');
    } catch (err) {
      setError(err.response?.data || 'Sınav oturumu oluşturulamadı.');
    }
  };

  const assignExamSession = async (sessionId) => {
    try {
      await api.post(`/exam-sessions/${sessionId}/assign`);
      setError('');
    } catch (err) {
      setError(err.response?.data || 'Adaylar oturuma yerleştirilemedi.');
    }
  };

  const publishExamSession = async (sessionId) => {
    if (!window.confirm('Bu sınav programı adaylara yayımlansın mı?')) return;
    try {
      await api.post(`/exam-sessions/${sessionId}/publish`);
      setExamSessions((items) => items.map((item) => item.id === sessionId ? { ...item, published: true } : item));
      setError('');
    } catch (err) {
      setError(err.response?.data || 'Sınav programı yayımlanamadı.');
    }
  };

  const autoScheduleExam = async () => {
    if (!sessionForm.departmentId || !sessionForm.examDate || !sessionForm.startTime || !sessionForm.endTime) {
      setError('Otomatik çizelgeleme için bölüm, tarih, başlangıç ve bitiş saati girin.');
      return;
    }
    const selectedClassroomIds = (sessionForm.classroomIds || []).map(Number);
    if (!selectedClassroomIds.length) {
      setError('Otomatik program icin en az bir aktif salon secin.');
      return;
    }
    try {
      const { data } = await api.post('/exam-sessions/auto-schedule', {
        departmentId: Number(sessionForm.departmentId),
        examDate: sessionForm.examDate,
        startTime: sessionForm.startTime,
        endTime: sessionForm.endTime,
        classroomIds: selectedClassroomIds,
        published: false,
      });
      setExamSessions((items) => [...items, ...(data.sessions || [])]);
      setExamView('list');
      setError(`${data.scheduledCandidateCount} aday için ${data.sessionCount} sınav oturumu otomatik oluşturuldu.`);
    } catch (err) {
      setError(err.response?.data || 'Otomatik sınav çizelgesi oluşturulamadı.');
    }
  };

  const saveClassroom = async (event) => {
    event.preventDefault();
    try {
      const payload = {
        ...classroomForm,
        departmentId: Number(classroomForm.departmentId),
        capacity: Number(classroomForm.capacity),
        active: Boolean(classroomForm.active),
      };
      const request = editingClassroomId
        ? api.put(`/classrooms/${editingClassroomId}`, payload)
        : api.post('/classrooms', payload);
      const { data } = await request;
      setClassrooms((items) => editingClassroomId
        ? items.map((item) => item.id === data.id ? data : item)
        : [...items, data]);
      setClassroomForm(emptyClassroom);
      setEditingClassroomId(null);
      setClassroomView('list');
      setError('');
    } catch (err) {
      setError(err.response?.data || 'Salon kaydedilemedi.');
    }
  };

  const editClassroom = (classroom) => {
    setEditingClassroomId(classroom.id);
    setClassroomView('create');
    setClassroomForm({
      departmentId: classroom.departmentId,
      name: classroom.name,
      capacity: classroom.capacity,
      building: classroom.building || '',
      active: classroom.active,
    });
  };

  const toggleClassroomActive = async (classroom) => {
    try {
      const payload = {
        departmentId: classroom.departmentId,
        name: classroom.name,
        capacity: classroom.capacity,
        building: classroom.building || '',
        active: !classroom.active,
      };
      const { data } = await api.put(`/classrooms/${classroom.id}`, payload);
      setClassrooms((items) => items.map((item) => item.id === classroom.id ? data : item));
    } catch (err) {
      setError(err.response?.data || 'Salon durumu güncellenemedi.');
    }
  };

  const openDocument = async (path) => {
    try {
      const { data } = await api.get(`/files/${path}`, { responseType: 'blob' });
      const url = URL.createObjectURL(data);
      window.open(url, '_blank', 'noopener,noreferrer');
      window.setTimeout(() => URL.revokeObjectURL(url), 60000);
    } catch {
      setError('Belge açılamadı.');
    }
  };

  const documentLink = (path, label) => path && (
    <button type="button" onClick={() => openDocument(path)} className="evrak-link-item">
      📄 {label}
    </button>
  );

  const activeClassroomsForSelectedDepartment = classrooms.filter((classroom) =>
    classroom.active && classroom.departmentId === Number(sessionForm.departmentId));

  const selectedManualClassroom = classrooms.find((classroom) =>
    classroom.id === Number(sessionForm.classroomId));

  const updateSessionDepartment = (departmentId) => {
    const activeRooms = classrooms.filter((classroom) =>
      classroom.active && classroom.departmentId === Number(departmentId));
    setSessionForm({
      ...sessionForm,
      departmentId,
      classroomId: activeRooms.length === 1 ? String(activeRooms[0].id) : '',
      classroomIds: activeRooms.length === 1 ? [activeRooms[0].id] : [],
      location: activeRooms.length === 1 ? (activeRooms[0].building || '') : '',
      room: activeRooms.length === 1 ? activeRooms[0].name : '',
    });
  };

  const updateManualClassroom = (classroomId) => {
    const selected = classrooms.find((classroom) => classroom.id === Number(classroomId));
    setSessionForm({
      ...sessionForm,
      classroomId,
      location: selected?.building || '',
      room: selected?.name || '',
    });
  };

  const toggleAutoClassroom = (classroomId) => {
    const id = Number(classroomId);
    const current = (sessionForm.classroomIds || []).map(Number);
    const next = current.includes(id)
      ? current.filter((item) => item !== id)
      : [...current, id];
    setSessionForm({ ...sessionForm, classroomIds: next });
  };

  const getApprovedJuryCount = (app) => (app.juryAssignments || [])
    .filter((assignment) => assignment.status === 'APPROVED').length;

  const getJuryAssignmentState = (app) => getApprovedJuryCount(app) >= 3 ? 'APPROVED' : 'WAITING';

  const filteredApplications = applications.filter((app) => {
    const queryText = `${app.applicantFullName} ${app.applicantUsername} ${app.programName} ${app.status}`;
    return textMatches(queryText, applicationFilters.query)
      && (!applicationFilters.status || app.status === applicationFilters.status)
      && (!applicationFilters.departmentId || Number(app.departmentId) === Number(applicationFilters.departmentId))
      && (!applicationFilters.juryAssignment || getJuryAssignmentState(app) === applicationFilters.juryAssignment);
  });

  const filteredApplicationIds = filteredApplications.map((app) => app.id);
  const allFilteredApplicationsSelected = filteredApplicationIds.length > 0
    && filteredApplicationIds.every((id) => selectedApplicationIds.includes(id));

  const toggleApplicationSelection = (applicationId) => {
    setSelectedApplicationIds((ids) => ids.includes(applicationId)
      ? ids.filter((id) => id !== applicationId)
      : [...ids, applicationId]);
  };

  const toggleFilteredApplicationsSelection = () => {
    setSelectedApplicationIds((ids) => {
      if (allFilteredApplicationsSelected) {
        return ids.filter((id) => !filteredApplicationIds.includes(id));
      }
      return [...new Set([...ids, ...filteredApplicationIds])];
    });
  };

  const bulkUpdateApplicationStatus = async (status) => {
    const allowedStatuses = status === 'PENDING_EVALUATION'
      ? ['SUBMITTED']
      : ['SUBMITTED', 'PENDING_EVALUATION'];
    const selectedApplications = selectedApplicationIds
      .map((id) => applications.find((app) => app.id === id))
      .filter((app) => app && allowedStatuses.includes(app.status));

    if (!selectedApplications.length) {
      setError('Seçili başvurular içinde bu işlem için uygun kayıt yok.');
      return;
    }

    if (status === 'REJECTED' && !window.confirm(`${selectedApplications.length} başvuru reddedilsin mi?`)) {
      return;
    }

    try {
      await Promise.all(selectedApplications.map((app) =>
        api.patch(`/applications/${app.id}/status`, { status })));
      const updatedIds = selectedApplications.map((app) => app.id);
      setApplications((items) => items.map((item) =>
        updatedIds.includes(item.id) ? { ...item, status } : item));
      if (selectedApp && updatedIds.includes(selectedApp.id)) {
        setSelectedApp((item) => ({ ...item, status }));
      }
      setSelectedApplicationIds((ids) => ids.filter((id) => !updatedIds.includes(id)));
      setError(`${selectedApplications.length} başvuru güncellendi.`);
    } catch (err) {
      setError(err.response?.data || 'Toplu başvuru işlemi tamamlanamadı.');
    }
  };

  const filteredJuryMembers = juryMembers.filter((jury) => {
    const assignments = getJuryAssignmentItems(jury);
    const queryText = `${jury.firstName} ${jury.lastName} ${jury.username} ${jury.juryField} ${assignments.map((item) => item.departmentName).join(' ')}`;
    return textMatches(queryText, juryFilters.query)
      && (!juryFilters.field || jury.juryField === juryFilters.field)
      && (!juryFilters.departmentId || (jury.assignedDepartmentIds || []).map(Number).includes(Number(juryFilters.departmentId)))
      && (!juryFilters.assignmentRole || assignments.some((item) =>
        (!juryFilters.departmentId || Number(item.departmentId) === Number(juryFilters.departmentId))
        && item.role === juryFilters.assignmentRole))
      && (!juryFilters.active || String(jury.active !== false) === juryFilters.active);
  });

  const filteredAdminUsers = adminUsers.filter((managedUser) => {
    const assignments = getUserDepartmentItems(managedUser);
    const queryText = `${managedUser.firstName} ${managedUser.lastName} ${managedUser.username} ${managedUser.role} ${assignments.map((item) => item.departmentName).join(' ')}`;
    return textMatches(queryText, adminFilters.query)
      && (!adminFilters.role || managedUser.role === adminFilters.role)
      && (!adminFilters.departmentId || (managedUser.assignedDepartmentIds || []).map(Number).includes(Number(adminFilters.departmentId)))
      && (!adminFilters.active || String(managedUser.active !== false) === adminFilters.active);
  });

  const filteredClassrooms = classrooms.filter((classroom) => {
    const queryText = `${classroom.name} ${classroom.departmentName} ${classroom.building}`;
    return textMatches(queryText, classroomFilters.query)
      && (!classroomFilters.departmentId || Number(classroom.departmentId) === Number(classroomFilters.departmentId))
      && (!classroomFilters.active || String(classroom.active) === classroomFilters.active);
  });

  const filteredExamSessions = examSessions.filter((session) => {
    const queryText = `${session.departmentName} ${session.location} ${session.room} ${session.examDate} ${session.sessionType}`;
    return textMatches(queryText, examFilters.query)
      && (!examFilters.departmentId || Number(session.departmentId) === Number(examFilters.departmentId))
      && (!examFilters.sessionType || session.sessionType === examFilters.sessionType)
      && (!examFilters.published || String(session.published) === examFilters.published);
  });

  const filteredPlacementDepartments = departments.filter((department) => {
    const quota = Number(department.quota || 0);
    const baseScore = Number(department.baseScoreRequirement || 0);
    const queryText = `${department.name} ${department.examType} ${department.quota} ${department.baseScoreRequirement}`;
    return textMatches(queryText, placementFilters.query)
      && (!placementFilters.examType || department.examType === placementFilters.examType)
      && (!placementFilters.quotaState
        || (placementFilters.quotaState === 'HAS_QUOTA' && quota > 0)
        || (placementFilters.quotaState === 'NO_QUOTA' && quota <= 0)
        || (placementFilters.quotaState === 'HAS_BASE_SCORE' && baseScore > 0));
  });

  const activeClassroomCount = classrooms.filter((classroom) => classroom.active).length;
  const submittedCount = applications.filter((app) => app.status === 'SUBMITTED').length;
  const pendingEvaluationCount = applications.filter((app) => app.status === 'PENDING_EVALUATION').length;
  const waitingJuryAssignmentCount = applications.filter((app) => getJuryAssignmentState(app) === 'WAITING').length;
  const completedCount = applications.filter((app) => app.status === 'COMPLETED').length;
  const rejectedCount = applications.filter((app) => app.status === 'REJECTED').length;
  const publishedExamCount = examSessions.filter((session) => session.published).length;
  const draftExamCount = examSessions.filter((session) => !session.published).length;
  const resultPublishedCount = applications.filter((app) => app.resultPublished).length;
  const departmentsWithoutClassroom = departments.filter((department) =>
    !classrooms.some((classroom) => classroom.departmentId === department.id && classroom.active)).length;
  const pendingResultDepartments = departments.filter((department) =>
    applications.some((app) => Number(app.departmentId) === Number(department.id) && app.status === 'COMPLETED' && !app.resultPublished)).length;
  const schedulableCandidateCount = applications.filter((app) => app.status === 'PENDING_EVALUATION').length;
  const unscheduledCandidateCount = applications.filter((app) =>
    app.status === 'PENDING_EVALUATION' && !app.examDate && !app.examSessionType).length;
  const sessionsWithoutJuryCount = examSessions.filter((session) => !(session.juries || []).length).length;
  const sessionsWithoutCandidatePlacementHint = examSessions.filter((session) => !session.published).length;

  return (
    <div className="admin-container">
      <nav className="navbar">
        <div className="navbar-brand"><h2>İNÖNÜ ÜNİVERSİTESİ</h2><p>Özel Yetenek Sınavı Otomasyonu</p></div>
        <div className="user-info">
          <span className="user-badge">{adminBadge}</span>
          <span>{user.firstName} {user.lastName}</span>
          <button onClick={onLogout} className="admin-btn admin-btn-danger">Çıkış</button>
        </div>
      </nav>

      <main className="admin-content">
        <div className="admin-tabs">
          <button className={`tab-button ${activeTab === 'overview' ? 'active' : ''}`} onClick={() => setActiveTab('overview')}>Genel Bakış</button>
          <button className={`tab-button ${activeTab === 'students' ? 'active' : ''}`} onClick={() => setActiveTab('students')}>Başvurular</button>
          {isSuperAdmin && <button className={`tab-button ${activeTab === 'admins' ? 'active' : ''}`} onClick={() => setActiveTab('admins')}>Admin Yönetimi</button>}
          <button className={`tab-button ${activeTab === 'jury' ? 'active' : ''}`} onClick={() => setActiveTab('jury')}>Jüri Yönetimi</button>
          <button className={`tab-button ${activeTab === 'classrooms' ? 'active' : ''}`} onClick={() => setActiveTab('classrooms')}>Salon Yönetimi</button>
          <button className={`tab-button ${activeTab === 'exam' ? 'active' : ''}`} onClick={() => setActiveTab('exam')}>Sınav Planlama</button>
          <button className={`tab-button ${activeTab === 'placement' ? 'active' : ''}`} onClick={() => setActiveTab('placement')}>Kontenjan ve Yerleştirme</button>
          {isSuperAdmin && <button className={`tab-button ${activeTab === 'settings' ? 'active' : ''}`} onClick={() => setActiveTab('settings')}>Sistem Ayarları</button>}
          {isSuperAdmin && <button className={`tab-button ${activeTab === 'audit' ? 'active' : ''}`} onClick={() => setActiveTab('audit')}>İşlem Geçmişi</button>}
        </div>
        {error && <div className="error-msg">{String(error)}</div>}
        {loading && <div className="admin-loading-text">Veriler yükleniyor...</div>}

        {!loading && activeTab === 'overview' && (
          <section className="admin-main-panel overview-panel">
            <div className="panel-header-row">
              <div>
                <h3>Genel Bakış</h3>
                <p className="admin-help-text compact">Başvuru, jüri, salon, sınav ve sonuç süreçlerinin hızlı yönetim özeti.</p>
              </div>
              <button className="admin-btn admin-btn-secondary" onClick={fetchData}>Yenile</button>
            </div>
            <div className="overview-actions-shell">
              <div className="overview-actions-header">
                <span className="panel-eyebrow">Hızlı işlemler</span>
                <h4>En sık kullanılan yönetim adımları</h4>
              </div>
              <div className="overview-action-grid">
                <button className="overview-action-card" type="button" onClick={() => setActiveTab('students')}>
                  <span>Başvuruları kontrol et</span>
                  <strong>{submittedCount + pendingEvaluationCount}</strong>
                  <small>Yeni veya değerlendirmede olan kayıtlar</small>
                </button>
                <button className="overview-action-card" type="button" onClick={() => setActiveTab('jury')}>
                  <span>Jüri atamalarını yönet</span>
                  <strong>{juryMembers.length}</strong>
                  <small>Asıl/yedek ve bölüm atamaları</small>
                </button>
                <button className="overview-action-card" type="button" onClick={() => setActiveTab('exam')}>
                  <span>Sınav planını tamamla</span>
                  <strong>{draftExamCount}</strong>
                  <small>Yayımlanmamış sınav oturumları</small>
                </button>
                <button className="overview-action-card" type="button" onClick={() => setActiveTab('placement')}>
                  <span>Yerleştirme sonuçları</span>
                  <strong>{pendingResultDepartments}</strong>
                  <small>Yayın öncesi kontrol gereken bölüm</small>
                </button>
              </div>
            </div>
            <div className="overview-card-grid">
              <div className="overview-card accent-blue">
                <span>Toplam Başvuru</span>
                <strong>{applications.length}</strong>
                <small>{submittedCount} yeni başvuru</small>
              </div>
              <div className="overview-card accent-amber">
                <span>Değerlendirmede</span>
                <strong>{pendingEvaluationCount}</strong>
                <small>{waitingJuryAssignmentCount} adayda jüri ataması bekliyor</small>
              </div>
              <div className="overview-card accent-green">
                <span>Tamamlanan</span>
                <strong>{completedCount}</strong>
                <small>{resultPublishedCount} sonuç yayımlandı</small>
              </div>
              <div className="overview-card accent-red">
                <span>Reddedilen</span>
                <strong>{rejectedCount}</strong>
                <small>Kontrol dışı kalan başvuru</small>
              </div>
              <div className="overview-card accent-indigo">
                <span>Jüri</span>
                <strong>{juryMembers.length}</strong>
                <small>Atama yönetimine hazır</small>
              </div>
              <div className="overview-card accent-teal">
                <span>Aktif Salon</span>
                <strong>{activeClassroomCount}</strong>
                <small>{departmentsWithoutClassroom} bölümde aktif salon yok</small>
              </div>
              <div className="overview-card accent-blue">
                <span>Sınav Oturumu</span>
                <strong>{examSessions.length}</strong>
                <small>{draftExamCount} taslak · {publishedExamCount} yayımlandı</small>
              </div>
              <div className="overview-card accent-amber">
                <span>Sonuç Bekleyen Bölüm</span>
                <strong>{pendingResultDepartments}</strong>
                <small>Tamamlanan aday var, yayın bekliyor</small>
              </div>
            </div>
          </section>
        )}

        {!loading && activeTab === 'students' && (
          <section className="admin-main-panel">
              <div className="panel-header-row"><h3>Başvurular</h3><button className="admin-btn admin-btn-secondary" onClick={fetchData}>Yenile</button></div>
              <div className="admin-filter-bar">
                <input placeholder="Aday adı, T.C. veya bölüm ara" value={applicationFilters.query} onChange={(e) => setApplicationFilters({ ...applicationFilters, query: e.target.value })} />
                <select value={applicationFilters.status} onChange={(e) => setApplicationFilters({ ...applicationFilters, status: e.target.value })}>
                  <option value="">Tüm durumlar</option>
                  {Object.entries(statusLabel).map(([value, label]) => <option key={value} value={value}>{label}</option>)}
                </select>
                <select value={applicationFilters.departmentId} onChange={(e) => setApplicationFilters({ ...applicationFilters, departmentId: e.target.value })}>
                  <option value="">Tüm bölümler</option>
                  {departments.map((department) => <option key={department.id} value={department.id}>{department.name}</option>)}
                </select>
                <select value={applicationFilters.juryAssignment} onChange={(e) => setApplicationFilters({ ...applicationFilters, juryAssignment: e.target.value })}>
                  <option value="">Tüm jüri durumları</option>
                  <option value="WAITING">Atama bekliyor</option>
                  <option value="APPROVED">3 jüri onaylandı</option>
                </select>
                <button className="admin-btn admin-btn-secondary" type="button" onClick={() => setApplicationFilters({ query: '', status: '', departmentId: '', juryAssignment: '' })}>Temizle</button>
              </div>
              <div className="bulk-action-bar">
                <div className="bulk-action-summary">
                  <strong>{selectedApplicationIds.length}</strong>
                  <span>başvuru seçildi</span>
                </div>
                <button className="admin-btn admin-btn-secondary" type="button" onClick={toggleFilteredApplicationsSelection} disabled={!filteredApplications.length}>
                  {allFilteredApplicationsSelected ? 'Filtredekileri Kaldır' : 'Filtredekileri Seç'}
                </button>
                <button className="admin-btn admin-btn-secondary" type="button" onClick={() => setSelectedApplicationIds([])} disabled={!selectedApplicationIds.length}>Seçimi Temizle</button>
                <button className="admin-btn admin-btn-success" type="button" onClick={() => bulkUpdateApplicationStatus('PENDING_EVALUATION')} disabled={!selectedApplicationIds.length}>Değerlendirmeye Gönder</button>
                <button className="admin-btn admin-btn-danger" type="button" onClick={() => bulkUpdateApplicationStatus('REJECTED')} disabled={!selectedApplicationIds.length}>Reddet</button>
              </div>
              <div className="table-responsive-wrapper">
                <table className="admin-table">
                  <thead><tr><th className="admin-select-cell"><input type="checkbox" checked={allFilteredApplicationsSelected} onChange={toggleFilteredApplicationsSelection} disabled={!filteredApplications.length} /></th><th>Aday</th><th>Bölüm</th><th>Durum</th><th>Jüri Ataması</th><th></th></tr></thead>
                  <tbody>{filteredApplications.map((app) => (
                    <React.Fragment key={app.id}>
                    <tr className={selectedApp?.id === app.id ? 'row-selected' : ''}>
                      <td className="admin-select-cell">
                        <input type="checkbox" checked={selectedApplicationIds.includes(app.id)} onChange={() => toggleApplicationSelection(app.id)} />
                      </td>
                      <td>
                        <div className="admin-person-cell">
                          <span>{app.applicantFullName}</span>
                          <small>T.C. {app.applicantUsername}</small>
                        </div>
                      </td>
                      <td><span className="admin-muted-cell">{app.programName}</span></td>
                      <td><span className={`admin-status status-${String(app.status).toLowerCase()}`}>{statusLabel[app.status] || app.status}</span></td>
                      <td>
                        <span className={`jury-assignment-chip ${getJuryAssignmentState(app).toLowerCase()}`}>
                          {getJuryAssignmentState(app) === 'APPROVED'
                            ? `${getApprovedJuryCount(app)} jüri onaylandı`
                            : 'Atama bekliyor'}
                        </span>
                      </td>
                      <td className="admin-table-actions">
                        <button className="admin-btn admin-btn-primary" onClick={() => selectedApp?.id === app.id ? setSelectedApp(null) : selectApplication(app)}>
                          {selectedApp?.id === app.id ? 'Kapat' : 'İncele'}
                        </button>
                      </td>
                    </tr>
                    {selectedApp?.id === app.id && (
                      <tr className="jury-inline-row">
                        <td colSpan="6">{renderApplicationDetailPanel(selectedApp)}</td>
                      </tr>
                    )}
                    </React.Fragment>
                  ))}</tbody>
                </table>
                {!filteredApplications.length && <div className="admin-empty-text">Filtreye uygun başvuru bulunmuyor.</div>}
              </div>
          </section>
        )}

        {!loading && activeTab === 'admins' && isSuperAdmin && (
          <section className="admin-main-panel jury-management-panel">
            <div className="panel-header-row">
              <div>
                <h3>Admin Yönetimi</h3>
                <p className="admin-help-text compact">Spor, müzik ve resim gibi bölüm adminlerinin aktif/pasif durumunu ve hangi bölümleri yönetebileceğini buradan kontrol edebilirsin.</p>
              </div>
              <button className="admin-btn admin-btn-secondary" onClick={fetchData}>Yenile</button>
            </div>
            <div className="admin-filter-bar">
              <input placeholder="Admin adı, T.C. veya bölüm ara" value={adminFilters.query} onChange={(e) => setAdminFilters({ ...adminFilters, query: e.target.value })} />
              <select value={adminFilters.role} onChange={(e) => setAdminFilters({ ...adminFilters, role: e.target.value })}>
                <option value="">Tüm admin rolleri</option>
                <option value="ADMIN">Genel Admin</option>
                <option value="DEPARTMENT_ADMIN">Bölüm Admini</option>
              </select>
              <select value={adminFilters.departmentId} onChange={(e) => setAdminFilters({ ...adminFilters, departmentId: e.target.value })}>
                <option value="">Tüm bölümler</option>
                {departments.map((department) => <option key={department.id} value={department.id}>{department.name}</option>)}
              </select>
              <select value={adminFilters.active} onChange={(e) => setAdminFilters({ ...adminFilters, active: e.target.value })}>
                <option value="">Aktif/Pasif hepsi</option>
                <option value="true">Aktif</option>
                <option value="false">Pasif</option>
              </select>
              <button className="admin-btn admin-btn-secondary" type="button" onClick={() => setAdminFilters({ query: '', role: '', departmentId: '', active: '' })}>Temizle</button>
            </div>
            <div className="table-responsive-wrapper">
              <table className="admin-table admin-users-table">
                <thead><tr><th>Admin</th><th>Rol</th><th>Yetkili Bölümler</th><th>Durum</th><th></th></tr></thead>
                <tbody>{filteredAdminUsers.map((managedUser) => (
                  <React.Fragment key={managedUser.id}>
                    <tr className={selectedAdminUser?.id === managedUser.id ? 'row-selected' : ''}>
                      <td>
                        <div className="admin-person-cell">
                          <span>{managedUser.firstName} {managedUser.lastName}</span>
                          <small>T.C. {managedUser.username}</small>
                        </div>
                      </td>
                      <td><span className="admin-field-chip">{managedUser.role === 'DEPARTMENT_ADMIN' ? 'Bölüm Admini' : 'Genel Admin'}</span></td>
                      <td>
                        <div className="jury-assignment-summary">
                          {getUserDepartmentItems(managedUser).length ? getUserDepartmentItems(managedUser).map((assignment) => (
                            <span key={`${managedUser.id}-${assignment.departmentId}`} className="jury-assignment-chip primary">
                              {assignment.departmentName}
                            </span>
                          )) : <span className="jury-assignment-chip empty">Yetki yok</span>}
                        </div>
                      </td>
                      <td><span className={`admin-status ${managedUser.active === false ? 'status-rejected' : 'status-completed'}`}>{managedUser.active === false ? 'Pasif' : 'Aktif'}</span></td>
                      <td className="admin-table-actions">
                        <button className="admin-btn admin-btn-primary" onClick={() => setSelectedAdminUser(selectedAdminUser?.id === managedUser.id ? null : managedUser)}>
                          {selectedAdminUser?.id === managedUser.id ? 'Kapat' : 'Yönet'}
                        </button>
                        <button className={managedUser.active === false ? 'admin-btn admin-btn-success' : 'admin-btn admin-btn-danger'} onClick={() => toggleAdminActive(managedUser)}>
                          {managedUser.active === false ? 'Aktifleştir' : 'Pasife Al'}
                        </button>
                      </td>
                    </tr>
                    {selectedAdminUser?.id === managedUser.id && (
                      <tr className="jury-inline-row">
                        <td colSpan="5">{renderAdminControlPanel(selectedAdminUser)}</td>
                      </tr>
                    )}
                  </React.Fragment>
                ))}</tbody>
              </table>
              {!filteredAdminUsers.length && <div className="admin-empty-text">Filtreye uygun admin bulunmuyor.</div>}
            </div>
          </section>
        )}

        {!loading && activeTab === 'jury' && (
          <section className="admin-main-panel jury-management-panel">
            <div className="panel-header-row">
              <div>
                <h3>Jüri Yönetimi</h3>
                <p className="admin-help-text compact">Jürileri listele, yeni jüri ekle ve bölüm/asıl-yedek atamalarını aynı satırda yönet.</p>
              </div>
              <button className="admin-btn admin-btn-secondary" onClick={fetchData}>Yenile</button>
            </div>
            <div className="admin-subtabs">
              <button className={`admin-subtab ${juryView === 'list' ? 'active' : ''}`} type="button" onClick={() => setJuryView('list')}>
                Jüri Listeleme
                <span>{filteredJuryMembers.length}</span>
              </button>
              <button className={`admin-subtab ${juryView === 'create' ? 'active' : ''}`} type="button" onClick={() => { setJuryView('create'); setSelectedJury(null); }}>
                Jüri Ekle
                <span>+</span>
              </button>
            </div>

            {juryView === 'list' && (
              <>
              <div className="admin-filter-bar">
                <input placeholder="Jüri adı, T.C. veya bölüm ara" value={juryFilters.query} onChange={(e) => setJuryFilters({ ...juryFilters, query: e.target.value })} />
                <select value={juryFilters.field} onChange={(e) => setJuryFilters({ ...juryFilters, field: e.target.value })}>
                  <option value="">Tüm alanlar</option>
                  <option value="SPOR">Spor</option>
                  <option value="MUSIC">Müzik</option>
                  <option value="ART">Resim</option>
                  <option value="CERAMIC">Seramik</option>
                </select>
                <select value={juryFilters.departmentId} onChange={(e) => setJuryFilters({ ...juryFilters, departmentId: e.target.value })}>
                  <option value="">Tüm bölümler</option>
                  {departments.map((department) => <option key={department.id} value={department.id}>{department.name}</option>)}
                </select>
                <select value={juryFilters.assignmentRole} onChange={(e) => setJuryFilters({ ...juryFilters, assignmentRole: e.target.value })}>
                  <option value="">Asıl/Yedek hepsi</option>
                  <option value="PRIMARY">Asıl</option>
                  <option value="BACKUP">Yedek</option>
                </select>
                <select value={juryFilters.active} onChange={(e) => setJuryFilters({ ...juryFilters, active: e.target.value })}>
                  <option value="">Aktif/Pasif hepsi</option>
                  <option value="true">Aktif</option>
                  <option value="false">Pasif</option>
                </select>
                <button className="admin-btn admin-btn-secondary" type="button" onClick={() => setJuryFilters({ query: '', field: '', departmentId: '', assignmentRole: '', active: '' })}>Temizle</button>
              </div>
              <div className="table-responsive-wrapper">
              <table className="admin-table jury-table">
                <thead><tr><th>Ad Soyad</th><th>Alan</th><th>Atama</th><th>Durum</th><th></th></tr></thead>
                <tbody>{filteredJuryMembers.map((jury) => (
                  <React.Fragment key={jury.id}>
                    <tr className={selectedJury?.id === jury.id ? 'row-selected' : ''}>
                      <td>
                        <div className="admin-person-cell">
                          <span>{jury.firstName} {jury.lastName}</span>
                          <small>T.C. {jury.username}</small>
                        </div>
                      </td>
                      <td><span className="admin-field-chip">{juryFieldLabel(jury.juryField)}</span></td>
                      <td>
                        <div className="jury-assignment-summary">
                          {getJuryAssignmentItems(jury).length ? getJuryAssignmentItems(jury).map((assignment) => (
                            <span
                              key={`${jury.id}-${assignment.departmentId}`}
                              className={`jury-assignment-chip ${assignment.role === 'BACKUP' ? 'backup' : 'primary'}`}
                            >
                              {assignment.departmentName} · {assignment.role === 'BACKUP' ? 'Yedek' : 'Asıl'}
                            </span>
                          )) : <span className="jury-assignment-chip empty">Atanmamış</span>}
                        </div>
                      </td>
                      <td><span className={`admin-status ${jury.active === false ? 'status-rejected' : 'status-completed'}`}>{jury.active === false ? 'Pasif' : 'Aktif'}</span></td>
                      <td className="admin-table-actions">
                        <button
                          className="admin-btn admin-btn-primary"
                          onClick={() => setSelectedJury(selectedJury?.id === jury.id ? null : jury)}
                        >
                          {selectedJury?.id === jury.id ? 'Kapat' : 'Yönet'}
                        </button>
                        {jury.active === false ? (
                          <button className="admin-btn admin-btn-success" onClick={() => toggleJuryActive(jury)}>Aktifleştir</button>
                        ) : (
                          <button className="admin-btn admin-btn-danger" onClick={() => deleteJury(jury.id)}>Pasife Al</button>
                        )}
                      </td>
                    </tr>
                    {selectedJury?.id === jury.id && (
                      <tr className="jury-inline-row">
                        <td colSpan="5">{renderJuryAssignmentPanel(selectedJury)}</td>
                      </tr>
                    )}
                  </React.Fragment>
                ))}</tbody>
              </table>
              {!filteredJuryMembers.length && <div className="admin-empty-text">Filtreye uygun jüri bulunmuyor.</div>}
              </div>
              </>
            )}

            {juryView === 'create' && (
              <div className="jury-create-card">
                <div className="jury-create-copy">
                  <span className="panel-eyebrow">Yeni jüri</span>
                  <h4>Jüri hesabı oluştur</h4>
                  <p>Jüriyi oluşturduktan sonra listeleme sekmesinden bölümünü ve asıl/yedek durumunu hemen atayabilirsin.</p>
                </div>
                <form onSubmit={createJury} className="jury-create-form">
                  <input placeholder="T.C. Kimlik No" value={juryForm.username} onChange={(e) => setJuryForm({ ...juryForm, username: e.target.value })} required />
                  <input placeholder="Ad" value={juryForm.firstName} onChange={(e) => setJuryForm({ ...juryForm, firstName: e.target.value })} required />
                  <input placeholder="Soyad" value={juryForm.lastName} onChange={(e) => setJuryForm({ ...juryForm, lastName: e.target.value })} required />
                  <input type="password" placeholder="Şifre (en az 8 karakter)" value={juryForm.password} onChange={(e) => setJuryForm({ ...juryForm, password: e.target.value })} required minLength="8" />
                  <select value={juryForm.juryField} onChange={(e) => setJuryForm({ ...juryForm, juryField: e.target.value })}>
                    <option value="SPOR">Spor</option>
                    <option value="MUSIC">Müzik</option>
                    <option value="ART">Resim</option>
                    <option value="CERAMIC">Seramik</option>
                  </select>
                  <button className="admin-btn admin-btn-success" type="submit">Jüri Oluştur</button>
                </form>
              </div>
            )}
          </section>
        )}

        {!loading && activeTab === 'settings' && isSuperAdmin && (
          <section className="admin-main-panel system-settings-panel">
            <div className="panel-header-row">
              <div>
                <span className="panel-eyebrow">Super Admin</span>
                <h3>Sistem Ayarları</h3>
                <p className="admin-help-text compact">Aday başvuru kuralları, belge zorunlulukları ve belge indirme izinlerini tek yerden yönet.</p>
              </div>
              <button className="admin-btn admin-btn-secondary" onClick={fetchData}>Yenile</button>
            </div>

            <div className="settings-grid">
              <div className="settings-card accent-blue">
                <h4>Başvuru Dönemi</h4>
                <label className="admin-inline-check">
                  <input
                    type="checkbox"
                    checked={Boolean(systemSettings.applicationsOpen)}
                    onChange={(e) => updateSystemSetting('applicationsOpen', e.target.checked)}
                  />
                  Başvurular adaylara açık
                </label>
                <div className="settings-two-column">
                  <label>
                    Başlangıç tarihi
                    <input
                      type="date"
                      value={systemSettings.applicationStartDate || ''}
                      onChange={(e) => updateSystemSetting('applicationStartDate', e.target.value)}
                    />
                  </label>
                  <label>
                    Bitiş tarihi
                    <input
                      type="date"
                      value={systemSettings.applicationEndDate || ''}
                      onChange={(e) => updateSystemSetting('applicationEndDate', e.target.value)}
                    />
                  </label>
                </div>
              </div>

              <div className="settings-card accent-green">
                <h4>Puan Kuralları</h4>
                <label>
                  Minimum TYT puanı
                  <input
                    type="number"
                    min="0"
                    max="500"
                    step="0.001"
                    value={systemSettings.minTytScore ?? 150}
                    onChange={(e) => updateSystemSetting('minTytScore', e.target.value)}
                  />
                </label>
                <label className="admin-inline-check">
                  <input
                    type="checkbox"
                    checked={Boolean(systemSettings.requireObp)}
                    onChange={(e) => updateSystemSetting('requireObp', e.target.checked)}
                  />
                  OBP bilgisini aday formunda zorunlu yap
                </label>
                <p className="admin-help-text compact">OBP kapalıyken aday isterse girer; admin resmi belgeler üzerinden kontrol edebilir.</p>
              </div>

              <div className="settings-card accent-purple">
                <h4>Belge Zorunlulukları</h4>
                <div className="settings-toggle-list">
                  {[
                    ['requireOsymDocument', 'ÖSYM sonuç belgesi'],
                    ['requireDiplomaDocument', 'Diploma / mezuniyet belgesi'],
                    ['requireHealthDocument', 'Sağlık raporu'],
                    ['requirePhotoDocument', 'Biyometrik fotoğraf'],
                    ['requireNationalDocument', 'Milli aday belgesi'],
                    ['requireDisabledDocument', 'Engelli aday raporu'],
                  ].map(([field, label]) => (
                    <label className="admin-inline-check" key={field}>
                      <input
                        type="checkbox"
                        checked={Boolean(systemSettings[field])}
                        onChange={(e) => updateSystemSetting(field, e.target.checked)}
                      />
                      {label}
                    </label>
                  ))}
                </div>
              </div>

              <div className="settings-card accent-orange">
                <h4>Aday Belge İndirme</h4>
                <label className="admin-inline-check">
                  <input
                    type="checkbox"
                    checked={Boolean(systemSettings.examDocumentEnabled)}
                    onChange={(e) => updateSystemSetting('examDocumentEnabled', e.target.checked)}
                  />
                  Sınava giriş belgesi indirilebilir
                </label>
                <label className="admin-inline-check">
                  <input
                    type="checkbox"
                    checked={Boolean(systemSettings.resultDocumentEnabled)}
                    onChange={(e) => updateSystemSetting('resultDocumentEnabled', e.target.checked)}
                  />
                  Sonuç belgesi indirilebilir
                </label>
              </div>
            </div>

            <div className="admin-actions-section settings-actions">
              <button className="admin-btn admin-btn-primary" type="button" onClick={saveSystemSettings}>Sistem Ayarlarını Kaydet</button>
            </div>
          </section>
        )}

        {!loading && activeTab === 'audit' && isSuperAdmin && (
          <section className="admin-main-panel">
            <div className="panel-header-row">
              <div>
                <h3>İşlem Geçmişi</h3>
                <p className="admin-help-text compact">Admin panelinde yapılan kritik işlemleri kim, ne zaman ve hangi kayıt üzerinde yaptı buradan izleyebilirsiniz.</p>
              </div>
              <button className="admin-btn admin-btn-secondary" onClick={fetchData}>Yenile</button>
            </div>
            <div className="admin-filter-bar audit-filter-bar">
              <input placeholder="Yapan kişi, hedef veya açıklama ara" value={auditFilters.query} onChange={(e) => setAuditFilters({ ...auditFilters, query: e.target.value })} />
              <select value={auditFilters.action} onChange={(e) => setAuditFilters({ ...auditFilters, action: e.target.value })}>
                <option value="">Tüm işlemler</option>
                {Object.entries(auditActionLabel).map(([value, label]) => <option key={value} value={value}>{label}</option>)}
              </select>
              <select value={auditFilters.targetType} onChange={(e) => setAuditFilters({ ...auditFilters, targetType: e.target.value })}>
                <option value="">Tüm hedefler</option>
                {Object.entries(auditTargetLabel).map(([value, label]) => <option key={value} value={value}>{label}</option>)}
              </select>
              <select value={auditFilters.limit} onChange={(e) => setAuditFilters({ ...auditFilters, limit: Number(e.target.value) })}>
                <option value="50">Son 50</option>
                <option value="100">Son 100</option>
                <option value="200">Son 200</option>
              </select>
              <button className="admin-btn admin-btn-secondary" type="button" onClick={() => setAuditFilters({ query: '', action: '', targetType: '', limit: 100 })}>Temizle</button>
            </div>
            <div className="table-responsive-wrapper">
              <table className="admin-table audit-table">
                <thead><tr><th>Tarih</th><th>Yapan</th><th>İşlem</th><th>Hedef</th><th>Açıklama</th></tr></thead>
                <tbody>{auditLogs.map((log) => (
                  <tr key={log.id}>
                    <td><span className="admin-muted-cell">{formatAuditDate(log.createdAt)}</span></td>
                    <td>
                      <div className="admin-person-cell">
                        <span>{log.actorFullName || '-'}</span>
                        <small>{log.actorUsername || '-'}</small>
                      </div>
                    </td>
                    <td><span className="admin-field-chip">{auditActionLabel[log.action] || log.action}</span></td>
                    <td>
                      <div className="admin-person-cell">
                        <span>{auditTargetLabel[log.targetType] || log.targetType}</span>
                        <small>{log.targetLabel || `#${log.targetId || '-'}`}</small>
                      </div>
                    </td>
                    <td>{log.description || '-'}</td>
                  </tr>
                ))}</tbody>
              </table>
              {!auditLogs.length && <div className="admin-empty-text">Filtreye uygun işlem kaydı bulunmuyor.</div>}
            </div>
          </section>
        )}

        {!loading && activeTab === 'placement' && (
          <div className="admin-content-grid">
            <section className="admin-main-panel">
              <h3>Bölüm Kontenjan ve Puan Ayarları</h3>
              <div className="admin-filter-bar">
                <input placeholder="Bölüm, sınav tipi veya puan ara" value={placementFilters.query} onChange={(e) => setPlacementFilters({ ...placementFilters, query: e.target.value })} />
                <select value={placementFilters.examType} onChange={(e) => setPlacementFilters({ ...placementFilters, examType: e.target.value })}>
                  <option value="">Tüm sınav tipleri</option>
                  <option value="INDIVIDUAL">Bireysel</option>
                  <option value="GROUP">Toplu</option>
                  <option value="TRACK">Parkur</option>
                </select>
                <select value={placementFilters.quotaState} onChange={(e) => setPlacementFilters({ ...placementFilters, quotaState: e.target.value })}>
                  <option value="">Tüm kontenjanlar</option>
                  <option value="HAS_QUOTA">Kontenjanı olanlar</option>
                  <option value="NO_QUOTA">Kontenjanı olmayanlar</option>
                  <option value="HAS_BASE_SCORE">TYT tabanı olanlar</option>
                </select>
                <button className="admin-btn admin-btn-secondary" type="button" onClick={() => setPlacementFilters({ query: '', examType: '', quotaState: '' })}>Temizle</button>
              </div>
              <div className="table-responsive-wrapper">
              <table className="admin-table">
                <thead><tr><th>Bölüm</th><th>Sınav Tipi</th><th>Kontenjan</th><th>TYT Taban</th><th>Asıl/Yedek</th><th>Pasif Son Süre</th><th>İşlemler</th></tr></thead>
                <tbody>{filteredPlacementDepartments.map((department) => (
                  <tr key={department.id}>
                    <td>{department.name}</td>
                    <td>
                      <select value={department.examType || 'INDIVIDUAL'} onChange={(e) => updateDepartmentField(department.id, 'examType', e.target.value)}>
                        <option value="INDIVIDUAL">Bireysel</option>
                        <option value="GROUP">Toplu</option>
                        <option value="TRACK">Parkur</option>
                      </select>
                    </td>
                    <td><input type="number" min="0" value={department.quota}
                      onChange={(e) => updateDepartmentField(department.id, 'quota', e.target.value)} /></td>
                    <td><input type="number" min="0" max="500" value={department.baseScoreRequirement}
                      onChange={(e) => updateDepartmentField(department.id, 'baseScoreRequirement', e.target.value)} /></td>
                    <td className="admin-compact-inputs">
                      <input type="number" min="1" title="Asıl jüri" value={department.requiredPrimaryJuryCount ?? 3}
                        onChange={(e) => updateDepartmentField(department.id, 'requiredPrimaryJuryCount', e.target.value)} />
                      <input type="number" min="0" title="Yedek jüri" value={department.requiredBackupJuryCount ?? 1}
                        onChange={(e) => updateDepartmentField(department.id, 'requiredBackupJuryCount', e.target.value)} />
                    </td>
                    <td><input type="number" min="0" value={department.jurySelfInactiveDeadlineHours ?? 24}
                      onChange={(e) => updateDepartmentField(department.id, 'jurySelfInactiveDeadlineHours', e.target.value)} /> saat</td>
                    <td className="admin-table-actions placement-actions">
                      <button className="admin-btn admin-btn-secondary" onClick={() => saveDepartmentSettings(department)}>Ayarları Kaydet</button>
                      <button className="admin-btn admin-btn-success" onClick={() => publishPlacement(department.id)}>Sonuçları Yayımla</button>
                    </td>
                  </tr>
                ))}</tbody>
              </table>
              {!filteredPlacementDepartments.length && <div className="admin-empty-text">Filtreye uygun bölüm bulunmuyor.</div>}
              </div>
            </section>
            <aside className="admin-sidebar-panel">
              <div className="detail-card">
                <div className="detail-header"><h4>Son Yayımlanan Sıralama</h4></div>
                <div className="detail-body">
                  {placementResults.length === 0 && <p>Bir bölümün sonuçlarını yayımladığınızda sıralama burada görünür.</p>}
                  {placementResults.map((result) => (
                    <p key={result.id}>
                      <strong>#{result.placementRank} {result.applicantFullName}</strong><br />
                      YP: {result.finalPlacementScore} · {result.placementStatus}
                    </p>
                  ))}
                </div>
              </div>
            </aside>
          </div>
        )}

        {!loading && activeTab === 'classrooms' && (
          <section className="admin-main-panel classroom-management-panel">
            <div className="panel-header-row">
              <div>
                <h3>Salon Yönetimi</h3>
                <p className="admin-help-text compact">Sınav yapılacak oda/salonları listele, yeni salon ekle veya mevcut salonu düzenle.</p>
              </div>
              <button className="admin-btn admin-btn-secondary" onClick={fetchData}>Yenile</button>
            </div>
            <div className="admin-subtabs">
              <button className={`admin-subtab ${classroomView === 'list' ? 'active' : ''}`} type="button" onClick={() => setClassroomView('list')}>
                Salon Listesi
                <span>{filteredClassrooms.length}</span>
              </button>
              <button
                className={`admin-subtab ${classroomView === 'create' ? 'active' : ''}`}
                type="button"
                onClick={() => { setClassroomView('create'); setEditingClassroomId(null); setClassroomForm(emptyClassroom); }}
              >
                Salon Ekle
                <span>+</span>
              </button>
            </div>

            {classroomView === 'list' && (
              <>
                <div className="admin-filter-bar">
                  <input placeholder="Salon, bina veya bölüm ara" value={classroomFilters.query} onChange={(e) => setClassroomFilters({ ...classroomFilters, query: e.target.value })} />
                  <select value={classroomFilters.departmentId} onChange={(e) => setClassroomFilters({ ...classroomFilters, departmentId: e.target.value })}>
                    <option value="">Tüm bölümler</option>
                    {departments.map((department) => <option key={department.id} value={department.id}>{department.name}</option>)}
                  </select>
                  <select value={classroomFilters.active} onChange={(e) => setClassroomFilters({ ...classroomFilters, active: e.target.value })}>
                    <option value="">Tüm durumlar</option>
                    <option value="true">Aktif</option>
                    <option value="false">Pasif</option>
                  </select>
                  <button className="admin-btn admin-btn-secondary" type="button" onClick={() => setClassroomFilters({ query: '', departmentId: '', active: '' })}>Temizle</button>
                </div>
              <div className="table-responsive-wrapper">
                <table className="admin-table classroom-table">
                  <thead><tr><th>Salon</th><th>Bölüm</th><th>Bina</th><th>Kapasite</th><th>Durum</th><th></th></tr></thead>
                  <tbody>{filteredClassrooms.map((classroom) => (
                    <tr key={classroom.id}>
                      <td>
                        <div className="admin-person-cell">
                          <span>{classroom.name}</span>
                          <small>{classroom.departmentName}</small>
                        </div>
                      </td>
                      <td>{classroom.departmentName}</td>
                      <td>{classroom.building || '-'}</td>
                      <td><span className="admin-field-chip">{classroom.capacity} kişi</span></td>
                      <td><span className={`admin-status ${classroom.active ? 'status-completed' : 'status-rejected'}`}>{classroom.active ? 'Aktif' : 'Pasif'}</span></td>
                      <td className="admin-table-actions">
                        <button className="admin-btn admin-btn-primary" onClick={() => editClassroom(classroom)}>Düzenle</button>
                        <button className={classroom.active ? 'admin-btn admin-btn-danger' : 'admin-btn admin-btn-success'} onClick={() => toggleClassroomActive(classroom)}>
                          {classroom.active ? 'Pasife Al' : 'Aktifleştir'}
                        </button>
                      </td>
                    </tr>
                  ))}</tbody>
                </table>
                {!filteredClassrooms.length && <div className="admin-empty-text">Filtreye uygun salon bulunmuyor.</div>}
              </div>
              </>
            )}

            {classroomView === 'create' && (
              <div className="jury-create-card classroom-create-card">
                <div className="jury-create-copy">
                  <span className="panel-eyebrow">{editingClassroomId ? 'Salon düzenleme' : 'Yeni salon'}</span>
                  <h4>{editingClassroomId ? 'Salon bilgilerini güncelle' : 'Sınav salonu oluştur'}</h4>
                  <p>Görsel sanatlar için toplu sınav kapasitesi, müzik/spor için bireysel veya parkur akışına uygun oda kapasitesi girilebilir.</p>
                </div>
                <form className="exam-session-form classroom-form-card" onSubmit={saveClassroom}>
                  <label>Bölüm</label>
                  <select value={classroomForm.departmentId} onChange={(e) => setClassroomForm({ ...classroomForm, departmentId: e.target.value })} required>
                    <option value="">Bölüm seçin</option>
                    {departments.map((department) => <option key={department.id} value={department.id}>{department.name}</option>)}
                  </select>
                  <label>Salon Adı</label>
                  <input value={classroomForm.name} onChange={(e) => setClassroomForm({ ...classroomForm, name: e.target.value })} placeholder="Örn: Müzik Odası-1" required />
                  <label>Bina</label>
                  <input value={classroomForm.building} onChange={(e) => setClassroomForm({ ...classroomForm, building: e.target.value })} placeholder="Örn: Güzel Sanatlar Fakültesi" />
                  <label>Kapasite</label>
                  <input type="number" min="1" value={classroomForm.capacity} onChange={(e) => setClassroomForm({ ...classroomForm, capacity: e.target.value })} required />
                  <label className="admin-inline-check">
                    <input type="checkbox" checked={classroomForm.active} onChange={(e) => setClassroomForm({ ...classroomForm, active: e.target.checked })} />
                    Aktif salon
                  </label>
                  <div className="admin-actions-section classroom-form-actions">
                    <button className="admin-btn admin-btn-success" type="submit">{editingClassroomId ? 'Güncelle' : 'Salon Ekle'}</button>
                    {editingClassroomId && <button className="admin-btn admin-btn-secondary" type="button" onClick={() => { setEditingClassroomId(null); setClassroomForm(emptyClassroom); }}>Vazgeç</button>}
                  </div>
                </form>
              </div>
            )}
          </section>
        )}

        {!loading && activeTab === 'exam' && (
          <section className="admin-main-panel exam-management-panel">
            <div className="panel-header-row">
              <div>
                <h3>Sınav Planlama</h3>
                <p className="admin-help-text compact">Oluşturulan sınav oturumlarını listele veya yeni manuel/otomatik sınav oturumu planla.</p>
              </div>
              <button className="admin-btn admin-btn-secondary" onClick={fetchData}>Yenile</button>
            </div>
            <div className="admin-subtabs">
              <button className={`admin-subtab ${examView === 'list' ? 'active' : ''}`} type="button" onClick={() => setExamView('list')}>
                Sınav Oturumları
                <span>{filteredExamSessions.length}</span>
              </button>
              <button className={`admin-subtab ${examView === 'create' ? 'active' : ''}`} type="button" onClick={() => setExamView('create')}>
                Yeni Sınav Oturumu
                <span>+</span>
              </button>
            </div>

            <div className="exam-control-grid">
              <div className={`exam-control-card ${unscheduledCandidateCount ? 'warning' : 'ok'}`}>
                <span>Oturumsuz Aday</span>
                <strong>{unscheduledCandidateCount}</strong>
                <small>{schedulableCandidateCount} değerlendirmedeki aday içinde</small>
              </div>
              <div className={`exam-control-card ${departmentsWithoutClassroom ? 'danger' : 'ok'}`}>
                <span>Aktif Salonsuz Bölüm</span>
                <strong>{departmentsWithoutClassroom}</strong>
                <small>Planlama öncesi salon eklenmeli</small>
              </div>
              <div className={`exam-control-card ${sessionsWithoutJuryCount ? 'warning' : 'ok'}`}>
                <span>Jürisiz Oturum</span>
                <strong>{sessionsWithoutJuryCount}</strong>
                <small>Otomatik jüri ataması kontrol edilmeli</small>
              </div>
              <div className={`exam-control-card ${draftExamCount ? 'info' : 'ok'}`}>
                <span>Taslak Oturum</span>
                <strong>{draftExamCount}</strong>
                <small>{publishedExamCount} oturum yayımlandı</small>
              </div>
            </div>

            {examView === 'list' && (
              <>
                <div className="admin-filter-bar">
                  <input placeholder="Bölüm, yer, salon veya tarih ara" value={examFilters.query} onChange={(e) => setExamFilters({ ...examFilters, query: e.target.value })} />
                  <select value={examFilters.departmentId} onChange={(e) => setExamFilters({ ...examFilters, departmentId: e.target.value })}>
                    <option value="">Tüm bölümler</option>
                    {departments.map((department) => <option key={department.id} value={department.id}>{department.name}</option>)}
                  </select>
                  <select value={examFilters.sessionType} onChange={(e) => setExamFilters({ ...examFilters, sessionType: e.target.value })}>
                    <option value="">Tüm sınav tipleri</option>
                    <option value="GROUP">Toplu</option>
                    <option value="INDIVIDUAL">Bireysel</option>
                    <option value="TRACK">Parkur</option>
                  </select>
                  <select value={examFilters.published} onChange={(e) => setExamFilters({ ...examFilters, published: e.target.value })}>
                    <option value="">Tüm yayın durumları</option>
                    <option value="true">Yayımlandı</option>
                    <option value="false">Taslak</option>
                  </select>
                  <button className="admin-btn admin-btn-secondary" type="button" onClick={() => setExamFilters({ query: '', departmentId: '', sessionType: '', published: '' })}>Temizle</button>
                </div>
              <div className="table-responsive-wrapper">
                <table className="admin-table">
                  <thead><tr><th>Bölüm</th><th>Tip</th><th>Tarih / Saat</th><th>Yer</th><th>Jüriler</th><th>Durum</th><th>İşlemler</th></tr></thead>
                  <tbody>{filteredExamSessions.map((session) => (
                    <tr key={session.id}>
                      <td>{session.departmentName}</td>
                      <td><span className="admin-field-chip">{session.sessionType === 'GROUP' ? 'Toplu' : 'Bireysel'}</span></td>
                      <td>{session.examDate} · {session.startTime}{session.endTime ? ` - ${session.endTime}` : ''}</td>
                      <td>{session.location} / {session.room}</td>
                      <td>
                        <div className="admin-jury-chip-list">
                          {(session.juries || []).length
                            ? session.juries.map((jury) => <span key={`${session.id}-${jury.juryId}`}>{jury.juryName} · {jury.replacement ? 'Yedek' : 'Asıl'}</span>)
                            : <span>Atama yok</span>}
                        </div>
                      </td>
                      <td><span className={`admin-status ${session.published ? 'status-completed' : 'status-pending_evaluation'}`}>{session.published ? 'Yayımlandı' : 'Taslak'}</span></td>
                      <td className="admin-table-actions">
                        <button className="admin-btn admin-btn-secondary" onClick={() => assignExamSession(session.id)}>Adayları Yerleştir</button>
                        <button className="admin-btn admin-btn-success" onClick={() => publishExamSession(session.id)}>Yayımla</button>
                      </td>
                    </tr>
                  ))}</tbody>
                </table>
                {!filteredExamSessions.length && <div className="admin-empty-text">Filtreye uygun sınav oturumu bulunmuyor.</div>}
              </div>
              </>
            )}

            {examView === 'create' && (
              <div className="jury-create-card exam-create-card">
                <div className="jury-create-copy">
                  <span className="panel-eyebrow">Yeni sınav oturumu</span>
                  <h4>Manuel ya da otomatik program oluştur</h4>
                  <p>Görsel sanatlar için toplu, müzik/spor için bireysel veya parkur akışına uygun oturum planlayabilirsin.</p>
                </div>
                <form className="exam-session-form classroom-form-card" onSubmit={createExamSession}>
                  <label>Bölüm</label>
                  <select value={sessionForm.departmentId} onChange={(e) => updateSessionDepartment(e.target.value)} required>
                    <option value="">Bölüm seçin</option>
                    {departments.map((department) => <option key={department.id} value={department.id}>{department.name}</option>)}
                  </select>
                  <label>Sınav Tipi</label>
                  {sessionForm.departmentId && (
                    <>
                      <label>Otomatik Programda Kullanilacak Aktif Salonlar</label>
                      <div className="classroom-choice-list">
                        {activeClassroomsForSelectedDepartment.length ? activeClassroomsForSelectedDepartment.map((classroom) => (
                          <label key={classroom.id} className="classroom-choice-item">
                            <input
                              type="checkbox"
                              checked={(sessionForm.classroomIds || []).map(Number).includes(classroom.id)}
                              onChange={() => toggleAutoClassroom(classroom.id)}
                            />
                            <span>
                              <strong>{classroom.name}</strong>
                              <small>{classroom.building || 'Bina bilgisi yok'} - Kapasite: {classroom.capacity}</small>
                            </span>
                          </label>
                        )) : <p className="admin-help-text">Bu bolum icin aktif salon yok. Salon Yonetimi sekmesinden aktif salon ekleyin.</p>}
                      </div>
                    </>
                  )}
                  <select value={sessionForm.sessionType} onChange={(e) => setSessionForm({ ...sessionForm, sessionType: e.target.value })}>
                    <option value="GROUP">Toplu sınav - aynı oda/saat</option>
                    <option value="INDIVIDUAL">Bireysel sınav - adaylar tek tek</option>
                  </select>
                  <label>Tarih</label>
                  <input type="date" value={sessionForm.examDate} onChange={(e) => setSessionForm({ ...sessionForm, examDate: e.target.value })} required />
                  <label>Başlangıç Saati</label>
                  <input type="time" value={sessionForm.startTime} onChange={(e) => setSessionForm({ ...sessionForm, startTime: e.target.value })} required />
                  <label>Bitiş Saati</label>
                  <input type="time" value={sessionForm.endTime} onChange={(e) => setSessionForm({ ...sessionForm, endTime: e.target.value })} />
                  {sessionForm.sessionType === 'INDIVIDUAL' && (
                    <>
                      <label>Aday Başına Süre (dk)</label>
                      <input type="number" min="1" value={sessionForm.candidateIntervalMinutes} onChange={(e) => setSessionForm({ ...sessionForm, candidateIntervalMinutes: e.target.value })} required />
                    </>
                  )}
                  <label>Manuel Oturum Salonu</label>
                  <select value={sessionForm.classroomId} onChange={(e) => updateManualClassroom(e.target.value)} required>
                    <option value="">Aktif salon secin</option>
                    {activeClassroomsForSelectedDepartment.map((classroom) => (
                      <option key={classroom.id} value={classroom.id}>
                        {classroom.building ? `${classroom.building} / ` : ''}{classroom.name} - Kapasite {classroom.capacity}
                      </option>
                    ))}
                  </select>
                  <p className="admin-help-text">
                    {selectedManualClassroom
                      ? `Bina: ${selectedManualClassroom.building || '-'} | Salon: ${selectedManualClassroom.name} | Kapasite: ${selectedManualClassroom.capacity}`
                      : 'Pasif salonlar listelenmez; sadece aktif salon secilebilir.'}
                  </p>
                  <label>Yer</label>
                  <input value={sessionForm.location} onChange={(e) => setSessionForm({ ...sessionForm, location: e.target.value })} placeholder="Örn: Güzel Sanatlar Fakültesi" required />
                  <label>Salon / Oda</label>
                  <input value={sessionForm.room} onChange={(e) => setSessionForm({ ...sessionForm, room: e.target.value })} placeholder="Örn: Desen Atölyesi 1" required />
                  <div className="admin-actions-section classroom-form-actions">
                    <button className="admin-btn admin-btn-primary" type="button" onClick={autoScheduleExam}>Otomatik Program Oluştur</button>
                    <button className="admin-btn admin-btn-success" type="submit">Manuel Oturum Oluştur</button>
                  </div>
                </form>
              </div>
            )}
          </section>
        )}
      </main>
    </div>
  );
}

export default AdminDashboard;
