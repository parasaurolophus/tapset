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

package us.rader.tapset;

import us.rader.tapset.nfc.NdefWriterActivity;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;

/**
 * {@link NdefWriterActivity} used by this app to write a NDEF 'U' record to a
 * {@link Tag}
 * 
 * @author Kirk
 */
public final class WriteTagActivity extends NdefWriterActivity {

    /**
     * Foreground dispatch request code
     */
    public static final int REQUEST_WRITE_TAG = 1;

    /**
     * @see #REQUEST_WRITE_TAG
     */
    public WriteTagActivity() {

        super(REQUEST_WRITE_TAG);

    }

    /**
     * Wizard-generated handler for an options {@link MenuItem}
     * 
     * @param item
     *            the {@link MenuItem}
     * 
     * @return <code>true</code> if and only if the {@link MenuItem} event was
     *         consumed
     * 
     * @see Activity#onOptionsItemSelected(MenuItem)
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case android.R.id.home:

                // This ID represents the Home or Up button. In the case of this
                // activity, the Up button is shown. Use NavUtils to allow users
                // to navigate up one level in the application structure. For
                // more details, see the Navigation pattern on Android Design:
                //
                // http://developer.android.com/design/patterns/navigation.html#up-vs-back
                //
                NavUtils.navigateUpFromSameTask(this);
                return true;

            default:

                return super.onOptionsItemSelected(item);

        }
    }

    /**
     * Return a {@link NdefMessage} containing the {@link Uri} returned by
     * {@link Intent#getData()} for the {@link Intent} used to launch this
     * {@link WriteTagActivity}
     * 
     * @param currentContents
     *            current contents of the {@link Tag}
     * 
     * @return the {@link NdefMessage} for the requested {@link Uri}
     * 
     * @see NdefWriterActivity#createNdefMessage(NdefMessage)
     */
    @Override
    protected NdefMessage createNdefMessage(NdefMessage currentContents) {

        Intent intent = getIntent();
        Uri uri = intent.getData();
        NdefRecord uriRecord = NdefWriterActivity.createUri(uri);
        NdefRecord aaRecord = NdefWriterActivity.createAar(getClass()
                .getPackage());
        return new NdefMessage(new NdefRecord[] { uriRecord, aaRecord });

    }

    /**
     * Prepare this instance to be displayed
     * 
     * @param savedInstanceState
     *            persisted app state or <code>null</code>
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_tag);
        // Show the Up button in the action bar.
        setupActionBar();

    }

    /**
     * Set up the {@link android.app.ActionBar}.
     */
    @SuppressLint("NewApi")
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setupActionBar() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {

            getActionBar().setDisplayHomeAsUpEnabled(true);

        }
    }

}
