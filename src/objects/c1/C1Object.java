package objects.c1;

public class C1Object 
{
	/**
	 * Used to keep track of the millisecond value for the event
	 */
	private long millis;
	
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
	// -- C1Object Constructor
	
	/**
	 * Constructs the new Log C1 Events object
	 * 
	 * @param time 	- Time sent in seconds
	 * @param state - The state of the event
	 * @param bit 	- The bit number of the event
	 */
	public C1Object(long time, int state, int bit)
	{
		this.millis = time;
			
		this.state = state;
		this.bit = bit;		
	}

	
	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- C1Object Getters & Setters
		
	/**
	 * Returns the milliseconds value of the event.
	 * 
	 * @return
	 */
	public long getMilli()
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
		return String.format("Bit %d: -- Milli: %d -- State: %d", this.bit, this.millis, this.state);
	}
}
