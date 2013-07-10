package mit.edu.obmg.glassheat;

import java.util.Calendar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import mit.edu.obmg.glassheat.ioio.IOIOGlassHeatService;
import mit.edu.obmg.glassheat.ioio.IOIOGlassHeatService.LocalBinder;
import mit.edu.obmg.glassheat.net.AsyncGetJSONTask;
import mit.edu.obmg.glassheat.net.Wifi;


import mit.edu.obmg.glassheat.tasks.GlassHeatReader;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
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

public class GlassHeatActivity extends Activity implements OnSeekBarChangeListener {
	private static final String TAG = GlassHeatActivity.class.getSimpleName(); 

	//UI
	private ToggleButton mDebugButton;

	private IOIOGlassHeatService mIOIOService;
	private boolean mBounded = false;  
	
	//Glass check
	private static final String ML_CLASS_URL = "http://tagnet.media.mit.edu/rfid/api/rfid_info";
	private static final String HIDEN_GLASS_URL = "http://18.85.54.205/glass";
	
	public final int mCheckInterval = 5000; // 5 minutes = 300000, 2 min = 120000
	public final int EXTRA_SHORT_INTERVAL = 5000; 
	public final int SHORT_INTERVAL = 30000; 
	public final int MEDIUM_INTERVAL = 120000; 
	public final int LONG_INTERVAL = 300000;  // 5 minutes
	public final int EXTRA_LONG_INTERVAL = 600000; 
	
	private TextView mfoundMe;
	private static final int WIFI_OFF = 500;
	private Wifi mWifi;  

	//Heat FeedBack
	private final int mOutHeatPin = 34;
	private final int mPWMFreq = 100;
	private final int POLLING_DELAY = 150;
	private final int HEAT_VALUE_MULTIPLIER = 100;
	
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
	
	/* Glass IDs
	 * e14-140-2, e14-151-1, e14-245-1, e14-251-1, e14-274-1, e14-333-1, e14-348-1, e14-445-1, e14-474-1, 
	 * e14-514-1, e14-514-2, e14-525-1, e14-548-1, e14-674-1, e15-003-1, e15-100-1, e15-200-1, e15-300-1, 
	 * e15-344-1, e15-383-1, e15-400-1, e15-443-1, e15-468-1
	 */
	
	private String mHiddenGlassId;
	private String mCurrentLocationGlassId;
	private int[][] distanceMatrix; 
	 
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		startService(new Intent(this, IOIOGlassHeatService.class));
				
		setContentView(R.layout.activity_main);
		mDebugButton = (ToggleButton) findViewById(R.id.button);
		mGlass = new MLGlass(); 
		//Glass
		mfoundMe = (TextView) findViewById(R.id.found_you);
		mfoundMe.setVisibility(View.INVISIBLE);

		//Heat
		mHeatText = (TextView) findViewById(R.id.seekBarText);
		mHeatBar = (SeekBar) findViewById(R.id.seekBarHeat);
		mHeatBar.setOnSeekBarChangeListener(this);
		mHeatBar.setProgress(0);

		distanceMatrix = new int[23][23]; 
		for(int i = 0; i < 23; i++){
			for(int j = 0; j < 23; j++){
				if(i == j){
					distanceMatrix[i][j] = 0; 
				}else{
					distanceMatrix[i][j] = i+j; 
				}
			}
		}
	    /*  You current location glass id should be your 'i' index of the matrix distanceMatrix[i][j]
	     *  the location you want to be at should be 'j'. The value distanceMatrix[i][j] will be the
	     *  distance from current location 'i' to desired location 'j'. 
	     */
		
		/* LETS make sure we have wifi connection... WHAT IF WE DONT? 
		 * HOW long does it take to turn on? should we wait and then start checking
		 * glass? 
		 */
		mWifi = new Wifi(this.getApplicationContext()); 
		mWifi.turnWifiOn(); 

		mAsyncHandler = new Handler(){
			@Override
			public void handleMessage(Message msg){
				switch (msg.what) {
				case WIFI_OFF:
					mWifi.turnWifiOn();
					break;
				}
			}
		};
		
		mCheckMLGlasshandler = new Handler(); 
		mCheckMLGlasshandler.postDelayed(mCheckMLGlassRunnable = new Runnable() { 
			@Override
			public void run() {
				checkMLGlass(); 
				// HAVE to remember that doing lots of HTTP (wifi) calls uses up battery. 
				mCheckMLGlasshandler.postDelayed(this, SHORT_INTERVAL);
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
        bindService(mIntent, mConnection, BIND_AUTO_CREATE);
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		if(mBounded) {
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
		super.onDestroy(); 
	}

	private void checkMLGlass(){
		AsyncGetJSONTask mlGlassCheck = new AsyncGetJSONTask(){
			
			@Override
			protected void onPostExecute(JSONObject result){
				mGlass.handleMLGlassJSONResults(result);
			}
		}; 
		mlGlassCheck.handler = mAsyncHandler; 
		mlGlassCheck.execute(ML_CLASS_URL, Integer.toString(mCheckInterval));
	}
	
	private void checkHiddenGlass(){
		AsyncGetJSONTask hiddenGlassCheck = new AsyncGetJSONTask(){
			
			@Override
			protected void onPostExecute(JSONObject result){
				mGlass.handleHiddenGlassJSONResults(result);
			}
		}; 
		hiddenGlassCheck.handler = mAsyncHandler; 
		hiddenGlassCheck.execute(HIDEN_GLASS_URL, Integer.toString(mCheckInterval));
	}
	
	@Override
	public void onProgressChanged(SeekBar seekBar, 	int progress, boolean fromUser) {

		if (System.currentTimeMillis() - mLastChange > POLLING_DELAY) {
			handleHeat(seekBar);
			mLastChange = System.currentTimeMillis();
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		mLastChange = System.currentTimeMillis();

	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		handleHeat(seekBar);

	}

	private void handleHeat(final SeekBar seekBar){
		//mHeatValue = seekBar.getProgress();
		mHeatText.setText("Heat Value: " + mHeatValue*100);
	}
	
	// NOTE: Tito had this in GlassHeatMain but it was not being called
	// delete if not needed. 
	public void handleGlass(String report){
		String hide = "e14-348-1";
		if (hide.equals(report)){
			mfoundMe.setVisibility(View.VISIBLE);
			mfoundMe.setText("you found Me");
			mHeatValue = 80;
		}else{
			mfoundMe.setVisibility(View.VISIBLE);
			mfoundMe.setText("Keep Looking");
			mHeatValue = 20;
		}
	}

}
