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
public class TaskGrabberTask extends AsyncTask<Void, Void, Object> 
{
	private final static String TAG = "TaskGrabberTask";
	
    private DefaultHttpClient mClient = HttpCommon.createGzipHttpClient();
    private int _project_id;
    private Context _appContext;
    
    public TaskGrabberTask(int id, Context c)
    {
    	_project_id = id;
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
        	HttpGet httppost = new HttpGet(url + "/projects/tasks/" + _project_id + ".json");
        	
        	// The progress dialog is non-cancelable, so set a shorter timeout than system's
        	HttpParams params = httppost.getParams();
        	HttpConnectionParams.setConnectionTimeout(params, 30000);
        	HttpConnectionParams.setSoTimeout(params, 30000);
   
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

				JSONArray jObject = new JSONArray( result.toString() );
				for (int i = 0; i < jObject.length(); ++i)
				{
					JSONObject o = jObject.getJSONObject(i);
					JSONObject p = new JSONObject(o.getString("task"));
					
					ContentValues cv = new ContentValues();
					cv.put(Tasks._ID, p.getInt("id"));
					cv.put(Tasks.TITLE, p.getString("description"));
					cv.put(Tasks.PROJECT_ID, p.getInt("project_id"));
					
					_appContext.getContentResolver().insert(Tasks.CONTENT_URI, cv);
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
