/**
 * ****************************************************************
 * File: 			L170Channel.java
 * Date Created:  	June 18, 2013
 * Programmer:		Dale Reed
 * 
 * Purpose:			Used for keeping track of all the Log 170 
 * 					Channel events so they can be easily sorted
 * 					and searched for the appropriate events
 * 
 * ****************************************************************
 */
package objects.log170;

import java.util.Vector;

public class L170Channel implements Comparable<L170Channel>
{
	/**
	 * Used for keeping track of the channel number
	 */
	private int bit;
	
	/**
	 * Used for keeping track of the current element so we can easily jump forwards
	 * or backwards to the event were looking for
	 */
	private int currentJumpElementIndex;

	/**
	 * Stores all of the elements for the specific channel in chronological order
	 */
	private Vector<L170Object> objects;
	
	/**
	 * Returns a subset of the states that are to be graphed
	 */
	private Vector<L170Object> graphObjects;
	

	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- L170Channel Constructor

	/**
	 * Construct a new Log 170 Channel Object with a specific bit number
	 * @param bit
	 */
	public L170Channel(int bit)
	{
		this.bit = bit;
		objects = new Vector<L170Object>();
	}


	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- L170Channel Getter's and Setters

	/**
	 *  Returns this objects bit number
	 * @return
	 */
	public int getBit()
	{
		return this.bit;
	}
	
	/**
	 * Return the array with all of this channel's objects
	 * @return
	 */
	public Vector<L170Object> getObjects()
	{
		return this.objects;
	}
	
	/**
	 *  Add a 170 Object to the objects array
	 * @param object
	 */
	public void addObject(L170Object object)
	{
		objects.add(object);
	}
	
	
	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- L170Channel Object Comparison

	/**
	 * Compares the current object with another one
	 */
	public int compareTo(L170Channel two) 
	{
		return (int)(this.bit - two.getBit());
	}

	
	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- L170Channel Event Retrieval Methods

	/**
	 * Returns all of the states between the min and max time, centered around the current time.
	 * @param minTime
	 * @param currentTime
	 * @param maxTime
	 * @return
	 */
	public Vector<L170Object> getStates(long minTime, long currentTime, long maxTime)
	{		
		// Assign/Re-assign the graph objects for storing the objects to be graphed
		graphObjects = new Vector<L170Object>();
				
		// Flag to check if we have found a valid object
		boolean found = false;
		
		// Start at the beginning of the array and work towards the end searching for any objects
		for (int i = 0; i < objects.size(); i++)
		{
			// Assign a 170 object to be checked against
			L170Object o1 = objects.elementAt(i);
			
			// Check to see if right is less than the object
			if (maxTime < o1.getMilli())
			{
				// Since we found an object to use, we won't have to find one going from the end of the array
				found = true;
				
				// As long as we are not looking at the element at index 0, we can find all previous elements
				if (i > 0)
				{
					// Get all previous elements starting at index i, and ones that are greater than left
					getPreviousElements(i, currentTime, minTime);
				}
				
				break;
			}			
		}
		
		// If we did not find a valid object, we go to the end of the array and get the first object that we can use
		if (!found)
		{
			// Start at the end of the array and work towards the start
			for (int i = objects.size()-1; i > 0; i--)
			{
				// Assign a 170 object to be checked against
				L170Object o1 = objects.elementAt(i);
				
				// If the 170 object's millisecond time is less than the minimum time we are looking for,
				// we add it to the graph objects. It should be the only one we add
				if (minTime > o1.getMilli())
				{
					graphObjects.add(o1);
					break;
				}
			}
		}
		
		return graphObjects;
	}	
	
	/**
	 *  Get all previous elements starting from the index, and that are greater than left
	 * @param index
	 * @param currentTime
	 * @param left
	 */
	private void getPreviousElements(int index, long currentTime, long left)
	{
		// The counter is used to help determine what index value to look at.
		int counter = 1;
		boolean done = false;
		
		// This tells us we need to set the current jump-to index.
		boolean setJumpIndex = true;

		while (!done)
		{
			// Get the new index value to use
			int newIndex = index - counter;
			
			// If the new index value is greater than 0, retrieve the element at that position
			if (newIndex >= 0)
			{		
				// Assign a 170 object from the selected index
				L170Object o = objects.elementAt(newIndex);
				
				// Add the 170 object to the graphObjects array
				graphObjects.add(o);
	
				if (setJumpIndex && o.getMilli() <= currentTime)
				{
					currentJumpElementIndex = newIndex;
					setJumpIndex = false;
				}

				// If the current object is less than left, we have finished searching through the array.
				if (o.getMilli() < left)
				{
					done = true;
				}
			}
			// If we reach this else there is nothing left to check, so we are done
			else
			{
				done = true;
			}
			
			// Increment the counter for use with the next index
			counter++;
		}
	}

	/**
	 *  Returns the currentJumpElementIndex
	 * @return
	 */
	public int getCurrentJumpElementIndex()
	{
		return this.currentJumpElementIndex;
	}
}






// -- These are to be removed
//	THESE TWO METHODS WILL EVENTUALLY BE REPLACED BY THE NEW MILLISECOND VERSIONS
//private boolean[] sixtyData;
//
//public void setSixtyData(boolean[] sixtyData)
//{
//	this.sixtyData = sixtyData;
//}
//
//public boolean[] getSixtyData()
//{
//	return this.sixtyData;
//}
