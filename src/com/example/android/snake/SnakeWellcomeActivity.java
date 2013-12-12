package com.example.android.snake;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SnakeWellcomeActivity extends Activity {
	EditText mNameEditText;
	SharedPreferences mSettings;
	Button mStartButton;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_snake_wellcome);
		
		mSettings = getSharedPreferences("snake", MODE_PRIVATE);
		mNameEditText = (EditText)findViewById(R.id.name);
		mNameEditText.setText(mSettings.getString("name", ""));
		mStartButton = (Button)findViewById(R.id.start);
		mStartButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String name = mNameEditText.getText().toString(); 
				if (!name.isEmpty()) {
					SharedPreferences.Editor editor = mSettings.edit();
					editor.putString("name", name);
					editor.commit();
					startActivity(new Intent(SnakeWellcomeActivity.this, Snake.class));
					finish();
				} else {
					Toast.makeText(SnakeWellcomeActivity.this, R.string.please_enter_you_nickname, Toast.LENGTH_LONG).show();
				}
			}
		});
		
		Button apSettingsButton = (Button)findViewById(R.id.button1);
		apSettingsButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
			}
		});
		
		Button wifiSettingsButton = (Button)findViewById(R.id.button2);
		wifiSettingsButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
			}
		});
	}
}
