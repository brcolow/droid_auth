package net.cryptodirect.authenticator;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;

import java.util.Locale;
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
    public synchronized void init(Context context)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            soundPool = SoundApiLevel21.initApiLevel21SoundPool();
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
        if (sounds.containsKey(sound.toUpperCase(Locale.US)))
        {
            playingSounds.put(sound, soundPool.play(sounds.get(sound.toUpperCase(Locale.US)), 1.0f, 1.0f, 1, (loop ? -1 : 0), 1.0f));
        }
    }

    public void stopSound(String sound)
    {
        if (playingSounds.containsKey(sound.toUpperCase(Locale.US)))
        {
            soundPool.stop(playingSounds.get(sound.toUpperCase(Locale.US)));
        }
    }
}
