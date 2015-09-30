/***************************************************************************************************
 * @(#)ArrayUtils.java
 ***************************************************************************************************/
package org.rdm.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;


/**
 *	A hodge-podge collection of utility functions for manipulating arrays of objects.
 */
public class ArrayUtils
{
    /** */
    public interface ArrayElementProcessor
    {
        Object	getProcessedArrayElement(Object element_i);
    }


    /** */
    public static class ConvertToUppercaseArrayElementProcessor implements ArrayElementProcessor
    {
        public static ArrayElementProcessor	sharedInstance()
        {
            if (ourSharedInstance == null)
            {
                ourSharedInstance = new ConvertToUppercaseArrayElementProcessor();
            }

            return ourSharedInstance;
        }

        public Object	getProcessedArrayElement(Object element_i)
        {
            return element_i.toString().toUpperCase();
        }

        private ConvertToUppercaseArrayElementProcessor()
        { }

        private static ArrayElementProcessor	ourSharedInstance;
    }


    /** */
    public static class ConvertToLowercaseArrayElementProcessor implements ArrayElementProcessor
    {
        public static ArrayElementProcessor	sharedInstance()
        {
            if (ourSharedInstance == null)
            {
                ourSharedInstance = new ConvertToLowercaseArrayElementProcessor();
            }

            return ourSharedInstance;
        }

        public Object	getProcessedArrayElement(Object element_i)
        {
            return element_i.toString().toLowerCase();
        }

        private ConvertToLowercaseArrayElementProcessor()
        { }

        private static ArrayElementProcessor	ourSharedInstance;
    }


    /***************************************************************************************************
     *
     *	Concatenate two byte arrays together
     *
     *		@param	firstArray_i				The first array to be concatenated
     *
     *		@param	secondArray_i				The second array to be concatenated
     *
     *	   @return the new <CODE>byte[]</CODE> with the content in firstArray_i, secondArray_i order
     *
     ***************************************************************************************************/
    public static byte[] concat(byte[] firstArray_i, byte[] secondArray_i) {
        byte[] newArray = new byte[firstArray_i.length+secondArray_i.length];
        System.arraycopy(firstArray_i, 0, newArray, 0, firstArray_i.length);
        System.arraycopy(secondArray_i, 0, newArray, firstArray_i.length, secondArray_i.length);

        return newArray;
    }

    /***************************************************************************************************
     *
     *	Extracts a smaller <CODE>byte[]</CODE> from a given <CODE>byte[]</CODE>.
     *
     *	@return the new <CODE>byte[]</CODE>, or <CODE>null</CODE> if the original is null.
     *
     ***************************************************************************************************/
    public static byte[]	extract(byte[] __array_i, int index_i, int length_i) throws IndexOutOfBoundsException
    {
        if (__array_i == null)
        {
            return null;
        }

        byte[] bytes = new byte[length_i];
        System.arraycopy(__array_i,index_i,bytes,0,length_i);
        return bytes;
    }



    /***************************************************************************************************
     *
     *	Contatenate to <CODE>byte[]</CODE>s into one.
     *
     *	@return the new <CODE>byte[]</CODE>.  This will never be <CODE>null</CODE>.
     *
     ***************************************************************************************************/
    public static byte[]	concatenate(byte[] __array1_i, byte[] __array2_i)
    {
        // calculate the new array length
        int newArrayLength = 0;

        if (__array1_i != null)
        {
            newArrayLength += __array1_i.length;
        }

        if (__array2_i != null)
        {
            newArrayLength += __array2_i.length;
        }

        byte[] newArray = new byte[newArrayLength];

        int array1Length = 0;
        if (__array1_i != null)
        {
            array1Length = __array1_i.length;
            System.arraycopy(__array1_i,0,newArray,0,array1Length);
        }

        if (__array2_i != null)
        {
            System.arraycopy(__array2_i,0,newArray,array1Length,__array2_i.length);
        }

        return newArray;
    }




    /** */
    public interface ObjectMapper
    {
        Object	getMappedValueFor(Object __object_i);

        Object	getDefaultMappedValue();
    }


    /** */
    public static class SimpleObjectMapper implements ObjectMapper
    {
        public SimpleObjectMapper(Map objectMapping_i)
        {
            this(objectMapping_i,null);
        }

        public SimpleObjectMapper(Map objectMapping_i, Object __defaultMappedValue_i)
        {
            if (objectMapping_i != null)
            {
                myObjectMapping.putAll(objectMapping_i);
            }

            myDefaultMappedValue = __defaultMappedValue_i;
        }

