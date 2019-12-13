package com.meari.echoshow.util;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.IvParameterSpec;
import java.security.Key;

public class DesUtils {
    private static String secretKey = "123456781234567812345678";
    private final static String iv = "01234567";
    private final static String encoding = "utf-8";

    /**
     * @param key
     */
    public void setSecreKey(String key) {
        DesUtils.setSecretKey(key);
    }

    /**
     * 3DES����
     *
     * @param plainText
     * @return
     * @throws Exception
     */
    public static String encode(String plainText) throws Exception {
        Key deskey = null;
        DESedeKeySpec spec = new DESedeKeySpec(getSecretKey().getBytes());
        SecretKeyFactory keyfactory = SecretKeyFactory.getInstance("desede");
        deskey = keyfactory.generateSecret(spec);

        Cipher cipher = Cipher.getInstance("desede/CBC/PKCS5Padding");
        IvParameterSpec ips = new IvParameterSpec(iv.getBytes());
        cipher.init(Cipher.ENCRYPT_MODE, deskey, ips);
        byte[] encryptData = cipher.doFinal(plainText.getBytes(encoding));
        return Base64.encode(encryptData);
    }


    public static String getSecretKey() {
        return secretKey;
    }

    public static void setSecretKey(String secretKey) {
        DesUtils.secretKey = secretKey;
    }

    public static void main(String[] args){
        try {
            System.out.println(DesUtils.encode("1a2b3c4d5e6f7g"));
            Key deskey = null;
            DESedeKeySpec spec = new DESedeKeySpec(getSecretKey().getBytes());
            SecretKeyFactory keyfactory = SecretKeyFactory.getInstance("desede");
            deskey = keyfactory.generateSecret(spec);

            Cipher cipher = Cipher.getInstance("desede/CBC/PKCS5Padding");
            IvParameterSpec ips = new IvParameterSpec(iv.getBytes());
            cipher.init(Cipher.DECRYPT_MODE, deskey, ips);
            byte[] encryptData = cipher.doFinal(Base64.decode("8TjvVsFBLsc/k79tXLjBIw=="));
            System.out.println(new String(encryptData));
            System.out.println(DesUtils.encode("520520520."));
        }catch(Exception e){

        }
    }
}
