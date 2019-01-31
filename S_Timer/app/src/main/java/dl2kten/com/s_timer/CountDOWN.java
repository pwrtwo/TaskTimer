package dl2kten.com.s_timer;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.concurrent.ExecutionException;

public class CountDOWN extends AppCompatActivity {

    private TextView timeTextView;
    private Button startBtn;
    private Button completeBtn;
    private CountDownTimer countDownTimer;
    private long timeLeft;
    private boolean runTimer;
    private Task task;
    private SharedPreferences prefs;
    private TrackTimer trackTimer;
    private static final String MyPREFERENCES = "MyPrefs";
    private static final String KEY_RUNNING = "key_running";
    private static final String KEY_ASYNCTIMERSTART = "key_asyncTimerStart";
    private static final String KEY_TIMELEFT = "key_timeLeft";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_count_down2);

        //Initialize components
        timeTextView = (TextView) findViewById(R.id.timeTextView);
        startBtn = (Button) findViewById(R.id.startBtn);
        completeBtn = (Button) findViewById(R.id.completeBtn);
        prefs = getSharedPreferences(MyPREFERENCES, MODE_PRIVATE);

        Intent in = getIntent();
        task = in.getParcelableExtra("dl2kten.com.s_timer.task");

        //Keeps track of time in background in case nagivate away from activity
        trackTimer = new TrackTimer();

        timeLeft = 0;
        runTimer = true;

        //check to see if a task is already started
        Boolean running = prefs.getBoolean(KEY_ASYNCTIMERSTART, false);

        if(!running) {
            //if haven't started timer set how much time is on timer
            getTime(task);
        } else {
            timeLeft = prefs.getInt(KEY_TIMELEFT, 0);
            startTimer();
        }

        updateTimer();

        //Button listeners
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTimer();
            }
        });

        completeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = prefs.edit();

                editor.putBoolean(KEY_ASYNCTIMERSTART, false);
                editor.putString(KEY_RUNNING, "false");
                editor.commit();

                if(timeLeft >= 0) {
                    //pass whether task was completed or not on to rewards class
                    Intent finished = new Intent(getApplicationContext(), Rewards.class);
                    startActivity(finished);
                } else {
                    Intent finished = new Intent(getApplicationContext(), TryAgain.class);
                    startActivity(finished);
                }

                countDownTimer.cancel();
            }
        });
    }

    /**
     * Runs timer
     */
    private void startTimer() {

        Boolean track = prefs.getBoolean(KEY_ASYNCTIMERSTART, false);

        if(!track || trackTimer.getStatus() != AsyncTask.Status.RUNNING) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(KEY_ASYNCTIMERSTART, true);
            editor.commit();
            int time = (int) timeLeft;
            trackTimer.execute(time);
        }

        if(runTimer) {
            countDownTimer = new CountDownTimer(timeLeft, 100) {
                @Override
                public void onTick(long millisUntilFinished) {
                    timeLeft = millisUntilFinished;
                    updateTimer();
                }

                @Override
                public void onFinish() {
                    //If timer runs out then did not finish on time
                    SharedPreferences.Editor editor = prefs.edit();

                    editor.putBoolean(KEY_ASYNCTIMERSTART, false);
                    editor.putString(KEY_RUNNING, "false");
                    editor.commit();

                    Intent finished = new Intent(getApplicationContext(), TryAgain.class);
                    startActivity(finished);
                }
            }.start();

            startBtn.setText("Pause");
            runTimer = false;
        } else {
            countDownTimer.cancel();
            startBtn.setText("Resume");
            runTimer = true;
        }

    }

    /**
     * Updates textview with count down
     */
    private void updateTimer() {
        int hours = (int) timeLeft / (60000 * 60);
        int mod = (int) timeLeft % (60000 * 60);
        int minutes = mod / 60000;
        mod = mod % 60000;
        int seconds = mod / 1000;
        mod = mod % 1000;
        int ms = mod / 100;

        String timeLeftText = "";

        if(hours < 10)
            timeLeftText += "0";

        timeLeftText += hours + ":";
        //adds 0 in front if single digit
        if(minutes < 10)
            timeLeftText += "0";

        timeLeftText += minutes + ":";

        if(seconds < 10)
            timeLeftText += "0";

        timeLeftText += seconds + "." + ms;

        timeTextView.setText(timeLeftText);
    }

    /**
     * Get the specific times of chosen task
     * @param task
     * @return
     */
    private void getTime(Task task) {
        int[][] times= new int[1][2];
        task.calcDuration();
        times[0][0] = Integer.parseInt(task.getMins());
        times[0][1] = Integer.parseInt(task.getSecs());

        timeLeft += times[0][0] * 60;
        timeLeft += times[0][1];
        timeLeft *= 1000;
    }


    @Override
    public void onBackPressed() {
        Intent in = new Intent(this, MainActivity.class);
        if(countDownTimer != null) {
            countDownTimer.cancel();
        }
        startActivity(in);
    }
    /**
     *
     */
    private class TrackTimer extends AsyncTask<Integer, Integer, Integer> {

        @Override
        protected Integer doInBackground(Integer... integers) {

            try {
                while(integers[0] > 0) {
                    Thread.sleep(1000);
                    integers[0] -= 1000;
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putInt(KEY_TIMELEFT, integers[0]);
                    editor.commit();
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            return integers[0];
        }

        protected void onPreExecute() {

        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
        }

        @Override
        protected void onPostExecute(Integer result) {
            SharedPreferences.Editor editor = prefs.edit();

            editor.putBoolean(KEY_ASYNCTIMERSTART, false);
            editor.putString(KEY_RUNNING, "false");
            editor.commit();

            cancel(true);
        }

    }


}
