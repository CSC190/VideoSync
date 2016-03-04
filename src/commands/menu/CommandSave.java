/**
 * ****************************************************************
 * File: 			CommandSave.java
 * Date Created:  	June 4, 2013
 * Programmer:		Dale Reed
 * 
 * Purpose:			To handle an action request from the menu to 
 * 					save a directory containing the files to the computer
 * 
 * ****************************************************************
 */
package commands.menu;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import views.tabbed_panels.DataWindow;

import models.DataModel;

public class CommandSave extends AbstractAction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- Command Save Methods

	/**
	 * Sets the references to the DataModel and Data Window
	 * @param dm
	 * @param g
	 */
	public void setTargets(DataModel dm, DataWindow g)
	{

	}

	
	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- Command Save Action Methods
	
	/**
	 * Called when the user selects the "Save" option from the File menu.
	 */
	public void actionPerformed(ActionEvent e) 
	{
		// TODO: This needs to save all the information of the current instance.
	}
}
