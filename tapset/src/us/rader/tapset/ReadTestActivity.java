package us.rader.tapset;

import us.rader.nfc.NfcReaderActivity;
import android.annotation.TargetApi;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * {@link NfcReaderActivity} used for unit testing purposes, only
 * 
 * @author Kirk
 * 
 */
public class ReadTestActivity extends NfcReaderActivity {

    /**
     * This of strings to display in the UI
     * 
     * This is initialized to an empty list in {@link #onCreate(Bundle)} and
     * updated in {@link #processTag(Tag)}. The list is actually displayed in
     * {@link #onTagProcessed(int, Parcelable)}
     */
    private String[] items;

    /**
     * Inflate the menu; this adds items to the action bar if it is present.
     * 
     * @param menu
     *            the options {@link Menu}
     * 
     * @return <code>true</code>
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.read_test, menu);
        return true;

    }

    /**
     * Handle an options {@link MenuItem}
     * 
     * @param item
     *            the {@link MenuItem}
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
     * Prepare this instance to be displayed
     * 
     * @param savedInstanceState
     *            the saved state of the UI or <code>null</code>
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_test);
        // Show the Up button in the action bar.
        setupActionBar();
        items = new String[0];

    }

    /**
     * Don't set a result or terminate this activity
     * 
     * Just update the UI
     * 
     * @see us.rader.nfc.NfcReaderActivity#onTagProcessed(int,
     *      android.os.Parcelable)
     */
    @Override
    protected void onTagProcessed(int resultCode, Parcelable result) {

        ListView listView = (ListView) findViewById(R.id.listView1);
        listView.setAdapter(new ArrayAdapter<String>(this,
                R.layout.list_item_view, items));

    }

    /**
     * Display the contents of the {@link Tag} to the user
     */
    @Override
    protected Parcelable processTag(Tag tag) {

        try {

            Ndef ndef = Ndef.get(tag);

            if (ndef == null) {

                return null;

            }

            ndef.connect();

            try {

                NdefMessage message = ndef.getNdefMessage();

                if (message == null) {

                    return null;

                }

                NdefRecord[] records = message.getRecords();
                items = new String[records.length];

                for (int index = 0; index < records.length; ++index) {

                    NdefRecord record = records[index];
                    items[index] = decodePayload(record);

                    if (items[index] == null) {

                        StringBuffer buffer = new StringBuffer();
                        boolean comma = false;

                        for (byte b : record.getPayload()) {

                            if (comma) {

                                buffer.append(", "); //$NON-NLS-1$

                            } else {

                                comma = true;

                            }

                            buffer.append(Byte.toString(b));

                        }

                        items[index] = buffer.toString();

                    }
                }

                return message;

            } finally {

                ndef.close();

            }

        } catch (Exception e) {

            Log.e(getClass().getName(), "processTag", e); //$NON-NLS-1$
            return null;

        }
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setupActionBar() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {

            getActionBar().setDisplayHomeAsUpEnabled(true);

        }
    }

}
