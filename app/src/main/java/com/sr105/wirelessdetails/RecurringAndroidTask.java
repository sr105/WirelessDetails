package com.sr105.wirelessdetails;

import android.os.Handler;

public class RecurringAndroidTask
{
    public static void run(final Runnable runnable, final long periodInMillis)
    {
        run(runnable, periodInMillis, periodInMillis);
    }

    public static void run(final Runnable runnable, final long periodInMillis,
                           final long initialDelayInMillis)
    {
        final Handler uiHandler = getUiHandler();
        uiHandler.postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                runnable.run();
                uiHandler.postDelayed(this, periodInMillis);
            }
        }, initialDelayInMillis);
    }

    public static void runOnce(final Runnable runnable, final long delayInMillis)
    {
        final Handler uiHandler = getUiHandler();
        uiHandler.postDelayed(runnable, delayInMillis);
    }

    // TODO: different handler per thread?
    // synchronized map of thread id to handler?
    // TODO: check if current thread has a running Looper, if not, the handler is useless
    private static Handler mUiHandler = null;

    private static final Handler getUiHandler()
    {
        if (mUiHandler == null)
        {
            mUiHandler = new Handler();
        }
        return mUiHandler;
    }
}
