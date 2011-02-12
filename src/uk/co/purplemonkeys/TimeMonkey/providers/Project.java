package uk.co.purplemonkeys.TimeMonkey.providers;

import android.net.Uri;
import android.provider.BaseColumns;

public class Project 
{
    public static final String AUTHORITY = "uk.co.purplemonkeys.TimeMonkey.providers.ProjectProvider";

    // This class cannot be instantiated
    private Project() {}

    /**
     * Project table
     */
    public static final class Projects implements BaseColumns 
    {
        // This class cannot be instantiated
        private Projects() {}

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/Projects");

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of notes.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.google.note";

        /**
         * The MIME type of a {@link #CONTENT_URI} sub-directory of a single note.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.google.note";

        /**
         * The default sort order for this table
         */
        public static final String DEFAULT_SORT_ORDER = "_ID";

        /**
         * The title of the note
         * <P>Type: TEXT</P>
         */
        public static final String TITLE = "title";
    }
}
