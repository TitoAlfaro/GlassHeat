/*
 * Written by Santiago Alfaro,
 * based on the HelloIOIO code
 */
package mit.edu.obmg.glassheat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import mit.edu.obmg.glassheat.tasks.GlassHeatReader;
import ioio.lib.api.DigitalOutput;
import ioio.lib.api.PwmOutput;
import ioio.lib.api.Uart;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOActivity;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.ToggleButton;

public class GlassHeatMain extends IOIOActivity implements OnSeekBarChangeListener{
	private static final String TAG = "HeatGlassMain";

	//UI
	private ToggleButton mDebugButton;

	//Glass check
	private static final String ML_GLASS = "http://tagnet.media.mit.edu/rfid/api/rfid_info";
	public final int mCheckInterval = 5000; // 5 minutes = 300000, 2 min = 120000
	private TextView mfoundMe;

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


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mDebugButton = (ToggleButton) findViewById(R.id.button);

		//Glass
		mfoundMe = (TextView) findViewById(R.id.found_you);
		mfoundMe.setVisibility(View.INVISIBLE);

		//Heat
		mHeatText = (TextView) findViewById(R.id.seekBarText);
		mHeatBar = (SeekBar) findViewById(R.id.seekBarHeat);
		mHeatBar.setOnSeekBarChangeListener(this);
		mHeatBar.setProgress(0);


		final Handler checkHandler = new Handler();
		checkHandler.postDelayed(new Runnable() { 
			@Override
			public void run() {
				checkMLGlass(); 
				checkHandler.postDelayed(this, mCheckInterval);
			}
		}, 1000);

	}

	@Override
	protected void onStart() {
		super.onStart();
	}
	
	@Override
	protected void onStop() {
		super.onStop();
	}
	
	class Looper extends BaseIOIOLooper {
		/** The on-board LED. */
		private DigitalOutput mDebugLED;

		//Heat
		private PwmOutput mHeatPWM;
		
		@Override
		protected void setup() throws ConnectionLostException {
			mDebugLED = ioio_.openDigitalOutput(0, true);
			mHeatPWM = ioio_.openPwmOutput(mOutHeatPin, mPWMFreq);
		}

		@Override
		public void loop() throws ConnectionLostException {
			mDebugLED.write(!mDebugButton.isChecked());

			try {
				if (mDebugButton.isChecked()== true) mHeatValue = mHeatBar.getProgress();
				mHeatPWM.setPulseWidth(mHeatValue*HEAT_VALUE_MULTIPLIER);
				Log.i(TAG, "setPulseWidth: "+ mHeatValue*HEAT_VALUE_MULTIPLIER);

				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
		}
	}

	@Override
	protected IOIOLooper createIOIOLooper() {
		return new Looper();
	}
	private void checkMLGlass(){
		GlassHeatReader glassCheck = new GlassHeatReader(GlassHeatMain.this);
		glassCheck.execute(ML_GLASS, Integer.toString(mCheckInterval));
	}

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
}