/*
 * Copyright (C) 2016 Morningstar
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

package com.android.settings.morningstar;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.UserHandle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.internal.logging.MetricsLogger;

import com.android.settings.R;
import com.android.settings.morningstar.AbstractAsyncSuCMDProcessor;
import com.android.settings.morningstar.ExposedAnimationDrawable;
import com.android.settings.SettingsPreferenceFragment;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ExtraSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String TAG = "ExtraSettings";

    private static final String KEY_CATEGORY_ANIMATION = "animation";
    private static final String KEY_CATEGORY_RECENTS = "recents";
    private static final String KEY_CATEGORY_SYSTEM = "system";
    private static final String KEY_CATEGORY_STATUSBAR = "statusbar";
    private static final String KEY_TOAST_ANIMATION = "toast_animation";
    private static final String KEY_LISTVIEW_ANIMATION = "listview_animation";
    private static final String KEY_LISTVIEW_INTERPOLATOR = "listview_interpolator";
    private static final String KEY_BOOT_ANIMATION = "boot_animation";
    private static final String KEY_SHOW_CLEAR_ALL_RECENTS = "show_clear_all_recents";
    private static final String KEY_RECENTS_CLEAR_ALL_LOCATION = "recents_clear_all_location";
    private static final String systemPath = "/system/media/bootanimation.zip";

    private static final int bootAniRequest = 201;

    private ListPreference mToastAnimation;
    private ListPreference mListViewAnimation;
    private ListPreference mListViewInterpolator;
    private Preference mBootAnimation;
    private SwitchPreference mRecentsClearAll;
    private ListPreference mRecentsClearAllLocation;

    private AlertDialog bootAnimationDialog;
    private String bootAnimationPath;
    private ImageView preview;
    private TextView error;
    private String errorMessage;
    private ExposedAnimationDrawable part1;
    private ExposedAnimationDrawable part2;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Activity activity = getActivity();
        final ContentResolver resolver = activity.getContentResolver();
        addPreferencesFromResource(R.xml.extras);

        PreferenceCategory animationPrefs = (PreferenceCategory)
                findPreference(KEY_CATEGORY_ANIMATION);
        PreferenceCategory recentsPrefs = (PreferenceCategory)
                findPreference(KEY_CATEGORY_RECENTS);
        PreferenceCategory systemPrefs = (PreferenceCategory)
                findPreference(KEY_CATEGORY_SYSTEM);
        PreferenceCategory statusbarPrefs = (PreferenceCategory)
                findPreference(KEY_CATEGORY_STATUSBAR);
        

        mToastAnimation = (ListPreference) findPreference(KEY_TOAST_ANIMATION);
        mToastAnimation.setSummary(mToastAnimation.getEntry());
        int currentToastAnimation = Settings.System.getInt(getContentResolver(), Settings.System.TOAST_ANIMATION, 1);
        mToastAnimation.setValueIndex(currentToastAnimation);
        mToastAnimation.setOnPreferenceChangeListener(this);

        mListViewAnimation = (ListPreference) findPreference(KEY_LISTVIEW_ANIMATION);
        int listviewanimation = Settings.System.getInt(getContentResolver(),
                Settings.System.LISTVIEW_ANIMATION, 0);
        mListViewAnimation.setValue(String.valueOf(listviewanimation));
        mListViewAnimation.setSummary(mListViewAnimation.getEntry());
        mListViewAnimation.setOnPreferenceChangeListener(this);

        mListViewInterpolator = (ListPreference) findPreference(KEY_LISTVIEW_INTERPOLATOR);
        int listviewinterpolator = Settings.System.getInt(getContentResolver(),
                Settings.System.LISTVIEW_INTERPOLATOR, 0);
        mListViewInterpolator.setValue(String.valueOf(listviewinterpolator));
        mListViewInterpolator.setSummary(mListViewInterpolator.getEntry());
        mListViewInterpolator.setOnPreferenceChangeListener(this);
        mListViewInterpolator.setEnabled(listviewanimation > 0);

        mBootAnimation = findPreference(KEY_BOOT_ANIMATION);

        mRecentsClearAll = (SwitchPreference) findPreference(KEY_SHOW_CLEAR_ALL_RECENTS);

        mRecentsClearAllLocation = (ListPreference) findPreference(KEY_RECENTS_CLEAR_ALL_LOCATION);
        int location = Settings.System.getIntForUser(resolver,
                Settings.System.RECENTS_CLEAR_ALL_LOCATION, 3, UserHandle.USER_CURRENT);
        mRecentsClearAllLocation.setValue(String.valueOf(location));
        mRecentsClearAllLocation.setSummary(mRecentsClearAllLocation.getEntry());
        mRecentsClearAllLocation.setOnPreferenceChangeListener(this);

        resetAnimation();
    }

    @Override
    public void onResume() {
        super.onResume();
        final ContentResolver resolver = getContentResolver();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    protected int getMetricsCategory() {
        return MetricsLogger.DONT_TRACK_ME_BRO;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mToastAnimation) {
            int index = mToastAnimation.findIndexOfValue((String) objValue);
            Settings.System.putString(getContentResolver(), Settings.System.TOAST_ANIMATION, (String) objValue);
            mToastAnimation.setSummary(mToastAnimation.getEntries()[index]);
            Toast.makeText(getActivity(), "Toast animation changed", Toast.LENGTH_SHORT).show();
        }
        if (preference == mListViewAnimation) {
            int value = Integer.parseInt((String) objValue);
            int index = mListViewAnimation.findIndexOfValue((String) objValue);
            Settings.System.putInt(getContentResolver(),
                    Settings.System.LISTVIEW_ANIMATION, value);
            mListViewAnimation.setSummary(mListViewAnimation.getEntries()[index]);
            mListViewInterpolator.setEnabled(value > 0);
        }
        if (preference == mListViewInterpolator) {
            int value = Integer.parseInt((String) objValue);
            int index = mListViewInterpolator.findIndexOfValue((String) objValue);
            Settings.System.putInt(getContentResolver(),
                    Settings.System.LISTVIEW_INTERPOLATOR, value);
            mListViewInterpolator.setSummary(mListViewInterpolator.getEntries()[index]);
        }
        if (preference == mRecentsClearAllLocation) {
            int location = Integer.valueOf((String) objValue);
            int index = mRecentsClearAllLocation.findIndexOfValue((String) objValue);
            Settings.System.putIntForUser(getActivity().getContentResolver(),
                    Settings.System.RECENTS_CLEAR_ALL_LOCATION, location, UserHandle.USER_CURRENT);
            mRecentsClearAllLocation.setSummary(mRecentsClearAllLocation.getEntries()[index]);
        }
        return true;
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mBootAnimation) {
            showBootAnimationDialog();
            return true;
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    private boolean resetAnimation() {
        boolean exists = false;
        if (new File(systemPath).exists()) {
            bootAnimationPath = systemPath;
            exists = true;
        } else {
            bootAnimationPath = "";
        }
        return exists;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == bootAniRequest) {
                if (data == null) {
                    return;
                }
                bootAnimationPath = data.getData().getPath();
                showBootAnimationDialog();
            }
        }
    }

    private void showBootAnimationDialog() {
        if (bootAnimationDialog != null) {
            bootAnimationDialog.cancel();
            bootAnimationDialog = null;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.boot_animation_preview);
        if (!bootAnimationPath.isEmpty() && (!systemPath.equalsIgnoreCase(bootAnimationPath))) {
            builder.setPositiveButton(R.string.apply, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    installBootAnimation(dialog, bootAnimationPath);
                    resetAnimation();
                }
            });
        }
        builder.setNeutralButton(R.string.set_custom, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                PackageManager pm = getActivity().getPackageManager();
                Intent testIntent = new Intent(Intent.ACTION_GET_CONTENT);
                testIntent.setType("file/*");
                List<ResolveInfo> list = pm.queryIntentActivities(testIntent, PackageManager.GET_ACTIVITIES);
                if (!list.isEmpty()) {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
                    intent.setType("file/*");
                    startActivityForResult(intent, bootAniRequest);
                } else {
                    Toast.makeText(getActivity(), R.string.no_file_manager, Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                resetAnimation();
                dialog.dismiss();
            }
        });

        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.boot_animation_preview_dialog, (ViewGroup) getActivity().findViewById(R.id.boot_animation_preview_root));
        error = (TextView) view.findViewById(R.id.boot_animation_preview_error);
        preview = (ImageView) view.findViewById(R.id.boot_animation_preview);
        preview.setVisibility(View.GONE);
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        preview.setLayoutParams(new LinearLayout.LayoutParams(size.x / 2, size.y / 2));
        error.setText(R.string.loading_preview);
        builder.setView(view);
        bootAnimationDialog = builder.create();
        bootAnimationDialog.setOwnerActivity(getActivity());
        bootAnimationDialog.show();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                initPreview(bootAnimationPath);
            }
        });
        thread.start();
    }

    private void initPreview(String path) {
        File zip = new File(path);
        ZipFile zipFile = null;
        String desc = "";
        InputStream inputStream = null;
        InputStreamReader inputReader = null;
        BufferedReader bReader = null;

        try {
            zipFile = new ZipFile(zip);
            ZipEntry entry = zipFile.getEntry("desc.txt");
            inputStream = zipFile.getInputStream(entry);
            inputReader = new InputStreamReader(inputStream);
            StringBuilder sb = new StringBuilder(0);
            bReader = new BufferedReader(inputReader);
            String read = bReader.readLine();
            while (read != null) {
                sb.append(read);
                sb.append('\n');
                read = bReader.readLine();
            }

            desc = sb.toString();
        } catch (Exception ex) {
            errorMessage = getActivity().getString(R.string.error_reading_zip);
            errorHandler.sendEmptyMessage(0);
            return;
        } finally {
            try {
                if (bReader != null) {
                    bReader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (inputReader != null) {
                    inputReader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        String[] info = desc.replace("\\r", "").split("\\n");
        int fps = Integer.parseInt(info[0].split(" ")[2]);
        String part1Name = info[1].split(" ")[3];
        String part2Name;
        int tempAnimationDuration = getAnimationDuration(fps);
        int fractionAnimationDuration = (tempAnimationDuration / 10) * 7;
        int finalAnimationDuration = tempAnimationDuration + fractionAnimationDuration;
        try {
            if (info.length > 2) {
                part2Name = info[2].split(" ")[3];
            } else {
                part2Name = "";
            }
        } catch (Exception e) {
            part2Name = "";
            e.printStackTrace();
        }

        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inSampleSize = 4;
        part1 = new ExposedAnimationDrawable();
        part2 = new ExposedAnimationDrawable();
        try {
            for (Enumeration<? extends ZipEntry> enumeration = zipFile.entries();
                enumeration.hasMoreElements(); ) {
                ZipEntry ze = enumeration.nextElement();
                if (ze.isDirectory()) {
                    continue;
                }
                String partName = ze.getName().split("/")[0];
                if (part1Name.equalsIgnoreCase(partName)) {
                    InputStream part1InStream = null;
                    try {
                        part1InStream = zipFile.getInputStream(ze);
                        part1.addFrame(new BitmapDrawable(getResources(), BitmapFactory.decodeStream(part1InStream, null, opt)), finalAnimationDuration);
                    } finally {
                        if (part1InStream != null) {
                            part1InStream.close();
                        }
                    }
                } else if (part2Name.equalsIgnoreCase(partName)) {
                    InputStream part2InStream = null;
                    try {
                        part2.addFrame(new BitmapDrawable(getResources(), BitmapFactory.decodeStream(part2InStream, null, opt)), finalAnimationDuration);
                    } finally {
                        if (part2InStream != null) {
                            part2InStream.close();
                        }
                    }
                }
            }
        } catch (IOException e) {
            errorMessage = getActivity().getString(R.string.error_loading_preview);
            errorHandler.sendEmptyMessage(0);
            return;
        }

        if (!part2Name.isEmpty()) {
            part1.setOneShot(false);
            part2.setOneShot(false);
            part1.setOnAnimationFinishedListener(new ExposedAnimationDrawable.OnAnimationFinishedListener() {
                @Override
                public void onAnimationFinished() {
                    preview.setImageDrawable(part2);
                    part1.stop();
                    part2.start();
                }
            });
        } else {
            part1.setOneShot(false);
        }
        finishedHandler.sendEmptyMessage(0);
    }

    private Handler errorHandler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            preview.setVisibility(View.GONE);
            error.setText(errorMessage);
        }
    };

    private Handler finishedHandler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            preview.setImageDrawable(part1);
            preview.setVisibility(View.VISIBLE);
            error.setVisibility(View.GONE);
            part1.start();
        }
    };

    private void installBootAnimation(DialogInterface dialog, String bootAnimationPath) {
        DateFormat dateFormat = new SimpleDateFormat("ddMMyyyy_HHmmss");
        Date date = new Date();
        String current = (dateFormat.format(date));
        new AbstractAsyncSuCMDProcessor() {
            @Override
            protected void onPostExecute(String result) {}
        }.execute("mount -o rw,remount /system", "cp -f /system/media/bootanimation.zip " + Environment.getExternalStorageDirectory() + "/bootanimation_backup_" + current + ".zip", "cp -f " + bootAnimationPath + " /system/media/bootanimation.zip", "chmod 644 /system/media/bootanimation.zip", "mount -o ro,remount /system");
    }
    private int getAnimationDuration(int fps) {
        return (1000 / fps);
    }
}
