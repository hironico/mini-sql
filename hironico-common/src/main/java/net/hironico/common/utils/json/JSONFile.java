package net.hironico.common.utils.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.cfg.MapperBuilder;
import com.fasterxml.jackson.databind.json.JsonMapper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class JSONFile {

    private static final MapperBuilder<JsonMapper, JsonMapper.Builder> builder;
    private static final JsonMapper mapper;

    static {
        // serialize ONLY annotated properties
        builder = JsonMapper.builder()
                .disable(MapperFeature.AUTO_DETECT_CREATORS,
                         MapperFeature.AUTO_DETECT_FIELDS,
                         MapperFeature.AUTO_DETECT_GETTERS,
                         MapperFeature.AUTO_DETECT_IS_GETTERS)
                .enable(SerializationFeature.INDENT_OUTPUT)
                .disable(SerializationFeature.WRITE_DATES_WITH_ZONE_ID);
        mapper = builder.build();
    }

    /**
     * Serialize to JSON string the object given as parameter, If it is a ResultSet then special handling 
     * is made in this class. <strong>Please note that if passed a resultset object, then the resultset is NOT closed 
     * by this method.</strong>
     * @param obj is the object to serialize in JSON
     * @return the serialized version of the passed object in JSON format.
     * @throws JsonProcessingException in case of any problem during serialization
     */
    public static String serialize(Object obj) throws JsonProcessingException {
        try {
            return mapper.writeValueAsString(obj);
        } catch (Exception ex) {
            IOException ioe = new IOException("cannot parse Result set", ex);
            throw JsonMappingException.fromUnexpectedIOE(ioe);
        }
    }

    public static <T> T parse(String jsonStr, Class<T> clazz) throws IOException {
        try {
            return (T) mapper.readValue(jsonStr.getBytes(), clazz);
        } catch (Exception ex) {
            throw new IOException(ex);
        }
    }

    public static <T> T load(InputStream in, Class<T> clazz) throws IOException {
        try {
            return (T) mapper.readValue(in, clazz);
        } catch (Exception ex) {
            throw new IOException(ex);
        }
    }

    public static <T> T load(File file, Class<T> clazz) throws IOException {
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