/**
 * ****************************************************************
 * File: 			GraphPanel.java
 * Date Created:  	June 7, 2013
 * Programmer:		Dale Reed
 * 
 * Purpose:			To control and render each graph as its own
 * 					entity as to allow each graph to contain its
 * 					own elements and attributes as it is notified
 * 					from the Data Model
 * 
 * ****************************************************************
 */

package views.tabbed_panels.graphs;
import javax.swing.JPanel;

import java.awt.Color;

import javax.swing.border.BevelBorder;
import java.awt.BorderLayout;
import javax.swing.JComboBox;
import javax.swing.JCheckBox;
import javax.swing.DefaultComboBoxModel;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Observable;
import java.util.Observer;
import java.util.Vector;

import javax.swing.border.LineBorder;

import models.DataModelProxy;

import objects.DeviceInputMap;
import objects.graphs.Line;

import java.awt.Dimension;

@SuppressWarnings("unchecked")
public class GraphPanel extends JPanel implements ActionListener, ItemListener, Observer
{
    private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- Graph Panel Variable Declarations

    /**
     * Used for keeping track of where the center line for the graph is.
     */
    private int graphTickCenter = 0;
    
    /**
     * Used to request state data from the DataModelProxy so that the graph can be rendered
     */
    private DataModelProxy dmp;

    /**
     * Used in managing the graph data and keeping it up to date.
     */
	private GraphPane panel_Graph;

	/**
	 * Used in enabling all of the graph options
	 */
	private JCheckBox checkbox_Enabled;

	/**
	 * Used in selecting one of the available devices that have been detected.
	 */
	private JComboBox combo_Devices;

	/**
	 * Used in selecting the appropriate channel based on the device selection.
	 */
	private JComboBox combo_Channel; 
	
	/**
	 * Used in selecting the color that the graph line will be rendered in.
	 * 
	 * TODO: This should be changed to use a color picker instead of hard-coded values.
	 */
	private JComboBox combo_Color;

	/**
	 * Used in keeping track of the current channel that the graph is rendering.
	 */
	private int currChannel;
	
	/**
	 * Used in keeping track of how many pixels there are between state & tick lines
	 */
	private int gap = 0;
	
	/**
	 * Used in keeping track of if the graph is enabled or not
	 */
	private boolean graphEnabled = false;

	/**
	 * Used in keeping track of the color that the graph line is going to be rendered with
	 */
	private Color myColor = Color.BLACK;
		
	/**
	 * Used for maintaining all of the control elements for the Graph
	 */
	private JPanel panel_Control;
	
	/**
	 * Used for setting the combo_Channel to its default when a null model is used.
	 */
	private String[] defaultChannelModel = new String[] {"Choose a Channel"};
	
	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- Graph Panel Construction 

	/**
	 * Creates a graph panel object for rendering the state data for the channel that is selected.
	 */
		public GraphPanel()
	{
		// Set the size, border, background and layout of the entire graph panel
		this.setPreferredSize(new Dimension(722, 59));	
		this.setBorder(new LineBorder(new Color(30, 144, 255)));
		this.setBackground(Color.LIGHT_GRAY);
		this.setLayout(new BorderLayout(0, 0));
		
		// Create the graph's control panel, setting its size, border, and layout (this is null because we are using the absolute layout).
		panel_Control = new JPanel();
		panel_Control.setPreferredSize(new Dimension(222, 57));
		panel_Control.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		panel_Control.setLayout(null);
		// Add the control panel to the primary graph panel
		this.add(panel_Control, BorderLayout.WEST);
		
		// Create the devices combo box and add it to the control panel
		combo_Devices = new JComboBox();
		combo_Devices.setEnabled(false);
		combo_Devices.setModel(new DefaultComboBoxModel(new String[] {"Devices", "C1", "170"}));
		combo_Devices.setBounds(2, 2, 113, 27);
		combo_Devices.addActionListener(this);
		panel_Control.add(combo_Devices);

		// Create the channel combo box and add it to the control panel
		combo_Channel = new JComboBox();
		combo_Channel.setEnabled(false);
		combo_Channel.setBounds(1, 29, 138, 26);
		combo_Channel.addActionListener(this);
		combo_Channel.setModel(new DefaultComboBoxModel(defaultChannelModel));
		panel_Control.add(combo_Channel);
		
		// Create the color combo box and add it to the control panel
		combo_Color = new JComboBox();
		combo_Color.setBounds(112, 2, 109, 26);
		panel_Control.add(combo_Color);
		combo_Color.setEnabled(false);
		combo_Color.addActionListener(this);
		combo_Color.setModel(new DefaultComboBoxModel(new String[] {"Black", "Red", "Green", "Yellow", "Blue", "Magenta", "Pink", "Orange"}));
		
		// Create the enabled check box and add it to the control panel
		checkbox_Enabled = new JCheckBox("Enabled");
		checkbox_Enabled.setBounds(135, 28, 81, 26);
		panel_Control.add(checkbox_Enabled);
		checkbox_Enabled.setEnabled(false);
		checkbox_Enabled.addItemListener(this);
		
		// Create the graph pane and add it to the entire graph panel
		panel_Graph = new GraphPane();
		panel_Graph.setPreferredSize(new Dimension(500, 57));
		panel_Graph.setLayout(new BorderLayout(0, 0));
		this.add(panel_Graph, BorderLayout.CENTER);
	}
	

	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- Java Event Listeners 
	// -- NOTE: Also includes the Observer 'update' method
	
