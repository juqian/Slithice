package jqian.util;

 
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
