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

import android.app.Activity;
import android.net.Uri;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.util.Log;

/**
 * Super class of any {@link Activity} that prompts the user to scan a NFC tag
 * so as to write a {@link NdefMessage} to it
 * 
 * <p>
 * Since writing a {@link NdefMessage} involves the use of the
 * {@link NfcAdapter} foreground dispatch mechanism in a way that is almost
 * identical to that which is used to read one, this class extends
 * {@link NdefReaderActivity} in a manner to which object-oriented purists might
 * object (pun intended) as being "inheritance for implementation," to which the
 * author replies, "even if you were correct, what is your point?" :-)
 * </p>
 * 
 * <p>
 * One way of dispensing with such criticism would be simply to give the
 * ancestor classes more cumbersome names, like
 * <code>NfcForegroundDispatcherActivity</code> and so on. That, of course, is
 * the real point of the "what is your point?" quip: complaints about
 * "inheritance for implementation" usually miss the point of why
 * object-oriented programming is useful and powerful to start with. It also is
 * often, as in this case, more a matter of terminology and point of view than
 * of objective (again, pun intended) reality. If two classes really share
 * enough in common to make "inheritance for implementation" useful, there
 * probably is some kind of "is a" relationship between them, even if it is
 * sometimes obfuscated a bit by type names chosen for clarity rather than
 * theoretical precision.
 * </p>
 * 
 * <p>
 * As a side note along these lines, consider that there has been enough
 * disagreement about the validity of the proscription against
 * "inheritance for implementation" that at least one historically significant
 * object-oriented programming language, C++, makes
 * "inheritance for implementation" the default form of inheritance between
 * classes while requiring the explicit use of the <code>public</code> keyword
 * to establish an "is a" relationship.
 * </p>
 * 
 * <p>
 * If the preceding ruminations fail to convince, another justification for this
 * "abuse" of inheritance, which is at least a little different from a
 * post-rationalization, it should be noted that this class does in fact offer
 * the option of "modifying" an existing {@link NdefMessage} in a given
 * {@link Tag} by first reading its contents before writing to it. In such cases
 * a "writer" class is also a "reader" and since Java doesn't support a mix-in
 * style of inheritance (i.e. it is sadly deficient in its approach to multiple
 * inheritance) it makes sense to provide for that possibility in the only way
 * practical when constrained by single-inheritance. The validity of this "is a"
 * relationship is reinforced by the tightly coupled semantics of some of the
 * helper data structures and methods of both classes, e.g. the inverse
 * relationships between and common use of
 * {@link NdefReaderActivity#WELL_KNOWN_URI_PREFIX} by both
 * {@link NdefReaderActivity#decodeUri(byte[])} and {@link #createUri(String)}
 * and so on. So there! :-)
 * </p>
 * 
 * @see #createNdefMessage(NdefMessage)
 * @see #processTag(Tag, us.rader.nfc.ForegroundDispatchActivity.ProcessTagTask)
 * 
 * @author Kirk
 */
public abstract class NdefWriterActivity extends NdefReaderActivity {

    /**
     * Create an "external" NDEF record
     * 
     * @param domainName
     *            the domain name to prepend to the external type string
     * 
     * @param type
     *            the external type string
     * 
     * @param payload
     *            the record payload
     * 
     * @return the {@link NdefRecord} or <code>null</code> if an error occurs
     */
    public static NdefRecord createExternal(String domainName, String type,
            byte[] payload) {

        try {

            byte[] external = (domainName + ":" + type).getBytes("UTF-8"); //$NON-NLS-1$ //$NON-NLS-2$
            return new NdefRecord(NdefRecord.TNF_EXTERNAL_TYPE, external, null,
                    payload);

        } catch (Exception e) {

            Log.e(NdefWriterActivity.class.getName(),
                    "error encoding external type", e); //$NON-NLS-1$
            return null;

        }
    }

    /**
     * Create a NDEF MIME media message
     * 
     * @param type
     *            the MIME type
     * 
     * @param payload
     *            the payload bytes
     * 
     * @return the {@link NdefRecord}
     */
    public static NdefRecord createMime(String type, byte[] payload) {

        try {

            byte[] bytes = type.getBytes("US-ASCII"); //$NON-NLS-1$
            return new NdefRecord(NdefRecord.TNF_MIME_MEDIA, bytes, null,
                    payload);

        } catch (UnsupportedEncodingException e) {

            throw new IllegalArgumentException(e);

        }

    }

