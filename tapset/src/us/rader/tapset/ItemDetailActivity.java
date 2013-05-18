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

import us.rader.tapset.item.Item;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;

import com.google.zxing.integration.android.IntentIntegrator;

/**
 * An activity representing a single SettingsItem detail screen.
 * 
 * <p>
 * This activity is only used on handset devices. On tablet-size devices, item
 * details are presented side-by-side with a list of items in a
 * {@link ItemListActivity}.
 * </p>
 * 
 * <p>
 * This activity is mostly just a 'shell' activity containing nothing more than
 * a {@link ItemDetailFragment}.
 * </p>
 */
public class ItemDetailActivity extends FragmentActivity {

    /**
     * Add the "Write tag..." options menu item
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_options, menu);
        return super.onCreateOptionsMenu(menu);

    }

    /**
     * Handle an options {@link MenuItem}
     * 
     * @param item
     *            the {@link MenuItem}
     * 
     * @return <code>true</code> if the menu item has been consumed,
     *         <code>false</code> to allow menu processing to continue
     * 
     * @see Activity#onOptionsItemSelected(MenuItem)
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.write_tag_item:

                return ItemListActivity.writeTag(this);

            case R.id.share_qr_code_item:

                return ItemListActivity.showQrCode(this);

            case R.id.scan_qr_code_item:

                return ItemListActivity.scanQrCode(this);

            case android.R.id.home:

                // This ID represents the Home or Up button. In the case of this
                // activity, the Up button is shown. Use NavUtils to allow users
                // to navigate up one level in the application structure. For
                // more details, see the Navigation pattern on Android Design:
                //
                // http://developer.android.com/design/patterns/navigation.html#up-vs-back
                //
                NavUtils.navigateUpTo(this, new Intent(this,
                        ItemListActivity.class));
                return true;

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
     * This override forwards the {@link Uri} from the result's
     * {@link Intent#getData()} to {@link Item#updateAllSettings(Context, Uri)}
     * on the assumption that the handler {@link Activity} was launched to read
     * a NFC tag or scan a QR code
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
     * @see Item#updateAllSettings(Context, Uri)
     * @see FragmentActivity#onActivityResult(int, int, Intent)
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode,
            Intent resultIntent) {

        switch (requestCode) {

            case ItemListActivity.REQUEST_CODE_WRITE_TAG:

                ItemListActivity.handleWriteTagResult(this, resultIntent);
                break;

            case IntentIntegrator.REQUEST_CODE:

                ItemListActivity.updateSettingsFromQr(this, requestCode,
                        resultCode, resultIntent);
                break;

            default:

                throw new IllegalStateException(
                        getString(R.string.unrecognized_result_code));

        }
    }

    /**
     * Prepare this {@link ItemDetailActivity} to be displayed
     * 
     * @param savedInstanceState
     *            persisted app state or <code>null</code>
     * 
     * @see FragmentActivity#onCreate(Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settingsitem_detail);
        String id = getIntent().getStringExtra(ItemDetailFragment.ARG_ITEM_ID);
        Item<?> settingsItem = Item.getSettingsItem(id);
        setTitle(settingsItem.getLabel());
        setupActionBar();

        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //
        if (savedInstanceState == null) {

            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putString(ItemDetailFragment.ARG_ITEM_ID, id);
            ItemDetailFragment fragment = new ItemDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.settingsitem_detail_container, fragment).commit();

        }

    }

    /**
     * Work around bugs in backward-compatibility library
     */
    @SuppressLint("NewApi")
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void setupActionBar() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            // Show the Up button in the action bar.
            ActionBar actionBar = getActionBar();
            actionBar.setDisplayHomeAsUpEnabled(true);

        }
    }
}
