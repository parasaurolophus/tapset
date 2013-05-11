package us.rader.nfctest;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import us.rader.nfc.NdefWriterActivity;
import android.app.Activity;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Unit test app for the <code>nfc</code> library
 * 
 * @author Kirk
 * 
 */
public class MainActivity extends NdefWriterActivity {

    /**
     * The {@link EditText} used to display the contents of a
     * {@link NdefMessage}
     */
    private EditText contentText;

    /**
     * Perform write tests when <code>true</code>, read tests otherwise
     */
    private boolean  writeMode;

    /**
     * Inflate the options {@link Menu}
     * 
     * @param menu
     *            the options {@link Menu}
     * 
     * @return <code>true</code>
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;

    }

    /**
     * Handle options menu item
     * 
     * @param item
     *            the selected {@link MenuItem}
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_write:

                boolean checked = !item.isChecked();
                item.setChecked(checked);
                writeMode = checked;
                return true;

            default:

                return super.onOptionsItemSelected(item);

        }

    }

    /**
     * Create the {@link NdefMessage} to write for unit-test purposes
     * 
     * @param ndef
     *            the {@link NdefMessage}
     */
    @Override
    protected NdefMessage createNdefMessage(Ndef ndef) {

        ArrayList<NdefRecord> records = new ArrayList<NdefRecord>();
        records.add(createText("some text", "en")); //$NON-NLS-1$//$NON-NLS-2$

        try {

            records.add(createMime("text/plain", //$NON-NLS-1$
                    "some more text".getBytes("UTF-8"))); //$NON-NLS-1$//$NON-NLS-2$

        } catch (UnsupportedEncodingException e) {

            display(e.getMessage());

        }

        records.add(createExternal("rader.us", "nfctest", //$NON-NLS-1$//$NON-NLS-2$
                new byte[] { 0, 1, 2 }));
        records.add(createUri("https://www.rader.us/")); //$NON-NLS-1$
        records.add(createUri("nfctest://www.rader.us/")); //$NON-NLS-1$
        return new NdefMessage(records.toArray(new NdefRecord[records.size()]));

    }

    /**
     * Prepare this {@link Activity} to be displayed
     * 
     * @param savedInstanceState
     *            saved stae of the UI or <code>null</code>
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        writeMode = false;
        contentText = (EditText) findViewById(R.id.contentText);

    }

    /**
     * Display the given {@link NdefMessage}
     * 
     * @param message
     *            the {@link NdefMessage}
     */
    @Override
    protected void onTagProcessed(NdefMessage message) {

        if (message == null) {

            contentText.setText(getString(R.string.no_ndef_message));
            return;

        }

        StringBuffer buffer = new StringBuffer();
        int count = 0;

        for (NdefRecord record : message.getRecords()) {

            if (count++ > 0) {

                buffer.append("\n\n"); //$NON-NLS-1$

            }

            try {

                String type = new String(record.getType(), "US-ASCII"); //$NON-NLS-1$
                String rawPayload = decodeRawPayload(record);
                String payload = decodePayload(record);

                buffer.append(record.getTnf());
                buffer.append("\n"); //$NON-NLS-1$
                buffer.append(type);
                buffer.append("\n"); //$NON-NLS-1$
                buffer.append(rawPayload);

                if (payload != null) {

                    buffer.append("\n"); //$NON-NLS-1$
                    buffer.append(payload);

                }

            } catch (Exception e) {

                buffer.append(R.string.error_parsing_ndef_record);

            }
        }

        contentText.setText(buffer.toString());
    }

    /**
     * Return the raw bytes of the payload in a human-readable format
     * 
     * @param record
     *            the {@link NdefRecord}
     * 
     * @return the raw payload in human-readable form
     */
    private String decodeRawPayload(NdefRecord record) {

        StringBuffer buffer = new StringBuffer();

        for (byte b : record.getPayload()) {

            if ((b >= 32) && (b <= 127)) {

                buffer.append((char) b);

            } else {

                buffer.append('[');
                buffer.append(String.format("%02x", b)); //$NON-NLS-1$
                buffer.append(']');

            }
        }

        return buffer.toString();

    }

    /**
     * Return either the result of reading the given {@link Tag} or of writing
     * the value returned by {@link #createNdefMessage(Ndef)} depending on the
     * selected test mode
     */
    @Override
    protected NdefMessage processTag(Tag tag) {

        if (writeMode) {

            return super.processTag(tag);

        }

        Ndef ndef = Ndef.get(tag);

        if (ndef == null) {

            display(getString(R.string.not_ndef));
            return null;

        }

        return ndef.getCachedNdefMessage();

    }

    /**
     * Show a {@link Toast} with the specified message
     * 
     * @param format
     *            the message format string
     * 
     * @param args
     *            the mesage format args
     */
    private void display(String format, Object... args) {

        final String message = String.format(format, args);

        runOnUiThread(new Runnable() {

            @Override
            public void run() {

                Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG)
                        .show();

            }

        });

    }

}
