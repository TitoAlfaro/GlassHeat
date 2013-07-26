package src.mit.edu.obmg.glassheat.net;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

public class AsyncGetJSONTask extends AsyncTask<String, Void, JSONObject>  {
	private static final String TAG = AsyncGetJSONTask.class.getSimpleName();
	
	public Handler handler; 
	
	protected JSONObject doInBackground(String... urls) {
    	String jsonString = "";
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
        	// handler wifi off?
        	// 500 means wifi off
        	handler.sendEmptyMessage(500);
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
	 * onPostExecute implemented inline when instantiated
	 */
}
