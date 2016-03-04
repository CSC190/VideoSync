/**
 * ****************************************************************
 * File: 			DataModel.java
 * Date Created:  	June 13, 2013
 * Programmer:		Dale Reed
 * 
 * Purpose:			To handle and control all aspects of the data 
 * 					that is being used for VideoSync. It is the central
 * 					container that delegates and manages information
 * 					that is shared between every object
 * 
 * ****************************************************************
 */
package models;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Observable;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import objects.DeviceInputMap;
import objects.c1.C1Channel;
import objects.graphs.Line;
import objects.log170.L170Channel;
import views.videos.VideoPlayer;
import analyzers.C1Analyzer;
import analyzers.C1Maxim;
import analyzers.L170Analyzer;
import analyzers.VBM;

@SuppressWarnings("resource")
public class DataModel extends Observable
{
	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- Data Window Variable Declarations
	
	/**
	 * Used for keeping track of the playback rate
	 */
	private float playbackRate = 1;
	
	/**
	 * Used to indicate if we are currently playing the data
	 */
	private boolean isPlaying = false;
	
	/**
	 * Used to indicate if we have loaded data
	 */
	private boolean dataLoaded = false;
	
	/**
	 * Stores the current directory based on the file in use
	 */
	private String currentDirectory;
	
	/**
	 * Stores a reference to the log 170 file
	 * 
	 * TODO: There should also be a file reference to the c1 file, or better yet 
	 *		 it should use an array to maintain a list of all the files that are 
	 *		 in use so that the values are not hard coded.
	 */
	private File L170File;
	
	/**
	 * Stores a reference to the c1 analyzer
	 */
	private C1Analyzer c1Analyzer;

	/**
	 * Stores a reference to the c1 analyzer
	 */
	private C1Maxim c1MaximAnalyzer;

	/**
	 * Stores a reference to the 170 analyzer
	 */
	private L170Analyzer l170Analyzer;
		
	/**
	 * Stores a list of all devices that can be used with the program
	 */
	private Vector<String> deviceList;
	
	/**
	 * Stores a list of all the c1 channel objects
	 */
	private Vector<C1Channel> c1Data;

	/**
	 * Stores a list of all the c1 maxim channel objects
	 */
	private Vector<C1Channel> c1MaximData;

	/**
	 * Stores a list of all the 170 channel objects
	 */
	private Vector<L170Channel> l170Data;

	/**
	 * Stores a list of all video players
	 */
	private Vector<VideoPlayer> videoPlayers;
	
	/**
	 * Stores and creates the input map for the c1 data
	 * 
	 * FIXME: The constructor portion should be moved to where the c1 data is loaded and initialized
	 */
	private Vector<DeviceInputMap> c1InputMap = new Vector<DeviceInputMap>();

	/**
	 * Stores and creates the input map for the c1 data
	 * 
	 * FIXME: The constructor portion should be moved to where the c1 data is loaded and initialized
	 */
	private Vector<DeviceInputMap> c1MaximInputMap = new Vector<DeviceInputMap>();

	/**
	 * Stores and creates the input map for the 170 data
	 * 
	 * FIXME: The constructor portion should be moved to where the 170 data is loaded and initialized
	 */
	private Vector<DeviceInputMap> l170InputMap = new Vector<DeviceInputMap>();
	
	/**
	 * Stores the graph scale that will be used in the Data Window
	 */
	private int gScale = 15;
	
	/**
	 * Stores the graph width in seconds that will be used in the Data Window
	 */
	private double gSeconds = 1;
	
	/**
	 * Stores the current time position of all the data elements
	 */
	private long currentPosition = 0;

	/**
	 * Stores the maximum time value found between the graphs
	 */
	private int maxTimeInMillis;
	
	/**
	 * Sets the initial value of the graph offset.
	 * 
	 * TODO: This should probably be converted into an array format so that we can make the offset
	 * 		 work for each device type instead of just one. Currently it does all devices simultaneously
	 */
	private int graphOffset = 0;
		
	/**
	 * Stores the VLC path to be used with the VideoPlayers
	 */
	private String vlcPath;

	
	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- Data Model Construction and Initialization Methods
	
