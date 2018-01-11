package com.shutterfly.missioncontrol.utils;
import java.util.UUID;

import com.shutterfly.missioncontrol.common.AppConstants;
import com.shutterfly.missioncontrol.util.Encryption;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

public class Utils {

  public static String getDecryptedString(String encryptedString, SecretKey secretKey)
      throws IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException {
    return Encryption.decrypt(encryptedString, secretKey);
  }

  public static SecretKey getSecretKey() {
    return Encryption.keyGenerator();
  }

  public static String getQARandomId(){
    return AppConstants.REQUEST_ID_PREPEND.concat(String.valueOf(UUID.randomUUID()));
  }
}