        public Object	getMappedValueFor(Object __object_i)
        {
            return myObjectMapping.get(__object_i);
        }

        public Object	getDefaultMappedValue()
        {
            return myDefaultMappedValue;
        }


        private final Map		myObjectMapping = new HashMap();
        private final Object	myDefaultMappedValue;
    }



    /***************************************************************************************************
     *
     *
     ***************************************************************************************************/
    public static Object[]	map(Object[] array_i, Map valueMappings_i)
    {
        return ArrayUtils.map(array_i,valueMappings_i,null);
    }


    /***************************************************************************************************
     *
     *
     ***************************************************************************************************/
    public static Object[]	map(Object[] array_i, Map valueMappings_i, Object __defaultValue_i)
    {
        return ArrayUtils.map(array_i,new SimpleObjectMapper(valueMappings_i,__defaultValue_i));
    }


    /***************************************************************************************************
     *
     *
     ***************************************************************************************************/
    public static Object[]	map(Object[] array_i, ObjectMapper mapper_i)
    {
        if (array_i == null)
        {
            return null;
        }

        Object[] mappedObjects = new Object[array_i.length];
        for (int i = 0; i < array_i.length; ++i)
        {
            mappedObjects[i] = mapper_i.getMappedValueFor(array_i[i]);
        }

        return mappedObjects;
    }



    /***************************************************************************************************
     *
     *
     ***************************************************************************************************/
    public static Object[]	map(Collection collection_i, ObjectMapper mapper_i)
    {
        return ArrayUtils.map(collection_i.toArray(),mapper_i);
    }


    /***************************************************************************************************
     *
     *
     ***************************************************************************************************/
    public static Object[]	map(Collection collection_i, Map valueMappings_i)
    {
        return ArrayUtils.map(collection_i,valueMappings_i,null);
    }


    /***************************************************************************************************
     *
     *
     ***************************************************************************************************/
    public static Object[]	map(Collection collection_i, Map valueMappings_i, Object __defaultValue_i)
    {
        return ArrayUtils.map(collection_i,new SimpleObjectMapper(valueMappings_i,__defaultValue_i));
    }



    /***************************************************************************************************
     *
     *	This method is used for validating the first index and length parameters against a given
     *	object array.
     *
     *		@param	array_i						The {@link Object} array to use for index validation.
     *
     *		@param	firstIndex_i				The first index to be validated.
     *
     *		@param	length_i					The length parameter to be validated.
     *
     *	@throws	IndexOutOfBoundsException if the first index is outside the bounds of the
     *			object array.
     *
     *	@throws IndexOutOfBoundsException if the first index combined with the length is
     *			outside the bounds of the object array.
     *
     *	@throws IllegalArgumentException if the length parameter is negative.
     *
     ***************************************************************************************************/
    public static void	validateObjectArrayFirstIndexAndLength(Object[] array_i, int firstIndex_i, int length_i) throws IndexOutOfBoundsException,
            IllegalArgumentException
    {
        if (length_i < 0)
        {
            throw new IllegalArgumentException("length cannot be " + length_i);
        }

        if ((firstIndex_i < 0) || (firstIndex_i > array_i.length))
        {
            throw new IndexOutOfBoundsException("the first index of " + firstIndex_i + " is outside of the bounds of the " + array_i.length + " element array");
        }

        if (firstIndex_i + length_i > array_i.length)
        {
            throw new IndexOutOfBoundsException("the first index of " + firstIndex_i + " and length of " + length_i + " is outside of the bounds of the " + array_i.length + " element array");
        }
    }



