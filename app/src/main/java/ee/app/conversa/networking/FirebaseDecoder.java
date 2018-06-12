package ee.app.conversa.networking;

import android.util.Base64;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SimpleTimeZone;

import ee.app.conversa.utils.Logger;

/**
 * Created by edgargomez on 2/10/18.
 */

public class FirebaseDecoder {

    private static final FirebaseDecoder INSTANCE = new FirebaseDecoder();

    public static FirebaseDecoder get() {
        return INSTANCE;
    }

    protected FirebaseDecoder() {
        // do nothing
    }

    private List<Object> convertJSONArrayToList(JSONArray array) {
        List<Object> list = new ArrayList<>();
        for (int i = 0; i < array.length(); ++i) {
            list.add(decode(array.opt(i)));
        }
        return list;
    }

    private Map<String, Object> convertJSONObjectToMap(JSONObject object) {
        Map<String, Object> outputMap = new HashMap<>();
        Iterator<String> it = object.keys();
        while (it.hasNext()) {
            String key = it.next();
            Object value = object.opt(key);
            outputMap.put(key, decode(value));
        }
        return outputMap;
    }

    private Object decode(Object object) {
        if (object instanceof JSONArray) {
            return convertJSONArrayToList((JSONArray) object);
        }

        if (object == JSONObject.NULL) {
            return null;
        }

        if (!(object instanceof JSONObject)) {
            return object;
        }

        JSONObject jsonObject = (JSONObject) object;

        String opString = jsonObject.optString("__op", null);

        String typeString = jsonObject.optString("__type", null);

        if (typeString == null) {
            return convertJSONObjectToMap(jsonObject);
        }

        if (typeString.equals("Date")) {
            String iso = jsonObject.optString("iso");
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
            dateFormat.setTimeZone(new SimpleTimeZone(0, "GMT"));

            try {
                return dateFormat.parse(iso);
            } catch (java.text.ParseException e) {
                // Should never happen
                Logger.error("decode", "could not parse date: " + iso, e);
                return null;
            }
        }

        if (typeString.equals("Bytes")) {
            String base64 = jsonObject.optString("base64");
            return Base64.decode(base64, Base64.NO_WRAP);
        }

//        if (typeString.equals("File")) {
//            return new ParseFile(jsonObject, this);
//        }

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

//        if (typeString.equals("Object")) {
//            return ParseObject.fromJSON(jsonObject, null, this);
//        }

//        if (typeString.equals("Relation")) {
//            return new ParseRelation<>(jsonObject, this);
//        }

        return null;
    }
}