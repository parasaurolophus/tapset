package us.rader.nfc;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
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
 * returned by {@link #processTag(Tag)} and expected by
 * {@link #onTagProcessed(Parcelable)} . It is constrained to extend
 * {@link Parcelable} so that such values can be easily passed from one
 * {@link Activity} to another as {@link Intent} extras and similar facilities
 * that require marshaling and un-marshaling via a {@link Parcel}.
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
 * @param <ContentType>
 *            the type of result to return from {@link #processTag(Tag)}
 * 
 * @see #processTag(Tag)
 * @see #onTagProcessed(Parcelable)
 * @see NdefReaderActivity
 * @see NdefWriterActivity
 * 
 * @author Kirk
 */
public abstract class NfcReaderActivity<ContentType extends Parcelable> extends
        Activity {

    /**
     * Invoke {@Link NfcReaderActivity#processTag(Tag)} and {@Link
     *  NfcReaderActivity#onTagProcessed(Parcelable)} asynchronously
     * 
     * Much of the NFC related API must be invoked on a worker thread separate
     * from the main UI thread, and yet the app also must be able to update the
     * state of its UI once the contents of a tag has been obtained. This
     * {@link AsyncTask} arranges to invoke the appropriate methods in the
     * appropriate threads.
     * 
     * @see NfcReaderActivity#processTag(Tag)
     * @see NfcReaderActivity#onTagProcessed(Parcelable)
     * 
     * @author Kirk
     */
    private class ProcessTagTask extends AsyncTask<Tag, Void, ContentType> {

        /**
         * Invoke {@link NfcReaderActivity#processTag(Tag)} in a worker thread
         * 
         * @param tags
         *            <code>tags[0]</code> is the {@link Tag} obtained using the
         *            foreground dispatch mechanism
         * 
         * @return the value returned by
         *         {@link NfcReaderActivity#processTag(Tag)}
         * 
         * @see AsyncTask#doInBackground(Object...)
         * @see NfcReaderActivity#processTag(Tag)
         */
        @Override
        protected ContentType doInBackground(Tag... tags) {

            try {

                setLastStatus(null);
                return processTag(tags[0]);

            } catch (Exception e) {

                Log.e(getClass().getName(), "error processing tag", e); //$NON-NLS-1$

                if (getLastStatus() == null) {

                    setLastStatus(e.getMessage());

                }

                return null;

            }
        }

        /**
         * Invoke {@link NfcReaderActivity#onTagProcessed(Parcelable)} on the UI
         * thread
         * 
         * @param result
         *            the value returned by
         *            {@link NfcReaderActivity#processTag(Tag)}
         * 
         * @see AsyncTask#onPostExecute(Object)
         * @see NfcReaderActivity#processTag(Tag)
         * @see NfcReaderActivity#onTagProcessed(Parcelable)
         */
        @Override
        protected void onPostExecute(ContentType result) {

            try {

                onTagProcessed(result);

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
     * Set to <code>null</code> before each invocation of
     * {@link #processTag(Tag)} and set to a diagnostic string if any errors
     * occur
     * 
     * @see #getLastStatus()
     */
    private String         lastStatus;

    /**
     * Cached {@link PendingIntent} for use in conjunction with the foreground
     * dispatch mechanism
     * 
     * @see #onResume()
     */
    private PendingIntent  pendingIntent;

    /**
     * Set {@link #lastStatus} to <code>null</code>
     */
    protected NfcReaderActivity() {

        lastStatus = null;

    }

    /**
     * Create the {@link IntentFilter} array to use when enabling foreground
     * dispatch
     * 
     * @return the {@link IntentFilter} array
     */
    public abstract IntentFilter[] createNfcIntentFilters();

    /**
     * Status message or <code>null</code> if no errors occurred during most
     * recent invocation of {@link #processTag(Tag)}
     * 
     * @return the most recent status message or <code>null</code>
     */
    public final String getLastStatus() {

        return lastStatus;

    }

    /**
     * Set {@link #lastStatus} to the given value
     * 
     * @param lastStatus
     *            the new value for {@link #lastStatus}
     */
    public final void setLastStatus(String lastStatus) {

        this.lastStatus = lastStatus;

    }

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
     * This method exists separately from {@link #processTag(Tag)} because of
     * the different contexts in which they are invoked. Specifically,
     * {@link #processTag(Tag)} is invoked in a worker thread as required by
     * most of the NFC API, while {@link #onTagProcessed(Parcelable)} is invoked
     * in the UI thread
     * 
     * @param result
     *            the value returned by {@link #processTag(Tag)}
     * 
     * @see #processTag(Tag)
     */
    protected abstract void onTagProcessed(ContentType result);

    /**
     * Process a {@link Tag}
     * 
     * This method can rely on, and must take account of being called in a
     * worker thread separate from the main UI.
     * 
     * @param tag
     *            the {@link Tag}
     * 
     * @return the app-specific data structure that is the result of having
     *         processed the {@link Tag}
     */
    protected abstract ContentType processTag(Tag tag);

}
