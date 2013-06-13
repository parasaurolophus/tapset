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

package us.rader.tapset.nfc;

import java.io.IOException;

import android.app.Activity;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.util.Log;

/**
 * Super class of any {@link Activity} that prompts the user to scan a NFC tag
 * so as to write a {@link NdefMessage} to it
 * 
 * Since writing a {@link NdefMessage} involves the use of the
 * {@link NfcAdapter} foreground dispatch mechanism in a way that is almost
 * identical to that which is used to read one, this class extends
 * {@link NdefReaderActivity}. This isn't mere "inheritance for implementation,"
 * (though objections along such lines are frequently more a matter of point of
 * view than of objective fact -- pun intended) since this presents a paradigm
 * in which the contents of a {@link Tag} can be "modified" by passing the
 * current {@link NdefMessage} to {@link #createNdefMessage(NdefMessage)}
 * 
 * @see #createNdefMessage(NdefMessage)
 * @see #processTag(Tag, us.rader.tapset.nfc.ForegroundDispatchActivity.ProcessTagTask)
 *      )
 * 
 * @author Kirk
 */
public abstract class NdefWriterActivity extends NdefReaderActivity {

    /**
     * If <code>true</code> attempt to mark the {@link Tag} read-only after
     * writing or while formatting in
     * {@link #processTag(Tag, us.rader.tapset.nfc.ForegroundDispatchActivity.ProcessTagTask)
     * )}
     * 
     * @see #isReadOnlyRequested()
     * @see #setReadonlyRequested(boolean)
     * @see #processTag(Tag,
     *      us.rader.tapset.nfc.ForegroundDispatchActivity.ProcessTagTask)
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
     *      us.rader.tapset.nfc.ForegroundDispatchActivity.ProcessTagTask)
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
     * {@link #processTag(Tag, us.rader.tapset.nfc.ForegroundDispatchActivity.ProcessTagTask)}
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
     *      us.rader.tapset.nfc.ForegroundDispatchActivity.ProcessTagTask)
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
     *            the
     *            {@link us.rader.tapset.nfc.ForegroundDispatchActivity.ProcessTagTask}
     * 
     * @return the {@link NdefMessage} that was written to the {@link Tag} or
     *         <code>null</code> if an error occurs
     * 
     * @see us.rader.tapset.nfc.NdefReaderActivity#processTag(Tag,
     *      us.rader.tapset.nfc.ForegroundDispatchActivity.ProcessTagTask)
     */
    @Override
    protected final NdefMessage processTag(Tag tag, ProcessTagTask task) {

        if (tag == null) {

            task.setOutcome(ProcessTagOutcome.NOTHING_TO_DO);
            return null;

        }

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
     *            the
     *            {@link us.rader.tapset.nfc.ForegroundDispatchActivity.ProcessTagTask}
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
        NdefMessage newMessage = createNdefMessage(currentContents);

        if (newMessage == null) {

            if (currentContents == null) {

                Log.e(NdefWriterActivity.class.getName(),
                        "cached NDEF message is null"); //$NON-NLS-1$
                task.setOutcome(ProcessTagOutcome.UNSUPPORTED_TAG);

            } else {

                task.setOutcome(ProcessTagOutcome.SUCCESSFUL_READ);

            }

            return currentContents;

        }

        if (!ndef.isWritable()) {

            task.setOutcome(ProcessTagOutcome.READ_ONLY_TAG);
            return currentContents;

        }

        byte[] bytes = newMessage.toByteArray();
        int maxSize = ndef.getMaxSize();

        if (bytes.length > maxSize) {

            task.setOutcome(ProcessTagOutcome.TAG_SIZE_EXCEEDED);
            return currentContents;

        }

        ndef.connect();

        try {

            ndef.writeNdefMessage(newMessage);

            if (readOnlyRequested) {

                ndef.makeReadOnly();

            }

            task.setOutcome(ProcessTagOutcome.SUCCESSFUL_WRITE);
            return newMessage;

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
     *            the
     *            {@link us.rader.tapset.nfc.ForegroundDispatchActivity.ProcessTagTask}
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