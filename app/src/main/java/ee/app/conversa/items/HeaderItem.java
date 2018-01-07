package ee.app.conversa.items;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import ee.app.conversa.R;
import ee.app.conversa.holders.HeaderViewHolder;
import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractHeaderItem;

/**
 * Created by edgargomez on 2/20/17.
 */

public class HeaderItem extends AbstractHeaderItem<HeaderViewHolder>
{

    final private String id;
    final private String title;

    public HeaderItem(String id, String title) {
        super();
        setHidden(false);
        this.id = id;
        this.title = title;
    }

    @Override
    public boolean equals(Object inObject) {
        if (inObject instanceof HeaderItem) {
            HeaderItem inItem = (HeaderItem) inObject;
            return this.getId().equals(inItem.getId());
        }
        return false;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return (title == null) ? "Encabezado" : title;
    }

    @Override
    public boolean isSelectable() {
        return false;
    }

    @Override
    public boolean isEnabled() {
        return false;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.category_header;
    }

    @Override
    public HeaderViewHolder createViewHolder(View view, FlexibleAdapter adapter) {
        return new HeaderViewHolder(view, adapter);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void bindViewHolder(FlexibleAdapter adapter, HeaderViewHolder holder, int position, List payloads) {
        if (payloads.size() == 0) {
            holder.mRtvHeader.setText(getTitle());
        }
    }

}
