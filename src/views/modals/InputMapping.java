/**
 * ****************************************************************
 * File: 			InputMapping.java
 * Date Created:  	July 24, 2013
 * Programmer:		Dale Reed
 * 
 * Purpose:			To allow a user to change the default name values
 * 					for the channels that they are currently using.
 * 					It will automatically save the mapping file if 
 * 					changes are made and the view exits.
 * 
 * ****************************************************************
 */
package views.modals;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Observable;
import java.util.Observer;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.SoftBevelBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import models.DataModelProxy;
import objects.DeviceInputMap;

// FIXME: This needs to be changed to detect what kind of detector station it is (Freeway, Ramp, Intersection) and display the appropriate values and fields

@SuppressWarnings({"rawtypes"})
public class InputMapping extends JFrame implements ActionListener, ItemListener, ListSelectionListener, KeyListener, Observer, MouseListener
{
	private static final long serialVersionUID = -5651018822338534325L;

	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- Video Player Variable Declarations
	
	/**
	 * Constant string for the suffix to the mapping file. It is prefaced by the device name.
	 */
	private final String MAPPING_NAME = "mapping.mpf";

	/**
	 * Constant string array containing all of the column names that will be displayed in the table.
	 */
	private final String[] columnNames = {"Bit", "Lane #", "Name", "Type", "Direction", "Detector Type"};

	/**
	 * Used for storing the table column data generated from the column names.
	 */
	private Vector<String> tableColumnData = new Vector<String>();
	
	/**
	 * Used for presenting all of the table rows with the data
	 */
	private Vector<Vector> tableRowData;
	
	/**
	 * Used for storing the resulting input map data
	 */
	private Vector<DeviceInputMap> inputMapData;

	/**
	 * Input File to read/write the input data from/to.
	 */
	private File inputFile;
	
	/**
	 * Reference to notify the Data Model that there are changes and to have the views immediately reflect those changes.
	 */
	private DataModelProxy dmp;
	
	/**
	 * Used in indicating that we want to write the updates to a file.
	 */
	private boolean writeUpdatesToFile = false;
	
	/**
	 * Used for indicating that changes were made to an input element.
	 */
	private boolean changesMade = false;
	
	/**
	 * Used for indicating that changes are currently being made.
	 */
	private boolean isEditing = false;
	
	/**
	 * Used for keeping track of which array element is being updated.
	 */
	private int index;

	/**
	 * Used for presenting all of the available channels to the user.
	 */
	private JTable table_Channels;

	/**
	 * Used for containing the JTable and allowing it to scroll if there is enough data to warrant it. 
	 */
	private JScrollPane scrollPane;
	
	/**
	 * Used for indicating what bit the data represents. This number is not changeable by the user.
	 * as it is the reference to all events within the program.
	 */
	private JLabel label_BitNumber;

	/**
	 * Used for allowing the user to indicate which lane number the detector is in
	 */
	private JTextField textfield_LaneNumber;

	/**
	 * Used for allowing the user to give the detector a more specific name.
	 */
	private JTextField textfield_ChannelName;

	/**
	 * Used for selecting what type of detector is being used.
	 */
	private JComboBox combo_DetectorType;

	/**
	 * Used for selecting what kind of detector is being used.
	 */
	private JComboBox combo_ChannelType;

	/**
	 * Used for indicating the direction the lane is heading (N, S, E, W)
	 */
	private JComboBox combo_Direction;
	
	/**
	 * Used for selecting which device is going to be displayed in the table.
	 */
	private JComboBox combo_DeviceSelect;
	
	/**
	 * Used to toggle the data in the edit pane so they can be enabled or disabled.
	 */
	private JButton button_EditUpdate;
	
	/**
	 * Used to apply all changes made.
	 */
//	private JButton button_ApplyChanges;
	
	/**
	 * Used to cancel & close the Input Mapping window.
	 */
	private JButton button_Cancel;


	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- Input Mapping Construction
	
