/**
 * ****************************************************************
 * File: 			GraphPane.java
 * Date Created:  	June 6, 2013
 * Programmer:		Dale Reed
 * 
 * Purpose:			To render the graph's on the JPanel as the data
 * 					is sent from the GraphPanel
 * 
 * ****************************************************************
 */

package views.tabbed_panels.graphs;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.Vector;

import javax.swing.JPanel;

import objects.graphs.Line;

public class GraphPane extends JPanel 
{	
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- Graph pane Variable Declarations

	/**
	 * Set the state line default color to Black
	 */
	private Color lineColor = Color.BLACK;
	
	/**
	 * Set the tick line default color to Gray
	 */
	private Color tickColor = Color.GRAY;
	
	/**
	 * Set the line stroke for the tick lines
	 */
	private BasicStroke normalStroke = new BasicStroke(1.0F);

	/**
	 * Set the line stroke or for the center tick line
	 */
	private BasicStroke centerStroke = new BasicStroke(2.0F);
	
	/**
	 * Set the line stroke for the thick center tick line.
	 */
	private BasicStroke centerThickStroke = new BasicStroke(2.0F);
	
	/**
	 * Used for storing the tick marks that are to be rendered
	 */
	private Vector<Line> ticks;
	
	/**
	 * Used for storing the state lines that are to be rendered
	 */
	private Vector<Line> states;
	
	/**
	 * Used for indicating if we need to draw the center line thicker.
	 */
	private boolean thickCenter;

	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- Graph Pane Construction 
	
	/**
	 * Construct a new Graph Pane with no parameters. Calls repaint when the view has been created.
	 */
	public GraphPane() 
	{
		super(null);
		
		this.repaint();
	}
	
	
	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- Graph Pane Paint 

	/**
	 * Paints the components onto the graphics window. This will render the tick marks as well as the state diagrams
	 * 
	 * @see Line
	 */
	public void paint(Graphics g)
	{
		super.paintComponent(g);
		
		Graphics2D g2d = (Graphics2D)g;
		
		// If the ticks array is not null and empty, we can draw the tick marks on the screen.
		if (ticks != null && !ticks.isEmpty())
		{
			// Loop through all of the tick marks in the array and draw them at the points
			for (int i = 0; i < ticks.size(); i++)
			{
				// Get the Line object from the specified index
				Line l = ticks.elementAt(i);
				
//				// If the index is at the middle position, we draw our center line
//				if (i == ticks.size() / 2)
//				{
//					// The center line is currently set to red
//					// Set the stroke to make the center line distinguishable
//					// TODO: Make this color changeable from a prefs file
//					g2d.setColor(Color.RED);
//					g2d.setStroke(centerStroke);
//				}
//				else
//				{
					// The center line is currently set to gray
					// Set the stroke to change it from the center line
					// TODO: Make this color changeable from a prefs file
					
					g2d.setColor(tickColor);
					g2d.setStroke(normalStroke);
//				}
				
				// Draw the line object onto the graphics window
				g2d.drawLine(l.getX0(), l.getY0(), l.getX1(), l.getY1());
			}
		}

		// Draw Center Line
		g2d.setColor(Color.RED);
		g2d.setStroke((this.thickCenter) ? centerThickStroke : centerStroke);
		g2d.drawLine((this.getWidth() / 2), 0, (this.getWidth() / 2), this.getHeight());
		
		// If the states array is not null, 
		if (states != null && !states.isEmpty())
		{
			// Loop through all of the state objects to be drawn on the screen.
			for (int i = states.size() - 1; i >= 0; i--)
			{
				// Get the Line object from the specified index
				Line l = states.elementAt(i);
								
				// Set the line color to be drawn. This is changeable for each graph pane
				// Set the stroke color to the same as center stroke. This makes it distinguishable
				// Draw the line object onto the graphics window
				g2d.setColor(lineColor);
				g2d.setStroke(centerStroke);
				g2d.drawLine(l.getX0(), l.getY0(), l.getX1(), l.getY1());
			}
		}
	}

	
	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- Graph Pane Setters 
	
	/**
	 * Set the states to be drawn by the panels paint method.
	 * Calls repaint when states has been assigned.
	 * @param states
	 */
	public void setStates(Vector<Line> states)
	{
		this.states = states;
		this.repaint();
	}
	
	/**
	 * Set the tick marks to be drawn by the panels paint method.
	 * Calls repaint when ticks has been assigned.
	 * @param ticks
	 */
	public void setTicks(Vector<Line> ticks, boolean thickCenter)
	{
		this.thickCenter = thickCenter;
		this.ticks = ticks;
		this.repaint();
	}
	
	/**
	 * Sets the line color to be used when rendering the states
	 * @param lineColor
	 */
	public void setLineColor(Color lineColor)
	{
		this.lineColor = lineColor;
		this.repaint();
	}
}
