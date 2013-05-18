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

import java.util.Map;

import us.rader.tapset.R;
import android.content.Context;
import android.media.AudioManager;
import android.net.Uri;
import android.text.Layout;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.SeekBar;

/**
 * {@link Item} for audio volumes (ringer volume, notification volume,
 * vibration etc.)
 * 
 * @author Kirk
 * 
 */
final class VolumeSettings extends Item<AudioManager> {

    /**
     * Set {@link VolumeSettings#includeOutput} when the UI check box state
     * changes
     * 
     * @see VolumeSettings#includeOutput
     * 
     * @author Kirk
     * 
     */
    private class IncludeOutputListener implements
            CompoundButton.OnCheckedChangeListener {

        /**
         * Set {@link VolumeSettings#includeOutput} to value of
         * <code>isChecked</code>
         * 
         * @param buttonView
         *            ignored
         * 
         * @param isChecked
         *            new state for {@link VolumeSettings#includeOutput}
         */
        @Override
        public void onCheckedChanged(CompoundButton buttonView,
                boolean isChecked) {

            includeOutput = isChecked;

        }

    }

    /**
     * Update the corresponding [@link AudioManager} setting when a radio button
     * is clicked
     * 
     * @author Kirk
     * 
     */
    private class RingerModeListener implements View.OnClickListener {

        /**
         * Invoke {@link AudioManager#setRingerMode(int)} with the value
         * corresponding to the specified radio button's id
         * 
         * @param button
         *            the radio button that was clicked
         */
        @Override
        public void onClick(View button) {

            int id = button.getId();
            AudioManager audioManager = getModel();

            switch (id) {

                case R.id.ringer_mode_normal:

                    audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                    break;

                case R.id.ringer_mode_vibrate:

                    audioManager
                            .setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
                    break;

                case R.id.ringer_mode_silent:

                    audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                    break;

                default:

                    throw new IllegalStateException(
                            "unsupported ringer mode radio button id " + id); //$NON-NLS-1$

            }

            updateUI();

        }

    }

    /**
     * Update the corresponding {@link AudioManager} properties when a volume
     * slider's state changes in the UI
     * 
     * @author Kirk
     * 
     */
    private final class VolumeSliderListener implements
            SeekBar.OnSeekBarChangeListener {

        /**
         * Update the corresponding {@link AudioManager} setting based on the
         * slider's id
         * 
         * @param seekBar
         *            the {@link SeekBar} whose state changed
         * 
         * @param progress
         *            the new state of the {@link SeekBar}
         * 
         * @param fromUser
         *            ignored
         * 
         */
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress,
                boolean fromUser) {

            int stream;
            int id = seekBar.getId();

            switch (id) {

                case R.id.alarm_volume_slider:

                    stream = AudioManager.STREAM_ALARM;
                    break;

                case R.id.media_volume_slider:

                    stream = AudioManager.STREAM_MUSIC;
                    break;

                case R.id.ringer_volume_slider:

                    stream = AudioManager.STREAM_RING;
                    break;

                default:

                    throw new IllegalStateException(
                            "unsupported volume slider id " + id); //$NON-NLS-1$

            }

            getModel().setStreamVolume(
                    stream,
                    seekBar.getProgress(),
                    AudioManager.FLAG_ALLOW_RINGER_MODES
                            | AudioManager.FLAG_PLAY_SOUND
                            | AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE
                            | AudioManager.FLAG_VIBRATE);
            updateUI();

        }

        /**
         * Required by interface but not used by this implementation
         * 
         * @param seekBar
         *            ignored
         * 
         */
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

