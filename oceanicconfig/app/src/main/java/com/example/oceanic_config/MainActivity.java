// Import necessary packages
package com.example.oceanic_config;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Objects;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

// Define the main class for the application
public class MainActivity extends AppCompatActivity {

    // Initialize ToggleButtons and booleans for dark mode and zoom settings
    private ToggleButton darkModeButton;
    private ToggleButton toggleZoomButton;
    private boolean darkMode;
    private boolean toggleZoom;

    // Define the codes for the external storage permissions
    private static final int EXTERNAL_STORAGE_PERMISSION_CODE = 100;
    private static final int REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION = 1;

    // Request write external storage permission
    private void requestWriteExternalStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION);
        }
    }
    // Method called when the activity is first created
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Call the superclass onCreate method
        super.onCreate(savedInstanceState);

        // Set the content view for the activity
        setContentView(R.layout.activity_main);

        // Define the directory and file for the scanner configuration
        File xmlFileDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File xmlFile = new File(xmlFileDir, "scanner.xml");

        // Initialize strings for link, port, ITS, mandat, and static zoom
        final String[] link = new String[1];
        final String[] port = new String[1];
        final String[] its = new String[1];
        final String[] mandat = new String[1];
        final String[] staticzoom = new String[1];

        // Find and assign views for the link, port, ITS, mandat, and static zoom fields, and the save button
        EditText linkEditText = findViewById(R.id.link_edittext);
        EditText portEditText = findViewById(R.id.port_edittext);
        EditText itsEditText = findViewById(R.id.its_edittext);
        EditText mandatEditText = findViewById(R.id.mandat_edittext);
        EditText staticzoomEditText = findViewById(R.id.staticzoom_edittext);
        Button saveButton = findViewById(R.id.save_button);

        darkModeButton = findViewById(R.id.darkmode_button);
        toggleZoomButton = findViewById(R.id.togglezoom_button);
        requestWriteExternalStoragePermission();
        if(!FileUtil.checkForScannerXml()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                FileUtil.createDefaultScannerXml(this);
            }
        }

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // Request permission to access external storage
            ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, EXTERNAL_STORAGE_PERMISSION_CODE);
        } else {
            // Basic checking for permissions etc.
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Log.d("TEST-TAG", "Path: " + Objects.requireNonNull(xmlFileDir).getAbsolutePath());
                if (xmlFileDir.exists()) {
                    File[] files = xmlFileDir.listFiles();
                    if (files != null && files.length > 0) {
                        Log.i("TEST-TAG", "Files are visible in selected directory");
                    } else {
                        Log.d("TEST-TAG", "No files found in selected directory " + xmlFileDir);
                    }
                }
            }
            try {
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = Objects.requireNonNull(dbf).newDocumentBuilder();
                Document doc = Objects.requireNonNull(db).parse(xmlFile);
                Log.d("TEST-TAG", "Opening file: "+xmlFile);
                Objects.requireNonNull(Objects.requireNonNull(doc).getDocumentElement()).normalize();

                // Parse the XML data and extract the link and mandat fields
                try {
                    Transformer transformer = Objects.requireNonNull(TransformerFactory.newInstance()).newTransformer();
                    StringWriter writer = new StringWriter();
                    Objects.requireNonNull(transformer).transform(new DOMSource(doc), new StreamResult(writer));
                    String xmlString = writer.toString();
                    NodeList nodeList = doc.getElementsByTagName("ScannerParameters");
                    Log.d("TEST-TAG", "Parsed XML data:\n " + xmlString);
                    Element element = (Element) Objects.requireNonNull(nodeList).item(0);
                    Log.w("FAILED-TAG", "FAIL HERE");
                    assert element != null;
                    link[0] = element.getElementsByTagName("link").item(0).getTextContent();
                    linkEditText.setText(link[0]);
                    port[0] = element.getElementsByTagName("port").item(0).getTextContent();
                    portEditText.setText(port[0]);
                    its[0] = element.getElementsByTagName("its").item(0).getTextContent();
                    itsEditText.setText(its[0]);
                    mandat[0] = element.getElementsByTagName("mandat").item(0).getTextContent();
                    mandatEditText.setText(mandat[0]);
                    darkMode = Boolean.parseBoolean(element.getElementsByTagName("darkmode").item(0).getTextContent());
                    darkModeButton.setChecked(darkMode);
                    toggleZoom = Boolean.parseBoolean(element.getElementsByTagName("togglezoom").item(0).getTextContent());
                    toggleZoomButton.setChecked(toggleZoom);
                    staticzoom[0] = element.getElementsByTagName("staticzoom").item(0).getTextContent();
                    staticzoomEditText.setText(staticzoom[0]);
                    Log.d("TEST-TAG", "Wyciągnięte dane: "+ link[0] +" / Port: "+ port[0] +" / ITS: "+ its[0] +" / Mandat:"+ mandat[0] + " / DarkMode: " + darkMode);
                }
                catch (TransformerException ignored) {

                }
            }
            catch (SAXException | IOException | ParserConfigurationException e) {
                e.printStackTrace();
            }

            assert saveButton != null;
            saveButton.setOnClickListener(v -> {
                // Check if write permission is granted
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    // Request write permission
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, EXTERNAL_STORAGE_PERMISSION_CODE);
                } else {
                    // Save the data to file
                    try {
                        // Read the text field values
                        String link1 = Objects.requireNonNull(Objects.requireNonNull(linkEditText).getText()).toString();
                        String port1 = Objects.requireNonNull(Objects.requireNonNull(portEditText).getText()).toString();
                        String its1 = Objects.requireNonNull(Objects.requireNonNull(itsEditText).getText()).toString();
                        String mandat1 = Objects.requireNonNull(Objects.requireNonNull(mandatEditText).getText()).toString();
                        String staticzoom1 = Objects.requireNonNull(Objects.requireNonNull(staticzoomEditText).getText()).toString();
                        darkMode = darkModeButton.isChecked();
                        toggleZoom = toggleZoomButton.isChecked();


                        // Parse the existing XML file
                        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                        DocumentBuilder db = Objects.requireNonNull(dbf).newDocumentBuilder();
                        Document doc = Objects.requireNonNull(db).parse(xmlFile);
                        Objects.requireNonNull(Objects.requireNonNull(doc).getDocumentElement()).normalize();

                        // Update the XML data with new values
                        NodeList nodeList = doc.getElementsByTagName("ScannerParameters");
                        Element element = (Element) nodeList.item(0);
                        Objects.requireNonNull(Objects.requireNonNull(Objects.requireNonNull(element).getElementsByTagName("link")).item(0)).setTextContent(link1);
                        element.getElementsByTagName("port").item(0).setTextContent(port1);
                        element.getElementsByTagName("its").item(0).setTextContent(its1);
                        element.getElementsByTagName("mandat").item(0).setTextContent(mandat1);
                        element.getElementsByTagName("darkmode").item(0).setTextContent(String.valueOf(darkMode));
                        element.getElementsByTagName("togglezoom").item(0).setTextContent(String.valueOf(toggleZoom));
                        element.getElementsByTagName("staticzoom").item(0).setTextContent(staticzoom1);

                        // Write the updated XML data to file
                        TransformerFactory transformerFactory = TransformerFactory.newInstance();
                        Transformer transformer = transformerFactory.newTransformer();
                        DOMSource source = new DOMSource(doc);
                        StreamResult result = new StreamResult(xmlFile);
                        transformer.transform(source, result);

                        Toast.makeText(getApplicationContext(), "Data saved successfully!", Toast.LENGTH_SHORT).show();

                    } catch (ParserConfigurationException | IOException | SAXException |
                             TransformerException e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "Error saving data!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            );
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("MissingSuperCall")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, try creating the file again
                FileUtil.createDefaultScannerXml(this);
            } else {
                // Permission denied, show a message to the user
                Toast.makeText(this, "Permission denied. Cannot create scanner.xml file",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
}