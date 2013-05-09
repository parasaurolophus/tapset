package us.rader.nfc;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.util.Log;

/**
 * {@link NfcReaderActivity} that reads the contents of a NDEF formatted
 * {@link Tag}
 * 
 * @see NfcReaderActivity
 * 
 * @author Kirk
 */
public abstract class NdefReaderActivity extends NfcReaderActivity<NdefMessage> {

    /**
     * Strings to prepend to the URI in a NDEF "well known URI" record.
     * 
     * <p>
     * The format of a NDEF "Well known URI" record, is as follows:
     * </p>
     * 
     * <ul>
     * 
     * <li>{@link NdefRecord#getTnf()} returns {@link NdefRecord#TNF_WELL_KNOWN}
     * 
     * <li>{@link NdefRecord#getType()} returns an array with the same contents
     * as {@link NdefRecord#RTD_URI} (i.e. the ASCII representation of the
     * string "U")
     * 
     * <li>The first byte of the array returned by
     * {@link NdefRecord#getPayload()} contains a code denoting a standard URI
     * prefix
     * 
     * <li>The remaining bytes of array returned by
     * {@link NdefRecord#getPayload()} contain the ASCII characters to append to
     * the prefix denoted by the first byte
     * 
     * </ul>
     * 
     * <p>
     * The number and order of the entries in this array must match the values
     * that appear as the first byte in the payload of such records
     * </p>
     * 
     * <p>
     * Note that this format supports a simple form of compression where
     * standard prefixes as long as 12 bytes (https://www.) are reduced to a
     * single-byte code for storage in the NFC tag, at the slight cost that for
     * code 0, meaning no prefix, the tag actually requires 1 extra byte when
     * compared to records with {@link NdefRecord#TNF_ABSOLUTE_URI} (i.e. use
     * the latter rather than {@link NdefRecord#TNF_WELL_KNOWN} where the URI
     * does not begin with one of the standard prefixes for a non-zero offset in
     * this array).
     * </p>
     * 
     * @see #decodeUri(byte[])
     */
    public static final String[] WELL_KNOWN_URI_PREFIX = {//@formatter:off

            // code 0 prefix is empty, meaning that you really
            // should have used TNF_ABSOLUTE_URI instead!
            "", //$NON-NLS-1$

            // code 1 prefix is http://www.
            "http://www.", //$NON-NLS-1$

            // code 2 prefix is https://www.
            "https://www.", //$NON-NLS-1$

            // code 3 prefix is http://
            "http://", //$NON-NLS-1$

            // code 4 prefix is https://
            "https://", //$NON-NLS-1$

            // code 5 prefix is tel:
            "tel:", //$NON-NLS-1$

            // code 6 prefix is mailto:
            "mailto:" //$NON-NLS-1$

    }; //@formatter:on

    /**
     * Decode the payload from a {@link NdefRecord#TNF_ABSOLUTE_URI} record, or
     * a {@link NdefRecord#TNF_WELL_KNOWN} record with
     * {@link NdefRecord#RTD_TEXT} or {@link NdefRecord#RTD_URI}
     * 
     * This will return <code>null</code> if the given {@link NdefRecord} is
     * <code>null</code> or doesn't have a type of contents supported by this
     * method
     * 
     * @param record
     *            the {@link NdefRecord}
     * 
     * @return the decoded payload string or <code>null</code>
     */
    public static String decodePayload(NdefRecord record) {

        if (record == null) {

            return null;

        }

        try {

            switch (record.getTnf()) {

                case NdefRecord.TNF_ABSOLUTE_URI:

                    // TODO: verify this, really use type rather than payload?
                    return new String(record.getType(), "US-ASCII"); //$NON-NLS-1$

                case NdefRecord.TNF_WELL_KNOWN:

                    return decodeWellKnown(record.getType(),
                            record.getPayload());

                default:

                    return null;

            }

        } catch (UnsupportedEncodingException e) {

            Log.e(NdefReaderActivity.class.getName(),
                    "error decoding the record payload", e); //$NON-NLS-1$
            return null;

        }
    }

    /**
     * Read the {@link NdefMessage} contained in the given tag, if any
     * 
     * This will return <code>null</code> if the given {@link Tag} is
     * <code>null</code> or doesn't contain a {@link NdefMessage}
     * 
     * @param tag
     *            the {@link Tag}
     * 
     * @return the {@link NdefMessage} contained in the given {@link Tag} or
     *         <code>null</code>
     * 
     * @throws IOException
     *             if an I/O error occurs
     * 
     * @throws FormatException
     *             if a NDEF format error occurs
     */
    public static NdefMessage readTag(Tag tag) throws IOException,
            FormatException {

        if (tag == null) {

            return null;

        }

        Ndef ndef = Ndef.get(tag);

        if (ndef == null) {

            Log.w(NdefReaderActivity.class.getName(),
                    "tag is not NDEF formatted"); //$NON-NLS-1$
            return null;

        }

        ndef.connect();

        try {

            NdefMessage message = ndef.getNdefMessage();

            if (message == null) {

                Log.w(NdefReaderActivity.class.getName(),
                        "NDEF formatted tag is empty"); //$NON-NLS-1$

            }

            return message;

        } finally {

            ndef.close();

        }
    }

