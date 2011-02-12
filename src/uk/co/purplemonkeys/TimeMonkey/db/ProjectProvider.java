package uk.co.purplemonkeys.TimeMonkey.db;

import java.util.HashMap;

import uk.co.purplemonkeys.TimeMonkey.db.Project.Projects;
import uk.co.purplemonkeys.TimeMonkey.db.Task.Tasks;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

/**
 * Provides access to a database of projects.
 */
public class ProjectProvider extends ContentProvider 
{
    private static final String TAG = "ProjectProvider";
    private static final String DATABASE_NAME = "timemonkey.db";
    private static final int DATABASE_VERSION = 1;
    private static final String PROJECT_TABLE_NAME = "projects";
    private static final String TASK_TABLE_NAME = "tasks";
    private static HashMap<String, String> sProjectProjectionMap;
    private static HashMap<String, String> sTaskProjectionMap;
    private static final UriMatcher sUriMatcher;

    private DatabaseHelper mOpenHelper;
    
    private static final int PROJECTS = 1;
    private static final int TASKS = 2;
    
    static 
    {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(Project.AUTHORITY, "projects", PROJECTS);
        sUriMatcher.addURI(Task.AUTHORITY, "tasks", TASKS);

        sProjectProjectionMap = new HashMap<String, String>();
        sProjectProjectionMap.put(Projects._ID, Projects._ID);
        sProjectProjectionMap.put(Projects.TITLE, Projects.TITLE);

        sTaskProjectionMap = new HashMap<String, String>();
        sTaskProjectionMap.put(Tasks._ID, Tasks._ID);
        sTaskProjectionMap.put(Tasks.PROJECT_ID, Tasks.PROJECT_ID);
        sTaskProjectionMap.put(Tasks.TITLE, Tasks.TITLE);
    }

    @Override
    public boolean onCreate() 
    {
        mOpenHelper = new DatabaseHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) 
    {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        switch (sUriMatcher.match(uri)) 
        {
	        case PROJECTS:
	        	qb.setTables(PROJECT_TABLE_NAME);
	            qb.setProjectionMap(sProjectProjectionMap);
	            break;
	        case TASKS:
	        	qb.setTables(TASK_TABLE_NAME);
	            qb.setProjectionMap(sTaskProjectionMap);
	            break;
	        default:
	            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        // If no sort order is specified use the default
        String orderBy;
        if (TextUtils.isEmpty(sortOrder)) 
        {
            orderBy = Projects.DEFAULT_SORT_ORDER;
        } 
        else 
        {
            orderBy = sortOrder;
        }

        // Get the database and run the query
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, orderBy);

        // Tell the cursor what uri to watch, so it knows when its source data changes
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    public String getType(Uri uri) 
    {
        switch (sUriMatcher.match(uri)) 
        {
	        case PROJECTS:
	            return Projects.CONTENT_TYPE;
	
	        case TASKS:
	            return Tasks.CONTENT_ITEM_TYPE;
	
	        default:
	            throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues initialValues)
    {
        // Validate the requested uri
        if (sUriMatcher.match(uri) != PROJECTS) 
        {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        ContentValues values;
        if (initialValues != null) 
        {
            values = new ContentValues(initialValues);
        } 
        else 
        {
            values = new ContentValues();
        }

        if (values.containsKey(Projects.TITLE) == false) 
        {
            Resources r = Resources.getSystem();
            values.put(Projects.TITLE, r.getString(android.R.string.untitled));
        }

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        long rowId = db.insert(PROJECT_TABLE_NAME, Projects.TITLE, values);
        if (rowId > 0) 
        {
            Uri noteUri = ContentUris.withAppendedId(Projects.CONTENT_URI, rowId);
            getContext().getContentResolver().notifyChange(noteUri, null);
            return noteUri;
        }

        throw new SQLException("Failed to insert row into " + uri);
    }

    @Override
    public int delete(Uri uri, String where, String[] whereArgs) 
    {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count;
        switch (sUriMatcher.match(uri)) 
        {
	        case PROJECTS:
	            count = db.delete(PROJECT_TABLE_NAME, where, whereArgs);
	            break;
	
	        case TASKS:
	            String rowId = uri.getPathSegments().get(1);
	            count = db.delete(TASK_TABLE_NAME, Tasks._ID + "=" + rowId
	                    + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
	            break;
	
	        default:
	            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String where, String[] whereArgs) 
    {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count;
        switch (sUriMatcher.match(uri)) 
        {
	        case PROJECTS:
	            count = db.update(PROJECT_TABLE_NAME, values, where, whereArgs);
	            break;
	
	        case TASKS:
	            String rowId = uri.getPathSegments().get(1);
	            count = db.update(TASK_TABLE_NAME, values, Tasks._ID + "=" + rowId
	                    + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
	            break;
	
	        default:
	            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        
        return count;
    }
    
    /**
     * This class helps open, create, and upgrade the database file.
     */
    private static class DatabaseHelper extends SQLiteOpenHelper 
    {
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + PROJECT_TABLE_NAME + " ("
                    + Projects._ID + " INTEGER PRIMARY KEY,"
                    + Projects.TITLE + " TEXT"
                    + ");");

            db.execSQL("CREATE TABLE " + TASK_TABLE_NAME + " ("
                    + Tasks._ID + " INTEGER PRIMARY KEY,"
                    + Tasks.PROJECT_ID + " INTEGER,"
                    + Tasks.TITLE + " TEXT"
                    + ");");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to " 
            			+ newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + PROJECT_TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + TASK_TABLE_NAME);
            onCreate(db);
        }
    }
}
