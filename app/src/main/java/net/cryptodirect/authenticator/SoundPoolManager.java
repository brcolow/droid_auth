package net.cryptodirect.authenticator;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.util.Log;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SoundPoolManager
{
    private static SoundPool soundPool;
    private static final SoundPoolManager INSTANCE = new SoundPoolManager();
    private static final Map<String, Integer> sounds = new ConcurrentHashMap<>();
    private static final Map<String, Integer> playingSounds = new ConcurrentHashMap<>();
    private static final String TAG = SoundPoolManager.class.getSimpleName();

    public static SoundPoolManager getInstance()
    {
        return INSTANCE;
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    public synchronized void init(Context context)
    {
        int currentApiVersion = android.os.Build.VERSION.SDK_INT;

        if (currentApiVersion >= Build.VERSION_CODES.LOLLIPOP)
        {
            SoundPool.Builder builder = new SoundPool.Builder();
            builder.setMaxStreams(4);
            builder.setAudioAttributes(new AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).setUsage(AudioAttributes.USAGE_NOTIFICATION_COMMUNICATION_INSTANT).build());
            soundPool = builder.build();
        }
        else
        {
            soundPool = new SoundPool(4, AudioManager.STREAM_NOTIFICATION, 0);
        }

        sounds.put("SCAN", soundPool.load(context, R.raw.scan, 1));
        //sounds.put("TICKTOCK", soundPool.load(context, R.raw.ticktock, 1));
    }

    public void playSound(String sound, boolean loop)
    {
        if (sounds.containsKey(sound.toUpperCase()))
        {
            playingSounds.put(sound, soundPool.play(sounds.get(sound.toUpperCase()), 1.0f, 1.0f, 1, (loop ? -1 : 0), 1.0f));
        }
    }

    public void stopSound(String sound)
    {
        if (playingSounds.containsKey(sound.toUpperCase()))
        {
            soundPool.stop(playingSounds.get(sound.toUpperCase()));
        }
    }
}
