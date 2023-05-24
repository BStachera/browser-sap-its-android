package com.example.oceprod;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.oceanic.oceprod.R;

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
public class FullscreenActivity extends AppCompatActivity {
    private WebView webView;
    private View decorView;
    private boolean darkMode;
    private boolean toggleZoom;
    private static final int EXTERNAL_STORAGE_PERMISSION_CODE = 100;
    private static final int REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION = 1;

    private void requestWriteExternalStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION);
        }
    }
    // Set up the activity
    @SuppressLint({"SetJavaScriptEnabled", "NewApi"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Set up local variables
        File xmlFileDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File xmlFile = new File(xmlFileDir, "scanner.xml");
        String link, port, its, mandat, result = null;
        Integer staticzoom = 49;
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
        decorView.setOnSystemUiVisibilityChangeListener(visibility -> {
            if (visibility == 0){
                decorView.setSystemUiVisibility(hideSystemBars());

            }
        });
        // Set the layout to full screen
        Objects.requireNonNull(getSupportActionBar()).hide();
        setContentView(R.layout.activity_fullscreen);
        webView = findViewById(R.id.webView);
        requestWriteExternalStoragePermission();
        if(!FileUtil.checkForScannerXml()) {
            FileUtil.createDefaultScannerXml(this);
        }

        if (ContextCompat.checkSelfPermission(FullscreenActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // Request permission to access external storage
            ActivityCompat.requestPermissions(FullscreenActivity.this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, EXTERNAL_STORAGE_PERMISSION_CODE);
        } else {
             // Basic checking for permissions etc.
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {


                Log.d("TEST-TAG", "Path: " + xmlFileDir.getAbsolutePath());
                if (xmlFileDir.exists()) {
                    File[] files = xmlFileDir.listFiles();
                    if (files != null && files.length > 0) {
                        Log.i("TEST-TAG", "Files are visible in selected directory");
                    } else {
                        Log.d("TEST-TAG", "No files found in selected directory " + xmlFileDir);
                    }
                }
            }

            // Read data from XML file
            try {
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                Document doc = db.parse(xmlFile);
                Log.d("TEST-TAG", "Opening file: "+xmlFile);
                doc.getDocumentElement().normalize();

                // Parse the XML data and extract the link and mandat fields
                try {
                    Transformer transformer = TransformerFactory.newInstance().newTransformer();
                    StringWriter writer = new StringWriter();
                    transformer.transform(new DOMSource(doc), new StreamResult(writer));
                    String xmlString = writer.toString();
                    NodeList nodeList = doc.getElementsByTagName("ScannerParameters");
                    Log.d("TEST-TAG", "Parsed XML data:\n " + xmlString);
                    Element element = (Element) nodeList.item(0);
                    link = element.getElementsByTagName("link").item(0).getTextContent();
                    port = element.getElementsByTagName("port").item(0).getTextContent();
                    its = element.getElementsByTagName("its").item(0).getTextContent();
                    mandat = element.getElementsByTagName("mandat").item(0).getTextContent();
                    darkMode = Boolean.parseBoolean(element.getElementsByTagName("darkmode").item(0).getTextContent());
                    toggleZoom = Boolean.parseBoolean(element.getElementsByTagName("togglezoom").item(0).getTextContent());
                    staticzoom = Integer.valueOf(element.getElementsByTagName("staticzoom").item(0).getTextContent());
                    result = link+":"+port+"/sap/bc/gui/sap/its/"+its+"?sap-client="+mandat;
                    Log.i("TEST-TAG", "Generated Link: " + result);
                    Log.i("TEST-TAG", "Dark-mode status: " + darkMode);
                }
                catch (TransformerException ignored) {

                }
            }
            catch (SAXException | IOException | ParserConfigurationException e) {
                e.printStackTrace();
            }

            // Set up the WebView

            webView.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageFinished(WebView view, String url) {



                    super.onPageFinished(view, url);
                    // check if darkmode is enabled
                    if (darkMode) {
                        // apply negative filter to the loaded website
                        String invertCss = "html {-webkit-filter: invert(100%); " +
                                "-moz-filter: invert(100%); " +
                                "-o-filter: invert(100%); " +
                                "-ms-filter: invert(100%); }";
                        String js = "(function() {" +
                                "var style = document.createElement('style');" +
                                "style.innerHTML = '" + invertCss + "';" +
                                "document.head.appendChild(style);" +
                                "})();";
                        view.loadUrl("javascript:" + js);
                    }
                }
            });

            WebSettings webSettings = webView.getSettings();
            String userAgent = "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (HTML, like Gecko) Chrome/112.0.0.0 Mobile Safari/537.36";
            webSettings.setJavaScriptEnabled(true);
            webSettings.setUserAgentString(userAgent);
            webSettings.setBuiltInZoomControls(toggleZoom);
            webView.setInitialScale(staticzoom);
            webView.loadUrl(result);

            //Disable automatic opening the keyboard (not working on scanner with own keyboard)
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(webView.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
            Log.v("TEST-TAG", "Opened app with Link: " + result);
            webView.requestFocus();
        }
    }

    // Override the onBackPressed method to prevent the back button from closing the app
    // If the WebView can go back, go back in the WebView history. Otherwise, call the super method to close the app.
    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus){
            decorView.setSystemUiVisibility(hideSystemBars());
        }
    }

    // Override the onWindowFocusChanged method to hide the system UI (status bar and navigation bar) when the app window has focus
    // Call the super method and set the system UI visibility to hide the status bar and navigation bar
    private int hideSystemBars (){
        return View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
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