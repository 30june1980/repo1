package com.shutterfly.missioncontrolservices.config;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.shutterfly.missioncontrolservices.util.Encryption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKey;
import java.util.Collections;

/**
 * @author Diptman Gupta
 */

public class ConnectToDatabase extends ConfigLoader {

    private Logger logger = LoggerFactory.getLogger(ConnectToDatabase.class);

    private MongoClient mongoClient;
    private static final int dbPort = 27017;

    public MongoClient getMongoConnection() {
        basicConfigNonWeb();
        String databaseName = config.getProperty("DatabaseName"); // Name of the database
        String userName = config.getProperty("UserName"); // user name

        SecretKey secretKey = Encryption.keyGenerator();
        String password;
        try {
            password = Encryption.decrypt(config.getProperty("DatabasePassword"), secretKey);
        } catch (Exception e) {
            logger.error("Database password decryption failed ", e);
            throw new RuntimeException("Database password decryption failed");
        }

        MongoCredential credential = MongoCredential.createScramSha1Credential(userName, databaseName,
                password.toCharArray());

        this.mongoClient = new MongoClient(new ServerAddress(config.getProperty("DatabaseAddress"), dbPort),
                Collections.singletonList(credential));
        return mongoClient;
    }

    public MongoClient getMongoMasterNodeConnection(String databaseAddress) {
        basicConfigNonWeb();
        String databaseName = config.getProperty("DatabaseName"); // Name of the database
        String userName = config.getProperty("UserName"); // user name

        SecretKey secretKey = Encryption.keyGenerator();
        String password;
        try {
            password = Encryption.decrypt(config.getProperty("DatabasePassword"), secretKey);
        } catch (Exception e) {
            logger.error("Database password decryption failed ", e);
            throw new RuntimeException("Database password decryption failed");
        }

        MongoCredential credential = MongoCredential.createScramSha1Credential(userName, databaseName,
                password.toCharArray());
        String[] dbaddress = databaseAddress.split(":");
        this.mongoClient = new MongoClient(new ServerAddress(dbaddress[0], Integer.parseInt(dbaddress[1])),
                Collections.singletonList(credential));
        return mongoClient;
    }

    public void closeMongoConnection() {
        if (this.mongoClient != null) {
            this.mongoClient.close();
        }
    }

}