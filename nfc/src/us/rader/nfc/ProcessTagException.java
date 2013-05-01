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

/**
 * {@link Exception} thrown by
 * {@link NfcReaderActivity#processTag(android.nfc.Tag)} to signal an error and
 * set the response code
 * 
 * @author Kirk
 * 
 */
public class ProcessTagException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    /**
     * The result code
     * 
     * @see #getResultCode()
     */
    private int resultCode;

    /**
     * 
     * @param resultCode
     *            the result code
     */
    public ProcessTagException(int resultCode) {

        this.resultCode = resultCode;

    }

    /**
     * 
     * @param resultCode
     *            the result code
     * 
     * @param detailMessage
     *            the message
     */
    public ProcessTagException(int resultCode, String detailMessage) {

        super(detailMessage);
        this.resultCode = resultCode;

    }

    /**
     * 
     * @param resultCode
     *            the result code
     * 
     * @param detailMessage
     *            the message
     * 
     * @param cause
     *            the cause
     */
    public ProcessTagException(int resultCode, String detailMessage,
            Throwable cause) {

        super(detailMessage, cause);
        this.resultCode = resultCode;

    }

    /**
     * 
     * @param resultCode
     *            the result code
     * 
     * @param cause
     *            the cause
     */
    public ProcessTagException(int resultCode, Throwable cause) {

        super(cause);
        this.resultCode = resultCode;

    }

    /**
     * Return the result code
     * 
     * @return the result code
     */
    public final int getResultCode() {

        return resultCode;

    }

}
