package jqian.slicer.view.global;

import java.io.File;
import java.util.*;
import jqian.util.metric.LineCounter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Tree;

public class SliceDistributionBar extends DistributionDisplayBar {
	//protected int _projectLines;
	protected int _maxFileLines;
	protected int _maxSlicedLinesInFile;
	protected ISliceDistribution _distribution;
		
	public SliceDistributionBar(Tree tree,int barLocatingColumn){
		super(tree,barLocatingColumn,tree.getShell().getDisplay().getSystemColor(SWT.COLOR_CYAN));
		//_projectLines = LineCounter.countProjectLines(projectRoot);		
	}
	
	public void setProject(File projectRoot,ISliceDistribution distribution){
		this._distribution = distribution;
		
		_maxFileLines = LineCounter.countMaxFileLinesInProject(projectRoot);		
		_maxSlicedLinesInFile = countMaxSliceInFile(projectRoot);
		
		_file2Info.clear();
	}
	
	private static boolean isJavaFile(String name){
		int length = name.length();			
		String ext = "";
		
		name = name.toLowerCase();			
		if(length>5){
			ext = name.substring(length-5);	
		}
		
		if(ext.equals(".java"))
			return true;
		else
			return false;
	}
	
	public int countMaxSliceInFile(File projectRoot){
		int line = 0;
		if(!projectRoot.isDirectory()){
			if(isJavaFile(projectRoot.getName()))
			    return _distribution.getLines(projectRoot).size();
			else
				return 0;
		}				
		
		//a directory
		File[] contents = projectRoot.listFiles();
		int size = contents.length;		
		for(int i=0;i<size;i++){
			File item = contents[i];
			int t = 0;
			if(item.isDirectory()){
				t = countMaxSliceInFile(item);						
			}
			else if(isJavaFile(item.getName())){									
				t = _distribution.getLines(item).size();				
			}
			
			line = (t>line)? t: line;			
		}
		
		return line;
	}
	
	public int getFileLineCount(Object itemdata){
		if(itemdata instanceof File){
			File file = (File)itemdata;
			FileInfo info = getFileInfo(file);			
			return info._lines;
		}
		else{
			return 0;
		}
	}
	
	public int getSliceLineCount(Object itemdata){
		if(itemdata instanceof File){
			File file = (File)itemdata;
			FileInfo info = getFileInfo(file);
			if(info._sliceLines==null)
				return 0;
			else
				return info._sliceLines.size();
		}
		else{
			return 0;
		}
	}
	
	
	@Override
	protected String getBarText(Object itemdata) {
		if(itemdata instanceof File){
			File file = (File)itemdata;
			FileInfo info = getFileInfo(file);
			
			if(info._lines==0 ||info._sliceLines.size()==0){
				return "";
			}
			
			float percent = ((float)(info._sliceLines.size()))/info._lines;
			return percentToString(percent);
		}
		else{
			return "";
		}
	}

	@Override
	protected int getBarWidth(Object itemdata) {
		int width = 0;	
		
		if(itemdata instanceof File){
			File file = (File)itemdata;
			FileInfo info = getFileInfo(file);
			//width = (info._lines*_columnWidth)/_maxFileLines;
			
			if(_maxSlicedLinesInFile>0){
				width = (info._sliceLines.size()*_columnWidth)/_maxSlicedLinesInFile;
			}
		}		
		
		return width;
	}
	
	@Override
	protected Collection<Float> getDistributions(Object itemdata){
		Collection<Float> slice = new LinkedList<Float>();
		
		if(!(itemdata instanceof File))
			return slice;
		
		File file = (File)itemdata;
		if(file.isDirectory())
			return slice;
		
		FileInfo info = getFileInfo(file);
		int totalLine = info._lines;
		
		Collection<Integer> sliceLines = info._sliceLines;			
		for(Iterator<Integer> it=sliceLines.iterator();it.hasNext();){
			float sline = (float)it.next();			
			float pos = sline/totalLine;
			slice.add(pos);
		}	
		
		return slice;
	}
	
	private FileInfo getFileInfo(File file){
		FileInfo info = _file2Info.get(file.getPath());
		if(info==null){
			info = new FileInfo();
			if(!file.isDirectory()){
			    info._lines = LineCounter.countFileLine(file);	
			    Collection<Integer> slices = _distribution.getLines(file);
			    if(slices==null){
			    	slices = Collections.emptySet();
			    }
			    
			    info._sliceLines = slices;			    
			}
			else{
				info._sliceLines = Collections.emptySet();
			}
			
			_file2Info.put(file.getPath(), info);
		}
		
		return info;	
	}
	
	private static class FileInfo{
		public int _lines;		
		public Collection<Integer> _sliceLines;
	}
	
	private Map<String,FileInfo> _file2Info = new HashMap<String,FileInfo>();
}


