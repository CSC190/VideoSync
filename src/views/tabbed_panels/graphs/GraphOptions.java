/**
 * ****************************************************************
 * File: 			GraphOptions.java
 * Date Created:  	June 28, 2013
 * Programmer:		Dale Reed
 * 
 * Purpose:			To control all aspects of the graphing options
 * 					pertaining to time width and graph offset
 * 
 * ****************************************************************
 */package views.tabbed_panels.graphs;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Observable;
import java.util.Observer;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;

import models.DataModel;
import models.DataModelProxy;

import commands.windows.graph.CommandGraphScale;
import commands.windows.graph.CommandGraphWidth;
import java.awt.Font;


// FIXME: Need to incorporate ActionListener for creating the resolution/scale values
public class GraphOptions extends JPanel implements Observer, KeyListener, ActionListener
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- Graph Options Variable Declarations
	
	/**
	 * Used for notifying the DataModel of any changes that need to be reflected application wide
	 */
	private DataModel dmf;

	/**
	 * Used for entering in the timing offset for the data
	 */
	private JTextField txtOffset;

	/**
	 * Used for selecting how much data is to be displayed
	 */
	private JComboBox graphWidthComboBox;
	
	/**
	 * Used for selecting the resolution of the data to be displayed
	 */
	private JComboBox graphScaleComboBox;
	
	/**
	 * Used for displaying the resolution of the graph tick marks.
	 */
	private JLabel tickScaleLabel;
	private JLabel fileNameLabel;
	private JLabel rightWindowTime;
	private JLabel leftWindowTime;
	
	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- Graph Options Construction 

	/**
	 * Creates the Graph Options panel with the DataModel that is to receive the updates
	 * 
	 * @param dm - DataModel to receive any updates
	 */
	public GraphOptions(DataModel dm) 
	{
		// Set the DataModel in the graph options to the one passed in the constructor
		this.dmf = dm;
		
		// Set the border, Layout, and Size of the Graph Options
		this.setBorder(new EtchedBorder(EtchedBorder.RAISED, Color.BLACK, Color.WHITE));
		this.setSize(new Dimension(724, 59));
		
		// Panel for containing the graph resolution. 
		// Sets the layout of the panel to null, indicating we are using the absolute positioning layout
		//JPanel resolutionPanel = new JPanel();
		//resolutionPanel.setBounds(2, 2, 240, 55);
		//resolutionPanel.setLayout(null);
		
		// Create a label for the Resolution
		JLabel lblScale = new JLabel("Resolution:");
		lblScale.setBounds(6, 9, 73, 16);
		lblScale.setHorizontalAlignment(SwingConstants.LEFT);
//		resolutionPanel.add(lblScale);
		
		// Create a combo box for the values in the resolution.
		graphScaleComboBox = new JComboBox();
		graphScaleComboBox.setBounds(77, 5, 91, 27);
		graphScaleComboBox.setName("scale");
		graphScaleComboBox.setModel(new DefaultComboBoxModel(new String[] {"15", "20", "40", "60"}));
		setLayout(null);
		
		tickScaleLabel = new JLabel("32 per Second");
		tickScaleLabel.setBounds(262, 21, 121, 16);
		add(tickScaleLabel);
		tickScaleLabel.setHorizontalAlignment(SwingConstants.LEFT);
		
		JLabel lblTickScale = new JLabel("Tick Scale:");
		lblTickScale.setBounds(262, 3, 121, 16);
		add(lblTickScale);
		lblTickScale.setHorizontalAlignment(SwingConstants.LEFT);
		
		// Create a label for the Graph Width
		JLabel lblGraphWidth = new JLabel("Graph Width:");
		lblGraphWidth.setBounds(386, 3, 156, 16);
		add(lblGraphWidth);
		lblGraphWidth.setHorizontalAlignment(SwingConstants.LEFT);
		
		// Create a combo box for the values in the graph width
		graphWidthComboBox = new JComboBox();
		graphWidthComboBox.setBounds(383, 20, 80, 27);
		add(graphWidthComboBox);
		graphWidthComboBox.setName("seconds");
		graphWidthComboBox.setModel(new DefaultComboBoxModel(new String[] {".100", ".25", ".5", "1", "2", "4", "8", "16", "32", "64"}));
		graphWidthComboBox.setSelectedIndex(4);
		
		// Create a label for indicating the width time scale
		JLabel lblSeconds = new JLabel("seconds");
		lblSeconds.setBounds(475, 21, 52, 16);
		add(lblSeconds);
		
		JLabel lblGraphOffset = new JLabel("Graph Offset:");
		lblGraphOffset.setBounds(554, 3, 164, 16);
		add(lblGraphOffset);
		lblGraphOffset.setHorizontalAlignment(SwingConstants.LEFT);
		
		txtOffset = new JTextField();
		txtOffset.setBounds(554, 17, 101, 28);
		add(txtOffset);
		txtOffset.setColumns(10);
		// Add a key listener to the text field to listen to any changes
		txtOffset.addKeyListener(this);
		
		JLabel lblFileName = new JLabel("File Name:");
		lblFileName.setBounds(6, 3, 163, 16);
		add(lblFileName);
		
		fileNameLabel = new JLabel("New label");
		fileNameLabel.setBounds(6, 21, 163, 16);
		add(fileNameLabel);
		
		leftWindowTime = new JLabel();
		leftWindowTime.setHorizontalAlignment(SwingConstants.LEFT);
		leftWindowTime.setFont(new Font("Lucida Grande", Font.BOLD, 16));
		leftWindowTime.setBounds(205, 31, 58, 22);
		add(leftWindowTime);
		
		rightWindowTime = new JLabel();
		rightWindowTime.setHorizontalAlignment(SwingConstants.RIGHT);
		rightWindowTime.setFont(new Font("Lucida Grande", Font.BOLD, 16));
		rightWindowTime.setBounds(660, 31, 58, 22);
		add(rightWindowTime);
		
		
		String val = (String)graphWidthComboBox.getSelectedItem();
		
		if (val.contains("."))
		{
			Float fVal = Float.parseFloat(val) / 2;
			
			leftWindowTime.setText("- " + fVal);
			rightWindowTime.setText("+ " + fVal);				
		}
		else
		{
			Integer iVal = Integer.parseInt(val) / 2;
			
			leftWindowTime.setText("- " + iVal);
			rightWindowTime.setText("+ " + iVal);				
			
		}

		
//		resolutionPanel.add(graphScaleComboBox);
		
		// Add the resolutionPanel to the GraphOptions Panel
		//this.add(resolutionPanel);

		// Panel for containing the Graph Width
		//JPanel widthPanel = new JPanel();
		//widthPanel.setBounds(242, 2, 240, 55);
		//this.add(widthPanel);
		//widthPanel.setLayout(null);
		
		// Panel for containing the graph timing offset
//		JPanel offsetPanel = new JPanel();
//		offsetPanel.setBounds(482, 2, 240, 55);
//		this.add(offsetPanel);
//		offsetPanel.setLayout(null);

	}
	
	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- Java Event Listeners & Action Commands
	// -- NOTE: None of the following methods have been currently implemented in this version

	/**
	 * Sets the graph's Scale Action Command so that it may fire the GraphScale action when necessary.
	 * @param cgs
	 */
	public void setScaleActionCommand(CommandGraphScale cgs)
	{
		// FIXME: Re-enable this when the scale integration has been completed
		//graphScaleComboBox.setAction(cgs);
	}
	
	/**
	 * Sets the graph's width (in seconds) and allows any changes that are made to the combo box to 
	 * fire off an action command to the CommandGraphWidth 
	 * 
	 * @param cgw
	 */
	public void setWidthActionCommand(CommandGraphWidth cgw)
	{
		graphWidthComboBox.setAction(cgw);
		graphWidthComboBox.addActionListener(this);
		// FIXME: When the ActionListener is integrated in, this should be re-enabled so that the
		// graph's scale options will change depending on the time that is being displayed
//		graphWidthComboBox.addActionListener(this);
	}

	/**
	 * Fires when a key typed event has been detected
	 */
	public void keyReleased(KeyEvent arg0) 
	{
		String input = txtOffset.getText();
		int start = input.indexOf('-');
		int sep = input.indexOf('.');

		if ((input.length() > 0 && start == -1) || (input.length() > 1 && start == 0))
		{
			try
	    	{
				int seconds = 0;
//				int millis = 0;
				double dmillis = 0;
						
				
				if (sep < 0)
				{
					if (start == 0)
						seconds = Integer.parseInt(input.substring(start + 1));
					else
						seconds = Integer.parseInt(input);

				}
				else
				{
					String secStr;
					if (start == 0)
						secStr = input.substring(1, sep);
//						seconds = Integer.parseInt(input.substring(start + 1, sep));
					else
						secStr = input.substring(0, sep);
//						seconds = Integer.parseInt(input.substring(0, sep));
					
					//= input.substring(0, sep);
					if (secStr.length() > 0)
					{
						seconds = Integer.parseInt(input.substring(0, sep));
					}

					String milliStr = input.substring(sep + 1);
//					if (milliStr.length() > 0)
//						millis = Integer.parseInt(input.substring(sep + 1));
//					s
					if (milliStr.length() > 0)
						dmillis = Double.parseDouble(input.substring(sep));
					
					System.out.println("milliStr: " + milliStr + " -- dmillis: " + dmillis);
				} 
				
				int offset = 0;
//				long offset = 0;

				if (start == -1)
					offset = seconds * 1000 + (int)(dmillis * 1000);
				else
					offset = seconds * 1000 + (int)(dmillis * 1000) * -1;

//				if (start == -1)
//					offset = seconds * 1000 + dmillis;
//				else
//					offset = seconds * 1000 + dmillis * -1;
				
				System.out.println("offset: " + offset);

					
				// Set the graph offset based on the Integer value from the txtOffset field.
				// If we have a number format exception with that value, we don't update the data model
	    		dmf.setGraphOffset(offset);
	    	}
	    	catch (NumberFormatException nfe)
	    	{
	    		System.out.println("NFE Reached: " + nfe.getMessage());
	    		nfe.printStackTrace();
	    	}		
		}
		else
		{
			dmf.setGraphOffset(0);
		}
	}
	
	// FIXME: This should be used when the ActionListener is integrated in for changing the available graph 
	// scale's depending on the amount of time that is being displayed in the graph window.
