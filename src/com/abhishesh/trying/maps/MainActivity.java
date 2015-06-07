package com.abhishesh.trying.maps;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.abhishesh.trying.maps.MyLocation.LocationResult;
/**
 * MainActivity : Like a splash window
 * @author Abhishesh
 * 
 */
public class MainActivity extends FragmentActivity{
	
    static Location mCurrentLocation;
    private Context mContext;
    private String TAG = getClass().getSimpleName();

    private TwitterCallback mCallback = new TwitterCallback() {
        @Override
        public void onLocationCompleted() {
            // TODO Auto-generated method stub
            startActivity(new Intent(MainActivity.this,TwitterGeoLocationActivity.class));
            finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_main);
        getIntent().addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        initLocationRequest();
    }
    
    /**
     * Function used to get the location 
     */
    private void initLocationRequest() {
        if(!checkForLocationServices()){
            Log.d(TAG,"I am going to die :(");
        } else{
            MyLocation myLocation = new MyLocation();
            myLocation.getLocation(this, locationResult);
        }
    }

    /**
     * Checks whether location services are enabled or not
     * @return boolean
     */
    public boolean checkForLocationServices(){
        LocationManager lm = (LocationManager)mContext.getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;
        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch(Exception ex) {
            Log.e(TAG,"gps services error");
        }
        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch(Exception ex) {
            Log.e(TAG,"network enabled error");
        }
        if(!gps_enabled && !network_enabled) {
            // notify user
            AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
            dialog.setMessage(mContext.getResources().getString(R.string.gps_network_not_enabled));
            dialog.setPositiveButton(mContext.getResources().getString(R.string.open_location_settings), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        mContext.startActivity(myIntent);
                        //get gps
                    }
                });
            dialog.setNegativeButton(mContext.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        finish(); //Finish if location service are not enabled and user not willing to enable :D
                    }
                });
            dialog.show();      
        }
        return network_enabled|gps_enabled;
    }
    
    LocationResult locationResult = new LocationResult(){
    	/**
    	 * Once the location is obtained start the map activity
    	 */
        @Override
        public void gotLocation(Location location){
            mCurrentLocation = location;
            if(mCurrentLocation!=null){
                mCallback.onLocationCompleted();
            }
        }
    };
}
