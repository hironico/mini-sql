package ch.ubp.pms.minisql;

public class DbConfigTest {

    public static void main(String... args) {


        String b64Crypted = "RTkU7Epv6kIgyhd2xigbqQ==";
        String decrypted = DbConfig.decryptPassword(b64Crypted);
        System.out.println(decrypted);
    }
}
