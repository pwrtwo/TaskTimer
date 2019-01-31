package dl2kten.com.s_timer;

import android.os.Parcel;
import android.os.Parcelable;
import android.view.WindowManager;

public class Task implements Parcelable{

    private String task;
    private String startTime;
    private String endTime;
    private String hour, mins, secs;
    private Boolean initiated;

    public Task(String task, String startTime, String endTime) {
        this.task = task;
        this.startTime = startTime;
        this.endTime = endTime;
        initiated = false;
    }

    public Task(Parcel in) {
        String[] data = new String[4];
        in.readStringArray(data);

        task = data[0];
        startTime = data[1];
        endTime = data[2];
        initiated = Boolean.valueOf(data[3]);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(new String[]{task, startTime, endTime, String.valueOf(initiated)});
    }

    public static final Creator<Task> CREATOR = new Parcelable.Creator<Task>() {

        @Override
        public Task createFromParcel(Parcel source) {
            return new Task(source);
        }

        @Override
        public Task[] newArray(int size) {
            return new Task[size];
        }

    };

    public Boolean getInitiated() {
        return initiated;
    }

    public void setInitiated(Boolean initiated) {
        this.initiated = initiated;
    }

    public String getTask() {
        return task;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public String getMins() {
        return mins;
    }

    public void setMins(String mins) {
        this.mins = mins;
    }

    public String getSecs() {
        return secs;
    }

    public void setSecs(String secs) {
        this.secs = secs;
    }

    public String getHour() {
        return hour;
    }

    public void setHour(String hour) {
        this.hour = hour;
    }

    /**
     * Calculate the minutes and seconds from start and end time
     * @return
     */
    public void calcDuration() {
        int time[] = new int[3];
        String[] startSections = startTime.split(":");
        String[] endSections = endTime.split(":");
        String[] durationSections = new String[startSections.length];

        for(int i = 0; i < startSections.length; i++) {
            int difference = Integer.parseInt(endSections[i]) -
                    Integer.parseInt(startSections[i]);

            durationSections[i] = Integer.toString(difference);
            time[i] = difference;
        }

        int minDifference = time[0] * 60 + time[1];
        int secDifference = time[2];

        hour = Integer.toString(time[0]);
        mins = Integer.toString(minDifference);
        secs = Integer.toString(secDifference);
    }
}
