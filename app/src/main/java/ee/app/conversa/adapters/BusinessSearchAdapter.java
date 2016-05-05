package ee.app.conversa.adapters;

import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ee.app.conversa.R;
import ee.app.conversa.model.Database.Business;
import ee.app.conversa.view.CircleImageView;

public class BusinessSearchAdapter extends BaseAdapter {

    private AppCompatActivity mActivity;
    private List<Business> mBusiness = new ArrayList<>();
    private BusinessSearchAdapter adapter;
    private List<Fav> favBusiness;

    public void clearFavBusiness(){ favBusiness.clear(); }

    public BusinessSearchAdapter(AppCompatActivity activity, List<Business> business) {
        mBusiness = business;
        mActivity = activity;
        favBusiness = new ArrayList<>();
        adapter = this;
    }

    @Override
    public int getCount()               { return (mBusiness == null) ? 0 : mBusiness.size(); }

    @Override
    public Object getItem(int position) { return null; }

    @Override
    public long getItemId(int position) { return 0; }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if(convertView == null) {
            // inflate the GridView item layout
            LayoutInflater inflater = LayoutInflater.from(mActivity);
            convertView = inflater.inflate(R.layout.business_item_search, parent, false);
            // initialize the view holder
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            // recycle the already inflated view
            holder = (ViewHolder) convertView.getTag();
        }

        final Business business = mBusiness.get(position);

//        Utils.displayImage(business.getmAvatarThumbFileId(), Const.BUSINESS_FOLDER, holder.ivBusinessBackground,
//                holder.pbLoadingSearchImage, ImageLoader.SMALL, R.drawable.business_default, false);

        holder.tvCategoryName.setText(business.getmTitle(mActivity));
        holder.tvName.setText(business.getDisplayName());

        if(business.isFavorite()){
            if (Build.VERSION.SDK_INT >= 16) {
                holder.ivFavorite.setBackground(mActivity.getResources().getDrawable(R.drawable.fav));
            } else {
                holder.ivFavorite.setBackgroundDrawable(mActivity.getResources().getDrawable(R.drawable.fav));
            }
        }else{
            if (Build.VERSION.SDK_INT >= 16) {
                holder.ivFavorite.setBackground(mActivity.getResources().getDrawable(R.drawable.fav_not));
            } else {
                holder.ivFavorite.setBackgroundDrawable(mActivity.getResources().getDrawable(R.drawable.fav_not));
            }
        }

        if(favBusiness.size() > 0) {
            for (Fav commerce : favBusiness) {
                if (commerce.getPosition() == position) {
                    if (Build.VERSION.SDK_INT >= 16) {
                        holder.ivFavorite.setBackground(mActivity.getResources().getDrawable(R.drawable.fav));
                    } else {
                        holder.ivFavorite.setBackgroundDrawable(mActivity.getResources().getDrawable(R.drawable.fav));
                    }
                }
            }
        }

        holder.ivFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean toFav = true;

                if(business.isFavorite())
                    toFav = false;
                else
                    toFav = true;

                for (Fav commerce : favBusiness) {
                    if(commerce.getPosition() == position){ toFav = false; }
                }

                if(toFav) {
//                    CouchDB.addFavoriteCommerceAsync(
//                            business.getmId(), new AddRemoveFavoriteFinish("add", view, position), mActivity, true
//                    );
                }else{
//                    CouchDB.removeFavoriteCommerceAsync(
//                            business.getmId(), new AddRemoveFavoriteFinish("remove", view, position), mActivity, true
//                    );
                }
            }
        });

        holder.ivStartChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                CouchDB.addUserContactAsync(business.getmId(), new AddContactFinish(position), mActivity, true);
            }
        });

        return convertView;
    }

    public void setItems(List<Business> business) { mBusiness = business; }

    public class ViewHolder {

        public CircleImageView ivBusinessBackground;
        public ProgressBar pbLoadingSearchImage;
        public TextView tvCategoryName;
        public TextView tvName;
        public ImageView ivFavorite;
        public ImageView ivStartChat;

        public ViewHolder(View itemView) {
            super();

            this.ivBusinessBackground = (CircleImageView) itemView
                    .findViewById(R.id.ivBusinessImageSearch);
            this.pbLoadingSearchImage = (ProgressBar) itemView
                    .findViewById(R.id.pbLoadingProgressBarSearch);
            this.ivFavorite = (ImageView) itemView
                    .findViewById(R.id.ivFavoriteSearch);
            this.ivStartChat = (ImageView) itemView
                    .findViewById(R.id.ivStartChatSearch);
            this.tvName = (TextView) itemView
                    .findViewById(R.id.tvNameSearch);
            this.tvCategoryName = (TextView) itemView
                    .findViewById(R.id.tvCategorySearch);
        }
    }

