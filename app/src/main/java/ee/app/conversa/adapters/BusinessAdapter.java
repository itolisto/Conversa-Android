package ee.app.conversa.adapters;

import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ee.app.conversa.R;
import ee.app.conversa.model.Database.dBusiness;
import ee.app.conversa.view.CircleImageView;

public class BusinessAdapter extends RecyclerView.Adapter<BusinessAdapter.ViewHolder>{

    private AppCompatActivity mActivity;
    private List<dBusiness> mBusiness = new ArrayList<>();
    private BusinessAdapter adapter;
    private List<Fav> favBusiness;

    public void clearFavBusiness(){ favBusiness.clear(); }

    public BusinessAdapter(AppCompatActivity activity, List<dBusiness> business) {
        mBusiness = business;
        mActivity = activity;
        favBusiness = new ArrayList<>();
        adapter = this;
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int i) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.business_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public int getItemCount() {
        return (mBusiness == null) ? 0 : mBusiness.size();
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int i) {
        dBusiness business = mBusiness.get(i);

//        Utils.displayImage(business.getmAvatarThumbFileId(), Const.BUSINESS_FOLDER, holder.ivBusinessBackground,
//                null, ImageLoader.SMALL, R.drawable.business_default, false);

        holder.tvBusiness.setText(business.getDisplayName());
        holder.tvAbout.setText(business.getAbout());

        if (business.isFavorite()) {
            if (Build.VERSION.SDK_INT >= 16) {
                holder.ivFavorite.setBackground(mActivity.getResources().getDrawable(R.drawable.fav));
            } else {
                holder.ivFavorite.setBackgroundDrawable(mActivity.getResources().getDrawable(R.drawable.fav));
            }
        } else {
            if (Build.VERSION.SDK_INT >= 16) {
                holder.ivFavorite.setBackground(mActivity.getResources().getDrawable(R.drawable.fav_not));
            } else {
                holder.ivFavorite.setBackgroundDrawable(mActivity.getResources().getDrawable(R.drawable.fav_not));
            }
        }

        if (favBusiness.size() > 0) {
            for (Fav commerce : favBusiness) {
                if (commerce.getPosition() == i) {
                    if (Build.VERSION.SDK_INT >= 16) {
                        holder.ivFavorite.setBackground(mActivity.getResources().getDrawable(R.drawable.fav));
                    } else {
                        holder.ivFavorite.setBackgroundDrawable(mActivity.getResources().getDrawable(R.drawable.fav));
                    }
                }
            }
        }
    }

    public void setItems(List<dBusiness> business) { mBusiness = business; }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        public CircleImageView ivBusinessBackground;
        public TextView tvBusiness;
        public TextView tvAbout;
        public ImageView ivFavorite;
        public ImageView ivStartChat;

        public ViewHolder(View itemView) {
            super(itemView);

            this.ivBusinessBackground = (CircleImageView) itemView
                    .findViewById(R.id.ivBusinessBackground);
            this.ivFavorite = (ImageView) itemView
                    .findViewById(R.id.ivFavorite);
            this.ivStartChat = (ImageView) itemView
                    .findViewById(R.id.ivStartChat);
            this.tvBusiness = (TextView) itemView
                    .findViewById(R.id.tvBusiness);
            this.tvAbout = (TextView) itemView
                    .findViewById(R.id.tvBusinessAbout);

            //LayoutHelper.scaleWidthAndHeightAbsolute(mActivity, 2.5f, this.ivBusinessBackground);

            ivFavorite.setOnClickListener(this);
            ivStartChat.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
//            int position = getPosition();
//            dBusiness business = mBusiness.get(position);
//
//            if(view.getId() == R.id.ivFavorite) {
//                boolean toFav = business.ismFavorite();
//
//                for (Fav commerce : favBusiness) {
//                    if(commerce.getPosition() == position){ toFav = false; }
//                }
//
//                if(toFav) {
//                    CouchDB.addFavoriteCommerceAsync(
//                            business.getmId(), new AddRemoveFavoriteFinish("add", view, position), mActivity, false
//                    );
//                } else {
//                    CouchDB.removeFavoriteCommerceAsync(
//                            business.getmId(), new AddRemoveFavoriteFinish("remove", view, position), mActivity, false
//                    );
//                }
//            } else {
//                if(view.getId() == R.id.ivStartChat) {
//                    CouchDB.addUserContactAsync(business.getmId(), new AddContactFinish(), mActivity, false);
//                }
//            }
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
//            ImageView image = (ImageView) view.findViewById( R.id.ivFavorite );
//            if (result) {
//                if(action.equals("add")) {
//                    if (Build.VERSION.SDK_INT >= 16) {
//                        image.setBackground(mActivity.getResources().getDrawable(R.drawable.fav));
//                    } else {
//                        image.setBackgroundDrawable(mActivity.getResources().getDrawable(R.drawable.fav));
//                    }
//                    //Agregar a lista
//                    Fav commerce = new Fav(position);
//                    favBusiness.add(commerce);
//                } else {
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
//                Logger.error("BusinessAdapter", action + ": " + position);
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
//        public AddContactFinish() { }
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

