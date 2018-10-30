package ee.app.conversa.holders;

import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by edgargomez on 10/31/16.
 */

public class BaseHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

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
