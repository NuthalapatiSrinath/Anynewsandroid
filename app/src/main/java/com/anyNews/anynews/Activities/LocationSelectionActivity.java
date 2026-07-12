package com.anyNews.anynews.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import com.anyNews.anynews.R;

public class LocationSelectionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_selection);
        String[] COUNTRIES = new String[]{"India",
                "China",
                "Indonesia",
                "Pakistan",
                "Bangladesh",
                "Japan",
                "Philippines",
                "Vietnam",
                "Iran",
                "Turkey",
                "Thailand",
                "Myanmar",
                "South Korea",
                "Iraq"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, COUNTRIES);
        AutoCompleteTextView textView = (AutoCompleteTextView)
                findViewById(R.id.locationAutocompleteTextVrew);
        textView.setAdapter(adapter);
    }
}