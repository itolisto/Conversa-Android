package ee.app.conversa;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.facebook.drawee.view.SimpleDraweeView;
import com.like.LikeButton;
import com.like.OnLikeListener;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import ee.app.conversa.extendables.ConversaActivity;
import ee.app.conversa.model.database.dBusiness;
import ee.app.conversa.utils.Const;
import ee.app.conversa.utils.Logger;

public class ActivityProfile extends ConversaActivity implements View.OnClickListener, OnLikeListener {

    private LikeButton mBtnFavorite;
    private dBusiness businessObject;
    private boolean addAsContact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        checkInternetConnection = false;

        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null) {
                finish();
            } else {
                businessObject = extras.getParcelable(Const.kClassBusiness);
                addAsContact = extras.getBoolean(Const.kYapDatabaseName);
            }
        } else {
            businessObject = savedInstanceState.getParcelable(Const.kClassBusiness);
            addAsContact = savedInstanceState.getBoolean(Const.kYapDatabaseName);
        }

        initialization();
    }

    @Override
    protected void initialization() {
        SimpleDraweeView mSdvBusinessImage = (SimpleDraweeView) findViewById(R.id.sdvBusinessImage);
        mBtnFavorite = (LikeButton) findViewById(R.id.btnFavorite);
        Button mBtnStartChat = (Button) findViewById(R.id.btnStartChat);

        if(businessObject.getAvatarThumbFileId().isEmpty()) {
            Uri path = Uri.parse("android.resource://ee.app.conversa/" + R.drawable.business_default);
            mSdvBusinessImage.setImageURI(path);
        } else {
            Uri uri = Uri.parse(businessObject.getAvatarThumbFileId());
            mSdvBusinessImage.setImageURI(uri);
        }

        mBtnFavorite.setLiked(false);
        mBtnFavorite.setEnabled(false);
        // Call Parse for registry
        HashMap<String, String> params = new HashMap<>();
        params.put("business", businessObject.getBusinessId());
        ParseCloud.callFunctionInBackground("profileInfo", params, new FunctionCallback<String>() {
            @Override
            public void done(String result, ParseException e) {
                if(e == null) {
                    parseResult(result);
                } else {
                    parseResult("");
                }
            }
        });

        mBtnFavorite.setOnLikeListener(this);
        mBtnStartChat.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v instanceof Button) {
            switch (v.getId()) {
                case R.id.btnStartChat:
                    Intent intent = new Intent(this, ActivityChatWall.class);
                    intent.putExtra(Const.kClassBusiness, businessObject);
                    intent.putExtra(Const.kYapDatabaseName, addAsContact);
                    startActivity(intent);
                    break;
            }
        }
    }

    @Override
    public void liked(final LikeButton likeButton) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("business", businessObject.getBusinessId());
        params.put("favorite", true);
        ParseCloud.callFunctionInBackground("favorite", params, new FunctionCallback<Object>() {
            @Override
            public void done(Object object, ParseException e) {
                likeButton.setLiked(true);
            }
        });
    }

    @Override
    public void unLiked(final LikeButton likeButton) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("business", businessObject.getBusinessId());
        ParseCloud.callFunctionInBackground("favorite", params, new FunctionCallback<Object>() {
            @Override
            public void done(Object object, ParseException e) {
                likeButton.setLiked(false);
            }
        });
    }

    private void parseResult(String result) {
        try {
            if (result.isEmpty()) {
                mBtnFavorite.setLiked(false);
            } else {
                JSONObject jsonRootObject = new JSONObject(result);

                JSONArray options = jsonRootObject.optJSONArray("options");
                JSONArray tags = jsonRootObject.optJSONArray("tags");
                int followers = jsonRootObject.optInt("followers", 0);
                boolean verified = jsonRootObject.optBoolean("verified", false);
                long since = jsonRootObject.optLong("since", 0);

                if (jsonRootObject.optBoolean("favorite", false)) {
                    mBtnFavorite.setLiked(true);
                } else {
                    mBtnFavorite.setLiked(false);
                }
            }
        } catch (JSONException e) {
            Logger.error("parseResult", e.getMessage());
        } finally {
            mBtnFavorite.setEnabled(true);
        }
    }
}