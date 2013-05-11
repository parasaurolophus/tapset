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
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.tech.Ndef;
import android.os.Build;
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
     * Operational mode to support unit tests
     * 
     * @author Kirk
     * 
     */
    private static enum Mode {

        /**
         * Normal app mode
         */
        NORMAL,

        /**
         * Perform NFC write unit tests
         */
        WRITE_TEST;

    }

    /**
     * The {@link Intent#getParcelableExtra(String)} key used to convery the
     * scanned {@link NdefMessage} back to the {@link Activity} that started
     * this one
     */
    public static final String EXTRA_RESULT = "us.rader.tapset.result"; //$NON-NLS-1$

    /**
     * The current operating {@link Mode}
     */
    private Mode               mode;

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
     * @return the {@link NdefMessage} for the requested {@link Uri}
     * 
     * @see NdefWriterActivity#createNdefMessage(Ndef)
     */
    @Override
    protected NdefMessage createNdefMessage(Ndef ndef) {

        switch (mode) {

            case WRITE_TEST:

                return createTestMessage();

            default:

                return createSettingsMessage();

        }
    }

    /**
     * Helper used by {@link NfcReaderActivity#onCreate(Bundle)}
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
        mode = Mode.NORMAL;

    }

    /**
     * Handle notification that a {@link NdefMessage} has been detected
     * 
     * @param result
     *            the {@link NdefMessage}
     */
    @Override
    protected void onTagProcessed(NdefMessage result) {

        if (result == null) {

            setResult(RESULT_FIRST_USER);

        } else {

            Intent intent = new Intent();
            intent.putExtra(EXTRA_RESULT, result);
            setResult(RESULT_OK, intent);

        }

        finish();

    }

    /**
     * Create a {@link NdefMessage} containing a {@link NdefRecord} representing
     * the {@link Uri} that was passed in the {@link Intent} with which this
     * activity was launched
     * 
     * @return the {@link NdefMessage}
     */
    private NdefMessage createSettingsMessage() {

        Intent intent = getIntent();
        Uri uri = intent.getData();
        NdefRecord record = createUri(uri);
        return new NdefMessage(new NdefRecord[] { record });

    }

    /**
     * Create a {@link NdefMessage} containing a collection of
     * {@link NdefRecord} instances used in unit tests
     * 
     * @return the {@link NdefMessage}
     */
    private NdefMessage createTestMessage() {

        NdefRecord uRecord = createUri(Uri.parse("http://www.rader.us/tapset")); //$NON-NLS-1$
        NdefRecord aRecord = createUri(Uri
                .parse("tapset://www.rader.us/tapset")); //$NON-NLS-1$
        NdefRecord tRecord = createText("test record", "en"); //$NON-NLS-1$ //$NON-NLS-2$
        NdefRecord mRecord = createMime("application/x-tapset", //$NON-NLS-1$
                new byte[] { 0, 1, 2 });
        return new NdefMessage(new NdefRecord[] { uRecord, aRecord, tRecord,
                mRecord });

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
