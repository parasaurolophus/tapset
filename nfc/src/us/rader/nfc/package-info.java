/**
 * @author Kirk
 *
 * Boilerplate classes for any {@link android.app.Activity} that reads or
 * writes the contents of a NFC {@link android.nfc.Tag} using the
 * {@link android.nfc.NfcAdapter} foreground dispatch mechanism.
 * 
 * <p>
 * Classes that extend {@link us.rader.nfc.NfcReaderActivity} directly or
 * indirectly inherit implementations of {@link android.app.Activity#onPause()} ,
 * {@link android.app.Activity#onResume()} and
 * {@link android.app.Activity#onNewIntent(android.content.Intent)} that work
 * together in conjunction with the foreground dispatch mechanism for accessing
 * the contents of a {@link android.nfc.Tag}. Such classes must provide implementations
 * of two <code>abstract</code> methods,
 * {@link us.rader.nfc.NfcReaderActivity#processTag(android.nfc.Tag)} and
 * {@link us.rader.nfc.NfcReaderActivity#onTagProcessed(android.os.Parcelable)}
 * </p>
 * 
 * <p>
 * Note that the generic parameter type for {@link us.rader.nfc.NfcReaderActivity}
 * is declared to be the type returned by
 * {@link us.rader.nfc.NfcReaderActivity#processTag(android.nfc.Tag)} and expected by
 * {@link us.rader.nfc.NfcReaderActivity#onTagProcessed(android.os.Parcelable)} .
 * It is constrained to extend {@link android.os.Parcelable} in the expectation that
 * such objects will be passed from one {@link android.app.Activity} to another as
 * {@link android.content.Intent} "extras."
 * </p>
 * 
 * @see us.rader.nfc.NfcReaderActivity
 * @see us.rader.nfc.NdefReaderActivity
 * @see us.rader.nfc.NdefWriterActivity
 */
package us.rader.nfc;