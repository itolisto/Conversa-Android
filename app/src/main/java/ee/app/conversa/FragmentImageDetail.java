/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ee.app.conversa;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;

import ee.app.conversa.utils.Logger;
import ee.app.conversa.utils.Utils;
import ee.app.conversa.view.ZoomableDraweeView;

/**
 * This fragment will populate the children of the ViewPager from {@link ActivityImageDetail}.
 */
public class FragmentImageDetail extends Fragment {
    private static final String IMAGE_DATA_EXTRA = "extra_image_data";
    private String mImageUrl;
    private ZoomableDraweeView mImageView;

    /**
     * Factory method to generate a new instance of the fragment given an image number.
     *
     * @param imageUrl The image url to load
     * @return A new instance of ImageDetailFragment with imageNum extras
     */
    public static FragmentImageDetail newInstance(String imageUrl) {
        final FragmentImageDetail f = new FragmentImageDetail();

        final Bundle args = new Bundle();
        args.putString(IMAGE_DATA_EXTRA, imageUrl);
        f.setArguments(args);

        return f;
    }

    /**
     * Empty constructor as per the Fragment documentation
     */
    public FragmentImageDetail() {}

    /**
     * Populate image using a url from extras, use the convenience factory method
     * {@link FragmentImageDetail#newInstance(String)} to create this fragment.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mImageUrl = getArguments() != null ? getArguments().getString(IMAGE_DATA_EXTRA) : "";
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // Inflate and locate the main ImageView
        final View v = inflater.inflate(R.layout.image_detail_fragment, container, false);
        mImageView = (ZoomableDraweeView) v.findViewById(R.id.imageView);
        Uri uri = Utils.getUriFromString(mImageUrl);

        if (uri == null) {
            uri = Utils.getDefaultImage(getActivity(), R.drawable.ic_business_default);
        }

        mImageView.setAllowTouchInterceptionWhileZoomed(true);
        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setUri(uri)
                .build();
        mImageView.setController(controller);
        mImageView.setTapListener(createTapListener(1));

        return v;
    }

    private GestureDetector.SimpleOnGestureListener createTapListener(final int position) {
        return new GestureDetector.SimpleOnGestureListener() {
            @Override
            public void onLongPress(MotionEvent e) {
                Logger.error("FragmentImageDetail", "onLongPress: " + position);
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                ((ActivityImageDetail)getActivity()).onClick(mImageView);
                return true;
            }
        };
    }

}