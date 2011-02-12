package uk.co.purplemonkeys.TimeMonkey.tasks;

import java.io.InputStream;
import java.util.Date;

import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
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
 * A background worker task for sending the working time entry to the
 * Dasher website.
 * @author "leenookx"
 */
public class WorkingTimePosterTask extends AsyncTask<Void, Void, Object> 
{
	private final static String TAG = "WorkingTimePosterTask";
	
    private DefaultHttpClient mClient = HttpCommon.createGzipHttpClient();
    private Context _appContext;
    
    int _project;
    int _task;
    Date _start;
    Date _end;
    
    public WorkingTimePosterTask(Context c, int project, int task, Date start, Date end)
    {
    	_appContext = c;
    	_project = project;
    	_task = task;
    	_start = start;
    	_end = end;
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
        	JSONObject post_params = new JSONObject();
        	post_params.put("auth_code", auth_code);
        	
        	JSONObject sub = new JSONObject();
        	sub.put("project_id", _project);
        	sub.put("task_id", _task);
        	sub.put("start_time", _start);
        	sub.put("end_time", _end);
        	
        	post_params.put("working_time", sub);
        	
        	HttpPost httprequest = new HttpPost(url + "/working_times");
        	
        	// The progress dialog is non-cancelable, so set a shorter timeout than system's
        	HttpParams params = httprequest.getParams();
        	HttpConnectionParams.setConnectionTimeout(params, 30000);
        	HttpConnectionParams.setSoTimeout(params, 30000);
   
        	StringEntity s = new StringEntity(post_params.toString());
        	s.setContentEncoding("UTF-8");
        	s.setContentType("application/json");
        	params.setParameter("authentication-token", auth_code);
        	httprequest.setEntity(s);
            
        	// Perform the HTTP POST request
        	HttpResponse response = mClient.execute(httprequest);
        	StringBuilder result;
        	if (response != null) 
        	{
	        	String status = response.getStatusLine().toString();
	            if (!status.contains("OK"))
	            {
	            	throw new HttpException(status);
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
