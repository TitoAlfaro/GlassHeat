package mit.edu.obmg.glassheat.tasks;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Calendar;

import mit.edu.obmg.glassheat.GlassHeatMain;
import mit.edu.obmg.glassheat.GlassHeatActivity;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

public class GlassHeatReader extends AsyncTask<String, Void, JSONObject>  {
    private static String TAG = "GlassHeatReader";
	WifiManager WiFi;
    private Context context;
    private GlassHeatMain mGHMain; 
	private GlassHeatActivity mGHActivity;
	

    String debugState;

	/*
     * Cosntruct a task
     * @param activity
     */
	public GlassHeatReader(){
	}
	
    public GlassHeatReader (GlassHeatMain activity){
    	super();
    	this.mGHMain = activity;
    	this.context = this.mGHMain.getApplicationContext();

        WiFi = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
    }
    
    public GlassHeatReader (GlassHeatActivity activity){
    	super();
    	this.mGHActivity = activity;
    	this.context = this.mGHActivity.getApplicationContext();

        WiFi = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
    }
    
    @Override
    protected JSONObject doInBackground(String... urls) {
    	String jsonString = "";
    	debugState = urls[0];
    	try {
            // defaultHttpClient
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpGet httpGET = new HttpGet(urls[0]);
            HttpResponse httpResponse = httpClient.execute(httpGET);
            int status = httpResponse.getStatusLine().getStatusCode();
            Log.d(TAG, "Status: "+status);
            HttpEntity httpEntity = httpResponse.getEntity();
            jsonString = EntityUtils.toString(httpEntity);
 
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
        	if(WiFi.isWifiEnabled() == true){
        		System.out.println("resetting WiFi");
        		WiFi.setWifiEnabled(false);
        		WiFi.setWifiEnabled(true);
        	}else WiFi.setWifiEnabled(true);
            e.printStackTrace();
        }
    	
    	JSONObject json = null;
    	try {
			json = new JSONObject(jsonString);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return json;
    }
    /*
    protected void onPostExecute(JSONObject result){
    	this.handler.sendEmptyMessage(0);
    }*/
    /*
    protected void onPostExecute(JSONObject result){

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
			        			System.out.println("I saw Tito! in " + name + " at: " + currentTime.getTime());
		        			}
		        			if (test.equals(micah)){
			        			System.out.println("I saw Micah! at " + name + " at: " + currentTime.getTime());
		        			}
		        			if (test.equals(sunny)){
			        			System.out.println("I saw Sunny! at " + name + " at: " + currentTime.getTime());
		        			}
		        			//if (test.equals(edwinna)){
			        		//	System.out.println("I saw Sunny! at " + name + " at: " + currentTime.getTime());
		        			//}
        				}

        		        //this.activity.handleGlass(report);
        			}
    			}
			}catch (JSONException e){
	    		continue;
	    	}
		}
    }
    */
}
