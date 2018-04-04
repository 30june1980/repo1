package com.shutterfly;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.testng.TestNG;
import org.testng.xml.Parser;
import org.testng.xml.XmlSuite;
public class ApplicationMain {

    public static void main(String[] args) {
        System.out.println("Starting automation suite for Mission Control...");
        TestNG testng = new TestNG();
        InputStream in = ApplicationMain.class.getResourceAsStream("/testng.xml");
        List<XmlSuite> suite;
        try {
            suite = (List<XmlSuite>) (new Parser(in).parse());
            testng.setXmlSuites(suite);
            testng.run();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
