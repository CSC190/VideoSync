/**
 * ****************************************************************
 * File: 			ImportFilter.java
 * Date Created:  	August 12, 2013
 * Programmer:		Dale Reed
 * 
 * Purpose:			To limit the file types that can be opened.
 * 
 * ****************************************************************
 */
package objects;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class ImportFilter extends FileFilter 
{
	/**
	 * Returns if a file can be opened up or not.
	 */
	public boolean accept(File f) 
	{
		// If the file is a directory we can go ahead and upload it.
		if (f.isDirectory())
			return true;
		
		// Gets the extension from the file to determine if it can be opened or not.
		String extension = Utils.getExtension(f);
		
		// If the extension is not null, then check to see if it can actaully be opened.
		if (extension != null)
		{
			// Check to see if the extension is one we can use
			if (extension.equals(Utils.mov) || extension.equals(Utils.dat) || extension.equals(Utils.mpf) || extension.equals(Utils.vbm) || extension.equals(Utils.c1) || extension.equals(Utils.c1maxim))
				return true;
		}
		
		// If we get to this point then we haven't found a valid file, then we return false.
		return false;
	}

	/**
	 * This is used for making it quick and easy to access the file extensions.
	 * 
	 * FIXME: This also should include all of the additional movie formats that can be used by VLC
	 */
	static class Utils 
	{
        public final static String mov = "mov";
        public final static String dat = "dat";
        public final static String mpf = "mpf";
        public final static String vbm  = "vbm";
        public final static String c1  = "c1";
        public final static String c1maxim  = "c1max";

        /**
         * Returns the extension of the file.
         * 
         * @param f - The file passed
         * @return
         */
        public static String getExtension(File f)
        {
        	// Initialize the extension to null
            String ext = null;
            
            // Get the name of the file.
            String s = f.getName();
      
            // Get the index position of the last . for the extension
            int i = s.lastIndexOf('.');
            
            // As long as i > 0 and i < the length of the string, then get the extension
            if (i > 0 && i < s.length() - 1)
            {
            	// Set the extension of the file from the substring of s.
                ext = s.substring(i+1).toLowerCase();
            }
            
            // Return the extension of the file if one was found
            return ext;
        }
    }

	
	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- This is an abstract method from FileFilter and is required with the extension
	
	/**
	 * Returns a description of the Import Filter
	 */
	public String getDescription() 
	{
		return null;
	}
}
