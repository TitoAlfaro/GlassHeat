package mit.edu.obmg.glassheat.ioio;


import java.lang.reflect.Method;

import mit.edu.obmg.glassheat.R;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import ioio.lib.api.DigitalOutput;
import ioio.lib.api.PwmOutput;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOService;

public class IOIOGlassHeatService extends IOIOService {
	private static final String TAG = IOIOGlassHeatService.class.getSimpleName(); 
	private boolean mIOIOConnected = false; 
	
	/** The on-board LED. */
	private DigitalOutput mDebugLED = null; 
	//Heat
	private PwmOutput mHeatPWM;
	private final int LED1_PIN = 1;
	private static final int HEAT_PIN = 34;
	private static final int PWM_FREQ = 10000;
	private final int HEAT_VALUE_MULTIPLIER = 100;
	
	
	
	private int mHeatBarValue = 0; 
	
	private boolean mDebugging = false; 
	
	/* TODO: finish later....
	 * NOTE: this may be to simple, may need it to be a dicontary or to contain a 
	 * structure, sot that we can say if pin N is in/out A/D or if we want PwmOut etc...
	 */
	private boolean[] mInput = new boolean[46]; // false means input...
	private boolean[] mOutput = new boolean[46]; // false means input...
	private boolean[] mPins = new boolean[46]; 
	public void initAllPinsFalse(){
		/*
		 * We initialize that there is no input or output 
		 * and all pins are off. 
		 */
		for(int i = 0; i < 46; i++) mInput[i] = false;
		for(int i = 0; i < 46; i++) mOutput[i] = false;
		// illegal to have mInput[i] == mOutput[j] == true, since cannot 
		// both be input and output for a pin... 
		for(int i = 0; i < 46; i++) mPins[i] = false; 
	}
	
	
	@Override
	protected IOIOLooper createIOIOLooper() {
		return new BaseIOIOLooper() {		

			@Override
			protected void setup() throws ConnectionLostException,
					InterruptedException {
				mIOIOConnected = true; 
				// TODO: make it so either digital in our out that is stored in 
				// a vector that can be called from a general purpose function later
				
				mDebugLED = ioio_.openDigitalOutput(0, true);
				mHeatPWM = ioio_.openPwmOutput(HEAT_PIN, PWM_FREQ);	
				
			}

			@Override
			public void loop() throws ConnectionLostException,
			    InterruptedException {
				
				//things that you want to repeat over and over 
				Thread.sleep(100);

				try {
					if (mDebugging == true){
						mHeatPWM.setPulseWidth(mHeatBarValue * HEAT_VALUE_MULTIPLIER);
						Log.i(TAG, "setPulseWidth: "+ mHeatBarValue * HEAT_VALUE_MULTIPLIER);
					} 
					Thread.sleep(100);
				} catch (InterruptedException e) {
				}
			}
		};
	}
	private boolean mLedState = false; 
	private boolean mPowerState = false; 
	private int mHeatState = 0;
	
	@SuppressWarnings("deprecation")
	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		if (intent != null && intent.getAction() != null
				&& intent.getAction().equals("stop")) {
			// User clicked the notification. Need to stop the service.
			nm.cancel(0);
			mIOIOConnected = false; 
			stopSelf();
		} else {
			// Service starting. Create a notification.
			@SuppressWarnings("deprecation")
			Notification notification = new Notification(
					R.drawable.ic_launcher, "IOIO service running",
					System.currentTimeMillis());
			// NOTE: notification deprecation, change in future. 
			notification
					.setLatestEventInfo(this, "IOIO Service", "Click to stop",
							PendingIntent.getService(this, 0, new Intent(
									"stop", null, this, this.getClass()), 0));
			notification.flags |= Notification.FLAG_ONGOING_EVENT;
			nm.notify(0, notification);
			
			/*
			Notification.Builder noti = new Notification.Builder(getBaseContext()); 
			
	         noti.setContentTitle("New mail from ")
	         .setContentText("IOIO service running")
	         .setSmallIcon(R.drawable.ic_launcher)
	         .build();
	         */
		}
	}

	public IBinder mBinder = new LocalBinder();
	
	@Override
	public IBinder onBind(Intent arg0) {
		return mBinder;
	}

	public class LocalBinder extends Binder{
		
		public IOIOGlassHeatService getServerInstance() {
			return IOIOGlassHeatService.this;
		}	
	}
	
	public boolean isConnected(){
		return this.mIOIOConnected;
	}
	
	public void setLED(boolean state){
		if(mIOIOConnected){
			try{
				mDebugLED.write(state);
			}catch(ConnectionLostException e){
				e.printStackTrace(); 
				mIOIOConnected = false; 
			}
		}
	}
	
	public void setHeatBarValue(int heatValue){
		if(mIOIOConnected){
				//TODO: need check for proper heat value, what is the range? 
				mHeatBarValue = heatValue;
		}
	}
}
