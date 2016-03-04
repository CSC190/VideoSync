/**
 * ****************************************************************
 * File: 			PlaybackPane.java
 * Date Created:  	June 26, 2013
 * Programmer:		Dale Reed
 * 
 * Purpose:			To handle and control aspects related to 
 * 					play back controls for moving around the data 
 * 					and video files.
 * 
 * ****************************************************************
 */
package controllers;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Hashtable;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import models.DataModelProxy;
import threads.ThreadSkip;

import commands.playback.CommandFrameForward;
import commands.playback.CommandFrameReverse;
import commands.playback.CommandPlay;

public class PlaybackPane extends JPanel implements Observer, MouseListener, ChangeListener
{
	private static final long serialVersionUID = 1L;
	

	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- Playback Pane Variable Declarations

	/**
	 * Used to notify the rest of the program of any changes that may occur.
	 */
	private DataModelProxy dmp;

	/**
	 * Used for playing or pausing the data
	 */
	private JButton button_Play;

	/**
	 * Used for fast forwarding the data
	 */
	private JButton button_FastForward;
	
	/**
	 * Used for rewinding the data
	 */
	private JButton button_Rewind;
	
	/**
	 * Used for jumping the frames forward one at a time
	 */
	private JButton btnFrameAdvance;
	
	/**
	 * Used for jumping the frames backwards.
	 */
	private JButton btnJumpBack;

	/**
	 * Used for indicating the data position overall.
	 */
	private JSlider slider_MoviePosition;
	
	/**
	 * Used for choosing the data's play back speed.
	 */
	private JSlider slider_MovieSpeed;

	/**
	 * Used for indicating the current time for the data.
	 */
	private JLabel lblCurrentPos;

	/**
	 * Used for indicating the maximum time for the data
	 */
	private JLabel label_MaxPosition;

	/**
	 * Used for skipping the data back and forth
	 */
	private ThreadSkip ts;

	
	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- Playback Pane Construction 

	/**
	 * Construct the Playback Pane to be displayed in the Data Window.
	 * 
 	 * Note: This sets up all the user interface elements to be displayed in the Event Detection Pane.
	 * 		 Most of this code has been generated by Window Builder for Eclipse
	 */
	public PlaybackPane() 
	{
		setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		setSize(new Dimension(422, 128));
		setLayout(new BorderLayout(0, 0));
		
		JLabel lblPlaybackControls = new JLabel("Playback Controls");
		add(lblPlaybackControls, BorderLayout.NORTH);
		lblPlaybackControls.setHorizontalAlignment(SwingConstants.CENTER);
		
		JPanel panel_2 = new JPanel();
		add(panel_2, BorderLayout.CENTER);
		panel_2.setLayout(null);
		
		JLabel lblMoviePosition = new JLabel("Movie Position");
		lblMoviePosition.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
		lblMoviePosition.setBounds(8, 6, 77, 16);
		panel_2.add(lblMoviePosition);
		
		slider_MoviePosition = new JSlider();
		slider_MoviePosition.setEnabled(false);
		slider_MoviePosition.addMouseListener(this);
		slider_MoviePosition.setValue(0);
		slider_MoviePosition.setMaximum(80);
		slider_MoviePosition.setBounds(75, 0, 337, 29);
		panel_2.add(slider_MoviePosition);
		
		slider_MovieSpeed = new JSlider();
		slider_MovieSpeed.setMajorTickSpacing(1);
		slider_MovieSpeed.setValue(0);
		slider_MovieSpeed.setMaximum(8);
		slider_MovieSpeed.setMinimum(1);
		
		Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();
		labelTable.put(1, 	new JLabel("1x"));
		labelTable.put(2, 	new JLabel("2x"));
		labelTable.put(3, 	new JLabel("3x"));
		labelTable.put(4, 	new JLabel("4x"));
		labelTable.put(5, 	new JLabel("5x"));
		labelTable.put(6, 	new JLabel("6x"));
		labelTable.put(7,	new JLabel("7x"));
		labelTable.put(8,	new JLabel("8x"));
		slider_MovieSpeed.setLabelTable(labelTable);
		
		slider_MovieSpeed.setPaintTicks(true);
		slider_MovieSpeed.setPaintLabels(true);
		slider_MovieSpeed.setSnapToTicks(true);
		slider_MovieSpeed.setEnabled(false);
		slider_MovieSpeed.setBounds(75, 28, 233, 36);
		slider_MovieSpeed.addChangeListener(this);
		panel_2.add(slider_MovieSpeed);
		
		JLabel lblMovieSpeed = new JLabel("Movie Speed");
		lblMovieSpeed.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
		lblMovieSpeed.setBounds(8, 38, 77, 16);
		panel_2.add(lblMovieSpeed);
		
		button_Play = new JButton("Play");
		button_Play.setEnabled(false);
		button_Play.setBounds(283, 73, 77, 29);
		panel_2.add(button_Play);
		
		button_FastForward = new JButton(">>");
		button_FastForward.setToolTipText("Fast Forward");
		button_FastForward.setEnabled(false);
		button_FastForward.setBounds(359, 73, 55, 29);
		button_FastForward.addMouseListener(this);
		panel_2.add(button_FastForward);
		
		button_Rewind = new JButton("<<");
		button_Rewind.setToolTipText("Rewind");
		button_Rewind.setEnabled(false);
		button_Rewind.setBounds(125, 73, 55, 29);
		button_Rewind.addMouseListener(this);
		panel_2.add(button_Rewind);
		
		lblCurrentPos = new JLabel("");
		lblCurrentPos.setBounds(8, 67, 116, 16);
		panel_2.add(lblCurrentPos);
		
		label_MaxPosition = new JLabel("");
		label_MaxPosition.setBounds(8, 82, 116, 16);
		panel_2.add(label_MaxPosition);
		
		btnFrameAdvance = new JButton(">");
		btnFrameAdvance.setToolTipText("Frame Advance");
		btnFrameAdvance.setEnabled(false);
		btnFrameAdvance.setBounds(306, 35, 98, 29);
		panel_2.add(btnFrameAdvance);
		
		btnJumpBack = new JButton("Jump Back");
		btnJumpBack.setToolTipText("Jump Back");
		btnJumpBack.setEnabled(false);
		btnJumpBack.setBounds(180, 73, 103, 29);
		panel_2.add(btnJumpBack);
	}

	
	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- Event Detection Pane Update Events 
	
