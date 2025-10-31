package net.hironico.common.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Utility class for XML serialization and deserialization using Jackson XmlMapper.
 */
public class XMLFile {

    private static final XmlMapper XML_MAPPER = getXmlMapper();

    private static XmlMapper getXmlMapper() {
        XmlMapper xmlMapper = new XmlMapper();
        xmlMapper.enable(SerializationFeature.INDENT_OUTPUT);
        xmlMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        xmlMapper.enable(JsonParser.Feature.IGNORE_UNDEFINED);
        return xmlMapper;
    }

    /**
     * Serializes an object to XML string.
     * @param obj the object to serialize
     * @return the XML string representation
     * @throws IOException if serialization fails
     */
    public static String serialize(Object obj) throws IOException {
        try {
            return XML_MAPPER.writeValueAsString(obj);
        } catch (Exception ex) {
            throw new IOException(ex);
        }
    }

    /**
     * Saves an object to a file in XML format.
     * @param file the file to save to
     * @param obj the object to serialize
     * @throws IOException if writing fails
     */
    public static void saveAs(File file, Object obj) throws IOException {
        try {
            XML_MAPPER.writeValue(file, obj);
        } catch (Exception ex) {
            throw new IOException(ex);
        }
    }

    /**
     * Loads an object from XML input stream.
     * @param <T> the type
     * @param in the input stream containing XML
     * @param clazz the class to deserialize to
     * @return the deserialized object
     * @throws IOException if reading fails
     */
    public static <T> T load(InputStream in, Class<T> clazz) throws IOException {
        try (InputStream source = in) {
            return XML_MAPPER.readValue(source, clazz);
        } catch (Exception ex) {
            throw new IOException(ex);
        }
    }

    /**
     * Loads an object from XML file.
     * @param <T> the type
     * @param file the file containing XML
     * @param clazz the class to deserialize to
     * @return the deserialized object
     * @throws IOException if reading fails
     */
    public static <T> T load(File file, Class<T> clazz) throws IOException {
        try {
            return XML_MAPPER.readValue(file, clazz);
        } catch (Exception ex) {
            throw new IOException(ex);
        }
    }
}
