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
import io.minio.MinioClient;
import io.minio.GetObjectArgs;
import org.springframework.beans.factory.annotation.Value;
import com.clinicapp.backend.model.core.Invoice;

@Component
public class PdfGenerator {

    private static final Font TITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.DARK_GRAY);
    private static final Font HEADER_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.BLACK);
    private static final Font BODY_FONT = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.BLACK);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final MinioClient minioClient;
    
    @Value("${minio.bucket-name}")
    private String bucketName;

    public PdfGenerator(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    public ByteArrayInputStream generate(Prescription prescription, HospitalInfo hospital) {
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter writer = PdfWriter.getInstance(document, out);
            addHeaderFooter(writer);

            document.open();
            boolean hasContent = false;
            if (prescription != null) {
                addDynamicHeader(document, hospital);
                addTitle(document);
                if (prescription.getPatient() != null && prescription.getMedecin() != null) {
                    addPatientInfo(document, prescription);
                    hasContent = true;
                }
                if ((prescription.getDiagnostic() != null && !prescription.getDiagnostic().trim().isEmpty()) ||
                    (prescription.getRecommandations() != null && !prescription.getRecommandations().trim().isEmpty())) {
                    addMedicalContent(document, prescription);
                    hasContent = true;
                }
                addSignature(document);
            }
            if (!hasContent) {
                document.add(new Paragraph("No data to display.", TITLE_FONT));
            }
        } catch (DocumentException | IOException e) {
            throw new PdfGenerationException("Error generating PDF", e);
        } finally {
            if (document.isOpen()) {
                document.close();
            }
        }

        return new ByteArrayInputStream(out.toByteArray());
    }

    /**
     * Generates a professional invoice PDF
     */
    public ByteArrayInputStream generateInvoice(Invoice invoice, HospitalInfo hospital) {
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter writer = PdfWriter.getInstance(document, out);
            addInvoiceHeaderFooter(writer);

            document.open();
            addDynamicHeader(document, hospital);
            addInvoiceTitle(document, invoice);
            addInvoiceInfo(document, invoice);
            addInvoiceDetails(document, invoice);
            addInvoiceSummary(document, invoice);
            addInvoiceFooter(document);

        } catch (DocumentException | IOException e) {
            throw new PdfGenerationException("Erreur lors de la génération de la facture PDF", e);
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
            try {
                var logoStream = minioClient.getObject(
                    GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(hospital.getLogoPath())
                        .build()
                );
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                logoStream.transferTo(baos);
                byte[] logoBytes = baos.toByteArray();
                Image logo = Image.getInstance(logoBytes);
                logo.scaleToFit(80, 80);
                PdfPCell logoCell = new PdfPCell(logo, false);
                logoCell.setBorder(Rectangle.NO_BORDER);
                logoCell.setHorizontalAlignment(Element.ALIGN_LEFT);
                headerTable.addCell(logoCell);
                logoStream.close();
            } catch (Exception e) {
                System.err.println("Error loading logo from Minio: " + e.getMessage());
                PdfPCell emptyCell = new PdfPCell();
                emptyCell.setBorder(Rectangle.NO_BORDER);
                headerTable.addCell(emptyCell);
            }
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
        
        if (hospitalInfo.length() == 0) {
            hospitalInfo.append("Hospital information not available");
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
        if (p == null || p.getPatient() == null || p.getMedecin() == null) return;
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingBefore(20f);

        addTableHeaderCell(table, "INFORMATIONS PATIENT");
        addTableHeaderCell(table, "INFORMATIONS PRESCRIPTEUR");

        String patientFirst = p.getPatient().getFirstName();
        String patientLast = p.getPatient().getLastName();
        String patientName = ((patientFirst != null ? patientFirst : "") +
                ((patientFirst != null && patientLast != null && !patientFirst.isBlank() && !patientLast.isBlank()) ? " " : "") +
                (patientLast != null ? patientLast : "")).trim();
        if (patientName.isEmpty()) patientName = "N/A";

        String doctorFirst = p.getMedecin().getFirstName();
        String doctorLast = p.getMedecin().getLastName();
        String doctorName = ((doctorFirst != null ? doctorFirst : "") +
                ((doctorFirst != null && doctorLast != null && !doctorFirst.isBlank() && !doctorLast.isBlank()) ? " " : "") +
                (doctorLast != null ? doctorLast : "")).trim();
        if (doctorName.isEmpty()) doctorName = "N/A";

        addTableCell(table, "Nom: " + patientName);
        addTableCell(table, "Médecin: " + doctorName);

        addTableCell(table, "Date de naissance: " + (p.getPatient().getDateOfBirth() != null ? p.getPatient().getDateOfBirth().toString() : "-"));
        addTableCell(table, "Date de prescription: " + (p.getCreatedAt() != null ? DATE_FORMATTER.format(p.getCreatedAt()) : "-"));

        addTableCell(table, "Genre: " + (p.getPatient().getGender() != null ? p.getPatient().getGender().toString() : "-"));
        addTableCell(table, "Email: " + (p.getMedecin().getEmail() != null ? p.getMedecin().getEmail() : "-"));

        // Patient contact block (multi-line allowed)
        StringBuilder patientContact = new StringBuilder();
        if (p.getPatient().getAddress() != null) patientContact.append("Adresse: ").append(p.getPatient().getAddress());
        if (p.getPatient().getPhoneNumber() != null) patientContact.append(patientContact.length() > 0 ? "\n" : "").append("Téléphone: ").append(p.getPatient().getPhoneNumber());
        if (p.getPatient().getEmail() != null) patientContact.append(patientContact.length() > 0 ? "\n" : "").append("Email: ").append(p.getPatient().getEmail());
        addTableCell(table, patientContact.length() > 0 ? patientContact.toString() : "");

        addTableCell(table, "");

        document.add(table);
        document.add(Chunk.NEWLINE);
    }

    private void addMedicalContent(Document document, Prescription p) throws DocumentException {
        if (p == null) return;
        if (p.getDiagnostic() != null && !p.getDiagnostic().trim().isEmpty()) {
            addSectionTitle(document, "Diagnostic");
            addBulletList(document, p.getDiagnostic());
        }
        if (p.getRecommandations() != null && !p.getRecommandations().trim().isEmpty()) {
            addSectionTitle(document, "\nRecommandations thérapeutiques");
            addBulletList(document, p.getRecommandations());
        }
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
                    throw new PdfGenerationException("Error generating header/footer", e);
                }
            }
        });
    }

    private void addInvoiceHeaderFooter(PdfWriter writer) {
        writer.setPageEvent(new PdfPageEventHelper() {
            public void onEndPage(PdfWriter writer, Document document) {
                try {
                    PdfPTable footer = new PdfPTable(1);
                    footer.setTotalWidth(500);
                    footer.getDefaultCell().setBorder(Rectangle.NO_BORDER);
                    footer.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);

                    Paragraph p = new Paragraph(
                            "Document comptable - " + (System.currentTimeMillis() % 2 == 0 ? "Original" : "Copie"),
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
                    throw new PdfGenerationException("Erreur d'en-tête/pied de page de la facture", e);
                }
            }
        });
    }

    private void addInvoiceTitle(Document document, Invoice invoice) throws DocumentException {
        Paragraph spacer = new Paragraph();
        spacer.setSpacingAfter(20f);
        document.add(spacer);
        
        // Main title
        Paragraph title = new Paragraph("FACTURE", TITLE_FONT);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        
        // Invoice number
        Paragraph invoiceNumber = new Paragraph("N° " + invoice.getId(), HEADER_FONT);
        invoiceNumber.setAlignment(Element.ALIGN_CENTER);
        invoiceNumber.setSpacingAfter(30f);
        document.add(invoiceNumber);
    }

    private void addInvoiceInfo(Document document, Invoice invoice) throws DocumentException {
        PdfPTable infoTable = new PdfPTable(2);
        infoTable.setWidthPercentage(100);
        infoTable.setSpacingBefore(20f);
        infoTable.setSpacingAfter(20f);

        // Patient information
        addTableHeaderCell(infoTable, "INFORMATIONS PATIENT");
        addTableHeaderCell(infoTable, "INFORMATIONS FACTURE");
        
        // Patient
        if (invoice.getPatient() != null) {
            String firstName = invoice.getPatient().getFirstName();
            String lastName = invoice.getPatient().getLastName();
            String fullName;
            if ((firstName == null || firstName.trim().isEmpty()) && (lastName == null || lastName.trim().isEmpty())) {
                fullName = "N/A";
            } else {
                String safeFirst = firstName != null ? firstName : "";
                String safeLast = lastName != null ? lastName : "";
                String space = (!safeFirst.isEmpty() && !safeLast.isEmpty()) ? " " : "";
                fullName = safeFirst + space + safeLast;
            }
            addTableCell(infoTable, "Nom: " + fullName);
            String issuedAtText = invoice.getIssuedAt() != null ? DATE_FORMATTER.format(invoice.getIssuedAt()) : "-";
            addTableCell(infoTable, "Date d'émission: " + issuedAtText);
            
            if (invoice.getPatient().getAddress() != null) {
                addTableCell(infoTable, "Adresse: " + invoice.getPatient().getAddress());
            }
            if (invoice.getPatient().getPhoneNumber() != null) {
                addTableCell(infoTable, "Téléphone: " + invoice.getPatient().getPhoneNumber());
            }
            if (invoice.getPatient().getEmail() != null) {
                addTableCell(infoTable, "Email: " + invoice.getPatient().getEmail());
            }
            
            addTableCell(infoTable, "Statut: " + (invoice.isPaid() ? "PAYÉE" : "EN ATTENTE"));
            if (invoice.isPaid() && invoice.getDatePaid() != null) {
                addTableCell(infoTable, "Date de paiement: " + DATE_FORMATTER.format(invoice.getDatePaid()));
            }
        }

        document.add(infoTable);
    }

    private void addInvoiceDetails(Document document, Invoice invoice) throws DocumentException {
        if (invoice.getDescription() != null && !invoice.getDescription().trim().isEmpty()) {
            addSectionTitle(document, "DÉTAIL DES SERVICES");
            
            PdfPTable detailsTable = new PdfPTable(2);
            detailsTable.setWidthPercentage(100);
            detailsTable.setSpacingBefore(10f);
            detailsTable.setSpacingAfter(20f);

            addTableHeaderCell(detailsTable, "Description");
            addTableHeaderCell(detailsTable, "Montant");

            // Split description into lines if it contains line breaks
            String[] lines = invoice.getDescription().split("\n");
            for (String line : lines) {
                if (!line.trim().isEmpty()) {
                    addTableCell(detailsTable, line.trim());
                    addTableCell(detailsTable, "");
                }
            }

            document.add(detailsTable);
        }
    }

    private void addInvoiceSummary(Document document, Invoice invoice) throws DocumentException {
        PdfPTable summaryTable = new PdfPTable(2);
        summaryTable.setWidthPercentage(60);
        summaryTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
        summaryTable.setSpacingBefore(20f);
        summaryTable.setSpacingAfter(20f);

        // Amount including tax (for now, we consider the amount includes tax)
        addTableHeaderCell(summaryTable, "Montant TTC");
        addTableCell(summaryTable, String.format("%.2f Fcfa", invoice.getAmount()));

        document.add(summaryTable);
    }

    private void addInvoiceFooter(Document document) throws DocumentException {
        Paragraph footer = new Paragraph();
        footer.setSpacingBefore(40f);
        
        // Payment terms
        Paragraph conditions = new Paragraph("Conditions de paiement:", HEADER_FONT);
        conditions.setSpacingAfter(10f);
        footer.add(conditions);
        
        Paragraph conditionsText = new Paragraph(
            "• Paiement à réception de facture\n" +
            "• Délai de paiement : 30 jours\n" +
            "• En cas de retard de paiement, des pénalités pourront être appliquées\n" +
            "• Pour toute question, contactez notre service comptabilité", 
            BODY_FONT
        );
        footer.add(conditionsText);
        
        // Signature
        Paragraph signature = new Paragraph("\n\nSignature et cachet de l'établissement :", HEADER_FONT);
        signature.setAlignment(Element.ALIGN_RIGHT);
        footer.add(signature);
        
        document.add(footer);
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