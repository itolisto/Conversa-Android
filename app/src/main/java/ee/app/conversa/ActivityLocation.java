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

package ee.app.conversa;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.LocationSource.OnLocationChangedListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import ee.app.conversa.extendables.ConversaActivity;
import ee.app.conversa.management.GPSTracker;
import ee.app.conversa.utils.Const;

/**
 * LocationActivity
 * 
 * Shows user current location or other user's previous sent location.
 */
public class ActivityLocation extends ConversaActivity implements OnMapReadyCallback, OnClickListener {

	// Public constants when calling this Activity for request codes
	public static final int PICK_LOCATION_REQUEST = 1003;

	private MapFragment mMap;
	private GPSTracker mGpsTracker;
	private String mTypeOfLocation;
	private double mLatitude;
	private double mLongitude;
	private Bitmap mMapPinBlue;
	private Bundle mExtras;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_location);
		initialization();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mGpsTracker != null)
			mGpsTracker.stopUsingGPS();
	}

	@Override
	protected void initialization() {
		super.initialization();
		mExtras = getIntent().getExtras();
		mTypeOfLocation = mExtras.getString(Const.LOCATION);
		mLatitude = mExtras.getFloat(Const.LATITUDE);
		mLongitude = mExtras.getFloat(Const.LONGITUDE);

		Button mBtnSend = (Button) findViewById(R.id.btnSend);
		mBtnSend.setTypeface(ConversaApp.getInstance(this).getTfRalewayMedium());
		mBtnSend.setOnClickListener(this);

		mMap = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
		mMap.getMapAsync(this);

		if (mTypeOfLocation.equals("userLocation")) {
			mBtnSend.setVisibility(View.GONE);
		}

		mMapPinBlue = BitmapFactory.decodeResource(getResources(),
				R.drawable.ic_location_me);
	}

	@Override
	public void onMapReady(GoogleMap map) {
		if (mTypeOfLocation.equals("userLocation")) {
			setLocation(map, mLatitude, mLongitude);
		} else {
			setGps(map);
		}
	}

	private void setLocation(GoogleMap map, double lat, double lon) {
		final String nameOfUser = mExtras.getString("nameOfUser");
		map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lon), 16));

		MarkerOptions markerOfUser = new MarkerOptions()
				.title(nameOfUser)
				.position(new LatLng(lat, lon))
				.icon(BitmapDescriptorFactory.fromBitmap(mMapPinBlue));

		map.addMarker(markerOfUser);

		mGpsTracker = new GPSTracker(this);

		if (mGpsTracker.canGetLocation()) {
			double myLat = mGpsTracker.getLatitude();
			double myLon = mGpsTracker.getLongitude();

			map.addMarker(
					new MarkerOptions()
							.title(nameOfUser)
							.position(new LatLng(myLat, myLon))
							.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_location)));
		}
	}

	private void setGps(final GoogleMap map) {
		final String nameOfUser = mExtras.getString("nameOfUser");
		mGpsTracker = new GPSTracker(this);

		if (mGpsTracker.canGetLocation()) {
			mLatitude = mGpsTracker.getLatitude();
			mLongitude = mGpsTracker.getLongitude();

			map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(
					mLatitude, mLongitude), 16));

			final Marker myMarker = map.addMarker(
					new MarkerOptions()
							.title(nameOfUser)
							.position(new LatLng(mLatitude, mLongitude))
							.icon(BitmapDescriptorFactory.fromBitmap(mMapPinBlue)));

			map.setOnMapClickListener(new OnMapClickListener() {
				@Override
				public void onMapClick(LatLng point) {
					mLatitude = point.latitude;
					mLongitude = point.longitude;
					map.clear();
					map.addMarker(new MarkerOptions().position(point).icon(
							BitmapDescriptorFactory.fromBitmap(mMapPinBlue)));
				}
			});

			mGpsTracker.setOnLocationChangedListener(new OnLocationChangedListener() {
				@Override
				public void onLocationChanged(Location location) {
					mLatitude = location.getLatitude();
					mLongitude = location.getLongitude();
					myMarker.setPosition(new LatLng(location.getLatitude(), location.getLongitude()));
				}
			});
		} else {
			mGpsTracker.showSettingsAlert();
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btnSend:
				Intent returnIntent = new Intent();
				returnIntent.putExtra("lat", mLatitude);
				returnIntent.putExtra("lon", mLongitude);
				setResult(Activity.RESULT_OK, returnIntent);
				finish();
				break;
		}
	}

}