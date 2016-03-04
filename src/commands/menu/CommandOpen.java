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
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import models.DataModel;
import views.tabbed_panels.DataWindow;

public class CommandOpen extends AbstractAction
{
	private static final long serialVersionUID = 1L;

	
	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- Command Open Variable Declarations
	
	/**
	 * Used to reference and notify the DataModel of any changes
	 */
	private DataModel dm;
	
	/**
	 * Used to reference and notify the DataWindow of any changes.
	 */
	private DataWindow g;
	
	/**
	 * Used for selecting files from the file system.
	 */
	private JFileChooser fc  = new JFileChooser();

	/**
	 * Used for confirming we found a mapping file.
	 */
	private File mpf;
	
	/**
	 * Used for confirming we found a Log 170 data file.
	 */
	private File dat;

	/**
	 * Used for confirming we found a Old VideoSync mapping file.
	 */
	private File vbm;

	/**
	 * Used for confirming we found a C1 data file.
	 */
	private File c1;


	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- Command Open Methods

	/**
	 * Sets the references to the DataModel and Data Window
	 * @param dm
	 * @param g
	 */
	public void setTargets(DataModel dm, DataWindow g)
	{
		// Set the DataModel from the passed parameter
		this.dm = dm;
		// Set the DataWindwo from the passed parameter.
		this.g = g;
	}

	
	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- Command Open Action Methods
	
	/**
	 * Called when the user selects the "Open" option from the File menu.
	 */
	public void actionPerformed(ActionEvent ae)
	{
		// Set the file selection mode of the file chooser to directories only.
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		
		// If the user approved of the directory to load, then proceed with opening the data.
		// Display the open dialog with the DataWindow as the parent.
		if (fc.showOpenDialog(g) == JFileChooser.APPROVE_OPTION)
		{
			// Check to see if the Data Model has already loaded up any data files.
			// If so, remove all data that has been loaded.
			if (dm.isDataLoaded())
				dm.removeAllData();
			
			// Get the file that was selected by the user.
			File file = fc.getSelectedFile();
			
			// Get a list of all the files that were found within the specified directory.
			File files[] = file.listFiles();
			
			// Loop through all of the files located in the directory.
			for (File f : files)
			{
				// Ensure that the file name does not start with a '.', which indicates a hidden file.
				// TODO: This is based of how hidden files are handled with Mac OS X..Check to see if Windows uses
				// a similar nomenclature or if this will suffice.
				if (!f.getName().startsWith("."))
				{	
					// Begin to check and see if the file can be loaded into VideoSync.
					loadFile(f);
				}
			}
			
			// Check to see if a mapping file has been found,
			// If so, use the mapping file instead of the vbm.
			// If no mapping file was found, use the vbm file
			if (mpf != null)
			{
				// Update the data model with the mapping file.
				this.dm.setMappingFile(mpf);
			}
			else if (vbm != null)
			{
				// Update the data model with the VBM file.
				this.dm.setVBMFile(vbm);
			}
		}
		
		// If the data file is null and the c1 file is null,
		// we need to notify the user that there was an error finding the file
		// and allow them to choose a data file from elsewhere.
		if (dat == null && c1 == null)
			displayDataError();
		
		// If the mapping file and vbm file is null, 
		// notify the user that a file wasn't found and ask if they want to
		// select one or have one be automatically created.
		if (mpf == null && vbm == null)
			displayMappingError();
		
		// Note: We don't use an if/else statement here because if both a
		// c1 file and log 170 file were found, we want to load them both 
		// up. Doing an if/else will only load one of them.
		
		// If the dat file is not null, then have the data model load it.
		if (dat != null)
		{
			this.dm.set170Data(dat);
		}
		
		// If the c1 file is not null, then have the data model load it.
		if (c1 != null)
		{
			this.dm.setC1Data(c1);
		}
	}
	
