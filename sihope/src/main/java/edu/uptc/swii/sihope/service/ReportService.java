package edu.uptc.swii.sihope.service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import org.springframework.core.io.ClassPathResource;

import edu.uptc.swii.sihope.domain.Cita;
import edu.uptc.swii.sihope.dto.response.CitasReportResponse;
import edu.uptc.swii.sihope.dto.response.CitasReportResponse.CitaDetailRow;
import edu.uptc.swii.sihope.dto.response.CitasReportResponse.ReportRow;
import edu.uptc.swii.sihope.repository.CitaRepository;

@Service
public class ReportService {

    private final CitaRepository citaRepository;

    public ReportService(CitaRepository citaRepository) {
        this.citaRepository = citaRepository;
    }

    @Transactional(readOnly = true)
    public CitasReportResponse citasAtendidas(LocalDate from, LocalDate to) {
        return citasAtendidas(from, to, null);
    }

    @Transactional(readOnly = true)
    public CitasReportResponse citasAtendidas(LocalDate from, LocalDate to, Integer monitorId) {
        List<Cita> citas = monitorId == null
                ? citaRepository.findByStatusAndDateBetweenOrderByDateAscStartTimeAsc(
                        Cita.ATENDIDA, from, to)
                : citaRepository.findByStatusAndMonitorIdAndDateBetweenOrderByDateAscStartTimeAsc(
                        Cita.ATENDIDA, monitorId, from, to);

        Map<String, Long> byMonitor = new LinkedHashMap<>();
        Map<String, Long> bySubject = new LinkedHashMap<>();
        for (Cita c : citas) {
            byMonitor.merge(monitorName(c), 1L, Long::sum);
            bySubject.merge(subjectName(c), 1L, Long::sum);
        }

        List<ReportRow> monitorRows = byMonitor.entrySet().stream()
                .map(e -> new ReportRow(e.getKey(), e.getValue())).toList();
        List<ReportRow> subjectRows = bySubject.entrySet().stream()
                .map(e -> new ReportRow(e.getKey(), e.getValue())).toList();

        // El listado detallado de citas solo se incluye cuando se filtra por un monitor.
        List<CitaDetailRow> details = monitorId == null
                ? null
                : citas.stream().map(this::detailRow).toList();

        String message = citas.isEmpty()
                ? "No hay citas atendidas en el periodo seleccionado."
                : null;

        return new CitasReportResponse(from.toString(), to.toString(), citas.size(),
                monitorRows, subjectRows, details, message);
    }

    public byte[] exportExcel(LocalDate from, LocalDate to) {
        return exportExcel(from, to, null);
    }

