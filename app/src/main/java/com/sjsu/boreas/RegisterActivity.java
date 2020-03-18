package com.sjsu.boreas;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.sjsu.boreas.database.User;

import java.io.FileOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class RegisterActivity extends AppCompatActivity implements LocationListener {

    String KEY = "AIzaSyDyGjh3NUYPVNdxlbRdZD38FDrX-bOf5B4";

	private static String TAG = "Boreas";
	private static String SUB_TAG = "---RegisterActivity ";
	
    EditText fullNameEditor;
    TextView locationLabel;

    private String bestProvider;
    private Criteria criteria;

    Location location;

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
        setContentView(R.layout.activity_register);

        fullNameEditor = findViewById(R.id.regitem_name);
        locationLabel = findViewById(R.id.regitem_locationlabel);

    }

    /**
     * Called by add location button
     * @param view The location button itself
     */
    public void addLocation(View view){
		Log.e(TAG, SUB_TAG+"AddLocation");
        if(ContextCompat.checkSelfPermission(this, "android.permission.ACCESS_FINE_LOCATION") == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(this, new String[]{
                    "Manifest.permission.ACCESS_FINE_LOCATION"
            }, 0);
        }else {
            obtainLocation();
        }
    }

    private void obtainLocation(){
		Log.e(TAG, SUB_TAG+"Obtain Location");
        checkLocationPermission();
        if(ContextCompat.checkSelfPermission(this, "android.permission.ACCESS_FINE_LOCATION") == PackageManager.PERMISSION_DENIED){
			Log.e(TAG, SUB_TAG+"Don't have permission for Location");
            return;
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
        }
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
