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

import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.nfc.tech.TagTechnology;
import android.os.Parcelable;
import android.util.Log;

/**
 * Abstract super class of {@link NfcReaderActivity} objects that can write NDEF
 * messages to tags
 * 
 * <p>
 * This is implemented by overriding the
 * {@link #processTag(Tag, ProcessTagTask)} method with its own
 * <code>final</code> to write a {@link NdefMessage} to the {@link Tag}. That
 * implementation of {@link #processTag(Tag, ProcessTagTask)} relies on the
 * <code>abstract</code> {@link #createNdefMessage()} method.
 * </p>
 * 
 * <p>
 * Note that the helper methods {@link #createTextRecord(String)} ,
 * {@link #createUriRecord(Uri)} etc. exist here as an alternative to
 * {@link NdefRecord#createUri(Uri)} and the like so as to provide backwards
 * compatibility with the earliest possible versions of the Android OS.
 * </p>
 * 
 * @author Kirk
 * 
 */
public abstract class NdefWriterActivity extends NfcReaderActivity {

    /**
     * Default value for {@link #readOnlyRequested}
     */
    private static final boolean DEFAULT_READ_ONLY_REQUESTED = false;

    /**
     * Helper method for constructing NDEF MIME records
     * 
     * @param type
     *            the MIME type string
     * 
     * @param payload
     *            the payload data
     * 
     * @return the {@link NdefRecord}
     */
    public static NdefRecord createMimeRecord(String type, byte[] payload) {

        try {

            byte[] bytes = type.getBytes("UTF-8"); //$NON-NLS-1$
            return new NdefRecord(NdefRecord.TNF_MIME_MEDIA, bytes, null,
                    payload);

        } catch (UnsupportedEncodingException e) {

            throw new IllegalArgumentException(e);

        }
    }

    /**
     * Helper method for constructing NDEF 'T' records
     * 
     * @param text
     *            the text
     * 
     * @return the {@link NdefRecord}
     */
    public static NdefRecord createTextRecord(String text) {

        try {

            byte[] payload = encodePayload((byte) 0, text, "UTF-8"); //$NON-NLS-1$
            return new NdefRecord(NdefRecord.TNF_WELL_KNOWN,
                    NdefRecord.RTD_TEXT, null, payload);

        } catch (UnsupportedEncodingException e) {

            throw new IllegalArgumentException(e);

        }
    }

    /**
     * Helper method for constructing NDEF 'U' records
     * 
     * @param uri
     *            the {@link Uri}
     * 
     * @return the 'U' {@link NdefRecord}
     */
    public static NdefRecord createUriRecord(Uri uri) {

        try {

            String text = uri.toString();

            if (text.startsWith("http://www.")) { //$NON-NLS-1$

                text = text.substring(11);
                byte[] payload = encodePayload((byte) 1, text, "US-ASCII"); //$NON-NLS-1$
                return new NdefRecord(NdefRecord.TNF_WELL_KNOWN,
                        NdefRecord.RTD_URI, null, payload);

            }

            return new NdefRecord(NdefRecord.TNF_ABSOLUTE_URI,
                    text.getBytes("US-ASCII"), null, null); //$NON-NLS-1$

        } catch (UnsupportedEncodingException e) {

            throw new IllegalArgumentException(e);

        }
    }

    /**
     * Support the extremely bizarre format specified for NDEF 'U' and 'T'
     * records
     * 
     * @param padByte
     *            the value for the first byte in the returned array
     * 
     * @param text
     *            the string whose encoded bytes begin at the second byte of the
     *            returned array
     * 
     * @param encoding
     *            the name of the character set to use when encoding the text
     *            string
     * 
     * @return the encoded payload array
     * 
     * @throws UnsupportedEncodingException
     *             if there is a bug in the VM
     */
    private static byte[] encodePayload(byte padByte, String text,
            String encoding) throws UnsupportedEncodingException {

        byte[] bytes = text.getBytes(encoding);
        byte[] payload = new byte[bytes.length + 1];
        payload[0] = padByte;
        System.arraycopy(bytes, 0, payload, 1, bytes.length);
        return payload;

    }

