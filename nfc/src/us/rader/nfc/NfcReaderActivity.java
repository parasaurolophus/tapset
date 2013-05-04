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
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.TagTechnology;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;

/**
 * Abstract super class of {@link Activity} objects that wait for a NFC
 * {@link Tag} to be scanned and process it in some way
 * 
 * <p>
 * Most apps that access read or write NFC tags must have at least one
 * {@link Activity} that uses the {@link NfcAdapter} foreground dispatch
 * mechanism. Doing so requires a good deal of boilerplate logic not only to
 * properly inter-operate with the NF API at specific points in the
 * {@link Activity} life-cycle, but also must take care to do so in a way that
 * ensure that the correct operations are are carried out in the appropriate
 * threads.
 * </p>
 * 
 * <p>
 * In addition, the NDEF records that are used by most applications require
 * complex formatting and parsing logic that is not well supported in any but
 * the very most recent versions of the Android SDK.
 * </p>
 * 
 * <p>
 * This class and {@link NdefWriterActivity} which extends it, provide the
 * standard boilerplate code for any application that uses these Android
 * features to read and write NFC tags, respectively.
 * </p>
 * 
 * @param <ResultType>
 *            the type of object returned by {@link #processTag(Intent, Tag)}
 *            and expected by {@link #onTagProcessed(int, Parcelable)}
 * 
 * @see #onCreate(Bundle)
 * @see #onResume()
 * @see #onPause()
 * @see #onNewIntent(Intent)
 * 
 * @author Kirk
 */
public abstract class NfcReaderActivity<ResultType extends Parcelable> extends
        Activity {

    /**
     * Invoke {@link NfcReaderActivity#processTag(Intent, Tag)} asynchronously.
     * 
     * Much of the Android NFC API must be called in worker threads separate
     * from the UI thread. Notifications of state changes, on the other hand,
     * should be handled on the main UI thread. This class uses the
     * {@link AsyncTask} to arrange to call the derived class's {@link Tag}
     * handling code in the correct threads
     * 
     * @see NfcReaderActivity#processTag(Intent, Tag)
     * 
     * @author Kirk
     * 
     */
    private class ProcessTagTask extends AsyncTask<Void, Void, ResultType> {

        /**
         * The {@link Intent} passed to
         * {@link NfcReaderActivity#onNewIntent(Intent)}
         */
        private Intent newIntent;

        /**
         * The {@link Tag} extracted from {@link #newIntent} as a convenience
         */
        private Tag    tag;

        /**
         * Initialize {@link #newIntent} and {@link #tag}
         * 
         * @param newIntent
         *            the {@link Intent}
         * 
         * @param tag
         *            the {@link Tag}
         */
        public ProcessTagTask(Intent newIntent, Tag tag) {

            this.newIntent = newIntent;
            this.tag = tag;

        }

        /**
         * The result code to pass back to the {@link Activity} that started
         * this one
         */
        private int resultCode;

        /**
         * Invoke {@link NfcReaderActivity#processTag(Intent, Tag)} in the
         * worker thread
         * 
         * @param params
         *            ignored, since the relevent values were passed to the
         *            constructor
         * 
         * @see NfcReaderActivity#processTag(Intent, Tag)
         */
        @Override
        protected ResultType doInBackground(Void... params) {

            try {

                resultCode = RESULT_OK;
                return processTag(newIntent, tag);

            } catch (ProcessTagException e) {

                Log.e(getClass().getName(), "doInBackground", e); //$NON-NLS-1$
                resultCode = e.getResultCode();
                return null;

            } catch (Exception e) {

                Log.e(getClass().getName(), "doInBackground", e); //$NON-NLS-1$
                resultCode = RESULT_TECHNOLOGY_ERROR;
                return null;

            }
        }

        /**
         * Report the result of processing the {@link Tag} to the user in an
         * {@link AlertDialog} and then exit this {@link NfcReaderActivity}
         */
        @Override
        protected void onPostExecute(ResultType result) {

            onTagProcessed(resultCode, result);

        }

    }






























    /**
     * {@link Intent#getStringExtra(String)} key used to return result of
     * invoking {@link #processTag(Intent, Tag)} to the {@link Activity} that started
     * this one
     */
    public static final String   EXTRA_RESULT            = "us.rader.nfc.result"; //$NON-NLS-1$

    /**
     * Result code indicating that there is no {@link NdefMessage} to process
     */
    public static final int      RESULT_NO_MESSAGE       = RESULT_FIRST_USER;

    /**
     * Result code indicating that the {@link Tag} passed to
     * {@link #processTag(Intent, Tag)} was <code>null</code>
     */
    public static final int      RESULT_NO_TAG           = RESULT_FIRST_USER + 1;

    /**
     * Result code indicating that the {@link Tag} passed to
     * {@link #processTag(Intent, Tag)} was <code>null</code>
     */
    public static final int      RESULT_NOT_FORMATABLE   = RESULT_FIRST_USER + 2;

    /**
     * Result code indicating that some method of some class that implements
     * {@link TagTechnology} threw an exception
     */
    public static final int      RESULT_TECHNOLOGY_ERROR = RESULT_FIRST_USER + 3;

    /**
     * The special-case URI prefixes for 'U' NDEF records
     */
    protected static final String[] WELL_KNOWN_URI_PREFIXES = {

    //@formatter:off

        // not a recognized prefix, use code 0
        "",             //$NON-NLS-1$

        // well-known prefix code 1
        "http://www.",  //$NON-NLS-1$

        // well-known prefix code 2
        "https://www.", //$NON-NLS-1$

        // well-known prefix code 3
        "http://",      //$NON-NLS-1$

        // well-known prefix code 4
        "https://"      //$NON-NLS-1$

    };

    //@formatter:on

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

            int index = payload[0] + 1;
            int textLength = payload.length - index;
            return new String(payload, index, textLength, "UTF-8"); //$NON-NLS-1$

        } else if (equals(type, NdefRecord.RTD_URI)) {

            return WELL_KNOWN_URI_PREFIXES[payload[0]]
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
        ProcessTagTask processTagTask = new ProcessTagTask(intent, tag);
        processTagTask.execute();

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
     * returned by {@link #processTag(Intent, Tag)}. The default implementation
     * is to use {@link Activity#setResult(int, Intent)} followed by
     * {@link Activity#finish()} on the assumption that this activity was
     * started for its result, but this class can be overridden to supplement or
     * replace that default behavior.
     * 
     * @param resultCode
     *            the result code set in
     *            {@link ProcessTagTask#doInBackground(Void...)}
     * 
     * @param result
     *            the value returned by {@link #processTag(Intent, Tag)}
     */
    protected void onTagProcessed(int resultCode, ResultType result) {

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
     * @param newIntent
     *            the {@link Intent} passed to {@link #onNewIntent(Intent)} in
     *            the response to the foreground dispatch request
     * 
     * @param tag
     *            the {@link Tag} extracted from <code>newIntent</code> as a
     *            convenience
     * 
     * @return the result of processing the {@link Tag}
     * 
     * @throws ProcessTagException
     *             if an error occurs while processing the {@link Tag}
     */
    protected abstract ResultType processTag(Intent newIntent, Tag tag)
            throws ProcessTagException;

}
