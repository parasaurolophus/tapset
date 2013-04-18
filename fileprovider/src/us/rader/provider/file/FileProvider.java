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

package us.rader.provider.file;

import java.io.File;
import java.io.FileNotFoundException;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

/**
 * Simple {@link ContentProvider} to share files without using the deprecated
 * MODE_WORLD_READABLE
 * 
 * @author Kirk
 */
public final class FileProvider extends ContentProvider {

    /**
     * Throw {@link UnsupportedOperationException}
     * 
     * @param uri
     *            ignored
     * 
     * @param selection
     *            ignored
     * 
     * @param selectionArgs
     *            ignored
     * 
     * @return unsupported operation doesn't return
     * 
     * @see ContentProvider#delete(Uri, String, String[])
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        throw new UnsupportedOperationException("not supported"); //$NON-NLS-1$

    }

    /**
     * Return MIME type for the given content {@link Uri}
     * 
     * TODO: hardwired to return "image/png" for now
     * 
     * @param uri
     *            the content {@link Uri}
     * 
     * @return the content's MIME type string
     * 
     * @see ContentProvider#getType(Uri)
     */
    @Override
    public String getType(Uri uri) {

        return "image/png"; //$NON-NLS-1$

    }

    /**
     * Throw {@link UnsupportedOperationException}
     * 
     * @param uri
     *            ignored
     * 
     * @param values
     *            ignored
     * 
     * @return doesn't return
     * 
     * @see ContentProvider#insert(Uri, ContentValues)
     */
    @Override
    public Uri insert(Uri uri, ContentValues values) {

        throw new UnsupportedOperationException("not supported"); //$NON-NLS-1$

    }

    /**
     * No-op for this class
     * 
     * @return <code>true</code>
     * 
     * @see ContentProvider#onCreate()
     */
    @Override
    public boolean onCreate() {

        // nothing to do here for this class

        return true;

    }

    /**
     * Return a {@link ParcelFileDescriptor} for reading from the file denoted
     * by the given content {@link Uri}
     * 
     * @param uri
     *            the content {@link Uri}
     * 
     * @param mode
     *            the requested access mode
     * 
     * @return the {@link ParcelFileDescriptor}
     * 
     * @throws FileNotFoundException
     *             if the specified file does not exist
     * 
     * @see ContentProvider#openFile(Uri, String)
     */
    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode)
            throws FileNotFoundException {

        int flags = 0;

        if ("r".equals(mode)) { //$NON-NLS-1$

            flags = ParcelFileDescriptor.MODE_READ_ONLY;

        } else if ("rw".equals(mode)) { //$NON-NLS-1$

            flags = ParcelFileDescriptor.MODE_READ_WRITE;

        } else if ("rwt".equals(mode)) { //$NON-NLS-1$

            flags = ParcelFileDescriptor.MODE_TRUNCATE
                    | ParcelFileDescriptor.MODE_READ_WRITE;

        } else {

            throw new IllegalArgumentException(
                    String.format(
                            "\"%s\" is not a valid mode; must be one of \"r,\" \"rw,\" or \"rwt\"", //$NON-NLS-1$
                            mode));

        }

        File file = new File(getContext().getFilesDir(), uri.getPath());

        if (file.exists()) {

            return (ParcelFileDescriptor.open(file, flags));

        }

        throw new FileNotFoundException(uri.getPath());

    }

    /**
     * Throw {@link UnsupportedOperationException}
     * 
     * @param uri
     *            ignored
     * 
     * @param projection
     *            ignored
     * 
     * @param selection
     *            ignored
     * 
     * @param selectionArgs
     *            ignored
     * 
     * @param sortOrder
     *            ignored
     * 
     * @return doesn't return
     * 
     * @see ContentProvider#query(Uri, String[], String, String[], String)
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {

        throw new UnsupportedOperationException("not supported"); //$NON-NLS-1$

    }

    /**
     * Throw {@link UnsupportedOperationException}
     * 
     * @param uri
     *            ignored
     * 
     * @param values
     *            ignored
     * 
     * @param selection
     *            ignored
     * 
     * @param selectionArgs
     *            ignored
     * 
     * @return doesn't return
     * 
     * @see ContentProvider#update(Uri, ContentValues, String, String[])
     */
    @Override
    public int update(Uri uri, ContentValues values, String selection,
            String[] selectionArgs) {

        throw new UnsupportedOperationException("not supported"); //$NON-NLS-1$

    }

}