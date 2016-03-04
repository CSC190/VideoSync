/**
 * ****************************************************************
 * File: 			DeviceInputMap.java
 * Date Created:  	July 23, 2013
 * Programmer:		Dale Reed
 * 
 * Purpose:			To keep track of the individual channel
 * 					elements for the user to customize the channel
 * 					attributes.
 * 
 * ****************************************************************
 */

package objects;

import java.io.Serializable;

public class DeviceInputMap implements Serializable
{
	private static final long serialVersionUID = 7654853157437949476L;

	/**
	 * Used for referencing the original channel number that is used in all of the data structures.
	 */
	private int bitNumber;
	
	/**
	 * Used for indicating which lane number it is.
	 */
	private int laneNumber;

	/**
	 * Used for storing a custom file name.
	 */
	private String channelName;
	
	/**
	 * Indicates if the channel is a intersection, freeway, or ramp
	 */
	private String detectorType;

	/**
	 * Indicates which general direction the lane is heading. 
	 * -- Valid Inputs:
	 * 					-- N: Northbound
	 * 					-- S: Southbound
	 * 					-- E: Eastbound
	 * 					-- W: Westbound
	 */
	private String direction;

	/**
	 * Indicates what type of detector is being used
	 * 
	 * -- Valid Inputs:
	 * 					-- Radar
	 * 					-- Loop
	 */
	private String channelType;

	
	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- DeviceInputMap Constructor
	
	/**
	 * Constructs a DeviceInputMap Object with only a bit number.
	 * 
	 * @param bit
	 */
	public DeviceInputMap(int bit)
	{
		// Calls the DeviceInputMap constructor with 2 parameters.
		this(bit, null);
	}
	
	/**
	 * Constructs a DeviceInputMap Object with a bit number and a channelName.
	 *
	 * @param bit
	 * @param channelName
	 */
	public DeviceInputMap(int bit, String channelName)
	{
		// Sets the bit number to the one that was passed
		this.bitNumber = bit;
		
		// If the channel name is not null, set it to the one passed, otherwise set it to a default one.
		this.channelName = (channelName == null) ? String.format("Channel %d", this.bitNumber) : channelName;
	}
	

	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- DeviceInputMap Getters & setters
	
	/**
	 * Set the lane number
	 * @param number
	 */
	public void setLaneNumber(int number)
	{
		this.laneNumber = number;
	}
	
	/**
	 * Set the channel name
	 * @param name
	 */
	public void setChannelName(String name)
	{
		this.channelName = name;
	}
	
	/**
	 * Set the detector type
	 * @param detectorType
	 */
	public void setDetectorType(String detectorType)
	{
		this.detectorType = detectorType;
	}
	
	/**
	 * Set the lane direction
	 * @param direction
	 */
	public void setDirection(String direction)
	{
		this.direction = direction;
	}
	
	/**
	 * Set the channel type
	 * @param channelType
	 */
	public void setChannelType(String channelType)
	{
		this.channelType = channelType;
	}
	
	/**
	 * Return the bit number for this object.
	 * @return
	 */
	public int getBitNumber()
	{
		return this.bitNumber;
	}
	
	/**
	 * Return the lane number for this object
	 * @return
	 */
	public int getLaneNumber()
	{
		return this.laneNumber;
	}
	
	/**
	 * Return the channel name for this object
	 * @return
	 */
	public String getChannelName()
	{
		return this.channelName;
	}
	
	/**
	 * Return the detector type for this object
	 * @return
	 */
	public String getDetectorType()
	{
		return this.detectorType;
	}
	
	/**
	 * Return the direction for this object
	 * @return
	 */
	public String getDirection()
	{
		return this.direction;
	}
	
	/**
	 * Return the channel type for this object.
	 * @return
	 */
	public String getChannelType()
	{
		return this.detectorType;
	}
	
	/**
	 * Returns a string representation of the device input map object
	 */
	public String toString()
	{
		// Return the formatted string
		return String.format("Bit: %3d\t Name: %s\t Lane #: %d\t Detector Type: %s\t Direction: %s\t Channel Type: %5s\t", bitNumber, channelName, laneNumber, detectorType, direction, channelType);
	}
}
