package net.hironico.common.utils.json;

import static org.junit.Assert.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestJSONFile {
    @JsonRootName("JSONTestObject")
    public static class JSONTestObject {
        @JsonProperty("firstName")
        public String firstName;

        @JsonProperty("lastName")
        public String lastName;

        @JsonProperty("nullValue")
        public String nullVal = null;

        @JsonProperty("siblings)")
        public List<String> siblings = new ArrayList<>();
    }

    protected static JSONTestObject testObj;
    protected static ObjectMapper mapper;
    protected static File testFile = new File("./testFile.json");

    @BeforeClass
    public static void init() {
        testObj = new JSONTestObject();
        testObj.firstName = "Nico";
        testObj.lastName = "RamRam";
        testObj.siblings.add("First");
        testObj.siblings.add("Second");
        testObj.siblings.add("Third");

        mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.disable(SerializationFeature.WRITE_DATES_WITH_ZONE_ID);

        // serialize ONLY annotated properties
        mapper.disable(MapperFeature.AUTO_DETECT_CREATORS,
                       MapperFeature.AUTO_DETECT_FIELDS,
                       MapperFeature.AUTO_DETECT_GETTERS,
                       MapperFeature.AUTO_DETECT_IS_GETTERS);
    }

    @AfterClass
    public static void destroy() {
        if (testFile.exists()) {
            if (!testFile.delete()) {
                System.out.println("WARNING: Could not delete test file: " + testFile.getAbsolutePath());
            }
        }
    }

    @Test
    public void test1Serialize() {
        try {
            String jsonStr = mapper.writeValueAsString(testObj);
            assertNotNull(jsonStr);

            String testValue = JSONFile.serialize(testObj);
            assertEquals(jsonStr, testValue);
        } catch (Exception ex) {
            fail("Could not serialize object.");
            ex.printStackTrace();
        }
    }

    @Test
    public void test2Write() {

        try {
            boolean result = JSONFile.saveAs(testFile, testObj);
            assertTrue("Could not save the JSON file.", result);
        } catch (Exception ex) {
            ex.printStackTrace();
            fail(ex.getMessage());
        }

    }

    @Test
    public void test3Load() {

        try {
            JSONTestObject obj = JSONFile.load(testFile, JSONTestObject.class);
            assertNotNull(obj);

            String serializedObj = JSONFile.serialize(obj);
            Path path = Paths.get(testFile.getAbsolutePath());
            String fileContent = new String(Files.readAllBytes(path));

            assertEquals(serializedObj, fileContent);

        } catch (Exception ex) {
            ex.printStackTrace();
            fail(ex.getMessage());
        }
    }
}