	public InputMapping()
	{
		// Set the main panel attributes that will contain all of the GUI elements. Setting the layout to 'null' allows positions to be absolute
		JPanel mainPanel = new JPanel();
		getContentPane().add(mainPanel, BorderLayout.CENTER);
		mainPanel.setLayout(null);
		
		// Create the content area to store all of the editing attributes.
		JPanel editPanel = new JPanel();
		editPanel.setBorder(new SoftBevelBorder(BevelBorder.LOWERED, null, null, null, null));
		editPanel.setBounds(535, 6, 180, 510);
		mainPanel.add(editPanel);
		editPanel.setLayout(null);
		
		// Create a static label as a title for the edit panel.
		JLabel label2 = new JLabel("Edit Channel Attributes");
		label2.setBounds(16, 9, 146, 16);
		editPanel.add(label2);
		
		// Create a static separator below the panel's title.
		JSeparator separator = new JSeparator();
		separator.setForeground(Color.BLACK);
		separator.setBounds(6, 28, 167, 12);
		editPanel.add(separator);

		// Create a static label for the channel number
		JLabel label3 = new JLabel("Channel Number");
		label3.setHorizontalAlignment(SwingConstants.CENTER);
		label3.setBounds(6, 52, 167, 16);
		editPanel.add(label3);
		
		// Create the label to hold the bit number for the selected channel. This element is not changeable by 
		// the user as it is the primary identifier that links all the data objects together.
		label_BitNumber = new JLabel("");
		label_BitNumber.setHorizontalAlignment(SwingConstants.CENTER);
		label_BitNumber.setBounds(6, 76, 165, 16);
		editPanel.add(label_BitNumber);
		
		// Create a static separator below the channel number.
		JSeparator separator_1 = new JSeparator();
		separator_1.setBounds(6, 97, 167, 12);
		editPanel.add(separator_1);

		// Create a static label for the lane number.
		JLabel label4 = new JLabel("Lane Number");
		label4.setHorizontalAlignment(SwingConstants.CENTER);
		label4.setBounds(6, 110, 167, 16);
		editPanel.add(label4);
		
		// Create a text field that will accept input from the user for the lane number.
		// Initializes its enabled status to false as there is no data immediately contained within it
		// Sets its max columns to 10
		// Adds a key listener for input.
		textfield_LaneNumber = new JTextField();
		textfield_LaneNumber.setEnabled(false);
		textfield_LaneNumber.setBounds(6, 130, 167, 28);
		textfield_LaneNumber.setColumns(10);
		textfield_LaneNumber.addKeyListener(this);
		editPanel.add(textfield_LaneNumber);
		
		// Create a static separator below the lane number.
		JSeparator separator_2 = new JSeparator();
		separator_2.setBounds(6, 166, 167, 12);
		editPanel.add(separator_2);

		// Create a static label for the channel name.
		JLabel label5 = new JLabel("Channel Name");
		label5.setHorizontalAlignment(SwingConstants.CENTER);
		label5.setBounds(6, 179, 167, 16);
		editPanel.add(label5);
		
		// Create a text field that will accept input from the user for the channel name.
		// Initializes its enabled status to false as there is no data immediately contained within it
		// Sets its max columns to 10
		// Adds a key listener for input.		
		textfield_ChannelName = new JTextField();
		textfield_ChannelName.setEnabled(false);
		textfield_ChannelName.setColumns(10);
		textfield_ChannelName.setBounds(6, 199, 167, 28);
		textfield_ChannelName.addKeyListener(this);
		editPanel.add(textfield_ChannelName);
		
		// Create a static separator below the channel name
		JSeparator separator_3 = new JSeparator();
		separator_3.setBounds(6, 239, 167, 12);
		editPanel.add(separator_3);
		
		// Create a static label for the detector type
		JLabel label6 = new JLabel("Detector Type");
		label6.setHorizontalAlignment(SwingConstants.CENTER);
		label6.setBounds(6, 252, 167, 16);
		editPanel.add(label6);
		
		// Create the combo box with the available detector types.
		combo_DetectorType = new JComboBox();
		combo_DetectorType.setEnabled(false);
		combo_DetectorType.setModel(new DefaultComboBoxModel(new String[] {"Select Type", "Radar", "Loop"}));
		combo_DetectorType.setBounds(6, 274, 167, 27);
		editPanel.add(combo_DetectorType);

		// Create a static separator below the 
		JSeparator separator_4 = new JSeparator();
		separator_4.setBounds(6, 312, 167, 12);
		editPanel.add(separator_4);		
		
		// Create a static label for the channel type
		JLabel label7 = new JLabel("Channel Type");
		label7.setHorizontalAlignment(SwingConstants.CENTER);
		label7.setBounds(6, 325, 167, 16);
		editPanel.add(label7);

		// Create the combo box with the available channel types
		combo_ChannelType = new JComboBox();
		combo_ChannelType.setEnabled(false);
		combo_ChannelType.setModel(new DefaultComboBoxModel(new String[] {"Select Type", "Intersection", "Freeway", "Ramp"}));
		combo_ChannelType.setBounds(6, 353, 167, 27);
		editPanel.add(combo_ChannelType);
		
		// Create a static separator below the 
		JSeparator separator_5 = new JSeparator();
		separator_5.setBounds(6, 392, 167, 12);
		editPanel.add(separator_5);
		
		// Create a static label for the channel direction
		JLabel label8 = new JLabel("Channel Direction");
		label8.setHorizontalAlignment(SwingConstants.CENTER);
		label8.setBounds(6, 405, 167, 16);
		editPanel.add(label8);
		
		// Create the combo box with the available directions.
		combo_Direction = new JComboBox();
		combo_Direction.setEnabled(false);
		combo_Direction.setModel(new DefaultComboBoxModel(new String[] {"Choose Direction", "Northbound", "Southbound", "Eastbound", "Westbound"}));
		combo_Direction.setBounds(6, 427, 167, 27);
		editPanel.add(combo_Direction);
		
		// Create the Edit/Update button.
		button_EditUpdate = new JButton("Edit");
		button_EditUpdate.setEnabled(false);
		button_EditUpdate.setBounds(30, 475, 120, 29);
		button_EditUpdate.addActionListener(this);
		editPanel.add(button_EditUpdate);
		
	
		// Create the content area to store the table layout	
		JPanel tablePanel = new JPanel();
		tablePanel.setBounds(6, 6, 517, 510);
		mainPanel.add(tablePanel);
		tablePanel.setLayout(null);
		
		
		// Create a static label for the available channels
		JLabel label1 = new JLabel("Available Channels");
		label1.setHorizontalAlignment(SwingConstants.CENTER);
		label1.setBounds(157, 6, 354, 22);
		tablePanel.add(label1);
		
		// Create the scroll pane to contain the table and allow it to scroll
		scrollPane = new JScrollPane();
		scrollPane.setBounds(6, 34, 505, 470);
		tablePanel.add(scrollPane);
		
		// Create the table and add it to the scroll pane's viewport.
		table_Channels = new JTable();
		scrollPane.setViewportView(table_Channels);
		
		// Create the device selection combo box and set its default text
		combo_DeviceSelect = new JComboBox();
		combo_DeviceSelect.setModel(new DefaultComboBoxModel(new String[] {"Select Device"}));
		combo_DeviceSelect.setBounds(6, 5, 139, 27);
		combo_DeviceSelect.addItemListener(this);
		tablePanel.add(combo_DeviceSelect);
		
		// Create the "Apply Changes" button
//		button_ApplyChanges = new JButton("Apply Changes");
//		button_ApplyChanges.addActionListener(this);
//		button_ApplyChanges.setBounds(260, 523, 128, 29);
//		mainPanel.add(button_ApplyChanges);

//		button_Cancel = new JButton("Cancel");
//		button_Cancel.setBounds(394, 523, 117, 29);
//		button_Cancel.addActionListener(this);
//		mainPanel.add(button_Cancel);

		// Create the close button so the user can cloes the window.
		button_Cancel = new JButton("Close");
		button_Cancel.setBounds(306, 523, 117, 29);
		button_Cancel.addActionListener(this);
		mainPanel.add(button_Cancel);

		// Set the size of the Input Mapping mane.
		this.setSize(722, 580);
	
		// Initialize the data that will hold all of the table column names
		tableColumnData = new Vector<String>();
		
		// Add the column names to the table column data array.
		for (int i = 0; i < columnNames.length; i++)
		{
			tableColumnData.add(columnNames[i]);
		}
	}

	
	public void displayPanel(boolean visible)
	{
		if (visible)
		{
//			if (inputFile == null)
//			{
//				if (dmp.dataLoaded())
//				{
//
////					// TODO: Check to see if the DMP has a mapping file already loaded, if not we'll create a new one.
////					inputFile = new File(this.dmp.getCurrentDirectory() + File.separator + (String)combo_DeviceSelect.getSelectedItem() + "_mapping.mpf");
//				}
//			}
		}
		else
		{
			if (writeUpdatesToFile)
				writeMappingToFile();

			this.button_EditUpdate.setEnabled(false);
			this.combo_DeviceSelect.setSelectedIndex(0);
			this.inputMapData = null;
			refreshTable();
			resetFieldsToDefaults();
		}
		
		this.setVisible(visible);
	}
	