    public byte[] exportExcel(LocalDate from, LocalDate to, Integer monitorId) {
        CitasReportResponse report = citasAtendidas(from, to, monitorId);
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            CellStyle header = wb.createCellStyle();
            org.apache.poi.ss.usermodel.Font bold = wb.createFont();
            bold.setBold(true);
            header.setFont(bold);

            Sheet resumen = wb.createSheet("Resumen");
            writeRow(resumen, 0, header, "Reporte de citas atendidas");
            writeRow(resumen, 1, null, "Periodo", report.from() + " a " + report.to());
            writeRow(resumen, 2, null, "Total de citas atendidas", String.valueOf(report.total()));
            if (report.message() != null) {
                writeRow(resumen, 3, null, "Nota", report.message());
            }

            Sheet porMonitor = wb.createSheet("Por monitor");
            writeRow(porMonitor, 0, header, "Monitor", "Citas atendidas");
            fillRows(porMonitor, header, report.byMonitor());

            Sheet porTema = wb.createSheet("Por tema");
            writeRow(porTema, 0, header, "Asignatura", "Citas atendidas");
            fillRows(porTema, header, report.bySubject());

            autosize(resumen, 2);
            autosize(porMonitor, 2);
            autosize(porTema, 2);

            if (report.details() != null && !report.details().isEmpty()) {
                Sheet detalle = wb.createSheet("Detalle de citas atendidas");
                writeRow(detalle, 0, header, "Estudiante", "Asignatura", "Fecha", "Hora inicio", "Hora fin");
                int i = 1;
                for (CitaDetailRow d : report.details()) {
                    writeRow(detalle, i++, null, d.student(), d.subject(), d.date(),
                            d.startTime(), d.endTime());
                }
                autosize(detalle, 5);
            }

            wb.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo generar el Excel del reporte.", e);
        }
    }

    public byte[] exportPdf(LocalDate from, LocalDate to) {
        return exportPdf(from, to, null);
    }

    public byte[] exportPdf(LocalDate from, LocalDate to, Integer monitorId) {
        CitasReportResponse report = citasAtendidas(from, to, monitorId);
        Document doc = new Document();
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfWriter.getInstance(doc, out);
            doc.open();

            Font title = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Font normal = FontFactory.getFont(FontFactory.HELVETICA, 11);

            addLetterhead(doc, report, title, normal);
            doc.add(new Paragraph(" ", normal));

            if (report.message() != null) {
                doc.add(new Paragraph(report.message(), normal));
            } else {
                doc.add(pdfTable("Por monitor", "Monitor", report.byMonitor()));
                doc.add(new Paragraph(" ", normal));
                doc.add(pdfTable("Por tema", "Asignatura", report.bySubject()));

                if (report.details() != null && !report.details().isEmpty()) {
                    doc.add(new Paragraph(" ", normal));
                    doc.add(pdfDetailTable(report.details()));
                }
            }

            doc.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo generar el PDF del reporte.", e);
        }
    }

    private void addLetterhead(Document doc, CitasReportResponse report, Font title, Font normal)
            throws Exception {
        Image logo = loadLogo();
        Paragraph titlePar = new Paragraph("Reporte de citas atendidas", title);
        Paragraph periodo = new Paragraph("Periodo: " + report.from() + " a " + report.to(), normal);
        Paragraph total = new Paragraph("Total de citas atendidas: " + report.total(), normal);

        if (logo == null) {
            doc.add(titlePar);
            doc.add(periodo);
            doc.add(total);
            return;
        }

        PdfPTable header = new PdfPTable(new float[]{1.2f, 5f});
        header.setWidthPercentage(100);

        PdfPCell logoCell = new PdfPCell(logo, true);
        logoCell.setBorder(com.lowagie.text.Rectangle.NO_BORDER);
        logoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        logoCell.setPaddingRight(10);
        header.addCell(logoCell);

        PdfPCell textCell = new PdfPCell();
        textCell.setBorder(com.lowagie.text.Rectangle.NO_BORDER);
        textCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        textCell.addElement(titlePar);
        textCell.addElement(periodo);
        textCell.addElement(total);
        header.addCell(textCell);

        doc.add(header);
    }

    private Image loadLogo() {
        try {
            ClassPathResource resource = new ClassPathResource("static/img/logo-sihope-fondoblanco.jpg");
            if (!resource.exists()) {
                return null;
            }
            try (java.io.InputStream in = resource.getInputStream()) {
                Image image = Image.getInstance(in.readAllBytes());
                image.scaleToFit(70, 70);
                return image;
            }
        } catch (Exception e) {
            return null;
        }
    }

    private PdfPTable pdfTable(String heading, String firstColumn, List<ReportRow> rows) {
        Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
        Font headFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
        Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 11);

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingBefore(4);

        PdfPCell section = new PdfPCell(new Phrase(heading, sectionFont));
        section.setColspan(2);
        section.setBackgroundColor(new java.awt.Color(245, 245, 245));
        table.addCell(section);

        table.addCell(new PdfPCell(new Phrase(firstColumn, headFont)));
        PdfPCell totalHead = new PdfPCell(new Phrase("Citas atendidas", headFont));
        totalHead.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(totalHead);

        for (ReportRow r : rows) {
            table.addCell(new PdfPCell(new Phrase(r.name(), cellFont)));
            PdfPCell total = new PdfPCell(new Phrase(String.valueOf(r.total()), cellFont));
            total.setHorizontalAlignment(Element.ALIGN_RIGHT);
            table.addCell(total);
        }
        return table;
    }

    private PdfPTable pdfDetailTable(List<CitaDetailRow> rows) {
        Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
        Font headFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
        Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 11);

        PdfPTable table = new PdfPTable(new float[]{3f, 3f, 2f, 2f});
        table.setWidthPercentage(100);
        table.setSpacingBefore(4);

        PdfPCell section = new PdfPCell(new Phrase("Detalle de citas atendidas", sectionFont));
        section.setColspan(4);
        section.setBackgroundColor(new java.awt.Color(245, 245, 245));
        table.addCell(section);

        table.addCell(new PdfPCell(new Phrase("Estudiante", headFont)));
        table.addCell(new PdfPCell(new Phrase("Asignatura", headFont)));
        table.addCell(new PdfPCell(new Phrase("Fecha", headFont)));
        table.addCell(new PdfPCell(new Phrase("Hora", headFont)));

        for (CitaDetailRow r : rows) {
            table.addCell(new PdfPCell(new Phrase(nullSafe(r.student()), cellFont)));
            table.addCell(new PdfPCell(new Phrase(nullSafe(r.subject()), cellFont)));
            table.addCell(new PdfPCell(new Phrase(nullSafe(r.date()), cellFont)));
            table.addCell(new PdfPCell(new Phrase(timeRange(r), cellFont)));
        }
        return table;
    }

    private void fillRows(Sheet sheet, CellStyle style, List<ReportRow> rows) {
        int i = 1;
        for (ReportRow r : rows) {
            writeRow(sheet, i++, null, r.name(), String.valueOf(r.total()));
        }
    }

    private void writeRow(Sheet sheet, int index, CellStyle style, String... values) {
        Row row = sheet.createRow(index);
        for (int c = 0; c < values.length; c++) {
            Cell cell = row.createCell(c);
            cell.setCellValue(values[c]);
            if (style != null) {
                cell.setCellStyle(style);
            }
        }
    }

    private void autosize(Sheet sheet, int columns) {
        for (int c = 0; c < columns; c++) {
            sheet.autoSizeColumn(c);
        }
    }

    private CitaDetailRow detailRow(Cita c) {
        return new CitaDetailRow(
                c.getStudent() == null ? "-" : fullName(c.getStudent().getFirstName(),
                        c.getStudent().getLastName()),
                subjectName(c),
                c.getDate() == null ? "" : c.getDate().toString(),
                c.getStartTime() == null ? "" : c.getStartTime().toString(),
                c.getEndTime() == null ? "" : c.getEndTime().toString());
    }

    private String timeRange(CitaDetailRow r) {
        String start = nullSafe(r.startTime());
        String end = nullSafe(r.endTime());
        if (start.isEmpty() && end.isEmpty()) {
            return "";
        }
        return start + " - " + end;
    }

    private String nullSafe(String value) {
        return value == null ? "" : value;
    }

    private String monitorName(Cita c) {
        return c.getMonitor() == null ? "-" : fullName(c.getMonitor().getFirstName(),
                c.getMonitor().getLastName());
    }

    private String subjectName(Cita c) {
        return c.getSubject() == null ? "-" : c.getSubject().getName();
    }

    private String fullName(String first, String last) {
        return ((first == null ? "" : first) + " " + (last == null ? "" : last)).trim();
    }
}
