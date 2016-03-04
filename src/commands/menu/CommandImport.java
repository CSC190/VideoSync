/**
 * ****************************************************************
 * File: 			CommandImport.java
 * Date Created:  	June 12, 2013
 * Programmer:		Dale Reed
 * 
 * Purpose:			To handle an action request from the menu to
 * 					import an additional file that may not have
 * 					been detected automatically.
 * 
 * ****************************************************************
 */

package commands.menu;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;

import objects.ImportFilter;

import views.tabbed_panels.DataWindow;

import models.DataModel;

public class CommandImport extends AbstractAction 
{
	private static final long serialVersionUID = 1L;

	
	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- Command Import Variable Declarations
	
	/**
	 * Used for notifying the data model what action is to be performed.
	 */
	private DataModel dm;
	
	/**
	 * Used for referencing the main view so the file chooser can be displayed.
	 */
	private DataWindow g;
	
	/**
	 * Used to allow the user to select files.
	 */
	private JFileChooser fc  = new JFileChooser();
	
	
	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- Command Import Methods

	/**
	 * Sets the references to the DataModel and DataWindow
	 * @param dm
	 * @param g
	 */
	public void setTargets(DataModel dm, DataWindow g)
	{
		// Set the data model from the passed parameter
		this.dm = dm;
		
		// Set the data window from the passed parameter
		this.g = g;
	}

	
	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- Command Import Action Methods

	/**
	 * Called when the user selects the "Import" option from the File menu.
	 */
	public void actionPerformed(ActionEvent ae) 
	{
		// Enable only files to be selected from the system
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		
		// Apply an import filter from the custom ImportFilter to only choose specific file types.
		fc.addChoosableFileFilter(new ImportFilter());
		
		// If the user has selected a file, then we need to determine what we need to do with it
		if (fc.showOpenDialog(g) == JFileChooser.APPROVE_OPTION)
		{
			// Get the file the user selected.
			File file = fc.getSelectedFile();

			// Variable to store the files extension
			String extension = "";
			
			// Retrieve the last index of a '.' so that we can get the extension
			int extensionStart = file.getName().lastIndexOf('.');
			
			// As long as the location of the period is not 0, we assume it is the correct one for finding the extension
			if (extensionStart > 0)
			{
				// Set the extension based on teh file name
				extension = file.getName().substring(extensionStart + 1);
			}
			
			// If our extension is a c1 file, then we tell the data model to load the c1 file.
			if (extension.equalsIgnoreCase("c1"))
			{
				dm.setC1Data(file);
			}
			// If our extension is a c1 file, then we tell the data model to load the c1 file.
			else if (extension.equalsIgnoreCase("c1max"))
			{
				dm.setC1MaximData(file);
			}
			// If our extension is a 170 file, then we tell the data model to load the 170 file.
			else if (extension.equalsIgnoreCase("vsc") || extension.equalsIgnoreCase("log") || extension.equalsIgnoreCase("dat"))
			{
				dm.set170Data(file);
			}
			// If our extension is a mov file, then we tell the data model to load the mov file.
			// FIXME: This also should include all of the additional movie formats that can be used by VLC
			else if (extension.equalsIgnoreCase("mov"))
			{
				dm.addVideoFile(file);
			}
		}
	}
}
