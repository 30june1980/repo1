package com.shutterfly.missioncontrolservices.fulfillmenthub.util;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
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
            File wsprop = new File(System.getProperty("user.dir")+"/src/main/java/com/shutterfly/missioncontrolservices/fulfillmenthub/config/"+sp);
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


 }


