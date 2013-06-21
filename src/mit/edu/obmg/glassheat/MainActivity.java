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

public class MainActivity extends IOIOActivity implements OnSeekBarChangeListener{
	private static final String TAG = "HeatGlass";

	//UI
	private ToggleButton button_;

	//Glass check
	private static final String ML_GLASS = "http://tagnet.media.mit.edu/rfid/api/rfid_info";
	public final int mCheckInterval = 30000; // 5 minutes = 300000, 2 min = 120000
	private TextView mfoundMe;

	//Heat FeedBack
	private final int mOutHeatPin = 34;
	private final int mPWMFreq = 100;
	private final int POLLING_DELAY = 150;
	//HeatBar UI
	private SeekBar mHeatBar;	
	private int mHeatValue;
	private TextView mHeatText;
	private long mLastChange;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		button_ = (ToggleButton) findViewById(R.id.button);

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
	protected void onStop() {
		super.onStop();
	}

	/**
	 * This is the thread on which all the IOIO activity happens. It will be run
	 * every time the application is resumed and aborted when it is paused. The
	 * method setup() will be called right after a connection with the IOIO has
	 * been established (which might happen several times!). Then, loop() will
	 * be called repetitively until the IOIO gets disconnected.
	 */
	class Looper extends BaseIOIOLooper {
		/** The on-board LED. */
		private DigitalOutput led_;

		//Heat
		private PwmOutput mHeatPWM;

		private PwmOutput mGreenLed;

		/**
		 * Called every time a connection with IOIO has been established.
		 * Typically used to open pins.
		 * 
		 * @throws ConnectionLostException
		 *             When IOIO connection is lost.
		 * 
		 * @see ioio.lib.util.AbstractIOIOActivity.IOIOThread#setup()
		 */
		@Override
		protected void setup() throws ConnectionLostException {
			led_ = ioio_.openDigitalOutput(0, true);
			mHeatPWM = ioio_.openPwmOutput(mOutHeatPin, mPWMFreq);


		}

		/**
		 * Called repetitively while the IOIO is connected.
		 * 
		 * @throws ConnectionLostException
		 *             When IOIO connection is lost.
		 * 
		 * @see ioio.lib.util.AbstractIOIOActivity.IOIOThread#loop()
		 */
		@Override
		public void loop() throws ConnectionLostException {
			led_.write(!button_.isChecked());

			try {
				//mHeatValue = mHeatBar.getProgress();
				mHeatPWM.setPulseWidth(mHeatValue*100);
				Log.i(TAG, "setPulseWidth: "+ mHeatValue*100);

				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
		}
	}

	/**
	 * A method to create our IOIO thread.
	 * 
	 * @see ioio.lib.util.AbstractIOIOActivity#createIOIOThread()
	 */
	@Override
	protected IOIOLooper createIOIOLooper() {
		return new Looper();
	}

	private void checkMLGlass(){
		GlassHeatReader glassCheck = new GlassHeatReader(MainActivity.this);
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
