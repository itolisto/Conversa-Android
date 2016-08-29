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

import java.util.HashMap;

import ee.app.conversa.extendables.ConversaActivity;
import ee.app.conversa.model.database.dBusiness;
import ee.app.conversa.utils.Const;

public class ActivityProfile extends ConversaActivity implements View.OnClickListener, OnLikeListener {

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
        final LikeButton mBtnFavorite = (LikeButton) findViewById(R.id.btnFavorite);
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
        ParseCloud.callFunctionInBackground("isFavorite", params, new FunctionCallback<Boolean>() {
            @Override
            public void done(Boolean result, ParseException e) {
                if(e == null) {
                    if (result) {
                        mBtnFavorite.setLiked(true);
                    } else {
                        mBtnFavorite.setLiked(false);
                    }
                } else {
                    mBtnFavorite.setLiked(false);
                }

                mBtnFavorite.setEnabled(true);
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
}
