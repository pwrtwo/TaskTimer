package dl2kten.com.s_timer;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class LaunchActivity extends AppCompatActivity {

    private static final String MYID = "MyID";
    private static final String KEY_LOGGEDON = "key_loggedOn";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent in;
        SharedPreferences sharedPreferences = getSharedPreferences(MYID, Context.MODE_PRIVATE);
        String loggedOn = sharedPreferences.getString(KEY_LOGGEDON, "Huh?");

        if(loggedOn.equals("Huh?") || loggedOn.equals("false")) {
            in = new Intent(this, Login.class);
            in.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            finish();
            startActivity(in);
        } else {
            in = new Intent(this, MainActivity.class);
            in.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            finish();
            startActivity(in);
        }
    }
}
