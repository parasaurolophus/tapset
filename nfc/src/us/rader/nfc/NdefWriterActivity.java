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

import android.net.Uri;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.nfc.tech.TagTechnology;
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
 * Note that the helper methods {@link #createTextRecord(String, String)} ,
 * {@link #createUriRecord(Uri)} etc. exist here as an alternative to
 * {@link NdefRecord#createUri(Uri)} and the like so as to provide backwards
 * compatibility with the earliest possible versions of the Android OS.
 * </p>
 * 
 * @author Kirk
 * 
 */
public abstract class NdefWriterActivity extends NfcReaderActivity<NdefMessage> {

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
     * @param language
     *            the language code (e.g. "en," "fr" etc.)
     * 
     * @return the {@link NdefRecord}
     */
    public static NdefRecord createTextRecord(String text, String language) {

        try {

            byte[] textBytes = text.getBytes("UTF-8"); //$NON-NLS-1$
            byte[] languageBytes = language.getBytes("UTF-8"); //$NON-NLS-1$
            int payloadLength = textBytes.length + languageBytes.length + 1;

            if (payloadLength > 255) {

                throw new IllegalArgumentException(
                        "maximum record size exceeded"); //$NON-NLS-1$

            }

            byte[] payload = new byte[payloadLength];
            payload[0] = (byte) languageBytes.length;
            System.arraycopy(languageBytes, 0, payload, 1, languageBytes.length);
            System.arraycopy(textBytes, 0, payload, languageBytes.length + 1,
                    textBytes.length);
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
            int index = 1;
            String prefix = null;

            while (index < WELL_KNOWN_URI_PREFIXES.length) {

                prefix = WELL_KNOWN_URI_PREFIXES[index];

                if (text.startsWith(prefix)) {

                    break;

                }

                index += 1;

            }

            if (index >= WELL_KNOWN_URI_PREFIXES.length) {

                index = 0;
                prefix = ""; //$NON-NLS-1$

            }

            text = text.substring(prefix.length());
            byte[] bytes = text.getBytes("US-ASCII"); //$NON-NLS-1$
            byte[] payload = new byte[bytes.length + 1];
            payload[0] = (byte) index;
            System.arraycopy(bytes, 0, payload, 1, bytes.length);
            return new NdefRecord(NdefRecord.TNF_WELL_KNOWN,
                    NdefRecord.RTD_URI, null, payload);

        } catch (UnsupportedEncodingException e) {

            throw new IllegalArgumentException(e);

        }
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
     * @see NfcReaderActivity#processTag(Tag, ProcessTagTask)
     * 
     * @throws ProcessTagException
     *             if an error occurs
     */
    @Override
    protected final NdefMessage processTag(Tag tag) throws ProcessTagException {

        try {

            if (tag == null) {

                throw new ProcessTagException(RESULT_NO_TAG,
                        getString(R.string.no_tag));
            }

            TagTechnology technology = Ndef.get(tag);

            if (technology == null) {

                technology = NdefFormatable.get(tag);

            }

            if (technology == null) {

                throw new ProcessTagException(RESULT_NOT_FORMATABLE,
                        getString(R.string.not_ndef_formatable));

            }

            technology.connect();

            try {

                NdefMessage ndefMessage = createNdefMessage();

                if (ndefMessage == null) {

                    throw new ProcessTagException(RESULT_NO_MESSAGE,
                            getString(R.string.no_ndef_message));

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

        } catch (FormatException e) {

            Log.e(NdefWriterActivity.class.getName(), "processTag", e); //$NON-NLS-1$
            throw new ProcessTagException(RESULT_FIRST_USER, e);

        } catch (IOException e) {

            Log.e(NdefWriterActivity.class.getName(), "processTag", e); //$NON-NLS-1$
            throw new ProcessTagException(RESULT_FIRST_USER, e);

        }

    }
}
