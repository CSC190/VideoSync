/**
 * ****************************************************************
 * File: 			C1Analyzer.java
 * Date Created:  	May 28, 2013
 * Programmer:		Dale Reed
 * 
 * Purpose:			To handle the analysis of the c1 data file and 
 * 					prepare it for use with the graphs
 * 
 * ****************************************************************
 */
package analyzers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.Vector;

import objects.c1.C1Channel;
import objects.c1.C1Object;
import objects.graphs.Line;

public class C1Analyzer 
{
	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- C1 Variable Declarations

	/**
	 * Used for keeping track of the start time for the analysis
	 */
	long sTime;
	
	/**
	 * Used for keeping track of the ending time of the analysis
	 */
	long eTime;

	/**
	 * Used for referencing the file that we are going to initially analyze
	 */
	private File c1File;
	
	/**
	 * Used for storing all of the raw events that are detected during analysis
	 */
	private Vector<C1Object> events;
	
	/**
	 * Used for storing all the individual channel information after analysis.
	 * This is what we search through when looking for events
	 */
	private Vector<C1Channel> channels;

	/**
	 * Used for storing all the graph lines that are to be rendered
	 */
	private Vector<Line> graphLines;

	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- C1Analyzer Construction 

	/**
	 * Constructor for creating the C1 Analyzer methods
	 */
	public C1Analyzer()
	{
		// Create an array to store all of the individual events
		events = new Vector<C1Object>();
		
		// Create an array to store all of the individual channels
		channels = new Vector<C1Channel>();
	}
	
	
	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- C1Analyzer Analysis methods 
	
	/**
	 * Performs the analysis of the C1 file and creates all the array data for use with the graphs
	 * 
	 * @param file
	 */
	public void performAnalysis(File file)
	{
		// Set the c1 source file to the one passed so we can analyze it
		this.c1File = file;
		
		System.out.println("File to load for C1 Analysis: " + this.c1File.getAbsolutePath());
		System.out.println("------------------------------------------------------");

		// Set the sTime to the current time
		sTime = System.currentTimeMillis();
		
		System.out.println("Analyzing C1 File: " + this.c1File.getName());

		// Surround everything in a try/catch block for catching any errors with reading the file
		char[] eventData = null;
		
		try 
		{
			// Create a buffered reader to read the contents of the C1 File
			@SuppressWarnings("resource")
			BufferedReader fileReader = new BufferedReader(new FileReader(this.c1File.getPath()));

			// A string to temporarily store each line to be read.
			String line = null;
			
			// Initialize the base time to -1. 
			long baseTimeValue = -1;
			
			// Read each line from the file and store it into line and continue running as long as line is not null
			while ((line = fileReader.readLine()) != null)
			{
				// Get the time parameter from the line by splitting the line around the space.
				Long time = Long.parseLong(line.split(" ")[0]);
				
				// If the baseTiem is -1, then we need to initialize it to the first records time.
				// This allows us to use the first event as time 0, making the time values small and more manageable.
				if (baseTimeValue == -1)
					baseTimeValue = time;

				// Adjust the time based on the baseTimeValue.
				time = time - baseTimeValue;
				
				// Get all of the state data from the line.
				String states = line.split(" ")[1];
			
				// If the event data hasn't been initialized, then we need to initialize it with the first set of data
				if (eventData == null)
				{
					// Initialize the event data with the length of the state size
					eventData = new char[states.length()];
					
					// Loop through all of the event states and store it into the appropriate index in the eventData
					for (int i = 0; i < states.length(); i++)
					{
						eventData[i] = states.charAt(i);
					}
				}
				else
				{
					// Loop through all of the states in the data and store it into the event data
					for (int i = 0; i < states.length(); i++)
					{
						// Retrieve the new and old states from the two arrays
						char n = states.charAt(i);
						char o = eventData[i];
						
						// If the new and old states do not match, then we have an event to take care of
						if (n != o)
						{		
							// Add a new event with the time element and set the state
							events.add(new C1Object(time, (n == '1') ? 1 : 0, i));
							
							// Update the old array with the updated value for comparision later
							eventData[i] = n;
						}
					}
				}				
			}
		}
		catch (FileNotFoundException e) 
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		// Now that we have all the elements extracted, we can create the finalized data structures
		createChannelArrays();
		
		// Assign all of the events to individual channels for easier use with the graphing system
		generateChannelData();
		
		// Insert an element at time 0 that has the opposite state of the first element
		insertStartElement();
		
		// Sort the channel array by bit number
		Collections.sort(channels);
		
		// The following only prints out the counts of events for each channel
		System.out.println();
		System.out.println("Total Events By Channel");

		// Loop through all of the channels and print out the number of events 
		for (C1Channel c : channels)
		{
			System.out.printf("Channel %d: %d events\n", c.getBit(), c.getObjects().size() / 2);
		}
		System.out.println();

		System.out.println("Finished Generating C1 element records: " + (System.currentTimeMillis() - sTime) + " ms");
		System.out.println("------------------------------------------------------");
	}
	