    /***************************************************************************************************
     *
     *	Populate a {@link Collection} with the contents of an array.
     *
     *		@param	collection_io			The {@link Collection} to be populated.
     *
     *		@param	__array_i				An array of objects to add to the collection.
     *
     *		@param	firstIndex_i			The index of the first element of the array to be
     *										added to the collection.
     *
     *		@param	length_i				The maximum number of elements to add to the
     *										collection.
     *
     *		@param	recurse_i				A flag that indicates whether or not to recurse into
     *										other <CODE>Object[]</CODE> elements.
     *
     *		@param  __elementProcessor_i	The array element processor.
     *
     *	@return	<CODE>true</CODE> if the {@link Collection} was modifed; otherwise, <CODE>false</CODE>.
     *
     *	@throws	IndexOutOfBoundsException if the first index is outside the bounds of the
     *			object array.
     *
     *	@throws IndexOutOfBoundsException if the first index combined with the length is
     *			outside the bounds of the object array.
     *
     *	@throws IllegalArgumentException if the length parameter is negative.
     *
     ***************************************************************************************************/
    public static boolean	populateCollection(Collection            collection_io,
                                                Object[]              __array_i,
                                                int                   firstIndex_i,
                                                int                   length_i,
                                                boolean               recurse_i,
                                                ArrayElementProcessor __elementProcessor_i)
    {
        if ((__array_i == null) || (__array_i.length == 0))
        {
            return false;
        }

        if (firstIndex_i >= __array_i.length)
        {
            throw new IndexOutOfBoundsException("first index of " + firstIndex_i + " is past the length of the array");
        }

        if (firstIndex_i + length_i > __array_i.length)
        {
            throw new IndexOutOfBoundsException("first index of " + firstIndex_i + " and length of " + length_i + " is past the length of the array");
        }

        final int collectionInitialSize = collection_io.size();

        copyArrayElementsIntoCollection(
                __array_i,
                firstIndex_i,
                length_i,
                recurse_i,
                __elementProcessor_i,
                collection_io
        );

        return (collection_io.size() != collectionInitialSize);
    }


    /**************************************************************************************************/
    private static void	copyArrayElementsIntoCollection(Object[]              components_i,
                                                           int                   offset_i,
                                                           int                   length_i,
                                                           boolean               recurse_i,
                                                           ArrayElementProcessor __elementProcessor_i,
                                                           Collection            collection_i)
    {
        while (length_i-- > 0)
        {
            Object object = components_i[offset_i++];

            if ((object instanceof Object[]) && recurse_i)
            {
                Object[] objectArray = (Object[])object;
                copyArrayElementsIntoCollection(
                        objectArray,
                        0,
                        objectArray.length,
                        recurse_i,
                        __elementProcessor_i,
                        collection_i
                );
                continue;
            }

            if (__elementProcessor_i != null)
            {
                object = __elementProcessor_i.getProcessedArrayElement(object);
            }

            collection_i.add(object);
        }
    }



    /***************************************************************************************************
     *
     *	Populate a {@link Collection} with the contents of an array.
     *
     *		@param	collection_io			The {@link Collection} to be populated.
     *
     *		@param	__array_i				An array of objects to add to the collection.
     *
     *		@param	firstIndex_i			The index of the first element of the array to be
     *										added to the collection.
     *
     *		@param	length_i				The maximum number of elements to add to the
     *										collection.
     *
     *		@param  __elementProcessor_i	The array element processor.
     *
     *	@return	<CODE>true</CODE> if the {@link Collection} was modifed; otherwise, <CODE>false</CODE>.
     *
     *	@throws	IndexOutOfBoundsException if the first index is outside the bounds of the
     *			object array.
     *
     *	@throws IndexOutOfBoundsException if the first index combined with the length is
     *			outside the bounds of the object array.
     *
     *	@throws IllegalArgumentException if the length parameter is negative.
     *
     ***************************************************************************************************/
    public static boolean	populateCollection(Collection collection_io, Object[] __array_i, int firstIndex_i, int length_i, ArrayElementProcessor __elementProcessor_i)
    {
        return ArrayUtils.populateCollection(
                collection_io,
                __array_i,
                firstIndex_i,
                length_i,
                false,
                __elementProcessor_i
        );
    }


    /***************************************************************************************************
     *
     *	Populate a {@link Collection} with the contents of an array.
     *
     *		@param	collection_io			The {@link Collection} to be populated.
     *
     *		@param	__array_i				An array of objects to add to the collection.
     *
     *		@param	firstIndex_i			The index of the first element of the array to be
     *										added to the collection.
     *
     *		@param	length_i				The maximum number of elements to add to the
     *										collection.
     *
     *	@return	<CODE>true</CODE> if the {@link Collection} was modifed; otherwise, <CODE>false</CODE>.
     *
     *	@throws	IndexOutOfBoundsException if the first index is outside the bounds of the
     *			object array.
     *
     *	@throws IndexOutOfBoundsException if the first index combined with the length is
     *			outside the bounds of the object array.
     *
     *	@throws IllegalArgumentException if the length parameter is negative.
     *
     ***************************************************************************************************/
    public static boolean	populateCollection(Collection collection_io, Object[] __array_i, int firstIndex_i, int length_i)
    {
        return ArrayUtils.populateCollection(
                collection_io,
                __array_i,
                firstIndex_i,
                length_i,
                null
        );
    }


