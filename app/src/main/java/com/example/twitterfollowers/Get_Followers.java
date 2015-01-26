package com.example.twitterfollowers;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.jirbo.adcolony.AdColony;
import com.jirbo.adcolony.AdColonyV4VCAd;
import com.jirbo.adcolony.AdColonyV4VCListener;
import com.jirbo.adcolony.AdColonyV4VCReward;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import twitter4j.IDs;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Trend;
import twitter4j.Trends;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.conf.ConfigurationBuilder;

public class Get_Followers extends ActionBarActivity {

	//Algorithim  values
	static int querycount = 15;
	static int MAX_USERS_FOLLOWING = 15;
	static int totaltoday;
	static double SCORE = 1.0;
	static int MAX_INT;
	
	//List of twitter instances
	ArrayList<Twitter> twitters;
	
	//Map of twitter instances and corresponding Query values
	HashMap<Twitter, String> twitterz;
	//Shared Preferences
	SharedPreferences sp;
	
	//Alarm to wake up the CPU
	private PendingIntent pendingIntent;
	private AlarmManager manager;
	
	//list of people that user is following
	static ArrayList<Long> following;
	
	//Title
	TextView title;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_get__followers);
		//setting font
		title = (TextView) this.findViewById(R.id.Followers_title);
		Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/thin.ttf");
		title.setTypeface(typeface);
		
		// Retrieve a PendingIntent that will perform a broadcast
	    Intent alarmIntent = new Intent(this, AlarmReceiver.class);
	    pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // SharedPreferences
        sp = getApplicationContext().getSharedPreferences("MyPref", 0);

        //A list that contains twitter instances
        twitters = new ArrayList<Twitter>();
        following = new ArrayList<Long>();
        getFollowing();
        twitterz = new HashMap<Twitter, String>();
        totaltoday = sp.getInt("totaltoday", 0);

        //listener for dump following button
        Button dumper = (Button) findViewById(R.id.dump);
        dumper.setOnClickListener(new View.OnClickListener()    {
            @Override
            public void onClick(View v) {
                AlertDialog alertDialog = new AlertDialog.Builder(Get_Followers.this).create(); //Read Update
                alertDialog.setTitle("Confirm");
                alertDialog.setMessage("Are you sure you want to dump all your following? Note this will not cause the app to stop functioning.");
                alertDialog.setButton(Dialog.BUTTON_POSITIVE, "Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new DumpTask().execute(null, null, null);
                        dialog.cancel();
                    }
                });
                alertDialog.setButton(Dialog.BUTTON_NEGATIVE, "No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                alertDialog.show();
            }
        });
        //setting up listener for ad button
        Button adButton = (Button) findViewById(R.id.AD);
        adButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // TODO Auto-generated method stub
                AdColonyV4VCAd ad = new AdColonyV4VCAd("vzab3154251cbe458488");
                ad.show();
            }
        });
        MAX_INT = sp.getInt("MAX_INT", 500);
        AdColonyV4VCListener listener = new AdColonyV4VCListener()
        {
            public void onAdColonyV4VCReward(AdColonyV4VCReward reward)
            {
                //Just an example, see API Details page for more information.
                if(reward.success())
                {
                    if(MAX_INT<950)	{
                        MAX_INT = MAX_INT+20;
                        Editor edit = sp.edit();
                        edit.putInt("MAX_INT", MAX_INT);
                        edit.commit();
                        Log.d("MAX_INT", "MAX_INT IS NOW "+MAX_INT);
                    }
                    else	{
                        Toast.makeText(getApplicationContext(), "Try again in 24 hours", Toast.LENGTH_SHORT).show();
                    }
                    Log.d("Rewarded", "rewarded");
                }
            }
        };

        AdColony.addV4VCListener(listener);

	    boolean alarmUp = sp.getBoolean("alarmOn", false);

	    if (alarmUp == true)
	    {
	    	Log.d("alarm", "alarmUp is true");
	    	Intent intent = getIntent();
	    	boolean fromAlarm = intent.getBooleanExtra("fromAlarm", false);
	    	if(fromAlarm == false)	{
	    		Log.d("alarm", "from alarm is false");
	    		return;
	    	}
	    	else	{
	    		Log.d("myTag", "Alarm is already active");

	    	}
	    }
	    else	{
	    	Log.d("alarm", "alarmUp is false");
	    	//sets alarm every 15 minutes
			manager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
			manager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), AlarmManager.INTERVAL_FIFTEEN_MINUTES, pendingIntent);
	    }


		long time = sp.getLong("time", 0);
		//if its not a new day
		if( time == 0)	{
			time = new Date().getTime();
			Editor edit = sp.edit();
			edit.putLong("time", time);
			edit.commit();
		}
		else	{
			long currentTime = new Date().getTime();
			//checking if its a new day
			if(currentTime - time >= 24*60*60*1000) 
			{   // if its a new day
			    // update cycleDay
			    time = currentTime;
			    Editor edit =sp.edit();
			    edit.putLong("time", time);
			    totaltoday = 0;
			    edit.putInt("totaltoday", totaltoday);
			    
			    MAX_INT = 500;
			    edit.putInt("MAX_INT", MAX_INT);
			    edit.commit();
			}
		}		
		int count = sp.getInt("count", 0);
		if(count != 0)	{
			Log.d("Get count", "Count is "+count);
			for(int x = 1; x<= count; x++)	{	
				ConfigurationBuilder builder = new ConfigurationBuilder();
				builder.setOAuthConsumerKey(MainActivity.keys.get(x-1));
				builder.setOAuthConsumerSecret(MainActivity.values.get(x-1));
				AccessToken accessToken = new AccessToken(sp.getString(MainActivity.PREF_KEY_OAUTH_TOKEN+x, ""), sp.getString(MainActivity.PREF_KEY_OAUTH_SECRET+x, ""));
				Twitter twitter = new TwitterFactory(builder.build()).getInstance(accessToken);
	            twitters.add(twitter);
			}
			Toast.makeText(getApplicationContext(), "Getting you followers this may take a while", Toast.LENGTH_LONG).show();
			// Sets the MAX_USERS_FOLLOWING depending on how many keys their are.
			MAX_USERS_FOLLOWING = MAX_USERS_FOLLOWING *twitters.size();
			Twitter[] twittera = new Twitter[twitters.size()];
			twitters.toArray(twittera);
			new Tends().execute(twittera);
		}
		else	{
			Log.e("ERROR", "An error has been encountered");
		}
	}
	
	private class Tends extends AsyncTask<Twitter, Void, Void>	{

		@Override
		protected Void doInBackground(Twitter... twitters) {
			// TODO Auto-generated method stub
			Twitter twitter = twitters[0];
			try {
				Trends trends = twitter.getPlaceTrends(1);
				Trend[] trendz = trends.getTrends();

				for(int x = 0; x<twitters.length; x++)	{
					twitterz.put(twitters[x], trendz[x].getQuery());
				}
			} catch (TwitterException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
		
		@Override
		 protected void onPostExecute(Void voids) {
	         looper();
	     }
	}
	
	private void looper() {
		if(!(totaltoday >= 1000))	{	
			for(int x =0; x < twitterz.size(); x++)	{
				Log.d("KEYS", "This has run "+x+" times");
				TaskParams params = new TaskParams(twitters.get(x), twitterz.get(twitters.get(x)));
				new GetFollowers().execute(params);
			}
		}
	}

	
	public static class TaskParams {
	    String query;
	    Twitter twitter;

	    TaskParams(Twitter twitter, String query) {
	        this.twitter = twitter;
	        this.query = query;
	    }
	}
	
	private class GetFollowers extends AsyncTask<TaskParams, Void, Void>	{

		@Override
		protected Void doInBackground(TaskParams... params) {
			// TODO Auto-generated method stub
			Log.d("TEST", "HOW MANY TIMES DOES THIS RUN?");
			Twitter twitter =  params[0].twitter;
			String querys = params[0].query;
			Query query = new Query(querys);
            query.setCount(querycount);
            try {
				QueryResult result = twitter.search(query);
				for(twitter4j.Status stat : result.getTweets())	{
					User user = stat.getUser();
	            	if(ShouldFollow(user, twitter))	{
	            		// IF following too many people  at once un-follow last person
	            		if(following.size() > MAX_USERS_FOLLOWING)	{
	            			Long id = following.get(0);
	            			following.remove(id);
	            			twitter.destroyFriendship(id);
	            		}
	            		long id = user.getId();
	            		twitter.createFriendship(id);
	            		following.add(id);
	            		SaveFollowing();
	            		twitter.createMute(id);
	            		totaltoday++;            
	            		Editor edit = sp.edit();
	            		edit.putInt("totaltoday", totaltoday);
	            		edit.commit();
	            	}
				}
			} catch (TwitterException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
			return null;
		}
	}

   private class DumpTask extends AsyncTask<Void, Void, Void> {
       @Override
       protected Void doInBackground(Void... voids) {
           //building twitter using the first key
           ConfigurationBuilder builder = new ConfigurationBuilder();
           builder.setOAuthConsumerKey(MainActivity.keys.get(1-1));
           builder.setOAuthConsumerSecret(MainActivity.values.get(1-1));
           AccessToken accessToken = new AccessToken(sp.getString(MainActivity.PREF_KEY_OAUTH_TOKEN+1, ""), sp.getString(MainActivity.PREF_KEY_OAUTH_SECRET+1, ""));
           Twitter twitter = new TwitterFactory(builder.build()).getInstance(accessToken);
           for(int x = 0; x<following.size(); x++) {
               try {
                   twitter.destroyFriendship(following.get(x));
               }
               catch(TwitterException t)   {
                   t.printStackTrace();
               }
           }
           following.clear();
           SaveFollowing();
           return null;
       }
       @Override
       protected void onPostExecute(Void v) {
            Toast.makeText(getApplicationContext(), "Fake following has been cleared", Toast.LENGTH_SHORT).show();
       }
   }
	
	public void SaveFollowing()	{
		String ser = SerializeObject.objectToString(following);
		if (ser != null && !ser.equalsIgnoreCase("")) {
		    SerializeObject.WriteSettings(getApplicationContext(), ser, "following.dat");
		} else {
		    SerializeObject.WriteSettings(getApplicationContext(), "", "following.dat");
		}
	}
	
	public void getFollowing()	{
		String ser = SerializeObject.ReadSettings(getApplicationContext(), "following.dat");
		if (ser != null && !ser.equalsIgnoreCase("")) {
		    Object obj = SerializeObject.stringToObject(ser);
		    // Then cast it to your object and 
		    if (obj instanceof ArrayList) {
		        // Do something
		    	try	{
		    		following = (ArrayList<Long>)obj;
		    	}
		    	catch(Exception e)	{
		    		Log.e("ERROR", "Error getting followers "+e.getMessage().toString());
		    	}
		    }
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.get__followers, menu);
		return true;
	}

	public boolean ShouldFollow(User user, Twitter twitter) {
		//function that calculates if we need to follow a given user.
		double followers = user.getFollowersCount();
    	Log.d("followers", followers+"");
    	if(followers == 0)	{
    		Log.d("No followers", "User has no followers");
    		return false;
    	}
		double friends = user.getFriendsCount();
		double score = friends/followers;
		try {
			IDs foids = twitter.getFollowersIDs(user.getId(), -1);
			long[] fola = foids.getIDs();
			IDs finds = twitter.getFriendsIDs(user.getId(), -1);
			long[] finga = finds.getIDs();
			
			double percent = 0;
			for(int lows=0; lows<fola.length; lows++)	{
				for(int lings=0; lings<finga.length; lings++)	{
					if(fola[lows] == finga[lings])	{
						percent++;
					}
				}
			}
			percent = percent / followers;
			if(percent<.7)	{
				Log.d("Not following user ", user.getScreenName());
				Log.d("", "");	
				return false;
			}
			score = score*percent;
			if(user.isVerified()) score = score +.3;
		} catch (TwitterException e) {
			e.printStackTrace();
		}
		
		if(score > SCORE)	{
			Log.d("Following user ", user.getScreenName());
			Log.d("", "");
			return true;
		}
		else	{
			Log.d("Not following user ", user.getScreenName());
			Log.d("", "");
			return false;
		}
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

	@Override
	public void onPause() {
		super.onPause();
		AdColony.pause();
	}

	@Override
	public void onResume() {
		super.onResume();
		AdColony.resume(this);
	}

}
