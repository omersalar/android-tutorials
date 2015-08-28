package org.androidtutorials.provider;

import android.app.Activity;

import android.net.Uri;
import android.os.Bundle;
import android.content.ContentValues;
import android.database.Cursor;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity {

    private EditText firstName;
    private EditText lastName;
    private EditText age;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        firstName = (EditText) findViewById(R.id.firstname);
        lastName = (EditText) findViewById(R.id.lastname);
        age = (EditText) findViewById(R.id.age);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public void deleteAllEmployees(View view) {
        int count = getContentResolver().delete(EmployeeProvider.CONTENT_URI, null, null);
        String countNum = "" + count + " records were deleted.";
        Toast.makeText(this, countNum, Toast.LENGTH_LONG).show();
    }

    public void addEmployee(View view) {
        ContentValues values = new ContentValues();

        values.put(EmployeeProvider.FIRSTNAME, firstName.getText().toString());
        values.put(EmployeeProvider.LASTNAME, lastName.getText().toString());
        int age_ = 0;
        try {
            age_ = Integer.parseInt(age.getText().toString());
        } catch (NumberFormatException ex) {
        }
        values.put(EmployeeProvider.AGE, age_);

        Uri uri = getContentResolver().insert(EmployeeProvider.CONTENT_URI, values);

        Toast.makeText(getBaseContext(), "Added: " + uri.toString(), Toast.LENGTH_LONG).show();
    }

    public void showAllEmployees(View view) {
        Cursor c = getContentResolver().query(EmployeeProvider.CONTENT_URI, null, null, null,
                EmployeeProvider.LASTNAME);
        String result = "";

        if (!c.moveToFirst()) {
            Toast.makeText(this, "No employees found!", Toast.LENGTH_LONG).show();
        } else {
            do {
                result = result + "\n" + c.getString(c.getColumnIndex(EmployeeProvider.FIRSTNAME))
                        + " with id " + c.getString(c.getColumnIndex(EmployeeProvider.ID));
            } while (c.moveToNext());
            Toast.makeText(this, result, Toast.LENGTH_LONG).show();
        }
        c.close();
    }
}