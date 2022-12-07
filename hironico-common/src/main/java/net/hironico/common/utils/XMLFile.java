package net.hironico.common.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class XMLFile {
    
    private static final XmlMapper XML_MAPPER = getXmlMapper();
    
    private static XmlMapper getXmlMapper() {
        XmlMapper xmlMapper = new XmlMapper();
        xmlMapper.enable(SerializationFeature.INDENT_OUTPUT);
        xmlMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        xmlMapper.enable(JsonParser.Feature.IGNORE_UNDEFINED);
        return xmlMapper;
    } 

    public static String serialize(Object obj) throws IOException {
        try {            
            return XML_MAPPER.writeValueAsString(obj);
        } catch (Exception ex) {
            throw new IOException(ex); 
        }
    }

    public static void saveAs(File file, Object obj) throws IOException {
        try {
            XML_MAPPER.writeValue(file, obj);
        } catch (Exception ex) {
            throw new IOException(ex);
        }
    }

    public static <T> T load(InputStream in, Class<T> clazz) throws IOException {
        try (InputStream source = in) {            
            return XML_MAPPER.readValue(source, clazz);
        } catch (Exception ex) {
            throw new IOException(ex);
        }
    }

    public static <T> T load(File file, Class<T> clazz) throws IOException {
        try {
            return XML_MAPPER.readValue(file, clazz);
        } catch (Exception ex) {
            throw new IOException(ex);
        }
    }
}