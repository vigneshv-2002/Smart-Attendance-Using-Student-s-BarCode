package com.attendance.util;

import com.attendance.model.Attendance;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jfree.data.category.CategoryDataset;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Utility class for exporting attendance data to Excel
 */
public class ExcelExporter {
    
    /**
     * Export attendance data to Excel
     */
    public static void exportAttendance(List<Attendance> attendanceList, File file, Date date) throws Exception {
        try (Workbook workbook = new XSSFWorkbook()) {
            // Create sheet
            Sheet sheet = workbook.createSheet("Attendance Data");
            
            // Create header row
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("ID");
            headerRow.createCell(1).setCellValue("Student ID");
            headerRow.createCell(2).setCellValue("Name");
            headerRow.createCell(3).setCellValue("Scan Time");
            headerRow.createCell(4).setCellValue("Status");
            
            // Style for header
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            
            for (int i = 0; i < 5; i++) {
                headerRow.getCell(i).setCellStyle(headerStyle);
            }
            
            // Format for times
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
            
            // Add data rows
            int rowNum = 1;
            for (Attendance attendance : attendanceList) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(attendance.getId());
                row.createCell(1).setCellValue(attendance.getStudentId());
                row.createCell(2).setCellValue(attendance.getStudent().getFullName());
                row.createCell(3).setCellValue(timeFormat.format(attendance.getScanTime()));
                row.createCell(4).setCellValue(attendance.getStatus());
            }
            
            // Format date for title
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            
            // Add title and date information
            Row titleRow = sheet.createRow(rowNum + 1);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Attendance Report for " + dateFormat.format(date));
            
            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);
            titleStyle.setFont(titleFont);
            titleCell.setCellStyle(titleStyle);
            
