/**
 * ****************************************************************
 * File: 			DataModelProxy.java
 * Date Created:  	June 13, 2013
 * Programmer:		Dale Reed
 * 
 * Purpose:			To act as an interface to the Data Model and 
 * 					not allow direct access to the Data Model 
 * 					components.
 * 
 * TODO: Note, this is not an ideal setup and should probably be 
 * 		 switched over to a extension of the Data Model rather than
 * 		 its current implementation
 * 
 * ****************************************************************
 */
package models;

import java.util.Vector;

import objects.DeviceInputMap;
import objects.graphs.Line;
import views.videos.VideoPlayer;

public class DataModelProxy
{
	/**
	 * Reference to the master Data Model
	 */
	private DataModel dm;
	
	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- Data Model Proxy Construction 

	/**
	 * Construct a Data Model Proxy with a reference to the Data Model
	 * @param dm
	 */
	public DataModelProxy (DataModel dm)
	{
		// Set the DataModelProxy object
		this.dm = dm;
	}


	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- Note: All of the following methods call their appropriate method within the Data Model, so no comments are included within the 
	//			functions themselves as they are all identical.
	
	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- Data Model Proxy Setters

	/**
	 * Notifies the data model to skip the data by a certain amount.
	 * @param amount
	 */
	public void skipVideo(int amount)
	{
		this.dm.skipVideo(amount);
	}
	
	/**
	 * Removes a video player from the data model.
	 * @param vp
	 */
	public void unregisterVideo(VideoPlayer vp)
	{
		this.dm.unregisterVideo(vp);
	}
	
	/**
	 * Update the input mapping data from the Input Mapping View
	 * @param device - The Device Type we are going to update.
	 * @param data - The mapping data to store
	 */
	public void updateInputMapForDevice(String device, Vector<DeviceInputMap> data)
	{
		if (device.equals("C1"))
		{
			this.dm.updateC1InputMap(data);
		}
		else if (device.equals("170"))
		{
			this.dm.update170InputMap(data);
		}
		else if (device.equals("Maxim"))
		{
			this.dm.updateMaximInputMap(data);
		}
	}

	/**
	 * Set the playback rate of the data
	 * @param rate
	 */
	public void setPlaybackRate(float rate)
	{
		this.dm.setPlaybackRate(rate);
	}
	
	/**
	 * Set the current position
	 * @param time
	 */
	public void setCurrentTime(long time)
	{
		dm.setCurrentPosition(time, false);
	}

	/**
	 * Set the current position based on a slider's value.
	 * @param position
	 */
	public void setSliderPosition(int position)
	{
		this.dm.setCurrentPosition(position, true);
	}

	/**
	 * Tell the Data Model Proxy to jump to a specific event
	 * @param device - The device to jump with
	 * @param event - The event within the device
	 * @param channel - The channel to jump with
	 * @param state - The state of the event we want
	 */
	public void jumpToEvent(String device, int event, int channel, int state)
	{
		this.dm.jumpToEvent(device, event, channel, state);
	}	
	
	
	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- Data Model Proxy Getters

	/**
	 * Used to indicate if data was loaded into the Data model
	 * @return
	 */
	public boolean dataLoaded()
	{
		return this.dm.isDataLoaded();
	}

	/**
	 * Return the value of the graph width in seconds
	 * @return
	 */
	public double getGraphWindowSeconds()
	{
		return this.dm.getSeconds();
	}

	/**
	 * Return the value of the Graph's scale 
	 * @return
	 */
	public double getGraphWindowScale()
	{
		return this.dm.getScale();
	}

	/**
	 * Return the data for a channel to be displayed in the graph.
	 * @param device
	 * @param center
	 * @param channel
	 * @param width
	 * @param base
	 * @param gap
	 * @param height
	 * @return
	 */
	public Vector<Line> getDataForChannel(String device, int center, int channel, int width, int base, int gap, int height)
	{
		return this.dm.getStateDataForDevice(device, center, channel, width, base, gap, height);
	}

	/**
	 * Return the value of if we are currently playing the data
	 * @return
	 */
	public boolean isPlaying()
	{
		return this.dm.isPlaying();
	}
	
	/**
	 * Return the current position of the data
	 * @return
	 */
	public long getCurrentPosition()
	{
		return this.dm.getCurrentPosition();
	}
	
	/**
	 * Return the max value to set the slider to.
	 * @return
	 */
	public int getSliderMax()
	{
		return this.dm.getMaxTimeInMillis();
	}

	/**
	 * Return the input mapping of the C1 Data
	 * @return
	 */
	public Vector<DeviceInputMap> getC1InputMap()
	{
		return dm.getC1InputMap();
	}

	/**
	 * Return the input mapping of the C1 Data
	 * @return
	 */
	public Vector<DeviceInputMap> getMaximInputMap()
	{
		return dm.getMaximInputMap();
	}
	
	/**
	 * Return the Input Mapping of the 170 Data
	 * @return
	 */
	public Vector<DeviceInputMap> get170InputMap()
	{
		return dm.get170InputMap();
	}
	
	/**
	 * Return the input map based on the the type of device we are looking at
	 * @param device
	 * @return
	 */
	public Vector<DeviceInputMap> getInputMapForDevice(String device)
	{
		if (device.equals("C1"))
			return this.dm.getC1InputMap();
		else if (device.equals("Maxim"))
			return this.dm.getMaximInputMap();
		else
			return this.dm.get170InputMap();
	}

	/**
	 * Return the base directory of the data
	 * @return
	 */
	public String getCurrentDirectory()
	{
		return this.dm.getCurrentDirectory();
	}

	/**
	 * Return the channel number based on the device and name it currently has.
	 * @param device
	 * @param name
	 * @return
	 */
	public int getChannelFromName(String device, String name)
	{
		return this.dm.getChannelFromName(device, name);
	}

	/**
	 * Returns the file name
	 * 
	 * FIXME: This should be modified to include any data file listed.
	 * @return
	 */
	public String getFileName()
	{
		return this.dm.getFileName();
	}
}

