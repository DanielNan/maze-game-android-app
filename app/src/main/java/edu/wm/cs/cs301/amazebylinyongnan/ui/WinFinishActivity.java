package edu.wm.cs.cs301.amazebylinyongnan.ui;

import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import edu.wm.cs.cs301.amazebylinyongnan.R;
import edu.wm.cs.cs301.amazebylinyongnan.falstad.MazeController;

public class WinFinishActivity extends AppCompatActivity {

    public static final String LOG_TAG = "WinFinishActivity";

    private Button backButton;
    private MediaPlayer sound;
    private TextView pathText;
    private TextView batteryText;
    private int pathLength;
    private int batteryLevel;

    MazeController controller = GeneratingActivity.getController();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_win_finish);
        initializeVariables();

        //set up sound effect
        sound = new MediaPlayer();
        sound = MediaPlayer.create(this, R.raw.laser);
        sound.setAudioStreamType(AudioManager.STREAM_MUSIC);
        sound.start();

        Intent intent = getIntent();
        pathLength = intent.getIntExtra("pathLength",0);
        batteryLevel = intent.getIntExtra("batteryLevel",0);

        pathText.setText(">> Path Length: "+controller.getPath());
        System.out.println(controller.getBattery());
        batteryText.setText(">> Energy Consumption: "+(2500 - controller.getBattery()));

        //backButton
        backButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view){
                //navigate back to AMazeActivity
                Intent intent = new Intent(WinFinishActivity.this, AMazeActivity.class);
                startActivity(intent);
                Log.v("Switch","From WinFinishActivity to AMazeActivity");
            }
        });

        Log.d(LOG_TAG,"onCreate");
    }

    /**
     * This method is created to help onCreate method to initialize all variables.
     */
    private void initializeVariables(){
        backButton = (Button) findViewById(R.id.button3);
        pathText = (TextView) findViewById(R.id.textView7);
        batteryText = (TextView) findViewById(R.id.textView8);
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
