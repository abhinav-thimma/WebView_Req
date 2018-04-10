package main.androidapp.com.serverreq;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.GeolocationPermissions;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.squareup.picasso.Picasso;
import android.Manifest;
import android.widget.Toast;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;

import com.loopj.android.http.*;

import cz.msebera.android.httpclient.Header;


public class MainActivity extends AppCompatActivity {


    final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private WebView myWebView;
    private static final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 101;
    private PermissionRequest myRequest;

    //qr code scanner object
    private IntentIntegrator qrScan;

    FloatingActionButton qrButton;


    String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //to remove the action bar
        getSupportActionBar().hide();

        //checking if we have location permission
        checkLocationPermission();

        myWebView = (WebView) findViewById(R.id.webView);
        qrButton = findViewById(R.id.qrButton);

        qrScan = new IntentIntegrator(this);


        qrButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                qrScan.initiateScan();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();



        FirebaseDatabase.getInstance().getReference().child("url").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                ProgressDialog p = new ProgressDialog(MainActivity.this);
                p.setMessage("Getting the app ready");
                p.setTitle("Loading..");
                p.show();


                WebSettings webSettings = myWebView.getSettings();
                webSettings.setJavaScriptEnabled(true);
                webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
                myWebView = findViewById(R.id.webView);
                myWebView.setWebViewClient(new GeoWebViewClient());
                myWebView.getSettings().setGeolocationEnabled(true);
                myWebView.setWebChromeClient(new GeoWebChromeClient());
                myWebView.loadUrl(dataSnapshot.getValue().toString()+FirebaseAuth.getInstance().getCurrentUser().getEmail().toString());


                p.dismiss();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



//        try {
//            Picasso.with(getApplicationContext())
//                    .load(FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl())
//                    .resize(100,100).into(myImageView);
//            //myImageView.setImageBitmap(bmp);
//        }
//        catch (Exception e)
//        { Toast.makeText(MainActivity.this,e.toString(),Toast.LENGTH_LONG).show();}


    }

    //this class helps in accessing location within a web view

    public class GeoWebChromeClient extends WebChromeClient {
        @Override
        public void onGeolocationPermissionsShowPrompt(String origin,
                                                       GeolocationPermissions.Callback callback) {
            // Always grant permission since the app itself requires location
            // permission and the user has therefore already granted it
            callback.invoke(origin, true, false);
        }

        String url = FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl().toString();

        @Override
        public void onPermissionRequest(final PermissionRequest request) {
            myRequest = request;

            for (String permission : request.getResources()) {
                switch (permission) {
                    case "android.webkit.resource.AUDIO_CAPTURE": {
                        askForPermission(request.getOrigin().toString(), Manifest.permission.RECORD_AUDIO, MY_PERMISSIONS_REQUEST_RECORD_AUDIO);
                        break;
                    }
                }
            }
        }

    }


    //this class is a webViewClient used for future endeavours

    public class GeoWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            // When user clicks a hyperlink, load in the existing WebView
            view.loadUrl(url);
            return true;
        }
    }


    //these functions are for getting ocation access from user

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle("Location Permission")
                        .setMessage("Grant Location access")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }


    public void askForPermission(String origin, String permission, int requestCode) {

        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                permission)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    permission)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{permission},
                        requestCode);
            }
        } else {
            myRequest.grant(myRequest.getResources());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.

                }
                return;
            }
            case MY_PERMISSIONS_REQUEST_RECORD_AUDIO: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    myRequest.grant(myRequest.getResources());
                    //myWebView.loadUrl("<your url>");

                } else {
                }
            }



        }
    }

    //on back pressed is overloaded so that it loads the previous webpages visited

    public void onBackPressed() {
        // Pop the browser back stack or exit the activity
        if (myWebView.canGoBack()) {
            myWebView.goBack();
        } else {
            super.onBackPressed();
        }
    }


    //These functions help in not reloading the webview when screen orientation changes

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        myWebView.saveState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        myWebView.restoreState(savedInstanceState);
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {

            if (result.getContents() == null) {
                Toast.makeText(this, "Result Not Found", Toast.LENGTH_LONG).show();
            } else {

                Toast.makeText(this, result.getContents(), Toast.LENGTH_LONG).show();


                String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
                String time = new Date().toString();

                myWebView.loadUrl("http://loopj.com/android-async-http/");
                        //"https://jpmconline.xyz/qr.php?qremail="+email+"&qrtime="+time+"&qrimage="+result.getContents());




            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }




}
