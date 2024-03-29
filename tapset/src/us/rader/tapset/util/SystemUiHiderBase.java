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

import android.app.Activity;
import android.view.View;
import android.view.WindowManager;

/**
 * A base implementation of {@link SystemUiHider}. Uses APIs available in all
 * API levels to show and hide the status bar.
 */
public class SystemUiHiderBase extends SystemUiHider {
    /**
     * Whether or not the system UI is currently visible. This is a cached value
     * from calls to {@link #hide()} and {@link #show()}.
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
    protected SystemUiHiderBase(Activity activity, View anchorView, int flags) {

        super(activity, anchorView, flags);

    }

    /**
     * Hide the system UI
     * 
     * @see SystemUiHider#hide()
     */
    @Override
    public void hide() {

        if ((mFlags & FLAG_FULLSCREEN) != 0) {

            mActivity.getWindow().setFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);

        }

        mOnVisibilityChangeListener.onVisibilityChange(false);
        mVisible = false;

    }

    /**
     * Return <code>true</code> if and only if the system UI is visible
     * 
     * @return <code>true</code> or <code>false</code> depending on whether or
     *         not the system UI is visible
     * 
     * @see SystemUiHider#isVisible()
     */
    @Override
    public boolean isVisible() {

        return mVisible;

    }

    /**
     * Initialize this instance
     * 
     * @see SystemUiHider#setup()
     */
    @Override
    public void setup() {

        if ((mFlags & FLAG_LAYOUT_IN_SCREEN_OLDER_DEVICES) == 0) {

            mActivity.getWindow().setFlags(
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                            | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                            | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        }
    }

    /**
     * Make the system UI visible
     * 
     * @see SystemUiHider#show()
     */
    @Override
    public void show() {

        if ((mFlags & FLAG_FULLSCREEN) != 0) {

            mActivity.getWindow().setFlags(0,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);

        }

        mOnVisibilityChangeListener.onVisibilityChange(true);
        mVisible = true;

    }

}
