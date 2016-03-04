/**
 * ****************************************************************
 * File: 			CommandOpen.java
 * Date Created:  	June 4, 2013
 * Programmer:		Dale Reed
 * 
 * Purpose:			To handle an action request from the menu to 
 * 					open a directory containing the files to be 
 * 					loaded into VideoSync
 * 
 * ****************************************************************
 */
package commands.menu;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import models.DataModel;

public class CommandWindowFront extends AbstractAction
{
	private static final long serialVersionUID = 1L;

	
	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- Command Open Variable Declarations
	
	/**
	 * Used to reference and notify the DataModel of any changes
	 */
	private DataModel dm;

	
	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- Command Window Front Methods

	/**
	 * Sets the references to the DataModel
	 * @param dm
	 */
	public void setTargets(DataModel dm)
	{
		this.dm = dm;
	}

	
	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- Command Window Front Action Methods
	
	/**
	 * Called when the user selects the "Bring to Front" option from the Options menu.
	 */
	public void actionPerformed(ActionEvent ae)
	{
		// Notify the data model to bring all the views to the front
		this.dm.presentAllViews();
	}
}
