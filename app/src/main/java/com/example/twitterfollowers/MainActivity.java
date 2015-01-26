package com.example.twitterfollowers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.jirbo.adcolony.AdColony;

public class MainActivity extends ActionBarActivity {
	
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
    
    // Progress dialog
    ProgressDialog pDialog;
    
    //verifier
    String verifier;
 
    // Twitter
    private static Twitter twitter;
    private static AccessToken accessToken;
    private static RequestToken requestToken;
     
    // Shared Preferences
    private static SharedPreferences mSharedPreferences;
     
    // Internet Connection detector
    private ConnectionDetector cd;
    
    //Map with the keys and Secrets for different logins
    private HashMap<String, String> keySecret;
    
    //Map of AccessTokens and their values
    private HashMap<String, String> tokens;
    
    //ArrayList of keys mapped to keySecret
    public static List<String> keys;
    
    //ArrayList of values mapped to KeySecret
    public static List<String> values;
    
    //Key for logintoTwitter();
    private int count = 0;
   
    //TextView telling users how many more times they need to login
    private TextView tv;
    
    //TextView of Title
    private TextView title;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		AdColony.configure(this, "version:1.1,store:google", "appc8b3dc3044ac47d18b", "vzab3154251cbe458488");
		//Text View
		tv = (TextView) findViewById(R.id.Timesleft);
		
		//Title
		title = (TextView) findViewById(R.id.Title);
		
		Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/thin.ttf");
		title.setTypeface(typeface);
		// Shared Preferences
        mSharedPreferences = getApplicationContext().getSharedPreferences("MyPref", 0);
        
		
        
        //List of keys mapped to keySecret
		keys = new ArrayList<String>();
		keys.add("yMyaN8XUSacSINQ9sG0eU0gZB");
		keys.add("qNaA3MSOCEhW1FXgJUvhVrcf0");
		keys.add("AJOPAfBVJBRvcsFsGKvfy45RW");
		keys.add("HaPG28Gvlq0EUvZDvE8G0xluz");
		keys.add("dT4tOpcdXLdybIryWFDKCyZnn");
		keys.add("9g9HYmo6ddJfyWozHyb9ceZZx");
		keys.add("BucvnM4FmsqxnecFbliUADjXS");
		keys.add("mdpn6e1zhQsvSjpSlVX1b6l30");
		keys.add("v2NgM3Fb99mmjEvqk5I9eiVLE");
		keys.add("cF633aXMxax59FBbeS0job9o4");
		
		
		//List of Values mapped to keySecret
		values = new ArrayList<String>();
		values.add("6I37hOVXP1jElLYPt8XExDHBmQR8P5eLjaOPPeUaTASMtBdaG3");
		values.add("ohJB09h3QFSyL0uFFwP6RVVOolR2NmOm8CgC9yyMCGUcqiv4zJ");
		values.add("9zAZiDRgHP0eWVFyrEJx9T1Y7Lfa30z6V21X05a7GCxr6M24jb");
		values.add("2R6qVCDKtzbI3k2yt87T4nT3w6hcx6nt2Wgl9LhW03eG37v2q7");
		values.add("S0wWC1BEKJiPayrpgHehkcfI0KtqYVFdVGRCYHfjGdrH2Y1y1K");
		values.add("1kjbGbYD9xbFeZBB40dhvH4hSMqaiNK0TBnTkRRIUkkEhmuI21");
		values.add("qVZymrBoUnfuvqfbDtb0vOBqw1JaKFYZLbmEdsA3wnt6I0A6Di");
		values.add("F0E1McxXliuslC0fGol54g4Vqz31EKCLjrKWTZETBihXO6O8sI");
		values.add("6oCEcbahJ6NjfoFqjyfkleRKoSSPK3Adr5xkpMSutUTYnlJTrC");
		values.add("LLFQIdfTViH9FtGbdRXDHfCxJq6fqKyxluZJaJR25ddPaU14Lz");
        //KeySecret storing your secrets since 1982
        keySecret = new HashMap<String, String>();
        for(int x =0 ; x<keys.size(); x++)	{
        	keySecret.put(keys.get(x), values.get(x));
        }
		
		// count of logins done
        count = mSharedPreferences.getInt("count", 0);
        int str = keySecret.size() - count;
		tv.setText("Sign in " + str + " more times");
		 