	/**
	 * Display an error message to the user that we were unable to find a 
	 * C1 or Log 170 Data file.
	 */
	private void displayDataError()
	{
		// Set the button options to be displayed to the user.
		Object[] options = {"Yes", "No", "Quit"};
		
		// Allows the while loop to run as long as necessary.
		boolean finished = false;

		while (!finished)
		{
			// Return the result from the Option Dialog, using the Data Window (g) as the parent view.
			int n = JOptionPane.showOptionDialog(g,
												"No data (.dat or .c1) file was found, would you like to select one?",
												null,
												JOptionPane.YES_NO_OPTION,
												JOptionPane.QUESTION_MESSAGE,
												null,
												options,
												options[1]);
			
			// If the user chooses Yes, then allow them to select a new data file 
			if (n == 0)
			{
				// Set the file chooser's selection mode to File's Only.
				fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
				
				// Display the open dialog with the DataWindow as the parent view.
				if (fc.showOpenDialog(g) == JFileChooser.APPROVE_OPTION)
				{
					// Get the file the user selected
					File file = fc.getSelectedFile();
	
					// See if the file loaded is a data file. 
					loadFile(file);
					
					// If either the dat file or c1 file has been found, 
					// we can exit the while loop and continue on loading data.
					if (dat != null || c1 != null)
					{
						finished = true;
					}
				}
			}
			// If the user selects 1 or 2, we can exit the program and quit
			else
			{
				System.exit(1);
			}
		}
	}

	/**
	 * Display an error message to the user that we were unable to find a 
	 * mapping file.
	 */
	private void displayMappingError()
	{
		// Set the button options to be displayed to the user.
		Object[] options = {"Yes", "No"};
		
		// Allows the while loop to run as long as necessary.
		boolean finished = false;
		
		while (!finished)
		{
			// Return the result from the Option Dialog, using the Data Window (g) as the parent view.
			int n = JOptionPane.showOptionDialog(g,
											"No mapping (.mpf or .vbm) file was found, would you like to select one?\nOne will be automatically generated if 'No' is selected",
											null,
											JOptionPane.YES_NO_OPTION,
											JOptionPane.QUESTION_MESSAGE,
											null,
											options,
											options[1]);	
					
			// If the user chooses Yes, then allow them to select a new data file 
			if (n == 0)
			{
				// Set the file chooser's selection mode to File's Only.
				fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
				
				// Display the open dialog with the DataWindow as the parent view.
				if (fc.showOpenDialog(g) == JFileChooser.APPROVE_OPTION)
				{
					// Get the file the user selected
					File file = fc.getSelectedFile();
	
					// See if the file loaded is a data file. 
					loadFile(file);
					
					// If either the mapping file or vbm file has been found, 
					// we can exit the while loop and continue on loading data.
					if (mpf != null || vbm != null)
					{
						finished = true;
					}
				}
			}
			// If the user doesn't choose a file, then create the defaults and continue on.
			else
			{
				finished = true;
				this.dm.set170InputMap();
			}
		}
	}
	
	/**
	 * Set the appropriate files to be loaded based on their extensions.
	 * @param file
	 */
	private void loadFile(File file)
	{
		// If the file is a mapping file, then set the local variable for the mapping file.
		if (file.getName().contains(".mpf"))
		{
			this.mpf = file;
		}
		// If the file is a old videosync mapping file, then set the local variable for the old videosync mapping file.
		else if (file.getName().contains(".vbm"))
		{
			this.vbm = file;
		}
		// If the file is a log 170 file, then set the local variable for the log 170 file.
		else if (file.getName().contains(".dat"))
		{
			this.dat = file;
		}
		// If the file is a c1 file, then set the local variable for the c1 file.
		else if (file.getName().contains(".c1"))
		{
			this.c1 = file;
		}
		// If the file is a mvoie file, then set the local variable for the movie file.
		else if (file.getName().contains(".mov"))
		{
			this.dm.addVideoFile(file);
		}
	}
}
