package edu.wm.cs.cs301.amazebylinyongnan.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import edu.wm.cs.cs301.amazebylinyongnan.R;
import edu.wm.cs.cs301.amazebylinyongnan.falstad.MazeController;

public class GeneratingActivity extends AppCompatActivity {

    public static final String LOG_TAG = "GeneratingActivity";



    private Button backButton;
    private ProgressBar progressBar;
    private TextView textView;
    private int progress = 0;
    private Handler handler = new Handler();
    private boolean back;
    //private boolean manual;
    private CoordinatorLayout coordinatorLayout;
    private boolean readSave = false;
    private int level;
    private String builder;
    private String driver;
    private String button;
    private String dots = ".";
    private static MazeController controller;
    private Handler progressBarHandler;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generating);
        initializeVariables();


        //get user input from AMazeActivity
        Intent userIntent = getIntent();
        level = userIntent.getIntExtra("SkillLevel", 0);
        builder = userIntent.getStringExtra("Builder");
        driver = userIntent.getStringExtra("Driver").toString();
        button = userIntent.getStringExtra("Button").toString();

        if (button.equals("Reload")){
            if(level <= 3 && readSave){
                controller = new MazeController(getFilesDir() + "Maze"+Integer.toString(level)+".xml");
                controller.setUpInputsInController(level, builder, driver);
                controller.setGeneratingActivity(this);

                controller.init();
                progressBarHandler = new Handler();
                Log.v("Reload", "Read file of maze level: "+Integer.toString(level));
            }
            else{
                controller = new MazeController();
                controller.setUpInputsInController(level, builder, driver);
                controller.setGeneratingActivity(this);

                //controller.generateMaze();
                controller.init();

                progressBarHandler = new Handler();
                Log.v("Too large to reload", "Generate new maze with level: "+Integer.toString(level));
            }

        }
        else{
            controller = new MazeController();
            controller.setUpInputsInController(level, builder, driver);
            controller.setGeneratingActivity(this);

            //controller.generateMaze();
            controller.init();

            progressBarHandler = new Handler();
            Log.v("Generate", "New maze with level: "+Integer.toString(level));
            //setUpProgressBar();
        }


        Log.d(LOG_TAG,"onCreate");

    }

    /**
     * This method is created to help onCreate method to initialize all the variables.
     */
    private void initializeVariables(){
        backButton = (Button) findViewById(R.id.button3);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        textView = (TextView) findViewById(R.id.textView3);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinateLayout);
    }

    /**
     * This method is for the special dot effect during the loading phase.
     * @param num
     */
    private void getDots(int num){
        if(num % 30 == 0){
            dots = ".";
        }
        else if(num % 30 == 10){
            dots = "..";
        }
        else if(num % 30 == 20){
            dots = "...";
        }
    }

    public void startPlayActivity() {
        //if manual then go to manualplayactivity
        if (!back && driver.equals("Manual")) {
            Intent intent = new Intent(GeneratingActivity.this, ManualPlayActivity.class);
            //intent.putExtra("MazeController", controller);
            startActivity(intent);
            Log.v("Navigate", "From GeneratingActivity to ManualPlayActivity");
        }
        //if auto then go to autoplayactivity
        else if (!back && !driver.equals("Manual")) {
            Intent intent2 = new Intent(GeneratingActivity.this, AutoPlayActivity.class);
            //intent2.putExtra("MazeController", controller);
            startActivity(intent2);
            Log.v("Navigate", "From GeneratingActivity to AutoPlayActivity");
        }
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

    //----------------- progress bar -----------------------------------

    public void updatePercentage(int percent){
        progress = percent;
        //special effect
        getDots(progress);
        handler.post(new Runnable(){
            public void run(){
                progressBar.setProgress(progress);
                textView.setText("Entering the Matrix " + progress + "% " + dots);
            }
        });
        try{
            Thread.sleep(10);
        }
        catch(InterruptedException e){
            e.printStackTrace();
        }

        //if user press backButton, navigate back to AMazeActivity
        backButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view){
                //navigate back to AMazeActivity
                Intent intent = new Intent(GeneratingActivity.this, AMazeActivity.class);
                intent.putExtra("From", "GeneratingActivity");
                startActivity(intent);
                back = true;
                Log.v("Navigate", "From GeneratingActivity to AMazeActivity");
            }
        });

    }

    //public Handler getProgressBarHandler(){
    //    return progressBarHandler;
    //}

    //public ProgressBar getProgressBar(){
    //    return progressBar;
    //}

    //----------------------- controller globalize ---------------
    public static MazeController getController(){
        return controller;
    }
    public static void setController(MazeController controller){
        GeneratingActivity.controller = controller;
    }

}
