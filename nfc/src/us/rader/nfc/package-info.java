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

/**
 * @author Kirk
 *
 * Boilerplate classes for any {@link android.app.Activity} that reads or
 * writes the contents of a NFC {@link android.nfc.Tag} using the
 * {@link android.nfc.NfcAdapter} foreground dispatch mechanism.
 * 
 * <p>
 * Classes that extend {@link us.rader.nfc.ForegroundDispatchActivity} directly or
 * indirectly inherit <code>final</code> implementations of
 * </p>
 * 
 * <ul>
 * 
 * <li> {@link us.rader.nfc.ForegroundDispatchActivity#onPause()}
 * 
 * <li> {@link us.rader.nfc.ForegroundDispatchActivity#onResume()}
 * 
 * <li> {@link us.rader.nfc.ForegroundDispatchActivity#onNewIntent(android.content.Intent)}
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
 * <li> {@link us.rader.nfc.ForegroundDispatchActivity#createNfcIntentFilters()}
 * 
 * <li> {@link us.rader.nfc.ForegroundDispatchActivity#processTag(android.nfc.Tag, us.rader.nfc.ForegroundDispatchActivity.ProcessTagTask)}
 * 
 * <li> {@link us.rader.nfc.ForegroundDispatchActivity#onTagProcessed(java.lang.Object, ProcessTagOutcome)}
 * 
 * </ul>
 * 
 * <p>
 * that are called at the correct points in the {@link android.app.Activity} life-cycle and on
 * appropriate threads.
 * </p>
 * 
 * <p>
 * Note that the generic parameter type for {@link us.rader.nfc.ForegroundDispatchActivity}
 * is declared to be the type returned by
 * {@link us.rader.nfc.ForegroundDispatchActivity#processTag(android.nfc.Tag, us.rader.nfc.ForegroundDispatchActivity.ProcessTagTask)}
 * and expected by
 * {@link us.rader.nfc.ForegroundDispatchActivity#onTagProcessed(java.lang.Object, ProcessTagOutcome)}.
 * The former is called on a worker thread when a {@link android.nfc.Tag} is detected while foreground
 * dispatch is enabled and whatever it returns is passed to the latter, on the UI thread
 * </p>
 * 
 * @see us.rader.nfc.ForegroundDispatchActivity
 * @see us.rader.nfc.NdefReaderActivity
 * @see us.rader.nfc.NdefWriterActivity
 */
package us.rader.nfc;

