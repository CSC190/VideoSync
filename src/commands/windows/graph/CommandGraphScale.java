/**
 * ****************************************************************
 * File: 			CommandGraphScale.java
 * Date Created:  	June 4, 2013
 * Programmer:		Dale Reed
 * 
 * Purpose:			To handle an action request from Data Window to
 * 					change the scale of the graph.
 * 
 * 
 * ****************************************************************
 */
package commands.windows.graph;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JComboBox;

import models.DataModel;

public class CommandGraphScale extends AbstractAction
{
	private static final long serialVersionUID = 1L;


	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- Command Graph Scale Variable Declarations
	
	/**
	 * Used to reference to the DataModel
	 */
	private DataModel dm;
	
	
	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- Command Graph Scale Methods

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
	// -- Command Graph Scale Action Methods
	
	/**
	 * Called when the user chooses to change the Graph Scale
	 */
	public void actionPerformed(ActionEvent e) 
	{
		// Get the combo box source that initiated the action
		JComboBox cb = (JComboBox) e.getSource();
			
		// Ensure that the name of the combobox is "Scale", if it is, then we can update the graph scale with 
		// the new chosen value of the combo box.
		if (cb.getName().equals("scale"))
		{
			// Update the graph scale based on the value of the Combo box.
			this.dm.setGraphScale(Integer.parseInt((String)cb.getSelectedItem()));
		}
	}
}
