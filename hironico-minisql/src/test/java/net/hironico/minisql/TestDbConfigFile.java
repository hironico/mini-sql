package net.hironico.minisql;

import java.io.File;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import static org.junit.Assert.*;

import net.hironico.common.utils.json.JSONFile;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestDbConfigFile {

    private static final String testDbConfigJsonFile = "./src/test/resources/test-dbconfig.json";

    @Test
    public void test1LoadTestConfigJsonFile() throws Exception {
        DbConfigFile dbConfigFile = JSONFile.load(new File(testDbConfigJsonFile), DbConfigFile.class);
        assertNotNull(dbConfigFile);
    }

    @Test
    public void test2SerializeJson() throws Exception {                
        JSONFile.load(new File(testDbConfigJsonFile), DbConfigFile.class);
        String configFileContent = JSONFile.serialize(DbConfigFile.all);

        assertNotNull(configFileContent);
        assertFalse(configFileContent.isEmpty());

        System.out.println(configFileContent);
    }
}