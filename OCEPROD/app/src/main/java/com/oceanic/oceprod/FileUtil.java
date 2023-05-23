package com.oceanic.oceprod;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.io.BufferedWriter;
import java.io.File;
import java.io.OutputStreamWriter;
import java.nio.file.Files;

public class FileUtil {
    private static final String TAG = "TEST-TAG";

    public static boolean checkForScannerXml() {
        // Check if scanner.xml file exists in the download directory
        String downloadPath = Environment.getExternalStorageDirectory().getPath() + "/Download/";
        Log.d(TAG, "Created default scanner.xml file at: " + downloadPath);
        File file = new File(downloadPath, "scanner.xml");
        File directory = new File(downloadPath);
        if (!directory.exists()) {
            // Create the directory if it doesn't exist
            directory.mkdirs();
        }
        return file.exists();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void createDefaultScannerXml(Context ignoredContext) {
        // Create a default scanner.xml file with empty fields
        String downloadPath = Environment.getExternalStorageDirectory().getPath() + "/Download/";
        File scannerFile = new File(downloadPath, "scanner.xml");
        try {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(scannerFile.toPath())));
            writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\" ?><ScannerParameters>\n" +
                    "<link></link>\n" +
                    "<port></port>\n" +
                    "<its></its>\n" +
                    "<mandat></mandat>\n" +
                    "<darkmode>false</darkmode>\n" +
                    "<togglezoom>false</togglezoom>\n" +
                    "<staticzoom>49</staticzoom>\n" +
                    "</ScannerParameters>");
            Log.d(TAG, "Created default scanner.xml file at: " + downloadPath);
            writer.close();
        } catch (Exception e) {
            Log.e(TAG, "Error creating scanner.xml file", e);
        }
    }
}