    /**
     * Return the content, minus the language code, of NDEF "T" record
     * 
     * <p>
     * The format of a "well known text" NDEF record is as follows:
     * </p>
     * 
     * <ul>
     * 
     * <li>{@link NdefRecord#getTnf()} returns {@link NdefRecord#TNF_WELL_KNOWN}
     * 
     * <li>{@link NdefRecord#getType()} returns an array with the same contents
     * as {@link NdefRecord#RTD_TEXT} (i.e. the ASCII representation of the
     * string "T")
     * 
     * <li>the first byte of the payload, i.e. the array returned by
     * {@link NdefRecord#getPayload()} contains the length of the language code
     * string (e.g. "en" for English and so on)
     * 
     * <li>the next <code>n</code> bytes, where <code>n</code> is the value of
     * the first payload byte, is the UTF-8 representation of the language code
     * string
     * 
     * <li>the remaining payload bytes are the UTF-8 representation of the text
     * string
     * 
     * </ul>
     * 
     * <p>
     * Note that this implementation takes account of but otherwise completely
     * ignores the contents of the language code string
     * </p>
     * 
     * @param payload
     *            the value returned by {@link NdefRecord#getPayload()}
     * 
     * @return the text string
     * 
     * @throws UnsupportedEncodingException
     *             if there is a bug in the Java virtual machine
     */
    private static String decodeText(byte[] payload)
            throws UnsupportedEncodingException {

        int languageLength = payload[0];
        int textStart = languageLength + 1;
        int textLength = payload.length - textStart;
        return new String(payload, textStart, textLength, "UTF-8"); //$NON-NLS-1$

    }

    /**
     * Extract the full URI from the given payload array
     * 
     * See {@link #WELL_KNOWN_URI_PREFIX} for a full description of the format
     * of such records
     * 
     * @param payload
     *            the result returned by {@link NdefRecord#getPayload()}
     * 
     * @return the full URI as a string
     * 
     * @throws UnsupportedEncodingException
     *             if there is a bug in the Java virtual machine
     * 
     * @see #WELL_KNOWN_URI_PREFIX
     */
    private static String decodeUri(byte[] payload)
            throws UnsupportedEncodingException {

        int code = payload[0];
        String uri = new String(payload, 1, payload.length - 1, "US-ASCII"); //$NON-NLS-1$

        if ((code >= 0) && (code < WELL_KNOWN_URI_PREFIX.length)) {

            uri = WELL_KNOWN_URI_PREFIX[code] + uri;

        }

        return uri;

    }

    /**
     * Decode the payload from a "T" or "U" record
     * 
     * @param type
     *            the record type
     * 
     * @param payload
     *            the record payload to decode
     * 
     * @return the decoded payload or <code>null</code>
     * 
     * @throws UnsupportedEncodingException
     *             if there is a bug in the Java virtual machine
     */
    private static String decodeWellKnown(byte[] type, byte[] payload)
            throws UnsupportedEncodingException {

        String s = new String(type, "US-ASCII"); //$NON-NLS-1$

        if ("T".equals(s)) { //$NON-NLS-1$

            return decodeText(payload);

        } else if ("U".equals(s)) { //$NON-NLS-1$

            return decodeUri(payload);

        } else {

            return null;

        }
    }

    /**
     * Return the {@link NdefMessage} contained in the given {@link Tag} or
     * <code>null</code>
     * 
     * This will return <code>null</code> if the {@link Tag} isn't NDEF
     * formatted or is empty.
     * 
     * @param tag
     *            the {@link Tag}
     * 
     * @return the {@link NdefMessage} or <code>null</code>
     * 
     * @see us.rader.nfc.NfcReaderActivity#processTag(android.nfc.Tag)
     */
    @Override
    protected NdefMessage processTag(Tag tag) {

        try {

            return readTag(tag);

        } catch (Exception e) {

            Log.e(NdefReaderActivity.class.getName(), "error processing tag", e); //$NON-NLS-1$

            if (getLastStatus() == null) {

                setLastStatus(e.getMessage());

            }

            return null;

        }

    }

}
