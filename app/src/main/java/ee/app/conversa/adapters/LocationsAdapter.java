package ee.app.conversa.adapters;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.List;

import ee.app.conversa.R;
import ee.app.conversa.model.database.Location;

/**
 * MessagesAdapter
 * 
 * Adapter class for chat wall messages.
 */

public class LocationsAdapter extends RecyclerView.Adapter<LocationsAdapter.ViewHolder>  {

	private AppCompatActivity mActivity;
	private List<Location> mLocations;

	public LocationsAdapter(AppCompatActivity activity, List<Location> locations) {
		this.mActivity  = activity;
		this.mLocations = locations;
	}

	@Override
	public int getItemCount() {
		return mLocations.size();
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int i) {
		View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.location_item, parent, false);
		return new ViewHolder(v);
	}

	public void setLocations(List<Location> locations) {
		mLocations = locations;
		notifyDataSetChanged();
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position) {
		Location crime = mLocations.get(position);
		holder.mTitleTextView.setText(crime.getName());
		holder.mDateTextView.setText(crime.getAddress());
		holder.mSolvedCheckBox.setChecked(false);
		Log.d("LocationsAdpater", "binding crime" + crime + "at position" + position);
	}

	class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
		private final TextView mTitleTextView;
		private final TextView mDateTextView;
		private final CheckBox mSolvedCheckBox;

		public ViewHolder(View itemView) {
			super(itemView);
			mTitleTextView = (TextView) itemView.findViewById(R.id.crime_list_item_titleTextView);
			mDateTextView = (TextView) itemView.findViewById(R.id.crime_list_item_dateTextView);
			mSolvedCheckBox = (CheckBox) itemView.findViewById(R.id.crime_list_item_solvedCheckBox);
			itemView.setOnClickListener(this);
		}

		@Override
		public void onClick(View v) {

		}
	}
}
