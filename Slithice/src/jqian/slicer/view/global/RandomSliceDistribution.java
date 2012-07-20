package jqian.slicer.view.global;

import java.io.File;
import java.util.*;

import org.eclipse.core.resources.IFile;
import jqian.slicer.plugin.*;
import jqian.util.metric.*;


/**
 * @author bruteforce
 *
 */
public class RandomSliceDistribution implements ISliceDistribution {
	private static RandomSliceDistribution _instance = new RandomSliceDistribution();
	public static ISliceDistribution v(){
		return _instance;
	}
	
	private RandomSliceDistribution(){	
	}	

	public Collection<Integer> getLines(IFile ifile){
		File file = WorkbenchHelper.getFile(ifile);
		return getLines(file);
	}
	
	public Collection<Integer> getLines(File file){
		Collection<Integer> slice = _file2slice.get(file);
		if(slice==null){
			slice = computeRandomSlice(file);
			_file2slice.put(file, slice);
		}
		return slice;
	}
	
	public Collection<Integer> getLines(String method){
		return null;
	}
	
	private Collection<Integer> computeRandomSlice(File file){
		Collection<Integer> out = new LinkedList<Integer>();
		int line = LineCounter.countFileLine(file);
		//keep about slicePercent of the whole file as slice
		double slicePercent = Math.random();
		if(slicePercent<(2.00/line)){
			slicePercent = 0;
			return out;
		}
		
		for(int i=0;i<line;i++){
			double d = Math.random();
			if(d<slicePercent){
				out.add(i);
			}
		}
		
		return out;
	}
	
	private Map<File,Collection<Integer>> _file2slice = new HashMap<File,Collection<Integer>>();
}
