package tr.edu.inonu.oys.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;
import tr.edu.inonu.oys.model.Application;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.text.DecimalFormat;
import java.time.LocalDate;

@Service
public class PdfGeneratorService {

    private static final Color INONU_BLUE = new Color(10, 77, 104);
    private static final Color INONU_YELLOW = new Color(242, 184, 7);
    private static final Color DARK_TEXT = new Color(15, 23, 42);
    private static final Color LIGHT_BG = new Color(248, 250, 252);
    private static final Color BORDER = new Color(203, 213, 225);

    public ByteArrayInputStream generateExamDocument(Application application) {
        Document document = new Document(PageSize.A4, 32, 32, 24, 24);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            BaseFont baseFont = createBaseFont();
            Font titleFont = new Font(baseFont, 16, Font.BOLD, INONU_BLUE);
            Font bandFont = new Font(baseFont, 12, Font.BOLD, Color.WHITE);
            Font sectionFont = new Font(baseFont, 10, Font.BOLD, Color.WHITE);
            Font bodyBold = new Font(baseFont, 9, Font.BOLD, DARK_TEXT);
            Font bodyNormal = new Font(baseFont, 9, Font.NORMAL, DARK_TEXT);
            Font smallFont = new Font(baseFont, 8, Font.NORMAL, DARK_TEXT);

            addExamHeader(document, titleFont, bandFont, bodyBold, smallFont, application);
            addExamInfo(document, sectionFont, bodyBold, bodyNormal, smallFont, application);
            addApprovalBlock(document, bodyBold, smallFont);
            addRulesBottomGap(document);
            addRules(document, sectionFont, bodyNormal, "SINAVDA UYULMASI GEREKEN KURALLAR", getExamRules());

            document.close();
        } catch (Exception e) {
            throw new RuntimeException("Sinav giris belgesi olusturulamadi.", e);
        }

