package dl2kten.com.s_timer;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class Data {

    private URL url;
    private String requesturl, tasks, startTimes, endTimes, alarm;
    private static final String MyPREFERENCES = "MyPrefs";
    private static final String KEY_TASK = "key_task";
    private static final String KEY_DESC = "key_desc";
    private static final String KEY_STARTTIME = "key_startTime";
    private static final String KEY_ENDTIME = "key_endTime";

    public Data() {
        tasks = "";
        startTimes = "";
        endTimes = "";
        alarm = "";
    }

    public Data(URL url) {

        this.url = url;
        tasks = "";
        startTimes = "";
        endTimes = "";
        alarm = "";
    }

    /**
     *
     * @return
     */
    public String getJSON() {
        HttpURLConnection connection = null;
        BufferedReader reader = null;


        try {
            connection = (HttpURLConnection) url.openConnection();
            //connection.connect();
            connection.setRequestMethod("GET");

            InputStream is = new BufferedInputStream(connection.getInputStream());

            reader = new BufferedReader(new InputStreamReader(is));

            StringBuffer sb = new StringBuffer();
            String line = "";

            while((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
                Log.d("Response: ", "> " + line);
            }

            return sb.toString();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(connection != null) {
                connection.disconnect();
            }

            if(reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return null;
    }

    /**
     *
     * @param input
     */
    public void parseJSON(String input) {
        try {
            JSONObject jo = new JSONObject(input);
            JSONArray jsonArray = jo.getJSONArray("data");

            for(int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);

                if(i == jsonArray.length() - 1) {
                    //parsing based on , so this avoids an extra task
                    tasks += jsonObject.getString("description");
                    startTimes += jsonObject.getString("start");
                    endTimes += jsonObject.getString("end");
                    alarm += jsonObject.getString("alarm");
                } else {
                    tasks += jsonObject.getString("description") + ",";
                    startTimes += jsonObject.getString("start") + ",";
                    endTimes += jsonObject.getString("end") + ",";
                    alarm += jsonObject.getString("alarm") + ",";
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    /**
     *
     * @param email password
     */
    public String pullData(String email) {
        requesturl = "http://comp4900group23.000webhostapp.com/getTasksToday.php?email="
                + email;

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(requesturl).build();


        Response response = null;

        try{
            response = client.newCall(request).execute();
            return response.body().string();

        }catch(IOException e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * User authentication
     * @param url
     * @param postData
     */
    public void pushJSON(URL url, String postData) {

        String data = "";

        HttpURLConnection httpURLConnection = null;

        try {
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("POST");

            httpURLConnection.setDoOutput(true);

            DataOutputStream dos = new DataOutputStream(httpURLConnection.getOutputStream());
            dos.writeBytes("PostData=" + postData);
            dos.flush();
            dos.close();

            InputStream in = httpURLConnection.getInputStream();
            InputStreamReader is = new InputStreamReader(in);

            int inputData = is.read();
            while(inputData != -1) {
                char current = (char) inputData;
                inputData = is.read();
                data += current;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(httpURLConnection != null)
                httpURLConnection.disconnect();
        }

    }

    /**
     *
     * @param email
     * @param password
     * @return
     */
    public String authenticate(String email, String password) {
        requesturl ="http://comp4900group23.000webhostapp.com/childAuth/"
                + email + "/" + password;

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(requesturl).build();


        Response response = null;

        try{
            response = client.newCall(request).execute();
            String result = response.body().string();

            JSONObject jo = new JSONObject(result);
            String msg = jo.getString("status");

            return msg;

        }catch(IOException e){
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getTasks() {
        return tasks;
    }

    public void setTasks(String tasks) {
        this.tasks = tasks;
    }

    public String getStartTimes() {
        return startTimes;
    }

    public void setStartTimes(String startTimes) {
        this.startTimes = startTimes;
    }

    public String getEndTimes() {
        return endTimes;
    }

    public void setEndTimes(String endTimes) {
        this.endTimes = endTimes;
    }

    public String getAlarm() {
        return alarm;
    }

    public void setAlarm(String alarm) {
        this.alarm = alarm;
    }
}
