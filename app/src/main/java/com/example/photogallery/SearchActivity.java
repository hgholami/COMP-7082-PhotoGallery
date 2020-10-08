package com.example.photogallery;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

public class SearchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
    }

    public void search(View view) throws JSONException {
        // Get values from fields
        String startDate = ((EditText) findViewById(R.id.startDateField)).getText().toString();
        String endDate = ((EditText) findViewById(R.id.endDateField)).getText().toString();
        String topLeftLat = ((EditText) findViewById(R.id.topLeftLatField)).getText().toString();
        String topLeftLng = ((EditText) findViewById(R.id.topLeftLngField)).getText().toString();
        String bottomRightLat = ((EditText) findViewById(R.id.bottomRightLatField)).getText().toString();
        String bottomRightLng = ((EditText) findViewById(R.id.bottomRightLngField)).getText().toString();
        String keyword = ((EditText) findViewById(R.id.keywordField)).getText().toString();

        JSONObject obj = new JSONObject();
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("startDate", startDate);
        intent.putExtra("endDate", endDate);
        intent.putExtra("topLeftLat", topLeftLat);
        intent.putExtra("topLeftLng", topLeftLng);
        intent.putExtra("bottomRightLat", bottomRightLat);
        intent.putExtra("bottomRightLng", bottomRightLng);
        intent.putExtra("keyword", keyword);

        setResult(RESULT_OK, intent);
        finish();
    }

}