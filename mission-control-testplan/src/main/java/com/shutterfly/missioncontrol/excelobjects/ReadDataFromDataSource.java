package com.shutterfly.missioncontrol.excelobjects;

import com.shutterfly.missioncontrol.util.XlSheetUtils;
import com.shutterfly.missioncontrol.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

// DO NOT CHANGE THIS CLASS
public class ReadDataFromDataSource {

    // Add a column here
    public static final String USERNAMEKEY = "UserNames";
    public static final String PASSWORDKEY = "Passwords";
    public static final int NUMBER_OF_USERS = 10;
    private String dataSource;
    private static final Logger logger = LoggerFactory.getLogger(ReadDataFromDataSource.class);

    public TestLoginData readLoginData(int sheetNumber) throws IOException {
        Map<Object, List<Object>> readUserNamePassword;
        readUserNamePassword = new XlSheetUtils().readSpecificSheetNumber(this.dataSource, sheetNumber);
        return this.mapTestLoginDataFromDataSource(readUserNamePassword);
    }

    private TestLoginData mapTestLoginDataFromDataSource(Map<Object, List<Object>> readUserNamePassword) {
        List<PortalUser> portalUsers = null;
        if (Objects.nonNull(readUserNamePassword) && !readUserNamePassword.isEmpty()) {
            List<Object> userNames = readUserNamePassword.get(USERNAMEKEY);
            List<Object> passwords = readUserNamePassword.get(PASSWORDKEY);
            portalUsers = new ArrayList<>(NUMBER_OF_USERS);
            for (int i = 0; i < userNames.size(); i++) {
                try {
                    portalUsers.add(new PortalUser(String.valueOf(userNames.get(i)),
                            Utils.getDecryptedString(String.valueOf(passwords.get(i)).trim(), Utils.getSecretKey())));
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.error("failed for user" + userNames.get(i) + e.getMessage());
                }
            }
        }
        return new TestLoginData(portalUsers);
    }

    public String getDataSource() {
        return dataSource;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }
}