            // Auto-size columns
            for (int i = 0; i < 5; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // Write to file
            try (FileOutputStream outputStream = new FileOutputStream(file)) {
                workbook.write(outputStream);
            }
        }
    }

    /**
     * Export daily attendance summary
     */
    public static void exportDailyAttendanceSummary(CategoryDataset dataset, File file, Date startDate, Date endDate) throws Exception {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Daily Attendance Summary");
            
            // Format dates for title
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            
            // Add title and date range
            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Daily Attendance Summary: " + dateFormat.format(startDate) + " to " + dateFormat.format(endDate));
            
            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);
            titleStyle.setFont(titleFont);
            titleCell.setCellStyle(titleStyle);
            
            // Create header row
            Row headerRow = sheet.createRow(2);
            headerRow.createCell(0).setCellValue("Date");
            headerRow.createCell(1).setCellValue("Present");
            headerRow.createCell(2).setCellValue("Absent");
            headerRow.createCell(3).setCellValue("Total");
            headerRow.createCell(4).setCellValue("Attendance Rate (%)");
            
            // Style for header
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            
            for (int i = 0; i < 5; i++) {
                headerRow.getCell(i).setCellStyle(headerStyle);
            }
            
            // Add data from the dataset
            int rowNum = 3;
            for (int i = 0; i < dataset.getColumnCount(); i++) {
                Row row = sheet.createRow(rowNum++);
                
                String date = (String) dataset.getColumnKey(i);
                int present = dataset.getValue("Present", date).intValue();
                int absent = dataset.getValue("Absent", date).intValue();
                int total = present + absent;
                double rate = (total > 0) ? (double) present / total * 100 : 0;
                
                row.createCell(0).setCellValue(date);
                row.createCell(1).setCellValue(present);
                row.createCell(2).setCellValue(absent);
                row.createCell(3).setCellValue(total);
                
                Cell rateCell = row.createCell(4);
                rateCell.setCellValue(rate);
                
                // Format percentage
                CellStyle percentStyle = workbook.createCellStyle();
                percentStyle.setDataFormat(workbook.createDataFormat().getFormat("0.00%"));
                rateCell.setCellStyle(percentStyle);
            }
            
            // Add summary row
            Row summaryRow = sheet.createRow(rowNum + 1);
            summaryRow.createCell(0).setCellValue("Summary");
            
            CellStyle summaryStyle = workbook.createCellStyle();
            Font summaryFont = workbook.createFont();
            summaryFont.setBold(true);
            summaryStyle.setFont(summaryFont);
            summaryRow.getCell(0).setCellStyle(summaryStyle);
            
            // Auto-size columns
            for (int i = 0; i < 5; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // Write to file
            try (FileOutputStream outputStream = new FileOutputStream(file)) {
                workbook.write(outputStream);
            }
        }
    }

    /**
     * Export student attendance details
     */
    public static void exportStudentAttendanceDetails(CategoryDataset dataset, File file, Date startDate, Date endDate) throws Exception {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Student Attendance Details");
            
            // Format dates for title
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            
            // Add title and date range
            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Student Attendance Details: " + dateFormat.format(startDate) + " to " + dateFormat.format(endDate));
            
            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);
            titleStyle.setFont(titleFont);
            titleCell.setCellStyle(titleStyle);
            
            // Create header row
            Row headerRow = sheet.createRow(2);
            headerRow.createCell(0).setCellValue("Student");
            headerRow.createCell(1).setCellValue("Attendance Percentage (%)");
            headerRow.createCell(2).setCellValue("Days Present");
            headerRow.createCell(3).setCellValue("Days Absent");
            headerRow.createCell(4).setCellValue("Total Days");
            
            // Style for header
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            
            for (int i = 0; i < 5; i++) {
                headerRow.getCell(i).setCellStyle(headerStyle);
            }
            
            // Add data
            int rowNum = 3;
            for (int i = 0; i < dataset.getColumnCount(); i++) {
                Row row = sheet.createRow(rowNum++);
                
                String student = (String) dataset.getColumnKey(i);
                double percentage = dataset.getValue("Attendance %", student).doubleValue();
                
                // Calculate days based on percentage (placeholder data)
                int totalDays = 20; // Placeholder
                int daysPresent = (int) Math.round(percentage / 100 * totalDays);
                int daysAbsent = totalDays - daysPresent;
                
                row.createCell(0).setCellValue(student);
                
                Cell percentCell = row.createCell(1);
                percentCell.setCellValue(percentage / 100); // Convert to decimal for formatting
                
                // Format percentage
                CellStyle percentStyle = workbook.createCellStyle();
                percentStyle.setDataFormat(workbook.createDataFormat().getFormat("0.00%"));
                percentCell.setCellStyle(percentStyle);
                
                row.createCell(2).setCellValue(daysPresent);
                row.createCell(3).setCellValue(daysAbsent);
                row.createCell(4).setCellValue(totalDays);
            }
            
            // Auto-size columns
            for (int i = 0; i < 5; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // Write to file
            try (FileOutputStream outputStream = new FileOutputStream(file)) {
                workbook.write(outputStream);
            }
        }
    }

    /**
     * Export attendance trends
     */
    public static void exportAttendanceTrends(CategoryDataset dataset, File file, Date startDate, Date endDate) throws Exception {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Attendance Trends");
            
            // Format dates for title
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            
            // Add title and date range
            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Attendance Rate Trends: " + dateFormat.format(startDate) + " to " + dateFormat.format(endDate));
            
            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);
            titleStyle.setFont(titleFont);
            titleCell.setCellStyle(titleStyle);
            
            // Create header row
            Row headerRow = sheet.createRow(2);
            headerRow.createCell(0).setCellValue("Date");
            headerRow.createCell(1).setCellValue("Attendance Rate (%)");
            
            // Style for header
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            
            for (int i = 0; i < 2; i++) {
                headerRow.getCell(i).setCellStyle(headerStyle);
            }
            
            // Add data
            int rowNum = 3;
            for (int i = 0; i < dataset.getColumnCount(); i++) {
                Row row = sheet.createRow(rowNum++);
                
                String date = (String) dataset.getColumnKey(i);
                double rate = dataset.getValue("Attendance Rate %", date).doubleValue();
                
                row.createCell(0).setCellValue(date);
                
                Cell rateCell = row.createCell(1);
                rateCell.setCellValue(rate / 100); // Convert to decimal for formatting
                
                // Format percentage
                CellStyle percentStyle = workbook.createCellStyle();
                percentStyle.setDataFormat(workbook.createDataFormat().getFormat("0.00%"));
                rateCell.setCellStyle(percentStyle);
            }
            
            // Auto-size columns
            for (int i = 0; i < 2; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // Write to file
            try (FileOutputStream outputStream = new FileOutputStream(file)) {
                workbook.write(outputStream);
            }
        }
    }
}