	/**
	 * Create the DataModel with the path to the VLC library.
	 * @param vlcPath
	 */
	public DataModel(String vlcPath)
	{
		// Set the local VLC path variable to the one passed
		this.vlcPath = vlcPath;

		// Initialize the C1 Analyzer
		// FIXME: This should be moved to where the loading of the c1 data is actually done
		c1Analyzer = new C1Analyzer();

		// Initialize the C1 Analyzer
		// FIXME: This should be moved to where the loading of the c1 data is actually done
		c1MaximAnalyzer = new C1Maxim();

		// Initialize the 170 Analyzer
		// FIXME: This shoudl be moved to where the loading of teh 170 data is actually done
		l170Analyzer = new L170Analyzer();
	
		initDeviceList();
	}
	
	/**
	 * Perform any initializations that need to be done upon startup. 
	 * 
	 * TODO: Currently this is only called from DataWindow.java - this may be able to be removed.
	 */
	public void init()
	{
		notifyObservers();
	}

	/**
	 * Initialize the device list for use in any views that utilize the devices
	 */
	private void initDeviceList()
	{
		// Create a new array to store the names of the devices
		deviceList = new Vector<String>();
		
		// Add an initial value to the list so it will be displayed.
		deviceList.add("Devices");
	}

	
	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- Data Model Getters and Setters
	
	/**
	 * Return a new DataModelProxy object from the DataModel
	 * @return
	 */
	public DataModelProxy returnProxy()
	{
		// Construct and return a new DataModelProxy object
		return new DataModelProxy(this);
	}
		
	/**
	 * Removes all existing data from the workspace so we can load up a new set
	 */
	public void removeAllData()
	{	
		// If the deviceList has more than 1 element in it, reinitialize the array
		if (deviceList.size() > 1)
			initDeviceList();
	
		// Loop through all of the VideoPlayers and shut them down.
		for (VideoPlayer vp : videoPlayers)
			vp.performShutdown(false);
		
		// Reinitialize all of the array objects that contain data to be used
		videoPlayers = new Vector<VideoPlayer>();
		c1Data = new Vector<C1Channel>();
		l170Data = new Vector<L170Channel>();
		c1InputMap = new Vector<DeviceInputMap>();
		l170InputMap = new Vector<DeviceInputMap>();
		
		// Notify all of the observers that the data model had reset and they need to do the same
		setChanged();
		notifyObservers("Reset");

	}
	
	
	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- Data Model: Input Mapping Methods

	/**
	 * Set the mapping file for the old VideoSync vbm file format.
	 * @param file
	 */
	public void setVBMFile(File file)
	{
		// Create a new VBM object from the source file
		VBM vbm = new VBM(file);
		
		// Create the input map file from the vbm file analysis.
		l170InputMap = vbm.analyze();
	}

