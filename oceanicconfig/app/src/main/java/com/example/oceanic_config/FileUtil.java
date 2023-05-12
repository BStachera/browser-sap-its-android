package com.example.oceanic_config;

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

    // Checks if the scanner.xml file exists in the Download directory
    public static boolean checkForScannerXml() {
        String downloadPath = Environment.getExternalStorageDirectory().getPath() + "/Download/";
        Log.d(TAG, "Created default scanner.xml file at: " + downloadPath);
        File file = new File(downloadPath, "scanner.xml");
        File directory = new File(downloadPath);
        if (!directory.exists()) {
            // If the directory does not exist, create it
            //noinspection ResultOfMethodCallIgnored
            directory.mkdirs();
        }
        return file.exists();
    }

    // Creates the default scanner.xml file with default parameters
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void createDefaultScannerXml(Context ignoredContext) {
        String downloadPath = Environment.getExternalStorageDirectory().getPath() + "/Download/";
        File scannerFile = new File(downloadPath, "scanner.xml");
        try {
            // Write the XML data to the file
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(scannerFile.toPath())));
            writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\" ?><ScannerParameters>\n" +
                    "<link></link>\n" +
                    "<port></port>\n" +
                    "<its></its>\n" +
                    "<mandat></mandat>\n" +
                    "<darkmode></darkmode>\n" +
                    "<togglezoom></togglezoom>\n" +
                    "<staticzoom></staticzoom>\n" +
                    "</ScannerParameters>");
            writer.close();
            // Log that the file was created successfully
            Log.d(TAG, "Created default scanner.xml file at: " + downloadPath);
        } catch (Exception e) {
            // Log any errors that occurred while creating the file
            Log.e(TAG, "Error creating scanner.xml file", e);
        }
    }
}