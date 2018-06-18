package ee.app.conversa.networking;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import ee.app.conversa.BuildConfig;
import ee.app.conversa.interfaces.FunctionCallback;
import ee.app.conversa.interfaces.OnCompleteFileFunction;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by edgargomez on 1/31/18.
 */

public class NetworkingManager {

    private static NetworkingManager INSTANCE = null;

    private final String BASE_URL = "https://us-central1-luminous-inferno-3905.cloudfunctions.net/api/";
    private final MediaType MEDIA_JSON = MediaType.parse("application/json; charset=utf-8");
    private final MediaType MEDIA_IMAGE = MediaType.parse("image/png");

    private final int READ_TIMEOUT_IN_MINUTE = 2;
    private final int WRITE_TIMEOUT_IN_MINUTE = 2;
    private final int CONNECTION_TIMEOUT_IN_MINUTE = 3;

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

    private OkHttpClient configureClient() {
        OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder();
        httpClientBuilder.readTimeout(READ_TIMEOUT_IN_MINUTE, TimeUnit.MINUTES);
        httpClientBuilder.writeTimeout(WRITE_TIMEOUT_IN_MINUTE, TimeUnit.MINUTES);
        httpClientBuilder.connectTimeout(CONNECTION_TIMEOUT_IN_MINUTE, TimeUnit.MINUTES);
        httpClientBuilder.retryOnConnectionFailure(false);
        return httpClientBuilder.build();
    }

    private Headers getFirebaseHeaders(String tokenId) {
        final Headers.Builder headersBuilder = new Headers.Builder();
        headersBuilder.set("Accept", "application/json");
        headersBuilder.set("Content-Type", "text/json; Charset=UTF-8");
        headersBuilder.set("X-Conversa-Application-Id", "def");
        headersBuilder.set("X-Conversa-Client-Version", BuildConfig.VERSION_NAME);
        headersBuilder.set("X-Conversa-Client-Key", "fdas");
        headersBuilder.set("Authorization", "Bearer " + ((tokenId == null) ? "" : tokenId));
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
        FirebaseUser current = FirebaseAuth.getInstance().getCurrentUser();

        final FormBody.Builder body = new FormBody.Builder();

        for (Map.Entry<String, ?> entry : requestJson.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            body.add(key, value.toString());
        }

        if (current != null) {
            current.getIdToken(true).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                @Override
                public void onComplete(@NonNull Task<GetTokenResult> task) {
                    //RequestBody body = RequestBody.create(MEDIA_JSON, requestJson.toString());

                    Request request = new Request.Builder()
                            .headers(getFirebaseHeaders(task.getResult().getToken()))
                            .url(getAbsoluteUrl(functionName))
                            .post(body.build())
                            .build();
                    execute(request, callback);
                }
            });
        } else {
            Request request = new Request.Builder()
                    .headers(getFirebaseHeaders(""))
                    .url(getAbsoluteUrl(functionName))
                    .post(body.build())
                    .build();
            execute(request, callback);
        }
    }

    @WorkerThread
    public <T> T postSync(@NonNull final String functionName, @NonNull final HashMap<String, ?> requestJson) throws FirebaseCustomException{
        FirebaseUser current = FirebaseAuth.getInstance().getCurrentUser();
        String token = "";

        if (current != null) {
            token = current.getIdToken(false).getResult().getToken();
        }

        //RequestBody body = RequestBody.create(MEDIA_JSON, requestJson.toString());
        FormBody.Builder body = new FormBody.Builder();

        for (Map.Entry<String, ?> entry : requestJson.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            body.add(key, value.toString());
        }

        Request request = new Request.Builder()
                .headers(getFirebaseHeaders(token))
                .url(getAbsoluteUrl(functionName))
                .post(body.build())
                .build();
        return executeSync(request);
    }

    public void get(@NonNull final String functionName, @NonNull final HashMap<String, String> requestJson, @Nullable final FunctionCallback callback) {
        FirebaseUser current = FirebaseAuth.getInstance().getCurrentUser();
        if (current != null) {
            current.getIdToken(true).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                @Override
                public void onComplete(@NonNull Task<GetTokenResult> task) {
                    Request request = new Request.Builder()
                            .headers(getFirebaseHeaders(task.getResult().getToken()))
                            .url(getUrlWithQueries(getAbsoluteUrl(functionName), requestJson))
                            .get()
                            .build();
                    execute(request, callback);
                }
            });
        } else {
            Request request = new Request.Builder()
                    .headers(getFirebaseHeaders(""))
                    .url(getAbsoluteUrl(functionName))
                    .get()
                    .build();
            execute(request, callback);
        }
    }

    public <T> T getSync(@NonNull final String functionName, @NonNull final HashMap<String, String> requestJson) throws FirebaseCustomException {
        FirebaseUser current = FirebaseAuth.getInstance().getCurrentUser();
        String token = "";

        if (current != null) {
            token = current.getIdToken(true).getResult().getToken();
        }

        Request request = new Request.Builder()
                .headers(getFirebaseHeaders(token))
                .url(getUrlWithQueries(getAbsoluteUrl(functionName), requestJson))
                .get()
                .build();
        return (T) executeSync(request);
    }

    private void execute(@NonNull final Request request, @Nullable final FunctionCallback callback) {
        try {
            OkHttpClient client = configureClient();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, final IOException e) {
                    call.cancel();
                    if (callback != null) {
                        Handler handler = new Handler(Looper.getMainLooper());

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                callback.done(null, new FirebaseCustomException(e));
                            }
                        });
                    }
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String json = response.body().string().trim();
                        Handler handler = new Handler(Looper.getMainLooper());

                        try {
                            final JSONArray results = new JSONArray(json);
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
                            final JSONObject results = new JSONObject(json);
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
                    } else {
                        if (callback != null) {
                            Handler handler = new Handler(Looper.getMainLooper());

                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    callback.done(null, new FirebaseCustomException(1, "Internal server error"));
                                }
                            });
                        }
                    }
                }
            });
        } catch (@NonNull IllegalStateException e) {
            if (callback != null)
                callback.done(null, new FirebaseCustomException(e));
        }
    }

    private <T> T executeSync(@NonNull final Request request) throws FirebaseCustomException {
        try {
            OkHttpClient client = configureClient();
            Response response = client.newCall(request).execute();

            if (response.isSuccessful()) {
                String json = "";

                if (response.body() != null)
                    json = response.body().string().trim();

                Object results;

                try {
                    results = new JSONArray(json);
                    return (T) results;
                } catch (JSONException ignored) {}

                try {
                    results = new JSONObject(json);
                    return (T) results;
                } catch (JSONException ignored) {}

                return null;
            } else {
                throw new IOException("Unexpected code " + response);
            }
        } catch (@NonNull IllegalStateException | IOException ignored) {
            return null;
        }
    }

}
