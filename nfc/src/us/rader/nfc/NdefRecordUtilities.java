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
import android.nfc.NdefRecord;
import android.util.Log;

/**
 * Utility class providing helper methods for creating and parsing instances of
 * {@link NdefRecord}
 * 
 * This provides slightly enhanced versions of some of the functionality
 * introduced in later versions of the Android SDK, in a manner that is
 * backwards compatible to SDK 10
 * 
 * @author Kirk
 * 
 */
public final class NdefRecordUtilities {

    /**
     * The singleton instance
     */
    private static NdefRecordUtilities singleton;

    static {

        singleton = new NdefRecordUtilities();

    }

    /**
     * Return the singleton instance
     * 
     * @return {@link #singleton}
     */
    public static NdefRecordUtilities getInstance() {

        return singleton;

    }

    /**
     * NDEF RTD for text records
     * 
     * @see NdefRecord#RTD_TEXT
     */
    private final String   RTD_TEXT              = "T";                         //$NON-NLS-1$

    /**
     * NDEF RTD for URI records
     * 
     * @see NdefRecord#RTD_URI
     */
    private final String   RTD_URI               = "U";                         //$NON-NLS-1$

    /**
     * URI prefix for "U" records with code byte 0
     * 
     * @see #WELL_KNOWN_URI_PREFIX
     */
    private final String   URI_PREFIX_00         = "";                          //$NON-NLS-1$

    /**
     * URI prefix for "U" records with code byte 1
     * 
     * @see #WELL_KNOWN_URI_PREFIX
     */
    private final String   URI_PREFIX_01         = "http://www.";               //$NON-NLS-1$

    /**
     * URI prefix for "U" records with code byte 2
     * 
     * @see #WELL_KNOWN_URI_PREFIX
     */
    private final String   URI_PREFIX_02         = "https://www.";              //$NON-NLS-1$

    /**
     * URI prefix for "U" records with code byte 3
     * 
     * @see #WELL_KNOWN_URI_PREFIX
     */
    private final String   URI_PREFIX_03         = "http://";                   //$NON-NLS-1$

    /**
     * URI prefix for "U" records with code byte 4
     * 
     * @see #WELL_KNOWN_URI_PREFIX
     */
    private final String   URI_PREFIX_04         = "https://";                  //$NON-NLS-1$

    /**
     * URI prefix for "U" records with code byte 5
     * 
     * @see #WELL_KNOWN_URI_PREFIX
     */
    private final String   URI_PREFIX_05         = "tel:";                      //$NON-NLS-1$

    /**
     * URI prefix for "U" records with code byte 6
     * 
     * @see #WELL_KNOWN_URI_PREFIX
     */

    private final String   URI_PREFIX_06         = "mailto:";                   //$NON-NLS-1$

    /**
     * URI prefix for "U" records with code byte 7
     * 
     * @see #WELL_KNOWN_URI_PREFIX
     */
    private final String   URI_PREFIX_07         = "ftp://anonymous:anonymous@"; //$NON-NLS-1$

    /**
     * URI prefix for "U" records with code byte 8
     * 
     * @see #WELL_KNOWN_URI_PREFIX
     */
    private final String   URI_PREFIX_08         = "ftp://ftp.";                //$NON-NLS-1$

    /**
     * URI prefix for "U" records with code byte 9
     * 
     * @see #WELL_KNOWN_URI_PREFIX
     */
    private final String   URI_PREFIX_09         = "ftps://";                   //$NON-NLS-1$

    /**
     * URI prefix for "U" records with code byte 10
     * 
     * @see #WELL_KNOWN_URI_PREFIX
     */
    private final String   URI_PREFIX_10         = "sftp://";                   //$NON-NLS-1$

    /**
     * URI prefix for "U" records with code byte 11
     * 
     * @see #WELL_KNOWN_URI_PREFIX
     */
    private final String   URI_PREFIX_11         = "smb://";                    //$NON-NLS-1$

    /**
     * URI prefix for "U" records with code byte 12
     * 
     * @see #WELL_KNOWN_URI_PREFIX
     */
    private final String   URI_PREFIX_12         = "nfs://";                    //$NON-NLS-1$

    /**
     * URI prefix for "U" records with code byte 13
     * 
     * @see #WELL_KNOWN_URI_PREFIX
     */
    private final String   URI_PREFIX_13         = "ftp://";                    //$NON-NLS-1$

    /**
     * URI prefix for "U" records with code byte 14
     */
    private final String   URI_PREFIX_14         = "dav://";                    //$NON-NLS-1$

    /**
     * URI prefix for "U" records with code byte 15
     * 
     * @see #WELL_KNOWN_URI_PREFIX
     */
    private final String   URI_PREFIX_15         = "news:";                     //$NON-NLS-1$

    /**
     * URI prefix for "U" records with code byte 16
     * 
     * @see #WELL_KNOWN_URI_PREFIX
     */
    private final String   URI_PREFIX_16         = "telnet://";                 //$NON-NLS-1$

    /**
     * URI prefix for "U" records with code byte 17
     * 
     * @see #WELL_KNOWN_URI_PREFIX
     */
    private final String   URI_PREFIX_17         = "imap:";                     //$NON-NLS-1$

