package mit.edu.obmg.glassheat;

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
    public String test;
    public String tito = "4D656469614C616243016115";
    public String tito2 = "4D656469614C616243014366";
    public String micah = "4D656469614C616243016115";
    public String sunny = "4D656469614C616243014441";
	
    
	public void handleMLGlassJSONResults(JSONObject result){
		if (result == null) return; 
		try{
    		pollers = result.getJSONArray(TAG_POLLERS);
		}
		catch (JSONException e){
    		e.printStackTrace();
    	}
		for (int i = 0; i < pollers.length(); i++){

			try{
			JSONObject c = pollers.getJSONObject(i);
			
    			if (c.getString(TAG_NAME) != null){
        			String name = c.getString(TAG_NAME);
        			
        			tag = c.getJSONObject(TAG_TAGS);
        			JSONArray id = tag.names();
        			if (id != null){
        				for(int j=0; j<id.length();j++){
	        				test = id.getString(j);
	        				//PrintDetect (test);
		        			if (test.equals(tito) || test.equals(tito2)){
		        				report = name;
			        			System.out.println("I saw Tito! in " + name + 
			        					" at: " + currentTime.getTime());
		        			}
		        			if (test.equals(micah)){
			        			System.out.println("I saw Micah! at " + name + 
			        					" at: " + currentTime.getTime());
		        			}
		        			if (test.equals(sunny)){
			        			System.out.println("I saw Sunny! at " + name + 
			        					" at: " + currentTime.getTime());
		        			}
        				}
        			}
    			}
			}catch (JSONException e){
	    		continue;
	    	}
		}
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
