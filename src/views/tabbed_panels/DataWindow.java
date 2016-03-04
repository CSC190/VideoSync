/**
 * ****************************************************************
 * File: 			DataWindow.java
 * Date Created:  	June 28, 2013
 * Programmer:		Dale Reed
 * 
 * Purpose:			To contain and display all of the view containers
 * 					pertaining to graphs, and statistics. 
 * 
 * ****************************************************************
 */
package views.tabbed_panels;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;

import models.DataModel;
import views.menus.MainMenuBar;
import views.tabbed_panels.graphs.GraphOptions;
import views.tabbed_panels.graphs.GraphPanel;

import commands.CommandList;

import controllers.EventDetectionPane;
import controllers.PlaybackPane;

public class DataWindow extends JFrame implements WindowListener
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- Data Window Variable Declarations
	
	/**
	 * Used for containing all functions that are not readily available from the keyboard & views
	 */
	private MainMenuBar mainMenuBar;
	
	/**
	 * Used for giving the user options with the graph panes.
	 */
	private GraphOptions panelOptions;
	
	/**
	 * Used for containing all of the graphs in a common area
	 */
	private JPanel graphsPane;

	/**
	 * Used to contain the panelPlayback & panelEvent panels
	 */
	private JPanel bottomPanel;

	/**
	 * Used for containing all of the playback controls
	 */
	private PlaybackPane panelPlayback;
	
	/**
	 * Used for containing the event detection methods
	 */
	private EventDetectionPane panelEvents;
	
	/**
	 * Used for notifying the DataModel of global application events
	 * -- Application Shutdown
	 */
	private DataModel dm;
	
	
	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- Data Window Construction 
	
	/**
	 * Creates the DataWindow with an initial reference to the DataModel so that it may be initialized.
	 * @param dm
	 */
	public DataWindow(DataModel dm)
	{
		// Set the title of the JFrame
		this.setTitle("VideoSync Data");
		
		// Set the size of the JFrame
		this.setSize(new Dimension(745, 750));
		
		// Set the location of the JFrame
//		// FIXME: This is temporary and only to be used while testing.
		this.setLocation(-1440, 0);

		// Set the DataModel as defined from the constructor
		this.dm = dm;
		
		// Adds the components to the JFrame
		addComponents();
	
		// Finalize the JFrame so it will display
		setResizable(false);
		setVisible(true);
 		
		// Have the DataModel perform its initialization
		dm.init();
		
		// Have the JFrame listen to window events
		this.addWindowListener(this);
		
		System.out.println("Panel Options Size: " + this.panelOptions.getSize().toString());
	}

	/**
	 * Adds all of the components to the JFrame so they will be presented upon startup.
	 */
	private void addComponents()
	{
		// Create and set the menu bar for the view
		mainMenuBar = new MainMenuBar();
		this.setJMenuBar(mainMenuBar);
		
		// Create a tabbed pane so we can use one view to display multiple views
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		
		// Add the tabbed pane to the center of the JFrame's content pane.
		this.getContentPane().add(tabbedPane, BorderLayout.CENTER);
		
		// Create the graphs pane with 9 rows and 1 column.
		graphsPane = new JPanel(new GridLayout(9, 1, 0, 0));
		
		// Create all of the views necessary for the graphs
		for (int i = 0; i < 9; i++)
		{
			// If i is 0, set the first panel item to the options
			// otherwise add the graph panels to it
			// also set all of them to observe the datamodel
			if (i != 0)
			{
				// Create a new Graph panel object and have it observe the Data Model
				GraphPanel gp = new GraphPanel();
				dm.addObserver(gp);
				
				// Add the graph panel to the graph pane
				graphsPane.add(gp);
			}
			else
			{
				// Create the Graph Options Panel and have it observe the Data Model
				panelOptions = new GraphOptions(dm);
				dm.addObserver(panelOptions);
				
				// Add the Graph Options Panel to the graphs pane
				graphsPane.add(panelOptions);
			}
		}
		
		// Add the Graphs Pane to the tabbed panel.
		tabbedPane.addTab("Graphs", null, graphsPane, null);
		
		// Add the SVO Panel to the tabbed panel.
		//panelSVO = new SVOPanel();
		//tabbedPane.addTab("SVO", null, panelSVO, null);
		
		// Create the bottom panel 
		bottomPanel = new JPanel();
		
		// Set the size of the bottom panel
		bottomPanel.setPreferredSize(new Dimension(745, 128));
		
		// Add the bottom panel to the 'South' portion of the JFrame's content pane.
		this.getContentPane().add(bottomPanel, BorderLayout.SOUTH);
		bottomPanel.setLayout(null);

		// Create the playback panel and add it to the bottom panel.
		panelPlayback = new PlaybackPane();
		panelPlayback.setBounds(0, 0, 420, 128);
		bottomPanel.add(panelPlayback);
		
		// Create the events panel and add it to the bottom panel.
		panelEvents = new EventDetectionPane();
		panelEvents.setBounds(420, 0, 319, 128);
		bottomPanel.add(panelEvents);
		
		// Have the playback & event panels observe the data model.
		dm.addObserver(panelPlayback);
		dm.addObserver(panelEvents);
	}

	/**
	 * Sets all of the action commands for the menu items.
	 * @param cl
	 */
	public void setCommands(CommandList cl) 
	{
		mainMenuBar.setOpenActionCommand(cl.getCommandOpen());
		mainMenuBar.setSaveActionCommand(cl.getCommandSave());
		mainMenuBar.setImportActionCommand(cl.getCommandImport());
		mainMenuBar.setQuitActionCommand(cl.getCommandQuit());
		mainMenuBar.setInputMappingActionCommand(cl.getCommandInputMapping());
		mainMenuBar.setWindowFrontActionCommand(cl.getCommandWindowFront());
		
		panelOptions.setScaleActionCommand(cl.getCommandGraphScale());
		panelOptions.setWidthActionCommand(cl.getCommandGraphWidth());
		
		panelPlayback.setPlayActionCommand(cl.getCommandPlay());
		panelPlayback.setFrameForwardActionCommand(cl.getCommandFrameForward());
		panelPlayback.setFrameBackwardActionCommand(cl.getCommandFrameReverse());
		
		// Set the key bindings to use some of the commands from the Command List
		setKeyBindings(cl);
	}

	/**
	 * Sets all of the key bindings for the keyboard when the focus is in the playback panel.
	 * @param cl
	 */
	private void setKeyBindings(CommandList cl)
	{
		// Create an input map using the input map from the root pane.
		InputMap im = this.getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		
		// Create two keystrokes for handling the player controls
		KeyStroke leftStroke = KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0);
		KeyStroke rightStroke = KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0);
		
		// Place the two keystrokes into the input map
		im.put(leftStroke, "frameBack");
		im.put(rightStroke, "frameForward");
		
		// Create an action map using the action map from the root pane.
		ActionMap amap = this.getRootPane().getActionMap();
		amap.put("frameBack", cl.getCommandFrameReverse());
		amap.put("frameForward", cl.getCommandFrameForward());
		
		// Have the playback panel listen to events.
		panelPlayback.requestFocus();
	}


	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- Java Event Listeners 

	public void windowClosing(WindowEvent e) 
	{
		Object[] options = {"Yes", "No"};
		
		// Show an option pane and get the result of their input.
		// Because JOptionPane requires a parent component to display the alert, we just create an empty JFrame so it will be displayed. 
		int n = JOptionPane.showOptionDialog(this,
											"Are you sure you wish to exit VideoSync?",
											null,
											JOptionPane.YES_NO_OPTION,
											JOptionPane.QUESTION_MESSAGE,
											null,
											options,
											options[1]);
		// User wants to quit.
		if (n == 0)
		{
			this.dm.performShutdownOperations();
		}
	}

	
	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- Java Event Listeners 
	// -- NOTE: None of the following are currently implemented in this version.
	
	public void windowActivated(WindowEvent e) { }

	public void windowClosed(WindowEvent e) { }
	
	public void windowDeactivated(WindowEvent e) { }
	
	public void windowDeiconified(WindowEvent e) { }
	
	public void windowIconified(WindowEvent e) { }
	
	public void windowOpened(WindowEvent e) { }
}
