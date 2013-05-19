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
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

/**
 * Super class for any {@link Activity} that uses the {@link NfcAdapter}
 * foreground dispatch mechanism
 * 
 * <p>
 * The foreground dispatch mechanism is the Android API for allowing an app to
 * wait for the user to scan a NFC tag and then take some action based on the
 * tag's contents, while bypassing any other apps that might be registered for
 * the same type of content. It is implemented by overriding particular
 * {@link Activity} life-cycle methods in specific ways. This class provides
 * derived classes with the boilerplate implementation necessary to use the
 * foreground dispatch mechanism, defining its own overridable methods to allow
 * app-specific processing of the tags' contents.
 * </p>
 * 
 * <p>
 * The generic parameter, <code>ContentType</code>, is declared as the type
 * returned by {@link #processTag(Tag, ProcessTagTask)} and expected by
 * {@link #onTagProcessed(Object, ProcessTagOutcome)}
 * </p>
 * 
 * <p>
 * This class presents the most bare-bones access to any NFC tag supported by
 * the drivers on any given device. If your need is to support a particular tag
 * technology, e.g. NDEF formatted tags, you should extend one of the
 * technology-specific library classes derived from this one rather than
 * extending this class directly.
 * </p>
 * 
 * <p>
 * Note that almost all of the methods of this class are declared to be either
 * <code>final</code> or <code>abstract</code>. This should be regarded as a
 * feature of good object-oriented design. The goal is to provide functionality
 * to derived classes in a way that is not easily subverted, whether
 * accidentally or deliberately, by the authors of those descendant classes.
 * </p>
 * 
 * @param <ContentType>
 *            the type of result to return from
 *            {@link #processTag(Tag, ProcessTagTask)} and to pass, in turn, to
 *            {@link #onTagProcessed(Object, ProcessTagOutcome)}
 * 
 * @see #createNfcIntentFilters()
 * @see #processTag(Tag, ProcessTagTask)
 * @see #onTagProcessed(Object, ProcessTagOutcome)
 * @see NdefReaderActivity
 * @see NdefWriterActivity
 * 
 * @author Kirk
 */
public abstract class ForegroundDispatchActivity<ContentType> extends Activity {

    /**
     * Invoke {@Link ForegroundDispatchActivity#processTag(Tag,
     * ProcessTagTask))} and {@Link
     * ForegroundDispatchActivity#onTagProcessed(Object, ProcessTagOutcome)}
     * asynchronously
     * 
     * Much of the NFC related API must be invoked on a worker thread separate
     * from the main UI thread, and yet the app also must be able to update the
     * state of its UI once the contents of a tag has been obtained and
     * processed. This {@link AsyncTask} arranges to invoke the appropriate
     * methods in the appropriate threads.
     * 
     * @see ForegroundDispatchActivity#processTag(Tag, ProcessTagTask)
     * @see ForegroundDispatchActivity#onTagProcessed(Object, ProcessTagOutcome)
     * 
     * @author Kirk
     */
    protected final class ProcessTagTask extends
            AsyncTask<Tag, Void, ContentType> {

        /**
         * Additional diagnostic information from the most recent invocation of
         * {@link #processTag(Tag, ProcessTagTask)}
         */
        private ProcessTagOutcome outcome;

        /**
         * Update {@link #outcome}
         * 
         * @param outcome
         *            New value for {@link #outcome}
         */
        public void setOutcome(ProcessTagOutcome outcome) {

            this.outcome = outcome;

        }

        /**
         * Invoke
         * {@link ForegroundDispatchActivity#processTag(Tag, ProcessTagTask)} in
         * a worker thread
         * 
         * This returns the result of calling
         * {@link ForegroundDispatchActivity#processTag(Tag, ProcessTagTask)}
         * which is then, in turn, passed to {@link #onPostExecute(Object)} on
         * the UI thread
         * 
         * @param tags
         *            <code>tags[0]</code> is the {@link Tag} obtained using the
         *            foreground dispatch mechanism
         * 
         * @return the value returned by
         *         {@link ForegroundDispatchActivity#processTag(Tag, ProcessTagTask)}
         * 
         * @see AsyncTask#doInBackground(Object...)
         * @see ForegroundDispatchActivity#processTag(Tag, ProcessTagTask)
         * @see #onPostExecute(Object)
         */
        @Override
        protected ContentType doInBackground(Tag... tags) {

            try {

                outcome = ProcessTagOutcome.NOTHING_TO_DO;
                return processTag(tags[0], this);

            } catch (Exception e) {

                Log.e(getClass().getName(), "error processing tag", e); //$NON-NLS-1$
                outcome = ProcessTagOutcome.TECHNOLOGY_ERROR;
                return null;

            }
        }

        /**
         * Invoke
         * {@link ForegroundDispatchActivity#onTagProcessed(Object, ProcessTagOutcome)}
         * on the UI thread
         * 
         * This is invoked on the UI thread with whatever value was returned by
         * {@link #doInBackground(Tag...)} on the worker thread. This method
         * also assumes that
         * {@link ForegroundDispatchActivity#processTag(Tag, ProcessTagTask)}
         * will have left {@link #outcome} in the correct state to provide
         * additional diagnostic information when
         * {@link ForegroundDispatchActivity#onTagProcessed(Object, ProcessTagOutcome)}
         * is invoked
         * 
         * @param result
         *            the value returned by
         *            {@link ForegroundDispatchActivity#processTag(Tag, ProcessTagTask)}
         * 
         * @see AsyncTask#onPostExecute(Object)
         * @see ForegroundDispatchActivity#processTag(Tag, ProcessTagTask)
         * @see ForegroundDispatchActivity#onTagProcessed(Object,
         *      ProcessTagOutcome)
         */
        @Override
        protected void onPostExecute(ContentType result) {

            try {

                onTagProcessed(result, outcome);

            } catch (Exception e) {

                Log.e(getClass().getName(), "error processing tag content", e); //$NON-NLS-1$

            }
        }

    }

