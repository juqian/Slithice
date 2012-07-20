package jqian.util.eclipse;

import org.eclipse.jdt.core.*;

public interface JavaElementFilter {
	/**
	 * Determine whether a leaf element should be displayed
	 * One can using this to avoid displaying IFields or IMethods
	 * @param element
	 * @return
	 */
	public boolean isLeafDisplayed(IJavaElement element);
	
	/**
	 * Determine whether library code should be displayed
	 * @return
	 */
	public boolean showLibraryCode();
}
