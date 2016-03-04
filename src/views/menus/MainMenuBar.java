/**
 * ****************************************************************
 * File: 			MainMenuBar.java
 * Date Created:  	June 5, 2013
 * Programmer:		Dale Reed
 * 
 * Purpose:			To contain and control all actions that are 
 * 					used from the Main Menu
 * 
 * ****************************************************************
 */
package views.menus;

import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import java.awt.Color;

import javax.swing.JSeparator;

import commands.menu.CommandImport;
import commands.menu.CommandInputMapping;
import commands.menu.CommandOpen;
import commands.menu.CommandQuit;
import commands.menu.CommandSave;
import commands.menu.CommandWindowFront;

public class MainMenuBar extends JMenuBar 
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- Main Menu Bar Variable Declarations

	/**
	 * Used for containing lists of actions that can be performed.
	 */
	private JMenu fileMenu;
	private JMenu toolsMenu;
	private JMenu windowMenu;
	
	/**
	 * 'File' menu items
	 */
	private JMenuItem fileMenuOpen;
	private JMenuItem fileMenuQuit;
	private JMenuItem fileMenuSave;
	private JMenuItem fileMenuImport;
	
	/**
	 * 'Tools' menu items
	 */
	private JMenuItem toolMenuMapping;
	
	/**
	 * 'Window' menu items
	 */
	private JMenuItem windowMenuFront;
	
	
	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- Main Menu Bar Construction 

	/**
	 * Creates a new menu bar containing all of the menu items needed for VideoSync
	 */
	public MainMenuBar() 
	{	
		// Create the parent 'File' menu container and add it to the MenuBar
		fileMenu = new JMenu("File");
		fileMenu.setBackground(Color.LIGHT_GRAY);
		this.add(fileMenu);
		
		// Create the 'Open' menu item and add it to the 'File' menu
		fileMenuOpen = new JMenuItem("Open Directory");
		fileMenu.add(fileMenuOpen);
		
		// Create the 'Save' menu item and add it to the 'File' menu
		fileMenuSave = new JMenuItem("Save Directory");
//		fileMenu.add(fileMenuSave);
		
		// Add a separator to the 'File' menu
		fileMenu.add(new JSeparator());
				
		// Create the 'Import' menu item and add it to the 'File' menu
		fileMenuImport = new JMenuItem("Import File");
		fileMenu.add(fileMenuImport);
		
		// Add a separator to the 'File' menu
		fileMenu.add(new JSeparator());

		// Create the 'Quit' menu item and add it to the 'File' menu
		fileMenuQuit = new JMenuItem("Quit");
		fileMenu.add(fileMenuQuit);
		
		// Create the parent 'Tools' menu container and add it to the MenuBar
		toolsMenu = new JMenu("Tools");
		toolsMenu.setBackground(Color.LIGHT_GRAY);
		add(toolsMenu);
		
		// Create the 'Input mapping' menu item and add it to the 'Tools' menu
		toolMenuMapping = new JMenuItem("Input Mapping");
		toolsMenu.add(toolMenuMapping);
		
		windowMenu = new JMenu("Window");
		
		windowMenuFront = new JMenuItem("Bring All To Front");		
		windowMenu.add(windowMenuFront);
		
		this.add(windowMenu);
	}
	
	
	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- Main Menu Bar Action Commands 
	
	/**
	 * Sets the Open Action Command to the 'Open' menu item.
	 * @param co
	 */
	public void setOpenActionCommand(CommandOpen co)
	{
		fileMenuOpen.setAction(co);
		fileMenuOpen.setText("Open Directory");	
	}
	
	/**
	 * Sets the Save Action Command to the 'Save' menu item.
	 * @param cs
	 */
	public void setSaveActionCommand(CommandSave cs)
	{
		fileMenuSave.setAction(cs);
		fileMenuSave.setText("Save Directory");	
	}
	
	/**
	 * Sets the Import Action Command to the 'Import' menu item.
	 * @param ci
	 */
	public void setImportActionCommand(CommandImport ci)
	{
		fileMenuImport.setAction(ci);
		fileMenuImport.setText("Import...");		
	}
	
	/**
	 * Sets the Quit Action Command to the 'Quit' menu item.
	 * @param cq
	 */
	public void setQuitActionCommand(CommandQuit cq)
	{
		fileMenuQuit.setAction(cq);
		fileMenuQuit.setText("Quit");
	}

	/**
	 * Sets the Input Mapping Action Command to the 'Input Mapping' menu item.
	 * @param cim
	 */
	public void setInputMappingActionCommand(CommandInputMapping cim)
	{
		toolMenuMapping.setAction(cim);
		toolMenuMapping.setText("Input Mapping");
	}

	public void setWindowFrontActionCommand(CommandWindowFront cwf)
	{
		windowMenuFront.setAction(cwf);
		windowMenuFront.setText("Bring All To Front");
	}
}