package net.cryptodirect.authenticator;

import android.annotation.TargetApi;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Build;

/**
 * We have to keep this functionality in a separate class because
 * the ClassLoader checks to see if all referenced classes exist,
 * thus if we are on an API before 21 we will get this error:
 * <p/>
 * E/dalvikvm﹕ Could not find class 'android.media.SoundPool$Builder',
 * referenced from method net.cryptodirect.authenticator.SoundPoolManager.init
 * <p/>
 * Even if the path through the code does not call that class.
 */
public class SoundApiLevel21
{
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static SoundPool initApiLevel21SoundPool()
    {
        SoundPool.Builder builder = new SoundPool.Builder();
        builder.setMaxStreams(4);
        builder.setAudioAttributes(new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_COMMUNICATION_INSTANT)
                .build());

        return builder.build();
    }
}
