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
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * A list fragment representing a list of SettingsItems. This fragment also
 * supports tablet devices by allowing list items to be given an 'activated'
 * state upon selection. This helps indicate which item is currently being
 * viewed in a {@link ItemDetailFragment}.
 * <p>
 * Activities containing this fragment MUST implement the
 * {@link ItemSelectedListener} interface.
 */
public class ItemListFragment extends ListFragment {

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface ItemSelectedListener {

        /**
         * Callback for when an item has been selected.
         * 
         * @param id
         *            the {@link Item} id of the selected item
         */
        public void onItemSelected(String id);

    }

    /**
     * A dummy implementation of the {@link ItemSelectedListener} interface that
     * does nothing. Used only when this fragment is not attached to an
     * activity.
     */
    private static ItemSelectedListener dummyItemSelectedListener;

    /**
     * The serialization (saved instance state) Bundle key representing the
     * activated item position. Only used on tablets.
     */
    private static final String         STATE_ACTIVATED_POSITION = "activated_position"; //$NON-NLS-1$

    static {

        dummyItemSelectedListener = new ItemSelectedListener() {

            @Override
            public void onItemSelected(String id) {

                // deliberately empty method implementation

            }

        };

    }

    /**
     * The current activated item position. Only used on tablets.
     */
    private int                         activatedPosition;

    /**
     * The fragment's current callback object, which is notified of list item
     * clicks.
     */
    private ItemSelectedListener        itemSelectedListener;

    /**
     * Initialize this instance to its inactive state
     */
    public ItemListFragment() {

        itemSelectedListener = dummyItemSelectedListener;
        activatedPosition = AdapterView.INVALID_POSITION;

    }

    /**
     * Attach this {@link ItemListFragment} to the given
     * {@link Activity}
     * 
     * Note that the given {@link Activity} must implement
     * {@link ItemSelectedListener}, a fact that is checked at run time due to
     * the failure to impose generic constraints on this method at compile time
     * 
     * @param activity
     *            the {@link Activity}
     * 
     * @see Fragment#onAttach(Activity)
     */
    @Override
    public void onAttach(Activity activity) {

        super.onAttach(activity);

        // Activities containing this fragment must implement its callbacks.
        if (!(activity instanceof ItemSelectedListener)) {

            throw new IllegalStateException(
                    "Activity must implement fragment's callbacks."); //$NON-NLS-1$

        }

        itemSelectedListener = (ItemSelectedListener) activity;

    }

    /**
     * Initialize this instance
     * 
     * @param savedInstanceState
     *            persisted state or <code>null</code>
     * 
     * @see Fragment#onCreate(Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setListAdapter(new ArrayAdapter<Item<?>>(getActivity(),
                android.R.layout.simple_list_item_activated_1,
                android.R.id.text1, Item.getSettingsItems()));

    }

    /**
     * Detach this instance from any {@link ItemSelectedListener} to which it is
     * attached
     * 
     * @see android.support.v4.app.Fragment#onDetach()
     */
    @Override
    public void onDetach() {

        super.onDetach();
        // Reset the active callbacks interface to the dummy implementation.
        itemSelectedListener = dummyItemSelectedListener;

    }

    /**
     * Handle a click event
     * 
     * @param listView
     *            the {@link ListView} on which the user clicked
     * 
     * @param view
     *            the container {@link View}
     * 
     * @param position
     *            the position of the item on which the user clicked
     * 
     * @param id
     *            the id of the item on which the user clicked
     * 
     * @see ListFragment#onListItemClick(ListView, View, int, long)
     */
    @Override
    public void onListItemClick(ListView listView, View view, int position,
            long id) {

        super.onListItemClick(listView, view, position, id);
        String settingsItemId = Item.getSettingsItems().get(position)
                .getId();
        itemSelectedListener.onItemSelected(settingsItemId);

    }

    /**
     * Persist any relevant app state on behalf of this instance
     * 
     * @param outState
     *            the {@link Bundle} in which to put the persisted state
     * 
     * @see Fragment#onSaveInstanceState(Bundle)
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);

        if (activatedPosition != AdapterView.INVALID_POSITION) {

            // Serialize and persist the activated item position.
            outState.putInt(STATE_ACTIVATED_POSITION, activatedPosition);

        }
    }

    /**
     * 
     * @param view
     *            the container {@link View}
     * 
     * @param savedInstanceState
     *            the {@link Bundle} containing persisted state data for this
     *            instance, or <code>null</code>
     * 
     * @see ListFragment#onViewCreated(View, Bundle)
     */
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        // Restore the previously serialized activated item position.
        if (savedInstanceState != null
                && savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {

            setActivatedPosition(savedInstanceState
                    .getInt(STATE_ACTIVATED_POSITION));

        }
    }

    /**
     * Update the selected item in this {@link ItemListFragment}
     * 
     * @param position
     *            the newly activated position
     */
    public void setActivatedPosition(int position) {

        if (position == AdapterView.INVALID_POSITION) {

            getListView().setItemChecked(activatedPosition, false);

        } else {

            getListView().setItemChecked(position, true);

        }

        activatedPosition = position;
    }

    /**
     * @param activateOnItemClick
     *            value indicating whether or not an item should be activated
     *            automatically when it is clicked Turns on activate-on-click
     *            mode. When this mode is on, list items will be given the
     *            'activated' state when touched.
     */
    public void setActivateOnItemClick(boolean activateOnItemClick) {

        // When setting CHOICE_MODE_SINGLE, ListView will automatically
        // give items the 'activated' state when touched.
        getListView().setChoiceMode(
                activateOnItemClick ? AbsListView.CHOICE_MODE_SINGLE
                        : AbsListView.CHOICE_MODE_NONE);

    }

}