package ee.app.conversa.items;

import android.support.v7.app.AppCompatActivity;
import android.view.View;

import java.util.List;

import ee.app.conversa.R;
import ee.app.conversa.holders.CategoryViewHolder;
import ee.app.conversa.interfaces.OnCategoryClickListener;
import ee.app.conversa.model.nCategory;
import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractSectionableItem;

/**
 * Created by edgargomez on 2/20/17.
 */

public class SectionableItem extends AbstractSectionableItem<CategoryViewHolder, HeaderItem>
{

    private AppCompatActivity activity;
    private OnCategoryClickListener listener;
    final private nCategory category;

    public SectionableItem(HeaderItem header, AppCompatActivity activity, OnCategoryClickListener listener, nCategory category) {
        super(header);
        this.activity = activity;
        this.listener = listener;
        this.category = category;
    }

    @Override
    public boolean equals(Object inObject) {
        return false;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.category_item;
    }

    @Override
    public CategoryViewHolder createViewHolder(View view, FlexibleAdapter adapter) {
        return new CategoryViewHolder(view, adapter, activity, listener);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void bindViewHolder(final FlexibleAdapter adapter, CategoryViewHolder holder, int position, List payloads) {
        if (payloads.size() == 0) {
            holder.setCategory(category);

            if (position == adapter.getSectionItems(getHeader()).size()) {
                holder.removeDivider(true);
            } else {
                holder.removeDivider(false);
            }
        }
    }

}
