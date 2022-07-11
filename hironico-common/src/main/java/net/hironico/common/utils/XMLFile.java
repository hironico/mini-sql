package net.hironico.common.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class XMLFile {

    public static String serialize(Object obj) throws IOException {
        try {
            XmlMapper xmlMapper = new XmlMapper();
            return xmlMapper.writeValueAsString(obj);

        } catch (Exception ex) {
            throw new IOException(ex); 
        }
    }

    public static void saveAs(File file, Object obj) throws IOException {
        try {
            XmlMapper xmlMapper = new XmlMapper();
            xmlMapper.enable(SerializationFeature.INDENT_OUTPUT);
            xmlMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
            xmlMapper.enable(JsonParser.Feature.IGNORE_UNDEFINED);
            xmlMapper.writeValue(file, obj);
        } catch (Exception ex) {
            throw new IOException(ex);
        }
    }

    public static <T> T load(InputStream in, Class<T> clazz) throws IOException {
        try (InputStream source = in) {
            XmlMapper xmlMapper = new XmlMapper();
            return xmlMapper.readValue(source, clazz);
        } catch (Exception ex) {
            throw new IOException(ex);
        }
    }

    public static <T> T load(File file, Class<T> clazz) throws IOException {
        try {
            XmlMapper xmlMapper = new XmlMapper();
            return xmlMapper.readValue(file, clazz);
        } catch (Exception ex) {
            throw new IOException(ex);
        }
    }
}