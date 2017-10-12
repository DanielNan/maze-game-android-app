package edu.wm.cs.cs301.amazebylinyongnan.ui;

import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import edu.wm.cs.cs301.amazebylinyongnan.R;

public class ZeroBatteryFinishActivity extends AppCompatActivity {

    public static final String LOG_TAG = "ZeroBatteryActivity";

    private Button backButton;
    private MediaPlayer sound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zero_battery_finish);
        initializeVariables();

        //set up sound effect
        sound = new MediaPlayer();
        sound = MediaPlayer.create(this, R.raw.camera);
        sound.setAudioStreamType(AudioManager.STREAM_MUSIC);
        sound.start();

        //backButton
        backButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view){
                //navigate back to AMazeActivity
                Intent intent = new Intent(ZeroBatteryFinishActivity.this, AMazeActivity.class);
                startActivity(intent);
                Log.v("Switch","From ZeroBatteryFinishActivity to AMazeActivity");
            }
        });

        Log.d(LOG_TAG,"onCreate");
    }

    /**
     * This method is created to help onCreate method to initialize all variables.
     */
    private void initializeVariables(){
        backButton = (Button) findViewById(R.id.button3);
    }

    @Override
    protected void onStart() {
        super.onStart();

        Log.d(LOG_TAG, "onStart");
    }

    @Override
    protected void onPause() {
        super.onPause();

        Log.d(LOG_TAG,"onPause");
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.d(LOG_TAG,"onResume");
    }

    @Override
    protected void onStop() {
        super.onStop();

        Log.d(LOG_TAG,"onStop");
    }
}
