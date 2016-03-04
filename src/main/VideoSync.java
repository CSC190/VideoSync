/**
 * ****************************************************************
 * File: 			VideoSync.java
 * Date Created:  	June 5, 2013
 * Programmer:		Dale Reed
 * 
 * Purpose:			This is the main entry point for VideoSync. 
 * 					It creates the Graph, DataModel, and CommandList
 * 					together since they are the minimum classes files
 * 					needed for the program to run.
 * 
 * ****************************************************************
 */

package main;

//import java.io.File;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Calendar;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import models.DataModel;
import objects.LogItem;
import views.tabbed_panels.DataWindow;

import commands.CommandList;

public class VideoSync 				
{
	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- VideoSync Variable Declarations
	
	private DataWindow dataWindow;
	private DataModel dataModel;

	private static String javaArchitecture;
	
	/**
	 * Entry Point for VideoSync to start
	 * @param args
	 */
	public static void main(String args[])
	{		
		// Before presenting any of the views for VideoSync, we need to ensure that all of the preferences and necessary
		// components have been installed & located before they are presented. If they have not been installed or located,
		// we need to either create them, or ask the user to locate them.
		try 
		{
			// Get the name of the os for determining the URL patterns that are going to be used.		
			String systemType = System.getProperty("os.name");
			javaArchitecture = System.getProperty("sun.arch.data.model");

			String vsLibLoc;
			String vlcLibLoc;
			if (systemType.startsWith("Mac"))
			{
				vsLibLoc = System.getProperty("user.home") + "/Library/VideoSync";
				vlcLibLoc = "/Contents/MacOS/lib/";
			}
			else if (systemType.startsWith("Windows"))
			{  
				// The Java Architecture Type
				vsLibLoc = System.getProperty("user.dir").substring(0, 3) + "VideoSync";
				vlcLibLoc = "";
			}
			else // Linux
			{
//				TODO: Fill in path to store application information for VideoSync
				vsLibLoc = "";
				vlcLibLoc = "";
			}
	
			// Stores the final location of VLC - this gets passed to the VideoPlayer via the DataModel 
			// so that it can be loaded with the NativeLibraries
			File vlc = null;
			
			// Temporarily stores the library during startup so we can either validate is existence or create it.
			File library = new File(vsLibLoc);
			
			// Temporarily stores the VideoSync preferences file so we can write the location of the VLC installation
			File prefsFile = new File(vsLibLoc + "/videosync.pref");
			
			// Stores the location of the log's directory so that any log files that get written out can be stored
			File logsFile = new File(vsLibLoc + "/logs/");

			// Temporarily stores the path of the VLC installation so it can be written to the prefs file.
			String vlcPath = null;

			// Starts up the LoggerThread so that we can easily log any messages that might help in diagnosing problems while in testing.
			//LoggerThread lt = new LoggerThread("LoggerThread", vsLibLoc + "/logs/");
			LoggerThread lt = new LoggerThread("LoggerThread", vsLibLoc + File.separator + "logs" + File.separator);

			// Verify that the library location exists. 
			// If so, confirm that the VLC path stored in the preference file is still valid
			// If not, we'll create the preferences file and search for the VLC installation.
			if (library.exists())
			{
				// Start the Logging Thread so it may begin receiving log files.
				lt.start();
	
				sendMessageToLogManager(new LogItem("Startup", Calendar.getInstance().getTime().toString(), "VideoSync Library found at " + vsLibLoc, "Notice"));
				
				// Verify that there is a preferences file.
				// If there is one, we'll read the location of the VLC installation and store it into vlcPath for use
				if (prefsFile.exists())
				{
					vlcPath = new BufferedReader(new FileReader(prefsFile)).readLine();
				}
				
				// Check to make sure we have a valid vlcPath and and that the path exists.
				// If it does, we can continue on with as no other checking needs to be done.
				// If it either of the two fail, we need to request input from the user to correct the issue.
				if (vlcPath == null || !new File(vlcPath).exists())
				{
					// Reset the vlcPath to null since it may be incorrect.
					vlcPath = null;
					
					// Request the user to select their VLC installation. The result gets stored into VLC
					vlc = handleNoVLCInstall();
					
					// Update the preferences file so that it may be used the next time VideoSync runs
					// If we failed to create the preferences file, send a notice to the log manager.
					if (!createPrefsFile(prefsFile, vlc.toString(), vlcLibLoc))
					{
						sendMessageToLogManager(new LogItem("Startup", Calendar.getInstance().getTime().toString(), "Failed to create VideoSync Preferences File at " + prefsFile.getPath(), "ERROR"));
					}					
				}
			}
			// We did not find a library file so we need to create one and search for th VLC installation
			else
			{
				// Create the Library & Log File Directories
				library.mkdir();
				logsFile.mkdir();
				
				// Start the Logging Thread so it may begin receiving log files.
				lt.start();
				
				sendMessageToLogManager(new LogItem("Startup", Calendar.getInstance().getTime().toString(), "VideoSync Library not found: Created Library & Log Files at " + vsLibLoc, "Notice"));
				sendMessageToLogManager(new LogItem("Startup", Calendar.getInstance().getTime().toString(), "Searching for VLC Application in /Applications/", "Notice"));
					
				// Check to see if VLC has been installed and stores the result into vlc.
				// -- passes the system type so we can use the correct file URIs
				vlc = isVLCInstalled(systemType);

				// If VLC is not null, indicating we found a valid installation, we can continue on and create the prefs file.
				if (vlc != null)
				{
					sendMessageToLogManager(new LogItem("Startup", Calendar.getInstance().getTime().toString(), "Found VLC Application at " + vlc.getPath(), "Notice"));
					sendMessageToLogManager(new LogItem("Startup", Calendar.getInstance().getTime().toString(), "Creating VideoSync Preferences File at " + prefsFile.getPath(), "Notice"));
						
					
					// Create the preferences file so that it may be used the next time VideoSync runs
					// If we failed to create the preferences file, send a notice to the log manager.
					if (!createPrefsFile(prefsFile, vlc.toString(), vlcLibLoc))
					{
						sendMessageToLogManager(new LogItem("Startup", Calendar.getInstance().getTime().toString(), "Failed to creat VideoSync Preferences File at " + prefsFile.getPath(), "ERROR"));
					}
				}
				// We failed to find VLC on our own, so we need to search for it.
				else
				{
					sendMessageToLogManager(new LogItem("Startup", Calendar.getInstance().getTime().toString(), "No VLC Installation Found. Requesting user to install/download VLC", "Warning"));
				
					int attempts = 0;
					boolean finished = false;
					
					// As long as we have not found a VLC installation, we need to keep asking for it.
					while (!finished)
					{
						// Request the user to locate their VLC installation and store the result into vlc.
						vlc = handleNoVLCInstall();
						
						// If VLC is null, send a message to the log manager indicating that a vlc installation was not found.
						if (vlc == null)
						{
							sendMessageToLogManager(new LogItem("Startup", Calendar.getInstance().getTime().toString(), "User failed to install VLC.", "ERROR"));
							
							// If we can't find an installation in 3 attempts, we will notify the user and quit.
							if (attempts == 3)
								System.exit(-1);
						}
						
						// If the VLC location exists, we can stop with this loop and start up the program.
						if (vlc.exists())
						{
							finished = true;
						}
						
						attempts++;
					}
					
					sendMessageToLogManager(new LogItem("Startup", Calendar.getInstance().getTime().toString(), "User Installed VLC at " + vlc.getAbsolutePath(), "Warning"));
					sendMessageToLogManager(new LogItem("Startup", Calendar.getInstance().getTime().toString(), "Creating VideoSync Preferences File at " + prefsFile.getPath(), "Notice"));
					
					// Create the preferences file so that it may be used the next time VideoSync runs
					// If we failed to create the preferences file, send a notice to the log manager.
					if (!createPrefsFile(prefsFile, vlc.toString(), vlcLibLoc))
					{
						sendMessageToLogManager(new LogItem("Startup", Calendar.getInstance().getTime().toString(), "Failed to creat VideoSync Preferences File at " + prefsFile.getPath(), "ERROR"));
					}					
				}
			}
				
			// This ensures we use the latest information regarding the vlcPath.
			// This only runs if the vlcPath stored in the prefs file was incorrect
			// or the location of VLC has changed.
			if (vlc != null || vlcPath == null || vlcPath == "")
			{
				vlcPath = vlc.getAbsolutePath() + vlcLibLoc;
			}
			
			// Create a new instance of VideoSync
			new VideoSync(vlcPath);
		} 
		catch (FileNotFoundException e) 
		{
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));

