package com.shutterfly.missioncontrol.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XlSheetUtils {

  Logger logger = LoggerFactory.getLogger(XlSheetUtils.class);

  private int ColumnsInXlSheet = 0;

  private Object[] excelDataToMapKeys;

  private int noOfSheetsWithSameInputDataSchema = 1;

  private int totalNumberOfSheets = 1;

  private XSSFWorkbook wb;

  private Map<Object, List<Object>> readSheetFromXlFile(int readSheetNumber) throws IOException {

    Map<Object, List<Object>> excelDataToMap = new LinkedHashMap<>(6);

    Sheet sheet = wb.getSheetAt(readSheetNumber);
    Row row;
    Cell cell;

    Iterator<Row> rowIterator = sheet.iterator();
    boolean isFirstRow = true;

    while (rowIterator.hasNext()) {
      row = rowIterator.next();
      Iterator<Cell> cellIterator = row.cellIterator();
      if (isFirstRow) {
        ColumnsInXlSheet = row.getLastCellNum();
        isFirstRow = false;
        while (cellIterator.hasNext()) {
          cell = cellIterator.next();
          excelDataToMap.put(getCellValue(cell), new LinkedList<>());

        }
        excelDataToMapKeys = excelDataToMap.keySet().toArray();
        continue;
      }
      while (cellIterator.hasNext()) {
        cell = cellIterator.next();
        if (cell.getColumnIndex() > ColumnsInXlSheet-1) {
          throw new RuntimeException("excel sheet is malformed");
        }
        List<Object> arrayList = excelDataToMap.get(excelDataToMapKeys[cell.getColumnIndex()]);
        arrayList.add(getCellValue(cell));
      }
    }
    return excelDataToMap;
  }

  /**
   *
   * @param fileName excel workbook
   * @param noOfSheetsWithSameInputDataSchema number of sheets to read into list of map, sheet number starts with 1
   * @return list of map
   * @throws IOException
   */
  public List<Map<Object, List<Object>>> readXlWorkBookInListOfMap(String fileName,
      int noOfSheetsWithSameInputDataSchema)
      throws IOException {
    List list = new LinkedList<Map<Object, List<Object>>>();
    this.totalNumberOfSheets = this.assignWbAndFindNUmberOfSheets(fileName);
    if (noOfSheetsWithSameInputDataSchema > totalNumberOfSheets
        || noOfSheetsWithSameInputDataSchema <= 0) {
      logger.warn("Invalid noOfSheetsWithSameInputDataSchema provided, setting it to zero");
      this.noOfSheetsWithSameInputDataSchema = this.totalNumberOfSheets;
    }

    for (int i = 0; i < noOfSheetsWithSameInputDataSchema; i++) {
      list.add(this.readSheetFromXlFile(i));
    }
    wb.close();
    return list;
  }

  /**
   *
   * @param fileName
   * @return list of map, map corresponding to excel sheet and list will have whole workbook
   * @throws IOException
   * will read the whole excel work book
   */

  public List<Map<Object, List<Object>>> readXlWorkBookInListOfMap(String fileName)
      throws IOException {
    return this.readXlWorkBookInListOfMap(fileName, 0);
  }

  /**
   * reads a specific sheet number  into map , if wrong sheet number is given then it reads sheet number 1
   * @param fileName
   * @param sheetNumberToReadFrom sheet number should start from 1
   * @return
   * @throws IOException
   */
  public Map<Object, List<Object>> readSpecificSheetNumber(String fileName,
      int sheetNumberToReadFrom)
      throws IOException {
    this.totalNumberOfSheets = this.assignWbAndFindNUmberOfSheets(fileName);
    --sheetNumberToReadFrom;
    if (sheetNumberToReadFrom > totalNumberOfSheets || sheetNumberToReadFrom<0) {
      logger.warn("Invalid noOfSheetsWithSameInputDataSchema provided, setting it to zero");
      sheetNumberToReadFrom = 0;
    }
    Map map = this.readSheetFromXlFile(sheetNumberToReadFrom);
    wb.close();
    return map;
  }

  private int assignWbAndFindNUmberOfSheets(String fileName) throws IOException {
    FileInputStream ExcelFileToRead = this.getFile(fileName);
    wb = new XSSFWorkbook(ExcelFileToRead);
    return wb.getNumberOfSheets();
  }

  private FileInputStream getFile(String filePath) throws FileNotFoundException {
    if (Objects.isNull(filePath)) {
      throw new RuntimeException("fileName name cannot be null");
    }
    File fileToRead = new File(filePath);
    if (!checkIfFileExistsAndNotDir(fileToRead)) {
      throw new RuntimeException("Either file does not exists or it is directory");
    }
    return new FileInputStream(fileToRead);
  }

  private Object getCellValue(Cell cell) {
    if (Objects.isNull(cell)) {
      throw new RuntimeException("cell value cannot be null");
    }
    switch (cell.getCellTypeEnum()) {
      case STRING:
        return cell.getStringCellValue();
      case NUMERIC:
        return cell.getNumericCellValue();
      case _NONE:
        return "";
      default:
        return "";
    }
  }

  /**
   * checks if file exists and not directory
   * @param file
   * @return
   */
  public boolean checkIfFileExistsAndNotDir(File file) {
    boolean checkIfFileExistsAndNotDir;
    checkIfFileExistsAndNotDir = (file.exists() && !file.isDirectory()) ? true : false;
    return checkIfFileExistsAndNotDir;
  }

  public static void main(String[] s) {
    String fileName = "/home/mohammad/Downloads/ruksad_testing.xlsx";
    try {
      Map map = new XlSheetUtils().readSpecificSheetNumber(fileName, 2);
      System.out.println("map =" + map);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
