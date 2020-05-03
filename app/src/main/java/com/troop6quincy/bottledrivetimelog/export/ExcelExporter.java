package com.troop6quincy.bottledrivetimelog.export;

import com.troop6quincy.bottledrivetimelog.Scout;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Exports Scout data to an excel workbook.
 *
 * @author Joe Desmond
 * @version 1.0
 * @since 1.0
 */
public class ExcelExporter {

    /**
     * Row index of title information and column titles
     */
    private static final int TITLE_ROW = 0;

    /**
     * Row index on which Scout data begins
     */
    private static final int DATA_ROW = 2;

    /**
     * Scout name column index
     */
    private static final int NAME_COLUMN = 0;

    /**
     * Scout check-in date column
     */
    private static final int CHECK_IN_DATE_COLUMN = 1;

    /**
     * Scout check-out date column
     */
    private static final int CHECK_OUT_DATE_COLUMN = 2;

    /**
     * Scout total hours worked column
     */
    private static final int TOTAL_HOURS_COLUMN = 3;

    /**
     * Scout money earned column
     */
    private static final int MONEY_EARNED_COLUMN = 4;

    /**
     * Creates an Excel workbook in the XLSX format with the given Scout information and
     * total money earned. If the total money is negative, no money column will be added.
     *
     * @param items Scout data
     * @param totalMoney total money earned, or -1
     * @param dateFormat format to use for date cells
     * @return an Excel workbook (XLSX) with the given data
     */
    public static final XSSFWorkbook export(final List<Scout> items, final double totalMoney, final String dateFormat) {
        final XSSFWorkbook workbook = new XSSFWorkbook();
        final CreationHelper createHelper = workbook.getCreationHelper();
        final XSSFSheet sheet = workbook.createSheet("Sheet 1");

        final Row titleRow = sheet.createRow(TITLE_ROW);
        final Cell scoutNameTitle = titleRow.createCell(NAME_COLUMN);
        scoutNameTitle.setCellValue("Name");

        final Cell checkInDateTitle = titleRow.createCell(CHECK_IN_DATE_COLUMN);
        checkInDateTitle.setCellValue("Check-in Time");

        final Cell checkOutDateTitle = titleRow.createCell(CHECK_OUT_DATE_COLUMN);
        checkOutDateTitle.setCellValue("Check-out Time");

        final Cell totalHoursTitle = titleRow.createCell(TOTAL_HOURS_COLUMN);
        totalHoursTitle.setCellValue("Total Hours");

        final CellStyle dateCellStyle = workbook.createCellStyle();
        dateCellStyle.setDataFormat(createHelper.createDataFormat().getFormat(dateFormat));

        final CellStyle hourCellStyle = workbook.createCellStyle();
        hourCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("0.000"));

        final CellStyle moneyCellStyle = workbook.createCellStyle();
        moneyCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("$#,##0.00;[Red]$#,##0.00"));

        // Contains the amount of money that each Scout has earned, based on how long they worked
        // and the total money earned
        final double[] moneyEarned;

        if (totalMoney >= 0) {
            final Cell moneyEarnedTitle = titleRow.createCell(MONEY_EARNED_COLUMN);
            moneyEarnedTitle.setCellValue("Money Earned");

            moneyEarned = new double[items.size()];

            // Calculate total minutes worked
            int totalMinutesWorked = 0;
            for (Scout scout : items) {
                if (scout.minutesCheckedIn >= 0) {
                    totalMinutesWorked += scout.minutesCheckedIn;
                }
            }

            // Calculate minutes worked for each Scout
            for (int i = 0; i < moneyEarned.length; i++) {
                final Scout scout = items.get(i);

                if (scout.minutesCheckedIn >= 0) {
                    final double minuteRatio = scout.minutesCheckedIn/(double)totalMinutesWorked;
                    moneyEarned[i] = minuteRatio * totalMoney;
                } else {
                    moneyEarned[i] = -1;
                }
            }
        } else {
            moneyEarned = null;
        }

        for (int i = 0; i < items.size(); i++) {
            final Scout scout = items.get(i);

            final int rowIndex = DATA_ROW + i;
            final Row row = sheet.createRow(rowIndex);

            final Cell nameCell = row.createCell(NAME_COLUMN);
            nameCell.setCellValue(scout.name);

            final Cell checkInDateCell = row.createCell(CHECK_IN_DATE_COLUMN);
            checkInDateCell.setCellValue(scout.checkIn);
            checkInDateCell.setCellStyle(dateCellStyle);

            if (scout.checkOut != null) {
                final Cell checkOutDateCell = row.createCell(CHECK_OUT_DATE_COLUMN);
                checkOutDateCell.setCellValue(scout.checkOut);
                checkOutDateCell.setCellStyle(dateCellStyle);

                final Cell hoursWorkedCell = row.createCell(TOTAL_HOURS_COLUMN);
                hoursWorkedCell.setCellValue(scout.minutesCheckedIn / 60.0);
                hoursWorkedCell.setCellStyle(hourCellStyle);

                if (moneyEarned != null) {
                    final Cell moneyEarnedCell = row.createCell(MONEY_EARNED_COLUMN);
                    moneyEarnedCell.setCellValue(moneyEarned[i]);
                    moneyEarnedCell.setCellStyle(moneyCellStyle);
                }
            }
        }

        return workbook;
    }
}