    /**
     * Create a NDEF "T" record
     * 
     * Note that this implementation only supports text strings that can be
     * encoded using UTF-8 even though the NDEF spec support UTF-16, as well
     * 
     * @param text
     *            the text string
     * 
     * @param language
     *            the language code ("en," "fr" etc.)
     * 
     * @return the {@link NdefRecord}
     */
    public static NdefRecord createText(String text, String language) {

        try {

            byte[] languageBytes = language.getBytes("UTF-8"); //$NON-NLS-1$

            if (languageBytes.length >= 32) {

                throw new IllegalArgumentException(
                        "maximim language code length exceeded"); //$NON-NLS-1$

            }

            byte[] textBytes = text.getBytes("UTF-8"); //$NON-NLS-1$
            int textStart = languageBytes.length + 1;
            int length = textBytes.length + textStart;
            byte[] payload = new byte[length];
            payload[0] = (byte) languageBytes.length;
            System.arraycopy(languageBytes, 0, payload, 1, languageBytes.length);
            System.arraycopy(textBytes, 0, payload, textStart, textBytes.length);
            return new NdefRecord(NdefRecord.TNF_WELL_KNOWN,
                    NdefRecord.RTD_TEXT, null, payload);

        } catch (UnsupportedEncodingException e) {

            throw new IllegalArgumentException(e);

        }

    }

    /**
     * Create a NDEF "U" record for the given string
     * 
     * @param uri
     *            the string
     * 
     * @return the {@link NdefRecord}
     * 
     * @see #createUri(Uri)
     */
    public static NdefRecord createUri(String uri) {

        try {

            byte code = 0;
            String prefix = ""; //$NON-NLS-1$

            for (byte index = 1; index < WELL_KNOWN_URI_PREFIX.length; ++index) {

                if (uri.startsWith(WELL_KNOWN_URI_PREFIX[index])) {

                    code = index;
                    prefix = WELL_KNOWN_URI_PREFIX[index];

                }
            }

            int prefixLength = prefix.length();

            if (prefixLength > 0) {

                uri = uri.substring(prefixLength);

            }

            byte[] bytes = uri.getBytes("UTF-8"); //$NON-NLS-1$
            byte[] payload = new byte[bytes.length + 1];
            payload[0] = code;
            System.arraycopy(bytes, 0, payload, 1, bytes.length);
            return new NdefRecord(NdefRecord.TNF_WELL_KNOWN,
                    NdefRecord.RTD_URI, null, payload);

        } catch (UnsupportedEncodingException e) {

            throw new IllegalArgumentException(e);

        }
    }

    /**
     * Create a NDEF "U" record for the given {@link Uri}
     * 
     * @param uri
     *            the {@link Uri}
     * 
     * @return the {@link NdefRecord}
     * 
     * @see #createUri(String)
     */
    public static NdefRecord createUri(Uri uri) {

        return createUri(uri.toString());

    }

    /**
     * If <code>true</code> attempt to mark the {@link Tag} read-only after
     * writing or while formatting in
     * {@link #processTag(Tag, us.rader.nfc.ForegroundDispatchActivity.ProcessTagTask)}
     * 
     * @see #isReadOnlyRequested()
     * @see #setReadonlyRequested(boolean)
     * @see #processTag(Tag,
     *      us.rader.nfc.ForegroundDispatchActivity.ProcessTagTask)
     */
    private boolean readOnlyRequested;

    /**
     * Initialize {@link #readOnlyRequested} to <code>false</code>
     */
    public NdefWriterActivity() {

        this(false);

    }

    /**
     * Initialize {@link #readOnlyRequested} to the given value
     * 
     * @param readOnlyRequested
     *            the initial value for {@link #readOnlyRequested}
     * 
     * @see #readOnlyRequested
     * @see #isReadOnlyRequested()
     * @see #setReadonlyRequested(boolean)
     */
    public NdefWriterActivity(boolean readOnlyRequested) {

        this.readOnlyRequested = readOnlyRequested;

    }

    /**
     * Return the current value of {@link #readOnlyRequested}
     * 
     * @return {@link #readOnlyRequested}
     * 
     * @see #setReadonlyRequested(boolean)
     * @see #processTag(Tag,
     *      us.rader.nfc.ForegroundDispatchActivity.ProcessTagTask)
     */
    public final boolean isReadOnlyRequested() {

        return readOnlyRequested;

    }

    /**
     * Set {@link #readOnlyRequested} to the given value
     * 
     * @param readOnlyRequested
     *            the new value for {@link #readOnlyRequested}
     * 
     * @see #readOnlyRequested
     * @see #isReadOnlyRequested()
     */
    public final void setReadonlyRequested(boolean readOnlyRequested) {

        this.readOnlyRequested = readOnlyRequested;

    }

    /**
     * Return the {@link NdefMessage} to write to a {@link Ndef} tag
     * 
     * {@link #processTag(Tag, us.rader.nfc.ForegroundDispatchActivity.ProcessTagTask)}
     * will only attempt to modify the {@link Tag} if this method returns a
     * non-null {@link NdefMessage}
     * 
     * @param currentContents
     *            the {@link NdefMessage} representing the current contents of a
     *            {@link Tag} or <code>null</code> if {@link Tag} is empty
     * 
     * @return the given {@link NdefMessage} or a new one with which to replace
     *         it
     * 
     * @see #processTag(Tag,
     *      us.rader.nfc.ForegroundDispatchActivity.ProcessTagTask)
     */
    protected abstract NdefMessage createNdefMessage(NdefMessage currentContents);

