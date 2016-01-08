package com.android.settings.morningstar;

import android.os.AsyncTask;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import com.android.settings.morningstar.CMDProcessor;

public abstract class AbstractAsyncSuCMDProcessor extends AsyncTask<String, Void, String> {
    private boolean mMountSystem;
    public final String FAILURE = "failed_no_command";

    public AbstractAsyncSuCMDProcessor(boolean mountSystem) {
        this.mMountSystem = mountSystem;
    }

    public AbstractAsyncSuCMDProcessor() {
        this.mMountSystem = false;
    }

    @Override
    protected String doInBackground(String... params) {
        if (params[0] == null || params[0].trim().equals("")) {
            return FAILURE;
        }
        String stdout = null;
        if (mMountSystem) {
            getMount("rw");
        }
        try {
            for (int i = 0; params.length > i; i++) {
                if (params[i] != null && !params[i].trim().equals("")) {
                    stdout = CMDProcessor.runSuCommand(params[i]).getStdout();
                } else {
                    return FAILURE;
                }
            }
        } finally {
            if (mMountSystem) {
                getMount("ro");
            }
        }
        return stdout;
    }

    private static boolean getMount(String mount) {
        String[] mounts = getMounts("/system");
        if (mounts != null && mounts.length >= 3) {
            String device = mounts[0];
            String path = mounts[1];
            String point = mounts[2];
            String preferredMountCmd = ("mount -o " + mount + ",remount -t " + point + ' ' + device + ' ' + path);
            if (CMDProcessor.runSuCommand(preferredMountCmd).success()) {
                return true;
            }
        }
        String fallbackMountCmd = ("busybox mount -o remount," + mount + " /system");
        return CMDProcessor.runSuCommand(fallbackMountCmd).success();
    }

    private static String[] getMounts(CharSequence path) {
        BufferedReader bReader = null;
        try {
            bReader = new BufferedReader(new FileReader("/proc/mounts"), 256);
            String line;
            while ((line = bReader.readLine()) != null) {
                if (line.contains(path)) {
                    return line.split(" ");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bReader != null) {
                try {
                    bReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    @Override
    protected abstract void onPostExecute(String result);
}