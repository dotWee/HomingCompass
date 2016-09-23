package com.ciriousjoker.homingcompass;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Objects;

public class MyLocationsActivity extends Activity {

    private static final String TAG = "MyLocationsActivity";
    public static String MY_PREFS_FILE;
    private MyAdapter adapter;
    private ArrayList<MyLocationItem> arrayList;
    private RelativeLayout relativeLayout_Header;
    private EditText editText_Header;
    private View view_Header_Overlay;
    private ImageButton imageButton_Header_AddLocation;
    private ImageButton imageButton_Header_SaveLocation;
    private View.OnClickListener clickListener_Header;
    private View divider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_locations);

        MY_PREFS_FILE = getString(R.string.shared_pref_file);

        ImageButton button_CloseActivity = (ImageButton) findViewById(R.id.button_CloseActivity);
        button_CloseActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        setupHeader();
        setupListViewAdapter();
    }

    private void setupHeader() {
        editText_Header = (EditText) findViewById(R.id.editText_MyLocation_Header);
        view_Header_Overlay = findViewById(R.id.view_MyLocationHeader_Overlay);
        imageButton_Header_AddLocation = (ImageButton) findViewById(R.id.icon_add_location);
        imageButton_Header_SaveLocation = (ImageButton) findViewById(R.id.button_add_location);
        relativeLayout_Header = (RelativeLayout) findViewById(R.id.relativeLayout_Header);
        divider = findViewById(R.id.divider_after_header);

        clickListener_Header = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                divider.setVisibility(View.VISIBLE);
                relativeLayout_Header.setBackgroundColor(Color.WHITE);
                editText_Header.setEnabled(true);
                view_Header_Overlay.setVisibility(View.GONE);
                imageButton_Header_AddLocation.setImageResource(R.drawable.ic_clear_grey_24dp);
                imageButton_Header_SaveLocation.setVisibility(View.VISIBLE);

                imageButton_Header_AddLocation.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        clearHeader();
                        imageButton_Header_AddLocation.setOnClickListener(clickListener_Header);
                    }
                });
                editText_Header.requestFocus();

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(editText_Header, InputMethodManager.SHOW_IMPLICIT);
            }
        };

        view_Header_Overlay.setOnClickListener(clickListener_Header);
        imageButton_Header_AddLocation.setOnClickListener(clickListener_Header);

    }

    private void clearHeader() {
        editText_Header.setEnabled(false);
        view_Header_Overlay.setVisibility(View.VISIBLE);
        imageButton_Header_AddLocation.setImageResource(R.drawable.ic_add_black_24dp);
        relativeLayout_Header.setBackgroundColor(Color.WHITE);
        imageButton_Header_SaveLocation.setVisibility(View.INVISIBLE);
        divider.setVisibility(View.INVISIBLE);
        relativeLayout_Header.setBackgroundResource(R.color.colorWhiteBackground);
        editText_Header.setText("");
    }

    public void removeLocationItem(View v) {
        MyLocationItem itemToRemove = (MyLocationItem) v.getTag();
        adapter.remove(itemToRemove);
        saveMyLocations();
    }

    public void openLocationPicker(View v) {
        launchMapActivity((int) v.getTag());
    }

    public void addLocationItem(View v) {
        String newLocationName = editText_Header.getText().toString();
        if (!Objects.equals(newLocationName, "")) {
            adapter.add(new MyLocationItem(newLocationName));
            launchMapActivity(adapter.getCount() - 1);
        }
        imageButton_Header_AddLocation.setOnClickListener(clickListener_Header);
        clearHeader();
    }

    private void setupListViewAdapter() {
        SharedPreferences prefs = getSharedPreferences(MY_PREFS_FILE, MODE_PRIVATE);
        Gson gson = new Gson();
        String json = prefs.getString(getString(R.string.shared_pref_my_locations_file), "");
        Type listOfObjects = new TypeToken<ArrayList<MyLocationItem>>() {
        }.getType();


        arrayList = gson.fromJson(json, listOfObjects);

        if (arrayList == null) {
            arrayList = new ArrayList<>();
        }


        adapter = new MyAdapter(MyLocationsActivity.this, R.layout.item_my_locations_layout, arrayList);
        ListView atomPaysListView = (ListView) findViewById(R.id.EnterPays_atomPaysList);
        atomPaysListView.setAdapter(adapter);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                double latitude = data.getDoubleExtra(getString(R.string.key_intent_latitude), 0.0);
                double longitude = data.getDoubleExtra(getString(R.string.key_intent_longitude), 0.0);

                //Log.i(TAG, "Latitude / Longitude: " + latitude + " / " + longitude);

                if (latitude != 0.0 || longitude != 0.0) {
                    adapter.getItem(data.getIntExtra(getString(R.string.key_intent_location_id), 0)).setLatitude(latitude);
                    adapter.getItem(data.getIntExtra(getString(R.string.key_intent_location_id), 0)).setLongitude(longitude);

                    saveMyLocations();

                    Toast.makeText(this, R.string.notice_saved, Toast.LENGTH_SHORT).show();
                } else {
                    if (data.getIntExtra(getString(R.string.key_intent_location_id), 0) >= 0) {
                        adapter.getArrayList().remove(data.getIntExtra(getString(R.string.key_intent_location_id), 0));
                        saveMyLocations();
                    }
                }
            }
        }
    }

    private void saveMyLocations() {
        SharedPreferences prefs = getSharedPreferences(MY_PREFS_FILE, MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = prefs.edit();
        Type listOfObjects = new TypeToken<ArrayList<MyLocationItem>>() {
        }.getType();

        Gson gson = new Gson();
        String strObject = gson.toJson(adapter.getArrayList(), listOfObjects);
        prefsEditor.putString(getString(R.string.shared_pref_my_locations_file), strObject);
        prefsEditor.apply();
    }

    private void launchMapActivity(int id) {
        Intent intentPickLocation = new Intent(MyLocationsActivity.this, MapsActivity.class);
        intentPickLocation.putExtra(getString(R.string.key_intent_location_id), id);
        intentPickLocation.putExtra(getString(R.string.key_intent_latitude), adapter.getItem(id).getLatitude());
        intentPickLocation.putExtra(getString(R.string.key_intent_longitude), adapter.getItem(id).getLongitude());
        intentPickLocation.putExtra(getString(R.string.key_intent_marker_title), adapter.getItem(id).getName());
        startActivityForResult(intentPickLocation, 0);
    }
}