package backend.service.excel;

import backend.entity.Threat;
import backend.entity.User;
import backend.repository.ThreatRepository;
import backend.repository.UserRepository;
import backend.service.AuditLogService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class ThreatExcelService {

    @Autowired
    private ThreatRepository threatRepository;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private UserRepository userRepository;

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        return userRepository.findByEmail(auth.getName()).orElse(null);
    }

    public ByteArrayInputStream generateExcel() throws IOException {

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Threat Report");

        // Header Style
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);

        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFont(headerFont);

        // Header Row
        Row header = sheet.createRow(0);
        String[] columns = {"ID", "Title", "Severity", "Source", "Status"};

        for (int i = 0; i < columns.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerStyle);
        }

        // Data
        List<Threat> threats = threatRepository.findAll();
        int rowNum = 1;

        for (Threat threat : threats) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(threat.getId());
            row.createCell(1).setCellValue(threat.getTitle());
            row.createCell(2).setCellValue(threat.getSeverity());
            row.createCell(3).setCellValue(threat.getSource());
            row.createCell(4).setCellValue(threat.getStatus());
        }

        // Auto Size Columns
        for (int i = 0; i < columns.length; i++) {
            sheet.autoSizeColumn(i);
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();

        auditLogService.createLog("REPORT_DOWNLOADED",
                "Threat report downloaded as Excel (" + threats.size() + " records)",
                getCurrentUser(), null);

        return new ByteArrayInputStream(out.toByteArray());
    }

}