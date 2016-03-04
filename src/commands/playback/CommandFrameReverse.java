/**
 * ****************************************************************
 * File: 			CommandFrameReverse.java
 * Date Created:  	June 5, 2013
 * Programmer:		Dale Reed
 * 
 * Purpose:			To handle an action request from the play back
 * 					view to reverse the video and graphs forward
 * 					by one frame based on the video timings
 * 
 * ****************************************************************
 */
package commands.playback;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import models.DataModel;

public class CommandFrameReverse extends AbstractAction
{
	private static final long serialVersionUID = 1L;


	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- Command Frame Reverse Variable Declarations
	
	/**
	 * Sets the references to the DataModel
	 * @param dm
	 */
	private DataModel dm;
	
	
	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- Command Frame Reverse Methods

	/**
	 * Sets the references to the DataModel
	 * @param dm
	 */
	public void setTarget(DataModel dm)
	{
		// Set the data model to the one passed
		this.dm = dm;
	}


	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- Command Frame Reverse Action Methods
	
	/**
	 * Called when the user chooses to frame reverse
	 */
	public void actionPerformed(ActionEvent arg0) 
	{
		// Notify the data model to reverse the videos and graphs by one frame.
		this.dm.reverseFrame();
	}
}
