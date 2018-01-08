package com.shutterfly.missioncontrol.util;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class XlSheetUtils {

    private final int COLUMNS = 5;
    Logger logger = LoggerFactory.getLogger(XlSheetUtils.class);

    public Map<String, List<String>> readSheetsFromXlFile(String fileName, int noOfSheetsWithSameInputDataSchema) throws IOException {

        InputStream ExcelFileToRead = getFile(fileName);
        HSSFWorkbook wb = new HSSFWorkbook(ExcelFileToRead);
        Map<String, List<String>> excelDataMap = new HashMap<>(COLUMNS);

        int numberOfSheets = wb.getNumberOfSheets();
        if (noOfSheetsWithSameInputDataSchema > numberOfSheets || noOfSheetsWithSameInputDataSchema < 0) {
            logger.warn("Invalid noOfSheetsWithSameInputDataSchema provided, setting it to zero");
            noOfSheetsWithSameInputDataSchema = 0;
        }

        for (int i = 0; i < noOfSheetsWithSameInputDataSchema; i++) {
            HSSFSheet sheet = wb.getSheetAt(i);
            HSSFRow row;
            HSSFCell cell;

            Iterator rows = sheet.rowIterator();
            boolean isFirstRow = true;
            while (rows.hasNext()) {
                row = (HSSFRow) rows.next();

                // skip first row
                if (isFirstRow) {
                    isFirstRow = false;
                    continue;
                }

                Iterator cells = row.cellIterator();

                for (int j = 0; j < COLUMNS; j++) {

                }
                while (cells.hasNext()) {
                    cell = (HSSFCell) cells.next();

                }
                System.out.println();
            }
        }

        wb.close();
        return excelDataMap;
    }

    private FileInputStream getFile(String filePath) throws FileNotFoundException {
        if (Objects.isNull(filePath))
            throw new RuntimeException("fileN name cannot be null");
        return new FileInputStream(new File(filePath));
    }

}