	/**
	 * Enables all of the items that can be enabled.
	 * @param enable
	 */
	public void enableAllItems(boolean enable)
	{
		slider_MoviePosition.setEnabled(enable);
		slider_MovieSpeed.setEnabled(enable);
		button_Rewind.setEnabled(enable);
		button_FastForward.setEnabled(enable);
		button_Play.setEnabled(enable);
		btnFrameAdvance.setEnabled(enable);
		btnJumpBack.setEnabled(enable);
	}

	/**
	 * Update the slider's position with the maximum value.
	 * @param max
	 */
	public void updateSliderMax(int max)
	{
		slider_MoviePosition.setMaximum(max);
	}
	
	/**
	 * Update the labels based on information from the data model
	 */
	public void updateLabels()
	{		
		// Update the current time label from the data model's current position
		lblCurrentPos.setText(convertToTimeFormat(dmp.getCurrentPosition()));

		// Update the current time label from the data model's maximum time
		label_MaxPosition.setText(convertToTimeFormat(dmp.getSliderMax()));
	}

	
	/**
	 * Convert the millisecond time value to HH:MM:SS.millis that is more human readable
	 * @param msTime
	 * @return
	 */
	private String convertToTimeFormat(long msTime)
	{
		// Get the number of millis in the time 
		int millis = (int)(msTime - ((msTime / 1000) * 1000));
		
		// Get the number of seconds in the time
		int seconds = (int)(msTime / 1000);
		
		// As long as seconds is greater than 59, subtract 60 from it
		while (seconds > 59)
			seconds -= 60;
		
		// Get the number of minutes from the millisecond time
		int minutes = (int)(msTime / 1000) / 60;
		
		// Get the total number of hours from the value of minutes
		int hours = minutes / 60;
		
		// Return the formatted string
		return String.format("%02d:%02d:%02d.%03d", hours, minutes, seconds, millis);
	}
	

	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- Playback Pane Getter's and Setter's

	/**
	 * Sets the action command for the Play Action
	 * @param commandReverse
	 */
	public void setPlayActionCommand(CommandPlay commandPlay) 
	{
		button_Play.setAction(commandPlay);
		button_Play.setText("Play");
		button_Play.setEnabled(false);
	}
	
	/**
	 * Sets the action command for the Frame Forwards Action
	 * @param commandReverse
	 */
	public void setFrameForwardActionCommand(CommandFrameForward commandForward)
	{
		btnFrameAdvance.setAction(commandForward);
		btnFrameAdvance.setText(">");
		btnFrameAdvance.setToolTipText("Frame Advance");
		btnFrameAdvance.setEnabled(false);
	}
	
