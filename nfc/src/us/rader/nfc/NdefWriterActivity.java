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

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import android.content.Intent;
import android.net.Uri;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.nfc.tech.TagTechnology;

/**
 * Abstract super class of {@link NfcReaderActivity} objects that can write NDEF
 * messages to tags
 * 
 * <p>
 * This is implemented by overriding the {@link #processTag(Intent, Tag)} method
 * with its own <code>final</code> to write a {@link NdefMessage} to the
 * {@link Tag}. That implementation of {@link #processTag(Intent, Tag)} relies
 * on the <code>abstract</code> {@link #createNdefMessage()} method.
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
     * If <code>true</code>, mark tags read-only during formatting or after
     * writing. Leave tag writable otherwise.
     * 
     * @see #isReadOnlyRequested()
     * @see #setReadOnlyRequested(boolean)
     * @see #processTag(Intent, Tag)
     * @see #writeNdef(Ndef, NdefMessage)
     * @see #writeNdefFormatable(NdefFormatable, NdefMessage)
     */
    private boolean              readOnlyRequested;

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
    protected final NdefRecord createMimeRecord(String type, byte[] payload) {

        try {

            byte[] bytes = type.getBytes("UTF-8"); //$NON-NLS-1$
            return new NdefRecord(NdefRecord.TNF_MIME_MEDIA, bytes, null,
                    payload);

        } catch (UnsupportedEncodingException e) {

            throw new IllegalArgumentException(e);

        }
    }

    /**
     * Return the {@link NdefMessage} to write to the {@link Tag}
     * 
     * @return the {@link NdefMessage}
     */
    protected abstract NdefMessage createNdefMessage();

    /**
     * Helper method for constructing NDEF 'T' records
     * 
     * @param text
     *            the text
     * 
     * @return the {@link NdefRecord}
     */
    protected final NdefRecord createTextRecord(String text) {

        try {

            byte[] bytes = text.getBytes("UTF-8"); //$NON-NLS-1$
            byte[] payload = new byte[bytes.length + 1];
            System.arraycopy(bytes, 0, payload, 1, bytes.length);
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
    protected final NdefRecord createUriRecord(Uri uri) {

        try {

            String string = uri.toString();

            if (string.startsWith("http://www.")) { //$NON-NLS-1$

                string = string.substring(11);
                byte[] bytes = string.getBytes("US-ASCII"); //$NON-NLS-1$
                byte[] payload = new byte[bytes.length + 1];
                payload[0] = 1;
                System.arraycopy(bytes, 0, payload, 1, bytes.length);
                return new NdefRecord(NdefRecord.TNF_WELL_KNOWN,
                        NdefRecord.RTD_URI, null, payload);

            }

            return new NdefRecord(NdefRecord.TNF_ABSOLUTE_URI,
                    string.getBytes("US-ASCII"), null, null); //$NON-NLS-1$

        } catch (UnsupportedEncodingException e) {

            throw new IllegalArgumentException(e);

        }
    }

    /**
     * Write the result of calling {@link #createNdefMessage()} to the given
     * {@link Tag}
     * 
     * Note that this overload of
     * {@link NfcReaderActivity#processTag(Intent, Tag)} is deliberately
     * <code>final</code>. Override {@link #createNdefMessage()} to supply the
     * payload to write to the {@link Tag}
     * 
     * @param intent
     *            ignored
     * 
     * @param tag
     *            the {@link Tag} to which to write
     * 
     * @see NfcReaderActivity#processTag(Intent, Tag)
     * @see #writeNdef(Ndef, NdefMessage)
     * @see #writeNdefFormatable(NdefFormatable, NdefMessage)
     */
    @Override
    protected final String processTag(Intent intent, Tag tag) {

        try {

            NdefMessage ndefMessage = createNdefMessage();

            if (ndefMessage == null) {

                return getString(R.string.no_ndef_message);

            }

            Ndef ndef = Ndef.get(tag);

            if (ndef == null) {

                NdefFormatable ndefFormatable = NdefFormatable.get(tag);

                if (ndefFormatable == null) {

                    return getString(R.string.not_ndef_formatable);

                }

                return writeNdefFormatable(ndefFormatable, ndefMessage);
            }

            return writeNdef(ndef, ndefMessage);

        } catch (Exception e) {

            Throwable cause = e;

            while (cause.getCause() != null) {

                cause = cause.getCause();

            }

            String message = cause.getMessage();

            if ((message == null) || "".equals(message)) { //$NON-NLS-1$

                message = "unknown error writing tag"; //$NON-NLS-1$

            }

            return message;

        }

    }

    /**
     * Write the given {@link NdefMessage} to the given {@link Ndef}
     * pre-formatted or non-empty tag
     * 
     * While it would be nice to have a single method for writing to any
     * {@link TagTechnology} instance, that is not practical given Android's
     * rather ill-thought-out NFC API.
     * 
     * @param ndef
     *            the {@link Ndef} tag
     * 
     * @param ndefMessage
     *            the {@link NdefMessage}
     * 
     * @return the message to display to the user as feedback on the result of
     * 
     *         this attempt to write to a tag
     * 
     * @throws IOException
     *             thrown if an I/O error is signaled by the NFC API
     * 
     * @throws FormatException
     *             thrown if a format error is signaled by the NFC API
     * 
     * @see #writeNdefFormatable(NdefFormatable, NdefMessage)
     */
    private String writeNdef(Ndef ndef, NdefMessage ndefMessage)
            throws IOException, FormatException {

        if (!ndef.isWritable()) {

            return getString(R.string.not_writable);

        }

        byte[] bytes = ndefMessage.toByteArray();
        int length = bytes.length;
        int maxLength = ndef.getMaxSize();

        if (length > maxLength) {

            return getString(R.string.insufficient_space, maxLength, length);

        }

        ndef.connect();

        try {

            ndef.writeNdefMessage(ndefMessage);

            if (readOnlyRequested) {

                if (!ndef.canMakeReadOnly()) {

                    return getString(R.string.read_only_not_supported);

                }

                ndef.makeReadOnly();

            }

            return getString(R.string.success_writing_tag, maxLength, length);

        } finally {

            ndef.close();

        }
    }

    /**
     * Write the given {@link NdefMessage} to the given {@link NdefFormatable}
     * tag
     * 
     * While it would be nice to have a single method for writing to any
     * {@link TagTechnology} instance, that is not practical given Android's
     * rather ill-thought-out NFC API.
     * 
     * @param ndefFormatable
     *            the {@link NdefFormatable} tag
     * 
     * @param ndefMessage
     *            the {@link NdefMessage}
     * 
     * @return the message to display to the user as feedback on the result of
     *         this attempt to write to a tag
     * 
     * @throws IOException
     *             thrown if an I/O error is signaled by the NFC API
     * 
     * @throws FormatException
     *             thrown if a format error is signaled by the NFC API
     * 
     * @see #writeNdef(Ndef, NdefMessage)
     */
    private String writeNdefFormatable(NdefFormatable ndefFormatable,
            NdefMessage ndefMessage) throws IOException, FormatException {

        ndefFormatable.connect();

        try {

            if (readOnlyRequested) {

                ndefFormatable.formatReadOnly(ndefMessage);

            } else {

                ndefFormatable.format(ndefMessage);

            }

            return getString(R.string.success_formatting_tag,
                    ndefMessage.toByteArray().length);

        } finally {

            ndefFormatable.close();

        }
    }
}
