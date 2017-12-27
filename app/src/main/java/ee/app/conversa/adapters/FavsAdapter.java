package ee.app.conversa.adapters;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;

import com.facebook.drawee.view.SimpleDraweeView;

import java.util.ArrayList;
import java.util.List;

import ee.app.conversa.R;
import ee.app.conversa.holders.BaseHolder;
import ee.app.conversa.holders.NHeaderViewHolder;
import ee.app.conversa.interfaces.OnContactClickListener;
import ee.app.conversa.interfaces.OnFavoriteClickListener;
import ee.app.conversa.model.Favorite;
import ee.app.conversa.utils.Logger;
import ee.app.conversa.utils.Utils;
import ee.app.conversa.view.MediumTextView;

/**
 * Created by root on 2/12/17.
 */

public class FavsAdapter extends BaseAdapter implements AdapterView.OnItemClickListener
{

    private final AppCompatActivity mActivity;
    private List<Object> mFavorites;
    private OnFavoriteClickListener localListener;

    public FavsAdapter(AppCompatActivity activity, OnFavoriteClickListener localListener, GridView gridView) {
        this.mActivity = activity;
        this.mFavorites = new ArrayList<>();
        this.localListener = localListener;
        gridView.setOnItemClickListener(this);
    }

    @Override
    public int getCount() {
        return mFavorites.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Favorite favorite = (Favorite)mFavorites.get(position);

        if (convertView == null) {
            final LayoutInflater layoutInflater = LayoutInflater.from(mActivity);
            convertView = layoutInflater.inflate(R.layout.favorite_item, null);
        }

        final SimpleDraweeView imageView = (SimpleDraweeView) convertView.findViewById(R.id.sdvFavoriteImage);
        final MediumTextView nameTextView = (MediumTextView) convertView.findViewById(R.id.mtvFavoriteDisplayName);

        Uri uri = Utils.getUriFromString(favorite.getAvatarUrl());

        if (uri == null) {
            uri = Utils.getDefaultImage(mActivity, R.drawable.ic_business_default);
        }

        imageView.setImageURI(uri);
        nameTextView.setText(favorite.getBusinessName());

        return convertView;
    }

    public void addLoad(boolean show) {
        int position = mFavorites.size();
        if (show) {
            mFavorites.add(position, new Object());
            notifyDataSetChanged();
            //notifyItemInserted(position);
        } else {
            mFavorites.remove(position - 1);
            notifyDataSetChanged();
        }
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Logger.error("FAVSADAPER", "LocalListener: " + localListener);
        if (localListener != null) {
            localListener.onFavoriteClick( (Favorite)mFavorites.get(position), view, position);
        }
    }

    public void addItems(List<Favorite> favorites) {
        this.mFavorites.addAll(favorites);
        notifyDataSetChanged();
    }

    public void addFavorites(List<Favorite> favorites) {
        mFavorites.addAll(favorites);
        notifyDataSetChanged();
    }

}
