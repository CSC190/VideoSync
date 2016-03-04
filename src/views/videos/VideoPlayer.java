/**
 * ****************************************************************
 * File: 			VideoPlayer.java
 * Date Created:  	June 28, 2013
 * Programmer:		Dale Reed
 * 
 * Purpose:			To handle and control all aspects of the video
 * 					player on its own and to receive updates from 
 * 					the data model as outside events request the 
 * 					video file to update positions, times, etc...
 * 
 * ****************************************************************
 */
package views.videos;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JFrame;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.JWindow;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import models.DataModel;
import models.DataModelProxy;
import uk.co.caprica.vlcj.binding.internal.libvlc_media_t;
import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.MediaPlayerEventListener;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import uk.co.caprica.vlcj.runtime.RuntimeUtil;

import com.sun.jna.NativeLibrary;
import com.sun.jna.platform.unix.X11.Window;

import javax.swing.JPanel;
import javax.swing.JLabel;

import java.awt.GridLayout;


public class VideoPlayer extends JFrame implements KeyListener, MediaPlayerEventListener, ComponentListener, ChangeListener, WindowListener, Observer, MouseMotionListener, MouseWheelListener, MouseListener
{		
	private static final long serialVersionUID = -1540486086323956431L;

	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- Video Player Variable Declarations
	
	/**
	 * Used for keeping track if the player is considered the 'Master Player'
	 */
	private int playerID;
	
	/** 
	 * Used with the master play to notify the Data Model when the time changed notice gets fired.
	 */
	private DataModelProxy dmp;
	
	/**
	 * Used in frameAdvance() for ensuring that the appropriate number of time has elapsed between frame jumps
	 */
	private long lastSystemTime;

	/**
	 * Keeps track of the video file used with this player.
	 */
	private File videoFile;
	
	/**
	 * VLCJ Media Components required for video playback.
	 */
	private EmbeddedMediaPlayer mediaPlayer;
	private EmbeddedMediaPlayerComponent mediaPlayerComponent;

	/**
	 * Stores the time parameter passed to the constructor on startup for jumping the video to that point in time. 
	 * This is so that if the video is loaded after we have started analysis, we can get to an approximate position
	 * in the video that matches all the other time syncs.
	 */
	private long currentTime;
	
	/**
	 * Stores the time parameter sent by the constructor during startup.
	 */
	private long sentTime;

	/**
	 * Used for displaying the current time on the video player.
	 */
	private JLabel label_PlaybackTime;
	
	/**
	 * These variables are only used with the stand alone video player.
	 */
	private JSlider rateSlider;
	private JSlider positionSlider;
	private JTextField timeJumpField;
	private String jumpString;
	private JFrame mediaController;
	
	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- Video Player Construction 

	/**
	 * Creates a video player with the associated file. If no file is passed throw a NullPointerException
	 * 
	 * @param file
	 * @param currentTime
	 * @param playerID
	 */
	public VideoPlayer(String vlcPath, File file, long currentTime, int playerID, DataModelProxy dmp, DataModel dm)
	{
		// TODO: This should really be handled through the dmp and not directly with the data model...
		dm.addObserver(this);
		
		if (vlcPath == null) throw new NullPointerException("A Valid VLC Application must be available to run");
		if (file == null || !file.exists()) throw new NullPointerException("A Valid Video File was not passed to the Video Player.");
		
		// Set the playerID
		this.playerID = playerID;

		// Set the DataModelProxy object
		this.dmp = dmp;
		
		// Set the current video time to match what was sent from the data model. 
		// This is so that we can add a video at any point during playback and it should jump to the same point in time.
		this.currentTime = currentTime;
		
		// Set the time sent by the constructor
		this.sentTime = currentTime;

		// Set the video file to be played back.
		this.videoFile = file;
		
		// Create the video player layout and set it on the screen.
		createVideoPlayer(vlcPath, playerID);
		
		// Set the video file so it can be used with VLC
		setVideoFile();
		
		// Create the Video Controller and Key listener only for the standalone version.
		//createVideoController();
		lastSystemTime = System.currentTimeMillis();
		
		// Set the Video Player's Title
		this.setTitle(file.getName());
		
		this.addWindowListener(this);
		
		
		

		this.mediaPlayerComponent.getMediaPlayer().setEnableMouseInputHandling(true);
		this.mediaPlayerComponent.getVideoSurface().addMouseListener(this);
		this.mediaPlayerComponent.getVideoSurface().addMouseMotionListener(this);
		this.mediaPlayerComponent.getVideoSurface().addMouseWheelListener(this);
		
		this.mediaPlayerComponent.getVideoSurface().requestFocusInWindow();
	}

