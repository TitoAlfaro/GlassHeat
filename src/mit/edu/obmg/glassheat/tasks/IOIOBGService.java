/**
 * Written by Santiago Alfaro and Micah Rye
 */

package mit.edu.obmg.glassheat.tasks;

import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOService;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

public class IOIOBGService extends IOIOService{
	private boolean mIOIOConnected = false;

	@Override
	protected IOIOLooper createIOIOLooper() {
		/**
		 * This is the thread on which all the IOIO activity happens. It will be run
		 * every time the application is resumed and aborted when it is paused. The
		 * method setup() will be called right after a connection with the IOIO has
		 * been established (which might happen several times!). Then, loop() will
		 * be called repetitively until the IOIO gets disconnected.
		 */
		return new BaseIOIOLooper() {

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
			protected void setup() throws ConnectionLostException,
					InterruptedException {
				mIOIOConnected = true; 
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
			public void loop() throws ConnectionLostException, InterruptedException {
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					ioio_.disconnect();
				}
			}
		};
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
	}

	public IBinder mBinder = new LocalBinder();

	public IBinder onBind(Intent arg0) {
		return mBinder;
	}
	public class LocalBinder extends Binder{

		public IOIOBGService getServerInstance() {
			return IOIOBGService.this;
		}
	}

	public boolean isConnected(){
		return this.mIOIOConnected;
	}
}