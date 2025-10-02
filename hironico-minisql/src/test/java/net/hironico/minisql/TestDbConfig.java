package net.hironico.minisql;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestDbConfig {

    @Test
    public void testEncryptDecrypt() {
        final String clear = "hironico is cool software";
        String b64Encrypted = DbConfig.encryptPassword(clear);
        String decrypted = DbConfig.decryptPassword(b64Encrypted);
        assertEquals(clear, decrypted);
    }
}
