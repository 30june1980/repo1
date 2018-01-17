package com.shutterfly.missioncontrol.utils;
import java.util.Objects;
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

  public static String replaceExactMatch(String toBeModified,String exactSearch,String toBeModifiedWith){
    return toBeModified.replaceAll("\\b"+exactSearch+"\\b",toBeModifiedWith);
  }

  public static String relaceInStringFromTill(String toBeModified,String from,String till,String replaceWith){

    if(Objects.isNull(toBeModified)||Objects.isNull(from))
      throw new RuntimeException("string to be modified and from string is needed");
    int fromIndex=toBeModified.indexOf(from);
    if(fromIndex<0)
      throw new RuntimeException(from +" is not found in "+toBeModified);
    int tillIndex=toBeModified.indexOf(till);
    StringBuilder stringBuilder;
    stringBuilder=new StringBuilder(toBeModified.substring(0,fromIndex));
    stringBuilder.append(" "+replaceWith.trim()+" ");
    if(tillIndex>-1)
        stringBuilder.append(toBeModified.substring((tillIndex+till.length())));
    return stringBuilder.toString();
  }
}
