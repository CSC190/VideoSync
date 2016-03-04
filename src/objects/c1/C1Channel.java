package objects.c1;

import java.util.Vector;

public class C1Channel  implements Comparable<C1Channel>
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
	private Vector<C1Object> objects;
	
	/**
	 * Returns a subset of the states that are to be graphed
	 */
	private Vector<C1Object> graphObjects;


	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- C1Channel Constructor
	
	/**
	 *  Construct a new C1 Channel Object with a specific bit number
	 * @param bit
	 */
	public C1Channel(int bit)
	{
		this.bit = bit;
		objects = new Vector<C1Object>();
	}


	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- C1Channel Getter's and Setters

	/**
	 *  Returns this objects bit number
	 * @return
	 */
	public int getBit()
	{
		return this.bit;
	}
	
	/**
	 *  Return the array with all of this channel's objects
	 * @return
	 */
	public Vector<C1Object> getObjects()
	{
		return this.objects;
	}
	
	/**
	 *  Add a C1 Object to the objects array
	 * @param object
	 */
	public void addObject(C1Object object)
	{
		objects.add(object);
	}
	
	
	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- C1 Object Comparison

	/**
	 * Compares the current object with another one
	 */
	public int compareTo(C1Channel two) 
	{
		return (int)(this.bit - two.getBit());
	}

	
	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- C1 Event Retrieval Methods

	/**
	 * Returns all of the states between the min and max time, centered around the current time.
	 * @param minTime
	 * @param currentTime
	 * @param maxTime
	 * @return
	 */
	public Vector<C1Object> getStates(long minTime, long currentTime, long maxtime)
	{		
		// Assign/Re-assign the graph objects for storing the objects to be graphed
		graphObjects = new Vector<C1Object>();
				
		// Flag to check if we have found a valid object
		boolean found = false;
		
		// Start at the beginning of the array and work towards the end searching for any objects
		for (int i = 0; i < objects.size(); i++)
		{
			// Assign a C1 object to be checked against
			C1Object o1 = objects.elementAt(i);
			
			// Check to see if right is less than the object
			if (maxtime < o1.getMilli())
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
				// Assign a C1 object to be checked against
				C1Object o1 = objects.elementAt(i);
				
				// If the C1 object's millisecond time is less than the minimum time we are looking for,
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
				// Assign a C1 object from the selected index
				C1Object o = objects.elementAt(newIndex);
				
				// Add the C1 object to the graphObjects array
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