	public void resetFieldsToDefaults()
	{
		label_BitNumber.setText(null);
		textfield_LaneNumber.setText(null);
		textfield_ChannelName.setText(null);
		combo_ChannelType.setSelectedIndex(0);
		combo_Direction.setSelectedIndex(0);
		combo_DetectorType.setSelectedIndex(0);
	}

	@Override
	public void valueChanged(ListSelectionEvent e) 
	{
		if (e.getSource() instanceof ListSelectionModel)
		{
			ListSelectionModel lsm = (ListSelectionModel)e.getSource();
	        if (lsm.isSelectionEmpty()) {
	            System.out.println("No rows are selected.");
	        } 
	        else 
	        {
	        }
		}
		else
		{
			System.out.println("Source: " + e.getSource());
		}
	}

	public void setUIElementsEnabled(boolean enabled)
	{
		textfield_LaneNumber.setEnabled(enabled);
		textfield_ChannelName.setEnabled(enabled);
		combo_DetectorType.setEnabled(enabled);
		combo_Direction.setEnabled(enabled);
		combo_ChannelType.setEnabled(enabled);
		
		if (enabled)
		{
			button_EditUpdate.setText("Update");
			combo_ChannelType.addItemListener(this);
			combo_Direction.addItemListener(this);
			combo_DetectorType.addItemListener(this);
		}
		else
		{
			button_EditUpdate.setText("Edit");
			combo_ChannelType.removeItemListener(this);
			combo_Direction.removeItemListener(this);
			combo_DetectorType.removeItemListener(this);
		}
	}
	
