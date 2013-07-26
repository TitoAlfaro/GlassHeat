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
    public String tito = "4D656469614C616243016115";
    public String tito2 = "4D656469614C616243014366";
    public String micah = "4D656469614C616243016447";
    public String sunny = "4D656469614C616243014441";
    
    public String[] glassIds;
	
	/* Glass IDs
	 * e14-140-2, e14-151-1, e14-245-1, e14-251-1, e14-274-1, e14-333-1, e14-348-1, e14-445-1, e14-474-1, 
	 * e14-514-1, e14-514-2, e14-525-1, e14-548-1, e14-674-1, e15-003-1, e15-100-1, e15-200-1, e15-300-1, 
	 * e15-344-1, e15-383-1, e15-400-1, e15-443-1, e15-468-1
	 */
    
    public MLGlass(){
    	initDistance();
    	/*
    	glassIds[] = 
    			 { e14-140-2, e14-151-1, e14-245-1, e14-251-1, e14-274-1, e14-333-1, e14-348-1, e14-445-1, e14-474-1, 
    			  e14-514-1, e14-514-2, e14-525-1, e14-548-1, e14-674-1, e15-003-1, e15-100-1, e15-200-1, e15-300-1, 
    			  e15-344-1, e15-383-1, e15-400-1, e15-443-1, e15-468-1 } ;
    			  */
    }
    
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
	
	public int getDistance(String curGlassId, String hiddenGlassId){
		/*
		 * for loop doing comparisions 
		 * 
		glassIds[i] = curGlassId; 
		glassIds[j] = hiddenGlassId;
		*/
		
		return 42;
	}
	
	public void initDistance(){
		for(int i = 0; i < 23; i++){
			for(int j = 0; j < 23; j++){
				if(i == j){
					distanceMatrix[i][j] = 0; 
				}else{
					distanceMatrix[i][j] = i+j; 
				}
			}
		}
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
