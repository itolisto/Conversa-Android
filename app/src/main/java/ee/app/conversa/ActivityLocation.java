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

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.LocationSource.OnLocationChangedListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import ee.app.conversa.management.GPSTracker;
import ee.app.conversa.utils.Const;

//import com.facebook.appevents.AppEventsLogger;

/**
 * LocationActivity
 * 
 * Shows user current location or other user's previous sent location.
 */

@SuppressLint("DefaultLocale")
public class ActivityLocation extends FragmentActivity implements OnMapReadyCallback {


	@Override
	protected void onPause() {
		super.onPause();
		// Logs 'app deactivate' App Event.
//		AppEventsLogger.deactivateApp(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		// Logs 'install' and 'app activate' App Events.
//		AppEventsLogger.activateApp(this);
	}

	private ActivityLocation sInstance;
	private SupportMapFragment mMap;
	private Button mBtnBack;
	private Button mBtnSend;
	private GPSTracker mGpsTracker;
	private String mAddressText;
	private String mTypeOfLocation;
	private double mLatitude;
	private double mLongitude;
	private Bitmap mMapPinBlue;
	private Bundle mExtras;
	private MarkerOptions markerOfUser;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_location);
		sInstance = this;
		if (checkGooglePlayServicesForUpdate()) return;
		initialization();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mGpsTracker != null) mGpsTracker.stopUsingGPS();
	}

	private boolean checkGooglePlayServicesForUpdate () {
		int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());
		if (status == ConnectionResult.SUCCESS) {
			return false;
		} else {
			Dialog d = GooglePlayServicesUtil.getErrorDialog(status, this, 1337);
			d.setCancelable(false);
			d.show();
			return true;
		}
	}

	private void initialization() {
		mExtras 		= getIntent().getExtras();
		mTypeOfLocation   = mExtras.getString(Const.LOCATION);
		mLatitude 		  = mExtras.getDouble(Const.LATITUDE);
		mLongitude 		  = mExtras.getDouble(Const.LONGITUDE);

		mBtnBack = (Button) findViewById(R.id.btnBack);
		mBtnSend = (Button) findViewById(R.id.btnSend);
		mBtnBack.setTypeface(ConversaApp.getTfRalewayMedium());
		mBtnSend.setTypeface(ConversaApp.getTfRalewayMedium());

		mMap = (SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map);
		mMap.getMapAsync(this);

		if (mTypeOfLocation.equals("userLocation")) {
			mBtnSend.setVisibility(View.INVISIBLE);
		}

		mMapPinBlue = BitmapFactory.decodeResource(getResources(),
				R.drawable.location_more_icon_active);
	}

	@Override
	public void onMapReady(GoogleMap map) {
//		map.setMyLocationEnabled(true);

		if (mTypeOfLocation.equals("userLocation")) {
			setLocation(map, mLatitude, mLongitude);
		} else {
			setGps(map);
		}

		setOnClickListener();
	}

	private void setOnClickListener() {
		mBtnSend.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// try to get address, and city, if failed then location sent
				// (just latitude and longitude)
//				new GetCityAsync(ActivityLocation.this).execute();
			}
		});

		mBtnBack.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ActivityLocation.this.finish();
			}
		});
	}

	private void setLocation(GoogleMap map, double lat, double lon) {
		final String nameOfUser = mExtras.getString("nameOfUser");
		map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lon),
				16));

		markerOfUser = new MarkerOptions()
				.title(nameOfUser)
				.position(new LatLng(lat, lon))
				.icon(BitmapDescriptorFactory.fromBitmap(mMapPinBlue));

		map.addMarker(markerOfUser);

//		new GetAdressNameAsync(ActivityLocation.this).execute(lat, lon, map);

		mGpsTracker = new GPSTracker(this);

		if (mGpsTracker.canGetLocation()) {
			double myLat = mGpsTracker.getLatitude();
			double myLon = mGpsTracker.getLongitude();

			map.addMarker(
					new MarkerOptions()
							.title(nameOfUser)
							.position(new LatLng(myLat, myLon))
							.icon(BitmapDescriptorFactory.fromResource(R.drawable.location_more_icon)));
		}
	}

	private void setGps(final GoogleMap map) {
		final String nameOfUser = mExtras.getString("nameOfUser");
		mGpsTracker = new GPSTracker(this);
		if (mGpsTracker.canGetLocation()) {
			mLatitude  = mGpsTracker.getLatitude();
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
					mLatitude  = point.latitude;
					mLongitude = point.longitude;
					map.clear();
					map.addMarker(new MarkerOptions().position(point).icon(
							BitmapDescriptorFactory.fromBitmap(mMapPinBlue)));
				}
			});

			mGpsTracker.setOnLocationChangedListener(new OnLocationChangedListener() {
				@Override
				public void onLocationChanged(Location location) {
					mLatitude  = location.getLatitude();
					mLongitude = location.getLongitude();
					myMarker.setPosition(new LatLng(location.getLatitude(), location.getLongitude()));
				}
			});
		} else {
			mGpsTracker.showSettingsAlert();
		}
	}