        return new ByteArrayInputStream(out.toByteArray());
    }

    public ByteArrayInputStream generateResultDocument(Application application) {
        Document document = new Document(PageSize.A4, 36, 36, 36, 36);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            BaseFont baseFont = createBaseFont();
            Font titleFont = new Font(baseFont, 16, Font.BOLD, INONU_BLUE);
            Font headerFont = new Font(baseFont, 11, Font.BOLD, Color.WHITE);
            Font bodyBold = new Font(baseFont, 10, Font.BOLD, DARK_TEXT);
            Font bodyNormal = new Font(baseFont, 10, Font.NORMAL, DARK_TEXT);
            Font scoreFont = new Font(baseFont, 24, Font.BOLD, INONU_BLUE);

            addDocumentHeader(document, titleFont, "INONU UNIVERSITESI\nOZEL YETENEK SINAVI SONUC BELGESI");
            addApplicantInfo(document, bodyBold, bodyNormal, application, qrContent(application));
            document.add(new Paragraph(" "));
            addScoreInfo(document, headerFont, bodyNormal, scoreFont, application);
            document.add(new Paragraph(" "));
            addRules(document, headerFont, bodyNormal, "SONUCLAR HAKKINDA BILGILENDIRME", getResultInfo());

            document.close();
        } catch (Exception e) {
            throw new RuntimeException("Sinav sonuc belgesi olusturulamadi.", e);
        }

        return new ByteArrayInputStream(out.toByteArray());
    }

    private void addExamHeader(Document document, Font titleFont, Font bandFont, Font bodyBold, Font smallFont,
                               Application application) throws DocumentException {
        PdfPTable header = new PdfPTable(1);
        header.setWidthPercentage(100);

        PdfPCell band = new PdfPCell(new Phrase("INONU UNIVERSITESI REKTORLUGU", bandFont));
        band.setHorizontalAlignment(Element.ALIGN_CENTER);
        band.setBackgroundColor(INONU_BLUE);
        band.setPadding(6);
        band.setBorder(Rectangle.NO_BORDER);
        header.addCell(band);

        PdfPCell title = new PdfPCell();
        title.setPadding(7);
        title.setBorder(Rectangle.BOX);
        title.setBorderColor(INONU_BLUE);
        title.setBorderWidth(1.5f);
        Paragraph unit = new Paragraph("Ozel Yetenek Sinavi Koordinatorlugu", smallFont);
        unit.setAlignment(Element.ALIGN_CENTER);
        title.addElement(unit);
        Paragraph titleText = new Paragraph("OZEL YETENEK SINAVI GIRIS BELGESI", titleFont);
        titleText.setAlignment(Element.ALIGN_CENTER);
        title.addElement(titleText);
        Paragraph subtitle = new Paragraph("Bu belge yalnizca ilan edilen sinav tarihi, salonu ve adayi icin gecerlidir.", smallFont);
        subtitle.setAlignment(Element.ALIGN_CENTER);
        title.addElement(subtitle);
        header.addCell(title);

        document.add(header);

        PdfPTable meta = new PdfPTable(3);
        meta.setWidthPercentage(100);
        meta.setWidths(new float[]{30, 30, 40});
        meta.addCell(metaCell("Belge No", "#OYS-" + application.getId(), bodyBold));
        meta.addCell(metaCell("Duzenleme Tarihi", String.valueOf(LocalDate.now()), bodyBold));
        meta.addCell(metaCell("Belge Turu", "Sinav Giris Belgesi", bodyBold));
        document.add(meta);
        document.add(spacer(6));
    }

    private void addExamInfo(Document document, Font sectionFont, Font bodyBold, Font bodyNormal, Font smallFont,
                             Application application) throws Exception {
        PdfPTable layout = new PdfPTable(2);
        layout.setWidthPercentage(100);
        layout.setWidths(new float[]{72, 28});

        PdfPCell left = new PdfPCell();
        left.setBorder(Rectangle.NO_BORDER);
        left.setPadding(0);
        left.addElement(sectionTable("ADAY BILGILERI", sectionFont,
                row("T.C. Kimlik No", applicantUsername(application)),
                row("Adi Soyadi", applicantFullName(application)),
                row("Basvuru No", "#OYS-" + application.getId()),
                row("Basvurulan Bolum", departmentName(application))
        ));
        left.addElement(spacer(5));
        left.addElement(sectionTable("SINAV BILGILERI", sectionFont,
                row("Sinav Programi", examScheduleText(application)),
                row("Sinav Tipi", examTypeText(application)),
                row("Tarih", examDateText(application)),
                row("Saat", examTimeTextSafe(application)),
                row("Bina / Yer", examLocationText(application)),
                row("Salon / Oda", examRoomText(application)),
                row("Aday Sirasi", application.getExamOrder() != null ? String.valueOf(application.getExamOrder()) : "-")
        ));
        layout.addCell(left);

        PdfPCell right = new PdfPCell();
        right.setPadding(7);
        right.setBorder(Rectangle.BOX);
        right.setBorderColor(BORDER);
        right.setBackgroundColor(LIGHT_BG);
        Paragraph photoTitle = new Paragraph("ADAY FOTOGRAFI", bodyBold);
        photoTitle.setAlignment(Element.ALIGN_CENTER);
        right.addElement(photoTitle);
        right.addElement(photoBox(smallFont));
        right.addElement(spacer(4));
        Paragraph qrTitle = new Paragraph("KAREKOD DOGRULAMA", bodyBold);
        qrTitle.setAlignment(Element.ALIGN_CENTER);
        right.addElement(qrTitle);
        Image qrImage = Image.getInstance(generateQRCodeImage(qrContent(application), 128, 128));
        qrImage.setAlignment(Image.ALIGN_CENTER);
        right.addElement(qrImage);
        Paragraph qrText = new Paragraph(qrContent(application), smallFont);
        qrText.setAlignment(Element.ALIGN_CENTER);
        right.addElement(qrText);
        right.addElement(spacer(4));
        right.addElement(controlBox(smallFont));
        layout.addCell(right);

        document.add(layout);
    }

    private PdfPTable photoBox(Font smallFont) {
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);
        PdfPCell cell = new PdfPCell(new Phrase("\n\n3x4\nFotograf\n\n", smallFont));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setBorder(Rectangle.BOX);
        cell.setBorderColor(BORDER);
        cell.setFixedHeight(72);
        table.addCell(cell);
        return table;
    }

    private PdfPTable controlBox(Font smallFont) {
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);
        PdfPCell cell = new PdfPCell(new Phrase("\nKontrol Eden / Imza\n", smallFont));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setBorder(Rectangle.BOX);
        cell.setBorderColor(BORDER);
        cell.setFixedHeight(42);
        table.addCell(cell);
        return table;
    }

    private PdfPTable sectionTable(String title, Font sectionFont, String[]... rows) throws DocumentException {
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);

        PdfPCell header = new PdfPCell(new Phrase(title, sectionFont));
        header.setBackgroundColor(INONU_BLUE);
        header.setPadding(5);
        header.setBorder(Rectangle.NO_BORDER);
        table.addCell(header);

        PdfPCell body = new PdfPCell();
        body.setPadding(0);
        body.setBorder(Rectangle.BOX);
        body.setBorderColor(BORDER);
        PdfPTable rowTable = new PdfPTable(2);
        rowTable.setWidthPercentage(100);
        rowTable.setWidths(new float[]{32, 68});
        for (String[] row : rows) {
            rowTable.addCell(labelCell(row[0]));
            rowTable.addCell(valueCell(row[1]));
        }
        body.addElement(rowTable);
        table.addCell(body);
        return table;
    }

    private PdfPCell labelCell(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, new Font(Font.HELVETICA, 9, Font.BOLD, DARK_TEXT)));
        cell.setPadding(4);
        cell.setBackgroundColor(LIGHT_BG);
        cell.setBorderColor(BORDER);
        return cell;
    }

    private PdfPCell valueCell(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(nullSafe(text), new Font(Font.HELVETICA, 9, Font.NORMAL, DARK_TEXT)));
        cell.setPadding(4);
        cell.setBorderColor(BORDER);
        return cell;
    }

    private PdfPCell metaCell(String label, String value, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(label + ": " + value, font));
        cell.setPadding(6);
        cell.setBorder(Rectangle.BOX);
        cell.setBorderColor(BORDER);
        cell.setBackgroundColor(LIGHT_BG);
        return cell;
    }

    private Paragraph spacer(float leading) {
        Paragraph paragraph = new Paragraph(" ");
        paragraph.setLeading(leading);
        return paragraph;
    }

    private String[] row(String label, String value) {
        return new String[]{label, value};
    }

    private void addDocumentHeader(Document document, Font titleFont, String title) throws DocumentException {
        PdfPTable headerTable = new PdfPTable(1);
        headerTable.setWidthPercentage(100);
        PdfPCell titleCell = new PdfPCell(new Phrase(title, titleFont));
        titleCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        titleCell.setPadding(12);
        titleCell.setBorder(Rectangle.BOTTOM);
        titleCell.setBorderColor(INONU_YELLOW);
        titleCell.setBorderWidth(3f);
        headerTable.addCell(titleCell);
        document.add(headerTable);
        document.add(spacer(5));
    }

    private void addApplicantInfo(Document document, Font bodyBold, Font bodyNormal, Application application, String qrContent) throws Exception {
        PdfPTable infoTable = new PdfPTable(2);
        infoTable.setWidthPercentage(100);
        infoTable.setWidths(new float[]{70, 30});

        PdfPCell infoCell = new PdfPCell();
        infoCell.setBorder(Rectangle.NO_BORDER);
        infoCell.addElement(new Paragraph("T.C. Kimlik No: " + applicantUsername(application), bodyBold));
        infoCell.addElement(new Paragraph("Adi Soyadi: " + applicantFullName(application), bodyNormal));
        infoCell.addElement(new Paragraph("Basvurulan Bolum: " + departmentName(application), bodyNormal));
        infoCell.addElement(new Paragraph("Basvuru No: #OYS-" + application.getId(), bodyNormal));
        infoTable.addCell(infoCell);

        Image qrImage = Image.getInstance(generateQRCodeImage(qrContent, 120, 120));
        PdfPCell qrCell = new PdfPCell(qrImage);
        qrCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        qrCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        qrCell.setBorder(Rectangle.NO_BORDER);
        infoTable.addCell(qrCell);
        document.add(infoTable);
    }

    private void addScoreInfo(Document document, Font headerFont, Font bodyNormal, Font scoreFont, Application application) throws DocumentException {
        PdfPTable scoreTable = new PdfPTable(1);
        scoreTable.setWidthPercentage(100);

        PdfPCell scoreHeader = new PdfPCell(new Phrase("OYSP DEGERLENDIRMESI", headerFont));
        scoreHeader.setBackgroundColor(INONU_BLUE);
        scoreHeader.setPadding(6);
        scoreTable.addCell(scoreHeader);

        DecimalFormat df = new DecimalFormat("#.###");
        String oyspScore = application.getOyspScore() != null ? df.format(application.getOyspScore()) : "HESAPLANAMADI";

        PdfPCell scoreCell = new PdfPCell();
        scoreCell.setPadding(20);
        scoreCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        scoreCell.setBackgroundColor(LIGHT_BG);

        Paragraph scoreP = new Paragraph(oyspScore, scoreFont);
        scoreP.setAlignment(Element.ALIGN_CENTER);
        scoreCell.addElement(scoreP);

        Paragraph textP = new Paragraph("OYSP Yerlestirme Puaniniz", bodyNormal);
        textP.setAlignment(Element.ALIGN_CENTER);
        scoreCell.addElement(textP);

        scoreTable.addCell(scoreCell);
        document.add(scoreTable);
    }

    private void addRules(Document document, Font headerFont, Font bodyNormal, String title, String rulesText) throws DocumentException {
        PdfPTable rulesTable = new PdfPTable(1);
        rulesTable.setWidthPercentage(100);

        PdfPCell rulesHeader = new PdfPCell(new Phrase(title, headerFont));
        rulesHeader.setBackgroundColor(INONU_BLUE);
        rulesHeader.setPadding(6);
        rulesHeader.setBorder(Rectangle.NO_BORDER);
        rulesTable.addCell(rulesHeader);

        PdfPCell rulesBody = new PdfPCell(new Phrase(rulesText, bodyNormal));
        rulesBody.setPadding(10);
        rulesBody.setBackgroundColor(LIGHT_BG);
        rulesBody.setBorderColor(BORDER);
        rulesTable.addCell(rulesBody);
        document.add(rulesTable);
    }

    private void addApprovalBlock(Document document, Font bodyBold, Font smallFont) throws DocumentException {
        document.add(spacer(4));
        PdfPTable footer = new PdfPTable(2);
        footer.setWidthPercentage(100);
        footer.setWidths(new float[]{50, 50});

        PdfPCell note = new PdfPCell();
        note.setPadding(6);
        note.setBorderColor(BORDER);
        note.addElement(new Paragraph("ACIKLAMA", bodyBold));
        note.addElement(new Paragraph("Belgede yer alan tarih, saat, salon ve aday sirasi esas alinir. Bilgiler sistem kayitlari ile eslestirilir.", smallFont));
        footer.addCell(note);

        PdfPCell sign = new PdfPCell(new Phrase("Ozel Yetenek Sinavi Koordinatorlugu\n\nYetkili Imza / Muhur", smallFont));
        sign.setHorizontalAlignment(Element.ALIGN_CENTER);
        sign.setPadding(6);
        sign.setBorderColor(BORDER);
        footer.addCell(sign);
        document.add(footer);
    }

    private void addRulesBottomGap(Document document) throws DocumentException {
        document.add(spacer(34));
    }

    private String getExamRules() {
        return "1. Adaylar sinav saatinden en az 30 dakika once salonda hazir bulunmalidir.\n" +
               "2. Bu belge ile birlikte resmi kimlik belgesi ibraz edilmelidir.\n" +
               "3. Sinav salonuna elektronik cihazlarla girilemez.\n" +
               "4. Adaylar ilan edilen salon, tarih ve saat bilgisinden sorumludur.";
    }

    private String getResultInfo() {
        return "1. Bu belge, Ozel Yetenek Sinavi sonucunda elde edilen OYSP puanini gosterir.\n" +
               "2. Yerlestirme islemleri ilgili kilavuzdaki kontenjan ve puan hesaplama yontemlerine gore yapilir.\n" +
               "3. Resmi itirazlar ve duyurular icin Inonu Universitesi duyurulari takip edilmelidir.";
    }

    private String qrContent(Application application) {
        return "INONU-OYS-VERIFY-" + application.getId() + "-" + applicantUsername(application);
    }

    private byte[] generateQRCodeImage(String text, int width, int height) throws Exception {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);
        ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
        return pngOutputStream.toByteArray();
    }

    private BaseFont createBaseFont() throws Exception {
        return BaseFont.createFont("Helvetica", "Cp1254", BaseFont.NOT_EMBEDDED);
    }

    private String applicantUsername(Application application) {
        return application.getApplicant() != null ? application.getApplicant().getUsername() : "-";
    }

    private String applicantFullName(Application application) {
        return application.getApplicant() != null
                ? application.getApplicant().getFirstName() + " " + application.getApplicant().getLastName()
                : "-";
    }

    private String departmentName(Application application) {
        return application.getProgramName() != null ? application.getProgramName() : "-";
    }

    private String examScheduleText(Application application) {
        return application.getExamSession() != null && application.getExamSession().isPublished()
                ? "Yayimlandi" : "Henuz yayimlanmadi";
    }

    private String examDateText(Application application) {
        return application.getExamSession() != null && application.getExamSession().isPublished()
                ? String.valueOf(application.getExamSession().getExamDate()) : "-";
    }

    private String examTimeTextSafe(Application application) {
        return application.getExamSession() != null && application.getExamSession().isPublished()
                ? examTimeText(application) : "-";
    }

    private String examLocationText(Application application) {
        return application.getExamSession() != null && application.getExamSession().isPublished()
                ? nullSafe(application.getExamSession().getLocation()) : "-";
    }

    private String examRoomText(Application application) {
        return application.getExamSession() != null && application.getExamSession().isPublished()
                ? nullSafe(application.getExamSession().getRoom()) : "-";
    }

    private String examTimeText(Application application) {
        if (application.getAppointmentStartTime() != null) {
            return application.getAppointmentStartTime()
                    + (application.getAppointmentEndTime() != null ? " - " + application.getAppointmentEndTime() : "");
        }
        return application.getExamSession().getStartTime()
                + (application.getExamSession().getEndTime() != null ? " - " + application.getExamSession().getEndTime() : "");
    }

    private String examTypeText(Application application) {
        if (application.getExamSession() == null || application.getExamSession().getSessionType() == null) {
            return "-";
        }
        return switch (application.getExamSession().getSessionType()) {
            case GROUP -> "Toplu sinav";
            case INDIVIDUAL -> "Bireysel sinav";
        };
    }

    private String nullSafe(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }
}
