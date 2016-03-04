/**
 * ****************************************************************
 * File: 			L170Object.java
 * Date Created:  	June 18, 2013
 * Programmer:		Dale Reed
 * 
 * Purpose:			To keep track of each individual Log 170 
 * 					Channel Object
 * 
 * ****************************************************************
 */
package objects.log170;

public class L170Object 
{
	/**
	 * Used to keep track of the millisecond value for the event
	 */
	private int millis;
	
	/**
	 * Used to keep track of the sixtieth value for the event
	 */
	private int sixty;
	
	/**
	 * Used to keep track of the state
	 */
	private int state;
	
	/**
	 * Used to keep track of the bit number
	 */
	private int bit;

	
	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- L170Object Constructor
	
	/**
	 * Constructs the new Log 170 Events object
	 * 
	 * @param time 	- Time sent in seconds
	 * @param sixty - The number of sixtieth for the event
	 * @param state - The state of the event
	 * @param bit 	- The bit number of the event
	 */
	public L170Object(int time, int sixty, int state, int bit)
	{
		// Convert the seconds to milliseconds, and convert and the sixtieth to milliseconds
		this.millis = (int)(((float)time * 1000) + ((float)sixty / (float)60) * 1000);
		
		// Store the total number of sixtieths for the event
		this.sixty = (time * 60) + sixty;
		
		// Store the state of the event
		this.state = state;
		
		// Store the bit number of the event
		this.bit = bit;		
	}
	
	
	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- L170Object Getters & Setters
		
	/**
	 * Returns the milliseconds value of the event.
	 * 
	 * @return
	 */
	public int getMilli()
	{
		return this.millis;
	}
	
	/**
	 * Returns the state of the event.
	 * @return
	 */
	public int getState()
	{
		return this.state;
	}

	/**
	 * Returns the bit number of the event.
	 * @return
	 */
	public int getBit()
	{
		return this.bit;
	}

	/**
	 * Returns a string with the format of the message to be written to the log file
	 */
	public String toString()
	{
		return String.format("Bit %d: -- Sixty: %d -- Milli: %d -- State: %d", this.bit, this.sixty, this.millis, this.state);
	}
}

//-- Unused methods 	
//	
//	public int getSixty()
//	{
//		return this.sixty;
//	}
//
//	public int betweenMilli(int min, int max)
//	{
//		if (this.millis > min && this.millis < max) return 0;
//		if (this.millis > max) return 1;
//		if (this.millis < min) return -1;
//		
//		return -5;
//	}

