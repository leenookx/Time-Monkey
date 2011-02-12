package uk.co.purplemonkeys.TimeMonkey.tasks;

import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONObject;

import uk.co.purplemonkeys.TimeMonkey.providers.Project.Projects;
import uk.co.purplemonkeys.TimeMonkey.providers.Task.Tasks;
import uk.co.purplemonkeys.common.http.HttpCommon;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * A background worker task for retrieving the list of projects associated
 * with the user details held in the preferences.
 * @author "leenookx"
 */
public class ProjectGrabberTask extends AsyncTask<Void, Void, Object> 
{
	private final static String TAG = "ProjectGrabberTask";
	
    private DefaultHttpClient mClient = HttpCommon.createGzipHttpClient();
    private Context _appContext;
    
    public ProjectGrabberTask(Context c)
    {
    	_appContext = c;
    }
   
    @Override
    public Object doInBackground(Void... unused_params) 
    {
    	HttpEntity entity = null;
    	
        try 
        {
        	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(_appContext);
        	String url = prefs.getString("pref_url", "http://localhost:3000");
        	String auth_code = prefs.getString("pref_auth_code", "abc123");

        	// Construct data
//        	JSONObject post_params = new JSONObject();
//        	post_params.put("auth_code", auth_code);
        	
//        	JSONObject sub = new JSONObject();
//        	
//        	post_params.put("links", sub);
        	
        	HttpGet httppost = new HttpGet(url + "/projects.json");
        	
        	// The progress dialog is non-cancelable, so set a shorter timeout than system's
        	HttpParams params = httppost.getParams();
        	HttpConnectionParams.setConnectionTimeout(params, 30000);
        	HttpConnectionParams.setSoTimeout(params, 30000);
   
//        	StringEntity s = new StringEntity(post_params.toString());
//        	s.setContentEncoding("UTF-8");
//        	s.setContentType("application/json");
        	httppost.addHeader("Content-Type", "application/json");
        	httppost.addHeader("authentication-token", auth_code);
            
        	// Perform the HTTP GET request
        	HttpResponse response = mClient.execute(httppost);
        	StringBuilder result;
        	if (response != null) 
        	{
	        	String status = response.getStatusLine().toString();
	            if (!status.contains("OK"))
	            {
	            	throw new HttpException(status);
	            }
	            
	            entity = response.getEntity();
				InputStream instream = entity.getContent();
				result = HttpCommon.convertStreamToString(instream);
				instream.close();

				// Delete all of the existing records.
				_appContext.getContentResolver().delete(Projects.CONTENT_URI, null, null);
				_appContext.getContentResolver().delete(Tasks.CONTENT_URI, null, null);
				
				JSONArray jObject = new JSONArray( result.toString() );
				for (int i = 0; i < jObject.length(); ++i)
				{
					JSONObject o = jObject.getJSONObject(i);
					JSONObject p = new JSONObject(o.getString("project"));
					
					ContentValues cv = new ContentValues();
					cv.put(Projects._ID, p.getInt("id"));
					cv.put(Projects.TITLE, p.getString("name"));
					
					_appContext.getContentResolver().insert(Projects.CONTENT_URI, cv);
				}
        	}
        	else
        	{
        		// TODO: Do something about this error?
        		throw new Exception();
        	}
            
            // TODO: Return something more meaningful later.
            return new Object();
        }
        catch (Exception e) 
        {
        	if (entity != null) 
        	{
        		try 
        		{
        			entity.consumeContent();
        		} 
        		catch (Exception e2) {
        			Log.e(TAG, "entity.consumeContent()", e2);
        		}
        	}
        	
        	Log.e(TAG, "ShareLinkTask", e);
        }
        
        return null;
    }
    
    @Override
    public void onPreExecute() 
    {
    }
   
    @Override
    public void onPostExecute(Object result) 
    {
    	if (result != null)
    	{
    	}
    	else
    	{
    	}
    }        
}