	/**
	 * Invoked when any of the combo boxes' values are changed.
	 */
    @Override
	public void actionPerformed(ActionEvent ae) 
	{
    	// If the source of the ActionEvent is the color combo box, change the color being used with the graph
		if (ae.getSource() == combo_Color)
		{
			// Depending on the index that was selected, change the color appropriately.
			switch(combo_Color.getSelectedIndex())
			{
				case 0:
					myColor = Color.BLACK;
					break;
					
				case 1:
					myColor = Color.RED;
					break;
					
				case 2:
					myColor = Color.GREEN;
					break;
					
				case 3:
					myColor = Color.YELLOW;
					break;
					
				case 4:
					myColor = Color.BLUE;
					break;
					
				case 5:
					myColor = Color.MAGENTA;
					break;
					
				case 6:
					myColor = Color.PINK;
					break;
					
				case 7:
					myColor = Color.ORANGE;
					break;
				
				// This selection should only occur if we had an error with the select boxes
				// or if we did not define a color to be used correctly
				default:
					myColor = Color.BLACK;
					break;
			}
			
			// Update the graph with the new line color to be used.
			panel_Graph.setLineColor(myColor);
		}
		
		// If the source of the ActionEvent is the devices combo box, retrieve the list of 
		// available channels for that device.
		if (ae.getSource() == combo_Devices)
		{
			// Create a temporary combo box element from the ActionEvent source.
			JComboBox combo = (JComboBox) ae.getSource();
			
			// Get the string of the selected item from the combo box.
			String item = (String) combo.getSelectedItem();
			
			// Set the combo box text from the information returned from the DataModelProxy
			setComboBoxText((combo.getSelectedIndex() == 0) ? null : this.dmp.getInputMapForDevice(item));
		}
		
		// If the source of the ActionEvent is the channels combo box, get the channel that was
		// selected and have the graph present that channel data
		if (ae.getSource() == combo_Channel)
		{
			// Create a temporary combo box element from the ActionEven source
			JComboBox combo = (JComboBox) ae.getSource();
			
			// Get the string of the selected item from the combo box.
			String item = (String) combo.getSelectedItem();
		
			// If the selected item does not equal the conditions, then we want to have the graph update
			// its display with the selected channel
			if (!item.equals("Select a Device") && !item.equals("Choose a Channel"))
			{
				// Get the channel number from the item by requesting the Data model to return the appropriate
				// channel number for the selected device.
				setCurrChannel(this.dmp.getChannelFromName((String)combo_Devices.getSelectedItem(), item));						
				
				// Tell the graph to update its contents.
				updateGraph();
			}		
			else
			{
				// Set the panel graph states to null so we don't continue rendering them if we reach this point.
				panel_Graph.setStates(null);
			}
		}
	}
 
    /**
     * Invoked when the checkbox's value is changed.
     */
	public void itemStateChanged(ItemEvent e) 
	{
		// Verify that the source is the enabled check box
		if (e.getSource() == checkbox_Enabled)
		{
			// Ensure that the data model proxy has detected that data was loaded
			// If not we don't do anything about it.
			if (dmp.dataLoaded())
			{
				// If the checkbox has been selected,
				// enable the graph's option boxes so they can
				// be selected by the user.
				// Otherwise disable them all
				if (((JCheckBox)e.getSource()).isSelected())
				{
					graphEnabled = true;
	
					combo_Devices.setEnabled(graphEnabled);
					combo_Channel.setEnabled(graphEnabled); 
					combo_Color.setEnabled(graphEnabled);
				}
				else
				{
					graphEnabled = false;
	
					combo_Devices.setEnabled(graphEnabled);
					combo_Channel.setEnabled(graphEnabled); 
					combo_Color.setEnabled(graphEnabled);
				}
				
				// Update the graph so it can reflect any changes that were made.
				updateGraph();
			}
		}
	}
	