	/**
	 *  Creates the channel arrays an initializes them for use
	 */
	private void createChannelArrays()
	{
		// Loop through each event that we found to create the channel data
		for (C1Object o : events)
		{
			// Create a temporary bit to hold the objects bit numer
			int bit = o.getBit();
			
			// Used in determining if we found a valid pin
			boolean foundBit = false;
			
			// Loop through all the channel objects we have so far
			for (C1Channel c : channels)
			{
				// If that channel object exists, we change the found flag to true
				if (c.getBit() == bit)
				{
					foundBit = true;
				}
			}
			
			// If the found flag is false, we didn't find that bit anywhere
			// so we need to add that channel object with the bit it will represent
			if (!foundBit)
			{
				channels.add(new C1Channel(bit));
			}
		}
	}
	
	/**
	 *  Adds all of the events to the appropriate channel's event arrays
	 */
	private void generateChannelData()
	{	
		// Loop through each Log 170 event we detected
		for (C1Object o : events)
		{			
			// Loop through each channel we have found
			for (C1Channel c : channels)
			{
				// If the channel and bit parameters match, we cna add the event object to the channel's array
				if (c.getBit() == o.getBit())
				{
					c.addObject(o);
				}
			}
		}
	}
	
	/**
	 * Inserts an element at the beginning of the array for making the graphing easier
	 */
	private void insertStartElement()
	{
		// Loop through all of the channel objects
		for (C1Channel c : channels)
		{
			// Get the Log 170 Object currently at the start
			C1Object o = c.getObjects().elementAt(0);
			
			// Create  new Log 170 Object at time 0, with the state opposite of the current start element
			C1Object newObj = new C1Object(0, (o.getState() == 0) ? 1 : 0, o.getBit());
			
			// Insert the new element at index 0. This will move all subsequent elements by +1
			c.getObjects().insertElementAt(newObj, 0);
		}
	}
	
	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- C1Analyzer Event Retrieval methods 
	
	/**
	 * Get the graph events to be displayed.
	 * 
	 * @param gw - graph width in pixels
	 * @param time - current time being displayed
	 * @param gSeconds - graph width in seconds
	 * @param channel - channel to be retrieved
	 * @param top - graph top line pixel location
	 * @param bottom - graph bottom line pixel location
	 * @return
	 */
	public Vector<Line> getGraphEvents(int gw, long time, double gSeconds, int channel, double top, double bottom)
	{
		// This is the number of milliseconds that are being displayed
		double difference = (gSeconds * 1000) / 2;

		// Min and Max is the range of times being displayed on the graph
		long min = (long) (time - difference);
		long max = (long) (time + difference);
		
		// Loop through each of the channel objects to find which one we are going to use with the graph
		for (C1Channel c : channels)
		{
			// If the channel we want matches the current channel, generate the graph objects for it
			if (c.getBit() == channel)
			{
				createGraphObjects(gw, min, time, max, c.getStates(min, time, max), top, bottom);
			}
		}
		
		return graphLines;
	}
	
