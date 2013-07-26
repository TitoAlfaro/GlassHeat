package src.mit.edu.obmg.glassheat.ioio;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.app.Notification;
import android.app.NotificationManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import ioio.lib.api.DigitalOutput;
import ioio.lib.api.IOIO;
import ioio.lib.api.IOIOFactory;
import ioio.lib.api.PwmOutput;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.android.IOIOService;
import android.app.Notification;

import android.app.NotificationManager;

import android.support.v4.app.NotificationCompat; 
import android.util.Log;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;



public class IOIOGlassHeatService extends IOIOService {
	private static final String TAG = IOIOGlassHeatService.class.getSimpleName(); 
	private boolean mIOIOConnected = false; 
		
	/** The on-board LED. */
	private DigitalOutput mDebugLED = null; 
	//Heat
	private PwmOutput mHeatPWM;
	private static final int HEAT_PIN = 40;
	private static final int PWM_FREQ = 2000;	//In Hz. 2khz is recommended for Peltier
	private final int HEAT_VALUE_MULTIPLIER = 10;
	
	private int mHeatBarValue = 0; 
	
	private boolean mDebugging = false; 
	private IOIOGlassHeatService mIOIOGlassHeatService; 
	private NotificationManager mNotificationMngr; 
	private IOIO ioio = null;
	private boolean disconnect = false;
	
	/* TODO: finish later....
	 * NOTE: this may be to simple, may need it to be a dicontary or to contain a 
	 * structure, so that we can say if pin N is in/out A/D or if we want PwmOut etc...
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
	protected BaseIOIOLooper createIOIOLooper() {
		return new BaseIOIOLooper() {		

			@Override
			protected void setup() throws ConnectionLostException,
					InterruptedException {
				mIOIOConnected = true; 
				ioio = IOIOFactory.create(); 
				mDebugLED = ioio_.openDigitalOutput(0, true);
				mHeatPWM = ioio_.openPwmOutput(HEAT_PIN, PWM_FREQ);	
				
				Intent intent = new Intent("stop", null, mIOIOGlassHeatService, mIOIOGlassHeatService.getClass());
				PendingIntent pIntent = PendingIntent.getService(mIOIOGlassHeatService, 0, intent, 0);
				
				Notification noti = new NotificationCompat.Builder(mIOIOGlassHeatService)
				 .setContentIntent(pIntent)
		         .setContentTitle("Click to stop!!!")
		         .setContentText("IOIO service running")
		         .setSmallIcon(R.drawable.ic_launcher)
		         .build();

				mNotificationMngr.notify(0, noti);
			}

			@Override
			public void loop() throws ConnectionLostException,
			    InterruptedException {
				//things that you want to repeat over and over 
				Thread.sleep(20000);
				if( disconnect ){
					ioio.disconnect();
				}
				/*
				if (mDebugging == true){
					mHeatPWM.setPulseWidth(mHeatBarValue * HEAT_VALUE_MULTIPLIER);
					Log.i(TAG, "setPulseWidth: "+ mHeatBarValue * HEAT_VALUE_MULTIPLIER);
				}else{
					//mHeatPWM.setPulseWidth(mHeatBarValue * HEAT_VALUE_MULTIPLIER);
				}
				*/
			
			}
		};
	}
	

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		mIOIOGlassHeatService = this;
		mNotificationMngr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		if (intent != null && intent.getAction() != null
				&& intent.getAction().equals("stop")) {
			// User clicked the notification. Need to stop the service.
			mNotificationMngr.cancel(0);
			if(ioio != null){
				//we make sure to disconnect from the IOIO board when the service stops
				ioio.disconnect();
			}
			mIOIOConnected = false; 
			if(ioio != null){
				ioio.disconnect();
				ioio.disconnect();
				ioio.disconnect();
			}
			stopSelf();
			disconnect = true;
		}
	}
	
	@Override
	public void onDestroy(){
		if(ioio != null){
			//we make sure to disconnect from the IOIO board when the service stops
			ioio.disconnect();
			mNotificationMngr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			mNotificationMngr.cancel(0);
			disconnect = true;
		}
		super.onDestroy();
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
				if(state == true){
					mDebugging = false;
				}else mDebugging = true;
			}catch(ConnectionLostException e){
				e.printStackTrace(); 
				mIOIOConnected = false; 
			}
		}
	}
	
	public void setHeatBarValue(int heatValue){
		if(mIOIOConnected){
			//TODO: need check for proper heat value, what is the range? 
			Log.d(TAG, "setting heat to "+ heatValue);
			mHeatBarValue = heatValue;
		}
	}
}
