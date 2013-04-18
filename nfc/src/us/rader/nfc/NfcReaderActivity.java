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
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

/**
 * Abstract super class of {@link Activity} objects that wait for a NFC
 * {@link Tag} to be scanned and process it in some way
 * 
 * <p>
 * Note that a number of {@link Activity} methods are overridden here and marked
 * <code>final</code>. This is to ensure that the tightly coupled behavior of
 * these methods isn't subverted lower in the inheritance tree. Where
 * appropriate, <code>abstract</code> methods are declared to allow the child
 * class to take actions at the same stage of the {@link Activity} life cycle.
 * For example, {@link #onCreate(Bundle)} calls {@link #initialize(Bundle)} and
 * {@link #onNewIntent(Intent)} (indirectly) calls
 * {@link #processTag(Intent, Tag)}.
 * </p>
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
     * Invoke {@link NfcReaderActivity#processTag(Intent, Tag)} asynchronously.
     * 
     * <p>
     * Much of the Android NFC API must be called in worker threads separate
     * from the UI thread. This class arranges to call the derived class's
     * {@link Tag} handling code in such a worker thread.
     * </p>
     * 
     * @see NfcReaderActivity#processTag(Intent, Tag)
     * 
     * @author Kirk
     * 
     */
    private class ProcessTagTask extends AsyncTask<Void, String, String> {

        /**
         * The {@link Intent} passed to
         * {@link NfcReaderActivity#onNewIntent(Intent)}
         */
        private Intent intent;

        /**
         * The {@link Tag} to process
         */
        private Tag    tag;

        /**
         * Initialize {@link #intent} and {@link #tag}
         * 
         * @param intent
         *            the {@link Intent}
         * 
         * @param tag
         *            the {@link Tag}
         */
        public ProcessTagTask(Intent intent, Tag tag) {

            this.intent = intent;
            this.tag = tag;

        }

        /**
         * Invoke {@link NfcReaderActivity#processTag(Intent, Tag)} in the
         * worker thread
         * 
         * @param ignored
         *            parameters are ignored
         * 
         * @see NfcReaderActivity#processTag(Intent, Tag)
         */
        @Override
        protected String doInBackground(Void... ignored) {

            try {

                return processTag(intent, tag);

            } catch (Exception e) {

                Log.e(getClass().getName(), "ProcessTagTask.doInBackground()", //$NON-NLS-1$
                        e);
                return getString(R.string.error_processing_tag);

            }
        }

        /**
         * Report the result of processing the {@link Tag} to the user in an
         * {@link AlertDialog} and then exit this {@link NfcReaderActivity}
         */
        @Override
        protected void onPostExecute(String result) {

            AlertDialog.Builder builder = new AlertDialog.Builder(
                    NfcReaderActivity.this);
            builder.setMessage(result);

            builder.setNeutralButton(android.R.string.ok,
                    new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int which) {

                            dialog.dismiss();
                            finish();

                        }

                    });

            builder.create().show();

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
     * Cached {@link NfcAdapter} for use by {@link #onResume()} and
     * {@link #onPause()}
     * 
     * @see #onCreate(Bundle)
     * @see #onResume()
     * @see #onPause()
     * @see #onNewIntent(Intent)
     */
    private NfcAdapter     nfcAdapter;

    /**
     * Cached {@link NfcAdapter} for use by {@link #onResume()}
     * 
     * @see #onCreate(Bundle)
     * @see #onResume()
     * @see #onPause()
     * @see #onNewIntent(Intent)
     */
    private PendingIntent  pendingIntent;

    /**
     * Cached {@link NfcAdapter} for use by {@link #onResume()}
     * 
     * @see #onCreate(Bundle)
     * @see #onResume()
     * @see #onPause()
     * @see #onNewIntent(Intent)
     */
    private IntentFilter[] pendingIntentFilters;

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
     * Called by {@link #onCreate(Bundle)}
     * 
     * Put any code you would ordinarily put in {@link #onCreate(Bundle)} here.
     * <code>super.onCreate(Bundle)</code> will have already been called.
     * 
     * @param savedInstanceState
     *            the {@link Bundle} that was passed to
     *            {@link #onCreate(Bundle)}
     */
    protected abstract void initialize(Bundle savedInstanceState);

    /**
     * Prepare this instance to be displayed
     * 
     * <p>
     * This override of {@link Activity#onCreate(Bundle)} is deliberately
     * <code>final</code>. Put any code you would ordinarily put in your own
     * override of {@link Activity#onCreate(Bundle)} in
     * {@link #initialize(Bundle)}, instead.
     * </p>
     * 
     * <p>
     * This is done to ensure that the tightly coupled behavior of this method
     * the other overridden {@link Activity} methods won't be accidentally
     * subverted by derived classes.
     * </p>
     * 
     * @see #initialize(Bundle)
     * @see #onResume()
     * @see #onPause()
     * @see #onNewIntent(Intent)
     */
    @Override
    protected final void onCreate(Bundle savedInstanceState) {

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

        try {

            initialize(savedInstanceState);

        } catch (Exception e) {

            Log.e(getClass().getName(), "onCreate(Bundle)", e); //$NON-NLS-1$

        }

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
        ProcessTagTask processTagTask = new ProcessTagTask(intent, tag);
        processTagTask.execute();

    }

    /**
     * Called by {@link #onPause()}
     */
    protected void paused() {

        // nothing to do in the base class

    }

    /**
     * Called by {@link #onResume()}
     */
    protected void resumed() {

        // nothing to do in the base class

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

        try {

            paused();

        } catch (Exception e) {

            Log.e(getClass().getName(), "onPause()", e); //$NON-NLS-1$

        }
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

        try {

            resumed();

        } catch (Exception e) {

            Log.e(getClass().getName(), "onPause()", e); //$NON-NLS-1$

        }
    }

    /**
     * Handle the result of scanning a NFC {@link Tag}
     * 
     * Note that this will be invoked in a worker thread separate from the UI
     * thread, a fact that can be relied on and must be taken account of by
     * derived classes' implementations of this <code>abstract</code> method.
     * 
     * @param intent
     *            the {@link Intent} passed to {@link #onNewIntent(Intent)}
     * 
     * @param tag
     *            the {@link Tag}
     * 
     * @return a message to display to the user before exiting this
     *         {@link NfcReaderActivity}
     */
    protected abstract String processTag(Intent intent, Tag tag);

}
