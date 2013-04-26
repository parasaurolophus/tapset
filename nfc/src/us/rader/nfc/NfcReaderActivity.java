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

package us.rader.nfc;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.widget.Toast;

/**
 * Abstract super class of {@link Activity} objects that wait for a NFC
 * {@link Tag} to be scanned and process it in some way
 * 
 * <p>
 * This class demonstrates the use of the {@link NfcAdapter} foreground dispatch
 * mechanism, as well as the standard pattern for handling events in worker
 * threads using classes derived from {@link AsyncTask}.
 * </p>
 * 
 * @see #onCreate(Bundle)
 * @see #onResume()
 * @see #onPause()
 * @see #onNewIntent(Intent)
 * 
 * @author Kirk
 */
public abstract class NfcReaderActivity extends Activity {

    /**
     * Invoke {@link NfcReaderActivity#processTag(Tag, ProcessTagTask)}
     * asynchronously.
     * 
     * <p>
     * Much of the Android NFC API must be called in worker threads separate
     * from the UI thread. This class arranges to call the derived class's
     * {@link Tag} handling code in such a worker thread.
     * </p>
     * 
     * @see NfcReaderActivity#processTag(Tag, ProcessTagTask)
     * 
     * @author Kirk
     * 
     */
    protected class ProcessTagTask extends AsyncTask<Tag, String, Parcelable> {

        /**
         * The result code to pass back to the {@link Activity} that started
         * this one
         */
        private int resultCode;

        /**
         * Invoke {@link NfcReaderActivity#processTag(Tag, ProcessTagTask)} in
         * the worker thread
         * 
         * @param tags
         *            pass <code>tags[0]</code> to
         *            {@link NfcReaderActivity#processTag(Tag, ProcessTagTask)}
         * 
         * @see NfcReaderActivity#processTag(Tag, ProcessTagTask)
         */
        @Override
        protected Parcelable doInBackground(Tag... tags) {

            try {

                resultCode = RESULT_OK;
                return processTag(tags[0], this);

            } catch (Exception e) {

                Log.e(getClass().getName(), "ProcessTagTask.doInBackground()", //$NON-NLS-1$
                        e);
                resultCode = RESULT_FIRST_USER;
                return null;

            }
        }

        /**
         * Report the result of processing the {@link Tag} to the user in an
         * {@link AlertDialog} and then exit this {@link NfcReaderActivity}
         */
        @Override
        protected void onPostExecute(Parcelable result) {

            Intent intent = new Intent();

            if (result == null) {

                setResult(resultCode);

            } else {

                intent.putExtra(EXTRA_RESULT, result);
                setResult(resultCode, intent);

            }

            finish();

        }

        /**
         * Report the given status message to the user in a brief {@link Toast}
         */
        @Override
        protected void onProgressUpdate(String... values) {

            Toast.makeText(NfcReaderActivity.this, values[0],
                    Toast.LENGTH_SHORT).show();

        }

    }

    /**
     * {@link Intent#getStringExtra(String)} key used to return result of
     * invoking {@link #processTag(Tag, ProcessTagTask)} to the {@link Activity}
     * that started this one
     */
    public static final String EXTRA_RESULT = "result"; //$NON-NLS-1$

    /**
     * Cached {@link NfcAdapter} for use by {@link #onResume()} and
     * {@link #onPause()}
     * 
     * @see #onCreate(Bundle)
     * @see #onResume()
     * @see #onPause()
     * @see #onNewIntent(Intent)
     */
    private NfcAdapter         nfcAdapter;

    /**
     * Cached {@link NfcAdapter} for use by {@link #onResume()}
     * 
     * @see #onCreate(Bundle)
     * @see #onResume()
     * @see #onPause()
     * @see #onNewIntent(Intent)
     */
    private PendingIntent      pendingIntent;

    /**
     * Cached {@link NfcAdapter} for use by {@link #onResume()}
     * 
     * @see #onCreate(Bundle)
     * @see #onResume()
     * @see #onPause()
     * @see #onNewIntent(Intent)
     */
    private IntentFilter[]     pendingIntentFilters;

    /**
     * Return the {@link Intent} to wrap in a {@link PendingIntent} for use with
     * the foreground dispatch mechanism
     * 
     * You should rarely, if ever, need to override this but it is placed in a
     * protected method just in case...
     * 
     * @return the {@link Intent} for to handle the result of the foreground
     *         dispatch to scan a {@link Tag}
     */
    protected Intent getDeferredIntent() {

        return new Intent(this, getClass());

    }

    /**
     * Prepare this instance to be displayed
     * 
     * 
     * @see #onResume()
     * @see #onPause()
     * @see #onNewIntent(Intent)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        Intent intent = getDeferredIntent();
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP
                | Intent.FLAG_ACTIVITY_NEW_TASK);
        pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        IntentFilter ndefDiscoveredFilter = new IntentFilter(
                NfcAdapter.ACTION_NDEF_DISCOVERED);
        IntentFilter tagDiscoveredFilter = new IntentFilter(
                NfcAdapter.ACTION_TAG_DISCOVERED);
        pendingIntentFilters = new IntentFilter[] { ndefDiscoveredFilter,
                tagDiscoveredFilter };

    }

    /**
     * Handle an {@link Intent} received in response to the
     * {@link #pendingIntent} used with the {@link #nfcAdapter} foreground
     * dispatch mechanism.
     * 
     * @see #onCreate(Bundle)
     * @see #onResume()
     * @see #onPause()
     */
    @Override
    protected final void onNewIntent(Intent intent) {

        super.onNewIntent(intent);
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        ProcessTagTask processTagTask = new ProcessTagTask();
        processTagTask.execute(tag);

    }

    /**
     * Disable the {@link #nfcAdapter} foreground dispatch
     * {@link #pendingIntent}
     * 
     * @see #onCreate(Bundle)
     * @see #onResume()
     * @see #onNewIntent(Intent)
     */
    @Override
    protected final void onPause() {

        super.onPause();
        nfcAdapter.disableForegroundDispatch(this);

    }

    /**
     * Enable the {@link #nfcAdapter} foreground dispatch {@link #pendingIntent}
     * 
     * @see #onCreate(Bundle)
     * @see #onPause()
     * @see #onNewIntent(Intent)
     */
    @Override
    protected final void onResume() {

        super.onResume();
        nfcAdapter.enableForegroundDispatch(this, pendingIntent,
                pendingIntentFilters, null);

    }

    /**
     * Handle the result of scanning a NFC {@link Tag}
     * 
     * Note that this will be invoked in a worker thread separate from the UI
     * thread, a fact that can be relied on and must be taken account of by
     * derived classes' implementations of this <code>abstract</code> method.
     * 
     * @param tag
     *            the {@link Tag}
     * 
     * @param task
     *            the {@link ProcessTagTask} invoking this method
     * 
     * @return the result of processing the {@link Tag}
     */
    protected abstract Parcelable processTag(Tag tag, ProcessTagTask task);

}
