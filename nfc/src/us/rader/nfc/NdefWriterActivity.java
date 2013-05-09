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
 * author replies, "you are correct, now get over it." :-)
 * </p>
 * 
 * <p>
 * One way of dispensing with such criticism would be simply to give the
 * ancestor classes more cumbersome names, like
 * <code>NfcForegroundDispatcherActivity</code> and so on. That, of course, is
 * the real point of the "get over it" quip: complaints about
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
 * If the preceding ruminations fail to convince, another justification for this
 * "abuse" of inheritance, which is at least a little different from a
 * post-rationalization, it should be noted that this class does in fact offer
 * the option of "modifying" an existing {@link NdefMessage} in a given
 * {@link Tag} by first reading its contents before writing to it. In such cases
 * a "writer" class is also a "reader" and since Java doesn't support a mix-in
 * style of inheritance (i.e. it is sadly deficient in its approach to multiple
 * inheritance) it makes sense to provide for that possibility in the only way
 * practical when constrained by single-inheritance.
 * </p>
 * 
 * @see #createNdefMessage(Ndef)
 * @see #processTag(Tag)
 * 
 * @author Kirk
 */
public abstract class NdefWriterActivity extends NdefReaderActivity {

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
            byte[] textBytes = text.getBytes("UTF-8"); //$NON-NLS-1$
            int textStart = languageBytes.length + 1;
            int length = textBytes.length + textStart;

            if (length > 255) {

                throw new IllegalArgumentException(
                        "maximum text message size exceeded"); //$NON-NLS-1$

            }

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

            byte code;
            byte[] bytes;

            if (uri.startsWith("http://www.")) { //$NON-NLS-1$

                code = 1;
                bytes = uri.substring(11).getBytes("US-ASCII"); //$NON-NLS-1$

            } else if (uri.startsWith("https://www.")) { //$NON-NLS-1$

                code = 2;
                bytes = uri.substring(12).getBytes("US-ASCII"); //$NON-NLS-1$

            } else if (uri.startsWith("http://")) { //$NON-NLS-1$

                code = 3;
                bytes = uri.substring(7).getBytes("US-ASCII"); //$NON-NLS-1$

            } else if (uri.startsWith("https://")) { //$NON-NLS-1$

                code = 4;
                bytes = uri.substring(8).getBytes("US-ASCII"); //$NON-NLS-1$

            } else if (uri.startsWith("tel:")) { //$NON-NLS-1$

                code = 5;
                bytes = uri.substring(4).getBytes("US-ASCII"); //$NON-NLS-1$

            } else {

                code = 0;
                bytes = uri.getBytes("US-ASCII"); //$NON-NLS-1$

            }

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
     * writing or while formatting in {@link #processTag(Tag)}
     * 
     * @see #isReadOnlyRequested()
     * @see #setReadonlyRequested(boolean)
     * @see #processTag(Tag)
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
     * @see #processTag(Tag)
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
     * Return the {@link NdefMessage} to write to the given {@link Ndef} tag
     * 
     * This method is passed the {@link Ndef} on the assumption that some
     * (most?) app's might want to engage in a read-modify-write cycle with the
     * NFC tags they use, rather than write only. In such cases, you can use the
     * given object to obtain the current {@link NdefMessage}. But note that
     * this will be <code>null</code> in cases where the given tag isn't yet
     * NDEF formatted or is empty. Note also that the {@link Ndef}, if not
     * <code>null</code> will also already be "connected" and will also be used
     * to write the {@link NdefMessage} so you should call neither
     * {@link Ndef#connect()} nor {@link Ndef#close()} here.
     * 
     * @param ndef
     *            the {@link Ndef} object by which can read the current contents
     *            of the tag or <code>null</code> if the tag isn't yet NDEF
     *            formatted or is empty
     * 
     * @return the {@link NdefMessage}
     * 
     * @see #processTag(Tag)
     */
    protected abstract NdefMessage createNdefMessage(Ndef ndef);

    /**
     * Write the result of calling {@link #createNdefMessage(Ndef)} to the given
     * {@link Tag}
     * 
     * @param tag
     *            the {@link Tag}
     * 
     * @return the {@link NdefMessage} that was written to the {@link Tag} or
     *         <code>null</code> if an error occurs
     * 
     * @see us.rader.nfc.NdefReaderActivity#processTag(android.nfc.Tag)
     */
    @Override
    protected NdefMessage processTag(Tag tag) {

        try {

            Ndef ndef = Ndef.get(tag);

            if (ndef == null) {

                NdefFormatable formatable = NdefFormatable.get(tag);

                if (formatable == null) {

                    return null;
                }

                return processNdefFormatable(formatable);

            }

            return processNdef(ndef);

        } catch (Exception e) {

            Log.e(NdefWriterActivity.class.getName(), "error processing tag", e); //$NON-NLS-1$

            if (getLastStatus() == null) {

                setLastStatus(e.getMessage());

            }

            return null;

        }
    }

    /**
     * Write the result of passing the given {@link Ndef} instance to
     * {@link #createNdefMessage(Ndef)} to the tag using that same {@link Ndef}
     * 
     * @param ndef
     *            the {@Link Ndef} instance
     * 
     * @return the {@link NdefMessage} that was written
     * 
     * @throws IOException
     *             if an I/O error occurs
     * 
     * @throws FormatException
     *             if a NDEF format error occurs
     */
    private NdefMessage processNdef(Ndef ndef) throws IOException,
            FormatException {

        ndef.connect();

        try {

            if (!ndef.isWritable()) {

                throw new IllegalStateException(
                        getString(R.string.read_only_tag));

            }

            NdefMessage message = createNdefMessage(ndef);
            byte[] bytes = message.toByteArray();
            int maxSize = ndef.getMaxSize();

            if (bytes.length > maxSize) {

                throw new IllegalStateException(getString(
                        R.string.tag_size_exceeded, bytes.length, maxSize));

            }

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
     * {@link #createNdefMessage(Ndef)} to the given {@link NdefFormatable}
     * 
     * @param formatable
     *            the {@Link NdefFormatable} instance
     * 
     * @return the {@link NdefMessage} that was written
     * 
     * @throws IOException
     *             if an I/O error occurs
     * 
     * @throws FormatException
     *             if a NDEF format error occurs
     */
    private NdefMessage processNdefFormatable(NdefFormatable formatable)
            throws IOException, FormatException {

        formatable.connect();

        try {

            NdefMessage message = createNdefMessage(null);

            if (readOnlyRequested) {

                formatable.formatReadOnly(message);

            } else {

                formatable.format(message);

            }

            return message;

        } finally {

            formatable.close();

        }
    }

}