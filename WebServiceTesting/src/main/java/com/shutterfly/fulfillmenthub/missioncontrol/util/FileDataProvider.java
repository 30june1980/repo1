package com.shutterfly.fulfillmenthub.missioncontrol.util;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.testng.annotations.DataProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by kedar on 26-09-2016.
 */
public class FileDataProvider {

    /*This class will read a data files and pass data to test classes during execution*/


    private Properties proObject = null;
    public static HSSFWorkbook wb;
    public static HSSFSheet sh;
    public static HSSFRow row;
    public static HSSFCell cell;
    public static FileInputStream fis;
    public static XSSFWorkbook xwb;
    public static XSSFSheet xsh;
    public static XSSFRow xrow;
    public static XSSFCell xcell;

    /*Method to load the properties file*/
    public Properties setResource(){
        Properties proObject = new Properties();
        String sp = "testsetupdata.properties";
        try{
            File wsprop = new File(System.getProperty("user.dir")+"/src/main/java/com/shutterfly/fulfillmenthub/missioncontrol/config/"+sp);
            proObject.load(new FileInputStream(wsprop));
        }catch (IOException e){
            System.out.println(e);
        }
        return proObject;
    }
    /*Method to retrieve data*/

    public String getProperty(String key){
        proObject = setResource();
        if (proObject==null)return null;
        return proObject.getProperty(key);
    }

    /*Method to load Excelfile .xls and .xlsx and retrieve data*/
    @DataProvider(name="readexcel")
    public Object[][]readexcel()throws Exception{
        String sheetpath = System.getProperty("user.dir")+"/Data/"+("readexcel.xls");
        Object[][]arrayObject=getExcelData(sheetpath,"sc");
        return arrayObject;
    }

    private Object[][]getExcelData(String filename,String sheetname){
        String [][]arrayExcelData=null;
        try{
            FileInputStream fis = new FileInputStream(filename);
            wb = new HSSFWorkbook(fis);
            sh = wb.getSheet(sheetname);
            int totalNoOfRows = sh.getPhysicalNumberOfRows();
            int totalNoOfCols = row.getPhysicalNumberOfCells();

            arrayExcelData = new String[totalNoOfRows][totalNoOfCols];

            for(int i=1;i<totalNoOfRows;i++){
                for(int j=1;j<totalNoOfCols;j++){
                    arrayExcelData[i-1][j] = row.getCell(i).getStringCellValue();
                }
            }
        }catch (FileNotFoundException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }
        return arrayExcelData;
    }
 }


