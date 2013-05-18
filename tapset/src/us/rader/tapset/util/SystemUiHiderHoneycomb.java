/*
 * Copyright 2013 Kirk Rader
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package us.rader.tapset.util;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.view.View;
import android.view.WindowManager;

/**
 * An API 11+ implementation of {@link SystemUiHider}. Uses APIs available in
 * Honeycomb and later (specifically {@link View#setSystemUiVisibility(int)}) to
 * show and hide the system UI.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class SystemUiHiderHoneycomb extends SystemUiHiderBase {

	/**
	 * Event handler class invoked when the system UI visibility changes
	 * 
	 * @author Kirk
	 * 
	 */
	private final class VisibilityChangeListener implements
			View.OnSystemUiVisibilityChangeListener {

		/**
		 * Update the state of the widgets when the system UI visibility changes
		 * 
		 * @param vis
		 *            flags indicating the state of the system UI
		 * 
		 * @see android.view.View.OnSystemUiVisibilityChangeListener#onSystemUiVisibilityChange(int)
		 */
		@Override
		public void onSystemUiVisibilityChange(int vis) {

			// Test against mTestFlags to see if the system UI is visible.
			if ((vis & mTestFlags) != 0) {

				if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {

					// Pre-Jelly Bean, we must manually hide the action bar and
					// use the old window flags API.
					mActivity.getActionBar().hide();
					mActivity.getWindow().setFlags(
							WindowManager.LayoutParams.FLAG_FULLSCREEN,
							WindowManager.LayoutParams.FLAG_FULLSCREEN);

				}

				// Trigger the registered listener and cache the visibility
				// state.
				mOnVisibilityChangeListener.onVisibilityChange(false);
				mVisible = false;

			} else {

				mAnchorView.setSystemUiVisibility(mShowFlags);

				if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {

					// Pre-Jelly Bean, we must manually show the action bar and
					// use the old window flags API.
					mActivity.getActionBar().show();
					mActivity.getWindow().setFlags(0,
							WindowManager.LayoutParams.FLAG_FULLSCREEN);

				}

				// Trigger the registered listener and cache the visibility
				// state.
				mOnVisibilityChangeListener.onVisibilityChange(true);
				mVisible = true;

			}
		}
	}

	/**
	 * Flags for {@link View#setSystemUiVisibility(int)} to use when hiding the
	 * system UI.
	 */
	private int mHideFlags;

	/**
	 * Flags for {@link View#setSystemUiVisibility(int)} to use when showing the
	 * system UI.
	 */
	private int mShowFlags;

	/**
	 * Event handler when the UI visibility changes
	 */
	private View.OnSystemUiVisibilityChangeListener mSystemUiVisibilityChangeListener = new VisibilityChangeListener();

	/**
	 * Flags to test against the first parameter in
	 * {@link android.view.View.OnSystemUiVisibilityChangeListener#onSystemUiVisibilityChange(int)}
	 * to determine the system UI visibility state.
	 */
	private int mTestFlags;

	/**
	 * Whether or not the system UI is currently visible. This is cached from
	 * {@link android.view.View.OnSystemUiVisibilityChangeListener}.
	 */
	private boolean mVisible = true;

	/**
	 * Constructor not intended to be called by clients. Use
	 * {@link SystemUiHider#getInstance} to obtain an instance.
	 * 
	 * @param activity
	 *            parent {@link Activity}
	 * 
	 * @param anchorView
	 *            container {@link View}
	 * 
	 * @param flags
	 *            options
	 */
	@SuppressLint("InlinedApi")
	protected SystemUiHiderHoneycomb(Activity activity, View anchorView,
			int flags) {

		super(activity, anchorView, flags);

		mShowFlags = View.SYSTEM_UI_FLAG_VISIBLE;
		mHideFlags = View.SYSTEM_UI_FLAG_LOW_PROFILE;
		mTestFlags = View.SYSTEM_UI_FLAG_LOW_PROFILE;

		if ((mFlags & FLAG_FULLSCREEN) != 0) {
			// If the client requested fullscreen, add flags relevant to hiding
			// the status bar. Note that some of these constants are new as of
			// API 16 (Jelly Bean). It is safe to use them, as they are inlined
			// at compile-time and do nothing on pre-Jelly Bean devices.
			mShowFlags |= View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
			mHideFlags |= View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
					| View.SYSTEM_UI_FLAG_FULLSCREEN;
		}

		if ((mFlags & FLAG_HIDE_NAVIGATION) != 0) {
			// If the client requested hiding navigation, add relevant flags.
			mShowFlags |= View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
			mHideFlags |= View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
					| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
			mTestFlags = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
		}
	}

	/** {@inheritDoc} */
	@Override
	public void hide() {
		mAnchorView.setSystemUiVisibility(mHideFlags);
	}

	/** {@inheritDoc} */
	@Override
	public boolean isVisible() {
		return mVisible;
	}

	/** {@inheritDoc} */
	@Override
	public void setup() {
		mAnchorView
				.setOnSystemUiVisibilityChangeListener(mSystemUiVisibilityChangeListener);
	}

	/** {@inheritDoc} */
	@Override
	public void show() {
		mAnchorView.setSystemUiVisibility(mShowFlags);
	}
}
