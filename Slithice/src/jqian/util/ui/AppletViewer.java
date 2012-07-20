/* 
 * AppletViewer - a simple Applet Viewer program.
 * @author  
 *    originally by Ian Darwin, http://www.darwinsys.com/
 *    modified by Ju Qian {jqian@live.com}
 */
package jqian.util.ui;

import java.applet.Applet;
import java.applet.AppletContext;
import java.applet.AppletStub;
import java.applet.AudioClip;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Label;
import java.awt.Panel;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.JFrame;

/**
 * This class could be used to view applets, and also could be used as a harness
 * for statically analyzing Applet classes. 
 */
public class AppletViewer {
	/** The main Frame of this program */
	JFrame f;

	/** The AppletAdapter (gives AppletStub, AppletContext, showStatus) */
	static AppletAdapter aa = null;
	
	/** The Applet instance we are running, or null. Can not be a JApplet
	 * until all the entire world is converted to JApplet. */		
	Applet applet = null;

	/** The width and height of the Applet */
	final int WIDTH = 250;
	final int HEIGHT = 200;
	
	public static void main(String[] av) {
		AppletViewer view = new AppletViewer(null);
		view.show();
	}

	/** Construct the GUI for an Applet Viewer */
	public AppletViewer(Applet applet) {
		this.applet = applet;
		
		f = new JFrame("AppletViewer");
		f.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				f.setVisible(false);
				f.dispose();
				System.exit(0);
			}
		});
		Container cp = f.getContentPane();
		cp.setLayout(new BorderLayout());

		// Instantiate the AppletAdapter which gives us AppletStub and AppletContext.
		if (aa == null)
			aa = new AppletAdapter();

		// The AppletAdapter also gives us showStatus.
		// Therefore, must add() it very early on, since the Applet's
		// Constructor or its init() may use showStatus()
		cp.add(BorderLayout.SOUTH, aa);

		showStatus("Loading Applet " + applet.getClass());		
		applet.setSize(WIDTH, HEIGHT);
		
		// Now right away, tell the Applet how to find showStatus et al.
		applet.setStub(aa);

		// Connect the Applet to the Frame.
		cp.add(BorderLayout.CENTER, applet);

		Dimension d = applet.getSize();
		d.height += aa.getSize().height;
		f.setSize(d);
		f.setVisible(true); // make the Frame and all in it appear

		showStatus("Applet " + applet.getClass() + " loaded");
	}
	
	public void show(){
		// Here we pretend to be a browser!
		applet.init();
		applet.start();
	}

	public void showStatus(String s) {
		aa.getAppletContext().showStatus(s);
	}
}

/*
 * AppletAdaptor: partial implementation of AppletStub and AppletContext.
 *
 * This code is far from finished, as you will see.
 *
 * @author  Ian Darwin, http://www.darwinsys.com/, for Learning Tree Course 478
 */
class AppletAdapter extends Panel implements AppletStub, AppletContext {
	private static final long serialVersionUID = 1L;
	
	/** The status window at the bottom */
	Label status = null;

	/** Construct the GUI for an Applet Status window */
	AppletAdapter() {
		super();

		// Must do this very early on, since the Applet's
		// Constructor or its init() may use showStatus()
		add(status = new Label());

		// Give "status" the full width
		status.setSize(getSize().width, status.getSize().height);

		showStatus("AppletAdapter constructed"); // now it can be said
	}

	/****************** AppletStub ***********************/
	/** Called when the applet wants to be resized.  */
	public void appletResize(int w, int h) {
		// applet.setSize(w, h);
	}

	/** Gets a reference to the applet's context.  */
	public AppletContext getAppletContext() {
		return this;
	}

	/** Gets the base URL.  */
	public URL getCodeBase() {
		return getClass().getResource(".");
	}

	/** Gets the document URL.  */
	public URL getDocumentBase() {
		return getClass().getResource(".");
	}

	/** Returns the value of the named parameter in the HTML tag.  */
	public String getParameter(String name) {
		String value = null;
		return value;
	}

	/** Determines if the applet is active.  */
	public boolean isActive() {
		return true;
	}

	/************************ AppletContext ************************/

	/** Finds and returns the applet with the given name. */
	public Applet getApplet(String an) {
		return null;
	}

	/** Finds all the applets in the document */
	public Enumeration<Applet> getApplets() {
		class AppletLister implements Enumeration<Applet>  {
			public boolean hasMoreElements() {
				return false;
			}

			public Applet nextElement() {
				return null;
			}
		}
		return new AppletLister();
	}

	/** Create an audio clip for the given URL of a .au file */
	public AudioClip getAudioClip(URL u) {
		return null;
	}

	/** Look up and create an Image object that can be paint()ed */
	public Image getImage(URL u) {
		return null;
	}

	/** Request to overlay the current page with a new one - ignored */
	public void showDocument(URL u) {
	}

	/** as above but with a Frame target */
	public void showDocument(URL u, String frame) {
	}

	/** Called by the Applet to display a message in the bottom line */
	public void showStatus(String msg) {
		if (msg == null)
			msg = "";
		status.setText(msg);
	}

	/* StreamKey stuff - new in JDK1.4 */
	Map<String,InputStream> streamMap = new HashMap<String,InputStream>();

	/** Associate the stream with the key. */
	public void setStream(String key, InputStream stream) throws IOException {
		streamMap.put(key, stream);
	}

	public InputStream getStream(String key) {
		return (InputStream) streamMap.get(key);
	}

	public Iterator<String> getStreamKeys() {
		return streamMap.keySet().iterator();
	}
}