            // nothing to do for this event

        }

        /**
         * Required by interface but not used by this implementation
         * 
         * @param seekBar
         *            ignored
         * 
         */
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

            // nothing to do for this event

        }
    }

    /**
     * Query string parameter for alarm volume
     * 
     * @see VolumeSliderListener
     * @see AudioManager#setStreamVolume(int, int, int)
     */
    private static final String PARAMETER_ALARM_VOLUME  = "av"; //$NON-NLS-1$

    /**
     * Query string parameter for media volume
     * 
     * @see VolumeSliderListener
     * @see AudioManager#setStreamVolume(int, int, int)
     */
    private static final String PARAMETER_MEDIA_VOLUME  = "mv"; //$NON-NLS-1$

    /**
     * Query string parameter for ringer mode (i.e. normal, vibrate or silent)
     * 
     * @see RingerModeListener
     * @see AudioManager#setRingerMode(int)
     */
    private static final String PARAMETER_RINGER_MODE   = "rm"; //$NON-NLS-1$

    /**
     * Query string parameter for ringer / notification volume
     * 
     * @see VolumeSliderListener
     * @see AudioManager#setStreamVolume(int, int, int)
     */
    private static final String PARAMETER_RINGER_VOLUME = "rv"; //$NON-NLS-1$

    /**
     * {@link SeekBar} providing the UI for setting the alarm volume
     * 
     * @see VolumeSliderListener
     * @see AudioManager#setStreamVolume(int, int, int)
     */
    private SeekBar             alarmSlider;

    /**
     * Value controlling whether or not to include this instance's settings when
     * populating the {@link Uri} query string parameters when writing to a NFC
     * tag or QR code
     * 
     * @see #addParameters(Context, Map)
     */
    private boolean             includeOutput;

    /**
     * {@link CheckBox} that provides UI for {@link #includeOutput}
     */
    private CheckBox            includeOutputCheckBox;

    /**
     * {@link SeekBar} providing the UI for setting the media volume
     * 
     * @see VolumeSliderListener
     * @see AudioManager#setStreamVolume(int, int, int)
     */
    private SeekBar             mediaSlider;

    /**
     * {@link RadioButton} providing part of the UI for selecting the ringer
     * mode
     * 
     * @see RingerModeListener
     * @see AudioManager#setRingerMode(int)
     */
    private RadioButton         ringerNormalButton;

    /**
     * {@link RadioButton} providing part of the UI for selecting the ringer
     * mode
     * 
     * @see RingerModeListener
     * @see AudioManager#setRingerMode(int)
     */
    private RadioButton         ringerSilentButton;

    /**
     * {@link SeekBar} providing the UI for setting the ringer / notification
     * volume
     * 
     * @see VolumeSliderListener
     * @see AudioManager#setStreamVolume(int, int, int)
     */
    private SeekBar             ringerSlider;

    /**
     * {@link RadioButton} providing part of the UI for selecting the ringer
     * mode
     * 
     * @see RingerModeListener
     * @see AudioManager#setRingerMode(int)
     */
    private RadioButton         ringerVibrateButton;

    /**
     * Initialize {@link Item} and {@link #includeOutput}
     * 
     * @param context
     *            the {@link Context}
     */
    VolumeSettings(Context context) {

        super((AudioManager) context.getSystemService(Context.AUDIO_SERVICE),
                context.getString(R.string.volume_settings_label));
        // TODO: currently, volume settings are included by default; need to
        // support persisting choices for things like whether or not to
        // include specific settings
        includeOutput = true;

    }

    /**
     * Return the {@link Layout} resource id for this instance
     * 
     * @return volume settings layout resource id
     * 
     * @see Item#getLayoutResource()
     */
    @Override
    public final int getLayoutResource() {

        return R.layout.volume_settings_layout;

    }

    /**
     * Add the query string parameters for this instance to the given
     * {@link Map}
     * 
     * Note that this won't add anything to the {@link Map} if
     * {@link #includeOutput} is <code>false</code>
     * 
     * @param context
     *            the {@link Context}
     * 
     * @param parameters
     *            the query string parameter {@link Map}
     * 
     * @see #includeOutput
     * @see Item#addParameters(Context, Map)
     */
    @Override
    protected final void addParameters(Context context,
            Map<String, Object> parameters) {

        if (includeOutput) {

            AudioManager audioManager = getModel();
            parameters.put(PARAMETER_ALARM_VOLUME,
                    audioManager.getStreamVolume(AudioManager.STREAM_ALARM));
            parameters.put(PARAMETER_MEDIA_VOLUME,
                    audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
            parameters.put(PARAMETER_RINGER_VOLUME,
                    audioManager.getStreamVolume(AudioManager.STREAM_RING));
            parameters.put(PARAMETER_RINGER_MODE, audioManager.getRingerMode());

        }
    }

    /**
     * Initialize the UI event handlers, cached state etc. for this instance
     * 
     * @param context
     *            the {@link Context}
     * 
     * @param settingsView
     *            the settings UI {@link View}
     * 
     * @return <code>true</code> if and only if the UI was successfully
     *         initialized
     * 
     * @see Item#initializeUi(Context, View)
     */
    @Override
    protected final boolean initializeUi(Context context, View settingsView) {

        ringerSlider = (SeekBar) settingsView
                .findViewById(R.id.ringer_volume_slider);
        mediaSlider = (SeekBar) settingsView
                .findViewById(R.id.media_volume_slider);
        alarmSlider = (SeekBar) settingsView
                .findViewById(R.id.alarm_volume_slider);
        ringerNormalButton = (RadioButton) settingsView
                .findViewById(R.id.ringer_mode_normal);
        ringerVibrateButton = (RadioButton) settingsView
                .findViewById(R.id.ringer_mode_vibrate);
        ringerSilentButton = (RadioButton) settingsView
                .findViewById(R.id.ringer_mode_silent);
        includeOutputCheckBox = (CheckBox) settingsView
                .findViewById(R.id.include_output);
        setUiInitialize(true);
        updateUI();
        VolumeSliderListener volumeSliderListener = new VolumeSliderListener();
        ringerSlider.setOnSeekBarChangeListener(volumeSliderListener);
        mediaSlider.setOnSeekBarChangeListener(volumeSliderListener);
        alarmSlider.setOnSeekBarChangeListener(volumeSliderListener);
        RingerModeListener ringerModeListener = new RingerModeListener();
        ringerNormalButton.setOnClickListener(ringerModeListener);
        ringerVibrateButton.setOnClickListener(ringerModeListener);
        ringerSilentButton.setOnClickListener(ringerModeListener);
        IncludeOutputListener includeOutputListener = new IncludeOutputListener();
        includeOutputCheckBox.setOnCheckedChangeListener(includeOutputListener);
        return true;

    }

    /**
     * Update the audio volume settings based on the relevant query string
     * parameters in the given {@link Uri}
     * 
     * @param context
     *            the {@link Context}
     * 
     * @param uri
     *            the {@link Uri} read from a NFC tag, QR code etc.
     * 
     * @see Item#updateSettings(Context, Uri)
     */
    @Override
    protected final void updateSettings(Context context, Uri uri) {

        AudioManager audioManager = getModel();
        int flags = AudioManager.FLAG_ALLOW_RINGER_MODES
                | AudioManager.FLAG_VIBRATE
                | AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE;
        String value;
        value = uri.getQueryParameter(PARAMETER_ALARM_VOLUME);

        if (value != null) {

            audioManager.setStreamVolume(AudioManager.STREAM_ALARM,
                    Integer.parseInt(value), flags);

        }

        value = uri.getQueryParameter(PARAMETER_MEDIA_VOLUME);

        if (value != null) {

            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                    Integer.parseInt(value), flags);

        }

        value = uri.getQueryParameter(PARAMETER_RINGER_VOLUME);

        if (value != null) {

            audioManager.setStreamVolume(AudioManager.STREAM_RING,
                    Integer.parseInt(value), flags);

        }

        value = uri.getQueryParameter(PARAMETER_RINGER_MODE);

        if (value != null) {

            try {

                audioManager.setRingerMode(Integer.parseInt(value));

            } catch (NumberFormatException e) {

                // ignore ill-formatted values

            }

        }

        updateUI();

    }

    /**
     * Update the volume sliders and ringer mode radio buttons in the UI to
     * match the current state of the corresponding properties of the given
     * {@link AudioManager}
     */
    private final void updateUI() {

        if (isUiInitialized()) {

            AudioManager audioManager = getModel();
            int mode = audioManager.getRingerMode();
            boolean enableSliders = (mode == AudioManager.RINGER_MODE_NORMAL);

            switch (mode) {

                case AudioManager.RINGER_MODE_NORMAL:

                    ringerNormalButton.setChecked(true);
                    break;

                case AudioManager.RINGER_MODE_VIBRATE:

                    ringerVibrateButton.setChecked(true);
                    break;

                case AudioManager.RINGER_MODE_SILENT:

                    ringerSilentButton.setChecked(true);
                    break;

                default:

                    // ignore unsupported values
                    break;

            }

            ringerSlider.setMax(audioManager
                    .getStreamMaxVolume(AudioManager.STREAM_RING));
            ringerSlider.setProgress(audioManager
                    .getStreamVolume(AudioManager.STREAM_RING));
            ringerSlider.setEnabled(enableSliders);
            mediaSlider.setMax(audioManager
                    .getStreamMaxVolume(AudioManager.STREAM_MUSIC));
            mediaSlider.setProgress(audioManager
                    .getStreamVolume(AudioManager.STREAM_MUSIC));
            mediaSlider.setEnabled(enableSliders);
            alarmSlider.setMax(audioManager
                    .getStreamMaxVolume(AudioManager.STREAM_ALARM));
            alarmSlider.setProgress(audioManager
                    .getStreamVolume(AudioManager.STREAM_ALARM));
            alarmSlider.setEnabled(enableSliders);

        }
    }
}
