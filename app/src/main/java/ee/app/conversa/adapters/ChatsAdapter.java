package ee.app.conversa.adapters;

import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import ee.app.conversa.R;
import ee.app.conversa.holders.ChatsViewHolder;
import ee.app.conversa.interfaces.OnContactClickListener;
import ee.app.conversa.interfaces.OnContactLongClickListener;
import ee.app.conversa.model.database.dbBusiness;

/**
 * ChatsAdapter class was implemented using https://github.com/writtmeyer/recyclerviewdemo
 * along with the post from Wolfram Rittmeyer which you could find at
 * http://www.grokkingandroid.com/first-glance-androids-recyclerview/
 *
 */
public class ChatsAdapter extends RecyclerView.Adapter<ChatsViewHolder> {

    private List<dbBusiness> mUsers;
    private OnContactClickListener listener;
    private OnContactLongClickListener longlistener;
    private SparseBooleanArray mSelectedPositions;
    private AppCompatActivity mActivity;

    public ChatsAdapter(AppCompatActivity activity, OnContactClickListener listener, OnContactLongClickListener longlistener) {
        this.mUsers = new ArrayList<>();
        this.mActivity = activity;
        this.listener = listener;
        this.longlistener = longlistener;
        this.mSelectedPositions = new SparseBooleanArray(1);
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    @Override
    public ChatsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ChatsViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(R.layout.user_item, parent, false),
                mActivity,
                listener,
                longlistener);
    }

    @Override
    public void onBindViewHolder(ChatsViewHolder holder, int position, List<Object> payloads) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads);
        } else {
            if (payloads.size() > 0) {
                if (payloads.get(0) instanceof String) {
                    switch ((String)payloads.get(0)) {
                        case "toggleActivate": {
                            holder.toggleActivate();
                            break;
                        }
                        case "updatePosition" : {
                            holder.updateLastMessage(mUsers.get(position).getBusinessId());
                            break;
                        }
                        case "updateLastMessage": {
                            holder.updateLastMessage(mUsers.get(position).getBusinessId());
                            break;
                        }
                        case "updateView": {
                            holder.updateView();
                            break;
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onBindViewHolder(ChatsViewHolder holder, int position) {
        holder.setContact(mUsers.get(position), position, mSelectedPositions);
    }

    // get the number of currently selected items
    public int getSelectedItemCount() {
        return mSelectedPositions.size();
    }

    // Currently selected items.
    public ArrayList<String> getSelectedItems() {
        ArrayList<String> items = new ArrayList<>(mSelectedPositions.size());
        for (int i = 0; i < mSelectedPositions.size(); i++) {
            items.add(Long.toString(mUsers.get(mSelectedPositions.keyAt(i)).getId()));
        }
        return items;
    }

    // Item changes its selection state
    public void toggleSelection(int position) {
        if (mSelectedPositions.get(position, false)) {
            mSelectedPositions.delete(position);
        } else {
            mSelectedPositions.put(position, true);
        }
        notifyItemChanged(position, "toggleActivate");
    }

    // Clear all selections
    public void clearSelections() {
        for (int i = 0; i < mSelectedPositions.size(); i++) {
            notifyItemChanged(mSelectedPositions.keyAt(i), "toggleActivate");
        }
        mSelectedPositions.clear();
    }

    public void clearItems() {
        mUsers.clear();
    }

    public void setItems(List<dbBusiness> users) {
        mUsers = users;
        notifyDataSetChanged();
    }

    public void addContact(dbBusiness user) {
        mUsers.add(0, user);
        notifyItemInserted(0);
    }

    public void updateContactPosition(String businessId) {
        for (int i = 0; i < mUsers.size(); i++) {
            if (mUsers.get(i).getBusinessId().equals(businessId)) {
                if (i > 0) {
                    mUsers.add(0, mUsers.remove(i));
                    notifyItemMoved(i, 0);
                }
                notifyItemChanged(0, "updatePosition");
                break;
            }
        }
    }

    public void updateContactLastMessage(String businessId) {
        for (int i = 0; i < mUsers.size(); i++) {
            if (mUsers.get(i).getBusinessId().equals(businessId)) {
                notifyItemChanged(i, "updateLastMessage");
                break;
            }
        }
    }

    public void updateContactView(String businessId) {
        for (int i = 0; i < mUsers.size(); i++) {
            if (mUsers.get(i).getBusinessId().equals(businessId)) {
                notifyItemChanged(i, "updateView");
                break;
            }
        }
    }

    public void updateContactRead(String businessId) {
        for (int i = 0; i < mUsers.size(); i++) {
            if (mUsers.get(i).getBusinessId().equals(businessId)) {
                notifyItemChanged(i, "updateUnread");
                break;
            }
        }
    }

    public void removeContacts() {
        List<String> positionsById = getSelectedItems();

        int i;
        while (true) {
            i = 0;
            while(true) {
                if (mUsers.get(i).getId() == Long.parseLong(positionsById.get(0))) {
                    mUsers.remove(i);
                    positionsById.remove(0);
                    notifyItemRemoved(i);
                    break;
                } else {
                    i++;
                }
            }

            if (positionsById.size() == 0)
                break;
        }

        mSelectedPositions.clear();
    }

}