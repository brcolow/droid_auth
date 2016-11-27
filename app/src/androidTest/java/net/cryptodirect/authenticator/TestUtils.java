package net.cryptodirect.authenticator;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.test.InstrumentationRegistry;

import java.io.File;
import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class TestUtils
{
    private TestUtils() {}

    @SuppressLint({"CommitPrefEdits", "ApplySharedPref"})
    public static void clearSharedPreferences()
    {
        File sharedPreferencesDirectory = new File(InstrumentationRegistry.getTargetContext()
                .getFilesDir().getParentFile(), "shared_prefs");
        String[] sharedPreferencesFileNames = sharedPreferencesDirectory.list();

        if (sharedPreferencesFileNames == null)
        {
            // occurs when "shared_prefs" folder was not created yet
            return;
        }

        for (String fileName : sharedPreferencesFileNames)
        {
            InstrumentationRegistry.getTargetContext().getSharedPreferences(fileName.replace(".xml", ""),
                    Context.MODE_PRIVATE).edit().clear().commit();
        }

        try { Thread.sleep(1000); } catch (InterruptedException e) {}

        for (String preferenceFile : sharedPreferencesFileNames)
        {
            new File(sharedPreferencesDirectory, preferenceFile).delete();
        }
    }

    public static void clearStorage(String... excludes)
    {
        File filesDir = InstrumentationRegistry.getTargetContext().getFilesDir();
        String[] directoryContent = filesDir.list();
        for (String content : directoryContent)
        {
            assertThat(deleteRecursive(new File(filesDir, content), excludes), is(true));
        }
    }

    public static void clearCache()
    {
        File cacheDir = InstrumentationRegistry.getTargetContext().getCacheDir();
        assertThat(deleteRecursive(cacheDir), is(true));
    }

    private static boolean deleteRecursive(File directory, String... excludes)
    {
        if (excludes.length > 0 && Arrays.asList(excludes).contains(directory.getName()))
        {
            return true;
        }

        if (directory.isDirectory())
        {
            String[] directoryContent = directory.list();
            for (String content : directoryContent)
            {
                assertThat(deleteRecursive(new File(directory, content), excludes), is(true));
            }
        }
        return directory.delete();
    }
}
