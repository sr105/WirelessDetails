package com.sr105.ntp;

import android.os.SystemClock;

import org.rdm.util.ReflectionObject;

import java.util.Date;

/**
 * SntpClient is a proxy (using reflection) of Android's hidden
 * android.net.SntpClient class.
 */
public class SntpClient extends ReflectionObject
{
    public final String className() { return "android.net.SntpClient"; }

    /**
     * Sends an SNTP request to the given host and processes the response.
     *
     * Note: This is added for convenience. It does not exist in the real SntpClient.
     *
     * @param host    host name of the server.
     * @param timeout network timeout in milliseconds.
     * @return current time as a Date
     */
    public static Date getTime(String host, int timeout)
    {
        try
        {
            SntpClient client = new SntpClient();
            if (client.requestTime(host, timeout))
            {
                long now = client.getNtpTime() + SystemClock.elapsedRealtime() - client.getNtpTimeReference();
                return new Date(now);
            }
        }
        catch (Exception ignored)
        {
        }
        return null;
    }

    /**
     * Sends an SNTP request to the given host and processes the response.
     *
     * @param host    host name of the server.
     * @param timeout network timeout in milliseconds.
     * @return true if the transaction was successful.
     */
    public boolean requestTime(String host, int timeout)
    {
        return invokeBooleanMethod("requestTime", host, timeout);
    }

    /**
     * Returns the time computed from the NTP transaction.
     *
     * @return time value computed from NTP server response.
     */
    public long getNtpTime()
    {
        return invokeLongMethod("getNtpTime");
    }

    /**
     * Returns the reference clock value (value of SystemClock.elapsedRealtime())
     * corresponding to the NTP time.
     *
     * @return reference clock corresponding to the NTP time.
     */
    public long getNtpTimeReference()
    {
        return invokeLongMethod("getNtpTimeReference");
    }
}
