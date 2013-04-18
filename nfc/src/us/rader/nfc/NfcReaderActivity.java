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
import android.widget.Toast;

/**
 * Abstract super class of {@link Activity} objects that wait for a NFC
 * {@link Tag} to be scanned and process its contents
 * 
 * <p>
 * Note that a number of {@link Activity} methods are overridden here and marked
 * <code>final</code>. This is to ensure that the tightly coupled behavior of
 * these methods isn't subverted lower in the inheritance tree. Where
 * appropriate, <code>abstract</code> methods are declared to allow the child
 * class to take actions at the same stage of the {@link Activity} life cycle.
 * For example, {@link #onCreate(Bundle)} calls {@link #initialize(Bundle)} and
 * {@link #onNewIntent(Intent)} (indirectly) calls {@link #processTag(Tag)}.
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
 * 
 */
public abstract class NfcReaderActivity extends Activity {

    /**
     * Invoke {@link NfcReaderActivity#processTag(Tag)} asynchronously.
     * 
     * <p>
     * Much of the Android NFC API must be called in worker threads separate
     * from the UI thread. This class arranges to call the derived class's
     * {@link Tag} handling code in such a worker thread.
     * </p>
     * 
     * @see NfcReaderActivity#processTag(Tag)
     * 
     * @author Kirk
     * 
     */
    private class ProcessTagTask extends AsyncTask<Tag, String, String> {

        /**
         * Invoke {@link NfcReaderActivity#processTag(Tag)} in the worker thread
         * 
         * @see NfcReaderActivity#processTag(Tag)
         */
        @Override
        protected String doInBackground(Tag... tags) {

            try {

                return processTag(tags[0]);

            } catch (Exception e) {

                return e.getMessage();

            }
        }

        /**
         * Report the result of processing the {@link Tag} to the user in an
         * {@link AlertDialog} and then exit this {@link NfcReaderActivity}
         */
        @Override
        protected void onPostExecute(String result) {

            alert(result, true);

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
     * Display the given message in an {@link AlertDialog} and, optionally, exit
     * this {@link NfcReaderActivity}
     * 
     * @param message
     *            the message to display
     * 
     * @param finish
     *            if true, also {@link Activity#finish()} this
     *            {@link NfcReaderActivity}
     */
    protected final void alert(final String message, final boolean finish) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message);

        builder.setNeutralButton(android.R.string.ok,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();

                        if (finish) {

                            finish();
                        }
                    }
                });

        builder.create().show();

    }

    /**
     * Called by {@link #onCreate(Bundle)}
     * 
     * Put any code you would ordinarily put in {@link #onCreate(Bundle)},
     * including Android SDK boilerplate code, here.
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
        initialize(savedInstanceState);
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        Intent intent = new Intent(this, getClass());
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

        if (tag == null) {

            alert("No tag scanned", true); //$NON-NLS-1$

        }

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
     * @return a message to display to the user before exiting this
     *         {@link NfcReaderActivity}
     */
    protected abstract String processTag(Tag tag);

}
