package com.sjsu.boreas;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sjsu.boreas.Firebase.RegisterOnFirebase;
import com.sjsu.boreas.database.User;

import java.io.FileOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class RegisterActivity extends Activity implements LocationListener {

    String KEY = "AIzaSyDyGjh3NUYPVNdxlbRdZD38FDrX-bOf5B4";

	private static String TAG = "Boreas";
	private static String SUB_TAG = "---RegisterActivity ";
	
    EditText fullNameEditor;
    TextView locationLabel;
    private LinearLayout registerLayout;

    private String bestProvider;
    private Criteria criteria;

    Location location;
    
    private Button sign_up;

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
			Log.e(TAG, SUB_TAG+"Permission for location isn't granted yet");
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
				Log.e(TAG, SUB_TAG+"Ask for permission");
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle(R.string.title_location_permission)
                        .setMessage(R.string.text_location_permission)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(RegisterActivity.this,
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
		Log.e(TAG, SUB_TAG+"On Create");
        super.onCreate(savedInstanceState);
        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_register_temp);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;
        int margin = (width/6);
        registerLayout = (LinearLayout) findViewById(R.id.login_layout);
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) registerLayout.getLayoutParams();
        params.setMargins(margin,0,0,0);

        fullNameEditor = findViewById(R.id.register_name);
        locationLabel = findViewById(R.id.permission_text);

        sign_up = findViewById(R.id.signup);

        sign_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG, SUB_TAG + "On click for signUp.");
                if(addLocation(v)){
                    completeRegistration(v);
                }
            }
        });
    }

    /**
     * Called by add location button
     * @param view The location button itself
     */
    public boolean addLocation(View view){
		Log.e(TAG, SUB_TAG+"AddLocation");
        if(ContextCompat.checkSelfPermission(this, "android.permission.ACCESS_FINE_LOCATION") == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(this, new String[]{
                    "Manifest.permission.ACCESS_FINE_LOCATION"
            }, 0);
        }else {
            if(obtainLocation()){
                return true;
            }
        }
        return false;
    }

    private boolean obtainLocation(){
		Log.e(TAG, SUB_TAG+"Obtain Location");
        checkLocationPermission();
        if(ContextCompat.checkSelfPermission(this, "android.permission.ACCESS_FINE_LOCATION") == PackageManager.PERMISSION_DENIED){
			Log.e(TAG, SUB_TAG+"Don't have permission for Location");
            return false;
		}
        criteria = new Criteria();
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		bestProvider = String.valueOf(locationManager.getBestProvider(criteria, true)).toString();
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if(location == null){
				 //This is what you need:
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, this);
                if(location == null){
					Log.e(TAG, SUB_TAG+"Location is still null");
				}
			}
            locationLabel.setText(location.getLatitude()+" , "+location.getLongitude());
            Log.e(TAG, SUB_TAG+"Location found: " + locationLabel.getText());
            return true;
        }
        return false;
    }

    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults){
		Log.e(TAG, SUB_TAG+"Request Permission");
		checkLocationPermission();
        switch(requestCode){
            case 0: //Location permission to add current location
                Log.e(TAG, SUB_TAG+"Location permission to add");
                obtainLocation();
//                if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    Log.e(TAG, SUB_TAG+"Permission Granted");
//                    obtainLocation();
//                }
                break;
        }
    }

    /**
     * Create unique user id from inputted data of user
     * @param data full name plus geo location. "Salted" with random number to lower chance of collision
     *             when location not provided.
     */
    private String generateUniqueUserId(String data){
		Log.e(TAG, SUB_TAG+"Generate Unique Use ID");
        data = (int)(Math.random() * 1024) + data; //random value in front
        try {
            MessageDigest hasher = MessageDigest.getInstance("MD5");
            hasher.update(data.getBytes());
            byte [] hash = hasher.digest();
            StringBuilder builder = new StringBuilder();
            //From: https://howtodoinjava.com/security/how-to-generate-secure-password-hash-md5-sha-pbkdf2-bcrypt-examples/
            System.out.println("Printing hash: ");
            for(byte b : hash){
                System.out.print(b+ " ");
                builder.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
            }
            return builder.toString();
        }catch (NoSuchAlgorithmException e){
            e.printStackTrace();
            System.exit(1);
            return "ERROR";
        }
    }

    /**
     * Called by REGISTER button
     * @param view The register button itself
     */
    public void completeRegistration(View view){
		Log.e(TAG, SUB_TAG+"Complete Registration");
        //Check if all fields are filled
        if(fullNameEditor.getText().toString().equals("") || location == null){
			Log.e(TAG, SUB_TAG+"Something not right with the info provided: " + fullNameEditor.getText() + ", " + "location: " + location);
            Toast.makeText(getApplicationContext(), R.string.reg_error_unfilled, Toast.LENGTH_LONG);
            return;
        }

        String name = fullNameEditor.getText().toString();
        String uniqueId = generateUniqueUserId(name + "\n" + location.getLatitude() + "\n" + location.getLongitude());
        final User myUser = new User(uniqueId, name, location.getLatitude(), location.getLongitude(), true);
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
				Log.e(TAG, SUB_TAG+"Adding user to database" + myUser);
                MainActivity.database.userDao().insertAll(myUser);
                finish();
            }
        });
		
		Log.e(TAG, SUB_TAG+"User: " + myUser);
        System.out.println(myUser);
        pushNewUserToFIrebase(myUser);
    }

    public void pushNewUserToFIrebase(User myUser){
        Log.e(TAG, SUB_TAG+"Push new user to firebase");
        boolean connected = false;

        if(networkIsAvailable()) {
            Log.e(TAG, SUB_TAG+"Network is available: so pushing to firebase");
            RegisterOnFirebase registerOnFirebase = new RegisterOnFirebase();
            registerOnFirebase.RegisterUserOnFirebase(myUser);
        }
        else{
            Log.e(TAG, SUB_TAG+"NEtwork isn't available");
        }
    }

    public boolean networkIsAvailable(){
        Log.e(TAG, SUB_TAG+"inside function networkIsAvailable");
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.e(TAG, SUB_TAG+"OnLocationChanged");
        Log.e(TAG, "Latitude:" + location.getLatitude() + ", Longitude:" + location.getLongitude());
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
