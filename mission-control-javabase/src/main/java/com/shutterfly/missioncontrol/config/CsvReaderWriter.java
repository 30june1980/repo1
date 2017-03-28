/**
 * 
 */
package com.shutterfly.missioncontrol.config;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * @author Diptman Gupta
 *
 */
public class CsvReaderWriter extends ConfigLoader {

	public void writeToCsv(String parameter) {

		Path FILE_PATH = Paths.get(config.getProperty("RequestIdCsvPath"));

		try (BufferedWriter writer = Files.newBufferedWriter(FILE_PATH, StandardCharsets.UTF_8,
				StandardOpenOption.APPEND)) {
			writer.write(parameter + ",");

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public String[] readCsv() throws IOException {
		String line = "";
		String cvsSplitBy = ",";
		String[] records = null;
		try (BufferedReader br = new BufferedReader(
				new FileReader(config.getProperty("RequestIdCsvPath")))) {
			while ((line = br.readLine()) != null) {
				// use comma as separator
				records = line.split(cvsSplitBy);
			}
		}
		return records;

	}

}