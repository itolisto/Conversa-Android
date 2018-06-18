package ee.app.conversa.networking;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ConversaDecoder {

    // This class isn't really a Singleton, but since it has no state, it's more efficient to get the
    // default instance.
    private static final ConversaDecoder INSTANCE = new ConversaDecoder();

    public static ConversaDecoder get() {
        return INSTANCE;
    }

    protected ConversaDecoder() {
        // do nothing
    }

    /* package */ List<Object> convertJSONArrayToList(JSONArray array) {
        List<Object> list = new ArrayList<>();
        for (int i = 0; i < array.length(); ++i) {
            list.add(decode(array.opt(i)));
        }
        return list;
    }

    /* package */ Map<String, Object> convertJSONObjectToMap(JSONObject object) {
        Map<String, Object> outputMap = new HashMap<>();
        Iterator<String> it = object.keys();
        while (it.hasNext()) {
            String key = it.next();
            Object value = object.opt(key);
            outputMap.put(key, decode(value));
        }
        return outputMap;
    }

    public Object decode(Object object) {
//        if (object instanceof JSONArray) {
//            return convertJSONArrayToList((JSONArray) object);
//        }
//
//        if (object == JSONObject.NULL) {
//            return null;
//        }
//
//        if (!(object instanceof JSONObject)) {
//            return object;
//        }
//
//        JSONObject jsonObject = (JSONObject) object;
//
//        String opString = jsonObject.optString("__op", null);
//        if (opString != null) {
//            try {
//                return ParseFieldOperations.decode(jsonObject, this);
//            } catch (JSONException e) {
//                throw new RuntimeException(e);
//            }
//        }
//
//        String typeString = jsonObject.optString("__type", null);
//        if (typeString == null) {
//            return convertJSONObjectToMap(jsonObject);
//        }
//
//        if (typeString.equals("Date")) {
//            String iso = jsonObject.optString("iso");
//            return ParseDateFormat.getInstance().parse(iso);
//        }
//
//        if (typeString.equals("Bytes")) {
//            String base64 = jsonObject.optString("base64");
//            return Base64.decode(base64, Base64.NO_WRAP);
//        }
//
//        if (typeString.equals("File")) {
//            return new ParseFile(jsonObject, this);
//        }
//
//        if (typeString.equals("GeoPoint")) {
//            double latitude, longitude;
//            try {
//                latitude = jsonObject.getDouble("latitude");
//                longitude = jsonObject.getDouble("longitude");
//            } catch (JSONException e) {
//                throw new RuntimeException(e);
//            }
//            return new ParseGeoPoint(latitude, longitude);
//        }
//
//        if (typeString.equals("Polygon")) {
//            List<ParseGeoPoint> coordinates = new ArrayList<ParseGeoPoint>();
//            try {
//                JSONArray array = jsonObject.getJSONArray("coordinates");
//                for (int i = 0; i < array.length(); ++i) {
//                    JSONArray point = array.getJSONArray(i);
//                    coordinates.add(new ParseGeoPoint(point.getDouble(0), point.getDouble(1)));
//                }
//            } catch (JSONException e) {
//                throw new RuntimeException(e);
//            }
//            return new ParsePolygon(coordinates);
//        }
//
//        if (typeString.equals("Object")) {
//            return ParseObject.fromJSON(jsonObject, null, this);
//        }
//
//        if (typeString.equals("Relation")) {
//            return new ParseRelation<>(jsonObject, this);
//        }
//
//        if (typeString.equals("OfflineObject")) {
//            throw new RuntimeException("An unexpected offline pointer was encountered.");
//        }

        return null;
    }
}