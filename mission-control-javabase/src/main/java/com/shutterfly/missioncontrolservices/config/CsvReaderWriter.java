/**
 *
 */
package com.shutterfly.missioncontrolservices.config;

import com.google.common.io.Resources;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import static org.testng.Assert.assertNotNull;

/**
 * @author Diptman Gupta
 */
public class CsvReaderWriter extends ConfigLoader {
    static boolean alreadyExecuted;
  //  static final String REQUEST_ID_CSV_PATH = "RequestIdCsvPath";
    static String requestIdCsvFilePath = "../mission-control-testplan/src/main/resources/requestId.csv";

    static String finalRequestIdCsvFilePath = "../mission-control-testplan/src/main/resources/finalRequestId.csv";
    private static final Logger logger = LoggerFactory.getLogger(CsvReaderWriter.class);


    private static void generateCsvFile() {
        System.out.print(requestIdCsvFilePath);
        if (!alreadyExecuted) {
            try (FileWriter writer = new FileWriter(requestIdCsvFilePath)) {
                alreadyExecuted = true;
            } catch (IOException e) {
                logger.error("Unable Generate request Id CSV file : " + e);
            }
        }
    }

    public void writeToCsv(String name, String parameter) {
        generateCsvFile();


        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(requestIdCsvFilePath), StandardCharsets.UTF_8,
                StandardOpenOption.APPEND)) {
            writer.write(name + ":" + parameter + ",");

        } catch (IOException e) {
            logger.error("Unable to write in CSV request ids to CSV file : " + e);
        }

    }

    public String[] readCsv(String filepath) throws IOException {
        String line = "";
        String cvsSplitBy = ",";
        String[] records = null;
        try (BufferedReader br = new BufferedReader(new FileReader(filepath))) {
            while ((line = br.readLine()) != null) {
                // use comma as separator
                records = line.split(cvsSplitBy);
            }
        }

        return records;

    }

    @Test
    public void csvIterator() throws IOException {
        basicConfigNonWeb();
        Reader in = new FileReader(requestIdCsvFilePath);
        Iterable<CSVRecord> records = CSVFormat.EXCEL.parse(in);
        for (CSVRecord record : records) {
            if (record.get(0).equalsIgnoreCase("Done")) {
                logger.info(record.get(1));
            }
        }
    }

    public String getRequestIdByKeys(String key) throws IOException {
        basicConfigNonWeb();
        String[] requestIds = readCsv(requestIdCsvFilePath);
        if (requestIds == null) {
            logger.error("RequestId CSV is blank");
            requestIds = new String[0];
        }
        String record = "";
        for (String id : requestIds) {
            String[] arrKeyValue = id.split(":");
            if (arrKeyValue.length == 2 && arrKeyValue[0].equalsIgnoreCase(key)) {

                record = arrKeyValue[1];
            }
        }
        if (record.isEmpty()) {
            requestIds = readCsv(finalRequestIdCsvFilePath);
            record = "";
            for (String id : requestIds) {
                String[] arrKeyValue = id.split(":");
                if (arrKeyValue.length == 2 && arrKeyValue[0].equalsIgnoreCase(key)) {
                    record = arrKeyValue[1];

                }
            }
        }

        assertNotNull(record, "Record Not available in csv files. Please add records in csv files");
        return record;

    }

}