//	@Override
//	public void actionPerformed(ActionEvent arg0) 
//	{		
//		graphScaleComboBox.setModel(new DefaultComboBoxModel(getValuesForResolution(graphWidthComboBox.getSelectedIndex())));
//		graphScaleComboBox.setSelectedIndex(0);
//	}

	
	
	
//	 /**
//	  * The following functions pertain to accessing or setting values as requested
//	  */
	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// --Graph Options Getter's & Setters

//	/**
//	 * Used in retrieving the appropriate values to be used with the resolution dropdown box
//	 * @param index
//	 * @return
//	 */
//	private String[] getValuesForResolution(int index)
//	{
//		String[] toReturn = null;
//		switch(index)
//		{
//			case 0:
//				toReturn = new String[] {"1", "10", "25", "50"};
//				break;
//			case 1:
//				toReturn = new String[] {"10", "25", "50", "100"};
//				break;
//			case 2:
//				toReturn = new String[] {"25", "50", "100", "200", "250"};
//				break;
//			case 3:
//				toReturn = new String[] {"25", "50", "100", "200", "250"};
//				break;
//			case 4:
//				toReturn = new String[] {"25", "50", "100", "200", "250"};
//				break;
//			case 5:
//				toReturn = new String[] {"25", "50", "100", "200", "250"};
//				break;
//			case 6:
//				toReturn = new String[] {"25", "50", "100", "200", "250"};
//				break;
//			case 7:
//				toReturn = new String[] {"25", "50", "100", "200", "250"};
//				break;
//		}
//		
//		return toReturn;
//	}

	
	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- Java Event Listeners - UNUSED
	// -- NOTE: None of the following methods have been currently implemented in this version
	
	@Override
	public void update(Observable o, Object arg) 
	{
		if (arg instanceof DataModelProxy)
		{
	    	double seconds =  ((DataModelProxy) arg).getGraphWindowSeconds();
	    	int ticksPer = 4;
	    	
	    	if (seconds < .5)
	    		ticksPer *= 4;
	    	else if (seconds > 16)
	    		ticksPer = 2;
	    	
	    	
	    	tickScaleLabel.setText(ticksPer * 2 + " per Second");
	    	
	    	this.fileNameLabel.setText(((DataModelProxy)arg).getFileName());
		}
	}


	@Override
	public void keyPressed(KeyEvent arg0) {}

	@Override
	public void keyTyped(KeyEvent arg0) {}
	public JLabel getTickScaleLabel() {
		return tickScaleLabel;
	}
	public JLabel getRightWindowTime() {
		return rightWindowTime;
	}
	public JLabel getLeftWindowTime() {
		return leftWindowTime;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) 
	{
		if (arg0.getSource() == graphWidthComboBox)
		{
			String val = (String)graphWidthComboBox.getSelectedItem();
			
			if (val.contains("."))
			{
				Float fVal = Float.parseFloat(val) / 2;
				
				leftWindowTime.setText("- " + fVal);
				rightWindowTime.setText("+ " + fVal);				
			}
			else
			{
				Integer iVal = Integer.parseInt(val) / 2;
				
				leftWindowTime.setText("- " + iVal);
				rightWindowTime.setText("+ " + iVal);				
				
			}
			
			
		}
	}
}
