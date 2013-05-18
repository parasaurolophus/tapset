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

import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.util.Log;

/**
 * {@link ForegroundDispatchActivity} that reads the contents of a NDEF
 * formatted {@link Tag}
 * 
 * @see ForegroundDispatchActivity
 * 
 * @author Kirk
 */
public abstract class NdefReaderActivity extends
        ForegroundDispatchActivity<NdefMessage> {

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
     * @see us.rader.nfc.ForegroundDispatchActivity#processTag(android.nfc.Tag,
     *      us.rader.nfc.ForegroundDispatchActivity.ProcessTagTask)
     */
    @Override
    protected NdefMessage processTag(Tag tag, ProcessTagTask task) {

        if (tag == null) {

            task.setOutcome(ProcessTagOutcome.NOTHING_TO_DO);
            return null;

        }

        Ndef ndef = Ndef.get(tag);

        if (ndef == null) {

            Log.w(NdefReaderActivity.class.getName(),
                    "tag is not NDEF formatted"); //$NON-NLS-1$
            task.setOutcome(ProcessTagOutcome.UNSUPPORTED_TAG);
            return null;

        }

        task.setOutcome(ProcessTagOutcome.SUCCESSFUL_READ);
        return ndef.getCachedNdefMessage();

    }

}