	/**
	 * Invoked when the Data Model sends out a notification that an event changed that requires the 
	 * observers to pay attention to the data coming in
	 */
	@Override
	public void update(Observable arg0, Object arg1) 
	{
		// If the notification argument passed is a string array, then we can update
		// the combo box text
		if (arg1 instanceof String[] || arg1 instanceof Vector)
		{
			setComboBoxText(arg1);
		}
		
		// If the notification argument is a string, then either update the channel list depending on the
		// current combo box selection or have the panel reset its content to defaults.
		if (arg1 instanceof String)
		{
			if (this.graphEnabled && combo_Devices.getSelectedIndex() != 0)
			{
				if (((String)arg1).equals("Input"))
					this.setComboBoxText(this.dmp.getInputMapForDevice((String)this.combo_Devices.getSelectedItem()));
				
				if (((String)arg1).equals("Reset"))
					resetPanel();
			}
		}

		// If the notification argument passes is an instance of the DataModelProxy, 
		// we then set the local variable of the data model proxy and update the graph
		if (arg1 instanceof DataModelProxy)
		{
			dmp = (DataModelProxy)arg1;

			if (dmp.dataLoaded())
				checkbox_Enabled.setEnabled(true);
		
			updateGraph();
		}
	}

	/**
	 * Resets the graph panel when the DataModel notifies the panel that major changes took place and everything needs to reset.
	 */
	private void resetPanel()
	{
		this.graphEnabled = false;
		this.checkbox_Enabled.setSelected(false);
		this.combo_Devices.setSelectedIndex(0);
		this.combo_Color.setSelectedIndex(0);
		this.combo_Channel.setSelectedIndex(0);
		
		updateGraph();
	}
	
	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- Graphing Pane Update Events 
	
	/**
	 * Calculates the vertical tick marks for the graph so they will be rendered at the appropriate locations.
	 */
    private void calculateTickMarks()
    {    	
    	// Get the width and height of the graph panel so we can determine the spacing of the gap for the tick lines
    	int width = panel_Graph.getSize().width;
    	int height = panel_Graph.getSize().height;

    	// Set the gap to be the width / how many lines to show
    	// FIXME: This needs to reflect a changeable number of tick lines per graph and not a fixed amount
    	    	
    	int lWidth = width / 2;
    	int rWidth = width / 2;
    	    	
    	double seconds =  this.dmp.getGraphWindowSeconds();
    	int ticksPer = 4;
    	
    	if (seconds < .5)
    		ticksPer *= 4;
    	else if (seconds > 16)
    		ticksPer = 2;
    	
    	int lGap = (int) (lWidth / (seconds * ticksPer));
    	int rGap = (int) (rWidth / (seconds * ticksPer));
    	
    	Vector<Line>ticks = new Vector<Line>();
    	
    	for (int i = lWidth; i > 0; i-=lGap)
    	{
    		ticks.add(new Line(i, 0, i, height));
    	}
    	
    	for (int i = lWidth; i < width + 1; i+=rGap)
    	{
    		ticks.add(new Line(i, 0, i, height));
    	}
   	
    	
    	
//    	int gap = (int) ((width / 2) / (this.dmp.getGraphWindowSeconds() * 4));
////    	int gap = width / 100;
//    	   
//    	System.out.println("Width: " + width);
//    	// Create an array to hold all of the line objects to be drawn for each tick
//    	Vector<Line>ticks = new Vector<Line>();
//    	
//    	// Create the correct number of tick lines that get to be rendered and displayed
//    	// FIXME: This needs to reflect a changeable number of tick lines per graph and not a fixed amount
////    	for (int i = 0; i < 100; i++)
//  
//    	    	
//    	// Calculate left marks
//    	int i;
//    	for (i = 0; i < ((width / 2) / gap); i++)
//    	{
//    		ticks.add(new Line(i, i * gap, 0, i * gap, height));   		
//    	}
//    	
//    	// Calculate right marks
//    	for (; i < (width / gap) + 1; i++)
//    	{
//    		System.out.println("I: " + i);
//    		// Create a new Line object and add it to the tick array
//    		ticks.add(new Line(i, i * gap, 0, i * gap, height));
//    	}
    	
//    	for (int i = 0; i < (width / gap) + 1; i++)
//    	{
//    		System.out.println("I: " + i);
//    		// Create a new Line object and add it to the tick array
//    		ticks.add(new Line(i, i * gap, 0, i * gap, height));
//    	}
    	
    	// Send the tick array to the graph panel so they can be rendered
    	panel_Graph.setTicks(ticks, (this.dmp.getGraphWindowSeconds() == 64) ? true : false);
    	
    	// Set the center position of the graph ticks to the size of the array / 2
    	graphTickCenter = ticks.size() / 2;
    }
    
