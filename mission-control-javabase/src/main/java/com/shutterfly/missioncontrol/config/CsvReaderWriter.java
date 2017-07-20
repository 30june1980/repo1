/**
 * 
 */
package com.shutterfly.missioncontrol.config;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.testng.annotations.Test;

/**
 * @author Diptman Gupta
 *
 */
public class CsvReaderWriter extends ConfigLoader {

	public void writeToCsv(String name,String parameter) {

		Path FILE_PATH = Paths.get(config.getProperty("RequestIdCsvPath"));

		try (BufferedWriter writer = Files.newBufferedWriter(FILE_PATH, StandardCharsets.UTF_8,
				StandardOpenOption.APPEND)) {
			writer.write(name +":"+ parameter + ","  );

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public String[] readCsv() throws IOException {
		String line = "";
		String cvsSplitBy = ",";
		String[] records = null;
		try (BufferedReader br = new BufferedReader(new FileReader(config.getProperty("RequestIdCsvPath")))) {
			while ((line = br.readLine()) != null) {
				// use comma as separator
				records = line.split(cvsSplitBy);
			}
		}
		return records;

	}

	// public static void main(String[] args) {
	@Test
	public void csvIterator() throws IOException {
		basicConfigNonWeb();
		Reader in = new FileReader(config.getProperty("RequestIdCsvPath"));
		Iterable<CSVRecord> records = CSVFormat.EXCEL.parse(in);
		for (CSVRecord record : records) {
			if (record.get(0).equalsIgnoreCase("Done")) {
				System.out.println(record.get(1));
			}
		}
	}
	
	
	public String  getRequestIdByKeys(String key) throws IOException {
		basicConfigNonWeb();
	   String[] requestId = readCsv();
		String record = "";
		for (String id : requestId) {
			String arrKeyValue[] = id.split(":");
			if (arrKeyValue.length == 2) {
				if (arrKeyValue[0].equalsIgnoreCase(key)) {
					record = arrKeyValue[1];
				}
			}
		}

		return record;

	}


}
