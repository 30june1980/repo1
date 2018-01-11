/**
 *
 */
package com.shutterfly.missioncontrol.config;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.shutterfly.missioncontrol.util.Encryption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKey;
import java.util.Arrays;

/**
 * @author Diptman Gupta
 */

public class ConnectToDatabase extends ConfigLoader {

    Logger logger = LoggerFactory.getLogger(ConnectToDatabase.class);

    MongoClient mongoClient;
    int dbPort = 27017;

    public MongoClient getMongoConnection() {
        basicConfigNonWeb();
        String databaseName = config.getProperty("DatabaseName"); // Name of the database
        String userName = config.getProperty("UserName"); // user name

        SecretKey secretKey = Encryption.keyGenerator();
        String password = null;
        try {
            password = Encryption.decrypt(config.getProperty("DatabasePassword"), secretKey);
        } catch (Exception e) {
            logger.error("Database password decryption failed ", e);
            throw new RuntimeException("Database password decryption failed");
        }

        MongoCredential credential = MongoCredential.createScramSha1Credential(userName, databaseName,
                password.toCharArray());

        this.mongoClient = new MongoClient(new ServerAddress(config.getProperty("DatabaseAddress"), dbPort),
                Arrays.asList(credential));
        return mongoClient;
    }

    public void closeMongoConnection() {
        if (this.mongoClient != null) {
            this.mongoClient.close();
        }
    }

}