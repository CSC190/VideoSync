/**
 * ****************************************************************
 * File: 			CommandList.java
 * Date Created:  	June 6, 2013
 * Programmer:		Dale Reed
 * 
 * Purpose:			To handle and manage all of the Command Elements
 * 					for use with the various actions that can be 
 * 					performed.
 * 
 * ****************************************************************
 */package commands;

import commands.menu.CommandImport;
import commands.menu.CommandInputMapping;
import commands.menu.CommandOpen;
import commands.menu.CommandQuit;
import commands.menu.CommandSave;
import commands.menu.CommandWindowFront;
import commands.playback.CommandFrameForward;
import commands.playback.CommandFrameReverse;
import commands.playback.CommandPlay;
import commands.playback.CommandSkip;

import commands.windows.graph.CommandGraphScale;
import commands.windows.graph.CommandGraphWidth;

import views.tabbed_panels.DataWindow;
import models.DataModel;

public class CommandList 
{
	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- CommandList Variable Declarations

	/**
	 * Opens the contents of a directory
	 */
	private CommandOpen co;
	
	/**
	 * Saves the project into a folder				
	 * TODO: Needs to be implemented
	 */
	private CommandSave cs;

	/**
	 * Imports an individual file into the project
	 */
	private CommandImport ci;			

	/**
	 * Starts the shutdown process for VideoSync
	 */
	private CommandQuit cq;			

	/**
	 * Starts the command input mapping
	 */
	private CommandInputMapping cim;		
	
	/**
	 * Command to update the graph scale 			
	 * TODO: Needs to be implemented properly - just a placeholder for now.
	 */
	private CommandGraphScale cgs;		
	
	/**
	 * Command to update the graph width
	 */
	private CommandGraphWidth cgw;		

	/**
	 * Command to play the videos and graphs
	 */
	private CommandPlay cp;		
	
	/**
	 * Command to jump forward frame by frame based on the video
	 */
	private CommandFrameForward cff;	

	/**
	 * Command to jump backward frame by frame based on the video
	 */
	private CommandFrameReverse cfr;

	/**
	 * Command to skip the videos and graphs backwards and forwards
	 */
	private CommandSkip commandSkip;
	
	/**
	 * Command to bring all the windows to the front
	 */
	private CommandWindowFront cwf;			
	
	
	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// --CommandList Construction 
	
	/**
	 * Creates a CommandList object with the two passed parameters

	 * @param dm
	 * @param g
	 */
	public CommandList(DataModel dm, DataWindow g)
	{
		// Create the Command Open Object
		co = new CommandOpen();
		
		// Create the Command Save Object
		cs = new CommandSave();
		
		// Create the Command Import Object
		ci = new CommandImport();
		
		// Create the Command Quit Object
		cq = new CommandQuit();
		
		// Create the Command Input Mapping Object
		cim = new CommandInputMapping();
		
		// Add the Command Input Mapping's input map view as an observer to the Data model.
		dm.addObserver(cim.getInputMapping());
	
		// Create the Command Graph Scale Object
		cgs = new CommandGraphScale();
		
		// Create the Command Graph Width Object
		cgw = new CommandGraphWidth();
		
		// Create the Command Play Object
		cp = new CommandPlay();
		
		// Create the Command Frame Forward Object
		cff = new CommandFrameForward();
		
		// Create the Command Frame Reverse Object
		cfr = new CommandFrameReverse();
		
		// Create the Command Skip Object
		commandSkip = new CommandSkip();
	
		// Create the Command Window Front Object
		cwf = new CommandWindowFront();

		// The following setTargets commands require a data model and a graph object.
		// These are passed from the Constructor's parameters.
		
		// Set the targets for Command Open. 
		co.setTargets(dm, g);
		
		// Set the targets for Command Save
		cs.setTargets(dm, g);
		
		// Set the targets for Command Import
		ci.setTargets(dm, g);
		
		// Set the targets for Command Quit
		cq.setTargets(dm, g);
		
		// Set the target for Command Import Mapping
		cim.setTargets(dm.returnProxy());
		
		// Set the target for Command Graph Scale
		cgs.setTarget(dm);
		
		// Set the target for Command Graph Width
		cgw.setTarget(dm);
		
		// Set the target for Command Play
		cp.setTarget(dm);
		
		// Set the target for Command Frame Forward
		cff.setTarget(dm);
		
		// Set the target for Command Frame Reverse
		cfr.setTarget(dm);
		
		// Set the target for Command Skip 
		commandSkip.setTarget(dm);
		
		// Set the target for Command Window Front
		cwf.setTargets(dm);
	}
	
	
	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- Command List Getters
	
	/**
	 * Returns the CommandOpen Object
	 * 
	 * @return
	 */
	public CommandOpen getCommandOpen()
	{
		return co;
	}

	/**
	 * Returns the CommandSave Object
	 * @return
	 */
	public CommandSave getCommandSave()
	{
		return cs;
	}

	/**
	 * Returns the CommandImport Object
	 * @return
	 */
	public CommandImport getCommandImport()
	{
		return ci;
	}

	/**
	 * Returns the CommandQuit Object
	 * @return
	 */
	public CommandQuit getCommandQuit()
	{
		return cq;
	}

	/**
	 * Returns the CommandGraphScale Object
	 * @return
	 */
	public CommandGraphScale getCommandGraphScale()
	{
		return cgs;
	}

	/**
	 * Returns the CommandGraphWidth Object
	 * @return
	 */
	public CommandGraphWidth getCommandGraphWidth()
	{
		return cgw;
	}

	/**
	 * Returns the CommandPlay Object
	 * @return
	 */
	public CommandPlay getCommandPlay()
	{
		return cp;
	}
	
	/**
	 * Returns the CommandFrameFoward Object
	 * @return
	 */
	public CommandFrameForward getCommandFrameForward()
	{
		return cff;
	}
	
	/**
	 * Returns the CommadnFrameReverse Object
	 * @return
	 */
	public CommandFrameReverse getCommandFrameReverse()
	{
		return cfr;
	}

	/**
	 * Returns the CommandInputMapping Object
	 * @return
	 */
	public CommandInputMapping getCommandInputMapping()
	{
		return this.cim;
	}
	
	/**
	 * Returns the CommandSkip Object
	 * @return
	 */
	public CommandSkip getCommandSkip()
	{
		return this.commandSkip;
	}
	
	/**
	 * Returns the CommandWindowFront Object
	 * @return
	 */
	public CommandWindowFront getCommandWindowFront()
	{
		return this.cwf;
	}
}
