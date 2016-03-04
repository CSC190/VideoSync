/**
 * ****************************************************************
 * File: 			CommandImportMapping.java
 * Date Created:  	July 23, 2013
 * Programmer:		Dale Reed
 * 
 * Purpose:			To handle an action request from the menu to
 * 					import mapping file.
 * 
 * ****************************************************************
 */
package commands.menu;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import views.modals.InputMapping;

import models.DataModelProxy;

public class CommandInputMapping extends AbstractAction
{
	private static final long serialVersionUID = 589846253157734111L;

	
	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- Command Import Mapping Variable Declarations
	
	/**
	 * Primarily used for keeping track of the Input Mappings visibility
	 */
	private InputMapping im;
			
	
	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- Command Import Mapping Constructor
	
	public CommandInputMapping()
	{
		// Construct the InputMapping so we can use it
		im = new InputMapping();
	}
	
	
	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- Command Import Mapping Methods
	
	/**
	 * Sets the InputMapping DataModelProxy reference.
	 * @param dmp
	 */
	public void setTargets(DataModelProxy dmp)
	{
		// Set the data model proxy for the Input Mapping
		im.setDataModelProxy(dmp);
	}

	
	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- Command Import Mapping Getters & Setters
	
	/**
	 * Return the InputMapping object
	 * @return
	 */
	public InputMapping getInputMapping()
	{
		return this.im;
	}

	
	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- Command Import Mapping Action Methods
	
	/**
	 * Called when the user selects the "Input Mapping" option from the Options menu.
	 */
	public void actionPerformed(ActionEvent arg0) 
	{
		// Check to see if the Input Mapping is visible. If not, display it.
		if (!im.isVisible())
			im.displayPanel(true);
	}
}
