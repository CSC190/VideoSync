/**
 * ****************************************************************
 * File: 			Line.java
 * Date Created:  	June 5, 2013
 * Programmer:		Dale Reed
 * 
 * Purpose:			Used for creating line elements that will
 * 					be used in creating all of the graph lines
 * 
 * ****************************************************************
 */
package objects.graphs;

public class Line 
{
	/**
	 * The first point for the line
	 */
	private int x0;
	private int y0;

	/**
	 * The second point for the line
	 */
	private int x1;
	private int y1;
	

	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- Line Constructor

	/**
	 * Construct a line element that can be displayed in the graph panel
	 * @param x0
	 * @param y0
	 * @param x1
	 * @param y1
	 */
	public Line(int x0, int y0, int x1, int y1)
	{
		this.x0 = x0;
		this.y0 = y0;
		
		this.x1 = x1;
		this.y1 = y1;
	}
	

	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- Line Getters

	/**
	 * Return the x0 position
	 * @return
	 */
	public int getX0()
	{
		return this.x0;
	}
	
	/**
	 * Return the y0 position
	 * @return
	 */
	public int getY0()
	{
		return this.y0;
	}
	
	/**
	 * Return the x1 position
	 * @return
	 */
	public int getX1()
	{
		return this.x1;
	}

	/**
	 * Return the y1 position
	 * @return
	 */
	public int getY1()
	{
		return this.y1;
	}
	
	/**
	 * Returns a string representation of the line object.
	 */
	public String toString()
	{
		return String.format("Line from (x0, y0) to (x1, y1): (%d, %d) to (%d, %d)", this.x0, this.y0, this.x1, this.y1);
	}
}
