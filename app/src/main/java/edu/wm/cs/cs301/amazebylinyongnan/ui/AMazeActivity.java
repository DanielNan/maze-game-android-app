package edu.wm.cs.cs301.amazebylinyongnan.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.util.Log;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import edu.wm.cs.cs301.amazebylinyongnan.R;

public class AMazeActivity extends AppCompatActivity {

    public static final String LOG_TAG = "AMazeActivity";

    private SeekBar seekBar;
    private TextView textView;
    private Spinner builderSpinner;
    private Spinner driverSpinner;
    private Button startButton;
    private Button reloadButton;
    private int level = 0;
    private String builder;
    private String driver;
    private CoordinatorLayout coordinatorLayout;

   @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_amaze);
        initializeVariables();

        //seekBar setup and user input tracking
        textView.setText("Skill Level: " + seekBar.getProgress());
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser) {
                progress = progressValue;
                textView.setText("Skill Level: " + progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                textView.setText("Skill Level: " + progress);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                textView.setText("Skill Level: " + progress);
            }
        });



        //startButton
        //once the button is pressed, send user input of skill level, builder, driver, and
        //the button being pressed to GeneratingActivity
        startButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view){
                //get data
                level = seekBar.getProgress();
                builder = builderSpinner.getSelectedItem().toString();
                driver = driverSpinner.getSelectedItem().toString();

                //start generating activity
                Intent intent = new Intent(AMazeActivity.this, GeneratingActivity.class);
                intent.putExtra("SkillLevel", level);
                intent.putExtra("Builder", builder);
                intent.putExtra("Driver", driver);
                intent.putExtra("Button", "Start");
                startActivity(intent);
                Log.v("Switch", "From AMazeActivity to GeneratingActivity");
            }
        });

        //loadButton
        //once the button is pressed, send user input of skill level, builder, driver, and
        //the button being pressed to GeneratingActivity
        reloadButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view){
                //get data
                level = seekBar.getProgress();
                builder = builderSpinner.getSelectedItem().toString();
                driver = driverSpinner.getSelectedItem().toString();

                //start loading(generating) maze and navigate to generating activity
                Intent intent1 = new Intent(AMazeActivity.this, GeneratingActivity.class);
                intent1.putExtra("SkillLevel", level);
                intent1.putExtra("Builder", builder);
                intent1.putExtra("Driver", driver);
                intent1.putExtra("Button", "Reload");
                startActivity(intent1);
                Log.v("Switch", "From AMazeActivity to GeneratingActivity");
            }
        });


        Log.d(LOG_TAG,"onCreate");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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


    /**
     * This method is created to help onCreate method to initialize all the variables.
     */
    private void initializeVariables(){
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        textView = (TextView) findViewById(R.id.textView2);
        builderSpinner = (Spinner) findViewById(R.id.spinner);
        driverSpinner  = (Spinner) findViewById(R.id.spinner2);
        startButton = (Button) findViewById(R.id.button2);
        reloadButton = (Button) findViewById(R.id.button);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinateLayout);
    }


}