	/**
	 * Set the mapping file based on the new VideoSync format
	 * @param mapping
	 */
	public void setMappingFile(File mapping)
	{	
		// Surround all of this in a try catch to catch any errors that may arise while reading in the data
		try
		{
			// Read the contents of the mapping file
			FileInputStream fis = new FileInputStream(mapping);
			
			// Create an object input stream from the mapping data
			ObjectInputStream ois = new ObjectInputStream(fis);

			// If the mapping file's name is for th 170, then set the mapping file
			if (mapping.getName().contains("170"))
			{
				// If the current input map file is not null, then clear it out
				if (this.l170InputMap != null)
					this.l170InputMap.clear();
				
				// Temporary variable to loop through the object input stream contents
				DeviceInputMap dim;
				
				// Loop through all objects in the file as long as its not null
				// adn add it to the array
				while ((dim = (DeviceInputMap)ois.readObject()) != null)
				{
					this.l170InputMap.add(dim);
				}
			}
			else if (mapping.getName().contains("C1"))
			{
				// TODO: This needs to implement the input mapping data for the c1 data
			}
		}
		catch (EOFException e)
		{
			e.printStackTrace();
		} 
		catch (FileNotFoundException e) 
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		} 
		catch (ClassNotFoundException e) 
		{
			e.printStackTrace();
		}
		
	}

	/**
	 * Set the initial C1 InputMap data.
	 */
	private void setC1InputMap()
	{
		// If our input map size is 0, then we are going to add all the channels to it
		if (c1InputMap.size() == 0)
		{
			// Create a temporary array from the current channel list.
			int[] channels = getChannelListForDevice("C1");
	
			// Loop through all of the channels we found from the 170 data
			for (int i = 0; i < channels.length; i++)
			{
				// Add a new DeviceInputMap object with the channel number to the array
				c1InputMap.add(new DeviceInputMap(channels[i]));
			}
		}
	}

	/**
	 * Set the initial C1 Maxim InputMap data.
	 */
	private void setC1MaximInputMap()
	{
		// If our input map size is 0, then we are going to add all the channels to it
		if (c1MaximInputMap.size() == 0)
		{
			// Create a temporary array from the current channel list.
			int[] channels = getChannelListForDevice("Maxim");
	
			// Loop through all of the channels we found from the 170 data
			for (int i = 0; i < channels.length; i++)
			{
				// Add a new DeviceInputMap object with the channel number to the array
				c1MaximInputMap.add(new DeviceInputMap(channels[i]));
			}
		}
	}

	/**
	 * Return the input map for the C1 data
	 * @return
	 */
	public Vector<DeviceInputMap> getC1InputMap()
	{
		return this.c1InputMap;
	}

	/**
	 * Updates the C1 input map from the InputMap View. 
	 * This will update all channel listings so they will automatically reflect them on the graphing window.
	 * 
	 * @param updated
	 */
	public void updateC1InputMap(Vector<DeviceInputMap> updated)
	{
		// Update the input map data
		this.c1InputMap = updated;

		// Notify all the observers of a change
		this.setChanged();
		this.notifyObservers("Input");
	}
	
	/**
	 * Updates the C1 input map from the InputMap View. 
	 * This will update all channel listings so they will automatically reflect them on the graphing window.
	 * 
	 * @param updated
	 */
	public void updateMaximInputMap(Vector<DeviceInputMap> updated)
	{
		// Update the input map data
		this.c1MaximInputMap = updated;

		// Notify all the observers of a change
		this.setChanged();
		this.notifyObservers("Input");
	}

	/**
	 * Return the input map for the 170 data
	 * @return
	 */
	public Vector<DeviceInputMap> getMaximInputMap()
	{
		return this.c1MaximInputMap;
	}

	
	/**
	 * Set the initial 170 InputMap data.
	 */
	public void set170InputMap()
	{
		// If our input map size is 0, then we are going to add all the channels to it
		if (l170InputMap.size() == 0)
		{
			// Create a temporary array from the current channel list.
			int[] channels = getChannelListForDevice("170");
	
			// Loop through all of the channels we found from the 170 data
			for (int i = 0; i < channels.length; i++)
			{
				// Add a new DeviceInputMap object with the channel number to the array
				l170InputMap.add(new DeviceInputMap(channels[i]));
			}
		}
	}
	
	/**
	 * Return the input map for the 170 data
	 * @return
	 */
	public Vector<DeviceInputMap> get170InputMap()
	{
		return this.l170InputMap;
	}

	/**
	 * Updates the 170 input map from the InputMap View. 
	 * This will update all channel listings so they will automatically reflect them on the graphing window.
	 * 
	 * @param updated
	 */
	public void update170InputMap(Vector<DeviceInputMap> updated)
	{
		// Update the input map data
		this.l170InputMap = updated;
		
		// Notify all the observers of a change
		this.setChanged();
		this.notifyObservers("Input");
	}
	
	
	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- Data Model: Channel Methods
	
	/**
	 * Return a list of all channels for a specific device
	 * @param device
	 * @return
	 */
	public int[] getChannelListForDevice(String device)
	{
		// If the device name matches 170,
		// then return the channel numbers from the 170 analyzer
		if (device.equals("170"))
			return l170Analyzer.getChannelNumbers();
		// otherwise if the name matches C1,
		// return the channel numbers from the C1 analyzer
		else if (device.equals("C1"))
			return c1Analyzer.getChannelNumbers();
		// otherwise if the name matches C1,
		// return the channel numbers from the C1 analyzer
		else if (device.equals("Maxim"))
			return c1MaximAnalyzer.getChannelNumbers();

		// If we get here return nothing
		return null;
	}

	/**
	 * Returns a list of all devices we have loaded data for
	 * @return
	 */
	public String[] getDeviceList()
	{
		// Create a new string array to the size of the device list
		String[] devices = new String[deviceList.size()];
		
		// Loop through all of the devices in the main array and add it to our temporary devices array.
		for (int i = 0; i < devices.length; i++)
		{
			devices[i] = deviceList.elementAt(i);
		}
		
		// Return the temporary device list
		return devices;
	}

	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- Data Model: C1 Data Methods
	
	/**
	 * Set the C1 data file and run the analysis on it.
	 * @param c1File
	 */
	public void setC1Data(File c1File)
	{
		// If were loading a new data file, deallocate the current data set
		if (c1Data != null)
			c1Data = null;
		
		// Analyze the contents of the new file
		c1Analyzer.performAnalysis(c1File);

		// Get the duration of the data from the analysis.
		this.maxTimeInMillis = c1Analyzer.getMaxTimeInMillis();
		
		// Indicate that we loaded some data
		dataLoaded = true;
		
		// If our list of devices does not contain "170", then add it
		if (!deviceList.contains("C1"))
		{
			deviceList.add("C1");
		}	
		
		// Set the C1 input map
		setC1InputMap();
		
		// Indicate that we have a change to all of the observers
		setChanged();
		
		// Notify any observers that respond directly to the Vector for the device list
		notifyObservers(getDeviceList());
		
		// Notify all observers of other changes
		notifyObservers();
	}

	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- Data Model: C1 Maxim Data Methods
	
	/**
	 * Set the C1 data file and run the analysis on it.
	 * @param c1File
	 */
	public void setC1MaximData(File c1MaximFile)
	{
		// If were loading a new data file, deallocate the current data set
		if (c1MaximData != null)
			c1MaximData = null;
		
		// Analyze the contents of the new file
		c1MaximAnalyzer.performAnalysis(c1MaximFile);

		// Get the duration of the data from the analysis.
		this.maxTimeInMillis = c1MaximAnalyzer.getMaxTimeInMillis();
		
		// Indicate that we loaded some data
		dataLoaded = true;
		
		// If our list of devices does not contain "Maxim", then add it
		if (!deviceList.contains("Maxim"))
		{
			deviceList.add("Maxim");
		}	
		
		// Set the C1 input map
		setC1MaximInputMap();
		
		// Indicate that we have a change to all of the observers
		setChanged();
		
		// Notify any observers that respond directly to the Vector for the device list
		notifyObservers(getDeviceList());
		
		// Notify all observers of other changes
		notifyObservers();
	}

	
	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- Data Model: 170 Data Methods

	/**
	 * Set the 170 data file and run the analysis on it.
	 * @param logFile
	 */
	public void set170Data(File logFile)
	{
		// If were loading a new data file, deallocate the current data set
		if (l170Data != null)
			l170Data = null;
		
		// Set the 170 file to the one passed for future use.
		this.L170File = logFile;
		
		// Analyze the contents of the new file
		l170Analyzer.performAnalysis(logFile);
		
		// Get the duration of the data from the analysis.
		this.maxTimeInMillis = l170Analyzer.getMaxTimeInMillis();
		
		// Indicate that we loaded some data
		dataLoaded = true;
		
		// If our list of devices does not contain "170", then add it
		if (!deviceList.contains("170"))
		{
			deviceList.add("170");
		}

		// Set the current directory to the absolute path to our new file
		this.currentDirectory = logFile.getParentFile().getAbsolutePath();
		
		// Set the 170 input map
		set170InputMap();
		
		// Indicate that we have a change to all of the observers
		setChanged();
		
		// Notify any observers that respond directly to the Vector for the device list
		notifyObservers(getDeviceList());
		
		// Notify all observers of other changes
		notifyObservers();
	}
	
	/**
	 * Returns the file name for the 170 data
	 * 
	 * FIXME: This should be modified to retrieve the file name for all devices
	 * @return
	 */
	public String getFileName()
	{
		// If the 170 file is not null, then return the name for the 170 file.
		if (this.L170File != null)
			return this.L170File.getName();
		// Otherwise just return a blank string.
		else
			return "";
	}

	
	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- Data Model: Video Methods
	
	/**
	 * Adds a video to the current workspace to be displayed.
	 * 
	 * @param vidFile
	 */
	public void addVideoFile(File vidFile)
	{
		// Make sure that the array that contains the video players is not null. 
		// If it is, then create a new instance of it so it can be used.
		if (videoPlayers == null)
			videoPlayers = new Vector<VideoPlayer>();

		boolean addVideo = true;
		// Ensure that the new video file has not already been loaded up.
		
		for (VideoPlayer vp : videoPlayers)
		{
			if (vidFile.getName().equals(vp.getVideoFile().getName()))
			{
				addVideo = false;
			}
		}
		
		if (!addVideo)
		{
			// Create the buttons for the JOptionPane
			Object[] options = {"Yes", "No"};
			
			// Show an option pane and get the result of their input.
			// Because JOptionPane requires a parent component to display the alert, we just create an empty JFrame so it will be displayed. 
			int n = JOptionPane.showOptionDialog(new JFrame(),
												"This video file has already been loaded.\nAre you sure you wish to add it anyways?",
												null,
												JOptionPane.YES_NO_OPTION,
												JOptionPane.QUESTION_MESSAGE,
												null,
												options,
												options[1]);
			
			// If 'n' is 0, then the user wants to add the video anyways.
			if (n == 0)
			{
				addVideo = true;
			}
		}
		
		// Adds the video to the workspace
		if (addVideo == true)
		{
			// Set the dataLoaded flag to true for future reference.
			dataLoaded = true;
			
			// Add the video player to the videoPlayers array.
			// The following parameters (listed in order) are passed on VideoPlayer Construction:
			// -- VLC path so the video libraries are loaded correctly
			// -- Video File to load
			// -- The current time position of the data
			// -- The ID number for the video player
			// -- A DataModelProxy Object created from this class
			// -- A reference to this class.
			//
			// FIXME: this shouldn't use both the proxy and this class....this should be changed to one or the other, not both.
			videoPlayers.add(new VideoPlayer(this.vlcPath, vidFile, currentPosition, (videoPlayers.size() + 1), new DataModelProxy(this), this));
		}
	}
	

	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- Data Model: Playback Methods
	
	/**
	 * Advance all data graphs and video by a frame
	 */
	public void advanceFrame()
	{
		// Loop through all of the video players
		for (VideoPlayer vp : videoPlayers)
		{
			// If the video player's id is 1, then it will also update the graphs to the same position.
			if (vp.getPlayerID() == 1)
				// Set the current position to the value returned from jumping the video forward by a frame.
				setCurrentPosition(vp.nextFrame(), false);
			else
				// Update all other video players if their id is not 1
				vp.nextFrame();
		}
		
		// Notify any observers of the change
		notifyObservers();
	}
		
	/**
	 * Reverse all data graphs and video by a 'frame'
	 * 
	 * NOTE: Due to VLC's implementation, there is no "frame back', so the data will jump back by ~500milliseconds
	 */
	public void reverseFrame()
	{
		// Loop through all of the video players
		for (VideoPlayer vp : videoPlayers)
		{
			// If the video player's id is 1, then it will also update the graphs to the same position.
			if (vp.getPlayerID() == 1)
				// Set the current position to the value returned from jumping the video backwards by a frame.
				setCurrentPosition(vp.previousFrame(), false);
			// Update all other video players if their id is not 1
			else
				vp.previousFrame();
		}

		// Notify any observers of the change
		notifyObservers();
	}

	/**
	 * Skip the video by the amount specified.
	 * @param amount
	 */
	public void skipVideo(int amount)
	{
		// Loop through all of the video players and skip them by the amount passed.
		for (VideoPlayer vp : videoPlayers)
		{
			vp.skipVideo(amount);
		}
		
		// Set the new position for the graphs by adding the current time to the new time value.
		long pos = this.getCurrentPosition() + amount;
		
		// Ensure that the new time value is not less than 0 and not greater than the max time found,
		// If the position is less than 0, assign it to 0
		if (pos < 0)
			pos = 0;
		// If the position is greater than the max time, assign it to the max time
		else if (pos > this.maxTimeInMillis)
			pos = this.maxTimeInMillis;
		
		// Update the current position to our new value.
		this.setCurrentPosition(pos, false);	
	}
		
	/**
	 * Return if we are currently playing the data
	 * @return
	 */
	public boolean isPlaying() 
	{
		return isPlaying;
	}
	
	/**
	 * Plays or Pauses the video based on the parameters value.
	 * @param isPlaying
	 */
	public void setPlaying(boolean isPlaying) 
	{
		// Set our isPlaying flag to the one passed
		this.isPlaying = isPlaying;

		// Loop through all of the video players
		for (VideoPlayer vp : videoPlayers)
		{
			// If we are now playing, we can play all the videos.
			if (this.isPlaying)
			{
				// Have the video players play
				vp.playVideo();
			}
			else
			{
				// Since we paused, we need to update the graph to the position of the master video player.
				// If the Video Player ID is 1, then update the graph position based on the video time of the master player.
				if (vp.getPlayerID() == 1)
					setCurrentPosition(vp.pauseVideo(), false);
				// Otherwise pause the videos.
				else
					vp.pauseVideo();
			}
		}
	}
	
	/**
	 * Set the playback rate of all the data
	 * @param rate
	 */
	public void setPlaybackRate(float rate)
	{
		// Update the Data Model's Playback rate to the one passed.
		this.playbackRate = rate;

		// Loop through all of the video players and update their playback rate.
		for (VideoPlayer vp : videoPlayers)
		{
			vp.setPlaybackSpeed(rate);
		}
		
		// Indicate to the observer that we have changes
		setChanged();
		
		// Notify all of our observers we have changes and we will send the value we want to update with.
		notifyObservers((double)playbackRate);
	}
	
	/**
	 * Return the current position of the data
	 * @return
	 */
	public long getCurrentPosition() 
	{
		return currentPosition;
	}
	
	/**
	 * Set the current data position to a new value. 
	 * 
	 * @param currentPosition
	 * @param isSliderUpdate
	 */
	public synchronized void setCurrentPosition(long currentPosition, boolean isSliderUpdate) 
	{
		// Update the current position based on the new time value.
		this.currentPosition = currentPosition;

		// If isSliderUpdate is true, we also need to update the video players to the correct position.
		if (isSliderUpdate)
		{
			// Ensure that we have video players to update.
			if (videoPlayers != null)
				// Loop through all of the video players and update their positions by jumping to a specific time.
				for (VideoPlayer vp : videoPlayers)
				{
					vp.jumpToTime(currentPosition);
				}	
		}
		
		// Call the local method to notify all observers of changes.
		notifyObservers();
	}

	/**
	 * Get the max time in milliseconds
	 * @return
	 */
	public int getMaxTimeInMillis()
	{
		return this.maxTimeInMillis;
	}
	
	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- Data Model: Graphing Methods
	
	
	/**
	 * Returns all of the state information for a specific device for graphing.
	 * 
	 * NOTE: This can be updated in the future with more devices and allow for expandability.
	 * 
	 * @param device
	 * @param center
	 * @param channel
	 * @param width
	 * @param bottom
	 * @param gap
	 * @param height
	 * @return
	 */
	public Vector<Line> getStateDataForDevice(String device, int center, int channel, int width, int bottom, int gap, int height)
	{
		// If the device is equal to "170", then return the states for the 170 data.
		if (device.equals("170"))
		{
			// Return the graph events from the 170 analysis.
			return l170Analyzer.getGraphEvents(width, currentPosition + this.graphOffset, gSeconds, channel, (height * 1.0), (bottom * 1.0));
		}
		// If the device is equal to "C1", then return the states for the C1 data.
		else if (device.equals("C1"))
		{
			// Return the graph events from the C1 Analysis
			return c1Analyzer.getGraphEvents(width, currentPosition + this.graphOffset, gSeconds, channel, (height * 1.0), (bottom * 1.0));
		}
		// If the device is equal to "C1", then return the states for the C1 data.
		else if (device.equals("Maxim"))
		{
			// Return the graph events from the C1 Analysis
			return c1MaximAnalyzer.getGraphEvents(width, currentPosition + this.graphOffset, gSeconds, channel, (height * 1.0), (bottom * 1.0));
		}
		
		// If we get to this point we don't have a device to return.
		return null;
	}
		
	/**
	 * Sets the graph scale based on the parameter value.
	 * @param scale
	 */
	public void setGraphScale(int scale)
	{
		// Set the graph scale
		this.gScale = scale;
		
		// Notify all observers of the changes
		notifyObservers();
	}
		
	/**
	 * Sets the graph width based on the parameter value.
	 * @param scale
	 */
	public void setGraphWidth(double d)
	{
		// Set the graph width
		this.gSeconds = d;

		// Notify all observers of the changes
		notifyObservers();
	}
	
	/**
	 * Return the current graph scale
	 * @return
	 */
	public int getScale()
	{
		return this.gScale;
	}

	
	/**
	 * Return the current graph width
	 * @return
	 */
	public double getSeconds()
	{
		return this.gSeconds;
	}	

	/**
	 * Set the graph offset amount so we can sync between the video and graphs.
	 * 
	 * FIXME: This needs to be expanded to allow multiple data sets to be adjusted individually.
	 * 
	 * @param offset
	 */
	public void setGraphOffset(int offset) 
	{
		// Set the graph offset amount to the parameter
		this.graphOffset = offset;
		
		// Notify all observers that we have changes
		this.notifyObservers();
	}


	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- Data Model: Observer Methods
	
	/**
	 * Notify all observers that we have changes to make.
	 */
	public void notifyObservers()
	{
		// Indicate to the observer that we have a change.
		setChanged();
		
		// Notify all observers with a new DataModelProxy object.
		notifyObservers(new DataModelProxy(this));
	}
	
	
	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- Data Model: TO FINSIH SORTING....
	
	/**
	 * Return the string with the current directory name.
	 * @return
	 */
	public String getCurrentDirectory()
	{
		return this.currentDirectory;
	}

	/**
	 * Return the flag indicating if we have loaded data
	 * 
	 * @return
	 */
	public boolean isDataLoaded() 
	{
		return dataLoaded;
	}
	
	/**
	 * Jumps to a specific event in time for a specific device.
	 * 
	 * FIXME: This needs to be updated to account for each individual graph offset, this may not be 
	 * 		  able to be done in this method and should only set the base time value and let the
	 * 		  analyzers handle the offsets instead.
	 * 
	 * @param device - 170, C1
	 * @param event - 1: Forward; 0: Backward
	 * @param channel
	 * @param bit - 1: On; 0: Off
	 */
	public void jumpToEvent(String device, int event, int channel, int bit)
	{
		// Set our initial position value to 0
		long position = 0;
		
		// If the device is a 170 type, then jump to one of its events
		if (device.equals("170"))
		{
			// If our event is 0 (meaning backwards), jump back by one event
			if (event == 0)
			{
				// Jump back to the previous event based on the channel number & bit value.
				position = l170Analyzer.returnPreviousTimeValueForEvent(channel, bit);
			}
			// Otherwise jump forwards
			else
			{
				// Jump forward to the previous event based on the channel number & bit value.
				position = l170Analyzer.returnNextTimeValueForEvent(channel, bit);
			}
		}
		// If the device is a C1 type, then jump to one of its events
		else if (device.equals("C1"))
		{
			// If our event is 0 (meaning backwards), jump back by one event
			if (event == 0)
			{
				// Jump back to the previous event based on the channel number & bit value.
				position = c1Analyzer.returnPreviousTimeValueForEvent(channel, bit);
			}
			// Otherwise jump forwards
			else
			{
				// Jump forward to the previous event based on the channel number & bit value.
				position = c1Analyzer.returnNextTimeValueForEvent(channel, bit);
			}
		}
		// If the device is a C1 type, then jump to one of its events
		else if (device.equals("Maxim"))
		{
			// If our event is 0 (meaning backwards), jump back by one event
			if (event == 0)
			{
				// Jump back to the previous event based on the channel number & bit value.
				position = c1MaximAnalyzer.returnPreviousTimeValueForEvent(channel, bit);
			}
			// Otherwise jump forwards
			else
			{
				// Jump forward to the previous event based on the channel number & bit value.
				position = c1MaximAnalyzer.returnNextTimeValueForEvent(channel, bit);
			}
		}
		
		// NOTE: This method can be expanded in the future to include other devices
				
		// Set the new position based on the graph's offset.
		setCurrentPosition(position - this.graphOffset, true);
	}
		
	/**
	 * Performs all shutdown operations for closing down videosync
	 *  
	 * TODO: Need to add in shutdown hook for if force closed.
	 */	
	public void performShutdownOperations() 
	{
		// If we have more than 1 video player currently in use, then we need to remove it before shutting down.
		if (videoPlayers != null && videoPlayers.size() > 0)
		{
			// Loop through all video players and have them perform shutdown operations.
			for (VideoPlayer vp : videoPlayers)
			{
				// The false means that some external source is closing it
				vp.performShutdown(false);
			}
		}
		
		// Quit VideoSync
		System.exit(1);
	}

	/**
	 * Presents all views to the front of the screen.
	 */
	public void presentAllViews()
	{
		// Notify the Observer that we have changes to make
		setChanged();
		
		// Notify all Observers that we have changes.
		notifyObservers("Present");
	}
		
	/**
	 * Removes a video player from the list. This is called when the video player shuts down from its own window by the user.
	 * @param vp
	 */
	public void unregisterVideo(VideoPlayer vp) 
	{
		// Remove the video player object from the array
		videoPlayers.remove(vp);
		
		// Destroy the object passed
		vp = null;
		
		// Update the remaining players with a new video id number so that there is always one that is the master player.
		for (int i = 0; i < videoPlayers.size(); i++)
		{
			videoPlayers.elementAt(i).setPlayerID(i + 1);
		}
	}

	/**
	 * Return the channel number for the name used in the selection windows.
	 * 
	 * NOTE: This should be updated to include additional devices when they are added in
	 * 
	 * @param device
	 * @param name
	 * @return
	 */
	public int getChannelFromName(String device, String name) 
	{
		// Make sure that the device being searched for is in our device list.
		if (deviceList.contains(device))
		{
			// Check to see if our name matches the 170
			if (device.equals("170"))
			{
				// Loop through the device input map contents searching for a channel with a matching name
				for (DeviceInputMap dim : l170InputMap)
				{
					// If the input map data has an element matching that name, 
					// return the bit number for that name.
					if (dim.getChannelName().equals(name))
					{
						return dim.getBitNumber();
					}
				}
			}
			// Check to see if our name matches the C1
			else if (device.equals("C1"))
			{
				// Loop through the device input map contents searching for a channel with a matching name
				for (DeviceInputMap dim : c1InputMap)
				{
					// If the input map data has an element matching that name, 
					// return the bit number for that name.
					if (dim.getChannelName().equals(name))
					{
						return dim.getBitNumber();
					}
				}
			}
			// Check to see if our name matches the Maxim
			else if (device.equals("Maxim"))
			{
				// Loop through the device input map contents searching for a channel with a matching name
				for (DeviceInputMap dim : c1MaximInputMap)
				{
					// If the input map data has an element matching that name, 
					// return the bit number for that name.
					if (dim.getChannelName().equals(name))
					{
						return dim.getBitNumber();
					}
				}
			}
		}
		
		// Return -1 if we didn't find a matching device in the list
		return -1;
	}
}