		//Tokens storing you tokens since 1983
        tokens = new HashMap<String, String>();
        for(int x  = 1; x<=count; x++)	{
        	tokens.put(mSharedPreferences.getString(PREF_KEY_OAUTH_TOKEN+x, null), mSharedPreferences.getString(PREF_KEY_OAUTH_SECRET+x, null));
        }
        //Sign in Button
		Button login = (Button) findViewById(R.id.sign);
		login.setOnClickListener(new OnClickListener()	{
			public void onClick(View v) {
				// When user clicks sign in button
				cd = new ConnectionDetector(MainActivity.this.getApplicationContext());
				 
		        // Check if Internet present
		        if (!cd.isConnectingToInternet()) {
		            // Internet Connection is not present
		            Toast.makeText(getApplicationContext(), "No internet Connection Detected", Toast.LENGTH_SHORT).show();
		            // stop executing code by return
		            return;
		        }
		        loginToTwitter();
			}
		});
			/** This if conditions is tested once is
		     * redirected from twitter page. Parse the uri to get oAuth
		     * Verifier
		     * */
		if (!isTwitterLoggedInAlready()) {
			Uri uri = getIntent().getData();
			if (uri != null && uri.toString().startsWith(TWITTER_CALLBACK_URL)) {
				// oAuth verifier
				verifier = uri.getQueryParameter(URL_TWITTER_OAUTH_VERIFIER);
				try {
					// Get the access token
					// sAccessToken accessToken =
					// twitter.getOAuthAccessToken(requestToken, verifier);
					new AccessTk().execute(requestToken);
					// Shared Preferences
				} catch (Exception e) {
					// Check log for login errors
					Log.e("Twitter login error", e.toString());
				}
			}
		}
	}
	protected void loginToTwitter() {
		// TODO Auto-generated method stub
		 // Check if already logged in
        if (!isTwitterLoggedInAlready()) {
            ConfigurationBuilder builder = new ConfigurationBuilder();
            String key = keys.get(count);
            Log.d("key "+key, "secret "+keySecret.get(key));
            builder.setOAuthConsumerKey(key);
            builder.setOAuthConsumerSecret(keySecret.get(key));
            Configuration configuration = builder.build();
             
            TwitterFactory factory = new TwitterFactory(configuration);
            twitter = factory.getInstance();
            //new RequestTk().execute(twitter);
            Thread thread  = new Thread(new Runnable(){
				public void run() {
					// TODO Auto-generated method stub
					try {
						requestToken = twitter.getOAuthRequestToken(TWITTER_CALLBACK_URL);
						MainActivity.this.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(requestToken.getAuthenticationURL())));
					} catch (Exception e) {
						Log.e("Error", e.toString());
					}
				} 
            });
            thread.start();
            count++;
            Editor ed = mSharedPreferences.edit();
            ed.putInt("count", count);
            ed.commit();
            Log.d("count", count+"");
        } else {
        	Log.d("Twitter is already logged in", "logged in");
        	Intent i = new Intent(getBaseContext(), Get_Followers.class);
			startActivity(i);
        } 	 	
	}

	private class AccessTk extends AsyncTask<RequestToken, Void, Void>	{
		@Override
		protected Void doInBackground(RequestToken... rts) {
			// TODO Auto-generated method stub
			RequestToken requestToken = rts[0];
			try {
				AccessToken accessToken = twitter.getOAuthAccessToken(requestToken, verifier);
				MainActivity.accessToken = accessToken;
				Log.d("Access Token", "Got aceessToken");
			} catch (TwitterException e) {	
				// TODO Auto-generated catch block
				Log.d("accessTK", e.toString());
			}
			return null;
		}
		@Override
		protected void onPostExecute(Void v) {
			Editor e = mSharedPreferences.edit();
			// After getting access token, access token secret
			// store them in application preferences
			Log.d("count test", ""+count);
			e.putString(PREF_KEY_OAUTH_TOKEN + count,
					accessToken.getToken());
			Log.d("TOKEN ", accessToken.getToken().toString());
			e.putString(PREF_KEY_OAUTH_SECRET + count,
					accessToken.getTokenSecret());
			Log.d("SECRET", accessToken.getTokenSecret().toString());
			// Store AccessToken values in this Map
			tokens.put(accessToken.getToken(), accessToken.getTokenSecret());
			// Store login status - true if all Preferences are saved
			if (count == keySecret.size()) {
				e.putBoolean(PREF_KEY_TWITTER_LOGIN, true);
				Intent i = new Intent(getBaseContext(), Get_Followers.class);
				startActivity(i);
			}
			e.commit(); // save changes
	     }	
	}
	
	private boolean isTwitterLoggedInAlready() {
		// return twitter login status from Shared Preferences
        return mSharedPreferences.getBoolean(PREF_KEY_TWITTER_LOGIN, false);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}