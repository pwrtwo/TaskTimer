package dl2kten.com.s_timer;

import android.app.AlertDialog;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

public class TimerService extends Service {

    private MediaPlayer mp;
    private boolean played;

    @Override
    public void onCreate() {

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, "Destroyed", Toast.LENGTH_LONG).show();

        if(mp != null && mp.isPlaying()) {
            mp.stop();
            mp.reset();
            mp.release();
        }
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);

        played = false;

        mp = MediaPlayer.create(this, R.raw.count);
        mp.start();
        Log.d("Testing", "Service start");

        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.release();
                if(!played) {
                    mp = MediaPlayer.create(TimerService.this, R.raw.race);
                    mp.start();
                    played = true;
                }
            }
        });

    }
}
