package com.clinicapp.backend.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.io.IOException;

import org.springframework.stereotype.Component;

import com.clinicapp.backend.model.core.Prescription;
import com.clinicapp.backend.model.core.HospitalInfo;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

@Component
public class PdfGenerator {

    private static final Font TITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.DARK_GRAY);
    private static final Font HEADER_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.BLACK);
    private static final Font BODY_FONT = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.BLACK);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public ByteArrayInputStream generate(Prescription prescription, HospitalInfo hospital) {
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter writer = PdfWriter.getInstance(document, out);
            addHeaderFooter(writer);

            document.open();
            addDynamicHeader(document, hospital);
            addTitle(document);
            addPatientInfo(document, prescription);
            addMedicalContent(document, prescription);
            addSignature(document);

        } catch (DocumentException | IOException e) {
            throw new PdfGenerationException("Erreur lors de la génération du PDF", e);
        } finally {
            if (document.isOpen()) {
                document.close();
            }
        }

        return new ByteArrayInputStream(out.toByteArray());
    }

    private void addDynamicHeader(Document document, HospitalInfo hospital) throws DocumentException, IOException {
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);

        if (hospital != null && hospital.getLogoPath() != null) {
            Image logo = Image.getInstance(hospital.getLogoPath());
            logo.scaleToFit(80, 80);
            PdfPCell logoCell = new PdfPCell(logo, false);
            logoCell.setBorder(Rectangle.NO_BORDER);
            logoCell.setHorizontalAlignment(Element.ALIGN_LEFT);
            headerTable.addCell(logoCell);
        } else {
            PdfPCell emptyCell = new PdfPCell();
            emptyCell.setBorder(Rectangle.NO_BORDER);
            headerTable.addCell(emptyCell);
        }

        StringBuilder hospitalInfo = new StringBuilder();
        if (hospital != null) {
            if (hospital.getName() != null) hospitalInfo.append(hospital.getName()).append("\n");
            if (hospital.getAddress() != null) hospitalInfo.append(hospital.getAddress()).append("\n");
            if (hospital.getPhone() != null) hospitalInfo.append("Tel: ").append(hospital.getPhone()).append("\n");
            if (hospital.getEmail() != null) hospitalInfo.append("Email: ").append(hospital.getEmail());
        }
        String[] lines = hospitalInfo.toString().split("\n");
        PdfPCell infoCell = new PdfPCell();
        infoCell.setBorder(Rectangle.NO_BORDER);
        infoCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        for (String line : lines) {
            Paragraph linePara = new Paragraph(line, HEADER_FONT);
            linePara.setAlignment(Element.ALIGN_RIGHT);
            linePara.setSpacingBefore(3f);
            infoCell.addElement(linePara);
        }
        headerTable.addCell(infoCell);

        document.add(headerTable);
        document.add(Chunk.NEWLINE);
    }

    private void addTitle(Document document) throws DocumentException {
        Paragraph spacer = new Paragraph();
        spacer.setSpacingAfter(30f);
        document.add(spacer);
        Paragraph title = new Paragraph("ORDONNANCE MÉDICALE\n\n", TITLE_FONT);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
    }

    private void addPatientInfo(Document document, Prescription p) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingBefore(20f);

        addTableHeaderCell(table, "Patient");
        addTableCell(table, p.getPatient().getFirstName() + " " + p.getPatient().getLastName());
        addTableHeaderCell(table, "Date de naissance");
        addTableCell(table, p.getPatient().getDateOfBirth() != null ? p.getPatient().getDateOfBirth().toString() : "-");
        addTableHeaderCell(table, "Genre");
        addTableCell(table, p.getPatient().getGender() != null ? p.getPatient().getGender().toString() : "-");
        addTableHeaderCell(table, "Médecin prescripteur");
        addTableCell(table, p.getMedecin().getFirstName() + " " + p.getMedecin().getLastName());
        addTableHeaderCell(table, "Date de prescription");
        addTableCell(table, DATE_FORMATTER.format(p.getCreatedAt()));

        document.add(table);
        document.add(Chunk.NEWLINE);
    }

    private void addMedicalContent(Document document, Prescription p) throws DocumentException {
        addSectionTitle(document, "Diagnostic");
        addBulletList(document, p.getDiagnostic());

        addSectionTitle(document, "\nRecommandations thérapeutiques");
        addBulletList(document, p.getRecommandations());
    }

    private void addSignature(Document document) throws DocumentException {
        Paragraph signature = new Paragraph("\n\nSignature et cachet du médecin :", HEADER_FONT);
        signature.setAlignment(Element.ALIGN_RIGHT);
        document.add(signature);
    }

    private void addHeaderFooter(PdfWriter writer) {
        writer.setPageEvent(new PdfPageEventHelper() {
            public void onEndPage(PdfWriter writer, Document document) {
                try {
                    PdfPTable footer = new PdfPTable(1);
                    footer.setTotalWidth(500);
                    footer.getDefaultCell().setBorder(Rectangle.NO_BORDER);
                    footer.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);

                    Paragraph p = new Paragraph(
                            "Document médical confidentiel - Hôpital Packt",
                            FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8)
                    );

                    PdfPCell cell = new PdfPCell(p);
                    cell.setBorder(Rectangle.NO_BORDER);
                    footer.addCell(cell);

                    footer.writeSelectedRows(
                            0, -1,
                            document.leftMargin(),
                            document.bottomMargin() - 10,
                            writer.getDirectContent()
                    );
                } catch (Exception e) {
                    throw new PdfGenerationException("Erreur d'en-tête/pied de page", e);
                }
            }
        });
    }

    private void addTableHeaderCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, HEADER_FONT));
        cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
        cell.setPadding(5);
        table.addCell(cell);
    }

    private void addTableCell(PdfPTable table, String text) {
        table.addCell(new Phrase(text != null ? text : "-", BODY_FONT));
    }

    private void addSectionTitle(Document document, String title) throws DocumentException {
        Paragraph sectionTitle = new Paragraph(title, HEADER_FONT);
        sectionTitle.setSpacingBefore(10f);
        document.add(sectionTitle);
    }

    private void addBulletList(Document document, String content) throws DocumentException {
        if (content == null || content.trim().isEmpty()) return;
        com.itextpdf.text.List bulletList = new com.itextpdf.text.List(com.itextpdf.text.List.UNORDERED);
        Font listFont = new Font(Font.FontFamily.HELVETICA, 11, Font.NORMAL, BaseColor.BLACK);
        for (String line : content.split("\n")) {
            if (!line.trim().isEmpty()) {
                bulletList.add(new ListItem(line.trim(), listFont));
            }
        }
        document.add(bulletList);
    }

    public static class PdfGenerationException extends RuntimeException {
        public PdfGenerationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}