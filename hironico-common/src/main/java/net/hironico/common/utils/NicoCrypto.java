package net.hironico.common.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Base64;
import java.util.logging.Logger;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Classe utilitaire pour stocker des passwords dans des fichiers de config.
 * Elle utilise l'algorythme AES inclu dans le JDK.
 */
public class NicoCrypto {
    private static final Logger LOGGER = Logger.getLogger(NicoCrypto.class.getName());

    /**
    * Cipher Info
    * Algorithm : for the encryption of electronic data
    * mode of operation : to avoid repeated blocks encrypt to the same values.
    * padding: ensuring messages are the proper length necessary for certain ciphers 
    * mode/padding are not used with stream cyphers.  
    */
    private Cipher cipher = null;

    public NicoCrypto() {
        try {
            cipher = Cipher.getInstance("AES"); //SunJCE provider AES algorithm, mode(optional) and padding schema(optional)  
        } catch (Exception ex) {
            LOGGER.severe("Unable to init Cypher for encryption operations !");
        }
    }

    /**
     * Loads a text file with Base key data then returns a SecretKey object initialized with this key.
     */
    public static SecretKey load(File keyFile) throws Exception {
        if (keyFile == null || !keyFile.exists()) {
            throw new FileNotFoundException(keyFile.getAbsolutePath() + ": not found.");
        }

        try (FileReader fr = new FileReader(keyFile); BufferedReader br = new BufferedReader(fr)) {
            StringBuffer content = new StringBuffer();
            String line = br.readLine();
            while (line != null) {
                content.append(line);
                line = br.readLine();
            }

            return generate(content.toString());

        }
    }

    /**
     * Generate a secret key from a super secret token content using AES algorithm.
     */
    public static SecretKey generate(String token) {
        byte[] encoded = Base64.getDecoder().decode(token);
        SecretKeySpec spec = new SecretKeySpec(encoded, "AES");
        return spec;
    }

    /**
     * Lazy generation of a NEW secret key token using AES algorithm.
     */
    public static SecretKey generate() throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(256); // block size is 128bits
        return keyGenerator.generateKey();
    }

    public static void main(String[] args) throws Exception {
        /* 
         create key 
         If we need to generate a new key use a KeyGenerator
         If we have existing plaintext key use a SecretKeyFactory
        */
        SecretKey secretKey = generate();


        System.out.println("Key format is: " + secretKey.getFormat());

        byte[] encoded = secretKey.getEncoded();
        encoded = Base64.getEncoder().encode(encoded);
        String encodedStr = new String(encoded);
        System.out.println("Encoded key is: " + encodedStr);

        String plainText = "RTkU7Epv6kIgyhd2xigbqQ==";
        System.out.println("Plain Text Before Encryption: " + plainText);

        NicoCrypto crypto = new NicoCrypto();

        String encryptedText = crypto.encrypt(plainText, secretKey);
        System.out.println("Encrypted Text After Encryption: " + encryptedText);

        String decryptedText = crypto.decrypt(encryptedText, secretKey);
        System.out.println("Decrypted Text After Decryption: " + decryptedText);
    }

    public String encrypt(String plainText, SecretKey secretKey) throws Exception {

        if (cipher == null) {
            throw new IllegalArgumentException("Cypher not initialized properly. Cannot encrypt.");
        }

        byte[] plainTextByte = plainText.getBytes();
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encryptedByte = cipher.doFinal(plainTextByte);
        Base64.Encoder encoder = Base64.getEncoder();
        String encryptedText = encoder.encodeToString(encryptedByte);
        return encryptedText;
    }

    public String decrypt(String encryptedText, SecretKey secretKey) throws Exception {

        if (cipher == null) {
            throw new Exception("Cypher not initialized properly. Cannot decrypt.");
        }

        Base64.Decoder decoder = Base64.getDecoder();
        byte[] encryptedTextByte = decoder.decode(encryptedText);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decryptedByte = cipher.doFinal(encryptedTextByte);
        String decryptedText = new String(decryptedByte);
        return decryptedText;
    }
}