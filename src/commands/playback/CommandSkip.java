/**
 * ****************************************************************
 * File: 			CommandSkip.java
 * Date Created:  	August 14, 2013
 * Programmer:		Dale Reed
 * 
 * Purpose:			To handle an action request from the play back
 * 					panel to skip the video and graphs ahead or back
 * 					by a specified interval.
 * 
 * 
 * ****************************************************************
 */
package commands.playback;

import java.awt.event.ActionEvent;
import java.util.Observable;
import java.util.Observer;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.Timer;

import models.DataModel;

public class CommandSkip extends AbstractAction implements Observer 
{
	private static final long serialVersionUID = 1L;


	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- Command Skip Variable Declarations
	
	/**
	 * The base rate we are going to skip the data by
	 */
	private final double baseRate = 200;
	
	/**
	 * Used to reference to the DataModel
	 */
	private DataModel dm;
	
	/**
	 * Timer to control the amount of time between skips
	 */
	private Timer skipTimer;

	/**
	 * Sets the initial timer speed to the same as the base rate.
	 */
	private double timerSpeed = baseRate;
	
	/**
	 * TODO: Figure out what this is used for.
	 */
	private boolean update = false;
	

	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- Command Skip Construction
	
	/**
	 * Create the Command Skip Object
	 */	
	public CommandSkip()
	{		
		// Create the new Timer Instance and set its duration and caller
		skipTimer = new Timer((int)timerSpeed, this);
		
		// Immediately pause the timer so that it isn't running upon startup
		skipTimer.stop();
	}
	
	
	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- Command Skip Methods

	/**
	 * Sets the references to the DataModel
	 * @param dm
	 */
	public void setTarget(DataModel dm)
	{
		// Set the data model to the one passed
		this.dm = dm;
		
		// Add the Command Skip object as an observer to the data model
		this.dm.addObserver(this);
	}


	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- Command Skip Action Methods
	
	/**
	 * Called when the user chooses to skip the data
	 */
	public void actionPerformed(ActionEvent e) 
	{
		if (e.getSource() instanceof JButton)
		{
			// TODO: Figure out if this is still necessary, as its currently blank and the program functions, it may not be necessary.
		}
		
		// If the data model is currently playing, then update the graphs position.
		if (this.dm.isPlaying())
		{
			// Get the last time value of the graph data
			long lastPos = this.dm.getCurrentPosition();
			
			// Calculate a new time value based on the last time value and the value of the timer speed.
			long newPos = lastPos + (int)timerSpeed;
						
			// TODO: Look into why we used an update flag here. This might be able to be removed.
			if (update)
				// Update the current position of the graph window to the one we have calculated.
				this.dm.setCurrentPosition(newPos, false);
			else
				update = true;
		}
	}


	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- Command Skip Update Events 
	
	/**
	 * Updates the Command Skip via a notification from the data model.
	 */
	public void update(Observable arg0, Object arg1) 
	{
		// If the argument passed is a type Double, then we can proceed with updating variables.
		// Otherwise we can just ignore it.
		if (arg1 instanceof Double)
		{
			// Update the timer speed based on the base time value and the parameter passed.
			timerSpeed = baseRate * (Double)arg1;
		}
	}
}
