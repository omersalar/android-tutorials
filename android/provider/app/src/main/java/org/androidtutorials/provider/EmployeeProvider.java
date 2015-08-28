package org.androidtutorials.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

public class EmployeeProvider extends ContentProvider {
    static final String     AUTHORITY_NAME = "org.androidtutorials.provider.Employees";
    static final String     URL            = "content://" + AUTHORITY_NAME + "/employees";
    static final Uri        CONTENT_URI    = Uri.parse(URL);

    // fields for the database
    static final String     ID             = DatabaseHandler.KEY_ID;
    static final String     FIRSTNAME      = DatabaseHandler.KEY_FIRSTNAME;
    static final String     LASTNAME       = DatabaseHandler.KEY_LASTNAME;
    static final String     AGE            = DatabaseHandler.KEY_AGE;

    // integer values used in content URI
    static final int        EMPLOYEE       = 1;
    static final int        EMPLOYEE_ID    = 2;

    // maps content URI "patterns" to the integer values that were set above
    static final UriMatcher uriMatcher;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY_NAME, "employees", EMPLOYEE);
        uriMatcher.addURI(AUTHORITY_NAME, "employees/#", EMPLOYEE_ID);
    }

    // database declarations
    private SQLiteDatabase  database;


    @Override
    public boolean onCreate() {
        DatabaseHandler dbHandler = new DatabaseHandler(getContext());
        // permissions to be writable
        database = dbHandler.getWritableDatabase();

        return database != null;
    }

    @Override
    public void shutdown() {
        super.shutdown();
        if (database != null) {
            database.close();
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(DatabaseHandler.TABLE_EMPLOYEE);

        switch (uriMatcher.match(uri)) {
        case EMPLOYEE:
            // Do nothing
            break;
        case EMPLOYEE_ID:
            queryBuilder.appendWhere(ID + "=" + uri.getLastPathSegment());
            break;
        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
        if (sortOrder == null || sortOrder.equals("")) {
            // No sorting-> sort on names by default
            sortOrder = LASTNAME;
        }
        Cursor cursor = queryBuilder.query(database, projection, selection, selectionArgs, null,
                null, sortOrder);
        /**
         * register to watch a content URI for changes
         */
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        long row = database.insert(DatabaseHandler.TABLE_EMPLOYEE, "", values);

        // If record is added successfully
        if (row > 0) {
            Uri newUri = ContentUris.withAppendedId(CONTENT_URI, row);
            getContext().getContentResolver().notifyChange(newUri, null);
            return newUri;
        }
        throw new SQLException("Fail to add a new record into " + uri);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int count = 0;

        switch (uriMatcher.match(uri)) {
        case EMPLOYEE:
            count = database.update(DatabaseHandler.TABLE_EMPLOYEE, values, selection,
                    selectionArgs);
            break;
        case EMPLOYEE_ID:
            count = database.update(DatabaseHandler.TABLE_EMPLOYEE, values,
                    ID + " = " + uri.getLastPathSegment()
                            + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""),
                    selectionArgs);
            break;
        default:
            throw new IllegalArgumentException("Unsupported URI " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int count = 0;

        switch (uriMatcher.match(uri)) {
        case EMPLOYEE:
            // delete all the records of the table
            count = database.delete(DatabaseHandler.TABLE_EMPLOYEE, selection, selectionArgs);
            break;
        case EMPLOYEE_ID:
            String id = uri.getLastPathSegment(); // gets the id
            count = database.delete(DatabaseHandler.TABLE_EMPLOYEE,
                    ID + " = " + id
                            + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""),
                    selectionArgs);
            break;
        default:
            throw new IllegalArgumentException("Unsupported URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;

    }

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
        case EMPLOYEE:
            /*
             * All contacts
             */
            return "vnd.android.cursor.dir/vnd.androidtutorials.employees";
        case EMPLOYEE_ID:
            /*
             * Particular contact
             */
            return "vnd.android.cursor.item/vnd.androidtutorials.employees";
        default:
            throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }
}