    /***************************************************************************************************
     *
     *	Populate a {@link Collection} with the contents of an array.
     *
     *		@param	collection_io			The {@link Collection} to be populated.
     *
     *		@param	__array_i				An array of objects to add to the collection.
     *
     *		@param  __elementProcessor_i	The array element processor.
     *
     *	@return	<CODE>true</CODE> if the {@link Collection} was modifed; otherwise, <CODE>false</CODE>.
     *
     ***************************************************************************************************/
    public static boolean	populateCollection(Collection collection_io, Object[] __array_i, ArrayElementProcessor __elementProcessor_i)
    {
        if (__array_i == null)
        {
            return false;
        }

        return ArrayUtils.populateCollection(
                collection_io,
                __array_i,
                0,
                __array_i.length,
                __elementProcessor_i
        );
    }



    /***************************************************************************************************
     *
     *	Populate a {@link Collection} with the contents of an array.
     *
     *		@param	collection_io			The {@link Collection} to be populated.
     *
     *		@param	__array_i				An array of objects to add to the collection.
     *
     *	@return	<CODE>true</CODE> if the {@link Collection} was modifed; otherwise, <CODE>false</CODE>.
     *
     ***************************************************************************************************/
    public static boolean	populateCollection(Collection collection_io, Object[] __array_i)
    {
        return ArrayUtils.populateCollection(
                collection_io,
                __array_i,
                null
        );
    }



    /***************************************************************************************************
     *
     *	Obtain an {@link Iterator} for a fixed-sized object array.
     *
     *		@param	array_i				An array of objects to iterate.
     *
     *	@return	a new {@link Iterator} instance.  Because the containter being iterated over is a
     *				fixed-size array, this {@link Iterator} does not support the {@link Iterator#remove()}
     *				method.
     *
     *	@throws	NullPointerException if the <CODE>array_i</CODE> parameter is <CODE>null</CODE>.
     *
     ***************************************************************************************************/
    public static Iterator	iterator(Object[] array_i)
    {
        // validate the input parameters
        if (array_i == null)
        {
            throw new NullPointerException();
        }

        return ArrayUtils.iterator(array_i,0,array_i.length);
    }


    /***************************************************************************************************
     *
     *	Obtain an {@link Iterator} for a fixed-sized object array.
     *
     *		@param	array_i				An array of objects to iterate.
     *
     *		@param	firstIndex_i		The index of the first element within the array that will
     *									be returned by the {@link Iterator}.  If this value is
     *									outside the bounds of the given array, the {@link Iterator#hasNext}
     *									method will always return <CODE>false</CODE>.
     *
     *		@param	length_i			The maximum number of array elements to be iterated over.
     *									If this number, coupled with the <CODE>firstIndex_i</CODE>
     *									parameter is more than the total number of elements in the
     *									array, the {@link Iterator#hasNext} will intelligently
     *									return <CODE>false</CODE> when the actual number of elements
     *									has been exhausted.
     *
     *	@return	a new {@link Iterator} instance.  Because the containter being iterated over is a
     *				fixed-size array, this {@link Iterator} does not support the {@link Iterator#remove()}
     *				method.
     *
     *	@throws	NullPointerException if the <CODE>array_i</CODE> parameter is <CODE>null</CODE>.
     *
     *	@throws	IllegalArgumentException if the <CODE>firstIndex_i</CODE> or <CODE>length_i</CODE>
     *			parameters are negative.
     *
     ***************************************************************************************************/
    public static Iterator	iterator(Object[] array_i, int firstIndex_i, int length_i)
    {
        class ArrayIterator implements Iterator
        {
            public ArrayIterator(Object[] array_i, int firstIndex_i, int length_i)
            {
                myArray = array_i;

                int lastIndex = firstIndex_i + length_i;
                if (lastIndex > array_i.length)
                {
                    lastIndex = array_i.length;
                }
                myLastIndex = lastIndex;

                myCurrentIndex = firstIndex_i;
            }

            public boolean	hasNext()
            {
                return (myCurrentIndex < myLastIndex);
            }

            public Object	next()
            {
                if (!hasNext())
                {
                    throw new NoSuchElementException();
                }

                return myArray[myCurrentIndex++];
            }

            public void	remove()
            {
                throw new UnsupportedOperationException();
            }


            private final Object[]	myArray;
            private final int		myLastIndex;
            private int				myCurrentIndex;
        }


        // validate the input parameters
        if (array_i == null)
        {
            throw new NullPointerException("object array is null");
        }

        if (firstIndex_i < 0)
        {
            throw new IllegalArgumentException("first index cannot be " + firstIndex_i);
        }

        if (length_i < 0)
        {
            throw new IllegalArgumentException("length cannot be " + length_i);
        }

        // return a new iterator
        return new ArrayIterator(array_i,firstIndex_i,length_i);
    }


