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

package us.rader.tapset.item;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import us.rader.tapset.R;
import us.rader.tapset.ItemDetailFragment;
import us.rader.tapset.ItemListFragment;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.text.Layout;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

/**
 * Base class of objects representing collections of device settings with their
 * own UI in the app and their on query string parameters in a NFC tag or QR
 * code content
 * 
 * @param <T>
 *            The settings manager type that provides the model for instances of
 *            classes derived from this one
 * 
 * @author Kirk
 */
public abstract class Item<T> {

    /**
     * Global mapping from id strings to known instances of classes derived from
     * this one
     */
    private static Map<String, Item<?>> settingsItemMap;

    /**
     * Ordered list of known instances of classes derived from this one
     */
    private static List<Item<?>>        settingsItems;

    /**
     * Create the {@link Uri} representing the current state of all selected
     * device settings
     * 
     * @param context
     *            the {@link Context}
     * 
     * @return the {@link Uri}
     */
    public static Uri createUri(Context context) {

        HashMap<String, Object> parameters = new HashMap<String, Object>();

        for (Item<?> settingsItem : settingsItems) {

            settingsItem.addParameters(context, parameters);

        }

        StringBuffer buffer = new StringBuffer("http://www.rader.us/tapset"); //$NON-NLS-1$
        char separator = '?';

        for (String key : parameters.keySet()) {

            Object value = parameters.get(key);

            if (value != null) {

                buffer.append(separator);

                if (separator == '?') {

                    separator = '&';

                }

                buffer.append(Uri.encode(key));
                buffer.append('=');
                buffer.append(Uri.encode(value.toString()));

            }
        }

        return Uri.parse(buffer.toString());

    }

    /**
     * Return the {@link Item} instance with the specified id
     * 
     * @param id
     *            the item id
     * 
     * @return selected item or <code>null</code> if no such ite exists
     * 
     * @see #getId()
     */
    public static Item<?> getSettingsItem(String id) {

        Item<?> settingsItem = settingsItemMap.get(id);
        return settingsItem;

    }

    /**
     * Return the {@link List} of {@link Item} instances for all device
     * settings
     * 
     * Only valid after a call to {@link #initialize(Context)}
     * 
     * @return all {@link Item} intances
     * 
     * @see #initialize(Context)
     */
    public static List<Item<?>> getSettingsItems() {

        return settingsItems;

    }

    /**
     * Create the {@link Item} instances for all device settings
     * 
     * @param context
     *            the {@link Context}
     * 
     * @see #getSettingsItems()
     */
    public static void initialize(Context context) {

        settingsItemMap = new HashMap<String, Item<?>>();
        settingsItems = new ArrayList<Item<?>>();
        addItem(new VolumeSettings(context));
        addItem(new RingtoneSettings(context));

    }

    /**
     * Update all device settings based on the query string parameters in the
     * given {@link Uri}
     * 
     * @param context
     *            the {@link Context}
     * 
     * @param uri
     *            the {@link Uri} as read from a NFC tag or QR code
     */
    public static void updateAllSettings(Context context, Uri uri) {

        for (Item<?> item : settingsItems) {

            item.updateSettings(context, uri);

        }

        Toast.makeText(context,
                context.getString(R.string.settings_updated, uri.toString()),
                Toast.LENGTH_SHORT).show();

    }

    /**
     * Add the given {@link Item} to the global list
     * 
     * @param item
     *            the {@link Item}
     * 
     * @see #getSettingsItems()
     * @see #getSettingsItem(String)
     */
    private static void addItem(Item<?> item) {

        settingsItems.add(item);
        settingsItemMap.put(item.getId(), item);

    }

    /**
     * The settings manager instance that provides the model for instances of
     * classes derived from this one
     */
    T               settingsModel;

    /**
     * The label string for this {@link Item}
     * 
     * @see #getLabel()
     */
    private String  label;

    /**
     * Value used to track whether or not {@link #setView(Context, View)} has
     * been called
     */
    private boolean uiInitialized;