	public void performUpdateForData()
	{
		if (button_EditUpdate.getText().equals("Edit"))
		{
			System.out.println("Enabling data to be edited");
		}
		else
		{
			if (changesMade)
			{
				System.out.println("Changes were made to data.");
				writeUpdatesToFile = true;
				changesMade = false;				
				
				DeviceInputMap dim = inputMapData.elementAt(this.index);
				dim.setLaneNumber(Integer.parseInt((this.textfield_LaneNumber.getText() == null || this.textfield_LaneNumber.getText().equals("")) ? "0" : this.textfield_LaneNumber.getText()));
				dim.setChannelName(this.textfield_ChannelName.getText());
				dim.setDetectorType((String)this.combo_DetectorType.getSelectedItem());
				dim.setDirection((String)this.combo_Direction.getSelectedItem());
				dim.setChannelType((String)this.combo_ChannelType.getSelectedItem());
				
				refreshTable();
				
				this.dmp.updateInputMapForDevice((String)combo_DeviceSelect.getSelectedItem(), inputMapData);
			}
		}
	}
	
	public void writeMappingToFile()
	{
		if (inputFile.exists())
			inputFile.delete();
		
		try 
		{
			FileOutputStream fos = new FileOutputStream(inputFile);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			
			for (DeviceInputMap dim : this.inputMapData)
			{
				oos.writeObject(dim);
			}
			
			oos.close();
		} 
		catch (FileNotFoundException e) 
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
	
	public void setDataModelProxy(DataModelProxy dmp)
	{
		this.dmp = dmp;
	}
		
	@Override
	public void update(Observable arg0, Object arg1) 
	{
		if (arg1 instanceof DataModelProxy)
		{
			this.dmp = (DataModelProxy)arg1;
		}
		
		if (arg1 instanceof String[])
		{
			// Set the combo box data to the string array that was sent
			combo_DeviceSelect.setModel(new DefaultComboBoxModel((String[]) arg1));
		}
	}
	
	public void refreshTable()
	{
		
		if (this.tableRowData != null)
			this.tableRowData.clear();
		else
			this.tableRowData = new Vector<Vector>();
		
		if (this.inputMapData != null)
		{
			for (int i = 0; i < this.inputMapData.size(); i++)
			{
				DeviceInputMap dim = inputMapData.elementAt(i);
				
				Vector<String> rowData = new Vector<String>();
				
				rowData.add(Integer.toString(dim.getBitNumber()));
				rowData.add(Integer.toString(dim.getLaneNumber()));
				rowData.add(dim.getChannelName());
				rowData.add(dim.getDetectorType());
				rowData.add(dim.getDirection());
				rowData.add(dim.getChannelType());
				
				this.tableRowData.add(rowData);
			}
		}
		
		this.table_Channels = new JTable(this.tableRowData, this.tableColumnData);
		
		
		this.table_Channels.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		this.table_Channels.addMouseListener(this);
		
		JScrollPane sp = new JScrollPane(this.table_Channels);
		scrollPane.setViewport(sp.getViewport());
	}
		
	
	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- Java Event Listeners 
	
	public void actionPerformed(ActionEvent e) 
	{
		if (e.getSource() == button_Cancel)
		{
			this.displayPanel(false);
		}
		else if (e.getSource() == button_EditUpdate)
		{
			if (!isEditing)
			{
				isEditing = true;
				setUIElementsEnabled(isEditing);
			}
			else
			{
				performUpdateForData();
				isEditing = false;
				setUIElementsEnabled(isEditing);
			}
		}
	}

	public void mouseClicked(MouseEvent e) {
		
        this.index = ((JTable)e.getSource()).getSelectedRow();
        this.button_EditUpdate.setEnabled(true);
        
    	int n = 0;
    	
    	if (changesMade)
    	{
    		System.err.println("Changes were made to a field...need to confirm cancel before moving on");
    		
    		Object[] options = {"Yes", "No"};
    		
		    n = JOptionPane.showOptionDialog(this, 
		    		"Unsaved changes were made to the channel. Are you sure you want to continue without saving?",
		    		null,
		    		JOptionPane.YES_NO_OPTION,
		    		JOptionPane.QUESTION_MESSAGE,
		    		null,
		    		options,
		    		options[1]);
		    
		    
    	}
    	
    	if (n == 0)
    	{
    		if (changesMade)
    			changesMade = false;
    		
        	if (isEditing)
        		setUIElementsEnabled(false);
        	
            setEditPaneContents();
    	}
    	else if (n == 1)
    	{
    	}
	}
	
	public void setEditPaneContents()
	{        
        DeviceInputMap dim = inputMapData.elementAt(this.index);
        
        System.out.println("------------------------------------------------------------------------");
        System.out.println(" -- setEditPaneContents()");
        System.out.println(" -- DeviceInputMap: " + dim);
        System.out.println("------------------------------------------------------------------------");
        
        label_BitNumber.setText(Integer.toString(dim.getBitNumber()));
        
        if (dim.getLaneNumber() != 0)
        	textfield_LaneNumber.setText(Integer.toString(dim.getLaneNumber()));

        if (dim.getChannelName() != null)
        	textfield_ChannelName.setText(dim.getChannelName());
        
        if (dim.getDetectorType() != null)
        	combo_DetectorType.setSelectedItem(dim.getDetectorType());
        	
        if (dim.getDirection() != null)
        	combo_Direction.setSelectedItem(dim.getDirection());
                
        if (dim.getChannelType() != null)
        	combo_ChannelType.setSelectedItem(dim.getChannelType());
	}

	/**
	 * Used for detecting and updating UI elements if there was a change to one of the combo boxes.
	 */
	public void itemStateChanged(ItemEvent e) 
	{		
		// If the source is the device selection combo box, we want to populate the view with the correct channel information.
		if (e.getSource() == combo_DeviceSelect)
		{	
			// Get the input mapping file from the current directory.
			String imf = this.dmp.getCurrentDirectory() + File.separator;
			
			if (((String)combo_DeviceSelect.getSelectedItem()).equals("170"))
			{
				if (inputMapData == null || inputMapData != this.dmp.get170InputMap())
				{
					this.inputMapData = this.dmp.get170InputMap();
				}						
			}
			else if (((String)combo_DeviceSelect.getSelectedItem()).equals("C1"))
			{
				if (inputMapData == null || inputMapData != this.dmp.getC1InputMap())
				{
					this.inputMapData = this.dmp.getC1InputMap();
				}
			}
			
			inputFile = new File(imf + (String)combo_DeviceSelect.getSelectedItem() + "_" + this.MAPPING_NAME);

			refreshTable();

		}
		else
		{
			changesMade = true;			
		}
	}

	/**
	 * Used for detecting if a key is typed from any of the TextFields
	 * 
	 * @param event
	 */
	public void keyTyped(KeyEvent event) 
	{
		// Update the changesMade value to true.
		changesMade = true;
	}
	
		
	/**
	 * The following functions pertain to the various implementations that are currently not being used by the class.
	 */
	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- Java Event Listener methods 
	// -- NOTE: None of the following are currently implemented in this version
	
	public void keyPressed(KeyEvent e) {}

	public void keyReleased(KeyEvent e) {}

	public void mouseEntered(MouseEvent arg0) {	}

	public void mouseExited(MouseEvent arg0) {}

	public void mousePressed(MouseEvent arg0) {}

	public void mouseReleased(MouseEvent arg0) {}
}
