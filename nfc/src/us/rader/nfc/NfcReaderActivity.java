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

import java.io.UnsupportedEncodingException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefRecord;
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
     * Uri's that start with this string will be encoded using
     * {@link NdefRecord#TNF_WELL_KNOWN} and {@link NdefRecord#RTD_URI} rather
     * than {@link NdefRecord#TNF_ABSOLUTE_URI}
     */
    public static final String WELL_KNOWN_URI_PREFIX = "http://www."; //$NON-NLS-1$

    /**
     * Invoke {@link NfcReaderActivity#processTag(Tag)} asynchronously.
     * 
     * Much of the Android NFC API must be called in worker threads separate
     * from the UI thread. Notifications of state changes, on the other hand,
     * should be handled on the main UI thread. This class uses the
     * {@link AsyncTask} to arrange to call the derived class's {@link Tag}
     * handling code in the correct threads
     * 
     * @see NfcReaderActivity#processTag(Tag)
     * 
     * @author Kirk
     * 
     */
    private class ProcessTagTask extends AsyncTask<Tag, String, Parcelable> {

        /**
         * The result code to pass back to the {@link Activity} that started
         * this one
         */
        private int resultCode;

        /**
         * Invoke {@link NfcReaderActivity#processTag(Tag)} in the worker thread
         * 
         * @param tags
         *            pass <code>tags[0]</code> to
         *            {@link NfcReaderActivity#processTag(Tag)}
         * 
         * @see NfcReaderActivity#processTag(Tag)
         */
        @Override
        protected Parcelable doInBackground(Tag... tags) {

            try {

                resultCode = RESULT_OK;
                return processTag(tags[0]);

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

            onTagProcessed(resultCode, result);

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
     * invoking {@link #processTag(Tag)} to the {@link Activity} that started
     * this one
     */
    public static final String EXTRA_RESULT = "us.rader.nfc.result"; //$NON-NLS-1$

    /**
     * Decode the payload of the given {@link NdefRecord} according to the rules
     * for the various kinds of NDEF messages it might contain
     * 
     * This will return the string representation of the payload or
     * <code>null</code> if there is no way to deduce the correct string
     * 
     * @param record
     *            the {@link NdefRecord}
     * 
     * @return the payload as a string, or <code>null</code> if no such mapping
     *         can be deduced merely from the {@link NdefRecord} properties
     */
    public static String decodePayload(NdefRecord record) {

        try {

            if (record == null) {

                return null;

            }

            byte[] type = record.getType();
            byte[] payload = record.getPayload();

            switch (record.getTnf()) {

                case NdefRecord.TNF_ABSOLUTE_URI:

                    return new String(type, "US-ASCII"); //$NON-NLS-1$

                case NdefRecord.TNF_WELL_KNOWN:

                    return decodeWellKnown(type, payload);

                default:

                    return null;

            }

        } catch (UnsupportedEncodingException e) {

            Log.e(NfcReaderActivity.class.getName(), "decodePayload", e); //$NON-NLS-1$
            throw new IllegalStateException(e);

        }
    }

    /**
     * Test two byte arrays for equality
     * 
     * Special-case version of <code>equals</code> that is a missing critical
     * feature in Java
     * 
     * @param array1
     *            the first array
     * 
     * @param array2
     *            the second array
     * 
     * @return <code>true</code> if and only if both parameters are
     *         <code>null</code> or byte arrays of identical length with
     *         identical contents
     */
    protected static boolean equals(byte[] array1, byte[] array2) {

        if (array1 == null) {

            return (array2 == null);

        }

        if (array2 == null) {

            return false;

        }

        if (array1.length != array2.length) {

            return false;

        }

        for (int index = 0; index < array1.length; ++index) {

            if (array1[index] != array2[index]) {

                return false;

            }

        }

        return true;

    }

    /**
     * Return the given payload as a string based on the given type
     * 
     * @param type
     *            the {@link NdefRecord#getType()} value denoting how to
     *            interpret <code>payload</code>
     * 
     * @param payload
     *            the payload bytes
     * 
     * @return the string that results from decoding the given payload array
     * 
     * @throws UnsupportedEncodingException
     *             if there is a bug in the VM
     */
    private static String decodeWellKnown(byte[] type, byte[] payload)
            throws UnsupportedEncodingException {

        if (equals(type, NdefRecord.RTD_TEXT)) {

            return new String(payload, 1, payload.length - 1, "UTF-8"); //$NON-NLS-1$

        } else if (equals(type, NdefRecord.RTD_URI)) {

            return WELL_KNOWN_URI_PREFIX
                    + new String(payload, 1, payload.length - 1, "US-ASCII"); //$NON-NLS-1$

        } else {

            return null;

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
     * Handle the result of having processed a {@link Tag}
     * 
     * This method is invoked by
     * {@link ProcessTagTask#onPostExecute(Parcelable)} and is passed the value
     * returned by {@link #processTag(Tag)}. The default implementation is to
     * use {@link Activity#setResult(int, Intent)} followed by
     * {@link Activity#finish()} on the assumption that this activity was
     * started for its result, but this class can be overridden to supplement or
     * replace that default behavior.
     * 
     * @param resultCode
     *            the result code set in
     *            {@link ProcessTagTask#doInBackground(Tag...)}
     * 
     * @param result
     *            the value returned by {@link #processTag(Tag)}
     */
    protected void onTagProcessed(int resultCode, Parcelable result) {

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
     * Handle the result of scanning a NFC {@link Tag}
     * 
     * Note that this will be invoked in a worker thread separate from the UI
     * thread, a fact that can be relied on and must be taken account of by
     * derived classes' implementations of this <code>abstract</code> method.
     * 
     * @param tag
     *            the {@link Tag}
     * 
     * @return the result of processing the {@link Tag}
     */
    protected abstract Parcelable processTag(Tag tag);

}
