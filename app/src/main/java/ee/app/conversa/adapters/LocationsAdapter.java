/*
 * The MIT License (MIT)
 * 
 * Copyright � 2013 Clover Studio Ltd. All rights reserved.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package ee.app.conversa.adapters;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

//import com.bignerdranch.android.multiselector.MultiSelector;

import java.util.List;

import ee.app.conversa.ConversaApp;
import ee.app.conversa.R;
import ee.app.conversa.model.Database.Location;

/**
 * MessagesAdapter
 * 
 * Adapter class for chat wall messages.
 */

public class LocationsAdapter extends RecyclerView.Adapter<LocationsAdapter.ViewHolder>  {

	//private MultiSelector mSelector;
	private AppCompatActivity mActivity;
	private List<Location> mLocations;

	public LocationsAdapter(AppCompatActivity activity, List<Location> locations) {//}, MultiSelector selector) {
		this.mActivity  = activity;
		this.mLocations = locations;
		//this.mSelector  = selector;
	}

	@Override
	public long getItemId(int position) { return super.getItemId(position); }

	@Override
	public int getItemCount() { return (mLocations == null) ? 0 : mLocations.size(); }

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int i) {
		View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.location_item, parent, false);
		ViewHolder mh = new ViewHolder(v);
		return mh;
	}

	public void setLocations(List<Location> locations) {
		mLocations = locations;
		notifyDataSetChanged();
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position) {
		Location crime = mLocations.get(position);
		if(position == 0) {
			ConversaApp.getPreferences().setBusLocation(crime.getLocationId());
		}
		holder.bindCrime(crime);
		Log.d("LocationsAdpater", "binding crime" + crime + "at position" + position);
	}

	private void selectCrime(Location c) {

	}

	class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
		private final TextView mTitleTextView;
		private final TextView mDateTextView;
		private final CheckBox mSolvedCheckBox;
		private Location mCrime;

		public ViewHolder(View itemView) {
			super(itemView);//, mSelector);
			mTitleTextView = (TextView) itemView.findViewById(R.id.crime_list_item_titleTextView);
			mDateTextView = (TextView) itemView.findViewById(R.id.crime_list_item_dateTextView);
			mSolvedCheckBox = (CheckBox) itemView.findViewById(R.id.crime_list_item_solvedCheckBox);
			itemView.setOnClickListener(this);
		}

		public void bindCrime(Location crime) {
			mCrime = crime;
			mTitleTextView.setText(crime.getName());
			mDateTextView.setText(crime.getAddress());
			mSolvedCheckBox.setChecked(false);//(crime.getId());
		}

		@Override
		public void onClick(View v) {
			if (mCrime != null) {
				ConversaApp.getPreferences().setBusLocation(mCrime.getLocationId());
			}
		}
	}
}
