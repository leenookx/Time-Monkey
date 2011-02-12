package uk.co.purplemonkeys.TimeMonkey;

import java.util.Calendar;
import java.util.Date;

import uk.co.purplemonkeys.TimeMonkey.providers.ProjectProvider;
import uk.co.purplemonkeys.TimeMonkey.providers.Project.Projects;
import uk.co.purplemonkeys.common.Common;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;

public class TimeMonkey extends Activity 
{
	private SharedPreferences preferences;
	private String version_info;
	private Button testButton;
	private boolean timerRunning = false;
	private Date starttime;
	private String[] PROJECTION = new String[] {
			Projects._ID, Projects.TITLE
		    };
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        // Initialise preferences
		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		
		try
		{
			PackageManager pm = getPackageManager();
			PackageInfo pi = pm.getPackageInfo("uk.co.purplemonkeys.TimeMonkey", 0);
			version_info = "Time Monkey " + pi.versionName;
		}
		catch (NameNotFoundException e)
		{
			version_info = "Couldn't determine version info.";
		}
		
		// Check to see if there are any preferences set. If not, then force the
		// user to enter some.
		if (preferences.getAll().size() == 0)
		{
			startActivity(new Intent(this, Preferences.class));
		}
		
		ButtonAction testListener = new ButtonAction();
		testButton = (Button) findViewById(R.id.btnStartStopTimer);
		testButton.setOnClickListener(testListener);
				
		// Load a Spinner and bind it to a data query.
		Spinner s2 = (Spinner) findViewById(R.id.ProjectSpinner);
		Cursor cur = managedQuery(Projects.CONTENT_URI, PROJECTION, null, null, null);
		     
		SimpleCursorAdapter adapter2 = new SimpleCursorAdapter(this,
		    android.R.layout.simple_spinner_item, // Use a template
		                                          // that displays a
		                                          // text view
		    cur, // Give the cursor to the list adapter
		    new String[] {Projects.TITLE}, // Map the NAME column in the
		                                         // people database to...
		    new int[] {android.R.id.text1}); // The "text1" view defined in
		                                     // the XML template
		                                         
		adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		s2.setAdapter(adapter2);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.timemonkey, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) 
    {
    	switch (item.getItemId()) 
    	{
    		case R.id.preferences_menu_id:
    			startActivity(new Intent(this, Preferences.class));
    			return true;
    		case R.id.about_menu_id:
    			Common.ShowAlertMessage(this, version_info);
    			return true;
    		case R.id.refresh_menu_id:
    			return true;
			default:
				break;
    	}

    	return super.onOptionsItemSelected(item);
    }

    /**
     * Class for handling click events on the Start/Stop timer button.
     * @author "leenookx"
     */
    private class ButtonAction implements OnClickListener
    {
		@Override
		public void onClick(View v) 
		{
			timerRunning = !timerRunning;
			
			if (timerRunning)
			{
				starttime = Calendar.getInstance().getTime();
				
				testButton.setText(R.string.btnTimer_Stop);
				
				TextView txt = (TextView)findViewById(R.id.StartTime);
				txt.setText( starttime.toLocaleString() );
			}
			else
			{
				Date stoptime = Calendar.getInstance().getTime();
				
				testButton.setText(R.string.btnTimer_Start);
				
				TextView txt = (TextView)findViewById(R.id.StartTime);
				txt.setText( R.string.timer_not_started );
			}
		}	
    }
}