    /**
     * Cached instance of {@link NfcAdapter} for use in conjunction with the
     * foreground dispatch mechanism
     * 
     * @see #onPause()
     * @see #onResume()
     */
    private NfcAdapter     adapter;

    /**
     * Cached {@link IntentFilter} array for use in conjunction with the
     * foreground dispatch mechanism
     * 
     * @see #onResume()
     */
    private IntentFilter[] filters;

    /**
     * Cached {@link PendingIntent} for use in conjunction with the foreground
     * dispatch mechanism
     * 
     * @see #onResume()
     */
    private PendingIntent  pendingIntent;

    /**
     * Create the {@link IntentFilter} array to use when enabling foreground
     * dispatch
     * 
     * @return the {@link IntentFilter} array
     */
    protected abstract IntentFilter[] createNfcIntentFilters();

    /**
     * Initialize the data structures used in conjunction with the foreground
     * dispatch mechanism
     * 
     * @param savedInstanceState
     *            saved state of the UI or <code>null</code>
     * 
     * @see android.app.Activity#onCreate(android.os.Bundle)
     * @see #onNewIntent(Intent)
     * @see #onPause()
     * @see #onResume()
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // cache the default NFC adapater
        adapter = NfcAdapter.getDefaultAdapter(this);

        // create the PendingIntent
        Intent intent = new Intent(this, getClass());
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        // create the IntentFilter array
        filters = createNfcIntentFilters();

    }

    /**
     * This method is called by the foreground dispatch mechanism when a
     * {@link Tag} has been detected and is ready to be processed
     * 
     * This method uses {@link ProcessTagTask} to process the {@link Tag}
     * obtained from the given {@link Tag} asynchronously and is
     * <code>final</code> to protect derived classes from accidentally
     * subverting the tightly coupled interaction between
     * {@link #onNewIntent(Intent)} , {@Link #onPause()} and
     * {@link #onResume()}
     * 
     * @param intent
     *            the {@link Intent} containing the data returned to this app
     *            from the foreground dispatch mechanism
     * 
     * @see android.app.Activity#onNewIntent(android.content.Intent)
     * @see #onPause()
     * @see #onResume()
     * @see ProcessTagTask
     */
    @Override
    protected final void onNewIntent(Intent intent) {

        super.onNewIntent(intent);
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        new ProcessTagTask().execute(tag);

    }

    /**
     * Disable foreground dispatch when this {@link Activity} is paused
     * 
     * This method is <code>final</code> so as to protect derived classes from
     * accidentally subverting the tightly coupled behavior of
     * {@link #onNewIntent(Intent)} , {@link #onPause()} and {@link #onResume()}
     * 
     * @see android.app.Activity#onPause()
     * @see #onNewIntent(Intent)
     * @see #onResume()
     */
    @Override
    protected final void onPause() {

        super.onPause();
        adapter.disableForegroundDispatch(this);

    }

    /**
     * Enable foreground dispatch when this {@link Activity} is resumed
     * 
     * This method is <code>final</code> so as to protect derived classes from
     * accidentally subverting the tightly coupled behavior of
     * {@link #onNewIntent(Intent)} , {@link #onPause()} and {@link #onResume()}
     * 
     * @see android.app.Activity#onResume()
     * @see #onNewIntent(Intent)
     * @see #onPause()
     */
    @Override
    protected final void onResume() {

        super.onRestart();
        adapter.enableForegroundDispatch(this, pendingIntent, filters, null);

    }

    /**
     * Handle the result of processing a {@link Tag}
     * 
     * This method exists separately from
     * {@link #processTag(Tag, ProcessTagTask)} because of the different
     * contexts in which they are invoked. Specifically,
     * {@link #processTag(Tag, ProcessTagTask)} is invoked in a worker thread as
     * required by most of the NFC API, while
     * {@link #onTagProcessed(Object, ProcessTagOutcome)} is invoked in the UI
     * thread
     * 
     * @param result
     *            the value returned by {@link #processTag(Tag, ProcessTagTask)}
     * 
     * @param outcome
     *            Additional diagnostic information
     * 
     * @see #processTag(Tag, ProcessTagTask)
     */
    protected abstract void onTagProcessed(ContentType result,
            ProcessTagOutcome outcome);

    /**
     * Process a {@link Tag}
     * 
     * This method can rely on, and must take account of being called in a
     * worker thread separate from the main UI
     * 
     * @param tag
     *            the {@link Tag}
     * 
     * @param task
     *            use{@link ProcessTagTask#setOutcome(ProcessTagOutcome)} to set
     *            additional diagnostic information
     * 
     * @return the app-specific data structure that is the result of having
     *         processed the {@link Tag}
     */
    protected abstract ContentType processTag(Tag tag, ProcessTagTask task);

}
