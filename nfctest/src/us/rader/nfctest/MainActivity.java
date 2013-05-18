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

package us.rader.nfctest;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import us.rader.nfc.NdefRecordUtilities;
import us.rader.nfc.NdefWriterActivity;
import us.rader.nfc.ProcessTagOutcome;
import android.app.Activity;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

/**
 * Unit test app for the <code>nfc</code> library
 * 
 * @author Kirk
 * 
 */
public class MainActivity extends NdefWriterActivity {

    /**
     * Helper used by
     * {@link MainActivity#onTagProcessed(NdefMessage, ProcessTagOutcome)} to
     * populate a {@link ListView}
     * 
     * @author Kirk
     * 
     */
    private class NdefRecordListAdapter extends SimpleAdapter {

        /**
         * Pass the parameters through to the <code>super</code> constructor
         * 
         * @param data
         *            the list's contents
         * 
         * @param resource
         *            the list entry layout resource
         * 
         * @param from
         *            the array of keys into each {@link Map} in
         *            <code>data</code>
         * 
         * @param to
         *            the array of resource id's in the specified list entry
         *            layout corresponding the keys in <code>from</code>
         */
        public NdefRecordListAdapter(List<Map<String, String>> data,
                int resource, String[] from, int[] to) {

            super(MainActivity.this, data, resource, from, to);
        }

    }

    /**
     * Key used to map the decoded content from a {@link NdefRecord}
     */
    private static final String KEY_CONTENT = "CONTENT"; //$NON-NLS-1$

    /**
     * Key used to map {@link NdefRecord#getPayload()}
     */
    private static final String KEY_PAYLOAD = "PAYLOAD"; //$NON-NLS-1$

    /**
     * Key used to map {@link NdefRecord#getTnf()}
     */
    private static final String KEY_TNF     = "TNF";    //$NON-NLS-1$

    /**
     * Key used to map {@link NdefRecord#getType()}
     */
    private static final String KEY_TYPE    = "TYPE";   //$NON-NLS-1$

    /**
     * The {@link EditText} used to display the contents of a
     * {@link NdefMessage}
     */
    private ListView            contentList;

    /**
     * Perform write tests when <code>true</code>, read tests otherwise
     */
    private boolean             writeMode;

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
     * @param currentContents
     *            the {@link NdefMessage}
     */
    @Override
    protected NdefMessage createNdefMessage(NdefMessage currentContents) {

        ArrayList<NdefRecord> records = new ArrayList<NdefRecord>();
        records.add(NdefRecordUtilities.createText("some text", "en")); //$NON-NLS-1$//$NON-NLS-2$

        try {

            records.add(NdefRecordUtilities.createMime("text/plain", //$NON-NLS-1$
                    "some more text".getBytes("UTF-8"))); //$NON-NLS-1$//$NON-NLS-2$

        } catch (UnsupportedEncodingException e) {

            display(e.getMessage());

        }

        records.add(NdefRecordUtilities.createExternal("rader.us", "nfctest", //$NON-NLS-1$//$NON-NLS-2$
                new byte[] { 0, 1, 2 }));
        records.add(NdefRecordUtilities.createUri("https://www.rader.us/")); //$NON-NLS-1$
        records.add(NdefRecordUtilities.createUri("nfctest://www.rader.us/")); //$NON-NLS-1$
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
        contentList = (ListView) findViewById(R.id.records_list);

    }

    /**
     * Display the given {@link NdefMessage}
     * 
     * @param message
     *            the {@link NdefMessage}
     * 
     * @param outcome
     *            additional diagnostic information
     */
    @Override
    protected void onTagProcessed(NdefMessage message, ProcessTagOutcome outcome) {

        List<Map<String, String>> data = new LinkedList<Map<String, String>>();
        String[] from = new String[] { KEY_TNF, KEY_TYPE, KEY_PAYLOAD,
                KEY_CONTENT };
        int[] to = new int[] { R.id.tnf_text, R.id.type_text,
                R.id.payload_text, R.id.content_text };

        if (message != null) {

            for (NdefRecord record : message.getRecords()) {

                try {

                    Map<String, String> map = new HashMap<String, String>();
                    map.put(KEY_TNF, Short.toString(record.getTnf()));
                    map.put(KEY_TYPE, new String(record.getType(), "US-ASCII")); //$NON-NLS-1$
                    map.put(KEY_PAYLOAD, decodeRawPayload(record));
                    String content = NdefRecordUtilities.decodePayload(record);

                    if (content == null) {

                        content = ""; //$NON-NLS-1$

                    }

                    map.put(KEY_CONTENT, content);
                    data.add(map);

                } catch (Exception e) {

                    Log.e(getClass().getName(), "error parsing NdefRecord", e); //$NON-NLS-1$

                }
            }
        }

        contentList.setAdapter(new NdefRecordListAdapter(data,
                R.layout.ndef_details, from, to));

    }

    /**
     * Return either the result of reading the given {@link Tag} or of writing
     * the value returned by {@link #createNdefMessage(NdefMessage)} depending
     * on the selected test mode
     */
    @Override
    protected NdefMessage processTag(Tag tag, ProcessTagTask task) {

        if (writeMode) {

            return super.processTag(tag, task);

        }

        Ndef ndef = Ndef.get(tag);

        if (ndef == null) {

            display(getString(R.string.not_ndef));
            task.setOutcome(ProcessTagOutcome.NOTHING_TO_DO);
            return null;

        }

        task.setOutcome(ProcessTagOutcome.SUCCESSFUL_READ);
        return ndef.getCachedNdefMessage();

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
