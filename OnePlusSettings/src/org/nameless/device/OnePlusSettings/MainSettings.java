/*
 * Copyright (C) 2016 The OmniROM Project
 * Copyright (C) 2022 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.device.OnePlusSettings;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.VibrationEffect;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragment;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceManager;

import org.nameless.device.OnePlusSettings.Doze.DozeSettingsActivity;
import org.nameless.device.OnePlusSettings.Preferences.CustomSeekBarPreference;
import org.nameless.device.OnePlusSettings.Preferences.SwitchPreference;
import org.nameless.device.OnePlusSettings.Preferences.VibratorStrengthPreference;
import org.nameless.device.OnePlusSettings.Utils.FileUtils;
import org.nameless.device.OnePlusSettings.Utils.FpsUtils;
import org.nameless.device.OnePlusSettings.Utils.HBMUtils;
import org.nameless.device.OnePlusSettings.Utils.SwitchUtils;
import org.nameless.device.OnePlusSettings.Utils.VibrationUtils;
import org.nameless.device.OnePlusSettings.Utils.VolumeUtils;

public class MainSettings extends PreferenceFragment
        implements Preference.OnPreferenceChangeListener {

    public static final String KEY_MUTE_MEDIA = "mute_media";
    public static final String KEY_AUTO_HBM_SWITCH = "auto_hbm";
    public static final String KEY_AUTO_HBM_THRESHOLD = "auto_hbm_threshold";
    public static final String KEY_HBM_SWITCH = "hbm";
    public static final String KEY_FPS_INFO = "fps_info";
    public static final String KEY_GAME_SWITCH = "game_mode";
    public static final String KEY_EDGE_TOUCH = "edge_touch";
    public static final String KEY_VIBSTRENGTH = "vib_strength";

    private static final String KEY_PREF_DOZE = "advanced_doze_settings";
    private static final String KEY_FPS_INFO_POSITION = "fps_info_position";
    private static final String KEY_FPS_INFO_COLOR = "fps_info_color";
    private static final String KEY_FPS_INFO_TEXT_SIZE = "fps_info_text_size";
    private static final String KEY_TOUCHPANEL = "touchpanel";
    private static final String KEY_EDGE_INFO = "edge_touch_info";

    private ListPreference mFpsInfoColor;
    private ListPreference mFpsInfoPosition;
    private Preference mDozeSettings;
    private Preference mEdgeTouchInfo;
    private SwitchPreference mMuteMedia;
    private SwitchPreference mAutoHBMSwitch;
    private SwitchPreference mHBMModeSwitch;
    private SwitchPreference mFpsInfo;
    private SwitchPreference mGameModeSwitch;
    private SwitchPreference mEdgeTouchSwitch;
    private CustomSeekBarPreference mFpsInfoTextSizePreference;
    private VibratorStrengthPreference mVibratorStrengthPreference;

    private ModeSwitch HBMModeSwitch;
    private ModeSwitch GameModeSwitch;
    private ModeSwitch EdgeModeSwitch;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        final Context context = getContext();
        addPreferencesFromResource(R.xml.main);

        mMuteMedia = (SwitchPreference) findPreference(KEY_MUTE_MEDIA);
        mMuteMedia.setChecked(VolumeUtils.isCurrentlyEnabled(context));
        mMuteMedia.setOnPreferenceChangeListener(this);

        mHBMModeSwitch = (SwitchPreference) findPreference(KEY_HBM_SWITCH);
        HBMModeSwitch = SwitchUtils.getHBMModeSwitch(context, mHBMModeSwitch);
        if (HBMModeSwitch.isSupported()) {
            mHBMModeSwitch.setEnabled(true);
        } else {
            mHBMModeSwitch.setEnabled(false);
            mHBMModeSwitch.setSummary(getString(R.string.unsupported_feature));
        }
        mHBMModeSwitch.setChecked(HBMModeSwitch.isCurrentlyEnabled());
        mHBMModeSwitch.setOnPreferenceChangeListener(HBMModeSwitch);

        mAutoHBMSwitch = (SwitchPreference) findPreference(KEY_AUTO_HBM_SWITCH);
        if (mHBMModeSwitch.isEnabled()) {
            mAutoHBMSwitch.setEnabled(true);
        } else {
            mAutoHBMSwitch.setEnabled(false);
            mAutoHBMSwitch.setSummary(getString(R.string.unsupported_feature));
        }
        mAutoHBMSwitch.setChecked(HBMUtils.isAutoHBMEnabled(context));
        mAutoHBMSwitch.setOnPreferenceChangeListener(this);

        mDozeSettings = (Preference) findPreference(KEY_PREF_DOZE);
        mDozeSettings.setOnPreferenceClickListener(preference -> {
            Intent intent = new Intent(getActivity().getApplicationContext(), DozeSettingsActivity.class);
            startActivity(intent);
            return true;
        });

        mFpsInfo = (SwitchPreference) findPreference(KEY_FPS_INFO);
        mFpsInfo.setChecked(FpsUtils.isFPSOverlayRunning(context));
        mFpsInfo.setOnPreferenceChangeListener(this);

        mFpsInfoPosition = (ListPreference) findPreference(KEY_FPS_INFO_POSITION);
        mFpsInfoPosition.setOnPreferenceChangeListener(this);

        mFpsInfoColor = (ListPreference) findPreference(KEY_FPS_INFO_COLOR);
        mFpsInfoColor.setOnPreferenceChangeListener(this);

        mFpsInfoTextSizePreference = (CustomSeekBarPreference) findPreference(KEY_FPS_INFO_TEXT_SIZE);
        mFpsInfoTextSizePreference.setOnPreferenceChangeListener(this);

        mGameModeSwitch = (SwitchPreference) findPreference(KEY_GAME_SWITCH);
        GameModeSwitch = SwitchUtils.getGameModeSwitch(context, mGameModeSwitch);
        if (GameModeSwitch.isSupported()) {
            mGameModeSwitch.setEnabled(true);
        } else {
            mGameModeSwitch.setEnabled(false);
            mGameModeSwitch.setSummary(getString(R.string.unsupported_feature));
        }
        mGameModeSwitch.setChecked(GameModeSwitch.isCurrentlyEnabled());
        mGameModeSwitch.setOnPreferenceChangeListener(GameModeSwitch);

        mEdgeTouchSwitch = (SwitchPreference) findPreference(KEY_EDGE_TOUCH);
        EdgeModeSwitch = SwitchUtils.getEdgeModeSwitch(context, mEdgeTouchSwitch);
        mEdgeTouchInfo = (Preference) findPreference(KEY_EDGE_INFO);
        if (SwitchUtils.isEdgeTouchSupported()) {
            if (EdgeModeSwitch.isSupported()) {
                mEdgeTouchSwitch.setEnabled(true);
            } else {
                mEdgeTouchSwitch.setEnabled(false);
                mEdgeTouchSwitch.setSummary(getString(R.string.unsupported_feature));
            }
            mEdgeTouchSwitch.setChecked(EdgeModeSwitch.isCurrentlyEnabled());
            mEdgeTouchSwitch.setOnPreferenceChangeListener(EdgeModeSwitch);
        } else {
            ((PreferenceGroup) findPreference(KEY_TOUCHPANEL)).removePreference(findPreference(KEY_EDGE_TOUCH));
            ((PreferenceGroup) findPreference(KEY_TOUCHPANEL)).removePreference(findPreference(KEY_EDGE_INFO));
        }

        mVibratorStrengthPreference =  (VibratorStrengthPreference) findPreference(KEY_VIBSTRENGTH);
        if (FileUtils.isFileWritable(VibrationUtils.FILE_LEVEL)) {
            mVibratorStrengthPreference.setValue(PreferenceManager.getDefaultSharedPreferences(context).
                    getInt(KEY_VIBSTRENGTH, VibrationUtils.getVibStrength()));
            mVibratorStrengthPreference.setOnPreferenceChangeListener(this);
        } else {
            mVibratorStrengthPreference.setEnabled(false);
            mVibratorStrengthPreference.setSummary(getString(R.string.unsupported_feature));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mHBMModeSwitch.setChecked(HBMModeSwitch.isCurrentlyEnabled());
        mFpsInfo.setChecked(FpsUtils.isFPSOverlayRunning(getContext()));
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        final Context context = getContext();
        if (preference == mMuteMedia) {
            boolean enabled = (Boolean) newValue;
            VolumeUtils.setEnabled(context, enabled);
        } else if (preference == mAutoHBMSwitch) {
            boolean enabled = (Boolean) newValue;
            HBMUtils.setAutoHBMEnabled(context, enabled);
            HBMUtils.enableService(context);
        } else if (preference == mFpsInfo) {
            boolean enabled = (Boolean) newValue;
            FpsUtils.setFpsService(context, enabled);
        } else if (preference == mFpsInfoPosition) {
            int position = Integer.parseInt(newValue.toString());
            if (FpsUtils.isPositionChanged(context, position)) {
                FpsUtils.setPosition(context, position);
                FpsUtils.notifySettingsUpdated(context);
            }
        } else if (preference == mFpsInfoColor) {
            int color = Integer.parseInt(newValue.toString());
            if (FpsUtils.isColorChanged(context, color)) {
                FpsUtils.setColorIndex(context, color);
                FpsUtils.notifySettingsUpdated(context);
            }
        } else if (preference == mFpsInfoTextSizePreference) {
            int size = Integer.parseInt(newValue.toString());
            if (FpsUtils.isSizeChanged(context, size - 1)) {
                FpsUtils.setSizeIndex(context, size - 1);
                FpsUtils.notifySettingsUpdated(context);
            }
        } else if (preference == mVibratorStrengthPreference) {
            int value = Integer.parseInt(newValue.toString());
            PreferenceManager.getDefaultSharedPreferences(context).edit().
                    putInt(KEY_VIBSTRENGTH, value).commit();
            VibrationUtils.setVibStrength(context, value);
            VibrationUtils.doHapticFeedbackForEffect(context, VibrationEffect.EFFECT_CLICK, true);
        }
        return true;
    }
}