    /**
     * URI prefix for "U" records with code byte 18
     * 
     * @see #WELL_KNOWN_URI_PREFIX
     */
    private final String   URI_PREFIX_18         = "rtsp://";                   //$NON-NLS-1$

    /**
     * URI prefix for "U" records with code byte 19
     * 
     * @see #WELL_KNOWN_URI_PREFIX
     */
    private final String   URI_PREFIX_19         = "urn:";                      //$NON-NLS-1$

    /**
     * URI prefix for "U" records with code byte 20
     * 
     * @see #WELL_KNOWN_URI_PREFIX
     */
    private final String   URI_PREFIX_20         = "pop:";                      //$NON-NLS-1$

    /**
     * URI prefix for "U" records with code byte 21
     * 
     * @see #WELL_KNOWN_URI_PREFIX
     */
    private final String   URI_PREFIX_21         = "sip:";                      //$NON-NLS-1$

    /**
     * URI prefix for "U" records with code byte 22
     * 
     * @see #WELL_KNOWN_URI_PREFIX
     */
    private final String   URI_PREFIX_22         = "sips:";                     //$NON-NLS-1$

    /**
     * URI prefix for "U" records with code byte 23
     * 
     * @see #WELL_KNOWN_URI_PREFIX
     */
    private final String   URI_PREFIX_23         = "tftp:";                     //$NON-NLS-1$

    /**
     * URI prefix for "U" records with code byte 24
     * 
     * @see #WELL_KNOWN_URI_PREFIX
     */
    private final String   URI_PREFIX_24         = "btspp://";                  //$NON-NLS-1$

    /**
     * URI prefix for "U" records with code byte 25
     * 
     * @see #WELL_KNOWN_URI_PREFIX
     */
    private final String   URI_PREFIX_25         = "btl2cap://";                //$NON-NLS-1$

    /**
     * URI prefix for "U" records with code byte 26
     * 
     * @see #WELL_KNOWN_URI_PREFIX
     */
    private final String   URI_PREFIX_26         = "btgoep://";                 //$NON-NLS-1$

    /**
     * URI prefix for "U" records with code byte 27
     * 
     * @see #WELL_KNOWN_URI_PREFIX
     */
    private final String   URI_PREFIX_27         = "tcpobex://";                //$NON-NLS-1$

    /**
     * URI prefix for "U" records with code byte 28
     * 
     * @see #WELL_KNOWN_URI_PREFIX
     */
    private final String   URI_PREFIX_28         = "irdaobex://";               //$NON-NLS-1$

    /**
     * URI prefix for "U" records with code byte 29
     * 
     * @see #WELL_KNOWN_URI_PREFIX
     */
    private final String   URI_PREFIX_29         = "file://";                   //$NON-NLS-1$

    /**
     * URI prefix for "U" records with code byte 30
     * 
     * @see #WELL_KNOWN_URI_PREFIX
     */
    private final String   URI_PREFIX_30         = "urn:epc:id:";               //$NON-NLS-1$

    /**
     * URI prefix for "U" records with code byte 31
     * 
     * @see #WELL_KNOWN_URI_PREFIX
     */
    private final String   URI_PREFIX_31         = "urn:epc:tag:";              //$NON-NLS-1$

    /**
     * URI prefix for "U" records with code byte 32
     * 
     * @see #WELL_KNOWN_URI_PREFIX
     */
    private final String   URI_PREFIX_32         = "urn:epc:pat:";              //$NON-NLS-1$

    /**
     * URI prefix for "U" records with code byte 33
     * 
     * @see #WELL_KNOWN_URI_PREFIX
     */
    private final String   URI_PREFIX_33         = "urn:epc:raw:";              //$NON-NLS-1$

    /**
     * URI prefix for "U" records with code byte 34
     * 
     * @see #WELL_KNOWN_URI_PREFIX
     */
    private final String   URI_PREFIX_34         = "urn:epc:";                  //$NON-NLS-1$

    /**
     * URI prefix for "U" records with code byte 35
     * 
     * @see #WELL_KNOWN_URI_PREFIX
     */
    private final String   URI_PREFIX_35         = "urn:nfc:";                  //$NON-NLS-1$

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
    private final String[] WELL_KNOWN_URI_PREFIX = { URI_PREFIX_00,
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
     * Prevent promiscuous instantiation of singleton class
     */
    private NdefRecordUtilities() {

        // nothing to do here

    }

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
    public NdefRecord createExternal(String domainName, String type,
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
    public NdefRecord createMime(String type, byte[] payload) {

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
    public NdefRecord createText(String text, String language) {

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
    public NdefRecord createUri(String uri) {

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
    public NdefRecord createUri(Uri uri) {

        return createUri(uri.toString());

    }

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
    public String decodePayload(NdefRecord record) {

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
    private String decodeMime(byte[] type, byte[] payload) {

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
    private String decodeText(byte[] payload)
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
    private String decodeUri(byte[] payload)
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
    private String decodeWellKnown(byte[] type, byte[] payload)
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

}