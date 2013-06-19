package mit.edu.obmg.glassheat;

import mit.edu.obmg.glassheat.tasks.GlassHeatReader;
import mit.edu.obmg.glassheat.tasks.IOIOBGService;
import mit.edu.obmg.glassheat.tasks.IOIOBGService.LocalBinder;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.view.Menu;

public class MainActivity extends Activity {
	private IOIOBGService mIOIOService;
	private boolean mBounded = false;

	private static final String ML_GLASS = "http://tagnet.media.mit.edu/rfid/api/rfid_info"; 
    public final int mCheckInterval = 30000; // 5 minutes = 300000, 2 min = 120000

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		startService(new Intent(this,IOIOBGService.class));

		final Handler checkHandler = new Handler();
		checkHandler.postDelayed(new Runnable() { 
			@Override
			public void run() {
				// on launch of activity we execute an async task 
				checkMLGlass(); 
				checkHandler.postDelayed(this, mCheckInterval);
			}
		}, 1000);

	}

	@Override
	protected void onStart() {
		super.onStart();
		
		Intent mIntent = new Intent(this, IOIOBGService.class);
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
		stopService((new Intent(this, IOIOBGService.class)));
		super.onDestroy(); 
	}

	ServiceConnection mConnection = new ServiceConnection() { 
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mBounded = true;
			LocalBinder mLocalBinder = (LocalBinder)service;
			mIOIOService = mLocalBinder.getServerInstance();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mBounded = false;
			mIOIOService = null;
		}
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	private void checkMLGlass(){
		GlassHeatReader glassCheck = new GlassHeatReader(MainActivity.this);
		glassCheck.execute(ML_GLASS, Integer.toString(mCheckInterval));
	}
	
	public void handleGlass(){
		
	}
}
