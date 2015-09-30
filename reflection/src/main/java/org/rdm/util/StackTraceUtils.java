/***************************************************************************************************
 *
 *	@(#)StackTraceUtils.java
 *
 ***************************************************************************************************/
package org.rdm.util;

import java.util.Map;

public final class StackTraceUtils
{		
	/***************************************************************************************************
	 *
	 * Simple utilities to return the stack trace of an
	 * exception as a String.
	 *
	 ***************************************************************************************************/

    public static void dumpAllStacks()
    {
        dumpAllStacks(null);
    }

    public static void dumpAllStacks(String __prefix)
    {
        String prefix = (__prefix == null) ? "" : __prefix;

        Map<Thread,StackTraceElement[]> traces = Thread.getAllStackTraces();
        for (Map.Entry<Thread, StackTraceElement[]> threadTrace : traces.entrySet())
        {
            System.out.println(prefix + " Thread: " + threadTrace.getKey().getId());
            for (StackTraceElement element : threadTrace.getValue())
            {
                System.out.println(prefix + element.getClassName() + " " + element.getFileName() + " " + " " + element.getMethodName() + " " + element.getLineNumber());
            }
            System.out.println(prefix + "------------");
        }
        System.out.println(prefix + " -----DONE-------");

    }

	public static String getStackTrace()
	{
		return getStackTrace(null, null);
	}

	public static String getStackTrace(String __seperator_i)
	{
		return getStackTrace(null, __seperator_i);
	}

	public static String getStackTrace(Throwable __throwable_i)
	{
		return getStackTrace(__throwable_i,null);
	}

	public static String getStackTrace(Throwable throwable_i, String __seperator_i)
	{
		final String seperator = (__seperator_i == null) ? System.getProperty("line.separator") : __seperator_i;

		final StringBuilder result = new StringBuilder();

		if (throwable_i != null)
		{
			// add the class name and any message passed to constructor
			result.append(throwable_i.toString());
			result.append(seperator);
		}

		StackTraceElement[] elements = (throwable_i != null) ? throwable_i.getStackTrace() : Thread.currentThread().getStackTrace();

		// add each element of the stack trace
		for (StackTraceElement element : elements)
		{
			result.append(element);
			result.append(seperator);
		}

		return result.toString();
	}	
}
