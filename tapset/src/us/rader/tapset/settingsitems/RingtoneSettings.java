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

package us.rader.tapset.settingsitems;

import java.util.Map;

import us.rader.tapset.R;
import android.content.Context;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.text.Layout;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

/**
 * {@link SettingsItem} for reading and writing {@link AudioManager} ringtone
 * API's in NDEF tags
 * 
 * @author Kirk
 * 
 */
class RingtoneSettings extends SettingsItem<RingtoneSettings.DummyManager> {

    /**
     * Dummy settings model to conform to generic parent class, for a settings
     * model with only static methods
     * 
     * @author Kirk
     * 
     */
    static class DummyManager {

        // this class is just a placeholder for a model type with only static
        // methods

    }

    /**
     * Handler for notification that a check box was toggled controlling whether
     * or not to include one of the volume settings in the NFC or QR code output
     * 
     * @author Kirk
     * 
     */
    private class IncludeOutputCheckedChangedListener implements
            CompoundButton.OnCheckedChangeListener {

        /**
         * Handle the check box toggle event
         * 
         * @param buttonView
         *            the button that was toggled
         * 
         * @param isChecked
         *            the new state of the button
         * 
         * @see OnCheckedChangeListener#onCheckedChanged(CompoundButton,
         *      boolean)
         */
        @Override
        public void onCheckedChanged(CompoundButton buttonView,
                boolean isChecked) {

            int id = buttonView.getId();

            switch (id) {

                case R.id.include_ring_tone:

                    includeRingTone = isChecked;
                    break;

                case R.id.include_notification_tone:

                    includeNotificationTone = isChecked;
                    break;

                case R.id.include_alarm_tone:

                    includeAlarmTone = isChecked;
                    break;

                default:

                    throw new IllegalStateException("" + id); //$NON-NLS-1$

            }
        }

    }

    /**
     * Query string parameter for the alarm tone
     */
    private static final String PARAMETER_ALARM_TONE        = "at"; //$NON-NLS-1$

    /**
     * Query string parameter for the notification tone
     */
    private static final String PARAMETER_NOTIFICATION_TONE = "nt"; //$NON-NLS-1$

    /**
     * Query string parameter for the notification ringer tone
     */
    private static final String PARAMETER_RING_TONE         = "rt"; //$NON-NLS-1$

    /**
     * State variable for the "include alarm tone" setting
     */
    private boolean             includeAlarmTone;

    /**
     * State variable for the "include notification tone" setting
     */
    private boolean             includeNotificationTone;

    /**
     * State variable for the "include ringer tone" setting
     */
    private boolean             includeRingTone;

    /**
     * Initialize this instance
     * 
     * @param context
     *            the {@link Context} to which this instance belongs
     */
    RingtoneSettings(Context context) {

        super(new DummyManager(), context
                .getString(R.string.ringtone_settings_label));
        includeRingTone = false;
        includeNotificationTone = false;
        includeAlarmTone = false;

    }

    /**
     * Return the resource id of the {@link Layout} for this instance
     * 
     * @return the resource id
     * 
     * @see SettingsItem#getLayoutResource()
     */
    @Override
    public int getLayoutResource() {

        return R.layout.ringtone_settings_layout;

    }

    /**
     * Add the query string parameters for this settings object to the given
     * {@link Map}
     * 
     * @param context
     *            the {@link Context}
     * 
     * @param parameters
     *            the parameters {@link Map}
     * 
     * @see SettingsItem#addParameters(Context, Map)
     */
    @Override
    protected void addParameters(Context context, Map<String, Object> parameters) {

        if (includeRingTone) {

            Uri uri = RingtoneManager.getActualDefaultRingtoneUri(context,
                    RingtoneManager.TYPE_RINGTONE);
            parameters.put(PARAMETER_RING_TONE, uri);

        }

        if (includeNotificationTone) {

            Uri uri = RingtoneManager.getActualDefaultRingtoneUri(context,
                    RingtoneManager.TYPE_NOTIFICATION);
            parameters.put(PARAMETER_NOTIFICATION_TONE, uri);

        }

        if (includeAlarmTone) {

            Uri uri = RingtoneManager.getActualDefaultRingtoneUri(context,
                    RingtoneManager.TYPE_ALARM);
            parameters.put(PARAMETER_ALARM_TONE, uri);

        }
    }

    /**
     * Initialize the ringtones' check box widgets
     * 
     * @param context
     *            the {@link Context}
     * 
     * @param settingsView
     *            the {@link View}
     * 
     * @see SettingsItem#setView(Context, View)
     */
    @Override
    protected boolean initializeUi(Context context, View settingsView) {

        CheckBox includeAlarmToneCheckBox = (CheckBox) settingsView
                .findViewById(R.id.include_alarm_tone);
        CheckBox includeNotificationToneCheckBox = (CheckBox) settingsView
                .findViewById(R.id.include_notification_tone);
        CheckBox includeRingToneCheckBox = (CheckBox) settingsView
                .findViewById(R.id.include_ring_tone);
        includeAlarmTone = includeAlarmToneCheckBox.isChecked();
        includeNotificationTone = includeNotificationToneCheckBox.isChecked();
        includeRingTone = includeRingToneCheckBox.isChecked();
        IncludeOutputCheckedChangedListener listener = new IncludeOutputCheckedChangedListener();
        includeAlarmToneCheckBox.setOnCheckedChangeListener(listener);
        includeNotificationToneCheckBox.setOnCheckedChangeListener(listener);
        includeRingToneCheckBox.setOnCheckedChangeListener(listener);
        return true;

    }

    /**
     * Update the device ringtone settings based on the values of the given
     * {@link Uri} query string parameters
     * 
     * @param context
     *            the {@link Context}
     * 
     * @param uri
     *            the {@link Uri}
     * 
     * @see SettingsItem#updateSettings(Context, Uri)
     */
    @Override
    protected void updateSettings(Context context, Uri uri) {

        updateRingtoneUri(context, RingtoneManager.TYPE_ALARM,
                uri.getQueryParameter(PARAMETER_ALARM_TONE));
        updateRingtoneUri(context, RingtoneManager.TYPE_NOTIFICATION,
                uri.getQueryParameter(PARAMETER_NOTIFICATION_TONE));
        updateRingtoneUri(context, RingtoneManager.TYPE_RINGTONE,
                uri.getQueryParameter(PARAMETER_RING_TONE));

    }

    /**
     * Update the specified ring tone to the given URI string
     * 
     * @param context
     *            the {@link Context}
     * 
     * @param type
     *            the
     *            {@link RingtoneManager#setActualDefaultRingtoneUri(Context, int, Uri)}
     *            tone type id
     * 
     * @param value
     *            the URI string
     * 
     * @see RingtoneManager#setActualDefaultRingtoneUri(Context, int, Uri)
     */
    private void updateRingtoneUri(Context context, int type, String value) {

        if (value != null) {

            try {

                Uri uri = Uri.parse(Uri.decode(value));
                RingtoneManager.setActualDefaultRingtoneUri(context, type, uri);

            } catch (Exception e) {

                // ignore invalid URI's

            }
        }
    }

}