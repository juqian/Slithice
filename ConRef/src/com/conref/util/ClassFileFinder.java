/** 
 *  
 * 
 * 	@author Ju Qian {jqian@live.com}
 * 	@date 2011-1-25
 * 	@version
 */
package com.conref.util;

import java.util.ArrayList;
import java.util.Collection;

 
/**
 * Find in jar files or directories
 */
public class ClassFileFinder extends JavaClassFinder{
 
	/**
	 * @param srcpath path separated with ';' that contain the .class files
	 *        jar files are also supported
	 */
	public ClassFileFinder(String classpath){
		super(classpath, ".class");
	}
	 
	public Collection<String> findInnerClasses(String className){
		Collection<String> allClasses = listClasses();
		Collection<String> innerClasses = new ArrayList<String>();
		for(String s: allClasses){
			if(s.contains(className) && s.length()>className.length()){
				innerClasses.add(s);
			}
		}
		
		return innerClasses;
	}
}
