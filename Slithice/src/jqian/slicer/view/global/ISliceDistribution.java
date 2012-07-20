package jqian.slicer.view.global;

import java.util.*;
import java.io.*;
import org.eclipse.core.resources.IFile;

/**
 * @author bruteforce
 * 
 */
public interface ISliceDistribution {
	public Collection<Integer> getLines(IFile ifile); 
	public Collection<Integer> getLines(File file); 
	public Collection<Integer> getLines(String method); 
}