			sendMessageToLogManager(new LogItem("Errors", Calendar.getInstance().getTime().toString(), e.getMessage() + " - Stack Trace: " + e, "ERROR"));
		} 
		catch (IOException e) 
		{
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));

			sendMessageToLogManager(new LogItem("Errors", Calendar.getInstance().getTime().toString(), e.getMessage() + " - Stack Trace: " + e, "ERROR"));
		} 
		catch (SecurityException se)
		{
			se.printStackTrace();
		}
	}

	
	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- VideoSync Construction 

	/**
	 * Creates VideoSync allocating the DataModel, Graph, and Command List
	 */
	private VideoSync(String vlcPath)
	{
		dataModel = new DataModel(vlcPath);
		dataWindow = new DataWindow(dataModel);
		
		CommandList cl = new CommandList(dataModel, dataWindow);
		
		dataWindow.setCommands(cl);

		File videoFile = new File("/Users/caltrans/Desktop/NB_Segment-2013-06-09-14-59-56.mov");
		
		if (videoFile.exists())
			dataModel.addVideoFile(videoFile);
		

	}

	
	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- VideoSync Startup Methods 

	/**
	 * Creates the preferences file for storing the VLC installation location.
	 * @param prefsFile
	 * @param vlcLoc
	 * @param vlcLib
	 * @return
	 */
	
	private static boolean createPrefsFile(File prefsFile, String vlcLoc, String vlcLib)
	{
		try 
		{
			// Ensure that the Library file has been created.
			// If not attempt to create it.
			// IF it cannot be created, return false.
			if (!prefsFile.getParentFile().exists())
				if (!prefsFile.getParentFile().mkdir())
				{
					sendMessageToLogManager(new LogItem("Startup", Calendar.getInstance().getTime().toString(), "Failed to create VideoSync Preferences File at " + prefsFile.getAbsolutePath(), "Notice"));

					return false;
				}

			// Create a FileWriter object so we can write the prefs file
			FileWriter fw = new FileWriter(prefsFile);
			
			// Write the VLC Location & VLC Library to the preferences file.
			fw.write(String.format("%s%s", vlcLoc, vlcLib));
			
			// Close the FileWriter for the prefs file.
			fw.close();
			
			// Write the sytem information so we it can potentially be used for debugging information
			writeSystemInfo(new File(prefsFile.getParent() + "/system.info"));
		}
		catch (IOException e) 
		{
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));

			sendMessageToLogManager(new LogItem("Errors", Calendar.getInstance().getTime().toString(), e.getMessage() + " - Stack Trace: " + e, "ERROR"));

			return false;
		} 
		
		// Return true indicating success
		return true;
	}
	
	/**
	 * Write the system information to a file so it can be included in error reporting
	 * @param file
	 * @return
	 */

	private static boolean writeSystemInfo(File sysFile)
	{
		sendMessageToLogManager(new LogItem("Startup", Calendar.getInstance().getTime().toString(), "Writing System Information at " + sysFile.getAbsolutePath(), "Notice"));
		
		try 
		{
			FileWriter fw = new FileWriter(sysFile);
			fw.write("OS Name:\t\t\t" 						+ System.getProperty("os.name") + "\n");
			fw.write("OS Version:\t\t\t" 					+ System.getProperty("os.version") + "\n");
			fw.write("OS Architecture:\t" 					+ System.getProperty("os.arch") + "\n");
			fw.write("Java Class Path:\t" 					+ System.getProperty("java.class.path") + "\n");
			fw.write("Java Home: \t\t\t" 					+ System.getProperty("java.home") + "\n");
			fw.write("Java Vendor:\t\t" 					+ System.getProperty("java.vendor") + "\n");
			fw.write("Java Version:\t\t" 					+ System.getProperty("java.version") + "\n");
			fw.write(" ----- System Information ------\n");
			fw.write("Number of Cores: \t\t\t\t" 			+ Runtime.getRuntime().availableProcessors() + "\n");
		    long maxMemory = Runtime.getRuntime().maxMemory();
		    fw.write("Maximum memory for JVM (bytes):\t" 	+  (maxMemory == Long.MAX_VALUE ? "no limit" : maxMemory) + "\n");
		    fw.write("Total Memory for JVM (bytes):\t" 		+ Runtime.getRuntime().totalMemory() + "\n");
			fw.close();
		} 
		catch (IOException e) 
		{
			sendMessageToLogManager(new LogItem("Startup", Calendar.getInstance().getTime().toString(), "Failed to write System Information at " + sysFile.getAbsolutePath(), "Notice"));

			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));

			sendMessageToLogManager(new LogItem("Errors", Calendar.getInstance().getTime().toString(), e.getMessage() + " - Stack Trace: " + e, "ERROR"));

			// Because we had an error, we need to return false.
			return false;
		}

		// Return true indicating success
		return true;
	}
	
	/**
	 * Checks to make sure that VLC is installed and able to be found. This method only runs if its 
	 * the first time VideoSync has been run.
	 * 
	 * @param systemType
	 * @return
	 */
	
	private static File isVLCInstalled(String systemType)
	{
		File applicationDirectory;
		
		// Open the default directories with the correct URI's for each type of OS
		if (systemType.startsWith("Mac"))
		{
			applicationDirectory = new File("/Applications");
		}
		else if (systemType.startsWith("Windows"))
		{
			applicationDirectory = File.listRoots()[1];	//new File(File.listRoots()[1]);
		}
		else // Linux
		{
//			TODO: Fill in path to store application information for VideoSync
			applicationDirectory = new File("");
		}
		
		// Store all the applications into a temporary array.
		File apps[] = applicationDirectory.listFiles();
		
		if (apps != null)
		{
			// Iterate through all of the Applications found and search for VLC 
			for (File app : apps)
			{
				if (app.isDirectory())
				{
					// If the name of the application starts with VLC, we can stop searching
					// and return the file for use in main();
					if (app.getName().startsWith("VLC"))
						return app;
				}					
			}
		}
		
		// If we did not find a valid file, we return null.
		return null;
	}
	
	/**
	 * Handles the user interaction for dealing with No Valid VLC installation
	 * @return
	 */
	
	private static File handleNoVLCInstall()
	{
		try 
		{
			Object[] options = {"Download VLC", "Select Installation", "Quit VideoSync"};
			
			// Show na option pane and get the result of their input.
			// Because JOptionPane requires a parent component to display the alert, we just create an empty JFrame so it will be displayed. 
			int n = JOptionPane.showOptionDialog(new JFrame(),
												"We were unable to find your VLC Installation. What would you like to do?",
												null,
												JOptionPane.YES_NO_CANCEL_OPTION,
												JOptionPane.QUESTION_MESSAGE,
												null,
												options,
												options[2]);
			
			// User wants to download VideoSync
			if (n == 0)
			{
				// Notify the user that they are being redirected to VLC's home page so they can install VLC.
				JOptionPane.showMessageDialog(new JFrame(), "You will be redirected to VLC's download page.\nPlease download the " + javaArchitecture + "bit version of VLC and install it to your root directory (i.e. C:\\).\nWhen the installation is complete, restart VideoSync");
				
				// Open the default web browser to install vlc
				Desktop.getDesktop().browse(new URI("http://www.videolan.org/vlc/"));
				
				sendMessageToLogManager(new LogItem("Startup", Calendar.getInstance().getTime().toString(), "User is downloading VLC from the web", "Notice"));
				
				// Sleep for 2 seconds to allow the log files to be finished writing and then exit.
				Thread.sleep(2000);
				System.exit(1);
			}
			// User wants to Select their VLC Installation
			else if (n == 1)
			{
				sendMessageToLogManager(new LogItem("Startup", Calendar.getInstance().getTime().toString(), "User is selecting their VLC Installation.", "Notice"));
				
				// Present a JFileChooser so the user may select their VLC installation.
				JFileChooser fc = new JFileChooser();
				
				// Allow Files to be selected
				// TODO: NOTE - This may need to change depending on OS 
				// 			- Mac applications are "files", while Windows applications should be a folder location and not the .exe
				fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
				
				if (fc.showOpenDialog(new JFrame()) == JFileChooser.APPROVE_OPTION)
				{
					// Return the file the user selected.
					return fc.getSelectedFile();
				}
			}
			// User wants to quit
			else
			{
				sendMessageToLogManager(new LogItem("Startup", Calendar.getInstance().getTime().toString(), "User elected not to install VLC. Quitting VideoSync", "Notice"));
				
				// Sleep for 2 seconds to allow the log files to be finished writing and then exit.
				Thread.sleep(2000);
				System.exit(1);
			}
		}
		catch (InterruptedException e) 
		{
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));

			sendMessageToLogManager(new LogItem("Errors", Calendar.getInstance().getTime().toString(), e.getMessage() + " - Stack Trace: " + e, "ERROR"));
		} 
		catch (IOException e) 
		{
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));

			sendMessageToLogManager(new LogItem("Errors", Calendar.getInstance().getTime().toString(), e.getMessage() + " - Stack Trace: " + e, "ERROR"));
		} 
		catch (URISyntaxException e) 
		{
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));

			sendMessageToLogManager(new LogItem("Errors", Calendar.getInstance().getTime().toString(), e.getMessage() + " - Stack Trace: " + e, "ERROR"));
		}
		
		return null;
	}
	
	/**
	 * Sends a new Log Item to the LoggerThread so it can be written to the appropriate Log File
	 * @param li
	 */
	private static void sendMessageToLogManager(LogItem li)
	{
		try 
		{
			// Acquire access to the LoggerThread
			LoggerThread.acquireAccess();
			
			// Send the LogItem to the LoggerThread
			LoggerThread.addToList(li);
			
			// Release Access to the LoggerThread
			LoggerThread.releaseAccess();
		}
		catch (InterruptedException e) 
		{
			e.printStackTrace();
		}
	}
}




















