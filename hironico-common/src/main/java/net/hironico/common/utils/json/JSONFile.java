package net.hironico.common.utils.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class JSONFile {

    private static final ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.disable(SerializationFeature.WRITE_DATES_WITH_ZONE_ID);

        // serialize ONLY annotated properties
        mapper.disable(MapperFeature.AUTO_DETECT_CREATORS,
                       MapperFeature.AUTO_DETECT_FIELDS,
                       MapperFeature.AUTO_DETECT_GETTERS,
                       MapperFeature.AUTO_DETECT_IS_GETTERS);
    }

    /**
     * Serialize to JSON string the object given as parameter, If it is a ResultSet then special handling 
     * is made in this class. <strong>Please note that if passed a resultset object, then the resultset is NOT closed 
     * by this method.</strong>
     */
    public static String serialize(Object obj) throws JsonProcessingException {
        try {
            return mapper.writeValueAsString(obj);
        } catch (Exception ex) {
            IOException ioe = new IOException("cannot parse Result set", ex);
            throw JsonMappingException.fromUnexpectedIOE(ioe);
        }
    }

    public static <T extends Object> T parse(String jsonStr, Class<T> clazz) throws IOException {
        try {
            return (T) mapper.readValue(jsonStr.getBytes(), clazz);
        } catch (Exception ex) {
            throw new IOException(ex);
        }
    }

    public static <T extends Object> T load(InputStream in, Class<T> clazz) throws IOException {
        try {
            return (T) mapper.readValue(in, clazz);
        } catch (Exception ex) {
            throw new IOException(ex);
        }
    }

    public static <T extends Object> T load(File file, Class<T> clazz) throws IOException {
        try {
            return (T) mapper.readValue(file, clazz);
        } catch (Exception ex) {
            throw new IOException(ex);
        }
    }

    public static boolean saveAs(File file, Object obj) throws IOException {
        try {
            mapper.writeValue(file, obj);
            return true;
        } catch (Exception ex) {
            throw new IOException(ex);
        }
    }
}