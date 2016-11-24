package ee.app.conversa.holders;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.View;

/**
 * Created by edgargomez on 10/31/16.
 */

public class BaseHolder extends ViewHolder implements View.OnClickListener, View.OnLongClickListener {

    protected final AppCompatActivity activity;

    public BaseHolder(View itemView, AppCompatActivity activity) {
        super(itemView);
        this.activity = activity;
    }

    @Override
    public void onClick(View v) { }

    @Override
    public boolean onLongClick(View v) { return false; }

}