    /***************************************************************************************************
     *
     *	Return the string represntation of an array of objects.
     *
     *		@param	__objects_i					The array of {@link Object}s to use in building
     *											the {@link String}.  If this parameter is <CODE>null</CODE>,
     *											then the string <CODE>"null"</CODE> will be returned.
     *
     *	@throws	IndexOutOfBoundsException if the first index is outside the bounds of the
     *			object array.
     *
     *	@throws IndexOutOfBoundsException if the first index combined with the length is
     *			outside the bounds of the object array.
     *
     *	@throws IllegalArgumentException if the length parameter is negative.
     *
     ***************************************************************************************************/
    public static String	toString(Object[] __objects_i, int firstIndex_i, int length_i, String __prefix_i, String delimiter_i, String __suffix_i)
    {
        if (__objects_i == null)
        {
            return NULL_STRING;
        }

        validateObjectArrayFirstIndexAndLength(__objects_i,firstIndex_i,length_i);

        StringBuilder buffer = new StringBuilder();

        if (__prefix_i != null)
        {
            buffer.append(__prefix_i);
        }

        final int count = __objects_i.length;
        if (count > 0)
        {
            buffer.append(toSafeString(__objects_i[0]));
            for (int i = 1; i < count; ++i)
            {
                buffer.append(delimiter_i)
                        .append(toSafeString(__objects_i[i]));
            }
        }

        if (__suffix_i != null)
        {
            buffer.append(__suffix_i);
        }

        return buffer.toString();
    }



    /***************************************************************************************************
     *
     *
     ***************************************************************************************************/
    public static String	toString(Object[] __objects_i, String __prefix_i, String delimiter_i, String __suffix_i)
    {
        if (__objects_i == null)
        {
            return NULL_STRING;
        }

        return ArrayUtils.toString(
                __objects_i,
                0,
                __objects_i.length,
                __prefix_i,
                delimiter_i,
                __suffix_i
        );
    }




    /***************************************************************************************************
     *
     *
     ***************************************************************************************************/
    public static String	toString(Object[] __objects_i, int firstIndex_i, int length_i, String delimiter_i)
    {
        return ArrayUtils.toString(
                __objects_i,
                firstIndex_i,
                length_i,
                null,
                delimiter_i,
                null
        );
    }




    /***************************************************************************************************
     *
     *
     ***************************************************************************************************/
    public static String	toString(Object[] __objects_i, String delimiter_i)
    {
        if (__objects_i == null)
        {
            return NULL_STRING;
        }

        return ArrayUtils.toString(
                __objects_i,
                0,
                __objects_i.length,
                delimiter_i
        );
    }



    /***************************************************************************************************
     *
     *
     ***************************************************************************************************/
    public static String	toString(Object[] __objects_i, int firstIndex_i, int length_i, char delimiter_i)
    {
        char[] characterArray = { delimiter_i };
        String delimiterString = new String(characterArray);
        return ArrayUtils.toString(
                __objects_i,
                firstIndex_i,
                length_i,
                delimiterString
        );
    }



    /***************************************************************************************************
     *
     *
     ***************************************************************************************************/
    public static String	toString(Object[] __objects_i, char delimiter_i)
    {
        if (__objects_i == null)
        {
            return NULL_STRING;
        }

        return ArrayUtils.toString(
                __objects_i,
                0,
                __objects_i.length,
                delimiter_i
        );
    }



    /***************************************************************************************************
     *
     *
     ***************************************************************************************************/
    public static Collection	toUnmodifiableCollection(Object[] __objects_i)
    {
        ArrayList array = new ArrayList();
        ArrayUtils.populateCollection(array,__objects_i);
        return Collections.unmodifiableCollection(array);
    }



    ////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //	IMPLEMENTATION DETAILS -- NO EXTERNAL REFERENCE TO THE FOLLOWING INFORMATION SHOULD BE MADE.
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////////

    private final static String	NULL_STRING = "" + (Object)null;



    /****************************************************************************************************/
    private static String	toSafeString(Object __object_i)
    {
        return "" + __object_i;
    }



    /****************************************************************************************************/
    private ArrayUtils()
    {
        /*** NOTHING TO DO HERE ***/
    }

}