/**
 * @author Kirk
 *
 * Boilerplate classes for any {@link android.app.Activity} that reads or
 * writes the contents of a NFC {@link android.nfc.Tag} using the
 * {@link android.nfc.NfcAdapter} foreground dispatch mechanism.
 * 
 * <p>
 * Classes that extend {@link us.rader.nfc.NfcReaderActivity} directly or
 * indirectly inherit <code>final</code> implementations of
 * </p>
 * 
 * <ul>
 * 
 * <li> {@link us.rader.nfc.NfcReaderActivity#onPause()}
 * 
 * <li> {@link us.rader.nfc.NfcReaderActivity#onResume()}
 * 
 * <li> {@link us.rader.nfc.NfcReaderActivity#onNewIntent(android.content.Intent)}
 * 
 * </ul>
 * 
 * <p>
 * that work together and in conjunction with the foreground dispatch mechanism
 * for accessing the contents of a {@link android.nfc.Tag}. Such classes must
 * provide implementations of three <code>abstract</code> methods
 * </p>
 * 
 * <ul>
 * 
 * <li> {@link us.rader.nfc.NfcReaderActivity#createNfcIntentFilters()}
 * 
 * <li> {@link us.rader.nfc.NfcReaderActivity#processTag(android.nfc.Tag)}
 * 
 * <li> {@link us.rader.nfc.NfcReaderActivity#onTagProcessed(java.lang.Object)}
 * 
 * </ul>
 * 
 * <p>
 * That are called at the correct points in the {@link android.app.Activity} and on
 * appropriate threads.
 * </p>
 * 
 * <p>
 * Note that the generic parameter type for {@link us.rader.nfc.NfcReaderActivity}
 * is declared to be the type returned by
 * {@link us.rader.nfc.NfcReaderActivity#processTag(android.nfc.Tag)} and expected by
 * {@link us.rader.nfc.NfcReaderActivity#onTagProcessed(java.lang.Object)}
 * </p>
 * 
 * @see us.rader.nfc.NfcReaderActivity
 * @see us.rader.nfc.NdefReaderActivity
 * @see us.rader.nfc.NdefWriterActivity
 */
package us.rader.nfc;