package jqian.util.metric;

import java.io.*;

/**
 */
public class LineCounter {
	public static int countProjectLines(File projectRoot){
		int lineCount = 0;
		if(!projectRoot.isDirectory()){
			if(isJavaFile(projectRoot.getName()))
			    return countFileLine(projectRoot);
			else
				return 0;
		}				
		
		//a directory
		File[] contents = projectRoot.listFiles();
		int size = contents.length;		
		for(int i=0;i<size;i++){
			File item = contents[i];
			if(item.isDirectory()){
				lineCount += countProjectLines(item);
			}
			else if(isJavaFile(item.getName())){									
				lineCount += countFileLine(item);				
			}
		}
		
		return lineCount;
	}
	
	public static int countMaxFileLinesInProject(File projectRoot){
		int line = 0;
		if(!projectRoot.isDirectory()){
			if(isJavaFile(projectRoot.getName()))
			    return countFileLine(projectRoot);
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
				t = countMaxFileLinesInProject(item);						
			}
			else if(isJavaFile(item.getName())){									
				t = countFileLine(item);				
			}
			
			line = (t>line)? t: line;			
		}
		
		return line;
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
	
	
	public static int countFileLine(File file){
		BufferedReader in = null;
		int numLines = 0;
		
		try {
			FileReader fileReader = new FileReader(file);
			in = new BufferedReader(fileReader);
			
			String line;
			do {
				line = in.readLine();
				if (line != null && isSolidJavaLine(line)){
					numLines++;					
				}
			}
			while (line != null);		

		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
			}
		}
		
		return numLines;
	}
	
	/**
	 * TODO currently no comments is filter out
	 */
	public static boolean isSolidJavaLine(String line){
		return true;		
	}
}