	/**
	 * Sets the action command for the Frame Backwards Action
	 * @param commandReverse
	 */
	public void setFrameBackwardActionCommand(CommandFrameReverse commandReverse)
	{
		btnJumpBack.setAction(commandReverse);
		btnJumpBack.setText("<");
		btnJumpBack.setToolTipText("Frame Back");
		btnJumpBack.setEnabled(false);
	}


	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- Java Event Listeners 
	// -- NOTE: Also includes the Observer 'update' method
	// -- 		Also includes the MouseListener methods
	// -- 		Also includes the ChangeListener methods

	/**
	 * Invoked when the Data Model sends out a notification that an event changed that requires the 
	 * observers to pay attention to the data coming in
	 */
	public void update(Observable o, Object arg)
	{
		// Check to see if arg is an instance of the DataModelProxy
		if (arg instanceof DataModelProxy)
		{
			// If dmp is null, then we assign arg to dmp
			if (dmp == null)
				dmp = (DataModelProxy)arg;
		
			// Check to see if we loaded data successfully into the DataModel
			// If there is data loaded in the data model, then we can enable all the UI elements.
			// Otherwise we disable everything until data is loaded.
			if (dmp.dataLoaded())
			{
				enableAllItems(true);
				
				// Update the slider's max value
				updateSliderMax(dmp.getSliderMax());
				
				// Update all of the Labels
				updateLabels();
				
				// Set the current position of the slider's position to the value in the data model
				slider_MoviePosition.setValue((int)dmp.getCurrentPosition());
				
				// If the data model is playing, then set the play button text to "Pause"
				// Otherwise set it to "Play"
				if (dmp.isPlaying())
				{
					button_Play.setText("Pause");
				}
				else
				{
					button_Play.setText("Play");
				}
			}
			else
			{
				// Disable all the items
				enableAllItems(false);
			}	
		}		
	}

	/**
	 * Invoked when any UI elements with a ChangeListener event fires
	 */
	public void stateChanged(ChangeEvent e) 
	{
		// If the source of the change event is the movie speed slider,
		// we then update the playback rate in the data model and notify
		// all of the model's listeners
		if (e.getSource() == slider_MovieSpeed)
		{
			// Set the playback rate to the value of the slider
			this.dmp.setPlaybackRate((float)((JSlider)e.getSource()).getValue());
		}
	}
	
	/**
	 * Invoked when any UI elements with a MouseListener event fires
	 */
	public void mousePressed(MouseEvent e) 
	{
		// If the source of the event is the rewind button, then start
		// skipping the video backwards.
		if (e.getSource() == button_Rewind)
		{
			// If the ThreadSkip object is null,
			// create a new thread skip object
			if (ts == null)
				// Create the thread skip with the dmp and direction values.
				// Direction parameter of -1 means backwards
				ts = new ThreadSkip(this.dmp, -1);
			
			// Start skipping the data
			ts.start();			
		}
		// If the source of the event is the fast forward button, then start
		// skipping the video backwards.
		else if (e.getSource() == button_FastForward)
		{			
			// If the ThreadSkip object is null,
			// create a new thread skip object
			if (ts == null)
				// Create the thread skip with the dmp and direction values.
				// Direction parameter of 1 means forwards
				ts = new ThreadSkip(this.dmp, 1);
			
			// Start skipping the data
			ts.start();
		}
	}

	public void mouseReleased(MouseEvent e) 
	{
		// If the source of our event is either the rewind or fast forward buttons,
		// we can stop the thread skip.
		if (e.getSource() == button_Rewind || e.getSource() == button_FastForward)
		{
			// Stop skipping the data
			ts.stopThread();
		}
		// Otherwise we have a slider event and can tell the data model to update
		// all observers with the value of the slider
		else
		{
			// Ensure that the data model proxy is not null and is not playing
			if (dmp != null && !dmp.isPlaying())
			{
				// Ensure that the source is our movie position slider
				if (e.getSource() == slider_MoviePosition)
				{
					// Create a temporary object for our source slider
					JSlider slider = (JSlider)e.getSource();
					
					// Set the position of the slider to the one that we pass
					dmp.setSliderPosition(slider.getValue());
				}
			}				
		}
	}
	
	
	/**
	 * The following functions pertain to the various implementations that are currently not being used by the class.
	 */
	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- Java Mouse Event Listeners 
	// -- NOTE: None of the following are currently implemented in this version

	public void mouseClicked(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
}
