package dl2kten.com.s_timer;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

public class Login extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private Button loginBtn;
    private String email, password, dataTasks, dataStart, dataEnd, dataAlarm;
    private static final String MyPREFERENCES = "MyPrefs";
    private static final String MYID = "MyID";
    private static final String KEY_EMAIL = "key_email";
    private static final String KEY_PASSWORD = "key_password";
    private static final String KEY_TASK = "key_task";
    private static final String KEY_STARTTIME = "key_startTime";
    private static final String KEY_ENDTIME = "key_endTime";
    private static final String KEY_LOGGEDON = "key_loggedOn";
    private static final String KEY_ALARM = "key_alarm";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailEditText = (EditText) findViewById(R.id.emailEditText);
        passwordEditText = (EditText) findViewById(R.id.passwordEditText);
        loginBtn = (Button) findViewById(R.id.loginBtn);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });
    }

    /**
     *  Checks whether fields are empty and stores value in sharedPreferences
     */
    private void loginUser() {

        email = emailEditText.getText().toString().trim();
        password = passwordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("Please Enter Email");
            emailEditText.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("Please Enter Password");
            passwordEditText.requestFocus();
            return;
        }


        SharedPreferences preferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor2 = preferences.edit();
        editor2.remove(MyPREFERENCES);
        editor2.clear();
        editor2.commit();

        String result = "";

        try {
            result = new PullData().execute(0).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }



        if(result.equals("pass")) {

            SharedPreferences idPrefs = getSharedPreferences(MYID, Context.MODE_PRIVATE);
            SharedPreferences.Editor idEditor = idPrefs.edit();

            idEditor.putString(KEY_EMAIL, email);
            idEditor.putString(KEY_PASSWORD, password);
            idEditor.putString(KEY_LOGGEDON, "true");
            idEditor.commit();

            SharedPreferences sharedPreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();

            editor.putString(KEY_TASK, dataTasks);
            editor.putString(KEY_STARTTIME, dataStart);
            editor.putString(KEY_ENDTIME, dataEnd);
            editor.putString(KEY_ALARM, dataAlarm);
            editor.commit();

            startActivity(new Intent(this, MainActivity.class));
        } else {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setTitle("Failed User Authentication");
            alertDialogBuilder.setMessage("Wrong Email or Wrong Pasword")
                    .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    public void onLoginSuccess() {
        finish();
    }
    /**
     *
     */
    private class PullData extends AsyncTask<Integer, Integer, String> {

        @Override
        protected String doInBackground(Integer... integers) {

            Data data = new Data();

            String msg = data.authenticate(email, password);

            if(msg == null) {
                msg = "";
            }

            if(msg.equals("pass")) {
                String result = data.pullData(email);

                data.parseJSON(result);

                dataTasks = data.getTasks();
                dataStart = data.getStartTimes();
                dataEnd = data.getEndTimes();
                dataAlarm = data.getAlarm();

                return "pass";
            } else {
                return "fail";
            }

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
    }
}
