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

	public String[] glassIds ={ 	"e14-151-1", 
			"e14-251-1", "e14-274-1", 
			"e14-333-1", "e14-348-1", "charm-1", "e15-344-1", "e15-383-1", "e15-300-1", 
			"e14-474-1", "e14-443-1", "e15-443-1", "e15-468-1", "charm-5", "e15-400-1", 
			"e14-525-1", "e14-514-2", "e14-514-1", "e14-548-1",
		"e14-674-1" } ;

	/* Glass IDs
	 * e14-140-2, e14-151-1, e14-245-1, e14-251-1, e14-274-1, e14-333-1, e14-348-1, e14-445-1, e14-474-1, 
	 * e14-514-1, e14-514-2, e14-525-1, e14-548-1, e14-674-1, e15-003-1, e15-100-1, e15-200-1, e15-300-1, 
	 * e15-344-1, e15-383-1, e15-400-1, e15-443-1, e15-468-1
	 */

	public MLGlass(){
		initDistance();

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

	private int[][] distanceMatrix = new int [20] [20]; 

	public int getDistance(String curGlassId, String hiddenGlassId){
		/*
		 * for loop doing comparisons 
		 */
		int curGlassIndex = -1;
		int hiddenGlassIndex = -1;
		for(int i=0; i<20; i++){
			if(curGlassId == glassIds[i]){
				curGlassIndex = i;
			}
			if(hiddenGlassId == glassIds[i]){
				hiddenGlassIndex = i;
			}
		}
		if(curGlassIndex == -1 || hiddenGlassIndex == -1) return -1;
		
		int distance = distanceMatrix [curGlassIndex][hiddenGlassIndex];

		return distance;
	}

	public void initDistance(){
		for(int i = 0; i < 20; i++){
			for(int j = 0; j < 20; j++){
				if (i == 0){
					if(j==0){
						distanceMatrix[i][j] = 0; 
						distanceMatrix[j][i] = 0; 
					}else if(j>0 &&  j<3){
						distanceMatrix[i][j] = 10; 
						distanceMatrix[j][i] = 10; 
					}else if(j>2 &&  j<9){
						distanceMatrix[i][j] = 20; 
						distanceMatrix[j][i] = 20; 
					}else if(j>8 &&  j<15){
						distanceMatrix[i][j] = 30; 
						distanceMatrix[j][i] = 30; 
					}else if(j>14 &&  j<19){
						distanceMatrix[i][j] = 40; 
						distanceMatrix[j][i] = 40; 
					}else if(j>18){
						distanceMatrix[i][j] = 50; 
						distanceMatrix[j][i] = 50; 
					}
				}else if (i>0 && i<3){
					if(j==0){
						distanceMatrix[i][j] = 10; 
						distanceMatrix[j][i] = 10; 
					}else if(j>0 &&  j<3){
						distanceMatrix[i][j] = i-j; 
						distanceMatrix[j][i] = i-j; 
					}else if(j>2 &&  j<9){
						distanceMatrix[i][j] = 10; 
						distanceMatrix[j][i] = 10; 
					}else if(j>8 &&  j<15){
						distanceMatrix[i][j] = 20; 
						distanceMatrix[j][i] = 20; 
					}else if(j>14 &&  j<19){
						distanceMatrix[i][j] = 30; 
						distanceMatrix[j][i] = 30; 
					}else if(j>18){
						distanceMatrix[i][j] = 40; 
						distanceMatrix[j][i] = 40; 
					}
				}else if (i>2 && i<9){
					if(j==0){
						distanceMatrix[i][j] = 20; 
						distanceMatrix[j][i] = 20; 
					}else if(j>0 &&  j<3){
						distanceMatrix[i][j] = 10; 
						distanceMatrix[j][i] = 10; 
					}else if(j>2 &&  j<9){
						distanceMatrix[i][j] = i-j; 
						distanceMatrix[j][i] = i-j; 
					}else if(j>8 &&  j<15){
						distanceMatrix[i][j] = 10; 
						distanceMatrix[j][i] = 10; 
					}else if(j>14 &&  j<19){
						distanceMatrix[i][j] = 20; 
						distanceMatrix[j][i] = 20; 
					}else if(j>18){
						distanceMatrix[i][j] = 30; 
						distanceMatrix[j][i] = 30; 
					}
				}else if (i>8 && i<15){
					if(j==0){
						distanceMatrix[i][j] = 30; 
						distanceMatrix[j][i] = 30; 
					}else if(j>0 &&  j<3){
						distanceMatrix[i][i] = 20; 
						distanceMatrix[j][i] = 20; 
					}else if(j>2 &&  j<9){
						distanceMatrix[i][i] = 10; 
						distanceMatrix[j][i] = 10; 
					}if(j>8 &&  j<15){
						distanceMatrix[i][i] = i-j; 
						distanceMatrix[j][i] = i-j; 
					}else if(j>14 &&  j<19){
						distanceMatrix[i][i] = 10; 
						distanceMatrix[j][i] = 10; 
					}else if(j>18){
						distanceMatrix[i][i] = 20; 
						distanceMatrix[j][i] = 20; 
					}
				}else if (i>14 && i<19){
					if(j==0){
						distanceMatrix[i][j] = 40; 
						distanceMatrix[j][i] = 40; 
					}else if(j>0 &&  j<3){
						distanceMatrix[i][i] = 30; 
						distanceMatrix[j][i] = 30; 
					}else if(j>2 &&  j<9){
						distanceMatrix[i][i] = 20; 
						distanceMatrix[j][i] = 20; 
					}if(j>8 &&  j<15){
						distanceMatrix[i][i] = 10; 
						distanceMatrix[j][i] = 10; 
					}else if(j>14 &&  j<19){
						distanceMatrix[i][i] = i-j; 
						distanceMatrix[j][i] = i-j; 
					}else if(j>18){
						distanceMatrix[i][i] = 10; 
						distanceMatrix[j][i] = 10; 
					}
				}else if (i>18){
					if(j==0){
						distanceMatrix[i][j] = 50; 
						distanceMatrix[j][i] = 50; 
					}else if(j>0 &&  j<3){
						distanceMatrix[i][i] = 40; 
						distanceMatrix[j][i] = 40; 
					}else if(j>2 &&  j<9){
						distanceMatrix[i][i] = 30; 
						distanceMatrix[j][i] = 30; 
					}if(j>8 &&  j<15){
						distanceMatrix[i][i] = 20; 
						distanceMatrix[j][i] = 20; 
					}else if(j>14 &&  j<19){
						distanceMatrix[i][i] = 10; 
						distanceMatrix[j][i] = 10; 
					}else if(j>18){
						distanceMatrix[i][i] = 0; 
						distanceMatrix[j][i] = 0; 
					}
				}
			}
		}
		Log.i(TAG, "after for loop");
	}

	public String mHiddenGlassId = "none"; 

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
