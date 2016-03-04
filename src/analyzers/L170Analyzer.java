/**
 * ****************************************************************
 * File: 			L170.java
 * Date Created:  	June 19, 2013
 * Programmer:		Dale Reed
 * 
 * Purpose:			To read in a .vbm/.dat file generated from a 
 * 					170 controller and to convert it into a format
 * 					that can be utilized by VideoSync.
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

import objects.graphs.Line;
import objects.log170.L170Channel;
import objects.log170.L170Object;

@SuppressWarnings("resource")
public class L170Analyzer 
{

	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- L170 Variable Declarations

	/**
	 * Used for indicating where the start point of the data in the records are.
	 */
	final int dataStartIndex = 7;

	/**
	 * Used in determining how long it takes to perform the analysis of the file.
	 */
	long sTime;
	long eTime;
	
	/**
	 * Used for keeping track of every event that was found in the data file.
	 */
	private Vector<L170Object> events;
	
	/**
	 * Used for keeping track of every channel that was found from the events.
	 */
	private Vector<L170Channel> channels;

	/**
	 * Used for storing the graph lines based on the parameters sent by the calling class
	 */
	private Vector<Line> graphLines;

	
	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- L170 Construction 
	
	/**
	 * Constructs the L170 Analyzer and initializes the event and channel arrays
	 */
	public L170Analyzer()
	{
		events = new Vector<L170Object>();
		channels = new Vector<L170Channel>();
	}
	
	
	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- L170 Analysis Methods
	
	/**
	 * Analyzes the sent data file so that it may be used with VideoSync
	 * @param file
	 * TODO: This analysis needs to be updated to handle 170 data files with different comment headers.
	 * 			Errors were found when analyzing data provided by Jerry Kwong
	 */
 	public void performAnalysis(File file)
	{
 		boolean wantFirstLine = true;
		System.out.println("------------------------------------------------------");

		sTime = System.currentTimeMillis();
		System.out.println("Loading Log 170 File: " + file.getName());
		
		String line = null;
		
		// Surround everything in a try/catch block for catching any errors with reading the file
		try 
		{
			// Assign a buffered reader from the file contents
			BufferedReader fileReader = new BufferedReader(new FileReader(file.getPath()));
			
			boolean baseTime = true;
			int baseTimeValue = 0;
						
			// As long as there is a line to be read, continue reading everything
			while ((line = fileReader.readLine()) != null)
			{
				System.out.println("Line: " + line);
				if (!line.equals(""))
				{					
					// Get the length of the line. This is used for extracting all the elements out
					int maxLineLength = line.length();
					
					// Make sure that the starting character is not a '<' and that the length is greater than 10.
					// If the length is less than 10, there is no event data
					if (line.charAt(0) != '<' && line.length() > 10 || wantFirstLine)
					{		
						wantFirstLine = false;
						
						String timeLine;
						int addition = 0;
						// ...Not sure what the point of this is at the moment - maybe one of the datafiles has a null character somewhere in it causing problems?
						if (line.charAt(0) == '\0')
						{
							addition = 1;
							timeLine = line.substring(3, dataStartIndex + addition);
						}
						else
						{
							timeLine = line.substring(2, dataStartIndex + addition);
						}
						
						// Get the contents of the event data
						String dataLine = line.substring(dataStartIndex + addition, maxLineLength - 2);
						
						
						System.out.println("dataLine: " + dataLine);
						
						// Check to see if we need to calculate the offset for the time to start at 0
						if (baseTime)
						{
							baseTimeValue = Integer.parseInt(timeLine, 16);
							baseTime = !baseTime;
						}
						
						// Get the time element for the current set of events
						int time = Integer.parseInt(timeLine, 16);
						// Adjust the time so that it calculating from 0
						int newTime = time - baseTimeValue;
														
						// Loop through all the data elements in the array, increasing a by 3 each time
						// because the data elements are in groups of 3
						for (int a = 0; a < dataLine.length(); a+=3)
						{
							// Retrieve the record to be analyzed and split up
							String record = dataLine.substring(a, a+3);
								
							// Convert the record into a binary string
							String data = Integer.toBinaryString(Integer.parseInt(record, 16));
							
							// If the binary string is less than 12, we need to add leading 0's to it 
							// so we can analyze it correctly. 
							while (data.length() < 12)
							{
								data = "0" + data;
							}
							
							// Double check to make sure the length is correct
							if (data.length() < 12)
							{
								System.err.println("BINARY DATA LENGTH ERROR");
							}
							else
							{
								// Retrieve the 60th time parameter from the binary string
								String t = data.substring(0, 6);
								// Retrieve the state parameter from the binary string
								String s = data.substring(6, 7);
								// Retrieve the bit parameter from the binary string
								String b = data.substring(7);
											
								// Convert the 60th parameter from a binary string to an integer
								int sixty = Integer.parseInt(t, 2);
								// Convert the state parameter from a binary string to an integer
								int state = Integer.parseInt(s, 2);
								// Convert the bit parameter from binary to an integer
								int bit = Integer.parseInt(b, 2);
								
								// Add a new Log 170 Object with the integer parameters
								events.add(new L170Object(newTime, sixty, state, bit));
	
								//TODO: Determine the point of this line
								//data = data.substring(0, data.length() - 1);
							}
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
		catch (StringIndexOutOfBoundsException siiob)
		{
			System.err.println("String Index Out Of Bounds for line '" + line + "'");
			siiob.printStackTrace();
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

		for (L170Channel c : channels)
		{
			System.out.printf("Channel %d: %d events\n", c.getBit(), c.getObjects().size() / 2);
		}

		System.out.println();
		
		System.out.println("Finished Generating element records: " + (System.currentTimeMillis() - sTime) + " ms");
		System.out.println("------------------------------------------------------");

	}
	
 	/**
 	 * Creates the channel array so we can sort all of our events by channels.
 	 */
	private void createChannelArrays()
	{
		// Loop through each event that we found to create the channel data
		for (L170Object o : events)
		{
			// Create a temporary bit to hold the objects bit number
			int bit = o.getBit();
			
			// Used in determining if we found a valid pin
			boolean foundBit = false;
			
			// Loop through all the channel objects we have so far
			for (L170Channel c : channels)
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
				channels.add(new L170Channel(bit));
			}
		}
	}

	/**
	 * Adds all of the events found in the analysis to each channel individually
	 */
	private void generateChannelData()
	{	
		// Loop through each Log 170 event we detected
		for (L170Object o : events)
		{			
			// Loop through each channel we have found
			for (L170Channel c : channels)
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
		for (L170Channel c : channels)
		{
			// Get the Log 170 Object currently at the start
			L170Object o = c.getObjects().elementAt(0);
			
			// Create  new Log 170 Object at time 0, with the state opposite of the current start element
			L170Object newObj = new L170Object(0, 0, (o.getState() == 0) ? 1 : 0, o.getBit());
			
			// Insert the new element at index 0. This will move all subsequent elements by +1
			c.getObjects().insertElementAt(newObj, 0);
		}
	}
	
	
	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- L170 Graph Segment Methods
	
	/**
	 * Get the graph events to be displayed. 
	 * @param gw 		- Graph width in pixels
	 * @param time 		- Current time being displayed
	 * @param gSeconds 	- Graph width in seconds
	 * @param channel 	- Channel to be retrieved
	 * @param top		- Top Pixel Location
	 * @param bottom	- Bottom Pixel Location
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
		for (L170Channel c : channels)
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
	 */
	private void createGraphObjects(double gw, long min, long center, long max, Vector<L170Object> gObjects, double top, double bottom)
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
			L170Object o = gObjects.elementAt(i);
			
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
	

	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- L170 Event Jumping Methods

	/**
	 * Returns the next time value for the specific channel and state so that the user can jump to that location.
	 * @param channel
	 * @param state
	 * @return
	 */
	public long returnNextTimeValueForEvent(int channel, int state)
	{
		long time = 0;
		
		// Loop through each channel element for a match to the channel
		for (L170Channel c : channels)
		{
			// Check to make sure that the channel number matches the channel in question
			if (c.getBit() == channel)
			{
				// We found the correct channel. Now lets get the index and determine its state
				L170Object o = c.getObjects().elementAt(c.getCurrentJumpElementIndex());
				
				if (o.getState() == state)
				{
					time = c.getObjects().elementAt(c.getCurrentJumpElementIndex() + 2).getMilli();
				}
				else
				{
					time = c.getObjects().elementAt(c.getCurrentJumpElementIndex() + 1).getMilli();
				}
			}
		}
		
		return time;
	}
	
	/**
	 * Returns the previous time value for the specific channel and state so that the user can jump to that location.
	 * @param channel
	 * @param state
	 * @return
	 */
	public long returnPreviousTimeValueForEvent(int channel, int state)
	{
		long time = 0;
		
		// Loop through each channel element for a match to the channel
		for (L170Channel c : channels)
		{
			// Check to make sure that the channel number matches the channel in question
			if (c.getBit() == channel)
			{
				// We found the correct channel. Now lets get the index and determine its state
				L170Object o = c.getObjects().elementAt(c.getCurrentJumpElementIndex());
				
				if (o.getState() == state)
				{
					time = c.getObjects().elementAt((c.getCurrentJumpElementIndex() > 2) ? c.getCurrentJumpElementIndex() - 2 : 0).getMilli();
				}
				else
				{
					time = c.getObjects().elementAt((c.getCurrentJumpElementIndex() > 1) ? c.getCurrentJumpElementIndex() - 1 : 0).getMilli();
				}
			}
		}
		
		return time;
	}


	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- L170 Getter Methods

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
		for (L170Channel c : channels)
		{
			// Retrieve the last object in the channel's event array
			L170Object o = c.getObjects().lastElement();
			
			// If the Log 170 Object's time is greater than the max found already,
			// Update the max time.
			if (o.getMilli() > max)
				max = o.getMilli();
		}
		
		return max;
	}	
}