package com.android.settings.morningstar;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.Settings;

import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.AnimationSeekBar;
import com.android.settings.R;

import com.android.internal.logging.MetricsLogger;
import com.android.internal.util.AnimationHelper;

import java.util.Arrays;

public class SystemAnimation extends SettingsPreferenceFragment implements OnPreferenceChangeListener {

    private static final String ACTIVITY_OPEN = "activity_open";
    private static final String ACTIVITY_CLOSE = "activity_close";
    private static final String TASK_OPEN = "task_open";
    private static final String TASK_CLOSE = "task_close";
    private static final String TASK_OPEN_BEHIND = "task_open_behind";
    private static final String TASK_MOVE_TO_FRONT = "task_move_to_front";
    private static final String TASK_MOVE_TO_BACK = "task_move_to_back";
    private static final String ANIMATION_DURATION = "animation_duration";
    private static final String ANIMATION_NO_OVERRIDE = "animation_no_override";
    private static final String WALLPAPER_OPEN = "wallpaper_open";
    private static final String WALLPAPER_CLOSE = "wallpaper_close";
    private static final String WALLPAPER_INTRA_OPEN = "wallpaper_intra_open";
    private static final String WALLPAPER_INTRA_CLOSE = "wallpaper_intra_close";
    private static final String ANIMATION_EXIT_ONLY = "animation_exit_only";
    private static final String ANIMATION_REVERSE_EXIT = "animation_reverse_exit";

    ListPreference mActivityOpenPref;
    ListPreference mActivityClosePref;
    ListPreference mTaskOpenPref;
    ListPreference mTaskClosePref;
    ListPreference mTaskOpenBehind;
    ListPreference mTaskMoveToFrontPref;
    ListPreference mTaskMoveToBackPref;
    ListPreference mWallpaperOpen;
    ListPreference mWallpaperClose;
    ListPreference mWallpaperIntraOpen;
    ListPreference mWallpaperIntraClose;
    AnimationSeekBar mAnimationDuration;
    SwitchPreference mAnimNoOverride;
    SwitchPreference mAnimExitOnly;
    SwitchPreference mAnimReverseOnly;

    private int[] mAnimations;
    private String[] mAnimationsStrings;
    private String[] mAnimationsNum;

