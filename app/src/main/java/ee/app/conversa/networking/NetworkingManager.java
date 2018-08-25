package ee.app.conversa.networking;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.ANRequest;
import com.androidnetworking.common.ANResponse;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONArrayRequestListener;
import com.androidnetworking.interfaces.StringRequestListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import ee.app.conversa.BuildConfig;
import ee.app.conversa.ConversaApp;
import ee.app.conversa.interfaces.FunctionCallback;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by edgargomez on 1/31/18.
 */

public class NetworkingManager {

    private static NetworkingManager INSTANCE = null;

    private final String BASE_URL = "https://us-central1-luminous-inferno-3905.cloudfunctions.net/api/";

    private NetworkingManager() {}

    public static synchronized NetworkingManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new NetworkingManager();
        }
        return(INSTANCE);
    }

    private String getAbsoluteUrl(String functionName) {
        return BASE_URL + functionName;
    }

    private Headers getFirebaseHeaders(String tokenId) {
        final Headers.Builder headersBuilder = new Headers.Builder();
        headersBuilder.set("Accept", "application/json");
        headersBuilder.set("Content-Type", "text/json; Charset=UTF-8");
        headersBuilder.set("X-Conversa-Application-Id", "def");
        headersBuilder.set("X-Conversa-Client-Version", BuildConfig.VERSION_NAME);
        headersBuilder.set("X-Conversa-Client-Key", "fdas");
        headersBuilder.set("Authorization", "Bearer " + tokenId);
        return headersBuilder.build();
    }

    private String getUrlWithQueries(@NonNull String url, @Nullable Map<String, String> queries) {
        HttpUrl urlParse = HttpUrl.parse(url);
        if (urlParse != null) {
            HttpUrl.Builder urlBuilder = urlParse.newBuilder();

            if (queries != null) {
                for (Map.Entry<String, String> entry : queries.entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue();
                    urlBuilder.addQueryParameter(key, value);
                }
            }

            return urlBuilder.build().toString();
        } else {
            return url;
        }
    }

    public <T> void post(@NonNull final String functionName, @NonNull final HashMap<String, ?> requestJson, @Nullable final FunctionCallback<T> callback) {
        HashMap<String, String> body = new HashMap<>(requestJson.size());

        for (Map.Entry<String, ?> entry : requestJson.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            body.put(key, value.toString());
        }

        AndroidNetworking.post(getAbsoluteUrl(functionName))
                .addBodyParameter(body)
                .addHeaders(getFirebaseHeaders(ConversaApp.getInstance(null).getPreferences().getFirebaseToken()))
                .setPriority(Priority.HIGH)
                .build()
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {
                        Handler handler = new Handler(Looper.getMainLooper());

                        try {
                            final JSONArray results = new JSONArray(response);
                            if (callback != null) {
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        callback.done((T)results, null);
                                    }
                                });
                            }
                            return;
                        } catch (JSONException ignored) {}

                        try {
                            final JSONObject results = new JSONObject(response);
                            if (callback != null) {
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        callback.done((T)results, null);
                                    }
                                });
                            }
                            return;
                        } catch (JSONException ignored) {}

                        if (callback != null) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    callback.done(null, new FirebaseCustomException(1, "Couldn't parse json string"));
                                }
                            });
                        }
                    }

                    @Override
                    public void onError(final ANError anError) {
                        if (callback != null) {
                            Handler handler = new Handler(Looper.getMainLooper());

                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    callback.done(null, new FirebaseCustomException(anError));
                                }
                            });
                        }
                    }
                });
    }

    public void get(@NonNull final String functionName, @NonNull final HashMap<String, String> requestJson, @Nullable final FunctionCallback callback) {
        AndroidNetworking.get(getUrlWithQueries(getAbsoluteUrl(functionName), requestJson))
                .addHeaders(getFirebaseHeaders(ConversaApp.getInstance(null).getPreferences().getFirebaseToken()))
                .setPriority(Priority.HIGH)
                .build()
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {
                        Handler handler = new Handler(Looper.getMainLooper());

                        try {
                            final JSONArray results = new JSONArray(response);
                            if (callback != null) {
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        callback.done(results, null);
                                    }
                                });
                            }
                            return;
                        } catch (JSONException ignored) {}

                        try {
                            final JSONObject results = new JSONObject(response);
                            if (callback != null) {
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        callback.done(results, null);
                                    }
                                });
                            }
                            return;
                        } catch (JSONException ignored) {}

                        if (callback != null) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    callback.done(null, new FirebaseCustomException(1, "Couldn't parse json string"));
                                }
                            });
                        }
                    }

                    @Override
                    public void onError(final ANError anError) {
                        if (callback != null) {
                            Handler handler = new Handler(Looper.getMainLooper());

                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    callback.done(null, new FirebaseCustomException(anError));
                                }
                            });
                        }
                    }
                });
    }

    @WorkerThread
    public <T> T postSync(@NonNull final String functionName, @NonNull final HashMap<String, ?> requestJson) throws FirebaseCustomException {
        String token = "";

        HashMap<String, String> body = new HashMap<>(requestJson.size());

        for (Map.Entry<String, ?> entry : requestJson.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            body.put(key, value.toString());
        }

        ANRequest request = AndroidNetworking.post(getAbsoluteUrl(functionName))
                .addHeaders(getFirebaseHeaders(token))
                .addBodyParameter(body)
                .build();

        ANResponse response = request.executeForString();

        if (response.isSuccess()) {
            String json = response.getResult().toString();

            try {
                return (T) new JSONArray(json);
            } catch (Exception ignored) {}

            try {
                return (T) new JSONObject(json);
            } catch (Exception ignored) {}

            return null;
        } else {
            ANError error = response.getError();
            throw new FirebaseCustomException(error);
        }
    }

    public <T> T getSync(@NonNull final String functionName, @NonNull final HashMap<String, String> requestJson) throws FirebaseCustomException {
        String token = "";

        ANRequest request = AndroidNetworking.get(getUrlWithQueries(getAbsoluteUrl(functionName), requestJson))
                .addHeaders(getFirebaseHeaders(token))
                .build();

        ANResponse response = request.executeForString();

        if (response.isSuccess()) {
            String json = response.getResult().toString();

            try {
                return (T) new JSONArray(json);
            } catch (Exception ignored) {}

            try {
                return (T) new JSONObject(json);
            } catch (Exception ignored) {}

            return null;
        } else {
            ANError error = response.getError();
            throw new FirebaseCustomException(error);
        }
    }

}
