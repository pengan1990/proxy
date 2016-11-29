package util;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.MappingJsonFactory;
import org.codehaus.jackson.map.ObjectMapper;


import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

/**
 * Created by pengan on 16-9-28.
 */
public class JsonUtil {

    public static JsonNode parseJson(String json) throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        JsonFactory f = new MappingJsonFactory();
        JsonParser jp = null;
        JsonNode rootNode = null;
        try {
            jp = f.createJsonParser(json);
            rootNode = mapper.readTree(jp);
        } catch (Exception e) {
            throw e;
        } finally {
            if (jp != null)
                jp.close();
        }
        return rootNode;
    }

    /**
     * just support 2 levels json serial
     *
     * @param values
     * @param keys
     * @return
     * @throws IOException
     */
    public static String convertArrToJson(Object[] values, String[] keys) throws IOException {
        ObjectMapper objectMap = new ObjectMapper();
        StringWriter sw = new StringWriter();
        JsonGenerator gen = objectMap.getJsonFactory().createJsonGenerator(sw);
        gen.writeStartObject();

        for (int idx = 0; idx < keys.length; idx++) {
            if (values[idx] instanceof Object[]) {
                handleArray(gen, (Object[]) values[idx], keys[idx]);
            } else {
                handleNormal(gen, values[idx], keys[idx]);
            }
        }
        sw.append('\n');
        gen.flush();
        gen.close();

        return sw.toString();
    }

    private static JsonGenerator handleList(JsonGenerator gen, List<Object> values, String key) throws IOException {
        gen.writeFieldName(key);
        gen.writeStartArray();
        for (Object value : values) {
            gen.writeObject(value);
        }
        gen.writeEndArray();
        return gen;
    }

    private static JsonGenerator handleArray(JsonGenerator gen, Object[] values, String key) throws IOException {
        gen.writeFieldName(key);
        gen.writeStartArray();
        for (Object value : values) {
            gen.writeObject(value);
        }
        gen.writeEndArray();
        return gen;
    }

    private static JsonGenerator handleNormal(JsonGenerator gen, Object value, String key) throws IOException {
        gen.writeFieldName(key);
        gen.writeObject(value);
        return gen;
    }

    /**
     * convert map to Json
     *
     * @param map
     * @return
     * @throws IOException
     */
    public static String convertMapToJson(Map<String, Object> map) throws IOException {
        ObjectMapper objectMap = new ObjectMapper();
        StringWriter sw = new StringWriter();
        JsonGenerator gen = objectMap.getJsonFactory().createJsonGenerator(sw);
        gen.writeStartObject();
        handleMap(gen, map);
        gen.writeEndObject();
//        sw.append('\n');
        gen.flush();
        gen.close();

        return sw.toString();
    }


    private static JsonGenerator handleMap(JsonGenerator gen, Map<String, Object> map) throws IOException {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (entry.getValue() instanceof Map) {
                gen.writeFieldName(entry.getKey());
                gen.writeStartObject();
                handleMap(gen, (Map<String, Object>) entry.getValue());
                gen.writeEndObject();
            } else if (entry.getValue() instanceof Object[]) {
                handleArray(gen, (Object[]) entry.getValue(), entry.getKey());
            } else if (entry.getValue() instanceof List) {
                handleList(gen, (List<Object>) entry.getValue(), entry.getKey());
            } else {
                handleNormal(gen, entry.getValue(), entry.getKey());
            }
        }
        return gen;
    }
}