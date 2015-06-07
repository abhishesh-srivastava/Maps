package com.abhishesh.trying.maps;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class TwitterGeoLocationActivity extends Activity{
    private static final String ACTION_REFRESH_MAP_UI = "com.abhishesh.refresh_map_view";
    GoogleMap map;
    TwitterHelperClass mHelper;
    private ArrayList<TweetBean>mTweetsList = new ArrayList<TweetBean>();
    private ArrayList<TweetBean>mExtraTweetsList = new ArrayList<TweetBean>();
    String keyword;
    private String TAG = getClass().getSimpleName();
    private HashMap<String, TweetBean>mMarkerMap = new HashMap<String, TweetBean>();
    private Drawable mDrawableForMarker;
    private Long REFRESH_PERIOD = (long) 10000;
    private BroadcastReceiver mReceiver;
    Marker displayMarker;
    AlarmManager alarmManager;
    PendingIntent pendingIntent;
    
    private GeolocationTweets mCallback = new GeolocationTweets() {
        /**
         * Callback function which gets called when geolocation based tweets are stored in list.
         * Uses : Plots Marker on map
         */
        @Override
        public void onTweetsCompleted() {
            Log.d(TAG,"I got the callback");
            map.clear();
            mMarkerMap.clear();
            for(TweetBean tweet : mTweetsList){
                if(tweet.getLocation()!=null){
                    LatLng item = new LatLng(tweet.getLocation().getLatitude(), tweet.getLocation().getLongitude());
                        if (map!=null){
                            Marker marker = map.addMarker(new MarkerOptions().position(item)
                                .title(tweet.userName));
                            marker.setSnippet(tweet.getBody());
                            mMarkerMap.put(marker.getId(), tweet);
                    }
                }
            }
            
            map.setOnMarkerClickListener(new OnMarkerClickListener() {
                /**
                 * Called when marker is clicked
                 * Loads image associated with profile asynchronously
                 */
                @Override
                public boolean onMarkerClick(final Marker marker) {
                    // TODO Auto-generated method stub
                    LoadMarkerImage task = new LoadMarkerImage();
                    task.execute(mMarkerMap.get(marker.getId()).getImageURL());
                    displayMarker = marker;
                    return true;
                }
            });
        }

        /**
         * Called when last 10 tweets needs to be unpinned
         */
        @Override
        public void onTweetsChanged() {
            int size = mTweetsList.size();
            int to_size = mExtraTweetsList.size();
            int loop_size = size - to_size;
            int i=0;
            while(loop_size<size && loop_size>0){
                mTweetsList.set(loop_size++, mExtraTweetsList.get(i++));
            }
            Collections.sort(mTweetsList, new Comparator<TweetBean>() {
                @Override
                public int compare(TweetBean lhs, TweetBean rhs) {
                    if(lhs.getDate() == null || rhs.getDate() == null)
                        return 0;
                    return lhs.getDate().compareTo(rhs.getDate());
                }
                });
            mCallback.onTweetsCompleted();
        }
    };
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_fragment);
        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map_fragment)).getMap();
        //Move map position near current location coordinates
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(MainActivity.mCurrentLocation.getLatitude(), MainActivity.mCurrentLocation.getLongitude()), 8.0f));
        keyword = getIntent().getStringExtra("city");
        init();
        
        map.setInfoWindowAdapter(new InfoWindowAdapter() {
            /**
             * View for displaying marker popup, if null default framework view would be used
             */
            @Override
            public View getInfoWindow(Marker marker) {
                // TODO Auto-generated method stub
                return null;
            }
            
            /**
             * For changing the content of infowindow
             * Called when showMarkerInfo method is called
             */
            @Override
            public View getInfoContents(Marker marker) {
                View v = getLayoutInflater().inflate(R.layout.detail_activity, null);
                TweetBean tb = mMarkerMap.get(marker.getId());
                String mUserNameText = tb.getUserName();
                String mBodyText = tb.getBody();
                TextView mBody = (TextView) v.findViewById(R.id.body);
                TextView mName = (TextView) v.findViewById(R.id.name);
                ImageView mImage = (ImageView) v.findViewById(R.id.image);
                Drawable d = mDrawableForMarker;
                mBody.setText(mBodyText);
                mName.setText(mUserNameText);
                if(d!=null)
                    mImage.setImageDrawable(d);
                return v;
            }
        });
    }
    
    /**
     * Function intended to get drawable from given URL
     * @param urlString
     * @return Drawable
     */
    public Drawable fetchDrawable(String urlString) {
           Drawable drawable = null;
            try {
               InputStream is = fetch(urlString);
               drawable = Drawable.createFromStream(is, "src");
               
           } catch(Exception e){
            Log.d(TAG, e.toString());
           }
           return drawable;
    }
    
    /**
     * get inputstream from parsing given url
     * @param urlString
     * @return
     * @throws MalformedURLException
     * @throws IOException
     */
    private InputStream fetch(String urlString) throws MalformedURLException, IOException {
        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpGet request = new HttpGet(urlString);
        HttpResponse response = httpClient.execute(request);
        return response.getEntity().getContent();
    }
    
    /**
     * Initialises initial setup, and execute task which performs loading of geolocation based tweets
     */
    private void init() {
        mHelper = new TwitterHelperClass(this);
        mHelper.loginToTwitter();
        LoadGeoLocationBasedTweets mTask = new LoadGeoLocationBasedTweets();
        mTask.execute(100);
    }
    
    
    /**
     * Function for listening to periodic updates for tweets
     */
    private void registerAlarmBroadcast()
    {
        mReceiver = new BroadcastReceiver()
        {
            /**
             * Periodically schedule loading of location based tweets
             */
            public void onReceive(Context context, Intent intent)
            {
                LoadGeoLocationBasedTweets mTask = new LoadGeoLocationBasedTweets();
                mTask.execute(10);
                
            }
        };
        registerReceiver(mReceiver, new IntentFilter(ACTION_REFRESH_MAP_UI) );
        pendingIntent = PendingIntent.getBroadcast( this, 0, new Intent(ACTION_REFRESH_MAP_UI),0 );
        alarmManager = (AlarmManager)(this.getSystemService( Context.ALARM_SERVICE ));
    }
    
    /**
     * Release receiver and cancel any alarm manager callback
     */
    private void unregisterAlarmBroadcast()
    {
        alarmManager.cancel(pendingIntent); 
        unregisterReceiver(mReceiver);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        registerAlarmBroadcast();
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        unregisterAlarmBroadcast();
    }

    /**
     * Called when activity is destroyed
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHelper.logoutFromTwitter();
    }
    
    /**
     * Class for asynchronously loading geolocation based tweets
     * @author Abhishesh
     *
     */
    private class LoadGeoLocationBasedTweets extends AsyncTask<Integer, Integer, ArrayList<TweetBean>>{		
        private static final int LAST_UPDATED_QUERY = 10;
        private static final int MAX_NO_OF_QUERY = 100;

        /**
         * Loads tweet data into list
         */
        @Override
        protected ArrayList<TweetBean> doInBackground(Integer... params) {
            if(params[0]==MAX_NO_OF_QUERY){
                mTweetsList = mHelper.getGeoLocationBasedTweets(MainActivity.mCurrentLocation, keyword,params[0]);
                return mTweetsList;
            } else if(params[0]==LAST_UPDATED_QUERY){
                 mExtraTweetsList = mHelper.getGeoLocationBasedTweets(MainActivity.mCurrentLocation, keyword,params[0]);
                 return mExtraTweetsList;
            }
            return null;
        }
        
        /**
         * For updating UI once results are published from doInBackground method
         */
        @Override
        protected void onPostExecute(ArrayList<TweetBean> result) {
            if(result!=null){
                if(result.size()>10){
                    
                    mCallback.onTweetsCompleted();
                }
                else
                    mCallback.onTweetsChanged();
                
            }
            alarmManager.cancel(pendingIntent);
            alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + REFRESH_PERIOD, pendingIntent);
        }
        
    }

    /**
     * Class used for downloading profile image of user from URL
     * @author Abhishesh
     *
     */
    private class LoadMarkerImage extends AsyncTask<String, Integer, Drawable>{
        @Override
        protected Drawable doInBackground(String... params) {
            String url = params[0];
            InputStream is = null;
            try {
                is = fetch(url);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Drawable d = Drawable.createFromStream(is, "src");
            return d;
        }
        
        /**
         * Calls Marker showInfoWindow method to display marker information
         */
        @Override
        protected void onPostExecute(Drawable result) {
            super.onPostExecute(result);
            mDrawableForMarker = result;
            displayMarker.showInfoWindow(); 
        }
    }
    
    private interface GeolocationTweets{
        void onTweetsCompleted();
        void onTweetsChanged();
    }
}
