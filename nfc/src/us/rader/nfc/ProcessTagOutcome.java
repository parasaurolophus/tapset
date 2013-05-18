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

import android.nfc.Tag;
import android.nfc.tech.TagTechnology;

/**
 * Additional diagnostic info for
 * {@link ForegroundDispatchActivity#onTagProcessed(Object, ProcessTagOutcome)}
 * 
 * @author Kirk
 */
public enum ProcessTagOutcome {

    /**
     * {@link ForegroundDispatchActivity#processTag(Tag)} reported that it took
     * no action for the given {@link Tag}
     */
    NOTHING_TO_DO,

    /**
     * Attempt to modify a write-protected {@link Tag}
     */
    READ_ONLY_TAG,

    /**
     * Successfully retrieved the contents of a {@link Tag}
     */
    SUCCESSFUL_READ,

    /**
     * Successfully modified the contents of a {@link Tag}
     */
    SUCCESSFUL_WRITE,

    /**
     * Attempt to write more data to a @link Tag} than it can hold
     */
    TAG_SIZE_EXCEEDED,

    /**
     * Some specific {@link TagTechnology} type's API threw an {@link Exception}
     * or reported a run-time error of some kind
     */
    TECHNOLOGY_ERROR,

    /**
     * The tag was empty when attempting to read, of an incompatible format type
     * when attempting write etc.
     */
    UNSUPPORTED_TAG

}