//    private class AddRemoveFavoriteFinish implements ResultListener<Boolean> {
//
//        String action;
//        View view;
//        int position;
//
//        public AddRemoveFavoriteFinish (String action, View view, int position) {
//            this.action = action;
//            this.view = view;
//            this.position = position;
//        }
//
//        @Override
//        public void onResultsSuccess(Boolean result) {
//            ImageView image = (ImageView) view.findViewById( R.id.ivFavoriteSearch );
//            if (result) {
//                if(action.equals("add")){
//                    if (Build.VERSION.SDK_INT >= 16) {
//                        image.setBackground(mActivity.getResources().getDrawable(R.drawable.fav));
//                    } else {
//                        image.setBackgroundDrawable(mActivity.getResources().getDrawable(R.drawable.fav));
//                    }
//                    //Agregar a lista
//                    Fav commerce = new Fav(position);
//                    favBusiness.add(commerce);
//                }else{
//                    if (Build.VERSION.SDK_INT >= 16) {
//                        image.setBackground(mActivity.getResources().getDrawable(R.drawable.fav_not));
//                    } else {
//                        image.setBackgroundDrawable(mActivity.getResources().getDrawable(R.drawable.fav_not));
//                    }
//                    //Eliminar de lista
//                    int i = 0;
//                    for (Fav commerce : favBusiness) {
//                        if(commerce.getPosition() == position){ favBusiness.remove(i); break; }
//                        i++;
//                    }
//                }
//            } else {
//                Toast.makeText(mActivity, mActivity.getString(R.string.fav_error), Toast.LENGTH_SHORT).show();
//            }
//        }
//
//        @Override
//        public void onResultsFail() {
//            Toast.makeText(mActivity, mActivity.getString(R.string.fav_error), Toast.LENGTH_SHORT).show();
//        }
//    }
//
//    private class AddContactFinish implements ResultListener<User> {
//
//        int position;
//
//        public AddContactFinish(int position) { this.position = position; }
//
//        @Override
//        public void onResultsSuccess(User result) {
//            if (result != null) {
//                Context context = mActivity;
//                UsersManagement.setToUser(result);
//
//                SettingsManager.ResetSettings();
//                if(ActivityChatWall.gCurrentMessages != null)
//                    ActivityChatWall.gCurrentMessages.clear();
//
//                context.startActivity(new Intent(context, ActivityChatWall.class));
//            } else {
//                Toast.makeText(mActivity, mActivity.getString(R.string.adding_contact_error), Toast.LENGTH_SHORT).show();
//            }
//        }
//
//        @Override
//        public void onResultsFail() {
//            Toast.makeText(mActivity, mActivity.getString(R.string.adding_contact_error), Toast.LENGTH_SHORT).show();
//        }
//    }

    // Es necesario ya que cuando se obtienen los negocios se envie cuales son favoritos
    // pero si escojo uno que aun no era favorito tengo que saber cual es su posicion porque
    // cuando se reciclan las vistas y no se cual era su posicion, todas las vistas en que se
    // reciclen usando esa vista van a tener favorito.
    class Fav{
        private int position;
        public Fav() {}
        public Fav(int position) { this.position = position; }
        public int getPosition() { return position; }
        public void setPosition(int position) { this.position = position; }
    }
}

