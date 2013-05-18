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

package us.rader.tapset;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

import us.rader.provider.file.FileProvider;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

/**
 * {@link Activity} used to display the QR code representation of the currently
 * selected device settings values
 * 
 * @author Kirk
 */
public class ShowQrCodeActivity extends Activity {

    /**
     * The default {@link Bitmap} if there is no QR code image to display
     */
    private static Bitmap    defaultBitmap;

    /**
     * Size of each side of the square QR image {@link Bitmap}
     */
    private static final int QR_DIMENSION = 350;

    static {

        defaultBitmap = Bitmap.createBitmap(QR_DIMENSION, QR_DIMENSION,
                Bitmap.Config.ARGB_8888);

    }

    /**
     * Return the QR code image {@link Bitmap} for the given {@link Uri}
     * 
     * @param uri
     *            the {@link Uri} to encode
     * 
     * @return the QR code {@link Bitmap}
     * 
     * @throws WriterException
     *             if an error occurs encoding the QR code
     */
    private static Bitmap encodeQrCode(Uri uri) throws WriterException {

        QRCodeWriter writer = new QRCodeWriter();
        Map<EncodeHintType, Object> hints = new HashMap<EncodeHintType, Object>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
        BitMatrix bitMatrix = writer.encode(uri.toString(),
                BarcodeFormat.QR_CODE, QR_DIMENSION, QR_DIMENSION, hints);
        Bitmap bitmap = Bitmap.createBitmap(QR_DIMENSION, QR_DIMENSION,
                Bitmap.Config.ARGB_8888);

        for (int y = 0; y < bitMatrix.getHeight(); ++y) {

            for (int x = 0; x < bitMatrix.getWidth(); ++x) {

                bitmap.setPixel(x, y, (bitMatrix.get(x, y) ? Color.BLACK
                        : Color.WHITE));

            }
        }

        return bitmap;

    }

    /**
     * The current QR code image {@link Bitmap}
     */
    private Bitmap    bitmap;

    /**
     * The {@link ImageView} in which to display the contents of {@link #bitmap}
     */
    private ImageView qrView;

    /**
     * Initialize the options {@link Menu}
     * 
     * @param menu
     *            the {@link Menu}
     * 
     * @return <code>true</code>
     * 
     * @see Activity#onCreateOptionsMenu(Menu)
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.qr_code_options, menu);
        return true;

    }

    /**
     * Handle an options {@link MenuItem}
     * 
     * @param item
     *            the {@link MenuItem}
     * 
     * @return <code>true</code> if and only if the {@link MenuItem} was
     *         consumed
     * 
     * @see Activity#onOptionsItemSelected(MenuItem)
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.share_item:

                shareQrCode();
                return true;

            case android.R.id.home:

                // This ID represents the Home or Up button. In the case of this
                // activity, the Up button is shown. Use NavUtils to allow users
                // to navigate up one level in the application structure. For
                // more details, see the Navigation pattern on Android Design:
                //
                // http://developer.android.com/design/patterns/navigation.html#up-vs-back
                //
                NavUtils.navigateUpFromSameTask(this);
                return true;

            default:

                return super.onOptionsItemSelected(item);

        }
    }

    /**
     * Prepare this instance to be displayed
     * 
     * @param savedInstanceState
     *            the persisted app state or <code>null</code>
     * 
     * @see Activity#onCreate(Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_qr_code);
        // Show the Up button in the action bar.
        setupActionBar();
        qrView = (ImageView) findViewById(R.id.qr_view);

        qrView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                shareQrCode();

            }

        });

        Uri uri = null;
        Intent intent = getIntent();

        if (intent != null) {

            uri = intent.getData();

        }

        createQrCode(uri);

    }

    /**
     * Set {@link #bitmap} to the QR code image {@link Bitmap} to display
     * 
     * This will fall back on {@link #defaultBitmap} if the given {@link Uri} is
     * <code>null</code> or an error occurs while encoding it to a QR code
     * 
     * @param uri
     *            the {@link Uri} to encode
     */
    private synchronized void createQrCode(Uri uri) {

        bitmap = defaultBitmap;

        if (null == uri) {

            Toast.makeText(this, R.string.no_uri_to_encode, Toast.LENGTH_LONG)
                    .show();

        } else {

            try {

                bitmap = encodeQrCode(uri);

            } catch (Exception e) {

                Toast.makeText(this, R.string.error_encoding_qr,
                        Toast.LENGTH_LONG).show();

            }

        }

        qrView.setImageBitmap(bitmap);

    }

    /**
     * Set up the {@link android.app.ActionBar}.
     */
    @SuppressLint("NewApi")
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setupActionBar() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {

            getActionBar().setDisplayHomeAsUpEnabled(true);

        }
    }

    /**
     * Save {@link #bitmap} to a file and then send a sharing {@link Intent}
     * wrapped in a chooser
     * 
     * @see Intent#ACTION_SEND
     * @see Intent#createChooser(Intent, CharSequence)
     * @see Activity#startActivity(Intent)
     */
    private void shareQrCode() {

        try {

            File file = getFileStreamPath("tapset_qr.png"); //$NON-NLS-1$
            FileOutputStream stream = openFileOutput(file.getName(), 0);

            try {

                bitmap.compress(CompressFormat.PNG, 100, stream);

            } finally {

                stream.close();

            }

            String label = getString(R.string.share_label_text);
            Uri uri = FileProvider
                    .getContentUri(getString(R.string.provider_authority_file),
                            file.getName());
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setData(uri);
            intent.setType(FileProvider.getMimeType(uri));
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Intent chooser = Intent.createChooser(intent, label);
            startActivity(chooser);

        } catch (Exception e) {

            // ignore errors here

        }
    }

}
