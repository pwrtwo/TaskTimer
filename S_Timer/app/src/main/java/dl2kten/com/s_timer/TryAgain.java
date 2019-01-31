package dl2kten.com.s_timer;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.VideoView;

public class TryAgain extends AppCompatActivity {

    private Button backBtn;
    private VideoView videoView;
    private static final String MYID = "MyID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_try_again);

        backBtn = (Button) findViewById(R.id.backBtn);
        videoView = (VideoView) findViewById(R.id.videoView);

        MediaController mc = new MediaController(this);
        mc.show(2000);
        videoView.setMediaController(mc);
        //play video
        Uri uri = Uri.parse("android.resource://"+getPackageName()+"/" + R.raw.lesson);
        videoView.setVideoURI(uri);
        videoView.requestFocus();
        videoView.start();

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //return to main activity
                Intent intent = new Intent(TryAgain.this , MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                startActivity(intent);
            }
        });
    }


    /**
     * Overrides back button so that user goes to list view of tasks
     * instead of back to timer to redo task
     */
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(TryAgain.this , MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        startActivity(intent);
        return;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_secondary, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.logoutItem) {
            logout();
            return true;
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

        finish();
        startActivity(in);
    }
}
