package uk.co.purplemonkeys.TimeMonkey;

import uk.co.purplemonkeys.common.Common;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class TimeMonkey extends Activity 
{
	private SharedPreferences preferences;
	private String version_info;
	private Button testButton;
	private boolean timerRunning = false;
	
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
				testButton.setText(R.string.btnTimer_Stop);
			}
			else
			{
				testButton.setText(R.string.btnTimer_Start);
			}
		}	
    }
}
