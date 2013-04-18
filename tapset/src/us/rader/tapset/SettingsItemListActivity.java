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

import us.rader.tapset.settingsitems.SettingsItem;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

/**
 * An activity representing a list of SettingsItems. This activity has different
 * presentations for handset and tablet-size devices. On handsets, the activity
 * presents a list of items, which when touched, lead to a
 * {@link SettingsItemDetailActivity} representing item details. On tablets, the
 * activity presents the list of items and item details side-by-side using two
 * vertical panes.
 * <p>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link SettingsItemListFragment} and the item details (if present) is a
 * {@link SettingsItemDetailFragment}.
 * <p>
 * This activity also implements the required
 * {@link SettingsItemListFragment.ItemSelectedListener} interface to listen for
 * item selections.
 */
public class SettingsItemListActivity extends FragmentActivity implements
        SettingsItemListFragment.ItemSelectedListener {

    /**
     * Initiate a request to scan a QR code on behalf of the given
     * {@link Activity}
     * 
     * The response will be delivered to the given instance's
     * {@link Activity#onActivityResult(int, int, Intent)}
     * 
     * @param activity
     *            the {@link Activity} to notify with the result of the request
     * 
     * @return <code>true</code>
     */
    public static boolean scanQrCode(Activity activity) {

        IntentIntegrator integrator = new IntentIntegrator(activity);
        integrator.initiateScan();
        return true;

    }

    /**
     * Launch a {@link ShowQrCodeActivity}
     * 
     * @param context
     *            the {@link Context} on whose behalf to launch a
     *            {@link ShowQrCodeActivity}
     * 
     * @return <code>true</code>
     */
    public static boolean showQrCode(Context context) {

        Intent intent = new Intent(
                "us.rader.tapset.qrcode", //$NON-NLS-1$
                SettingsItem.createUri(context), context,
                ShowQrCodeActivity.class);
        context.startActivity(intent);
        return true;

    }

    /**
     * Launch a {@link WriteTagActivity}
     * 
     * @param context
     *            the {@link Context} on whose behalf to launch a
     *            {@link WriteTagActivity}
     * 
     * @return <code>true</code>
     */
    public static boolean writeTag(Context context) {

        Intent intent = new Intent(
                "us.rader.tapset.writetag", //$NON-NLS-1$
                SettingsItem.createUri(context), context,
                WriteTagActivity.class);
        context.startActivity(intent);
        return true;

    }

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean twoPane;

    /**
     * Initialize {@link #twoPane} to <code>false</code>
     */
    public SettingsItemListActivity() {

        twoPane = false;

    }

    /**
     * Initialize the options {@link Menu}
     * 
     * @param menu
     *            the options {@link Menu}
     * 
     * @return <code>true</code>
     * 
     * @see Activity#onCreateOptionsMenu(Menu)
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_options, menu);
        return true;

    }

    /**
     * Callback method from
     * {@link SettingsItemListFragment.ItemSelectedListener} indicating that the
     * item with the given ID was selected.
     */
    @Override
    public void onItemSelected(String id) {

        if (twoPane) {

            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putString(SettingsItemDetailFragment.ARG_ITEM_ID, id);
            SettingsItemDetailFragment fragment = new SettingsItemDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.settingsitem_detail_container, fragment)
                    .commit();

        } else {

            // In single-pane mode, simply start the detail activity
            // for the selected item ID.
            Intent detailIntent = new Intent(this,
                    SettingsItemDetailActivity.class);
            detailIntent.putExtra(SettingsItemDetailFragment.ARG_ITEM_ID, id);
            startActivity(detailIntent);

        }
    }

    /**
     * Handle an options {@link MenuItem}
     * 
     * @param item
     *            the {@link MenuItem} to handle
     * 
     * @return <code>true</code> if and only if the {@link MenuItem} was
     *         consumed
     * 
     * @see Activity#onOptionsItemSelected(MenuItem)
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.write_tag_item:

                return writeTag(this);

            case R.id.share_qr_code_item:

                return showQrCode(this);

            case R.id.scan_qr_code_item:

                return scanQrCode(this);

            default:

                return super.onOptionsItemSelected(item);

        }
    }

    /**
     * Notification that the {@link Activity} that was launched to handle a
     * {@link Activity#startActivityForResult(Intent, int)} or
     * {@link Activity#startActivityForResult(Intent, int, Bundle)} request has
     * returned a result
     * 
     * This override uses
     * {@link IntentIntegrator#parseActivityResult(int, int, Intent)} to forward
     * the {@link Uri} from the result's {@link Intent#getData()} to
     * {@link SettingsItem#updateAllSettings(Context, Uri)} on the assumption
     * that the handler {@link Activity} was launched to scan a QR code
     * 
     * @param requestCode
     *            the request id originally passed to
     *            {@link Activity#startActivityForResult(Intent, int)}
     * 
     * @param resultCode
     *            the result code set by the handler {@link Activity} using
     *            {@link Activity#setResult(int)} or
     *            {@link Activity#setResult(int, Intent)}
     * 
     * @param resultIntent
     *            the result intent set by the handler {@link Activity} by
     *            {@link Activity#setResult(int, Intent)}
     * 
     * @see SettingsItem#updateAllSettings(Context, Uri)
     * @see FragmentActivity#onActivityResult(int, int, Intent)
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode,
            Intent resultIntent) {

        try {

            IntentResult intentResult = IntentIntegrator.parseActivityResult(
                    requestCode, resultCode, resultIntent);
            SettingsItem.updateAllSettings(this,
                    Uri.parse(intentResult.getContents()));

        } catch (Exception e) {

            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();

        }

    }

    /**
     * Prepare this {@link SettingsItemListActivity} to be displayed
     * 
     * @param savedInstanceState
     *            persisted app state or <code>null</code>
     * 
     * @see FragmentActivity#onCreate(Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        SettingsItem.initialize(this);
        setContentView(R.layout.activity_settingsitem_list);

        if (findViewById(R.id.settingsitem_detail_container) != null) {

            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            twoPane = true;
            SettingsItemListFragment settingsItemListFragment = (SettingsItemListFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.settingsitem_list);
            settingsItemListFragment.setActivateOnItemClick(true);

        }

        Intent intent = getIntent();

        if (intent != null) {

            Uri uri = intent.getData();

            if (uri != null) {

                SettingsItem.updateAllSettings(this, uri);

            }
        }

    }

}