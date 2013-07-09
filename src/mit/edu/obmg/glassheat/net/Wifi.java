package mit.edu.obmg.glassheat.net;

import android.content.Context;
import android.net.wifi.WifiManager;

public class Wifi {

	private WifiManager mWifi;
	
	public Wifi(Context ctx){
		mWifi = (WifiManager)ctx.getSystemService(Context.WIFI_SERVICE);
	}
	
	public boolean isWifiOn(){
		return mWifi.isWifiEnabled(); 
	}
	
	public void turnWifiOn(){
		if(mWifi.isWifiEnabled() == true){
    		// NOTE: ask Tito if there is a reason why he flips wifi enabled?
    		// FOR NOW I just return since on.
    		return; 
    		//mWifi.setWifiEnabled(false);
    		//mWifi.setWifiEnabled(true);
    	}else {
    		mWifi.setWifiEnabled(true);
    	}
	}
}
