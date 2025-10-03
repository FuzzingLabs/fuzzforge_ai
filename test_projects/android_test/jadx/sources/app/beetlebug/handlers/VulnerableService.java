package app.beetlebug.handlers;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.provider.Settings;
import android.widget.Toast;

/* loaded from: classes10.dex */
public class VulnerableService extends Service {
    private MediaPlayer player;

    @Override // android.app.Service
    public int onStartCommand(Intent intent, int flags, int startId) {
        MediaPlayer create = MediaPlayer.create(this, Settings.System.DEFAULT_RINGTONE_URI);
        this.player = create;
        create.setLooping(true);
        this.player.start();
        Toast.makeText(this, "Flag Found: 0x222103A", 1).show();
        return 1;
    }

    @Override // android.app.Service
    public void onDestroy() {
        super.onDestroy();
        this.player.stop();
    }

    @Override // android.app.Service
    public IBinder onBind(Intent intent) {
        return null;
    }
}
