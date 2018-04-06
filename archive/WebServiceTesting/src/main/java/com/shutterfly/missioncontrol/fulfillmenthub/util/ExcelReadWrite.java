package com.shutterfly.missioncontrolservices.fulfillmenthub.util;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;

/**
 * Created by kedar on 28-09-2016.
 */
public class ExcelReadWrite {
    public static XSSFWorkbook wb;
    public static XSSFSheet sh;
    public static XSSFRow row;
    public static XSSFCell cell;
    String filename = "readexcel.xlsx";
    File xls = new File(System.getProperty("user.dir")+"/Data/"+filename);
    private FileInputStream fis = new FileInputStream(xls);

    public ExcelReadWrite() throws FileNotFoundException {
    }

    public String excelReadWrite() throws IOException {

        //XSSFWorkbook workbook = null;
        XSSFWorkbook wb = new XSSFWorkbook(fis);
        XSSFSheet sh = wb.getSheet("Sheet1");
        try {


            Iterator rows = sh.rowIterator();
            while (rows.hasNext()){
                row=(XSSFRow) rows.next();
                Iterator cells = row.cellIterator();
                while (cells.hasNext()){
                    cell = (XSSFCell)cells.next();
                    System.out.println(cell.getStringCellValue());
                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }
        return String.valueOf(cell);

    }
    //@Test
    /*public void readSpecificCellFromExcel() throws IOException {
        //XSSFWorkbook wb = null;
            XSSFWorkbook wb = new XSSFWorkbook(fis);
            XSSFSheet sh = wb.getSheet("Sheet1");

            Iterator row1 = sh.rowIterator();
            while (row1.hasNext()){
                row=(XSSFRow) row1.next();

                System.out.println(row.getcell(0));
                System.out.println(row.getcell(1));
            }

    }*/
}
