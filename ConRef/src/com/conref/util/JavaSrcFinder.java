/** 
 *  
 * 
 * 	@author Ju Qian {jqian@live.com}
 * 	@date 2011-1-25
 * 	@version
 */
package com.conref.util;


 
/**
 *
 */
public class JavaSrcFinder extends JavaClassFinder {
	/**
	 * @param srcpath path separated with ';' that contain the .java files
	 *        zip, jar files are also supported
	 */
	public JavaSrcFinder(String srcpath){
		super(srcpath, ".java");
	}
}
