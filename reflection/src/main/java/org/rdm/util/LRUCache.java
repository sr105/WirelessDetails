/***************************************************************************************************
*
*	@(#)LRUCache.java
*
***************************************************************************************************/
package org.rdm.util;


import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;


/**
*	A simple least-recently-used (LRU) cache with a fixed number of elements.  When the maximum
*	element count is reached, the oldest entry is dropped from the object.
*/
public class LRUCache
{
	/***************************************************************************************************
	*
	*	Create an empty LRU cache with a given maximum capacity.
	*
	***************************************************************************************************/
	public LRUCache(int cacheSize_i)
	{
		myCacheSize = cacheSize_i;

		final int hashTableCapacity = (int)Math.ceil(myCacheSize / HASH_TABLE_LOAD_FACTOR) + 1;
		myMap = new LinkedHashMap(hashTableCapacity,HASH_TABLE_LOAD_FACTOR,true)
		{
			// (an anonymous inner class)
			private static final long serialVersionUID = 1;

			protected boolean removeEldestEntry(Map.Entry eldestEntry_i)
			{
				return (size() > myCacheSize);
			}
		};
	}



	/***************************************************************************************************
	*
	***************************************************************************************************/
	public void	put(Object key_i, Object value_i)
	{
		myMap.put(key_i,value_i);
	}



	/***************************************************************************************************
	*
	*	Attempt to retrieve the the value associated with a given key.  This will cause the entry
	*	to become the most recent one used.
	*
	***************************************************************************************************/
	public Object	get(Object key_i)
	{
		return myMap.get(key_i);
	}



	/***************************************************************************************************
	*
	*	Determines if the cache contains a specific element.
	*
	***************************************************************************************************/
	public boolean	containsKey(Object key_i)
	{
		return myMap.containsKey(key_i);
	}



	/***************************************************************************************************
	*
	*	Remove all elements from the cache.
	*
	***************************************************************************************************/
	public void	clear()
	{
		myMap.clear();
	}



	/***************************************************************************************************
	*
	*	Determine if the queue contains no elements via the {@link java.util.Collection} interface.
	*
	*		@return		<CODE>true</CODE> if the queue is empty.
	*
	***************************************************************************************************/
	public boolean	isEmpty()
	{
		return myMap.isEmpty();
	}



	/***************************************************************************************************
	*
	*
	***************************************************************************************************/
	public Object	remove(Object key_i)
	{
		return myMap.remove(key_i);
	}



	/***************************************************************************************************
	*
	*
	***************************************************************************************************/
	public int	capacity()
	{
		return myCacheSize;
	}



	/***************************************************************************************************
	*
	*
	***************************************************************************************************/
	public int	usedEntries()
	{
		return myMap.size();
	}



	/***************************************************************************************************
	*
	*
	***************************************************************************************************/
	public int	unusedEntries()
	{
		return capacity() - usedEntries();
	}



	/***************************************************************************************************
	*
	*	Return a {@link Map} that is the current snapshot of the current LRU cache contents.
	*
	***************************************************************************************************/
	public Map	toMap()
	{
		return new LinkedHashMap(myMap);
	}



	/***************************************************************************************************
	*
	*
	***************************************************************************************************/
	public Iterator<Map.Entry>	iterator()
	{
		return myMap.entrySet().iterator();
	}


	/***************************************************************************************************
	*
	*
	***************************************************************************************************/
	public String	toString()
	{
		return myMap.toString();
	}




	////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	//	IMPLEMENTATION DETAILS -- NO EXTERNAL REFERENCE TO THE FOLLOWING INFORMATION SHOULD BE MADE.
	//
	////////////////////////////////////////////////////////////////////////////////////////////////////

	private static final float   HASH_TABLE_LOAD_FACTOR = 0.75f;


	private final int			myCacheSize;
	private final LinkedHashMap	myMap;

}