    @Override
    protected int getMetricsCategory() {
        return MetricsLogger.DEVELOPMENT;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.system_animation_title);
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.system_animations);

        PreferenceScreen prefs = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();
        mAnimations = AnimationHelper.getAnimationsList();
        int animqty = mAnimations.length;
        mAnimationsStrings = new String[animqty];
        mAnimationsNum = new String[animqty];
        for (int i = 0; i < animqty; i++) {
            mAnimationsStrings[i] = AnimationHelper.getProperName(getActivity(), mAnimations[i]);
            mAnimationsNum[i] = String.valueOf(mAnimations[i]);
        }

        mActivityOpenPref = (ListPreference) prefs.findPreference(ACTIVITY_OPEN);
        mActivityOpenPref.setOnPreferenceChangeListener(this);
        mActivityOpenPref.setSummary(getProperSummary(mActivityOpenPref));
        mActivityOpenPref.setEntries(mAnimationsStrings);
        mActivityOpenPref.setEntryValues(mAnimationsNum);

        mActivityClosePref = (ListPreference) prefs.findPreference(ACTIVITY_CLOSE);
        mActivityClosePref.setOnPreferenceChangeListener(this);
        mActivityClosePref.setSummary(getProperSummary(mActivityClosePref));
        mActivityClosePref.setEntries(mAnimationsStrings);
        mActivityClosePref.setEntryValues(mAnimationsNum);

        mTaskOpenPref = (ListPreference) prefs.findPreference(TASK_OPEN);
        mTaskOpenPref.setOnPreferenceChangeListener(this);
        mTaskOpenPref.setSummary(getProperSummary(mTaskOpenPref));
        mTaskOpenPref.setEntries(mAnimationsStrings);
        mTaskOpenPref.setEntryValues(mAnimationsNum);

        mTaskClosePref = (ListPreference) prefs.findPreference(TASK_CLOSE);
        mTaskClosePref.setOnPreferenceChangeListener(this);
        mTaskClosePref.setSummary(getProperSummary(mTaskClosePref));
        mTaskClosePref.setEntries(mAnimationsStrings);
        mTaskClosePref.setEntryValues(mAnimationsNum);

        mTaskOpenBehind = (ListPreference) prefs.findPreference(TASK_OPEN_BEHIND);
        mTaskOpenBehind.setOnPreferenceChangeListener(this);
        mTaskOpenBehind.setSummary(getProperSummary(mTaskOpenBehind));
        mTaskOpenBehind.setEntries(mAnimationsStrings);
        mTaskOpenBehind.setEntryValues(mAnimationsNum);

        mTaskMoveToFrontPref = (ListPreference) prefs.findPreference(TASK_MOVE_TO_FRONT);
        mTaskMoveToFrontPref.setOnPreferenceChangeListener(this);
        mTaskMoveToFrontPref.setSummary(getProperSummary(mTaskMoveToFrontPref));
        mTaskMoveToFrontPref.setEntries(mAnimationsStrings);
        mTaskMoveToFrontPref.setEntryValues(mAnimationsNum);

        mTaskMoveToBackPref = (ListPreference) prefs.findPreference(TASK_MOVE_TO_BACK);
        mTaskMoveToBackPref.setOnPreferenceChangeListener(this);
        mTaskMoveToBackPref.setSummary(getProperSummary(mTaskMoveToBackPref));
        mTaskMoveToBackPref.setEntries(mAnimationsStrings);
        mTaskMoveToBackPref.setEntryValues(mAnimationsNum);

        mWallpaperOpen = (ListPreference) prefs.findPreference(WALLPAPER_OPEN);
        mWallpaperOpen.setOnPreferenceChangeListener(this);
        mWallpaperOpen.setSummary(getProperSummary(mWallpaperOpen));
        mWallpaperOpen.setEntries(mAnimationsStrings);
        mWallpaperOpen.setEntryValues(mAnimationsNum);

        mWallpaperClose = (ListPreference) prefs.findPreference(WALLPAPER_CLOSE);
        mWallpaperClose.setOnPreferenceChangeListener(this);
        mWallpaperClose.setSummary(getProperSummary(mWallpaperClose));
        mWallpaperClose.setEntries(mAnimationsStrings);
        mWallpaperClose.setEntryValues(mAnimationsNum);

        mWallpaperIntraOpen = (ListPreference) prefs.findPreference(WALLPAPER_INTRA_OPEN);
        mWallpaperIntraOpen.setOnPreferenceChangeListener(this);
        mWallpaperIntraOpen.setSummary(getProperSummary(mWallpaperIntraOpen));
        mWallpaperIntraOpen.setEntries(mAnimationsStrings);
        mWallpaperIntraOpen.setEntryValues(mAnimationsNum);

        mWallpaperIntraClose = (ListPreference) prefs.findPreference(WALLPAPER_INTRA_CLOSE);
        mWallpaperIntraClose.setOnPreferenceChangeListener(this);
        mWallpaperIntraClose.setSummary(getProperSummary(mWallpaperIntraClose));
        mWallpaperIntraClose.setEntries(mAnimationsStrings);
        mWallpaperIntraClose.setEntryValues(mAnimationsNum);

        int defaultDuration = Settings.System.getInt(resolver,
                Settings.System.ACTIVITY_ANIMATION_DURATION, 0);
        mAnimationDuration = (AnimationSeekBar) prefs.findPreference(ANIMATION_DURATION);
        mAnimationDuration.setInitValue((int) (defaultDuration));
        mAnimationDuration.setOnPreferenceChangeListener(this);

        mAnimNoOverride = (SwitchPreference) prefs.findPreference(ANIMATION_NO_OVERRIDE);
        mAnimNoOverride.setChecked((Settings.System.getInt(resolver,
                Settings.System.ANIMATION_NO_OVERRIDE, 0) == 1));
        mAnimNoOverride.setOnPreferenceChangeListener(this);

        mAnimExitOnly = (SwitchPreference) prefs.findPreference(ANIMATION_EXIT_ONLY);
        mAnimExitOnly.setChecked((Settings.System.getInt(resolver,
                Settings.System.ANIMATION_EXIT_ONLY, 1) == 1));
        mAnimExitOnly.setOnPreferenceChangeListener(this);

        mAnimReverseOnly = (SwitchPreference) prefs.findPreference(ANIMATION_REVERSE_EXIT);
        mAnimReverseOnly.setChecked((Settings.System.getInt(resolver,
                Settings.System.ANIMATION_REVERSE_EXIT, 0) == 1));
        mAnimReverseOnly.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        boolean result = false;
        if (preference == mActivityOpenPref) {
            int val = Integer.parseInt((String) newValue);
            result = Settings.System.putInt(resolver,
                    Settings.System.ACTIVITY_ANIMATIONS[0], val);
        } else if (preference == mActivityClosePref) {
            int val = Integer.parseInt((String) newValue);
            result = Settings.System.putInt(resolver,
                    Settings.System.ACTIVITY_ANIMATIONS[1], val);
        } else if (preference == mTaskOpenPref) {
            int val = Integer.parseInt((String) newValue);
            result = Settings.System.putInt(resolver,
                    Settings.System.ACTIVITY_ANIMATIONS[2], val);
        } else if (preference == mTaskClosePref) {
            int val = Integer.parseInt((String) newValue);
            result = Settings.System.putInt(resolver,
                    Settings.System.ACTIVITY_ANIMATIONS[3], val);
        } else if (preference == mTaskOpenBehind) {
            int val = Integer.parseInt((String) newValue);
            result = Settings.System.putInt(resolver,
                    Settings.System.ACTIVITY_ANIMATIONS[10], val);
        } else if (preference == mTaskMoveToFrontPref) {
            int val = Integer.parseInt((String) newValue);
            result = Settings.System.putInt(resolver,
                    Settings.System.ACTIVITY_ANIMATIONS[4], val);
        } else if (preference == mTaskMoveToBackPref) {
            int val = Integer.parseInt((String) newValue);
            result = Settings.System.putInt(resolver,
                    Settings.System.ACTIVITY_ANIMATIONS[5], val);
        } else if (preference == mWallpaperOpen) {
            int val = Integer.parseInt((String) newValue);
            result = Settings.System.putInt(resolver,
                    Settings.System.ACTIVITY_ANIMATIONS[6], val);
        } else if (preference == mWallpaperClose) {
            int val = Integer.parseInt((String) newValue);
            result = Settings.System.putInt(resolver,
                    Settings.System.ACTIVITY_ANIMATIONS[7], val);
        } else if (preference == mWallpaperIntraOpen) {
            int val = Integer.parseInt((String) newValue);
            result = Settings.System.putInt(resolver,
                    Settings.System.ACTIVITY_ANIMATIONS[8], val);
        } else if (preference == mWallpaperIntraClose) {
            int val = Integer.parseInt((String) newValue);
            result = Settings.System.putInt(resolver,
                    Settings.System.ACTIVITY_ANIMATIONS[9], val);
         } else if (preference == mTaskOpenBehind) {
            int val = Integer.parseInt((String) newValue);
            result = Settings.System.putInt(resolver,
                    Settings.System.ACTIVITY_ANIMATIONS[10], val);
        } else if (preference == mAnimationDuration) {
            int val = Integer.parseInt((String) newValue);
            result = Settings.System.putInt(resolver,
                    Settings.System.ACTIVITY_ANIMATION_DURATION, val);
        } else if (preference == mAnimNoOverride) {
            boolean value = (Boolean) newValue;
            result = Settings.System.putInt(resolver, Settings.System.ANIMATION_NO_OVERRIDE, value ? 1 : 0);
        } else if (preference == mAnimExitOnly) {
            boolean value = (Boolean) newValue;
            result = Settings.System.putInt(resolver, Settings.System.ANIMATION_EXIT_ONLY, value ? 1 : 0);
        } else if (preference == mAnimReverseOnly) {
            boolean value = (Boolean) newValue;
            result = Settings.System.putInt(resolver, Settings.System.ANIMATION_REVERSE_EXIT, value ? 1 : 0);
        }
        preference.setSummary(getProperSummary(preference));
        return result;
    }

    private String getProperSummary(Preference preference) {
        String mString = "";
        if (preference == mActivityOpenPref) {
            mString = Settings.System.ACTIVITY_ANIMATIONS[0];
        } else if (preference == mActivityClosePref) {
            mString = Settings.System.ACTIVITY_ANIMATIONS[1];
        } else if (preference == mTaskOpenPref) {
            mString = Settings.System.ACTIVITY_ANIMATIONS[2];
        } else if (preference == mTaskClosePref) {
            mString = Settings.System.ACTIVITY_ANIMATIONS[3];
        } else if (preference == mTaskOpenBehind) {
            mString = Settings.System.ACTIVITY_ANIMATIONS[10];
        } else if (preference == mTaskMoveToFrontPref) {
            mString = Settings.System.ACTIVITY_ANIMATIONS[4];
        } else if (preference == mTaskMoveToBackPref) {
            mString = Settings.System.ACTIVITY_ANIMATIONS[5];
        } else if (preference == mWallpaperOpen) {
            mString = Settings.System.ACTIVITY_ANIMATIONS[6];
        } else if (preference == mWallpaperClose) {
            mString = Settings.System.ACTIVITY_ANIMATIONS[7];
        } else if (preference == mWallpaperIntraOpen) {
            mString = Settings.System.ACTIVITY_ANIMATIONS[8];
        } else if (preference == mWallpaperIntraClose) {
            mString = Settings.System.ACTIVITY_ANIMATIONS[9];
        }

        int mNum = Settings.System.getInt(getActivity().getContentResolver(), mString, 0);
        return mAnimationsStrings[mNum];
    }
}
