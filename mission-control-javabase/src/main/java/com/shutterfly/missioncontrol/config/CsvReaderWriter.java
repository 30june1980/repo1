/**
 * 
 */
package com.shutterfly.missioncontrol.config;

import static org.testng.Assert.assertNotNull;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

/**
 * @author Diptman Gupta
 *
 */
public class CsvReaderWriter extends ConfigLoader {
	static boolean alreadyExecuted;
	static final String REQUEST_ID_CSV_PATH = "RequestIdCsvPath";
	private static final Logger logger = LoggerFactory.getLogger(CsvReaderWriter.class);
	private static void generateCsvFile() {
		if (!alreadyExecuted) {
			try (FileWriter writer = new FileWriter(config.getProperty(REQUEST_ID_CSV_PATH))) {
				alreadyExecuted = true;
			} catch (IOException e) {
				logger.error("Unable Generate request Id CSV file : " + e);
			}
		}
	}

	public void writeToCsv(String name, String parameter) {
		generateCsvFile();
		Path filePath = Paths.get(config.getProperty(REQUEST_ID_CSV_PATH));

		try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8,
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
		try (BufferedReader br = new BufferedReader(new FileReader(config.getProperty(filepath)))) {
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
		Reader in = new FileReader(config.getProperty(REQUEST_ID_CSV_PATH));
		Iterable<CSVRecord> records = CSVFormat.EXCEL.parse(in);
		for (CSVRecord record : records) {
			if (record.get(0).equalsIgnoreCase("Done")) {
				logger.info(record.get(1));
			}
		}
	}

	public String getRequestIdByKeys(String key) throws IOException {
		basicConfigNonWeb();
		String[] requestIds = readCsv(REQUEST_ID_CSV_PATH);
		String record = "";
		for (String id : requestIds) {
			String[] arrKeyValue = id.split(":");
			if (arrKeyValue.length == 2 && arrKeyValue[0].equalsIgnoreCase(key)) {

				record = arrKeyValue[1];
			}
		}
		if (record.isEmpty()) {
			requestIds = readCsv("FinalRequestIdCsvPath");
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