    /**
     * Write the result of calling {@link #createNdefMessage(NdefMessage)} to
     * the given {@link Tag}
     * 
     * @param tag
     *            the {@link Tag}
     * 
     * @param task
     *            use {@link ProcessTagTask#setOutcome(ProcessTagOutcome)} to
     *            provide additional diagnostic information to
     *            {@link ForegroundDispatchActivity#onTagProcessed(Object, ProcessTagOutcome)}
     * 
     * @return the {@link NdefMessage} that was written to the {@link Tag} or
     *         <code>null</code> if an error occurs
     * 
     * @see us.rader.nfc.NdefReaderActivity#processTag(android.nfc.Tag,
     *      us.rader.nfc.ForegroundDispatchActivity.ProcessTagTask)
     */
    @Override
    protected NdefMessage processTag(Tag tag, ProcessTagTask task) {

        try {

            Ndef ndef = Ndef.get(tag);

            if (ndef == null) {

                NdefFormatable formatable = NdefFormatable.get(tag);

                if (formatable == null) {

                    return null;
                }

                return processNdefFormatable(formatable, task);

            }

            return processNdef(ndef, task);

        } catch (Exception e) {

            Log.e(NdefWriterActivity.class.getName(), "error processing tag", e); //$NON-NLS-1$
            task.setOutcome(ProcessTagOutcome.TECHNOLOGY_ERROR);
            return null;

        }
    }

    /**
     * Write the result of passing the given {@link Ndef} instance to
     * {@link #createNdefMessage(NdefMessage)} to the tag using that same
     * {@link Ndef}
     * 
     * @param ndef
     *            the {@Link Ndef} instance
     * 
     * @param task
     *            use {@link ProcessTagTask#setOutcome(ProcessTagOutcome)} to
     *            provide additional diagnostic information to
     *            {@link ForegroundDispatchActivity#onTagProcessed(Object, ProcessTagOutcome)}
     * 
     * @return the {@link NdefMessage} that was written
     * 
     * @throws IOException
     *             if an I/O error occurs
     * 
     * @throws FormatException
     *             if a NDEF format error occurs
     */
    private NdefMessage processNdef(Ndef ndef, ProcessTagTask task)
            throws IOException, FormatException {

        NdefMessage currentContents = ndef.getCachedNdefMessage();
        NdefMessage message = createNdefMessage(currentContents);

        if (message == null) {

            task.setOutcome(ProcessTagOutcome.NOTHING_TO_DO);
            return currentContents;

        }

        if (!ndef.isWritable()) {

            task.setOutcome(ProcessTagOutcome.READ_ONLY_TAG);
            return currentContents;

        }

        byte[] bytes = message.toByteArray();
        int maxSize = ndef.getMaxSize();

        if (bytes.length > maxSize) {

            task.setOutcome(ProcessTagOutcome.TAG_SIZE_EXCEEDED);
            return currentContents;

        }

        ndef.connect();

        try {

            ndef.writeNdefMessage(message);

            if (readOnlyRequested) {

                ndef.makeReadOnly();

            }

            return message;

        } finally {

            ndef.close();

        }
    }

    /**
     * Write the result of passing <code>null</code> to
     * {@link #createNdefMessage(NdefMessage)} to the given
     * {@link NdefFormatable}
     * 
     * @param formatable
     *            the {@Link NdefFormatable} instance
     * 
     * @param task
     *            use {@link ProcessTagTask#setOutcome(ProcessTagOutcome)} to
     *            provide additional diagnostic information to
     *            {@link ForegroundDispatchActivity#onTagProcessed(Object, ProcessTagOutcome)}
     * 
     * @return the {@link NdefMessage} that was written
     * 
     * @throws IOException
     *             if an I/O error occurs
     * 
     * @throws FormatException
     *             if a NDEF format error occurs
     */
    private NdefMessage processNdefFormatable(NdefFormatable formatable,
            ProcessTagTask task) throws IOException, FormatException {

        formatable.connect();

        try {

            NdefMessage message = createNdefMessage(null);

            if (message == null) {

                task.setOutcome(ProcessTagOutcome.NOTHING_TO_DO);
                return null;

            }

            if (readOnlyRequested) {

                formatable.formatReadOnly(message);

            } else {

                formatable.format(message);

            }

            task.setOutcome(ProcessTagOutcome.SUCCESSFUL_WRITE);
            return message;

        } finally {

            formatable.close();

        }
    }

}