//	class GetAdressNameAsync extends ConversaAsync<Object, Void, Void> {
//
//		private boolean mLoaded = false;
//		private GoogleMap map;
//
//		protected GetAdressNameAsync(Context context) { super(context); }
//
//		@Override
//		protected void onPreExecute() { super.onPreExecute(); }
//
//		@Override
//		protected Void backgroundWork(Object... params) {
//			Geocoder geocoder = new Geocoder(ActivityLocation.this,
//					Locale.getDefault());
//			List<Address> addresses = null;
//			try {
//				// Call the synchronous getFromLocation() method by passing in
//				// the lat/long values.
//				addresses = geocoder.getFromLocation((double)params[0], (double)params[1], 1);
//				map = (GoogleMap) params[2];
//			} catch (IOException e) {
//				Logger.error("LOG", e.toString());
//				// Update UI field with the exception.
//			}
//			if (addresses != null && addresses.size() > 0) {
//				Address address = addresses.get(0);
//				// Format the first line of address (if available), city, and
//				// country name.
//				mAddressText = "";
//				if(address.getMaxAddressLineIndex() > 0) {
//					mAddressText = mAddressText + address.getAddressLine(0);
//				}
//				if(address.getLocality() != null) {
//					mAddressText = mAddressText + ", " + address.getLocality();
//				}
//
//				// Update the UI via a message handler
//				mLoaded = true;
//			}
//			return null;
//		}
//
//		@Override
//		protected void onPostExecute(Void result) {
//			if (!mLoaded) {
//				mAddressText = "Getting adress failed.";
//			} else {
//				markerOfUser.title(mAddressText);
//				map.addMarker(markerOfUser);
//			}
//
//			super.onPostExecute(result);
//		}
//	}
//
//	class GetCityAsync extends ConversaAsync<Void, Void, Void> {
//
//		private boolean mLoaded = false;
//
//		protected GetCityAsync(Context context) { super(context); }
//
//		@Override
//		protected void onPreExecute() { super.onPreExecute(); }
//
//		@Override
//		protected Void backgroundWork(Void... params) {
//			Geocoder geocoder = new Geocoder(ActivityLocation.this,
//					Locale.getDefault());
//			List<Address> addresses = null;
//
//			try {
//				// Call the synchronous getFromLocation() method by passing in
//				// the lat/long values.
//				addresses = geocoder.getFromLocation(mLatitude, mLongitude, 1);
//			} catch (IOException e) {
//				Logger.error("LOG", e.toString());
//				// Update UI field with the exception.
//			}
//			if (addresses != null && addresses.size() > 0) {
//				Address address = addresses.get(0);
//				// Format the first line of address (if available), city, and
//				// country name.
//				mAddressText = "";
//				if(address.getMaxAddressLineIndex() > 0) {
//					mAddressText = mAddressText + address.getAddressLine(0);
//				}
//				if(address.getLocality() != null) {
//					mAddressText = mAddressText + ", " + address.getLocality();
//				}
//
//				mLoaded = true;
//			}
//			return null;
//		}
//
//		@Override
//		protected void onPostExecute(Void result) {
//			super.onPostExecute(result);
//			if (!mLoaded) {
//				mAddressText = "";
//			}
//
//			if (mLatitude != 0 && mLongitude != 0) {
//				mLoaded = true;
//			} else {
//				mLoaded = false;
//			}
//
//			if (mLoaded == true) {
//				new SendMessageAsync(sInstance,
//						SendMessageAsync.TYPE_LOCATION).execute(mAddressText, Double.toString(mLatitude),
//						Double.toString(mLongitude));
//				ActivityLocation.this.finish();
//			} else {
//				Toast.makeText(ActivityLocation.this,
//						"Getting location failed", Toast.LENGTH_SHORT).show();
//			}
//		}
//	}
}
