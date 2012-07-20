package jqian.slicer.view.global;

import java.util.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Tree;

/**
 * @author bruteforce
 * A bar located on a tree item to display the distribution of sth.
 */
public abstract class DistributionDisplayBar {
	protected int _barLocatingColumn; //The column where the bar locates
	protected Tree _tree;
	protected Listener _paintListener;
	protected int _columnWidth;
	protected Display _display;
	protected Color _distributionColor;
	
	/**
	 *
	 * @param tree
	 * @param barLocatingColumn
	 */
	protected DistributionDisplayBar(Tree tree,int barLocatingColumn,Color distributionColor){
		this._tree = tree;
		this._barLocatingColumn = barLocatingColumn;
		this._paintListener = new PaintListener(); 
		this._columnWidth = tree.getColumn(barLocatingColumn).getWidth();
		this._display = tree.getShell().getDisplay();
		this._distributionColor = distributionColor;
	}
	
	public void enable(){
		/*
		 * NOTE: MeasureItem, PaintItem and EraseItem are called repeatedly.
		 * Therefore, it is critical for performance that these methods be
		 * as efficient as possible.
		 */
		_tree.addListener(SWT.PaintItem,_paintListener);	
	}
	
	public void disable(){
		_tree.removeListener(SWT.PaintItem,_paintListener);	
	}
	
	/**
	 * Determine the length of the colored bar
	 * @param itemdata The itemdata corresponds to a tree/table item	 
	 */
	protected abstract int getBarWidth(Object itemdata);	
	
	/**
	 * Determine the text displayed in colored bar
	 * @param itemdata The itemdata corresponds to a tree/table item	 
	 */
	protected abstract String getBarText(Object itemdata);
	
	/** 
	 * The distributions are represented as a couple of relevant positions	in percentage
	 * @param itemdata The itemdata corresponds to a tree/table item	
	 */
	protected abstract Collection<Float> getDistributions(Object itemdata);
	
	
	protected void drawDistribution(GC gc,Object itemdata,int barLeft,int barTop,int barHeight){
		int barWidth = getBarWidth(itemdata);
		
		Color original = gc.getForeground();		
		gc.setForeground(_distributionColor);
		
		Collection<Float> distibutes = getDistributions(itemdata);
		for(Iterator<Float> it=distibutes.iterator();it.hasNext();){
			Float pos = it.next();
			drawDistributionLine(gc,barLeft,barTop,barWidth,barHeight,pos);
		}
		
		gc.setForeground(original);
	}
	
	protected static void drawDistributionLine(GC gc,int barLeft,int barTop,int barWidth, int barHeight, float relevantPos){
		int position = (int)(barWidth * relevantPos);
		gc.drawLine(barLeft+position, barTop, barLeft+position, barTop+barHeight);		
	}
	
	
	private class PaintListener implements Listener{		
		public void handleEvent(Event event) {
			if (event.index == _barLocatingColumn) {
				TreeItem item = (TreeItem)event.item;
				Object data = item.getData();
				
				int width = getBarWidth(data);
				//float percent = getSlicePercentage(data);					

				GC gc = event.gc;

				Color foreground = gc.getForeground();
				Color background = gc.getBackground();

				
				gc.setForeground(_display.getSystemColor(SWT.COLOR_RED));
				gc.setBackground(_display.getSystemColor(SWT.COLOR_YELLOW));

				//draw a file size diaplay bar
				gc.fillGradientRectangle(event.x, event.y, width, event.height,true);
				Rectangle rect2 = new Rectangle(event.x,event.y,width-1,event.height-1);
				gc.drawRectangle(rect2);
				gc.setForeground(_display.getSystemColor(SWT.COLOR_LIST_FOREGROUND));

				//draw slices on the file size display bar
				drawDistribution(gc, data, event.x, event.y, event.height);

				//display the percentage text					
				String text = getBarText(data);
				Point size = event.gc.textExtent(text);
				int offset = Math.max(0, (event.height - size.y) / 2);
				gc.drawText(text, event.x + 2, event.y + offset, true);

				gc.setForeground(background);
				gc.setBackground(foreground);				
			}
		}
	}
	
	public static String percentToString(float percent){
		int high = (int)(percent*100);
		int low = (int)(percent*10000)%100;
		String text = ""+high+"."+low+"%";
		return text;
	}
}
