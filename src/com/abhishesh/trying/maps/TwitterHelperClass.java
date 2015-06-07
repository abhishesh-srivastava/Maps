package com.abhishesh.trying.maps;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import twitter4j.GeoLocation;
import twitter4j.Query;
import twitter4j.Query.ResultType;
import twitter4j.Query.Unit;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Location;
import android.net.Uri;
import android.util.Log;
/**
 * Helper class which uses twitter4j api methods
 * @author Abhishesh
 *
 */
public class TwitterHelperClass {
    static String TWITTER_CONSUMER_KEY = "your-consumer-key";
    static String TWITTER_CONSUMER_SECRET = "consumer-secret-key";
    static String TWITTER_ACCESS_TOKEN_KEY = "access-token-key";
    static String TWITTER_ACCESS_TOKEN_SECRET = "access-token-secret-key";
    // Preference Constants
    static String PREFERENCE_NAME = "twitter_oauth";
    static final String PREF_KEY_OAUTH_TOKEN = "oauth_token";
    static final String PREF_KEY_OAUTH_SECRET = "oauth_token_secret";
    static final String PREF_KEY_TWITTER_LOGIN = "isTwitterLogedIn";
    static final String TWITTER_CALLBACK_URL = "oauth://t4jsample";
    // Twitter oauth urls
    static final String URL_TWITTER_AUTH = "auth_url";
    static final String URL_TWITTER_OAUTH_VERIFIER = "oauth_verifier";
    static final String URL_TWITTER_OAUTH_TOKEN = "oauth_token";
    // Twitter
    private static Twitter twitter;
    private static RequestToken requestToken;
    ConfigurationBuilder builder;
    Configuration configuration;
    // Shared Preferences
    private static SharedPreferences mSharedPreferences;
    private Context mContext;
    private String TAG = getClass().getSimpleName();
    private HashMap<GeoLocation, TweetBean>mLocationHash = new HashMap<GeoLocation, TweetBean>();
    private boolean isTwitterLoggedInAlready() {
        return mSharedPreferences.getBoolean(PREF_KEY_TWITTER_LOGIN, false);
    }
    
    public TwitterHelperClass(Context context) {
        mContext =  context;
        mSharedPreferences = mContext.getSharedPreferences("my_pref", Activity.MODE_PRIVATE);
    }
    
    /**
     * Login to twitter
     */
    public void loginToTwitter() {
        // Check if already logged in
        if (!isTwitterLoggedInAlready()) {
            builder = new ConfigurationBuilder();            
            builder.setOAuthConsumerKey(TWITTER_CONSUMER_KEY);
            builder.setOAuthConsumerSecret(TWITTER_CONSUMER_SECRET);
            builder.setOAuthAccessToken(TWITTER_ACCESS_TOKEN_KEY);
            builder.setOAuthAccessTokenSecret(TWITTER_ACCESS_TOKEN_SECRET);
            configuration = builder.build();
            TwitterFactory factory = new TwitterFactory(configuration);
            twitter = factory.getInstance();
            Thread thread = new Thread(new Runnable(){
                @Override
                public void run() {
                    try {
                        requestToken = twitter.getOAuthRequestToken();
                        mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri
                                .parse(requestToken.getAuthenticationURL())));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            thread.start();         
        } else {
            Log.d(TAG,"User Alread Logged In");
        }
    }
    
    public void logoutFromTwitter() {
        // Clear the shared preferences
        Editor e = mSharedPreferences.edit();
        e.remove(PREF_KEY_OAUTH_TOKEN);
        e.remove(PREF_KEY_OAUTH_SECRET);
        e.remove(PREF_KEY_TWITTER_LOGIN);
        e.commit();
    }
    
    /**
     * Returns list of recent tweets having geolocation information under 100 miles radiues
     * @param loc
     * @param keyword
     * @param totalQuery
     * @return
     */
    public ArrayList<TweetBean> getGeoLocationBasedTweets(Location loc, String keyword,int totalQuery){
        ArrayList<TweetBean>listOfTweets = new ArrayList<TweetBean>();
        try {
            Query query = new Query();
            GeoLocation location = new GeoLocation(loc.getLatitude(), loc.getLongitude());
            Unit unit = Query.MILES; // or Query.KM;
            query.setGeoCode(location, 100, unit);
            query.setCount(50);
            query.resultType(ResultType.recent);
            QueryResult result;
            do {
            result = twitter.search(query);
            List<Status> tweets = result.getTweets();
            for (Status tweet : tweets) {
                TweetBean tbean = new TweetBean();
                tbean.setBody(tweet.getText());
                tbean.setImageURL(tweet.getUser().getOriginalProfileImageURL());
                tbean.setLocation(tweet.getGeoLocation());
                tbean.setUserName(tweet.getUser().getScreenName());
                tbean.setDate(tweet.getCreatedAt());
                if(listOfTweets.size()>=totalQuery)
                    break;
                if(!mLocationHash.containsKey(tweet.getGeoLocation())){
                    mLocationHash.put(tweet.getGeoLocation(), tbean);
                    listOfTweets.add(tbean);
                }
            }
            if(listOfTweets.size()>=totalQuery)
                break;
           } while ((query = result.nextQuery()) != null);
        } catch (TwitterException te) {
            Log.d(TAG,"Failed to search tweets: " + te.getMessage());
        }
        
        /**
         * Sorting tweets according to date since we need to unpin oldest last 10 pins
         */
        Collections.sort(listOfTweets, new Comparator<TweetBean>() {
            @Override
            public int compare(TweetBean lhs, TweetBean rhs) {
                if(lhs.getDate() == null || rhs.getDate() == null)
                    return 0;
                return lhs.getDate().compareTo(rhs.getDate());
            }
        });
        return listOfTweets;
    }
}
