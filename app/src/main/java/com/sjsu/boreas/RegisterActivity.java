package com.sjsu.boreas;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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

import com.sjsu.boreas.Database.LocalDatabaseReference;
import com.sjsu.boreas.Database.LoggedInUser.LoggedInUser;
import com.sjsu.boreas.OnlineConnectionHandlers.FirebaseDataRefAndInstance;
import com.sjsu.boreas.Database.Contacts.User;
import com.sjsu.boreas.SecurityRelatedStuff.SecurityStuff;

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
    LocationManager locationManager;

    private EditText password;
    private EditText confirmPassword;
    private Button sign_up;
    private Button log_in;

    public LocalDatabaseReference localDatabaseReference = LocalDatabaseReference.get();
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

        fullNameEditor = findViewById(R.id.register_username);
        locationLabel = findViewById(R.id.permission_text);

        password = findViewById(R.id.register_password);
        confirmPassword = findViewById(R.id.register_confirm_password);
        sign_up = findViewById(R.id.signup);
        log_in = findViewById(R.id.login_register_act);

        log_in.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG, SUB_TAG+"CLicked login in register activity");
                MainActivity.context.onActivityResult(0, MainActivity.LOGIN_ACTIVITY_REQUEST_CODE, null);
            }
        });

        sign_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG, SUB_TAG + "On click for signUp.");
                //TODO: check if location is on at all.
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
		// TODO: either replace with checklocation function or remove altogether
        if(ContextCompat.checkSelfPermission(this, "android.permission.ACCESS_FINE_LOCATION") == PackageManager.PERMISSION_DENIED){
            Log.e(TAG, SUB_TAG+"addLocation(): Don't have permission for Location");
            ActivityCompat.requestPermissions(this, new String[]{
                    "Manifest.permission.ACCESS_FINE_LOCATION"
            }, 0);
        }else {
            return obtainLocation();
        }
        return false;
    }

    private boolean obtainLocation(){
		Log.e(TAG, SUB_TAG+"Obtain Location");
        checkLocationPermission();
        if(ContextCompat.checkSelfPermission(this, "android.permission.ACCESS_FINE_LOCATION") == PackageManager.PERMISSION_DENIED){
			Log.e(TAG, SUB_TAG+"obtainLocation(): Don't have permission for Location");
            return false;
		}
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		//TODO: Request Location Update and implement callback onLocationChanged function
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Log.e(TAG, SUB_TAG+"location enabled");
            criteria = new Criteria();
            bestProvider = String.valueOf(locationManager.getBestProvider(criteria, true)).toString();
            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            if(location == null){
                Log.e(TAG, SUB_TAG+"location is null");
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, this);
			}

            else{
                Toast.makeText(RegisterActivity.this, "latitude:" + location.getLatitude() + " longitude:" + location.getLongitude(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, SUB_TAG+"Location found: " + locationLabel.getText());
            }

            return true;
        }
        return false;
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.e(TAG, SUB_TAG+"onLocationChanged()");
        //remove location callback:
        locationManager.removeUpdates(this);

        //open the map:
        this.location = location;
        Toast.makeText(RegisterActivity.this, "latitude:" + location.getLatitude() + " longitude:" + location.getLongitude(), Toast.LENGTH_SHORT).show();
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
		String passwordStr = password.getText().toString();
		String confirmPasswordStr = confirmPassword.getText().toString();

        //Check if all fields are filled
        if(fullNameEditor.getText().toString().equals("") || passwordStr.equals("") || confirmPasswordStr.equals("")){
            Log.e(TAG, SUB_TAG+"One of the fields isn't filled");
            Toast.makeText(this, R.string.reg_error_unfilled, Toast.LENGTH_LONG);
            return;
        }

        if(!passwordStr.equals(confirmPasswordStr)){
            Log.e(TAG, SUB_TAG+"The 2 provided passwords don't match.");
            Toast.makeText(this, R.string.reg_error_passwords_dont_match, Toast.LENGTH_LONG);
            return;
        }

        if(location==null){
            Toast.makeText(this, "Could not get your location. Please try again in a while.", Toast.LENGTH_LONG);
            Log.e(TAG, SUB_TAG+"Something not right with the info provided: " + fullNameEditor.getText() + ", " + "location: " + location);
            return;
        }

        String name = fullNameEditor.getText().toString();
        String uniqueId = generateUniqueUserId(name + "\n" + location.getLatitude() + "\n" + location.getLongitude());

        String hashedPassword = null;
        hashedPassword = SecurityStuff.hashThePassword(passwordStr);

        if(hashedPassword == null){
            Log.e(TAG, SUB_TAG+"Something went wrong with the hash yo");
            Toast.makeText(this,"Something went wrong with the password provided yo", Toast.LENGTH_LONG);
        }

        final LoggedInUser myUser = new LoggedInUser(uniqueId, name, location.getLatitude(), location.getLongitude(), hashedPassword);
        localDatabaseReference.registerUser(myUser);
        FirebaseDataRefAndInstance.pushNewUserToFIrebase(myUser, this);

        Log.e(TAG, SUB_TAG+"User: " + myUser);
        System.out.println(myUser);

        localDatabaseReference.wipeAllPreviousUserData();
        MainActivity.context.onActivityResult(0, MainActivity.REGISTER_ACTIVITY_DONE_CODE, null);
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