    /**
     * Calculates the state lines for the graph to display them in the graph window
     */
    public void calculateStateLines()
    {
    	// Get the device name from the device combo box.
    	String device = (String)combo_Devices.getSelectedItem();
    	
    	// If the device name equals Devices, then we nullify out the graph's state data
    	// Otherwise get the state data from the data model proxy and pass it to the graph to be rendered
    	if (device.equals("Devices"))
    	{
    		panel_Graph.setStates(null);
    	}
    	else
    	{
    		// Request the state data from the data model proxy and send it too the graph panel
        	Vector<Line> states = dmp.getDataForChannel(device, graphTickCenter, currChannel, panel_Graph.getSize().width, 10, gap, 47);
        	
        	// Send the state data off to the graph for rendering
        	panel_Graph.setStates(states);   		
    	}
    }
    
    /**
     * Updates the graph by rendering the tick & state lines
     */
    public void updateGraph()
	{
    	// Calculate the tick marks to be rendered on the graph
		calculateTickMarks();
		
		// If the graph is enabled, go ahead and calculate the state lines
		// Otherwise nullify the state lines in the graph panel so they are not rendered
		if (this.graphEnabled)
			calculateStateLines();
		else
			panel_Graph.setStates(null);
	}


	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- Graph Panel Getter's & Setters
    
    /**
	 * Sets the combo box text from the object data that was passed to the function
	 * 
	 * @param data
	 */
	public void setComboBoxText(Object data)
	{
		// If the data passed is an instance of a String array, we can set the 
		// combo devices data model from the object that was passed
		// Otherwise if its not a String array, we are instead setting the channel data 
		if (data instanceof String[])
		{
			// Set the combo box data to the string array that was sent
			combo_Devices.setModel(new DefaultComboBoxModel((String[]) data));
		}
		else if (data instanceof Vector)
		{
			if (data != null)
			{
				int selectedIndex = combo_Channel.getSelectedIndex();
				
				// Get the size of the vector array that was passed.
				int dataSize = ((Vector<DeviceInputMap>)data).size();
				
				// Create a string array to the length of the data sent + 1
				// This allows us to place a default string at the first position
				String[] strings = new String[dataSize + 1];
				
				// Set the first element to our default string
				strings[0] = "Choose a Channel";

				// Create string a string array for each data element and place it in the string array
				for (int i = 1; i <= dataSize; i++)
				{
					strings[i] = ((Vector<DeviceInputMap>)data).elementAt(i - 1).getChannelName();
				}
				
				// Set the combo box data to the string array we just generated
				combo_Channel.setModel(new DefaultComboBoxModel(strings));
				
				if (selectedIndex != 0)
					combo_Channel.setSelectedIndex(selectedIndex);
			}
		}
		else if (data == null)
		{
			combo_Channel.setModel(new DefaultComboBoxModel(this.defaultChannelModel));
			panel_Graph.setStates(null);
		}
// 	OLD COMBO BOX SETTING METHOD - REPLACED BY else if ON 7/30/2013 - DR
//		else
//		{		
//			// Ensure that the data is not null, if it is we default the combo box for 
//			// the channel data back to its default
//			if (data != null)
//			{
//				// Convert the object data from an object to an integer array
//				int[] d = (int[]) data;
//				
//				// Create a string array to the length of the data sent + 1
//				// This allows us to place a default string at the first position
//				String[] strings = new String[d.length + 1];
//				
//				// Set the first element to our default string
//				strings[0] = "Choose a Channel";
//				
//				// Create string a string array for each data element and place it in the string arary
//				for (int i = 1; i <= d.length; i++)
//				{
//					strings[i] = String.format("Channel %d", d[i - 1]);
//				}
//				
//				// Set the combo box data to the string array we just generated
//				combo_Channel.setModel(new DefaultComboBoxModel(strings));
//			}
//			else
//			{
//				combo_Channel.setModel(new DefaultComboBoxModel(new String[]{"Select a Device"}));
//			}
//		}
	}
	
	/**
	 * Set the panel's current channel
	 * 
	 * @param currChannel
	 */
	public void setCurrChannel(int currChannel) 
	{
		this.currChannel = currChannel;
	}	
}
