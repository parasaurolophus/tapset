package us.rader.nfc;

import java.io.UnsupportedEncodingException;

import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
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
     * NDEF RTD for text records
     * 
     * @see NdefRecord#RTD_TEXT
     */
    public static final String      RTD_TEXT              = "T";                         //$NON-NLS-1$

    /**
     * NDEF RTD for URI records
     * 
     * @see NdefRecord#RTD_URI
     */
    public static final String      RTD_URI               = "U";                         //$NON-NLS-1$

    /**
     * URI prefix for "U" records with code byte 0
     * 
     * @see #WELL_KNOWN_URI_PREFIX
     */
    protected static final String   URI_PREFIX_00         = "";                          //$NON-NLS-1$

    /**
     * URI prefix for "U" records with code byte 1
     * 
     * @see #WELL_KNOWN_URI_PREFIX
     */
    protected static final String   URI_PREFIX_01         = "http://www.";               //$NON-NLS-1$

    /**
     * URI prefix for "U" records with code byte 2
     * 
     * @see #WELL_KNOWN_URI_PREFIX
     */
    protected static final String   URI_PREFIX_02         = "https://www.";              //$NON-NLS-1$

    /**
     * URI prefix for "U" records with code byte 3
     * 
     * @see #WELL_KNOWN_URI_PREFIX
     */
    protected static final String   URI_PREFIX_03         = "http://";                   //$NON-NLS-1$

    /**
     * URI prefix for "U" records with code byte 4
     * 
     * @see #WELL_KNOWN_URI_PREFIX
     */
    protected static final String   URI_PREFIX_04         = "https://";                  //$NON-NLS-1$

    /**
     * URI prefix for "U" records with code byte 5
     * 
     * @see #WELL_KNOWN_URI_PREFIX
     */
    protected static final String   URI_PREFIX_05         = "tel:";                      //$NON-NLS-1$

    /**
     * URI prefix for "U" records with code byte 6
     * 
     * @see #WELL_KNOWN_URI_PREFIX
     */
    protected static final String   URI_PREFIX_06         = "mailto:";                   //$NON-NLS-1$

    /**
     * URI prefix for "U" records with code byte 7
     * 
     * @see #WELL_KNOWN_URI_PREFIX
     */
    protected static final String   URI_PREFIX_07         = "ftp://anonymous:anonymous@"; //$NON-NLS-1$

    /**
     * URI prefix for "U" records with code byte 8
     * 
     * @see #WELL_KNOWN_URI_PREFIX
     */
    protected static final String   URI_PREFIX_08         = "ftp://ftp.";                //$NON-NLS-1$

    /**
     * URI prefix for "U" records with code byte 9
     * 
     * @see #WELL_KNOWN_URI_PREFIX
     */
    protected static final String   URI_PREFIX_09         = "ftps://";                   //$NON-NLS-1$

    /**
     * URI prefix for "U" records with code byte 10
     * 
     * @see #WELL_KNOWN_URI_PREFIX
     */
    protected static final String   URI_PREFIX_10         = "sftp://";                   //$NON-NLS-1$

    /**
     * URI prefix for "U" records with code byte 11
     * 
     * @see #WELL_KNOWN_URI_PREFIX
     */
    protected static final String   URI_PREFIX_11         = "smb://";                    //$NON-NLS-1$

    /**
     * URI prefix for "U" records with code byte 12
     * 
     * @see #WELL_KNOWN_URI_PREFIX
     */
    protected static final String   URI_PREFIX_12         = "nfs://";                    //$NON-NLS-1$

    /**
     * URI prefix for "U" records with code byte 13
     * 
     * @see #WELL_KNOWN_URI_PREFIX
     */
    protected static final String   URI_PREFIX_13         = "ftp://";                    //$NON-NLS-1$

    /**
     * URI prefix for "U" records with code byte 14
     */
    protected static final String   URI_PREFIX_14         = "dav://";                    //$NON-NLS-1$

    /**
     * URI prefix for "U" records with code byte 15
     * 
     * @see #WELL_KNOWN_URI_PREFIX
     */
    protected static final String   URI_PREFIX_15         = "news:";                     //$NON-NLS-1$

    /**
     * URI prefix for "U" records with code byte 16
     * 
     * @see #WELL_KNOWN_URI_PREFIX
     */
    protected static final String   URI_PREFIX_16         = "telnet://";                 //$NON-NLS-1$

    /**
     * URI prefix for "U" records with code byte 17
     * 
     * @see #WELL_KNOWN_URI_PREFIX
     */
    protected static final String   URI_PREFIX_17         = "imap:";                     //$NON-NLS-1$

    /**
     * URI prefix for "U" records with code byte 18
     * 
     * @see #WELL_KNOWN_URI_PREFIX
     */
    protected static final String   URI_PREFIX_18         = "rtsp://";                   //$NON-NLS-1$

    /**
     * URI prefix for "U" records with code byte 19
     * 
     * @see #WELL_KNOWN_URI_PREFIX
     */
    protected static final String   URI_PREFIX_19         = "urn:";                      //$NON-NLS-1$

    /**
     * URI prefix for "U" records with code byte 20
     * 
     * @see #WELL_KNOWN_URI_PREFIX
     */
    protected static final String   URI_PREFIX_20         = "pop:";                      //$NON-NLS-1$

    /**
     * URI prefix for "U" records with code byte 21
     * 
     * @see #WELL_KNOWN_URI_PREFIX
     */
    protected static final String   URI_PREFIX_21         = "sip:";                      //$NON-NLS-1$

    /**
     * URI prefix for "U" records with code byte 22
     * 
     * @see #WELL_KNOWN_URI_PREFIX
     */
    protected static final String   URI_PREFIX_22         = "sips:";                     //$NON-NLS-1$

    /**
     * URI prefix for "U" records with code byte 23
     * 
     * @see #WELL_KNOWN_URI_PREFIX
     */
    protected static final String   URI_PREFIX_23         = "tftp:";                     //$NON-NLS-1$

    /**
     * URI prefix for "U" records with code byte 24
     * 
     * @see #WELL_KNOWN_URI_PREFIX
     */
    protected static final String   URI_PREFIX_24         = "btspp://";                  //$NON-NLS-1$

    /**
     * URI prefix for "U" records with code byte 25
     * 
     * @see #WELL_KNOWN_URI_PREFIX
     */
    protected static final String   URI_PREFIX_25         = "btl2cap://";                //$NON-NLS-1$

    /**
     * URI prefix for "U" records with code byte 26
     * 
     * @see #WELL_KNOWN_URI_PREFIX
     */
    protected static final String   URI_PREFIX_26         = "btgoep://";                 //$NON-NLS-1$

    /**
     * URI prefix for "U" records with code byte 27
     * 
     * @see #WELL_KNOWN_URI_PREFIX
     */
    protected static final String   URI_PREFIX_27         = "tcpobex://";                //$NON-NLS-1$

    /**
     * URI prefix for "U" records with code byte 28
     * 
     * @see #WELL_KNOWN_URI_PREFIX
     */
    protected static final String   URI_PREFIX_28         = "irdaobex://";               //$NON-NLS-1$

    /**
     * URI prefix for "U" records with code byte 29
     * 
     * @see #WELL_KNOWN_URI_PREFIX
     */
    protected static final String   URI_PREFIX_29         = "file://";                   //$NON-NLS-1$

    /**
     * URI prefix for "U" records with code byte 30
     * 
     * @see #WELL_KNOWN_URI_PREFIX
     */
    protected static final String   URI_PREFIX_30         = "urn:epc:id:";               //$NON-NLS-1$

    /**
     * URI prefix for "U" records with code byte 31
     * 
     * @see #WELL_KNOWN_URI_PREFIX
     */
    protected static final String   URI_PREFIX_31         = "urn:epc:tag:";              //$NON-NLS-1$

    /**
     * URI prefix for "U" records with code byte 32
     * 
     * @see #WELL_KNOWN_URI_PREFIX
     */
    protected static final String   URI_PREFIX_32         = "urn:epc:pat:";              //$NON-NLS-1$

    /**
     * URI prefix for "U" records with code byte 33
     * 
     * @see #WELL_KNOWN_URI_PREFIX
     */
    protected static final String   URI_PREFIX_33         = "urn:epc:raw:";              //$NON-NLS-1$

    /**
     * URI prefix for "U" records with code byte 34
     * 
     * @see #WELL_KNOWN_URI_PREFIX
     */
    protected static final String   URI_PREFIX_34         = "urn:epc:";                  //$NON-NLS-1$

    /**
     * URI prefix for "U" records with code byte 35
     * 
     * @see #WELL_KNOWN_URI_PREFIX
     */
    protected static final String   URI_PREFIX_35         = "urn:nfc:";                  //$NON-NLS-1$

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
     * single-character string "U")
     * 
     * <li>The first byte of the array returned by
     * {@link NdefRecord#getPayload()} contains a code denoting a standard URI
     * prefix as defined in the NDEF forum specification for such records
     * 
     * <li>The remaining bytes of the array returned by
     * {@link NdefRecord#getPayload()} contain the ASCII characters to append to
     * the prefix denoted by the first byte
     * 
     * </ul>
     * 
     * <p>
     * The number and order of the entries in this array must match the values
     * that appear as the first byte in the payload of such records as laid out
     * in the NDEF format specifcation documents.
     * </p>
     * 
     * @see #decodeUri(byte[])
     */
    protected static final String[] WELL_KNOWN_URI_PREFIX = { URI_PREFIX_00,
            URI_PREFIX_01, URI_PREFIX_02, URI_PREFIX_03, URI_PREFIX_04,
            URI_PREFIX_05, URI_PREFIX_06, URI_PREFIX_07, URI_PREFIX_08,
            URI_PREFIX_09, URI_PREFIX_10, URI_PREFIX_11, URI_PREFIX_12,
            URI_PREFIX_13, URI_PREFIX_14, URI_PREFIX_15, URI_PREFIX_16,
            URI_PREFIX_17, URI_PREFIX_18, URI_PREFIX_19, URI_PREFIX_20,
            URI_PREFIX_21, URI_PREFIX_22, URI_PREFIX_23, URI_PREFIX_24,
            URI_PREFIX_25, URI_PREFIX_26, URI_PREFIX_27, URI_PREFIX_28,
            URI_PREFIX_29, URI_PREFIX_30, URI_PREFIX_31, URI_PREFIX_32,
            URI_PREFIX_33, URI_PREFIX_34, URI_PREFIX_35

                                                          };

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

                    // oddly, the NDEF spec mandates putting the URI in the type
                    // field rather than the payload
                    return new String(record.getType(), "US-ASCII"); //$NON-NLS-1$

                case NdefRecord.TNF_WELL_KNOWN:

                    return decodeWellKnown(record.getType(),
                            record.getPayload());

                case NdefRecord.TNF_MIME_MEDIA:

                    return decodeMime(record.getType(), record.getPayload());

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
     * Return the <code>payload</code> decoded using UTF-8 if the MIME
     * <code>type</code> begins with "text/"
     * 
     * Some comnonly-used, special-case MIME "media" types correspond to text
     * data, e.g. vcard data. This method assumes that any record whose MIME
     * type begins with "text/" has a payload that can interpreted as a UTF-8
     * text string. It returns <code>null</code> for any other record.
     * 
     * @param type
     *            the value returned by {@link NdefRecord#getType()}
     * 
     * @param payload
     *            the value returned by {@link NdefRecord#getPayload()}
     * 
     * @return the value of <code>payload</code> decoded using UTF-8 or
     *         <code>null</code>
     */
    private static String decodeMime(byte[] type, byte[] payload) {

        try {

            String mime = new String(type, "UTF-8"); //$NON-NLS-1$

            if (mime.startsWith("text/")) { //$NON-NLS-1$

                return new String(payload, "UTF-8"); //$NON-NLS-1$

            }

        } catch (Exception e) {

            Log.e(NdefReaderActivity.class.getName(),
                    "error decoding MIME record as text", e); //$NON-NLS-1$

        }

        return null;

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

        int status = payload[0];
        int languageLength = status & 0x01F;
        int encodingFlag = status & 0x80;
        String encoding = ((encodingFlag == 0) ? "UTF-8" : "UTF-16"); //$NON-NLS-1$//$NON-NLS-2$
        int textStart = languageLength + 1;
        int textLength = payload.length - textStart;
        return new String(payload, textStart, textLength, encoding);

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
        String uri = new String(payload, 1, payload.length - 1, "UTF-8"); //$NON-NLS-1$

        if ((code > 0) && (code < WELL_KNOWN_URI_PREFIX.length)) {

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

        String rtd = new String(type, "US-ASCII"); //$NON-NLS-1$

        if (RTD_TEXT.equals(rtd)) {

            return decodeText(payload);

        } else if (RTD_URI.equals(rtd)) {

            return decodeUri(payload);

        } else {

            return null;

        }
    }

    /**
     * Create the {@link IntentFilter} array to use when enabling foreground
     * dispatch
     * 
     * @return the {@link IntentFilter} array
     */
    @Override
    protected final IntentFilter[] createNfcIntentFilters() {

        IntentFilter ndefFilter = new IntentFilter(
                NfcAdapter.ACTION_NDEF_DISCOVERED);
        IntentFilter tagFilter = new IntentFilter(
                NfcAdapter.ACTION_TAG_DISCOVERED);
        return new IntentFilter[] { ndefFilter, tagFilter };

    }

    /**
     * Return the {@link NdefMessage} contained in the given {@link Tag} or
     * <code>null</code>
     * 
     * <p>
     * This will return <code>null</code> if the {@link Tag} isn't NDEF
     * formatted, is empty or an error is encountered while reading it.
     * </p>
     * 
     * <p>
     * Note that this implementation is deliberately <em>not</em> declared to be
     * <code>final</code>. While this is a slightly weaker expression of
     * object-oriented design than is often wise, it is done so as to give
     * {@link NdefWriterActivity} the freedom it needs to modify the contents of
     * the {@link Tag} in addition to merely reading from it. I.e.this is a case
     * proving that "all methods / classes should be either <code>final</code>
     * or <code>abstract</code>" is a guideline rather than a hard-and-fast
     * rule. Of course, this "defect" could have been easily avoided using a
     * "mix-in" style of object-oriented design, but that is not possible given
     * Java's single-inheritance semantics.
     * </p>
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

        } catch (Exception e) {

            Log.e(NdefReaderActivity.class.getName(),
                    "error reading NDEF message from tag", e); //$NON-NLS-1$

            if (getLastStatus() == null) {

                setLastStatus(getString(R.string.error_reading_ndef_message));

            }

            return null;

        }

    }

}
