package com.shutterfly.missioncontrol.listener;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;
import com.shutterfly.missioncontrol.config.ConfigLoader;
import com.shutterfly.missioncontrol.config.ConnectToDatabase;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ISuite;
import org.testng.ISuiteListener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.mongodb.client.model.Filters.eq;

public class ParentSuiteListener extends ConfigLoader implements ISuiteListener {

    private Logger logger = LoggerFactory.getLogger(ParentSuiteListener.class);

    private MongoClient mongoClient;
    private MongoDatabase database;
    private String masterNodeUrl;

    private static final String USER = "username";
    private static final String FSUSER = "FileServerUsername";
    private static final String FSPASS = "FileServerPassword";
    private static final String PASS = "password";
    private static final String TITLE = "title";
    private static final String ECG = "ECG";
    private static final String DATABASE_NAME = "missioncontrol";
    private static final String DATABASE_ADDRESS = "DatabaseAddress";
    private static final String COLLECTION_NAME = "server";

    private static final String CREDENTIALS = "credentials";
    private static final String FILE_PATH = "existing-credentials.txt";

    @Override
    public void onStart(ISuite iSuite) {
        logger.info("Changing credentials on server.....");

        File file = new File(FILE_PATH);
        logger.info("Backing up existing credentials at: ", file.getAbsolutePath());
        if (file.exists()) {
            logger.error("Server credentials already backed up.");
            throw new RuntimeException("Server credentials are already backed up. Either restore those manually on the " +
                    "server, or delete the file IF YOU ARE REALLY SURE ABOUT IT.....");
        } else {
            MongoCollection<Document> serverCollection = getServerCollectionFromServer();
            Map<String, String> credentialsMap = getCredentialsFromServer(serverCollection);
            writeCredentialsToFile(file, credentialsMap);
            credentialsMap.put(USER, config.getProperty(FSUSER));
            credentialsMap.put(PASS, config.getProperty(FSPASS));
            setCredentialsOnServer(credentialsMap, serverCollection);
            mongoClient.close();
        }
    }

    @Override
    public void onFinish(ISuite iSuite) {
        logger.info("Reverting credentials on server.....");
        File file = new File(FILE_PATH);

        logger.info("Restoring existing credentials from: ", file.getAbsolutePath());
        if (!file.exists()) {
            logger.error("Credentials file doesn't exist...");
            throw new RuntimeException("Fatal error: Credentials not backed up / there is some permissions related issue. Please try again.....");
        } else {
            Map<String, String> credentialsMap = readCredentialsFromFile(file);
            MongoCollection<Document> serverCollection = getServerCollectionFromServer();
            setCredentialsOnServer(credentialsMap, serverCollection);
            mongoClient.close();
            try {
                Files.delete(file.toPath());
            } catch (IOException e) {
                logger.error("Failed to delete the credentials backup file.", e);
                throw new RuntimeException("Failed to delete credentials backup file. Check for permissions or check if file exists.....");

            }
        }
    }

    private MongoCollection<Document> getServerCollectionFromServer() {
        ConnectToDatabase connectToDatabase = new ConnectToDatabase();
        mongoClient = connectToDatabase.getMongoConnection();
        database = mongoClient.getDatabase(DATABASE_NAME);
        if (!isMasterNode()) {
            mongoClient.close();
            logger.warn("Current mongodb node is not master node. Attempting to connect to mongo db master node.....");
            mongoClient = connectToDatabase.getMongoMasterNodeConnection(masterNodeUrl);
            database = mongoClient.getDatabase(DATABASE_NAME);
        }
        return database.getCollection(COLLECTION_NAME);
    }

    private boolean isMasterNode() {
        Document commandResult = database.runCommand(new BasicDBObject("isMaster", 1));
        masterNodeUrl = (String) commandResult.get("primary");
        return (masterNodeUrl.equalsIgnoreCase(config.getProperty(DATABASE_ADDRESS)));
    }

    private Map<String, String> getCredentialsFromServer(MongoCollection<Document> serverCollection) {
        Map<String, String> credentialsMap = new HashMap<>();
        Document document = serverCollection.find(eq(TITLE, ECG)).first();
        if (document == null) {
            throw new RuntimeException("ECG document doesn't exist, please try again");
        }
        Document credentials = (Document) document.get(CREDENTIALS);
        String username = (String) credentials.get(USER);
        String password = String.join("", (ArrayList<String>) credentials.get(PASS));
        credentialsMap.put(USER, username);
        credentialsMap.put(PASS, password);
        return credentialsMap;
    }

    private void setCredentialsOnServer(Map<String, String> credentialsMap, MongoCollection<Document> serverCollection) {
        Document document = serverCollection.find(eq(TITLE, ECG)).first();
        if (document == null) {
            throw new RuntimeException("ECG document doesn't exist, please try again");
        }
        Document credentials = (Document) document.get(CREDENTIALS);

        credentials.put(USER, credentialsMap.get(USER));
        ArrayList<Character> passArrayList = new ArrayList<>(credentialsMap.get(PASS).length());
        for (char c : credentialsMap.get(PASS).toCharArray()) {
            passArrayList.add(c);
        }
        credentials.put(PASS, passArrayList);
        document.put(CREDENTIALS, credentials);
        save(serverCollection, document);
    }

    private static void save(MongoCollection<Document> collection, Document document) {
        Object id = document.get("_id");
        if (id == null) {
            collection.insertOne(document);
        } else {
            collection.replaceOne(eq("_id", id), document, new UpdateOptions().upsert(true));
        }
    }

    private void writeCredentialsToFile(File file, Map<String, String> credentialsMap) {
        try (PrintStream printStream = new PrintStream(file)) {
            printStream.println(credentialsMap.get(USER));
            printStream.println(credentialsMap.get(PASS));
        } catch (FileNotFoundException e) {
            logger.error("Failed to write to the file.", e);
            throw new RuntimeException("Failed to write to the file, please check file permissions", e);
        }
    }

    private Map<String, String> readCredentialsFromFile(File file) {
        Map<String, String> credentialsMap = new HashMap<>();
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            String line;
            int i = 0;
            while ((line = bufferedReader.readLine()) != null && i < 2) {
                credentialsMap.put(i++ == 0 ? USER : PASS, line.trim());
            }
        } catch (IOException e) {
            logger.error("Failed to read from the file.", e);
            throw new RuntimeException("Failed to read from the file. Please check if the file exists or has correct permissions", e);
        }
        return credentialsMap;
    }

}