	/**
	 *  Creates the graph line objects to be drawn
	 *  
	 * @param gw
	 * @param min
	 * @param center
	 * @param max
	 * @param gObjects
	 * @param top
	 * @param bottom
	 */
	private void createGraphObjects(double gw, long min, long center, long max, Vector<C1Object> gObjects, double top, double bottom)
	{
		graphLines = new Vector<Line>();
		
		// Used in helping figure out where the lines will be drawn at (represents milliseconds)
		long timeBeingDisplayed = max - min;

		// This gets reassigned as we loop through each of the objects to be drawn. 
		long previousTime = max - min;
		
		// Loop through all of the objects to be graphed. 
		// NOTE: These are listed in reverse order
		for (int i = 0; i < gObjects.size(); i++)
		{
			// Retrieve the object that is going to be drawn.
			C1Object o = gObjects.elementAt(i);
			
			// Create variables to hold the line left and right positions;
			double lineLeftPx = 0.0, lineRightPx = 0.0;
			
			// This returns the millisecond position of the line to be drawn
			double lineLeft = (o.getMilli() - min);
			// If lineLeft in pixels is less than 0, we don't need to draw anything less than that, so we reset it to 0.0
			if (lineLeft < 0.0)
				lineLeft = 0.0;
			// Convert the lineLeft in milliseconds to pixels
			if (lineLeft != 0.0)
			{
				// This takes the ratio of the graph width and the time window and assigns it to a pixel value
				lineLeftPx = (gw / timeBeingDisplayed) * lineLeft;
			}	
			
			// This returns the millisecond position of the line to be drawn
			double lineRight = (previousTime * 1.0);
			// This takes the ratio of the graph width and the time window and assigns it to a pixel value
			lineRightPx = (gw / timeBeingDisplayed) * lineRight;
			
			// Get the location in pixels of where the horizontal line is to be drawn
			// NOTE: With the java graphics api, point (0, 0) is located in the TOP LEFT of the screen, not bottom Left.
			double horizontal = (o.getState() == 0) ? top : bottom;

			// Create a new Line object and add it to the array to be return for drawing
			graphLines.add(new Line((int)lineLeftPx, (int)horizontal, (int)lineRightPx, (int)horizontal));
			
			// If the current index is not 1 position smaller than the array size, we need to create a vertical line for it
			if (i != gObjects.size() - 1)
			{
				// Create a new vertical line object and add it to the array
				graphLines.add(new Line((int)lineLeftPx, (int)bottom, (int)lineLeftPx, (int)top));
			}
			
			// Re-assign the previous time.
			previousTime = o.getMilli() - min;
		}
	}
	
	/**
	 *  Returns an array with all of the channel numbers
	 * @return
	 */
	public int[] getChannelNumbers()
	{
		// Create an integer array to store the channel numbers
		int[] numbers = new int[channels.size()];
		
		// Loop through all of the channel objects we have
		for (int i = 0; i < channels.size(); i++)
		{
			// assign the channel number to the correct index position for use
			numbers[i] = channels.elementAt(i).getBit();
		}
		
		return numbers;
	}

	/**
	 *  Return the maximum time value found from all of the channel elements
	 * @return
	 */
	public int getMaxTimeInMillis()
	{
		int max = 0;
		
		// Loop through each channel element
		for (C1Channel c : channels)
		{
			// Retrieve the last object in the channel's event array
			C1Object o = c.getObjects().lastElement();
			
			// If the Log 170 Object's time is greater than the max found already, then update the max time.
			if (o.getMilli() > max)
				max = (int) o.getMilli();
		}
		
		return max;
	}	

	
	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- Event Jumping 
	
	/**
	 * Returns the next time event for a specific channel and state
	 * @param channel
	 * @param state
	 * @return
	 */
	public long returnNextTimeValueForEvent(int channel, int state)
	{
		long time = 0;
		
		// Loop through each channel element for a match to the channel
		for (C1Channel c : channels)
		{
			// Check to make sure that the channel number matches the channel in question
			if (c.getBit() == channel)
			{
				// We found the correct channel. Now lets get the index and determine its state
				C1Object o = c.getObjects().elementAt(c.getCurrentJumpElementIndex());
				
				// If the object state is the same as we are looking for, then we need to jump back by two states
				// Otherwise we only need to jump back by one position
				if (o.getState() == state)
				{
					time = c.getObjects().elementAt(c.getCurrentJumpElementIndex() + 2).getMilli();
				}
				else
				{
					time = c.getObjects().elementAt(c.getCurrentJumpElementIndex() + 1).getMilli();
				}
				
				// Since we found an event, then we can break out of the loop as there is no reason to continue searching.
				break;
			}
		}
		
		// Return the time found after we have finished searching
		return time;
	}
	
	/**
	 * Returns the previous time event for a specific channel and state
	 * 
	 * @param channel
	 * @param state
	 * @return
	 */
	public long returnPreviousTimeValueForEvent(int channel, int state)
	{
		long time = 0;
		
		// Loop through each channel element for a match to the channel
		for (C1Channel c : channels)
		{
			// Check to make sure that the channel number matches the channel in question
			if (c.getBit() == channel)
			{
				// We found the correct channel. Now lets get the index and determine its state
				C1Object o = c.getObjects().elementAt(c.getCurrentJumpElementIndex());
				
				// If the object state is the same as we are looking for, then we need to jump back by two states
				// Otherwise we only need to jump back by one position
				if (o.getState() == state)
				{
					time = c.getObjects().elementAt(c.getCurrentJumpElementIndex() - 2).getMilli();
				}
				else
				{
					time = c.getObjects().elementAt(c.getCurrentJumpElementIndex() - 1).getMilli();
				}
				
				// Since we found an event, then we can break out of the loop as there is no reason to continue searching.
				break;
			}
		}
		
		// Return the time found after we have finished searching
		return time;
	}
}
