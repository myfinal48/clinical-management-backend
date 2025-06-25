package com.clinicapp.backend.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Component;

import com.clinicapp.backend.model.core.Prescription;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

@Component
public class PdfGenerator {

    private static final Font TITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.DARK_GRAY);
    private static final Font HEADER_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.BLACK);
    private static final Font BODY_FONT = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.BLACK);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public ByteArrayInputStream generate(Prescription prescription) {
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter writer = PdfWriter.getInstance(document, out);
            addHeaderFooter(writer);

            document.open();
            addTitle(document);
            addPatientInfo(document, prescription);
            addMedicalContent(document, prescription);
            addSignature(document);

        } catch (DocumentException e) {
            throw new PdfGenerationException("Erreur lors de la génération du PDF", e);
        } finally {
            if (document.isOpen()) {
                document.close();
            }
        }

        return new ByteArrayInputStream(out.toByteArray());
    }

    private void addTitle(Document document) throws DocumentException {
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
        addTableHeaderCell(table, "Médecin prescripteur");
        addTableCell(table, p.getMedecin().getFirstName() + " " + p.getMedecin().getLastName());
        addTableHeaderCell(table, "Date de prescription");
        addTableCell(table, DATE_FORMATTER.format(p.getCreatedAt()));

        document.add(table);
        document.add(Chunk.NEWLINE);
    }

    private void addMedicalContent(Document document, Prescription p) throws DocumentException {
        addSectionTitle(document, "Diagnostic");
        document.add(new Paragraph(p.getDiagnostic(), BODY_FONT));

        addSectionTitle(document, "\nRecommandations thérapeutiques");
        document.add(new Paragraph(p.getRecommandations(), BODY_FONT));
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

    public static class PdfGenerationException extends RuntimeException {
        public PdfGenerationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
