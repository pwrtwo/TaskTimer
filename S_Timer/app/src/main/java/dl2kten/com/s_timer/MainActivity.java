package dl2kten.com.s_timer;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    private ListView myListView;
    private Button stopAlarmBtn;
    private Task[] tasks;
    private SharedPreferences prefs;
    private AlarmManager am;
    private static final Handler updateHandler = new Handler();
    private static final Handler invalidateHandler = new Handler();
    private Runnable r;
    private boolean syncWithServer;
    private static final String MyPREFERENCES = "MyPrefs";
    private static final String KEY_TASK = "key_task";
    private static final String KEY_DONE = "key_done";
    private static final String KEY_RUNNING = "key_running";
    private static final String KEY_MISSEDALARM = "key_missedAlarm";
    private static final String KEY_STARTTIME = "key_startTime";
    private static final String KEY_ENDTIME = "key_endTime";
    private static final String KEY_EMAIL = "key_email";
    private static final String KEY_ALARM = "key_alarm";
    private static final String KEY_UPDATE = "key_update";
    private static final String KEY_INVALIDATE = "key_invalidate";
    private static final String MYID = "MyID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // hide status bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);


        Resources res = getResources();
        myListView = (ListView) findViewById(R.id.myListView);
        stopAlarmBtn = (Button) findViewById(R.id.stopAlarmButton);
        prefs = getSharedPreferences(MyPREFERENCES, MODE_PRIVATE);

        syncWithServer = false;

        getData();
        showTasks();
        //start background threads, daily task update, alarm, and invalidate tasks
        setUpdate();
        invalidateItems();
        startBackgroundA();

        myListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Check to see if a task if already running
                SharedPreferences.Editor editor = prefs.edit();
                String running = prefs.getString(KEY_RUNNING, "Huh?");

                //Check to see if task already ran
                Boolean ran = alreadyRan(position);

                if(!ran) {
                    if(running.equals("Huh?") || running.equals("false")
                            || running.equals(Integer.toString(position))) {
                        //Get list of done tasks and add to it

                        String done = prefs.getString(KEY_DONE, "Huh?");

                        if(!done.equals("Huh?")) {
                            done += ",";
                            done += Integer.toString(position);
                        } else {
                            done = Integer.toString(position);
                        }

                        editor.putString(KEY_DONE, done);
                        editor.putString(KEY_RUNNING, Integer.toString(position));
                        editor.commit();

                        Intent countDown = new Intent(getApplicationContext(), CountDOWN.class);
                        countDown.putExtra("dl2kten.com.s_timer.task", tasks[position]);
                        startActivity(countDown);
                    }
                }

            }
        });


        stopAlarmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopService(new Intent(MainActivity.this, TimerService.class));
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.syncItem) {
            sync();
            return true;
        }

        if(id == R.id.logoutItem) {
            logout();
            return true;
        }

        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        updateHandler.removeCallbacks(r);
        //invalidateHandler uses anonymous runnable, should I change it?
        invalidateHandler.removeCallbacksAndMessages(null);
    }

    /**
     * Set task objects to contain task name, start time and end time, and description
     */
    private void getData() {

        String[] allTasks = prefs.getString(KEY_TASK, "Huh?").split(",");
        String[] allST = prefs.getString(KEY_STARTTIME, "Huh?").split(",");
        String[] allET = prefs.getString(KEY_ENDTIME, "Huh?").split(",");

        String done = prefs.getString(KEY_DONE, "Huh?");

        if(!allTasks[0].equals("Huh?")) {
            tasks = new Task[allTasks.length];

            //take out spaces at beginning if it exists and initialize tasks
            for (int i = 0; i < tasks.length; i++) {
                allTasks[i] = allTasks[i].trim();
                allST[i] = allST[i].trim();
                allET[i] = allET[i].trim();

                tasks[i] = new Task(allTasks[i], allST[i], allET[i]);

            }

            //If there are tasks that are already done set initiated to true
            if (!done.equals("Huh?") && !done.equals("false")) {
                String[] tasksFinished = done.split(",");
                int[] doneNumber = new int[tasksFinished.length];

                for (int i = 0; i < tasksFinished.length; i++) {
                    tasksFinished[i] = tasksFinished[i].trim();
                    doneNumber[i] = Integer.parseInt(tasksFinished[i]);
                }

                for (int i = 0; i < doneNumber.length; i++) {
                    tasks[doneNumber[i]].setInitiated(true);
                }
            }
        } else {
            tasks = null;
        }
    }

    /**
     * Display three tasks that needs to be completed and all
     * completed tasks, fill itemadapter
     */
    private void showTasks()
    {
        SharedPreferences prefs = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        ArrayList<Task> shownTasks = new ArrayList<Task>();
        boolean withinRange = true;
        int count = 0;

        if(tasks != null) {
            for (int i = 0; i < tasks.length; i++) {
                if (tasks[i].getInitiated()) {
                    shownTasks.add(tasks[i]);
                } else {
                    withinRange = checkBeforeTime(tasks[i]);
                    if (count < 3 && withinRange) {
                        shownTasks.add(tasks[i]);
                        count++;
                    }
                }
            }

            String[] tempTasks = new String[shownTasks.size()];
            String[] tempStartTime = new String[shownTasks.size()];
            String[] tempEndTime = new String[shownTasks.size()];
            String[] tempMins = new String[shownTasks.size()];
            String[] tempSecs = new String[shownTasks.size()];
            Boolean[] tempInitiated = new Boolean[shownTasks.size()];

            for (int i = 0; i < tempTasks.length; i++) {
                tasks[i].calcDuration();
                tempTasks[i] = tasks[i].getTask();
                tempStartTime[i] = tasks[i].getStartTime();
                tempEndTime[i] = tasks[i].getEndTime();
                tempMins[i] = tasks[i].getMins();
                tempSecs[i] = tasks[i].getSecs();
                tempInitiated[i] = tasks[i].getInitiated();
            }

            ItemAdapter itemAdapter = new ItemAdapter(this, tempTasks, tempStartTime, tempEndTime,
                    tempMins, tempSecs, tempInitiated);
            int length = itemAdapter.getCount();
            myListView.setAdapter(itemAdapter);
        } else {
            String[] tempTasks = {"Updating tasks"};
            String[] tempStartTime = {"0:0:0"};
            String[] tempEndTime = {"0:0:0"};
            String[] tempMins = {"0"};
            String[] tempSecs = {"0"};
            Boolean[] tempInitiated = {false};

            ItemAdapter itemAdapter = new ItemAdapter(this, tempTasks, tempStartTime, tempEndTime,
                    tempMins, tempSecs, tempInitiated);
            myListView.setAdapter(itemAdapter);
        }
    }

    /**
     *  Checks to see if a task had already been completed
     * @param position
     * @return
     */
    private Boolean alreadyRan(int position) {
        String done = prefs.getString(KEY_DONE, "Huh?");
        String running = prefs.getString(KEY_RUNNING, "Huh?");

        if(done.equals("Huh?")) {
            return false;
        }

        if(running.equals("Huh?") || running.equals("false")) {
            String[] doneTasks = done.split(",");
            for(int i = 0; i < doneTasks.length; i++) {
                if(position == Integer.parseInt(doneTasks[i])) {
                    return true;
                }
            }
        } else {
            if(position == Integer.parseInt(running)) {
                return false;
            }
        }

        return false;
    }

    /**
     *
     */
    private void logout() {
        Intent in = new Intent(this, LaunchActivity.class);
        SharedPreferences sharedPreferences = getSharedPreferences(MYID, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(MYID);
        editor.clear();
        editor.commit();

        startActivity(in);
    }

    /**
     *
     */
    private void sync() {
        try {
            syncWithServer = true;
            new PullData().execute(0).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }


    }

    /**
     * Runs alarm manager in background to wake up and start service at
     * certain times
     */
    private void startBackgroundA() {
        Calendar calendar = Calendar.getInstance();
        Calendar check = Calendar.getInstance();

        check.setTimeInMillis(System.currentTimeMillis());
        calendar.setTimeInMillis(System.currentTimeMillis());

        String alarms = prefs.getString(KEY_ALARM, "Huh?");
        String startTimes = prefs.getString(KEY_STARTTIME,"Huh?");

        if(!alarms.equals("Huh?")) {
            String[] alarmTimes = alarms.split(",");
            String[] startTime = startTimes.split(",");
            for(int i = 0; i < alarmTimes.length; i++) {
                if(alarmTimes[i].equals("1")) {
                    String[] sections = startTime[i].split(":");

                    int minute = Integer.parseInt(sections[1]);
                    if(minute == 0) {
                        minute = 60;
                        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(sections[0]) - 1);
                    } else {
                        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(sections[0]));
                    }

                    calendar.set(Calendar.MINUTE, minute - 1);
                    calendar.set(Calendar.SECOND, Integer.parseInt(sections[2]));
                    calendar.set(Calendar.MILLISECOND, 0);

                    //So that it doesn't start after the set time, before was starting after set time
                    //ie, set at 5pm turn on app at 5:10 starts alarm
                    if (calendar.before(check)) {

                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putBoolean(KEY_MISSEDALARM, true);
                        editor.commit();//might pop up a dialog or something to inform an alarm was missed
                    } else {

                        am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                        Intent in = new Intent(this, AlarmReceiver.class);
                        final int id = (int) System.currentTimeMillis();
                        PendingIntent pin = PendingIntent.getBroadcast(this, id, in, 0);

                        am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pin);
                    }


                }
            }

        }
    }

    /**
     * Sets up daily wipe of sharedpreferences and data pulling
     */
    private void setUpdate() {
        final long delay = 1000;
        //final Handler h = new Handler(Looper.getMainLooper());

        r = new Runnable() {
            @Override
            public void run() {
                SharedPreferences sharedPreferences = getSharedPreferences(MyPREFERENCES,
                        Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();

                Calendar calendar = Calendar.getInstance();

                calendar.setTimeInMillis(System.currentTimeMillis());
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int min = calendar.get(Calendar.MINUTE);

                if(hour == 23 && min == 59) {

                    editor.remove(MyPREFERENCES);
                    editor.clear();
                    editor.commit();

                    getData();
                    showTasks();
                }

                if(hour == 0 && min == 1) {
                    try {
                        new PullData().execute(0).get();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }

                    getData();
                    showTasks();
                }
                updateHandler.postDelayed(this, delay);
            }
        };

        updateHandler.post(r);
    }

    /**
     * Checks time interval of tasks, if current time too early or too much time has
     * passed after end time task is either not displayed or changed to done
     */
    private void invalidateItems() {
        //final Handler handler = new Handler();
        boolean b = invalidateHandler.postDelayed(new Runnable() {
            @Override
            public void run() {

                SharedPreferences.Editor editor = prefs.edit();

                String invalidate = prefs.getString(KEY_INVALIDATE, "Huh?");
                if(invalidate.equals("Huh?")) {
                    editor.putString(KEY_INVALIDATE, "true");
                    editor.commit();
                }

                String ran = prefs.getString(KEY_DONE, "Huh?");

                if(!ran.equals("Huh?")) {
                    String[] tasksOver = ran.split(",");

                    int[] tasksDone = new int[tasksOver.length];

                    for (int i = 0; i < tasksOver.length; i++) {
                        tasksDone[i] = Integer.parseInt(tasksOver[i]);
                    }

                    for (int i = 0; i < tasks.length; i++) {
                        boolean completed = false;
                        //check to see if any tasks are still undone
                        for (int j = 0; j < tasksDone.length; j++) {
                            if (i == tasksDone[j])
                                completed = true;
                        }

                        if (!completed) {
                            if (checkGonePastTime(tasks[i])) {
                                ran += "," + Integer.toString(i);
                                editor.putString(KEY_DONE, ran);
                                editor.commit();
                                getData();
                                showTasks();
                            }

                        }
                    }
                } else {
                    if(tasks!=null) {
                        for (int i = 0; i < tasks.length; i++) {

                            if (checkGonePastTime(tasks[i])) {
                                if(ran.equals("Huh?")) {
                                    ran = Integer.toString(i);
                                } else {
                                    ran += "," + Integer.toString(i);
                                }
                                editor.putString(KEY_DONE, ran);
                                editor.commit();
                                getData();
                                showTasks();
                            }

                        }
                    }
                }

                //update showTasks
                getData();
                showTasks();
                //check every 30mins 1800000
                invalidateHandler.postDelayed(this, 1800000);
            }
        }, 1000);
    }

    /**
     *
     * @param task
     * @return
     */
    private boolean checkGonePastTime(Task task) {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        int minute = Calendar.getInstance().get(Calendar.MINUTE);

        String endTime = task.getEndTime();
        String[] sections = endTime.split(":");
        int taskHours = Integer.parseInt(sections[0]);
        int taskMinutes = Integer.parseInt(sections[1]);

        int taskDifference = hour - taskHours;
        int minDifference = minute - taskMinutes;

        taskDifference = taskDifference * 60 + minDifference;

        //change this to 90 later
        if(taskDifference > 90) {
            return true;
        }
        return false;
    }

    /**
     *
     * @param task
     * @return
     */
    private boolean checkBeforeTime(Task task) {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        int minute = Calendar.getInstance().get(Calendar.MINUTE);

        String startTime = task.getStartTime();
        String[] sections = startTime.split(":");
        int taskHours = Integer.parseInt(sections[0]);
        int taskMinutes = Integer.parseInt(sections[1]);
        int taskDifference = taskHours - hour;
        int minuteDifference = taskMinutes - minute;
        taskDifference = taskDifference * 60 + minuteDifference;

        if(taskDifference < 120) {
            return true;
        }

        return false;
    }

    /**
     * Pulls data from database
     */
    private class PullData extends AsyncTask<Integer, Integer, String> {

        @Override
        protected String doInBackground(Integer... integers) {

            Data data = new Data();

            SharedPreferences prefs = getSharedPreferences(MYID, Context.MODE_PRIVATE);

            String email = prefs.getString(KEY_EMAIL, "Huh?");
            String result = data.pullData(email);

            data.parseJSON(result);

            if(!syncWithServer) {
                SharedPreferences sharedPreferences = getSharedPreferences(MyPREFERENCES,
                        Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();

                editor.putString(KEY_TASK, data.getTasks());
                editor.putString(KEY_STARTTIME, data.getStartTimes());
                editor.putString(KEY_ENDTIME, data.getEndTimes());
                editor.commit();
            } else {
                processData(data);
                syncWithServer = false;
            }

            return "pass";

        }

        protected void onPreExecute() {

        }

        @Override
        protected void onProgressUpdate(Integer... progress) {

        }

        @Override
        protected void onPostExecute(String result) {

            cancel(true);
        }

        private void processData(Data d) {
            String tasks = d.getTasks();
            String startTime = d.getStartTimes();
            String endTime = d.getEndTimes();
            String alarmTime = d.getAlarm();
            String[] taskStartTimes = startTime.split(",");
            String[] taskEndTimes = endTime.split(",");
            String[] taskTasks = tasks.split(",");
            String[] taskAlarm = alarmTime.split(",");
            String newEndTimes = "";
            String newStartTimes = "";
            String newTasks = "";
            String newAlarm = "";

            int count = 0;

            int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
            int minute = Calendar.getInstance().get(Calendar.MINUTE);

            if(startTime!=null) {

                for (int i = 0; i < taskStartTimes.length; i++) {
                    //splits 00:00:00 to hour, minute, seconds
                    String[] sections = taskStartTimes[i].split(":");
                    if (hour < Integer.parseInt(sections[0]) || (hour == Integer.parseInt(sections[0])
                        && minute < Integer.parseInt(sections[1]))) {

                            if(i == 0) {
                                newStartTimes += taskStartTimes[i];
                                newEndTimes += taskEndTimes[i];
                                newTasks += taskTasks[i];
                                newAlarm += taskAlarm[i];
                            } else {
                                newStartTimes += "," + taskStartTimes[i];
                                newEndTimes += "," + taskEndTimes[i];
                                newTasks += "," + taskTasks[i];
                                newAlarm += "," + taskAlarm[i];
                            }
                    }
                }

                count = 0;
            }

            SharedPreferences sharedPreferences = getSharedPreferences(MyPREFERENCES,
                    Context.MODE_PRIVATE);

            String previousStartTimes = sharedPreferences.getString(KEY_STARTTIME, "Huh?");
            String previousEndTimes = sharedPreferences.getString(KEY_ENDTIME, "Huh?");
            String previousTasks = sharedPreferences.getString(KEY_TASK, "Huh?");
            String previousAlarms = sharedPreferences.getString(KEY_ALARM, "Huh?");

            taskStartTimes = previousStartTimes.split(",");
            taskEndTimes = previousEndTimes.split(",");
            taskTasks = previousTasks.split(",");
            taskAlarm = previousAlarms.split(",");
            String oldEndTimes = "";
            String oldStartTimes = "";
            String oldTasks = "";
            String oldAlarms = "";

            if(!previousStartTimes.equals("Huh?")) {

                for (int i = 0; i < taskStartTimes.length; i++) {
                    //splits 00:00:00 to hour, minute, seconds
                    String[] sections = taskStartTimes[i].split(":");
                    if (hour > Integer.parseInt(sections[0]) || (hour == Integer.parseInt(sections[0])
                            && minute > Integer.parseInt(sections[1]))) {
                            if(i == 0) {
                                oldStartTimes += taskStartTimes[i];
                                oldEndTimes += taskEndTimes[i];
                                oldTasks += taskTasks[i];
                                oldAlarms += taskAlarm[i];
                            } else {
                                oldStartTimes += "," + taskStartTimes[i];
                                oldEndTimes += "," + taskEndTimes[i];
                                oldTasks += "," + taskTasks[i];
                                oldAlarms += "," + taskAlarm[i];
                            }
                    }
                }
                count = 0;
            }

            if(!newTasks.equals("") && !oldTasks.equals("")) {

                oldTasks += newTasks;
                oldStartTimes += newStartTimes;
                oldEndTimes += newEndTimes;
                oldAlarms += newAlarm;

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(KEY_TASK, oldTasks);
                editor.putString(KEY_STARTTIME, oldStartTimes);
                editor.putString(KEY_ENDTIME, oldEndTimes);
                editor.putString(KEY_ALARM, oldAlarms);
                editor.commit();
            }

        }

    }


}