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
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * A fragment representing a single SettingsItem detail screen. This fragment is
 * either contained in a {@link ItemListActivity} in two-pane mode (on
 * tablets) or a {@link ItemDetailActivity} on handsets.
 */
public class ItemDetailFragment extends Fragment {

    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id"; //$NON-NLS-1$

    /**
     * The {@link Item} to use when populating this list item detail UI
     */
    private Item<?>    settingsItem;

    /**
     * Initialize this instance
     * 
     * @param savedInstanceState
     *            the persisted state of this {@link Fragment}, or
     *            <code>null</code>
     * 
     * @see Fragment#onCreate(Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {

            settingsItem = Item.getSettingsItem(getArguments()
                    .getString(ARG_ITEM_ID));

        }

    }

    /**
     * Initialize the {@link View} containing this {@link Fragment}
     * 
     * @param inflater
     *            the {@link LayoutInflater}
     * 
     * @param container
     *            the {@link ViewGroup}
     * 
     * @param savedInstanceState
     *            the persisted state of this {@link Fragment}, or
     *            <code>null</code>
     * 
     * @return the {@link View}
     * 
     * @see Fragment#onCreateView(LayoutInflater, ViewGroup, Bundle)
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        View rootView = inflater.inflate(settingsItem.getLayoutResource(),
                container, false);
        settingsItem.setView(getActivity(), rootView);
        return rootView;

    }

}
