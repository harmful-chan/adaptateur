package org.bfqq.adaptateur.common.utils;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ConvertCellValueUtil {
    private static final String[] POSSIBLE_DATE_FORMATS = {"yyyy-MM-dd", "yyy-MM-dd HH:mm:ss", "yyyy/MM/dd", "yyyy/MM/dd HH:mm:ss"};

    public static Integer parseInteger(Cell cell){
        String m = cell.toString();
        int d1 = 0;
        if (m.contains("(") && m.contains(")")) {
            int i = m.indexOf("(");
            int j = m.indexOf(")");

            m = m.substring(i + 1, j);

            try {
                d1 = Integer.parseInt(m);
            } catch (NumberFormatException e) {
                // Handle parsing exception if necessary
            }
        }
        return d1;
    }


    public static <T> T cv(Cell cell, Class<T> targetType) {
        if (cell == null) {
            return null;
        }

        switch (cell.getCellType()) {
            case STRING:
                return convertStringCellValue(cell.getStringCellValue(), targetType);
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return  convertNumericDateCellValue(cell.getDateCellValue(), targetType);
                } else {
                    return convertNumericCellValue(cell.getNumericCellValue(), targetType);
                }
            case BOOLEAN:
                return targetType.cast(cell.getBooleanCellValue());
            case FORMULA:
                return convertStringCellValue(cell.getCellFormula(), targetType);
            default:
                return null;
        }
    }

    private static <T> T convertStringCellValue(String cellValue, Class<T> targetType) {
        if (String.class.isAssignableFrom(targetType)) {
            return targetType.cast(cellValue);
        } else if (Date.class.isAssignableFrom(targetType)) {
            return targetType.cast(parseDate(cellValue));
        } else {
            return null;
        }
    }

    private static <T> T convertNumericCellValue(double cellValue, Class<T> targetType) {
        if (Double.class.isAssignableFrom(targetType) || Integer.class.isAssignableFrom(targetType)) {
            return targetType.cast(cellValue);
        } else {
            return null;
        }
    }

    private static <T> T convertNumericDateCellValue(Date cellValue, Class<T> targetType) {
        if (Date.class.isAssignableFrom(targetType)) {
            return  targetType.cast(cellValue);
        } else {
            return null;
        }
    }

    private static Date parseDate(String dateString) {
        for (String format : POSSIBLE_DATE_FORMATS) {
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat(format);
                return dateFormat.parse(dateString);
            } catch (ParseException e) {
                // Ignore and try next format
            }
        }
        return null; // Unable to parse date
    }

}
