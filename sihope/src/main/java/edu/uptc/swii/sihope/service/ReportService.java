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
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import edu.uptc.swii.sihope.domain.Cita;
import edu.uptc.swii.sihope.dto.response.CitasReportResponse;
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
        List<Cita> citas = citaRepository
                .findByStatusAndDateBetweenOrderByDateAscStartTimeAsc(Cita.ATENDIDA, from, to);

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

        String message = citas.isEmpty()
                ? "No hay citas atendidas en el periodo seleccionado."
                : null;

        return new CitasReportResponse(from.toString(), to.toString(), citas.size(),
                monitorRows, subjectRows, message);
    }

    public byte[] exportExcel(LocalDate from, LocalDate to) {
        CitasReportResponse report = citasAtendidas(from, to);
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

            wb.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo generar el Excel del reporte.", e);
        }
    }

    public byte[] exportPdf(LocalDate from, LocalDate to) {
        CitasReportResponse report = citasAtendidas(from, to);
        Document doc = new Document();
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfWriter.getInstance(doc, out);
            doc.open();

            Font title = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Font normal = FontFactory.getFont(FontFactory.HELVETICA, 11);

            doc.add(new Paragraph("Reporte de citas atendidas", title));
            doc.add(new Paragraph("Periodo: " + report.from() + " a " + report.to(), normal));
            doc.add(new Paragraph("Total de citas atendidas: " + report.total(), normal));
            doc.add(new Paragraph(" ", normal));

            if (report.message() != null) {
                doc.add(new Paragraph(report.message(), normal));
            } else {
                doc.add(pdfTable("Por monitor", "Monitor", report.byMonitor()));
                doc.add(new Paragraph(" ", normal));
                doc.add(pdfTable("Por tema", "Asignatura", report.bySubject()));
            }

            doc.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo generar el PDF del reporte.", e);
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

    private String monitorName(Cita c) {
        return c.getMonitor() == null ? "—" : fullName(c.getMonitor().getFirstName(),
                c.getMonitor().getLastName());
    }

    private String subjectName(Cita c) {
        return c.getSubject() == null ? "—" : c.getSubject().getName();
    }

    private String fullName(String first, String last) {
        return ((first == null ? "" : first) + " " + (last == null ? "" : last)).trim();
    }
}
