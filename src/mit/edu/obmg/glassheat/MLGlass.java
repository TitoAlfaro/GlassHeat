package src.mit.edu.obmg.glassheat;

import java.util.Calendar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.net.wifi.WifiManager;
import android.util.Log;

public class MLGlass {

	private static final String TAG = "MLGlass";
	
	// TODO: make better variables
	public static final String TAG_POLLERS = "pollers";
	public JSONArray pollers = null;
	public static final String TAG_NAME = "name";
	public JSONArray name = null;
	public static final String TAG_TAGS = "tags";
	public JSONObject tag = null;
	public Calendar currentTime = Calendar.getInstance();
	public String report;
    public String tito = "4D656469614C616243016115";
    public String tito2 = "4D656469614C616243014366";
    public String micah = "4D656469614C616243016447";
    public String sunny = "4D656469614C616243014441";
	
    
	public void handleMLGlassJSONResults(JSONObject result){
		if (result == null) return; 
		try{
    		pollers = result.getJSONArray(TAG_POLLERS);
		}
		catch (JSONException e){
			pollers = null; 
			e.printStackTrace();
    	}
	}
	
	/*
	 * Should be called after handleMLGlassJSONResults so that pollers is set.
	 */
	public String locationOf(String person){
		if( pollers == null) return "NONE";
		String curGlass;
		
		for (int i = 0; i < pollers.length(); i++){
			try{
				JSONObject c = pollers.getJSONObject(i);
    			if (c.getString(TAG_NAME) != null){
        			String glassName = c.getString(TAG_NAME);	
        			tag = c.getJSONObject(TAG_TAGS);
        			JSONArray id = tag.names();
        			if (id != null){
        				for(int j=0; j<id.length();j++){
        					curGlass = id.getString(j);
		        			if (curGlass.equals(person)){
			        			Log.d("FOUND YOU", "I saw " + person + " in " + glassName + 
			        					" at: " + currentTime.getTime());
			        			return glassName;
		        			}
        				}
        			}
    			}
			}catch (JSONException e){
	    		continue;
	    	}
		}
		return "NONE";
	}
	
	private int[][] distanceMatrix; 
	
	public int getDistance(){
		for(int i = 0; i < 23; i++){
			for(int j = 0; j < 23; j++){
				if(i == j){
					distanceMatrix[i][j] = 0; 
				}else{
					distanceMatrix[i][j] = i+j; 
				}
			}
		}
		
		return 42;
	}
	
	private String mHiddenGlassId; 
	
	public void handleHiddenGlassJSONResults(JSONObject result){

		try {
			mHiddenGlassId = result.getString("GLASS_ID");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			mHiddenGlassId = "none";
		}
	}
	
	public String getHiddenGlassId(){
		return mHiddenGlassId; 
	}
	
	
	
}