    /**
     * If <code>true</code>, mark tags read-only during formatting or after
     * writing. Leave tag writable otherwise.
     * 
     * @see #isReadOnlyRequested()
     * @see #setReadOnlyRequested(boolean)
     * @see #processTag(Tag, ProcessTagTask)
     */
    private boolean readOnlyRequested;

    /**
     * Initialize {@link #readOnlyRequested} to
     * {@link #DEFAULT_READ_ONLY_REQUESTED}
     * 
     * @see #NdefWriterActivity(boolean)
     */
    protected NdefWriterActivity() {

        this(DEFAULT_READ_ONLY_REQUESTED);

    }

    /**
     * Initialize {@link #readOnlyRequested} to the given value
     * 
     * @param readOnlyRequested
     *            new value for {@link #readOnlyRequested}
     * 
     * @see #readOnlyRequested
     */
    protected NdefWriterActivity(boolean readOnlyRequested) {

        setReadOnlyRequested(readOnlyRequested);

    }

    /**
     * Gets the state of {@link #readOnlyRequested}
     * 
     * @return {@link #readOnlyRequested}
     * 
     * @see #readOnlyRequested
     * @see #setReadOnlyRequested(boolean)
     */
    public final boolean isReadOnlyRequested() {

        return readOnlyRequested;

    }

    /**
     * Sets the state of {@link #readOnlyRequested}
     * 
     * @param readOnlyRequested
     *            new value for {@link #readOnlyRequested}
     * 
     * @see #readOnlyRequested
     * @see #isReadOnlyRequested()
     */
    public final void setReadOnlyRequested(boolean readOnlyRequested) {

        this.readOnlyRequested = readOnlyRequested;

    }

    /**
     * Return the {@link NdefMessage} to write to the {@link Tag}
     * 
     * @return the {@link NdefMessage}
     */
    protected abstract NdefMessage createNdefMessage();

    /**
     * Write the result of calling {@link #createNdefMessage()} to the given
     * {@link Tag}
     * 
     * Note that this overload of
     * {@link NfcReaderActivity#processTag(Tag, ProcessTagTask)} is deliberately
     * <code>final</code>. Override {@link #createNdefMessage()} to supply the
     * payload to write to the {@link Tag}
     * 
     * @param tag
     *            the {@link Tag} to which to write
     * 
     * @param task
     *            the {@Link ProcessTagTask} executing this method
     * 
     * @see NfcReaderActivity#processTag(Tag, ProcessTagTask)
     */
    @Override
    protected final Parcelable processTag(Tag tag, ProcessTagTask task) {

        try {

            TagTechnology technology = Ndef.get(tag);

            if (technology == null) {

                technology = NdefFormatable.get(tag);

            }

            if (technology == null) {

                task.onProgressUpdate(getString(R.string.not_ndef_formatable));
                return null;

            }

            technology.connect();

            try {

                NdefMessage ndefMessage = createNdefMessage();

                if (ndefMessage == null) {

                    task.onProgressUpdate(getString(R.string.no_ndef_message));
                    return null;

                }

                if (technology instanceof Ndef) {

                    Ndef ndef = (Ndef) technology;
                    ndef.writeNdefMessage(ndefMessage);

                    if (readOnlyRequested) {

                        ndef.makeReadOnly();

                    }

                } else {

                    NdefFormatable formatable = (NdefFormatable) technology;

                    if (readOnlyRequested) {

                        formatable.formatReadOnly(ndefMessage);

                    } else {

                        formatable.format(ndefMessage);

                    }
                }

                return ndefMessage;

            } finally {

                technology.close();
            }

        } catch (Exception e) {

            Log.e(NdefWriterActivity.class.getName(), "processTag", e); //$NON-NLS-1$
            return null;

        }

    }
}
