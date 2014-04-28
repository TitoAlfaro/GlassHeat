package mit.edu.obmg.glassheat;

import java.util.Calendar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import mit.edu.obmg.glassheat.ioio.IOIOGlassHeatService;
import mit.edu.obmg.glassheat.ioio.IOIOGlassHeatService.LocalBinder;
import mit.edu.obmg.glassheat.net.AsyncGetJSONTask;
import mit.edu.obmg.glassheat.net.Wifi;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class GlassHeatActivity extends Activity implements 	OnClickListener,
OnSeekBarChangeListener {
	private static final String TAG = GlassHeatActivity.class.getSimpleName(); 

	//UI
	private ToggleButton mDebugButton;
	private boolean mDebugState;

	private IOIOGlassHeatService mIOIOService;
	private boolean mBounded = false;  

	//Glass check
	private static final String ML_CLASS_URL = "http://tagnet.media.mit.edu/rfid/api/rfid_info";
	private static final String HIDEN_GLASS_URL = "http://18.85.54.205/glass";

	public final int mCheckInterval = 5000; // 5 minutes = 300000, 2 min = 120000
	public final int EXTRA_SHORT_INTERVAL = 2000; 
	public final int SHORT_INTERVAL = 30000; 
	public final int MEDIUM_INTERVAL = 120000; 
	public final int LONG_INTERVAL = 300000;  // 5 minutes
	public final int EXTRA_LONG_INTERVAL = 600000; 

	private TextView mfoundMe;
	private static final int WIFI_OFF = 500;
	private Wifi mWifi;
	public WifiManager titoWiFi;
	//Heat FeedBack
	private final int POLLING_DELAY = 150;
	public int heatDistance;

	//HeatBar UI
	private SeekBar mHeatBar;	
	private int mHeatValue;
	private TextView mHeatText;
	private long mLastChange;

	private Handler mCheckMLGlasshandler; 
	private Runnable mCheckMLGlassRunnable; 
	private Handler mCheckHiddenGlasshandler; 
	private Runnable mCheckHiddenGlassRunnable; 
	private Handler mAsyncHandler; 

	private MLGlass mGlass;

	private String mCurrentLocationGlassId;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		startService(new Intent(this, IOIOGlassHeatService.class));

		//setContentView(R.layout.activity_main);
		setContentView(R.layout.activity_main);
		mDebugButton = (ToggleButton) findViewById(R.id.button);
		mDebugButton.setOnClickListener(this);

		mGlass = new MLGlass(); 
		//Glass
		mfoundMe = (TextView) findViewById(R.id.found_you);
		mfoundMe.setVisibility(View.INVISIBLE);

		//Heat
		mHeatText = (TextView) findViewById(R.id.seekBarText);
		mHeatBar = (SeekBar) findViewById(R.id.seekBarHeat);
		mHeatBar.setEnabled(false);
		//mHeatBar.setClickable(false);
		mHeatBar.setOnSeekBarChangeListener(this);


		/*  You current location glass id should be your 'i' index of the matrix distanceMatrix[i][j]
		 *  the location you want to be at should be 'j'. The value distanceMatrix[i][j] will be the
		 *  distance from current location 'i' to desired location 'j'. 
		 */

		/* LETS make sure we have wifi connection... WHAT IF WE DONT? 
		 * HOW long does it take to turn on? should we wait and then start checking
		 * glass? 
		 */
		//mWifi = new Wifi(this.getApplicationContext()); 
		//mWifi.turnWifiOn(); 

		ConnectivityManager connectivityManager = (ConnectivityManager)
				this.getSystemService(Context.CONNECTIVITY_SERVICE);
		titoWiFi = (WifiManager)this.getSystemService(Context.WIFI_SERVICE);

		/*if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI) == null){
			dealWithWiFi(titoWiFi, connectivityManager);
		}*/

		//dealWithWiFi(titoWiFi, connectivityManager);

		//if (titoWiFi.isWifiEnabled() == false)dealWithWiFi(titoWiFi);

		/*mAsyncHandler = new Handler(){
			@Override
			public void handleMessage(Message msg){
				switch (msg.what) {
				case WIFI_OFF:
					mWifi.turnWifiOn();
					break;
				}
			}
		};*/


		mCheckMLGlasshandler = new Handler(); 
		mCheckMLGlasshandler.postDelayed(mCheckMLGlassRunnable = new Runnable() { 
			@Override
			public void run() {
				checkMLGlass(); 
				// HAVE to remember that doing lots of HTTP (wifi) calls uses up battery. 
				mCheckMLGlasshandler.postDelayed(this, EXTRA_SHORT_INTERVAL);
			}
		}, 1000);


		mCheckHiddenGlasshandler = new Handler();
		mCheckHiddenGlasshandler.postDelayed(mCheckHiddenGlassRunnable = new Runnable() { 
			@Override
			public void run() {
				checkHiddenGlass(); 
				mCheckHiddenGlasshandler.postDelayed(this, LONG_INTERVAL);
			}
		}, 1000);


		/*
		 * mCheckHiddenGlasshandler = new Handler();
		mCheckHiddenGlasshandler.postDelayed(mCheckHiddenGlassRunnable = new Runnable() { 
			@Override
			public void run() {
				if hidden glass and I know where I am at check the distance
				and assign to heat
				mIOIOService.setHeatBarValue( getDistance() );
			}
		}, 1000);
		 */

	}

	private void dealWithWiFi(WifiManager wifi, ConnectivityManager manager) {
		Log.d(TAG,"WiFi: " + wifi.isWifiEnabled());
		Log.i(TAG, "resetting WiFi");
		NetworkInfo networkInfo = null;
		networkInfo = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		if (wifi.isWifiEnabled() == false){
			wifi.setWifiEnabled(true);
			while (networkInfo == null){

				Log.d(TAG,"No connection yet");
			}
		}else{
			wifi.setWifiEnabled(false);
			Log.i(TAG, "WiFi off");
			wifi.setWifiEnabled(true);
			Log.d(TAG,"WiFi: " + wifi.isWifiEnabled());
			while (networkInfo == null){

				Log.d(TAG,"No connection yet");
			}
		}

	}


	/*
	 * Connect to IOIOGlassHeatService
	 */
	ServiceConnection mConnection = new ServiceConnection() { 
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			Toast.makeText(GlassHeatActivity.this, "Service is connected", 1000).show();
			mBounded = true;
			LocalBinder mLocalBinder = (LocalBinder)service;
			mIOIOService = mLocalBinder.getServerInstance(); 
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			Toast.makeText(GlassHeatActivity.this, "Service is disconnected", 
					Toast.LENGTH_SHORT).show();
			mBounded = false;
			mIOIOService = null;
		}
	};

	@Override
	protected void onStart() {
		super.onStart();
		Intent mIntent = new Intent(this, IOIOGlassHeatService.class);
		//obtain a persistent connection to a service. Likewise, creates the service
		//if not already running (calling service's onCreate).
		bindService(mIntent, mConnection, BIND_AUTO_CREATE);
	}

	@Override
	protected void onStop() {
		super.onStop();
		if(mBounded) {
			// remove persistent connection to service. If no other binds to the service 
			// the services onDestory will be called. 
			unbindService(mConnection);
			mBounded = false;
		}
	}

	@Override
	protected void onDestroy(){
		//stop the service on exit of program
		//stopService((new Intent(this, IOIOFoodcamService.class)));
		// make sure we stop runnable when exiting app. 
		mCheckMLGlasshandler.removeCallbacks(mCheckMLGlassRunnable);
		mCheckHiddenGlasshandler.removeCallbacks(mCheckHiddenGlassRunnable);
		stopService(new Intent(this,IOIOGlassHeatService.class));
		super.onDestroy(); 
	}

	private int checkMLGlass(){
		AsyncGetJSONTask mlGlassCheck = new AsyncGetJSONTask(){

			@Override
			protected void onPostExecute(JSONObject result){
				mGlass.handleMLGlassJSONResults(result);
				String g = mGlass.locationOf(mGlass.tito2); 
				int distance = -2;
				if(mGlass.mHiddenGlassId != "none"){
					distance = mGlass.getDistance(g, mGlass.mHiddenGlassId );
				}
				
				if(distance == 50){
					heatDistance = 100;
				}else if(distance == 40){
					heatDistance = 200;
				}else if(distance == 30){
					heatDistance = 300;
				}else if(distance == 20){
					heatDistance = 400;
				}else if(distance == 10){
					heatDistance = 500;
				}else if(distance == 6){
					heatDistance = 1000;
				}else if(distance == 5){
					heatDistance = 1100;
				}else if(distance == 4){
					heatDistance = 1200;
				}else if(distance == 3){
					heatDistance = 1300;
				}else if(distance == 2){
					heatDistance = 1400;
				}else if(distance == 1){
					heatDistance = 1500;
				}else if(distance == 0){
					heatDistance = 1600;
				}
				// SET heat value in IOIOService 
				if (mDebugState == true){
					mHeatBar.setEnabled(true);
					mHeatValue = mHeatBar.getProgress() * 2;
				}else{
					mHeatBar.setEnabled(false);
					mHeatValue = heatDistance;
					mHeatBar.setProgress(0);
				}
				mHeatText.setText("Heat Value: " + mHeatValue);
				mIOIOService.setHeatValue(mHeatValue);
				
				
				Toast.makeText(GlassHeatActivity.this,"Found you at: "+g+", distance = "+distance, Toast.LENGTH_SHORT).show();
				/*
				 * IF we have the hidden glass id then 
				 * we want to check distance btn current location "g'"
				 * and hidden glass. 
				 * getDistanctBtn('e14-114-1', 'e15-224-2')
				 */


				Log.d(TAG, "glass loc: " + g);
			}
		}; 
		mlGlassCheck.handler = mAsyncHandler; 
		mlGlassCheck.execute(ML_CLASS_URL, Integer.toString(mCheckInterval));

		return heatDistance;
	}

	private void checkHiddenGlass(){
		AsyncGetJSONTask hiddenGlassCheck = new AsyncGetJSONTask(){

			@Override
			protected void onPostExecute(JSONObject result){
				mGlass.handleHiddenGlassJSONResults(result);

				//mHiddenGlass = result
			}
		}; 
		hiddenGlassCheck.handler = mAsyncHandler; 
		hiddenGlassCheck.execute(HIDEN_GLASS_URL, Integer.toString(mCheckInterval));
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, 	int progress, boolean fromUser) {

		if (System.currentTimeMillis() - mLastChange > POLLING_DELAY) {
			//handleHeat(seekBar);
			mLastChange = System.currentTimeMillis();
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		mLastChange = System.currentTimeMillis();

	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		//handleHeat(seekBar);

	}

	/*
	private void handleHeat(final SeekBar seekBar){
		mHeatValue = seekBar.getProgress();

		if (mDebugState == true){
			mHeatText.setText("Heat Value: " + mHeatValue*100);
			mIOIOService.setHeatBarValue(mHeatValue);
		}else{
			mHeatText.setText("Heat Value: " + heatDistance);
			mIOIOService.setHeatBarValue(heatDistance);
		}
	}
    */
	
	@Override
	public void onClick(View v) {
		switch (v.getId()){

		case R.id.button:
			if (mDebugState == true){
				mDebugState = false;
				mDebugButton.setChecked(mDebugState);
				mDebugButton.setText("Debug Off");
				try{
					mIOIOService.setLED(true);
				}catch(Exception e){
					e.printStackTrace();
				}
			}else{
				mDebugState = true;
				mDebugButton.setChecked(mDebugState);
				mDebugButton.setText("Debug On");
				try{
					mIOIOService.setLED(false);
				}catch(Exception e){
					e.printStackTrace();
				}
			}
			break;
		}		
	}
}