	/**
	 * Creates the video container to display the video file in
	 * 
	 * @param id
	 */
	public void createVideoPlayer(String vlcPath, int id)
	{					
		// Sets the Log Level for VLCJ. Only used if the -Dvlcj.log= system property has not already been set
//		final String VLCJ_LOG_LEVEL = "DEBUG";	
//		if (null == System.getProperty("vlcj.log"))
//		{
//			System.setProperty("vlcj.log", VLCJ_LOG_LEVEL);
//		}

		// Load up the native VLC library for the corresponding OS
		NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), vlcPath);

		// Create a media player component to display the video file within
		mediaPlayerComponent = new EmbeddedMediaPlayerComponent();
//		mediaPlayerComponent = new EmbeddedMediaPlayerComponent()
//		{
//		
//			@Override
//	        protected java.awt.Window onGetOverlay() 
//			{
//	            final JWindow transparentWindow = new JWindow();
//	
//	            // Set basic window opacity if required - the window system must support WindowTranslucency (i.e. PERPIXEL_TRANSLUCENT)!
////	            transparentWindow.setOpacity(0.8f);
//	            // White with transparent alpha channel - WindowTranslucency is required for translucency.
//	            transparentWindow.setBackground(new Color(1.0f, 1.0f, 1.0f, 1.0f));
//	
//	            final JLabel superImposedLightweigtLabel = new JLabel("Hello, VLC.", JLabel.CENTER);
//	            superImposedLightweigtLabel.setOpaque(true);
//	
//	            transparentWindow.getContentPane().add(superImposedLightweigtLabel);
//	            return transparentWindow;
//	        }
//		};
//		
//		mediaPlayerComponent.getMediaPlayer().enableOverlay(true);


		// Retrieve the media player from the component so that we can register some event listeners to the player
		mediaPlayer = mediaPlayerComponent.getMediaPlayer();
		mediaPlayer.addMediaPlayerEventListener(this);
		getContentPane().setLayout(new BorderLayout(0, 0));
		
		// Add the mediaPlayer to the layout's center of the JFrame
		getContentPane().add(mediaPlayerComponent);
		
		JPanel panel = new JPanel();
		getContentPane().add(panel, BorderLayout.SOUTH);
		panel.setLayout(new GridLayout(0, 1, 0, 0));
		
		label_PlaybackTime = new JLabel("Playback Time");
		panel.add(label_PlaybackTime);
				
