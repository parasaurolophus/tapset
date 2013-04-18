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

import us.rader.nfc.NdefWriterActivity;
import us.rader.nfc.NfcReaderActivity;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;

/**
 * {@link NdefWriterActivity} used by this app to write a NDEF 'U' record
 * followed by a AAR linking to the Play store
 * 
 * @author Kirk
 */
public final class WriteTagActivity extends NdefWriterActivity {

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

        }

        return super.onOptionsItemSelected(item);

    }

    /**
     * Return a {@link NdefMessage} containing the {@link Uri} returned by
     * {@link Intent#getData()} for the {@link Intent} used to launch this
     * {@link WriteTagActivity}
     * 
     * @return the {@link NdefMessage} for the requested {@lini Uri}
     * 
     * @see NdefWriterActivity#createNdefMessage()
     */
    @Override
    protected NdefMessage createNdefMessage() {

        Intent intent = getIntent();
        Uri uri = intent.getData();
        NdefRecord record = createUriRecord(uri);
        return new NdefMessage(new NdefRecord[] { record });

    }

    /**
     * Helper used by {@link NfcReaderActivity#onCreate(Bundle)}
     * 
     * @param savedInstanceState
     *            persisted app state or <code>null</code>
     * 
     * @see NfcReaderActivity#initialize(Bundle)
     */
    @Override
    protected void initialize(Bundle savedInstanceState) {

        setContentView(R.layout.activity_write_tag);
        // Show the Up button in the action bar.
        setupActionBar();

    }

    /**
     * Set up the {@link android.app.ActionBar}.
     */
    private void setupActionBar() {

        getActionBar().setDisplayHomeAsUpEnabled(true);

    }

}
