import { test, expect } from '@playwright/test';

const password = 'Test1234!';
const applicant = '22222222222';
const juries = ['33333333332', '44444444444', '55555555552'];

async function login(page, username) {
  await page.goto('/');
  await page.getByPlaceholder('T.C. Kimlik numaranızı girin').fill(username);
  await page.getByPlaceholder('Şifrenizi girin').fill(password);
  await page.getByRole('button', { name: 'Giriş Yap' }).click();
}

async function logout(page) {
  await page.getByRole('button', { name: /Çıkış/ }).click();
}

test('aday, admin ve üç jüri ile sonuç yayımlama akışı', async ({ page, request }) => {
  await request.post('http://127.0.0.1:8080/api/auth/register', {
    data: { username: applicant, password, firstName: 'Demo', lastName: 'Aday' },
  });

  const adminLogin = await request.post('http://127.0.0.1:8080/api/auth/login', {
    data: { username: '10000000146', password },
  });
  expect(adminLogin.ok()).toBeTruthy();
  const { token } = await adminLogin.json();
  const headers = { Authorization: `Bearer ${token}` };
  const departmentResponse = await request.get('http://127.0.0.1:8080/api/departments/all', { headers });
  const [department] = await departmentResponse.json();
  for (let index = 0; index < juries.length; index++) {
    const createResponse = await request.post('http://127.0.0.1:8080/api/users/create', {
      headers,
      data: { username: juries[index], password, firstName: `Jüri${index + 1}`, lastName: 'Üyesi', role: 'JURY', juryField: 'SPOR' },
    });
    const createBody = await createResponse.text();
    if (!createResponse.ok()) throw new Error(`Jüri oluşturma ${createResponse.status()}: ${createBody}`);
    const jury = JSON.parse(createBody);
    const assignmentResponse = await request.post('http://127.0.0.1:8080/api/jury/assign', {
      headers,
      data: { juryId: jury.id, departmentId: department.id },
    });
    expect(assignmentResponse.ok()).toBeTruthy();
  }

  await login(page, applicant);
  await page.getByRole('button', { name: 'Hemen Başvuru Formunu Doldur' }).click();
  await page.getByPlaceholder('Örn: 245.670').fill('300');
  await page.getByPlaceholder('Örn: 425.50').fill('400');
  await page.getByRole('button', { name: /İleri: Bölüm Tercihi/ }).click();
  await page.getByText('Spor Bilimleri Fakültesi', { exact: true }).click();
  await page.getByText('Beden Eğitimi ve Spor Öğretmenliği', { exact: true }).click();
  await page.getByRole('button', { name: /İleri: Evrakları Yükle/ }).click();
  const pdf = { name: 'document.pdf', mimeType: 'application/pdf', buffer: Buffer.from('%PDF-1.4') };
  const photo = { name: 'photo.jpg', mimeType: 'image/jpeg', buffer: Buffer.from([0xff, 0xd8, 0xff, 0xe0]) };
  const fileInputs = page.locator('input[type=file]');
  await fileInputs.nth(0).setInputFiles(pdf);
  await fileInputs.nth(1).setInputFiles(pdf);
  await fileInputs.nth(2).setInputFiles(pdf);
  await fileInputs.nth(3).setInputFiles(photo);
  page.once('dialog', (dialog) => dialog.accept());
  await page.getByRole('button', { name: /Başvuruyu Resmi Olarak Tamamla/ }).click();
  await expect(page.getByText('Sınav Belgeleri ve Başvuru Durumu')).toBeVisible();
  await logout(page);

  await login(page, '10000000146');
  await page.getByRole('button', { name: 'İncele' }).click();
  await page.getByRole('button', { name: 'Değerlendirmeye Gönder' }).click();
  await logout(page);

  for (let index = 0; index < juries.length; index++) {
    await login(page, juries[index]);
    await page.getByRole('button', { name: 'Değerlendir' }).click();
    await page.getByPlaceholder('Puanı Girin').fill(String(80 + index * 5));
    await page.getByRole('button', { name: 'Puanı Onayla ve Kilitle' }).click();
    await logout(page);
  }

  await login(page, '10000000146');
  await page.getByRole('button', { name: 'Kontenjan ve Yerleştirme' }).click();
  const departmentRow = page.locator('tbody tr').filter({ hasText: 'Beden Eğitimi ve Spor Öğretmenliği' });
  await departmentRow.locator('input[type=number]').nth(0).fill('1');
  await departmentRow.getByRole('button', { name: 'Ayarları Kaydet' }).click();
  page.once('dialog', (dialog) => dialog.accept());
  await departmentRow.getByRole('button', { name: 'Sonuçları Yayımla' }).click();
  await expect(page.getByText(/#1 Demo Aday/)).toBeVisible();
  await logout(page);

  await login(page, applicant);
  await expect(page.getByText('Asıl Olarak Yerleştiniz')).toBeVisible();
  await expect(page.getByText(/Bölüm sıralamanız: 1/)).toBeVisible();
});