//		// Temporary logic for setting the size/position of the player on the 2nd monitor
//		if (id == 1)
//		{
//			this.setSize(694, 452);
//			//this.setLocation(-694, 540);
//		}
//		else
//		{
//			this.setSize(694, 446);
//			//this.setLocation(-694, 993);
//		}
		
		// Make the video player visible.
		this.setVisible(true);
	}
	
	/**
	 * Set the video player's source to the video file that was used in the constructor
	 */
	public void setVideoFile()
	{
		// Set the player to quite mode and not print out any information as it plays.
		String[] options = {"quiet=true"};
		
		// Tell the instantiate with the video file and use the options included
		mediaPlayer.playMedia(videoFile.getAbsolutePath(), options);
		
		// This gives the player enough time to startup and get ready to go 
		// and also give enough time for "pause" to take effect. We pause after 
		// 400ms because when playMedia() is used, it immediately starts
		// playing the video files.
		try 
		{
			Thread.sleep(400);
		} 
		catch (InterruptedException e) 
		{
			e.printStackTrace();
		}
		
		// Pause the media player.
		mediaPlayer.pause();
		
		// Jump the video to the time sent by the constructor.
		jumpToTime(this.sentTime);

		// If the current time & sent times do not match, it probably means the video attempted to play
		// prematurely, so we need to ensure the video time matches with the time sent.
		if (this.currentTime != this.sentTime)
			this.currentTime = this.sentTime;
		
		// Occasionally this throws a NullPointerException which doesn't appear to be critical, so just silently
		// catch the error and continue on.
		try
		{
			// Display the video information for debugging information.
			System.out.println("-------------------------------- VIDEO INFORMATION --------------------------------");
			System.out.println(" - Video File: " + videoFile.getName());
			System.out.println(" - Video Path: " + videoFile.getPath());
			System.out.println(" - Video Length: " + mediaPlayer.getLength() + " ms");
			System.out.println(" - Video Size: " + mediaPlayer.getVideoDimension().width + "x" + mediaPlayer.getVideoDimension().height);
			System.out.println(" - Video Frame Rate: " + mediaPlayer.getFps());
			System.out.println(" - Video Time between frames: " + (1000 / mediaPlayer.getFps()));		
			System.out.println("-----------------------------------------------------------------------------------");
		}
		catch (NullPointerException npe)
		{
			
		}
		
		this.setVideoTimeLabel(mediaPlayer.getTime());
	}
			
	/**
	 * This is only used when using a static instance of the video controller.
	 * 
	 * <p>
	 * Creates a basic window to handle the sliders and jump to field for jumping the video around.
	 */
	public void createVideoController()
	{
		// Ensure that the video player is visible before creating the controller
		if (this.isVisible())
		{
			mediaController = new JFrame();
			mediaController.setVisible(true);
			mediaController.setSize(this.getWidth(), 100);
			mediaController.setLocation(this.getX(), (int)(this.getHeight() + this.getLocation().getY()));
			mediaController.getContentPane().setLayout(new FlowLayout());
			
			rateSlider = new JSlider(0, 5);
			rateSlider.setValue(0);
			rateSlider.setMajorTickSpacing(1);
			rateSlider.setSnapToTicks(true);
			rateSlider.addChangeListener(this);
			rateSlider.setName("Rate");
			
			positionSlider = new JSlider(0, (int) mediaPlayer.getLength());
			positionSlider.setValue(0);
			positionSlider.addChangeListener(this);
			positionSlider.setName("Position");
			
			timeJumpField = new JTextField();
			timeJumpField.setColumns(10);
			timeJumpField.addKeyListener(new KeyAdapter() 
			{    
		        public void keyReleased(KeyEvent e) 
		        {		        	
		        	jumpString = timeJumpField.getText();
		        	
		        	if (e.getKeyCode() == 10)
		        	{
			        	try
			        	{
			        		jumpToTime(Integer.parseInt(jumpString));
			        	}
			        	catch (NumberFormatException nfe)
			        	{
			        		System.err.println("Number Format Exception");
			        	}
		        	}
		        };
		    });
			
			mediaController.getContentPane().add(rateSlider);
			mediaController.getContentPane().add(positionSlider);
			mediaController.getContentPane().add(timeJumpField);
		}
	}

	
	/**
	 * The following functions pertain to accessing any of the private elements used with the Video Player
	 */
	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- Video Player Getter's & Setters

	/**
	 * Returns the player ID
	 * 
	 * @return playerID
	 */
	public int getPlayerID()
	{
		return this.playerID;
	}

	/**
	 * Sets the player ID. This is invoked when a video player has been removed and we need to re-assign one to 
	 * be the master player.
	 * @param id
	 */
	public void setPlayerID(int id)
	{
		this.playerID = id;
	}
	
	/**
	 * Returns the video file used for the container.
	 */
	public File getVideoFile()
	{
		return this.videoFile;
	}

	
	/**
	 * The following functions pertain directly to the control of the player, as well 
	 * as the access and setting of information to be used by the media player after 
	 * the media player has been setup and configured by the constructor.
	 */
	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- Media Player Event Functions
	
	/**
	 * Begin playing back the video from the current position
	 */
	public void playVideo()
	{
		mediaPlayer.play();
	}
	
	/**
	 * Pause the video at its current position.
	 * 
	 * <p>
	 * TODO: Remove the print statement
	 * After a 75 ms delay, print out the current time of the video.
	 */
	public long pauseVideo()
	{
		mediaPlayer.pause();

		try 
		{
			Thread.sleep(75);
		} 
		catch (InterruptedException e)
		{
			System.err.println(" -- Error sleeping for 75 ms in nextFrame()");
		}

		System.out.println("Media Player Time: " + mediaPlayer.getTime());
		
		return mediaPlayer.getTime();
	}

	/**
	 * Fast Forward or Rewind the video back by the specificed amount.
	 * 
	 * @param amount
	 */
	public void skipVideo(int amount)
	{
		mediaPlayer.skip(amount);
	}
	
	/**
	 * Advance the video by one frame
	 * 
	 * @return Returns the current time for use for other classes to determine where to sync up with the video at
	 */
	public long nextFrame()
	{		
		long currTime = System.currentTimeMillis();
		
		if (currTime - lastSystemTime >= 500)
		{
			System.out.println("\n **** nextFrame() ****");
			
			mediaPlayer.nextFrame();
		
			lastSystemTime = currTime;
			System.out.println("Player Time (ms): " + mediaPlayer.getTime());
		}		
		
		this.setVideoTimeLabel(mediaPlayer.getTime());
		
		return mediaPlayer.getTime();
	}

	/**
	 * Reverse the video by one frame
	 * 
	 * <p>
	 * FIXME: Fix the timings so that it will accurately move the video back by one frame. Currently moving 
	 * 		  the video backwards 1/2 second in time. This appears to vary based on the type of codec used.
	 * 		  For example, the Casio Camera's can only do 500ms jumps, while EvoCam Recordings can 
	 * 		  handle ~250ms jumps.
	 * 
	 * @return Returns the current time for use for other classes to determine where to sync up with the video at 
	 */
	public long previousFrame()
	{
		long currentTime = mediaPlayer.getTime();
		
		// Frame timing is calculated using 1000 / mediaPlayer.getFps() and done when the video loads. 
		// It is the same value as the last line in the video information block in the console text
		long newTime = (long) (currentTime - 500);

		System.out.println("\n**** previousFrame() ****");
		System.out.println("Time to jump to (ms): " + newTime);
		
		jumpToTime(newTime);
		
		this.setVideoTimeLabel(mediaPlayer.getTime());

		return newTime;
	}

	/**
	 * Jumps the video to a specific point in the video time.
	 * 
	 * @param time - Time since the beginning in milliseconds
	 */
	public void jumpToTime(long time)
	{
		System.out.println("\n **** jumpToTime(long time) ****");
		System.out.println("Jumping Video To Time (ms): " + time);
		
		mediaPlayer.setTime(time);
	}

	/**
	 * Adjusts the playback rate for the video. 
	 * 
	 * @param rate - 0.5 is half speed, 1.0 is normal speed, 2.0 is double speed, etc...
	 */
	public void setPlaybackSpeed(float rate)
	{
		mediaPlayer.setRate(rate);
	}

	/**
	 * Returns the playback rate for the video file.
	 * @return
	 */
	public float getPlaybackRate()
	{
		return mediaPlayer.getRate();
	}
	
	/**
	 * Returns the current video time in milliseconds since the start of the video.
	 * 
	 * @return The time to be returned
	 */
	public long getVideoTime() 
	{
		return mediaPlayer.getTime();
	}

	/**
	 * Update the label for the current video time.
	 * 
	 * @param time - Currently this is the time since the beginning in milliseconds
	 */
	private void setVideoTimeLabel(long time)
	{		 
		label_PlaybackTime.setText(convertToTimeFormat(time));
	}
	
	/**
	 * Returns the media player being used so we can access elements of it for use with the other Java classes
	 * 
	 * @return {@link EmbeddedMediaPlayer} - Returns the media player
	 */
	public EmbeddedMediaPlayer getMediaPlayer()
	{
		return this.mediaPlayer;
	}

	/**
	 * Notifies the model that the time has changed so that other time based components can be kept in sync.
	 */
	private void notifyModelOfTimeChange()
	{
		// If the player ID is 1, it gets to update the model with the current video time.
		if (playerID == 1)
		{
			// Perform notification to model of time change.
			dmp.setCurrentTime(this.mediaPlayer.getTime());
		}
	}
	
	/**
	 * Converts a time value into HH:MM:SS.sss format for display in the video player.
	 * @param msTime
	 * @return
	 */
	private String convertToTimeFormat(long msTime)
	{
		int millis = (int)(msTime - ((msTime / 1000) * 1000));
		int seconds = (int)(msTime / 1000);
		
		while (seconds > 59)
			seconds -= 60;
		
		int minutes = (int)(msTime / 1000) / 60;
		int hours = minutes / 60;
		
		return String.format("%02d:%02d:%02d.%03d", hours, minutes, seconds, millis);
	}
	
	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- Java Event Listeners 
	// -- NOTE: keyPressed and stateChanged are temporary listener while the Video player is being developed to full functionality

	public void update(Observable arg0, Object arg1) 
	{
		if (arg1 instanceof String)
		{
			if (((String)arg1).equals("Present"))
			{
				this.toFront();
			}
		}
	}

	public void keyPressed(KeyEvent ke) {
		
		if (ke.getKeyCode() == KeyEvent.VK_RIGHT)
			nextFrame();
		else if (ke.getKeyCode() == KeyEvent.VK_LEFT)
			previousFrame();
		else if (ke.getKeyCode() == KeyEvent.VK_SPACE)
		{
			if (mediaPlayer.isPlaying())
				pauseVideo();
			else
				playVideo();
		}
	}
	
	public void stateChanged(ChangeEvent ce) {
		
		if (ce.getSource() instanceof JSlider)
		{
			JSlider slider = (JSlider)ce.getSource();
			
			if (slider.getName() == "Rate")
			{
				int value = ((JSlider) ce.getSource()).getValue();
				
				switch (value)
				{
					case 0:
						setPlaybackSpeed((float) .25);
						break;
						
					case 1:
						setPlaybackSpeed((float) .50);
						break;
						
					case 2:
						setPlaybackSpeed((float) 1);
						break;
						
					case 3:
						setPlaybackSpeed((float) 2);
						break;
						
					case 4:
						setPlaybackSpeed((float) 4);
						break;
						
					case 5:
						setPlaybackSpeed((float) 8);
						break;
				}
			}
			else if (slider.getName() == "Position")
			{
				jumpToTime((long)slider.getValue());
			}
		}
	}

	/**
	 * Invoked when the component's position changes
	 */
	public void componentMoved(ComponentEvent ce) {
//			Point position = ce.getComponent().getLocation();
//			int w = ce.getComponent().getSize().width;
//			int h = ce.getComponent().getSize().height;
//	
//			System.out.printf("New Frame Information (x, y, w, h): (%d, %d, %d, %d)\n", position.x, position.y, w, h);
	}

	/**
	 * Invoked when then component's size changes
	 */
	public void componentResized(ComponentEvent ce) {
//			Point position = ce.getComponent().getLocation();
//			int w = ce.getComponent().getSize().width;
//			int h = ce.getComponent().getSize().height;
//	
//			System.out.printf("New Frame Information (x, y, w, h): (%d, %d, %d, %d)\n", position.x, position.y, w, h);
//			
	}

	/**
	 * Invoked when VLC notifies Java that the time has changed
	 * 
	 * <p>
	 * Note: this does not happen with each frame that changes. It only appears to happen around every few keyframes (approximately 300 milliseconds)
	 */
	public void timeChanged(MediaPlayer mp, long time) 
	{
		System.out.println("Time Change Detected " + time);
		
		// Notifies the model that the time has changed.
		notifyModelOfTimeChange();
		
		// Update the time label to the current time as it has now changed.
		setVideoTimeLabel(time);
	}
	
	/**
	 * Invoked when the window close button is pressed. 
	 */
	public void windowClosing(WindowEvent arg0) 
	{
		performShutdown(true);
	}	
	
	/**
	 * Performed when the VideoPlayer needs to be shut down - either by itself or by some external caller.
	 * It releases all VLC libraries before notifying the data model to unregister the video player.	
	 */
	public void performShutdown(boolean self)
	{
		// Hide the video player
		this.setVisible(false);
		
		// Release the Media Player & Media Player Components
		this.mediaPlayer.release();
		this.mediaPlayerComponent.release();

		// Dispose of all GUI components
		this.dispose();

		// Notify the Data Model that the video is going away only if the shutdown task was called by windowClosing
		if (self)
			this.dmp.unregisterVideo(this);
	}
	
	/**
	 * The following functions pertain to the various implementations that are currently not being used by the class.
	 */
	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- MediaPlayerEventListener methods 
	// -- NOTE: None of the following are currently implemented in this version

	public void backward(MediaPlayer arg0) {}

	public void buffering(MediaPlayer arg0, float arg1) {}

	public void endOfSubItems(MediaPlayer arg0) {}

	public void error(MediaPlayer arg0) {}

	public void finished(MediaPlayer arg0) {}

	public void forward(MediaPlayer arg0) {}

	public void lengthChanged(MediaPlayer arg0, long arg1) {}

	public void mediaChanged(MediaPlayer arg0, libvlc_media_t arg1, String arg2) {}

	public void mediaDurationChanged(MediaPlayer arg0, long arg1) {}

	public void mediaFreed(MediaPlayer arg0) {}

	public void mediaMetaChanged(MediaPlayer arg0, int arg1) {}

	public void mediaParsedChanged(MediaPlayer arg0, int arg1) {}

	public void mediaStateChanged(MediaPlayer arg0, int arg1) {}

	public void mediaSubItemAdded(MediaPlayer arg0, libvlc_media_t arg1) {}

	public void newMedia(MediaPlayer arg0) {}

	public void opening(MediaPlayer arg0) {}

	public void pausableChanged(MediaPlayer arg0, int arg1) {}

	public void paused(MediaPlayer arg0) {}

	public void playing(MediaPlayer arg0) {}

	public void positionChanged(MediaPlayer arg0, float arg1) {}

	public void seekableChanged(MediaPlayer arg0, int arg1) {}

	public void snapshotTaken(MediaPlayer arg0, String arg1) {}
	
	public void stopped(MediaPlayer arg0) {}

	public void subItemFinished(MediaPlayer arg0, int arg1) {}

	public void subItemPlayed(MediaPlayer arg0, int arg1) {}

	public void titleChanged(MediaPlayer arg0, int arg1) {}

	public void videoOutput(MediaPlayer arg0, int arg1) {}

	public void componentHidden(ComponentEvent arg0) {}

	public void componentShown(ComponentEvent arg0) {}

	public void keyReleased(KeyEvent arg0) {}
	
	public void keyTyped(KeyEvent arg0) {}

	public void windowActivated(WindowEvent arg0) {}

	public void windowClosed(WindowEvent arg0) {}

	public void windowDeactivated(WindowEvent arg0) {}

	public void windowDeiconified(WindowEvent arg0) {}

	public void windowIconified(WindowEvent arg0) {}

	public void windowOpened(WindowEvent arg0) {}

	
	

	
	
	/***********
	 * THIS IS THE BASICS FOR DRAWING ON THE CANVAS OF THE VIDEO PLAYER
	 * @author caltrans
	 * 
	 * NOTE: USED https://github.com/caprica/vlcj/blob/master/src/test/java/uk/co/caprica/vlcj/test/inputlistener/InputListenerTest.java AS THE BASIS FOR SETTING UP THE LISTENERS
	 */
	
	
	private int startX, startY;
	
	public void drawOnVideoPlayer()
	{
		System.out.println("Drawing on Video Player");
		
			
		
		
//		mediaPlayerComponent.getVideoSurface().paintAll(((Graphics2D)g).drawRect(30, 30, 50, 50));

		
	}
	
		

	@Override
	public void mouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
      System.out.println(arg0);
		
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
      System.out.println(arg0);
      this.drawOnVideoPlayer();
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	
//	private class MouseHandler extends MouseAdapter {
//
//        @Override
//        public void mouseClicked(MouseEvent event) {
//        	startX = event.getX();
//        	startY = event.getY();
//        }
//
//        @Override
//        public void mousePressed(MouseEvent event) {
//            System.out.println(event);
//        }
//
//        @Override
//        public void mouseReleased(MouseEvent event) {
//            System.out.println(event);
//        }
//
//        @Override
//        public void mouseDragged(MouseEvent event) {
////            System.out.println(event);
//        }
//
//        @Override
//        public void mouseEntered(MouseEvent event) {
////            System.out.println(event);
//        }
//
//        @Override
//        public void mouseExited(MouseEvent event) {
////            System.out.println(event);
//        }
//
//        @Override
//        public void mouseMoved(MouseEvent event) {
////            System.out.println(event);
//        }
//
//
//        @Override
//        public void mouseWheelMoved(MouseWheelEvent event) {
////            System.out.println(event);
//        }
//    }
}