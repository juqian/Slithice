package jqian.slicer.view;

//import org.eclipse.jface.window.*;
import jqian.slicer.plugin.ID;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolTip;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tray;
import org.eclipse.swt.widgets.TrayItem;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.*; 

public class BalloonMessage {
	private static TrayItem item;
	
	private static void hideTrayIcon(){
		if(item!=null){
			item.setVisible(false);
		}
		//item.dispose();
	}
	
	public static void showMessage(Shell shell,String title,String msg){		
		Display display = shell.getDisplay();
		Tray tray = display.getSystemTray();
		
		if (tray == null) {
			ToolTip tip = new ToolTip(shell, SWT.BALLOON | SWT.ICON_INFORMATION);
			tip.setMessage(msg);
			tip.setText(title);
			tip.setLocation(100, 100);
			tip.setAutoHide(true);
			tip.setVisible(true);
			return;
		}
		
		ToolTip tip = null;
		if(item==null){
			item = new TrayItem(tray, SWT.NONE);
			Image image = display.getSystemImage(SWT.ICON_INFORMATION);
			item.setImage(image);	
			
			tip = new ToolTip(shell, SWT.BALLOON | SWT.ICON_INFORMATION);
			tip.setMessage(msg);
			tip.setText(title);
			item.setToolTip(tip);
		}
		else{			
			tip = item.getToolTip();
		}
		
		item.setVisible(true);
		tip.setVisible(true);
		tip.setAutoHide(true);	
			
		//hide tray icon
		tip.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				hideTrayIcon();
			}
			public void widgetSelected(SelectionEvent e) {
				hideTrayIcon();
			}
		});

		Listener listener =  new Listener() {
			public void handleEvent(Event event) {				
				hideTrayIcon();
			}
		};
		tip.addListener(SWT.Hide, listener);
		
		//tip.addDisposeListener(new DisposeListener() {
		//	public void widgetDisposed(DisposeEvent e) {}});
		//tip.addListener(SWT.Close,listener);
		//tip.addListener(SWT.Close,listener);
		
		
		//item.addListener(SWT.Close, listener);
		//item.addListener(SWT.Show, listener);
		//item.addListener(SWT.Hide, listener); 
		//item.addListener(SWT.MenuDetect, listener);
	}
	
	public static void main(String[] args) throws Exception{
		
		
		/*Shell shell = new Shell();
		
		
		
		//BalloonMessage.showMessage(shell,"Pyxis Slicer","Finishing the construction of dependence graph.");

		while(true){
			Thread.sleep(10000);
		}*/
		
		Display display = new Display();
		Shell shell = new Shell(display);	

		shell.pack();
		shell.open();

		BalloonMessage.showMessage(shell,ID.DISPLAY_NAME,"Finishing the construction of dependence graph.");

		// Set up the event loop.
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				// If no more entries in event queue
				display.sleep();
			}
		}

		display.dispose();
	}
}
