package ee.app.conversa.dialog;

import android.app.Dialog;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import ee.app.conversa.ConversaApp;
import ee.app.conversa.R;
import ee.app.conversa.adapters.ChatsAdapter;

/**
 * Created by gomez on 06/01/2015.
 */
public class DeleteUserDialog extends Dialog implements View.OnClickListener {

    private Button delete;
    private Button no_delete;

    Context context;
    String userId;
    int position;
    ChatsAdapter adapter;

    public DeleteUserDialog(ChatsAdapter usersChatAdapter, AppCompatActivity mActivity, String id, int i) {//, String type) {
        super(mActivity, android.R.style.Theme_Translucent_NoTitleBar);
        setContentView(R.layout.dialog_delete_user);

        adapter = usersChatAdapter;
        context = mActivity;
        userId = id;
        position = i;

        delete = (Button) findViewById(R.id.delete);
        no_delete = (Button) findViewById(R.id.dont_delete);

        delete.setTypeface(ConversaApp.getTfRalewayRegular());
        no_delete.setTypeface(ConversaApp.getTfRalewayRegular());

        delete.setOnClickListener(this);
        no_delete.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.equals(delete)) {
            removeContact();
        } else {
            if(v.equals(no_delete)) {
                DeleteUserDialog.this.dismiss();
            }
        }
    }

    void removeContact() {
        if (userId != null) {
//            CouchDB.removeUserContactAsync(userId, new DeleteResultListener(), context, true);
        } else {
            DeleteUserDialog.this.dismiss();
            Toast.makeText(context, context.getString(R.string.delete_error1), Toast.LENGTH_SHORT).show();
        }
    }

//    class DeleteResultListener implements ResultListener<Boolean> {
//
//        @Override
//        public void onResultsSuccess(Boolean result) {
//            DeleteUserDialog.this.dismiss();
//            if(result) {
//                //Refrescar lista de usuarios
//                if(adapter != null) {
//                    Toast.makeText(context, context.getString(R.string.delete_successful), Toast.LENGTH_SHORT).show();
//                    adapter.removeItem(position);
//                    ConversaApp.getDB().deleteContactById(userId);
//                    if(adapter.getItemCount() == 0 && FragmentUsersChat.mLvUsers != null && FragmentUsersChat.mLvUsers != null) {
//                        FragmentUsersChat.mIvNoUsers.setVisibility(View.VISIBLE);
//                        FragmentUsersChat.mLvUsers.setVisibility(View.GONE);
//                    }
//                } else {
//                    Toast.makeText(context, context.getString(R.string.delete_error2), Toast.LENGTH_SHORT).show();
//                }
//            } else {
//                Toast.makeText(context, context.getString(R.string.delete_error1), Toast.LENGTH_SHORT).show();
//            }
//        }
//
//        @Override
//        public void onResultsFail() {
//            DeleteUserDialog.this.dismiss();
//            Toast.makeText(context, context.getString(R.string.delete_successful), Toast.LENGTH_SHORT).show();
////            Snackbar.make(context, context.getString(R.string.delete_successful), Snackbar.LENGTH_SHORT).show();
//            adapter.removeItem(position);
//            ConversaApp.getDB().deleteContactById(userId);
//            if(adapter.getItemCount() == 0 && FragmentUsersChat.mLvUsers != null && FragmentUsersChat.mLvUsers != null) {
//                FragmentUsersChat.mIvNoUsers.setVisibility(View.VISIBLE);
//                FragmentUsersChat.mLvUsers.setVisibility(View.GONE);
//            }
//        }
//    }
}
