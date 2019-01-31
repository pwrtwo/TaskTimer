package dl2kten.com.s_timer;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ItemAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    private Context context;
    private String[] tasks;
    private String[] startTime;
    private String[] endTime;
    private String[] mins;
    private String[] secs;
    private Boolean[] initiated;
    private static final String MyPREFERENCES = "MyPrefs";
    private static final String KEY_RUNNING = "key_running";

    /**
     * Constructor to initialize variables
     * @param c
     * @param tasks
     * @param startTime
     * @param endTime
     * @param initiated
     */
    public ItemAdapter(Context c, String[] tasks, String[] startTime,
                       String[] endTime, String[] mins, String[] secs,
                       Boolean[] initiated) {
        this.context = c;
        this.tasks = tasks;
        this.startTime = startTime;
        this.endTime = endTime;
        this.mins = mins;
        this.secs = secs;
        this.initiated = initiated;
        //to fill in values for the task activity
        mInflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return tasks.length;
    }

    @Override
    public Object getItem(int position) {
        return tasks[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * Inflates the activity_task.xml layout
     * @param position
     * @param convertView
     * @param parent
     * @return
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = mInflater.inflate(R.layout.activity_task, null);
        TextView task = (TextView) v.findViewById(R.id.taskTextView);
        TextView numMins = (TextView) v.findViewById(R.id.numMins);
        TextView numSecs = (TextView) v.findViewById(R.id.numSecs);
        TextView startTextView = (TextView) v.findViewById(R.id.startTextView);
        TextView endTextView = (TextView) v.findViewById(R.id.endTextView);

        String minFormat = "";
        String secFormat = "";

        //add 0 if single digit
        if(Integer.parseInt(mins[position]) < 10)
            minFormat = "0";

        if(Integer.parseInt(secs[position]) < 10)
            secFormat = "0";

        minFormat += mins[position];
        secFormat += secs[position];

        task.setText(tasks[position]);
        numMins.setText(minFormat);
        numSecs.setText(secFormat);
        startTextView.setText(startTime[position]);
        endTextView.setText(endTime[position]);

        SharedPreferences prefs = context.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        String running = prefs.getString(KEY_RUNNING, "Huh?");

        //If app is closed and restarted want itemadapter to reflect running state
        //Happened when app froze and restarted main_activity showed if task was finished running
        if(running.equals("Huh?") || running.equals("false")) {
            if (initiated[position]) {
                v.setBackgroundColor(context.getResources().getColor(R.color.item));
            } else {
                v.setBackgroundColor(context.getResources().getColor(R.color.primaryoffset));
            }
        } else {
            if(position == Integer.parseInt(running)) {
                v.setBackgroundColor(context.getResources().getColor(R.color.pressedOffset));
            }
        }

        return v;
    }
}
