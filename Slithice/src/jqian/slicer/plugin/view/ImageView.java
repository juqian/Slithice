/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package jqian.slicer.plugin.view;

import jqian.slicer.plugin.ID;
import jqian.slicer.plugin.WorkbenchHelper;
import jqian.slicer.view.SWTImageCanvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;


/**
 * This ImageView class shows how to use SWTImageCanvas to 
 * manipulate images. 
 * <p>
 * To facilitate the usage, you should setFocus to the canvas
 * at the beginning, and call the dispose at the end.
 * <p>
 * @author Chengdong Li: cli4@uky.edu
 * @see uky.article.imageviewer.SWTImageCanvas
 */

public class ImageView extends ViewPart {
	public SWTImageCanvas imageCanvas;
	
	/**
	 * The constructor.
	 */
	public ImageView() {
	}
	
	public void loadImage(String filename){
		imageCanvas.loadImage(filename);
	}
	
	/**
	 * Create the GUI.
	 * @param frame The Composite handle of parent
	 */
	public void createPartControl(Composite frame) {
		imageCanvas=new SWTImageCanvas(frame);
	}

	/**
	 * Called when we must grab focus.
	 * @see org.eclipse.ui.part.ViewPart#setFocus
	 */
	public void setFocus() {
		imageCanvas.setFocus();
	}

	/**
	 * Called when the View is to be disposed
	 */
	public void dispose() {
		imageCanvas.dispose();
		super.dispose();
	}
	
	
	public static void showImage(final String file){
		class ShowGraph implements Runnable{
			public void run(){
				ImageView view = (ImageView) WorkbenchHelper.openView(ID.SDG_VIEW_ID);
				view.loadImage(file);
			}
		}
		
		IWorkbench workbench = PlatformUI.getWorkbench();
		workbench.getDisplay().syncExec(new ShowGraph());
	}

}