    /**
     * Initialize {@link #label} using the given {@link Context} to look up the
     * specified string resource id
     * 
     * @param settingsModel
     *            the settings manager instance of type &lt;T&gt;
     * 
     * @param label
     *            the label string
     * 
     * @see #label
     * @see #getLabel()
     */
    protected Item(T settingsModel, String label) {

        this.settingsModel = settingsModel;
        this.label = label;
        uiInitialized = false;

    }

    /**
     * Return the id string for this instance
     * 
     * @return the id string
     * 
     * @see #getSettingsItem(String)
     */
    public final String getId() {

        return getLabel().toLowerCase(Locale.getDefault());

    }

    /**
     * Return the label for this instance
     * 
     * Used when constructing the {@link ItemListFragment} UI
     * 
     * @return {@link #label}
     * 
     * @see #label
     * @see #Item(Object, String)
     */
    public final String getLabel() {

        return label;

    }

    /**
     * Return the resource id of the {@link Layout} to use for this instance's
     * UI
     * 
     * Used for the UI in {@link ItemDetailFragment}
     * 
     * @return the {@link Layout} resource id
     */
    public abstract int getLayoutResource();

    /**
     * Return the position of this instance in the global list
     * 
     * @return the positon of this instance
     * 
     * @see #getSettingsItems()
     */
    public final int getPosition() {

        int position = 0;

        for (Item<?> settingsItem : settingsItems) {

            if (settingsItem == this) {

                return position;

            }

            position += 1;

        }

        throw new IllegalStateException(MessageFormat.format(
                "{0} not found in list of settings items", toString())); //$NON-NLS-1$

    }

    /**
     * Initialize this instance's UI widgets
     * 
     * @param context
     *            the {@link Context}
     * 
     * @param settingsView
     *            the {@link View}
     * 
     * @see #initializeUi(Context, View)
     */
    public final void setView(Context context, View settingsView) {

        uiInitialized = initializeUi(context, settingsView);

    }

    /**
     * Override {@link Object#toString()} on behalf of {@link ArrayAdapter} used
     * in {@link ItemListFragment#onCreate(Bundle)}
     */
    @Override
    public final String toString() {

        return getLabel();

    }

    /**
     * Add the query string parameter name / value mappings for this instance to
     * the given {@link Map}
     * 
     * This is called by {@link #createUri(Context)} when constructing the
     * {@link Uri} to write to a NFC tag or QR code
     * 
     * @param context
     *            the {@link Context}
     * 
     * @param parameters
     *            the name / value mappings from the {@link Uri} query string
     *            parameters will be constructed
     */
    protected abstract void addParameters(Context context,
            Map<String, Object> parameters);

    /**
     * Get the settings model object
     * 
     * @return {@link #settingsModel}
     */
    protected final T getModel() {

        return settingsModel;

    }

    /**
     * Called by {@link #setView(Context, View)} to initialize any UI event
     * handlers and cached state for the given {@link View}
     * 
     * @param context
     *            the {@link Context}
     * 
     * @param view
     *            the settings {@link View}
     * 
     * @return <code>true</code> if and only if the UI was successfully
     *         initialized
     * 
     * @see #setView(Context, View)
     * @see #isUiInitialized()
     */
    protected abstract boolean initializeUi(Context context, View view);

    /**
     * Get the value of {@link #uiInitialized}
     * 
     * @return {@link #uiInitialized}
     * 
     * @see #setView(Context, View)
     */
    protected final boolean isUiInitialized() {

        return uiInitialized;

    }

    /**
     * Set the value of {@link #uiInitialized}
     * 
     * @param uiInitialized
     *            the new value for {@link #uiInitialized}
     * 
     * @see #setView(Context, View)
     */
    protected final void setUiInitialize(boolean uiInitialized) {

        this.uiInitialized = uiInitialized;

    }

    /**
     * Update the devices managed by this instance based on the query string
     * parameters in the given {@link Uri}
     * 
     * This is called by {@link #updateAllSettings(Context, Uri)}, once for each
     * entry in {@link #settingsItems}
     * 
     * @param context
     *            the {@link Context}
     * 
     * @param uri
     *            the {@link Uri}
     * 
     * @see #settingsItems
     * @see #getSettingsItems()
     */
    protected abstract void updateSettings(Context context, Uri uri);

}