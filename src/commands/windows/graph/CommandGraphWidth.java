/**
 * ****************************************************************
 * File: 			CommandGraphWidth.java
 * Date Created:  	May 29, 2013
 * Programmer:		Dale Reed
 * 
 * Purpose:			To handle an action request from Data Window to
 * 					change the time width of the graph.
 * 
 * 
 * ****************************************************************
 */
package commands.windows.graph;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JComboBox;

import models.DataModel;

public class CommandGraphWidth extends AbstractAction
{
	private static final long serialVersionUID = 1L;


	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- Command Graph Width Variable Declarations
	
	/**
	 * Used to reference to the DataModel
	 */
	private DataModel dm;
	
	
	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- Command Graph Width Methods

	/**
	 * Sets the references to the DataModel
	 * @param dm
	 */
	public void setTarget(DataModel dm)
	{
		this.dm = dm;
	}


	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- Command Graph Scale Action Methods
	
	/**
	 * Called when the user chooses to change the Graph Scale
	 */	
	public void actionPerformed(ActionEvent e) 
	{
		// Get the combo box source that initiated the action
		JComboBox cb = (JComboBox) e.getSource();
			
		// Ensure that the name of the combo box is "Seconds", if it is, then we can update the graph scale with 
		// the new chosen value of the combo box.
		if (cb.getName().equals("seconds"))
		{
			// Update the graph scale based on the value of the Combo box.
			this.dm.setGraphWidth(Double.parseDouble((String)cb.getSelectedItem()));
		}
	}
}
