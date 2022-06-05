/*
 * Copyright (C) 2018 The LineageOS Project
 * Copyright (C) 2022 The Nameless-AOSP Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.nameless.device.OnePlusSettings.Services;

import android.content.Context;
import android.hardware.input.InputManager;
import android.media.AudioManager;
import android.os.VibrationEffect;
import android.view.KeyEvent;

import androidx.annotation.Keep;

import com.android.internal.os.DeviceKeyHandler;

import org.nameless.device.OnePlusSettings.Utils.FileUtils;
import org.nameless.device.OnePlusSettings.Utils.VibrationUtils;
import org.nameless.device.OnePlusSettings.Utils.VolumeUtils;

@Keep
public class KeyHandler implements DeviceKeyHandler {

    // Slider key codes
    private static final String MODE_NORMAL = "3";
    private static final String MODE_VIBRATION = "2";
    private static final String MODE_SILENCE = "1";

    private final Context mContext;
    private final AudioManager mAudioManager;
    private final InputManager mInputManager;

    private String lastCode;

    public KeyHandler(Context context) {
        mContext = context;

        mAudioManager = mContext.getSystemService(AudioManager.class);
        mInputManager = mContext.getSystemService(InputManager.class);

        lastCode = MODE_NORMAL;
    }

    public KeyEvent handleKeyEvent(KeyEvent event) {
        if (event.getAction() != KeyEvent.ACTION_DOWN) {
            return event;
        }

        if (!mInputManager.getInputDevice(event.getDeviceId()).getName().equals("oplus,hall_tri_state_key")) {
            return event;
        }

        final String scanCode = FileUtils.readOneLine("/proc/tristatekey/tri_state").trim();

        switch (scanCode) {
            case MODE_NORMAL:
                mAudioManager.setRingerModeInternal(AudioManager.RINGER_MODE_NORMAL);
                VibrationUtils.doHapticFeedback(mContext, VibrationEffect.EFFECT_HEAVY_CLICK, true);
                if (lastCode.equals(MODE_SILENCE)) VolumeUtils.changeMediaVolume(mAudioManager, mContext);
                break;
            case MODE_VIBRATION:
                mAudioManager.setRingerModeInternal(AudioManager.RINGER_MODE_VIBRATE);
                VibrationUtils.doHapticFeedback(mContext, VibrationEffect.EFFECT_DOUBLE_CLICK, true);
                if (lastCode.equals(MODE_SILENCE)) VolumeUtils.changeMediaVolume(mAudioManager, mContext);
                break;
            case MODE_SILENCE:
                mAudioManager.setRingerModeInternal(AudioManager.RINGER_MODE_SILENT);
                VolumeUtils.changeMediaVolume(mAudioManager, mContext);
                break;
            default:
                return event;
        }

        lastCode = scanCode;

        return null;
    }
}
