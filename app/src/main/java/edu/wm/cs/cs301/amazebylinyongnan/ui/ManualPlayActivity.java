package edu.wm.cs.cs301.amazebylinyongnan.ui;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Switch;

import edu.wm.cs.cs301.amazebylinyongnan.R;
import edu.wm.cs.cs301.amazebylinyongnan.falstad.BasicRobot;
import edu.wm.cs.cs301.amazebylinyongnan.falstad.ManualDriver;
import edu.wm.cs.cs301.amazebylinyongnan.falstad.MazeController;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.widget.TextView;


public class ManualPlayActivity extends AppCompatActivity {

    public static final String LOG_TAG = "ManualPlayActivity";

    private Button backButton;
    private Button shortcutButton;
    private ImageButton up;
    private ImageButton down;
    private ImageButton left;
    private ImageButton right;
    private CoordinatorLayout coordinatorLayout;
    private Switch localMap;
    private Switch globalMap;
    private Switch solution;
    private ProgressBar batteryBar;
    private MediaPlayer sound;

    //-----------------Graphics----------------
    private Bitmap bitmap;
    private Canvas canvas;
    private View gameGraphic;

    //------------Falstad Fields----------------
    MazeController controller = GeneratingActivity.getController();
    private boolean manual = false;
    private BasicRobot robot;
    private ManualDriver driver;
    private Intent intent;
    private TextView batteryNotification;
    private Handler handler = new Handler();
    private int progress;
    private int pathLength;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual_play);
        initializeVariables();

        //---------------------------------Sound------------------------------------------------
        //set up background music
        sound = new MediaPlayer();
        sound = MediaPlayer.create(this, R.raw.ph);
        sound.setAudioStreamType(AudioManager.STREAM_MUSIC);
        sound.start();

        //----------------------------MazeController, Visual------------------------------------
        //set up mazeController, mazePanel, and buttons & toggles
        intent = getIntent();
        //controller = (MazeController) intent.getSerializableExtra("MazeController");

        controller.setManualPlayActivity(this);
        setUpButtonsNToggles();

        createGraphics();
        controller.beginGraphics();
        //------------------------------Driver & Robot------------------------------------------
        //set up driver & robot
        driver = controller.getManualDriver();
        //setUpDriverNRobot();


        //-------------------------------Log----------------------------------------------------
        Log.d(LOG_TAG,"onCreate");

    }

    //------------------------------onCreate method helpers-------------------------------------
    /**
     * This method is created to help onCreate method to initialize all variables.
     */
    private void initializeVariables(){
        backButton = (Button) findViewById(R.id.button8);
        shortcutButton = (Button) findViewById(R.id.button9);
        up = (ImageButton) findViewById(R.id.button4);
        down = (ImageButton) findViewById(R.id.button5);
        left = (ImageButton) findViewById(R.id.button6);
        right = (ImageButton) findViewById(R.id.button7);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinateLayout);
        localMap = (Switch) findViewById(R.id.switch1);
        globalMap = (Switch) findViewById(R.id.switch2);
        solution = (Switch) findViewById(R.id.switch3);
        batteryBar = (ProgressBar) findViewById(R.id.progressBar2);
        gameGraphic = findViewById(R.id.gameGraphic);
        batteryNotification = (TextView) findViewById(R.id.textView4);
    }

    private void setUpButtonsNToggles(){
        //backButton
        backButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view){
                //navigate back to AMazeActivity
                Intent intent = new Intent(ManualPlayActivity.this, AMazeActivity.class);
                startActivity(intent);
                Log.v("Navigate", "From ManualPlayActivity to AMazeActivity");
            }
        });

        //shortcutButton
        shortcutButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view){
                //navigate to WinFinishActivity
                Intent intent = new Intent(ManualPlayActivity.this, WinFinishActivity.class);
                startActivity(intent);
                Log.v("Navigate", "From ManualPlayActivity to WinFinishActivity");
            }
        });

        //upButton
        up.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view){
                driver.keyDown('k');
                Log.v("Button","[UP] is pressed");
            }
        });

        //downButton
        down.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view){
                driver.keyDown('j');
                Log.v("Button","[DOWN] is pressed");
            }
        });

        //leftButton
        left.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view){
                driver.keyDown('h');
                Log.v("Button","[LEFT] is pressed");
            }
        });

        //rightButton
        right.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view){
                driver.keyDown('l');
                Log.v("Button", "[RIGHT] is pressed");
            }
        });

        //set up switches(toggles)
        localMap.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if(isChecked) {
                    controller.keyDown('m');
                    //Snackbar.make(coordinatorLayout, "Local map is now turned on.", Snackbar.LENGTH_LONG).show();
                    Log.v("Toggle","Local map is turned on");
                }else{
                    controller.keyDown('m');
                    //Snackbar.make(coordinatorLayout, "Local map is now turned off.", Snackbar.LENGTH_LONG).show();
                    Log.v("Toggle","Local map is turned off");
                }
            }
        });

        globalMap.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if(isChecked) {
                    controller.keyDown('z');
                    //Snackbar.make(coordinatorLayout, "Global map is now turned on.", Snackbar.LENGTH_LONG).show();
                    Log.v("Toggle","Global map is turned on");
                }else{
                    controller.keyDown('z');
                    //Snackbar.make(coordinatorLayout, "Global map is now turned off.", Snackbar.LENGTH_LONG).show();
                    Log.v("Toggle","Global map is turned off");
                }
            }
        });

        solution.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if(isChecked) {
                    controller.keyDown('s');
                    //Snackbar.make(coordinatorLayout, "Solution mode is now turned on.", Snackbar.LENGTH_LONG).show();
                    Log.v("Toggle","Solution mode is turned on");
                }else{
                    controller.keyDown('s');
                    //Snackbar.make(coordinatorLayout, "Solution mode is now turned off.", Snackbar.LENGTH_LONG).show();
                    Log.v("Toggle","Solution mode is turned off");
                }
            }
        });

        batteryBar.setMax(2500);
        batteryBar.setProgress(2500);
    }

    public void createGraphics(){
        bitmap = Bitmap.createBitmap(400,400,Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
        controller.getPanel().setCanvas(canvas);
        controller.getPanel().setManualPlayActivity(this);
        gameGraphic.setBackground(new BitmapDrawable(getResources(), bitmap));
        Log.v("Graphic", "Creating g1raphics");
    }

    public void navigateToWinActivity(){
        //navigate to WinFinishActivity
        Intent intent = new Intent(ManualPlayActivity.this, WinFinishActivity.class);
        intent.putExtra("pathLength",pathLength);
        intent.putExtra("batteryLevel",progress);
        startActivity(intent);
        Log.v("Navigate", "From ManualPlayActivity to WinFinishActivity");
    }

    //----------------------Battery Level & Path Length------------------------

    public void updateBatteryLevel(float batteryLevel){
        progress = (int) batteryLevel;
        if (batteryLevel > 0){


            handler.post(new Runnable(){
                public void run(){
                    batteryBar.setProgress(progress);
                    batteryNotification.setText("Battery Level:  "+ progress + " / 2500 - "+
                    (progress * 100 / 2500)+"%");
                }
            });
            try{
                Thread.sleep(20);
            }
            catch(InterruptedException e){
                e.printStackTrace();
            }


            //batteryNotification.setText("Battery Level: "+(int)(batteryLevel / 2500)+"%...");
        }
        else{
            batteryBar.setProgress(0);
            //navigate to ZeroBatteryFinishActivity
            Intent intent = new Intent(ManualPlayActivity.this, ZeroBatteryFinishActivity.class);
            startActivity(intent);
            Log.v("Navigate", "From ManualPlayActivity to ZeroBatteryFinishActivity");
        }
    }


    public void updatePathLength(int pathLength){
        this.pathLength = pathLength;
    }

    //-------------------------New methods for Graphics-------------------------------

    public void updateGraphics(){
        gameGraphic.setBackground(new BitmapDrawable(getResources(), bitmap));
    }

    public Bitmap getGroundBMP(){
        Bitmap ground = BitmapFactory.decodeResource(getResources(), R.drawable.ground2);
        return ground;
    }

    public Bitmap getSkyBMP(){
        Bitmap sky = BitmapFactory.decodeResource(getResources(), R.drawable.sky);
        return sky;
    }

    public Bitmap getWallBMP(){
        Bitmap wall = BitmapFactory.decodeResource(getResources(), R.drawable.wall);
        return wall;
    }



    //--------------------------activity lifecycle-------------------------------------


    @Override
    protected void onStart() {
        super.onStart();

        Log.d(LOG_TAG, "onStart");
    }

    @Override
    protected void onPause() {
        super.onPause();
        sound.stop();

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
