package edu.wm.cs.cs301.amazebylinyongnan.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;

import edu.wm.cs.cs301.amazebylinyongnan.R;
import edu.wm.cs.cs301.amazebylinyongnan.falstad.BasicRobot;
import edu.wm.cs.cs301.amazebylinyongnan.falstad.ManualDriver;
import edu.wm.cs.cs301.amazebylinyongnan.falstad.MazeController;
import edu.wm.cs.cs301.amazebylinyongnan.falstad.RobotDriver;

public class AutoPlayActivity extends AppCompatActivity {

    public static final String LOG_TAG = "AutoPlayActivity";


    private Button backButton;
    private Button shortcutButton;
    private Button zeroShortCutButton;
    private Button resume;
    private Button pause;
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
    private RobotDriver driver;
    private Intent intent;
    private TextView batteryNotification;
    private Handler handler = new Handler();
    private int progress;
    private int pathLength;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto_play);
        initializeVariables();

        //set up background music
        sound = new MediaPlayer();
        sound = MediaPlayer.create(this, R.raw.ph);
        sound.setAudioStreamType(AudioManager.STREAM_MUSIC);
        //decomment the line below if want to play the background music
        sound.start();

        //----------------------------MazeController, Visual------------------------------------
        //set up mazeController, mazePanel, and buttons & toggles
        intent = getIntent();
        //controller = (MazeController) intent.getSerializableExtra("MazeController");

        controller.setAutoPlayActivity(this);
        setUpButtonsNToggles();

        createGraphics();
        controller.beginGraphics();
        //------------------------------Driver & Robot------------------------------------------
        //set up driver & robot

        driver = controller.getAutoDriver();

        //-------------------------------------------------------------

        /*
        try{
            driver.drive2Exit();
        } catch(Exception e){};
        */
        //setUpDriverNRobot();



        Log.d(LOG_TAG,"onCreate");
    }

    /**
     * This method is created to help onCreate method to initialize all variables.
     */
    private void initializeVariables(){
        backButton = (Button) findViewById(R.id.button8);
        shortcutButton = (Button) findViewById(R.id.button9);
        resume = (Button) findViewById(R.id.button11);
        pause = (Button) findViewById(R.id.button10);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinateLayout);
        localMap = (Switch) findViewById(R.id.switch1);
        globalMap = (Switch) findViewById(R.id.switch2);
        solution = (Switch) findViewById(R.id.switch3);
        batteryBar = (ProgressBar) findViewById(R.id.progressBar2);
        gameGraphic = findViewById(R.id.gameGraphic);
        batteryNotification = (TextView) findViewById(R.id.textView4);

    }

    private void setUpButtonsNToggles() {
        //backButton
        backButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view){
                //navigate back to AMazeActivity
                Intent intent = new Intent(AutoPlayActivity.this, AMazeActivity.class);
                startActivity(intent);
                Log.v("Switch","From AutoPlayActivity to AMazeActivity");
            }
        });

        //shortcutButton
        shortcutButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view){
                //navigate to WinFinishActivity
                Intent intent = new Intent(AutoPlayActivity.this, WinFinishActivity.class);
                startActivity(intent);
                Log.v("Switch","From AutoPlayActivity to WinFinishActivity");
            }
        });



        //startButton
        resume.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view){
                /*
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            driver.drive2Exit();
                        } catch(Exception e){};
                    }
                }, 50);
                */
                driver.resumeDrive();
                Log.v("Button","[Resume] is pressed");
            }
        });

        //pauseButton
        pause.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view){
                driver.pauseDrive();
                Log.v("Button","[Pause] is pressed");
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
        controller.getPanel().setAutoPlayActivity(this);
        gameGraphic.setBackground(new BitmapDrawable(getResources(), bitmap));
    }

    public void navigateToWinActivity(){
        //navigate to WinFinishActivity
        Intent intent = new Intent(AutoPlayActivity.this, WinFinishActivity.class);
        intent.putExtra("pathLength",pathLength);
        intent.putExtra("batteryLevel",progress);
        startActivity(intent);
        Log.v("Switch", "From ManualPlayActivity to WinFinishActivity");
    }


    //----------------------Battery Level & Path Length------------------------

    public void updateBatteryLevel(float batteryLevel){
        progress = (int) batteryLevel;
        if (batteryLevel > 0){


            handler.post(new Runnable(){
                public void run(){
                    batteryBar.setProgress(progress);
                    batteryNotification.setText("Battery Level: "+ progress + " / 2500 - "+
                            (progress * 100 / 2500)+"%");
                }
            });
            try{
                Thread.sleep(10);
            }
            catch(InterruptedException e){
                e.printStackTrace();
            }


            //batteryNotification.setText("Battery Level: "+(int)(batteryLevel / 2500)+"%...");
        }
        else{
            batteryBar.setProgress(0);
            //navigate to ZeroBatteryFinishActivity
            Intent intent = new Intent(AutoPlayActivity.this, ZeroBatteryFinishActivity.class);
            startActivity(intent);
            Log.v("Switch", "From ManualPlayActivity to ZeroBatteryFinishActivity");
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




    //--------------------------------------------------------------------------------




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
        sound.stop();

        driver.pauseDrive();

        Log.d(LOG_TAG,"onStop");
    }

}