///**
// * ****************************************************************
// * File: 			VideoSync.java
// * Date Created:  	June 5, 2013
// * Programmer:		Dale Reed
// * 
// * Purpose:			This is the main entry point for VideoSync. 
// * 					It creates the Graph, DataModel, and CommandList
// * 					together since they are the minimum classes files
// * 					needed for the program to run.
// * 
// * ****************************************************************
// */
//
//package main;
//
//import java.awt.Desktop;
//import java.io.BufferedReader;
//import java.io.File;
//import java.io.FileNotFoundException;
//import java.io.FileReader;
//import java.io.FileWriter;
//import java.io.IOException;
//import java.io.PrintWriter;
//import java.io.StringWriter;
//import java.net.URI;
//import java.net.URISyntaxException;
//import java.util.Calendar;
//
//import javax.swing.JFileChooser;
//import javax.swing.JFrame;
//import javax.swing.JOptionPane;
//
//import models.DataModel;
//import objects.LogItem;
//import views.tabbed_panels.DataWindow;
//import commands.CommandList;
//
//public class VideoSync 				
//{
//	//-------------------------------------------------------------------------------------------------------------------------------------
//	//-------------------------------------------------------------------------------------------------------------------------------------
//	// -- VideoSync Variable Declarations
//	
//	private DataWindow dataWindow;
//	private DataModel dataModel;
//
//	/**
//	 * Entry Point for VideoSync to start
//	 * @param args
//	 */
//	public static void main(String args[])
//	{		
//		// Before presenting any of the views for VideoSync, we need to ensure that all of the preferences and necessary
//		// components have been installed & located before they are presented. If they have not been installed or located,
//		// we need to either create them, or ask the user to locate them.
//		try 
//		{
//			// Get the name of the os for determining the URL patterns that are going to be used.		
//			String systemType = System.getProperty("os.name");
//			
//			String vsLibLoc;
//			String vlcLibLoc;
//			if (systemType.startsWith("Mac"))
//			{
//				vsLibLoc = System.getProperty("user.home") + "/Library/VideoSync";
//				vlcLibLoc = "/Contents/MacOS/lib/";
//			}
//			else if (systemType.startsWith("Windows"))
//			{
////				TODO: Fill in path to store application information for VideoSync
//				vsLibLoc = "";
//				vlcLibLoc = "";
//			}
//			else // Linux
//			{
////				TODO: Fill in path to store application information for VideoSync
//				vsLibLoc = "";
//				vlcLibLoc = "";
//			}
//	
//			// Stores the final location of VLC - this gets passed to the VideoPlayer via the DataModel 
//			// so that it can be loaded with the NativeLibraries
//			File vlc = null;
//			
//			// Temporarily stores the library during startup so we can either validate is existence or create it.
//			File library = new File(vsLibLoc);
//			
//			// Temporarily stores the VideoSync preferences file so we can write the location of the VLC installation
//			File prefsFile = new File(vsLibLoc + "/videosync.pref");
//			
//			// Stores the location of the log's directory so that any log files that get written out can be stored
//			File logsFile = new File(vsLibLoc + "/logs/");
//
//			// Temporarily stores the path of the VLC installation so it can be written to the prefs file.
//			String vlcPath = null;
//
//			// Starts up the LoggerThread so that we can easily log any messages that might help in diagnosing problems while in testing.
//			LoggerThread lt = new LoggerThread("LoggerThread", vsLibLoc + "/logs/");
//
//			// Verify that the library location exists. 
//			// If so, confirm that the VLC path stored in the preference file is still valid
//			// If not, we'll create the preferences file and search for the VLC installation.
//			if (library.exists())
//			{
//				// Start the Logging Thread so it may begin receiving log files.
//				lt.start();
//	
//				sendMessageToLogManager(new LogItem("Startup", Calendar.getInstance().getTime().toString(), "VideoSync Library found at " + vsLibLoc, "Notice"));
//				
//				// Verify that there is a preferences file.
//				// If there is one, we'll read the location of the VLC installation and store it into vlcPath for use
//				if (prefsFile.exists())
//				{
//					vlcPath = new BufferedReader(new FileReader(prefsFile)).readLine();
//				}
//				
//				// Check to make sure we have a valid vlcPath and and that the path exists.
//				// If it does, we can continue on with as no other checking needs to be done.
//				// If it either of the two fail, we need to request input from the user to correct the issue.
//				if (vlcPath == null || !new File(vlcPath).exists())
//				{
//					// Reset the vlcPath to null since it may be incorrect.
//					vlcPath = null;
//					
//					// Request the user to select their VLC installation. The result gets stored into VLC
//					vlc = handleNoVLCInstall();
//					
//					// Update the preferences file so that it may be used the next time VideoSync runs
//					// If we failed to create the preferences file, send a notice to the log manager.
//					if (!createPrefsFile(prefsFile, vlc.toString(), vlcLibLoc))
//					{
//						sendMessageToLogManager(new LogItem("Startup", Calendar.getInstance().getTime().toString(), "Failed to create VideoSync Preferences File at " + prefsFile.getPath(), "ERROR"));
//					}					
//				}
//			}
//			// We did not find a library file so we need to create one and search for th VLC installation
//			else
//			{
//				// Create the Library & Log File Directories
//				library.mkdir();
//				logsFile.mkdir();
//				
//				// Start the Logging Thread so it may begin receiving log files.
//				lt.start();
//				
//				sendMessageToLogManager(new LogItem("Startup", Calendar.getInstance().getTime().toString(), "VideoSync Library not found: Created Library & Log Files at " + vsLibLoc, "Notice"));
//				sendMessageToLogManager(new LogItem("Startup", Calendar.getInstance().getTime().toString(), "Searching for VLC Application in /Applications/", "Notice"));
//					
//				// Check to see if VLC has been installed and stores the result into vlc.
//				// -- passes the system type so we can use the correct file URIs
//				vlc = isVLCInstalled(systemType);
//
//				// If VLC is not null, indicating we found a valid installation, we can continue on and create the prefs file.
//				if (vlc != null)
//				{
//					sendMessageToLogManager(new LogItem("Startup", Calendar.getInstance().getTime().toString(), "Found VLC Application at " + vlc.getPath(), "Notice"));
//					sendMessageToLogManager(new LogItem("Startup", Calendar.getInstance().getTime().toString(), "Creating VideoSync Preferences File at " + prefsFile.getPath(), "Notice"));
//						
//					
//					// Create the preferences file so that it may be used the next time VideoSync runs
//					// If we failed to create the preferences file, send a notice to the log manager.
//					if (!createPrefsFile(prefsFile, vlc.toString(), vlcLibLoc))
//					{
//						sendMessageToLogManager(new LogItem("Startup", Calendar.getInstance().getTime().toString(), "Failed to creat VideoSync Preferences File at " + prefsFile.getPath(), "ERROR"));
//					}
//				}
//				// We failed to find VLC on our own, so we need to search for it.
//				else
//				{
//					sendMessageToLogManager(new LogItem("Startup", Calendar.getInstance().getTime().toString(), "No VLC Installation Found. Requesting user to install/download VLC", "Warning"));
//				
//					int attempts = 0;
//					boolean finished = false;
//					
//					// As long as we have not found a VLC installation, we need to keep asking for it.
//					while (!finished)
//					{
//						// Request the user to locate their VLC installation and store the result into vlc.
//						vlc = handleNoVLCInstall();
//						
//						// If VLC is null, send a message to the log manager indicating that a vlc installation was not found.
//						if (vlc == null)
//						{
//							sendMessageToLogManager(new LogItem("Startup", Calendar.getInstance().getTime().toString(), "User failed to install VLC.", "ERROR"));
//							
//							// If we can't find an installation in 3 attempts, we will notify the user and quit.
//							if (attempts == 3)
//								System.exit(-1);
//						}
//						
//						// If the VLC location exists, we can stop with this loop and start up the program.
//						if (vlc.exists())
//						{
//							finished = true;
//						}
//						
//						attempts++;
//					}
//					
//					sendMessageToLogManager(new LogItem("Startup", Calendar.getInstance().getTime().toString(), "User Installed VLC at " + vlc.getAbsolutePath(), "Warning"));
//					sendMessageToLogManager(new LogItem("Startup", Calendar.getInstance().getTime().toString(), "Creating VideoSync Preferences File at " + prefsFile.getPath(), "Notice"));
//					
//					// Create the preferences file so that it may be used the next time VideoSync runs
//					// If we failed to create the preferences file, send a notice to the log manager.
//					if (!createPrefsFile(prefsFile, vlc.toString(), vlcLibLoc))
//					{
//						sendMessageToLogManager(new LogItem("Startup", Calendar.getInstance().getTime().toString(), "Failed to creat VideoSync Preferences File at " + prefsFile.getPath(), "ERROR"));
//					}					
//				}
//			}
//				
//			// This ensures we use the latest information regarding the vlcPath.
//			// This only runs if the vlcPath stored in the prefs file was incorrect
//			// or the location of VLC has changed.
//			if (vlc != null || vlcPath == null || vlcPath == "")
//			{
//				vlcPath = vlc.getAbsolutePath() + vlcLibLoc;
//			}
//			
//			// Create a new instance of VideoSync
//			new VideoSync(vlcPath);
//		} 
//		catch (FileNotFoundException e) 
//		{
//			StringWriter errors = new StringWriter();
//			e.printStackTrace(new PrintWriter(errors));
//
//			sendMessageToLogManager(new LogItem("Errors", Calendar.getInstance().getTime().toString(), e.getMessage() + " - Stack Trace: " + e, "ERROR"));
//		} 
//		catch (IOException e) 
//		{
//			StringWriter errors = new StringWriter();
//			e.printStackTrace(new PrintWriter(errors));
//
//			sendMessageToLogManager(new LogItem("Errors", Calendar.getInstance().getTime().toString(), e.getMessage() + " - Stack Trace: " + e, "ERROR"));
//		} 
//	}
//
//	
//	//-------------------------------------------------------------------------------------------------------------------------------------
//	//-------------------------------------------------------------------------------------------------------------------------------------
//	// -- VideoSync Construction 
//
//	/**
//	 * Creates VideoSync allocating the DataModel, Graph, and Command List
//	 */
//	private VideoSync(String vlcPath)
//	{
//		dataModel = new DataModel(getVLCPath());
//		dataWindow = new DataWindow(dataModel);
//		
//		CommandList cl = new CommandList(dataModel, dataWindow);
//		
//		dataWindow.setCommands(cl);
//	
//		// LOG 170 TESTING
//		// Temporary Methods to set video file and data file automatically
////		dataModel.set170Data(new File("/Users/caltrans/Desktop/Chico Pre-Collision/Results-Pre/2013.6.1_13.00.00/2013.6.1_13.00.00.dat"));
////		dataModel.setVBMFile(new File("/Users/caltrans/Desktop/Chico Pre-Collision/Results-Pre/2013.6.1_13.00.00/chico data.vbm"));
//////		dataModel.setMappingFile(new File("/Users/caltrans/Desktop/Chico Pre-Collision/Results-Pre/2013.6.1_13.00.00/170_mapping.mpf"));
////		dataModel.addVideoFile(new File("/Users/caltrans/Desktop/Chico Pre-Collision/Results-Pre/2013.6.1_13.00.00/NB_Segment-2013-06-01-13-00-07.mov"));
////		dataModel.addVideoFile(new File("/Users/caltrans/Desktop/Chico Pre-Collision/Results-Pre/2013.6.1_13.00.00/SB_Segment-2013-06-01-13-00-07.mov"));
//				
//		// C1 TESTING
//		// Temporary Methods to set video file and data file automatically
////		dataModel.setC1Data(new File("/Users/caltrans/Desktop/Hwy 50 Test/Results/Collection 1/c1data.c1"));
////		dataModel.addVideoFile(new File("/Users/caltrans/Desktop/Hwy 50 Test/Results/Collection 1/CIMG0269_rot.mov"));
//	}
//
//	private String getVLCPath()
//	{
//		String vsLibLoc;
//		String vlcLibLoc = null;
//
//		// Before presenting any of the views for VideoSync, we need to ensure that all of the preferences and necessary
//		// components have been installed & located before they are presented. If they have not been installed or located,
//		// we need to either create them, or ask the user to locate them.
//		try 
//		{
//			// Get the name of the os for determining the URL patterns that are going to be used.		
//			String systemType = System.getProperty("os.name");
//			
//			if (systemType.startsWith("Mac"))
//			{
//				vsLibLoc = System.getProperty("user.home") + "/Library/VideoSync";
//				vlcLibLoc = "/Applications/VLC.app/Contents/MacOS/lib/";
//			}
//			else if (systemType.startsWith("Windows"))
//			{
////				TODO: Fill in path to store application information for VideoSync
//				vsLibLoc = "";
//				vlcLibLoc = "";
//			}
//			else // Linux
//			{
////				TODO: Fill in path to store application information for VideoSync
//				vsLibLoc = "";
//				vlcLibLoc = "";
//			}
//	
//			// Stores the final location of VLC - this gets passed to the VideoPlayer via the DataModel 
//			// so that it can be loaded with the NativeLibraries
//			File vlc = null;
//			
//			// Temporarily stores the library during startup so we can either validate is existence or create it.
//			File library = new File(vsLibLoc);
//			
//			// Temporarily stores the VideoSync preferences file so we can write the location of the VLC installation
//			File prefsFile = new File(vsLibLoc + "/videosync.pref");
//			
//			// Stores the location of the log's directory so that any log files that get written out can be stored
//			File logsFile = new File(vsLibLoc + "/logs/");
//
//			// Temporarily stores the path of the VLC installation so it can be written to the prefs file.
//			String vlcPath = null;
//
//			// Starts up the LoggerThread so that we can easily log any messages that might help in diagnosing problems while in testing.
//			LoggerThread lt = new LoggerThread("LoggerThread", vsLibLoc + "/logs/");
//
//			// Verify that the library location exists. 
//			// If so, confirm that the VLC path stored in the preference file is still valid
//			// If not, we'll create the preferences file and search for the VLC installation.
//			if (library.exists())
//			{
//				// Start the Logging Thread so it may begin receiving log files.
//				lt.start();
//	
//				sendMessageToLogManager(new LogItem("Startup", Calendar.getInstance().getTime().toString(), "VideoSync Library found at " + vsLibLoc, "Notice"));
//				
//				// Verify that there is a preferences file.
//				// If there is one, we'll read the location of the VLC installation and store it into vlcPath for use
//				if (prefsFile.exists())
//				{
//					vlcPath = new BufferedReader(new FileReader(prefsFile)).readLine();
//				}
//				
//				// Check to make sure we have a valid vlcPath and and that the path exists.
//				// If it does, we can continue on with as no other checking needs to be done.
//				// If it either of the two fail, we need to request input from the user to correct the issue.
//				if (vlcPath == null || !new File(vlcPath).exists())
//				{
//					// Reset the vlcPath to null since it may be incorrect.
//					vlcPath = null;
//					
//					// Request the user to select their VLC installation. The result gets stored into VLC
//					vlc = handleNoVLCInstall();
//					
//					// Update the preferences file so that it may be used the next time VideoSync runs
//					// If we failed to create the preferences file, send a notice to the log manager.
//					if (!createPrefsFile(prefsFile, vlc.toString(), vlcLibLoc))
//					{
//						sendMessageToLogManager(new LogItem("Startup", Calendar.getInstance().getTime().toString(), "Failed to create VideoSync Preferences File at " + prefsFile.getPath(), "ERROR"));
//					}					
//				}
//			}
//			// We did not find a library file so we need to create one and search for th VLC installation
//			else
//			{
//				// Create the Library & Log File Directories
//				library.mkdir();
//				logsFile.mkdir();
//				
//				// Start the Logging Thread so it may begin receiving log files.
//				lt.start();
//				
//				sendMessageToLogManager(new LogItem("Startup", Calendar.getInstance().getTime().toString(), "VideoSync Library not found: Created Library & Log Files at " + vsLibLoc, "Notice"));
//				sendMessageToLogManager(new LogItem("Startup", Calendar.getInstance().getTime().toString(), "Searching for VLC Application in /Applications/", "Notice"));
//					
//				// Check to see if VLC has been installed and stores the result into vlc.
//				// -- passes the system type so we can use the correct file URIs
//				vlc = isVLCInstalled(systemType);
//
//				// If VLC is not null, indicating we found a valid installation, we can continue on and create the prefs file.
//				if (vlc != null)
//				{
//					sendMessageToLogManager(new LogItem("Startup", Calendar.getInstance().getTime().toString(), "Found VLC Application at " + vlc.getPath(), "Notice"));
//					sendMessageToLogManager(new LogItem("Startup", Calendar.getInstance().getTime().toString(), "Creating VideoSync Preferences File at " + prefsFile.getPath(), "Notice"));
//						
//					
//					// Create the preferences file so that it may be used the next time VideoSync runs
//					// If we failed to create the preferences file, send a notice to the log manager.
//					if (!createPrefsFile(prefsFile, vlc.toString(), vlcLibLoc))
//					{
//						sendMessageToLogManager(new LogItem("Startup", Calendar.getInstance().getTime().toString(), "Failed to creat VideoSync Preferences File at " + prefsFile.getPath(), "ERROR"));
//					}
//				}
//				// We failed to find VLC on our own, so we need to search for it.
//				else
//				{
//					sendMessageToLogManager(new LogItem("Startup", Calendar.getInstance().getTime().toString(), "No VLC Installation Found. Requesting user to install/download VLC", "Warning"));
//				
//					int attempts = 0;
//					boolean finished = false;
//					
//					// As long as we have not found a VLC installation, we need to keep asking for it.
//					while (!finished)
//					{
//						// Request the user to locate their VLC installation and store the result into vlc.
//						vlc = handleNoVLCInstall();
//						
//						// If VLC is null, send a message to the log manager indicating that a vlc installation was not found.
//						if (vlc == null)
//						{
//							sendMessageToLogManager(new LogItem("Startup", Calendar.getInstance().getTime().toString(), "User failed to install VLC.", "ERROR"));
//							
//							// If we can't find an installation in 3 attempts, we will notify the user and quit.
//							if (attempts == 3)
//								System.exit(-1);
//						}
//						
//						// If the VLC location exists, we can stop with this loop and start up the program.
//						if (vlc.exists())
//						{
//							finished = true;
//						}
//						
//						attempts++;
//					}
//					
//					sendMessageToLogManager(new LogItem("Startup", Calendar.getInstance().getTime().toString(), "User Installed VLC at " + vlc.getAbsolutePath(), "Warning"));
//					sendMessageToLogManager(new LogItem("Startup", Calendar.getInstance().getTime().toString(), "Creating VideoSync Preferences File at " + prefsFile.getPath(), "Notice"));
//					
//					// Create the preferences file so that it may be used the next time VideoSync runs
//					// If we failed to create the preferences file, send a notice to the log manager.
//					if (!createPrefsFile(prefsFile, vlc.toString(), vlcLibLoc))
//					{
//						sendMessageToLogManager(new LogItem("Startup", Calendar.getInstance().getTime().toString(), "Failed to creat VideoSync Preferences File at " + prefsFile.getPath(), "ERROR"));
//					}					
//				}
//			}
//				
//			// This ensures we use the latest information regarding the vlcPath.
//			// This only runs if the vlcPath stored in the prefs file was incorrect
//			// or the location of VLC has changed.
//			if (vlc != null || vlcPath == null || vlcPath == "")
//			{
//				vlcPath = vlc.getAbsolutePath() + vlcLibLoc;
//			}
//		} 
//		catch (FileNotFoundException e) 
//		{
//			StringWriter errors = new StringWriter();
//			e.printStackTrace(new PrintWriter(errors));
//
//			sendMessageToLogManager(new LogItem("Errors", Calendar.getInstance().getTime().toString(), e.getMessage() + " - Stack Trace: " + e, "ERROR"));
//		} 
//		catch (IOException e) 
//		{
//			StringWriter errors = new StringWriter();
//			e.printStackTrace(new PrintWriter(errors));
//
//			sendMessageToLogManager(new LogItem("Errors", Calendar.getInstance().getTime().toString(), e.getMessage() + " - Stack Trace: " + e, "ERROR"));
//		} 
//		
//		
//		return vlcLibLoc;
//	}
//	
//	//-------------------------------------------------------------------------------------------------------------------------------------
//	//-------------------------------------------------------------------------------------------------------------------------------------
//	// -- VideoSync Startup Methods 
//
//	/**
//	 * Creates the preferences file for storing the VLC installation location.
//	 * @param prefsFile
//	 * @param vlcLoc
//	 * @param vlcLib
//	 * @return
//	 */	
//	private static boolean createPrefsFile(File prefsFile, String vlcLoc, String vlcLib)
//	{
//		try 
//		{
//			// Ensure that the Library file has been created.
//			// If not attempt to create it.
//			// IF it cannot be created, return false.
//			if (!prefsFile.getParentFile().exists())
//				if (!prefsFile.getParentFile().mkdir())
//				{
//					sendMessageToLogManager(new LogItem("Startup", Calendar.getInstance().getTime().toString(), "Failed to create VideoSync Preferences File at " + prefsFile.getAbsolutePath(), "Notice"));
//
//					return false;
//				}
//
//			// Create a FileWriter object so we can write the prefs file
//			FileWriter fw = new FileWriter(prefsFile);
//			
//			// Write the VLC Location & VLC Library to the preferences file.
//			fw.write(String.format("%s%s", vlcLoc, vlcLib));
//			
//			// Close the FileWriter for the prefs file.
//			fw.close();
//			
//			// Write the sytem information so we it can potentially be used for debugging information
//			writeSystemInfo(new File(prefsFile.getParent() + "/system.info"));
//		}
//		catch (IOException e) 
//		{
//			StringWriter errors = new StringWriter();
//			e.printStackTrace(new PrintWriter(errors));
//
//			sendMessageToLogManager(new LogItem("Errors", Calendar.getInstance().getTime().toString(), e.getMessage() + " - Stack Trace: " + e, "ERROR"));
//
//			return false;
//		} 
//		
//		// Return true indicating success
//		return true;
//	}
//	
//	/**
//	 * Write the system information to a file so it can be included in error reporting
//	 * @param file
//	 * @return
//	 */
//	private static boolean writeSystemInfo(File sysFile)
//	{
//		sendMessageToLogManager(new LogItem("Startup", Calendar.getInstance().getTime().toString(), "Writing System Information at " + sysFile.getAbsolutePath(), "Notice"));
//		
//		try 
//		{
//			FileWriter fw = new FileWriter(sysFile);
//			fw.write("OS Name:\t\t\t" 						+ System.getProperty("os.name") + "\n");
//			fw.write("OS Version:\t\t\t" 					+ System.getProperty("os.version") + "\n");
//			fw.write("OS Architecture:\t" 					+ System.getProperty("os.arch") + "\n");
//			fw.write("Java Class Path:\t" 					+ System.getProperty("java.class.path") + "\n");
//			fw.write("Java Home: \t\t\t" 					+ System.getProperty("java.home") + "\n");
//			fw.write("Java Vendor:\t\t" 					+ System.getProperty("java.vendor") + "\n");
//			fw.write("Java Version:\t\t" 					+ System.getProperty("java.version") + "\n");
//			fw.write(" ----- System Information ------\n");
//			fw.write("Number of Cores: \t\t\t\t" 			+ Runtime.getRuntime().availableProcessors() + "\n");
//		    long maxMemory = Runtime.getRuntime().maxMemory();
//		    fw.write("Maximum memory for JVM (bytes):\t" 	+  (maxMemory == Long.MAX_VALUE ? "no limit" : maxMemory) + "\n");
//		    fw.write("Total Memory for JVM (bytes):\t" 		+ Runtime.getRuntime().totalMemory() + "\n");
//			fw.close();
//		} 
//		catch (IOException e) 
//		{
//			sendMessageToLogManager(new LogItem("Startup", Calendar.getInstance().getTime().toString(), "Failed to write System Information at " + sysFile.getAbsolutePath(), "Notice"));
//
//			StringWriter errors = new StringWriter();
//			e.printStackTrace(new PrintWriter(errors));
//
//			sendMessageToLogManager(new LogItem("Errors", Calendar.getInstance().getTime().toString(), e.getMessage() + " - Stack Trace: " + e, "ERROR"));
//
//			// Because we had an error, we need to return false.
//			return false;
//		}
//
//		// Return true indicating success
//		return true;
//	}
//	
//	/**
//	 * Checks to make sure that VLC is installed and able to be found. This method only runs if its 
//	 * the first time VideoSync has been run.
//	 * 
//	 * @param systemType
//	 * @return
//	 */
//	private static File isVLCInstalled(String systemType)
//	{
//		File applicationDirectory;
//		
//		// Open the default directories with the correct URI's for each type of OS
//		if (systemType.startsWith("Mac"))
//		{
//			applicationDirectory = new File("/Applications");
//		}
//		else if (systemType.startsWith("Windows"))
//		{
////			TODO: Fill in path to store application information for VideoSync
//			applicationDirectory = new File("");
//		}
//		else // Linux
//		{
////			TODO: Fill in path to store application information for VideoSync
//			applicationDirectory = new File("");
//		}
//		
//		// Store all the applications into a temporary array.
//		File apps[] = applicationDirectory.listFiles();
//		
//		// Iterate through all of the Applications found and search for VLC 
//		for (File app : apps)
//		{
//			// If the name of the application starts with VLC, we can stop searching
//			// and return the file for use in main();
//			if (app.getName().startsWith("VLC"))
//				return app;
//		}
//		
//		// If we did not find a valid file, we return null.
//		return null;
//	}
//	
//	/**
//	 * Handles the user interaction for dealing with No Valid VLC installation
//	 * @return
//	 */
//	private static File handleNoVLCInstall()
//	{
//		try 
//		{
//			Object[] options = {"Download VLC", "Select Installation", "Quit VideoSync"};
//			
//			// Show na option pane and get the result of their input.
//			// Because JOptionPane requires a parent component to display the alert, we just create an empty JFrame so it will be displayed. 
//			int n = JOptionPane.showOptionDialog(new JFrame(),
//												"We were unable to find your VLC Installation. What would you like to do?",
//												null,
//												JOptionPane.YES_NO_CANCEL_OPTION,
//												JOptionPane.QUESTION_MESSAGE,
//												null,
//												options,
//												options[2]);
//			
//			// User wants to download VideoSync
//			if (n == 0)
//			{
//				// Notify the user that they are being redirected to VLC's home page so they can install VLC.
//				JOptionPane.showMessageDialog(new JFrame(), "You will be redirected to VLC's download page.\nWhen the installation is complete, restart VideoSync");
//				
//				// Open the default web browser to install vlc
//				Desktop.getDesktop().browse(new URI("http://www.videolan.org/vlc/"));
//				
//				sendMessageToLogManager(new LogItem("Startup", Calendar.getInstance().getTime().toString(), "User is downloading VLC from the web", "Notice"));
//				
//				// Sleep for 2 seconds to allow the log files to be finished writing and then exit.
//				Thread.sleep(2000);
//				System.exit(1);
//			}
//			// User wants to Select their VLC Installation
//			else if (n == 1)
//			{
//				sendMessageToLogManager(new LogItem("Startup", Calendar.getInstance().getTime().toString(), "User is selecting their VLC Installation.", "Notice"));
//				
//				// Present a JFileChooser so the user may select their VLC installation.
//				JFileChooser fc = new JFileChooser();
//				
//				// Allow Files to be selected
//				// TODO: NOTE - This may need to change depending on OS 
//				// 			- Mac applications are "files", while Windows applications should be a folder location and not the .exe
//				fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
//				
//				if (fc.showOpenDialog(new JFrame()) == JFileChooser.APPROVE_OPTION)
//				{
//					// Return the file the user selected.
//					return fc.getSelectedFile();
//				}
//			}
//			// User wants to quit
//			else
//			{
//				sendMessageToLogManager(new LogItem("Startup", Calendar.getInstance().getTime().toString(), "User elected not to install VLC. Quitting VideoSync", "Notice"));
//				
//				// Sleep for 2 seconds to allow the log files to be finished writing and then exit.
//				Thread.sleep(2000);
//				System.exit(1);
//			}
//		}
//		catch (InterruptedException e) 
//		{
//			StringWriter errors = new StringWriter();
//			e.printStackTrace(new PrintWriter(errors));
//
//			sendMessageToLogManager(new LogItem("Errors", Calendar.getInstance().getTime().toString(), e.getMessage() + " - Stack Trace: " + e, "ERROR"));
//		} 
//		catch (IOException e) 
//		{
//			StringWriter errors = new StringWriter();
//			e.printStackTrace(new PrintWriter(errors));
//
//			sendMessageToLogManager(new LogItem("Errors", Calendar.getInstance().getTime().toString(), e.getMessage() + " - Stack Trace: " + e, "ERROR"));
//		} 
//		catch (URISyntaxException e) 
//		{
//			StringWriter errors = new StringWriter();
//			e.printStackTrace(new PrintWriter(errors));
//
//			sendMessageToLogManager(new LogItem("Errors", Calendar.getInstance().getTime().toString(), e.getMessage() + " - Stack Trace: " + e, "ERROR"));
//		}
//		
//		return null;
//	}
//	
//	/**
//	 * Sends a new Log Item to the LoggerThread so it can be written to the appropriate Log File
//	 * @param li
//	 */
//	private static void sendMessageToLogManager(LogItem li)
//	{
//		try 
//		{
//			// Acquire access to the LoggerThread
//			LoggerThread.acquireAccess();
//			
//			// Send the LogItem to the LoggerThread
//			LoggerThread.addToList(li);
//			
//			// Release Access to the LoggerThread
//			LoggerThread.releaseAccess();
//		}
//		catch (InterruptedException e) 
//		{
//			e.printStackTrace();
//		}
//	}
//}
