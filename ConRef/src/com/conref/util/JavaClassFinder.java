/** 
 *  
 * 
 * 	@author Ju Qian {jqian@live.com}
 * 	@date 2011-1-25
 * 	@version
 */
package com.conref.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Enumeration;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

 
/**
 * Find java classes either in .java format or in .class format.
 * Directory structures are understood as the package sturcture
 */
class JavaClassFinder {
	private Object[] _dirs;
	private String _ext;
	private Set<String> _classes;
	/**
	 * @param srcpath path separated with ';' that contain the .java files
	 *        zip, jar files are also supported
	 */
	public JavaClassFinder(String srcpath, String ext){
		this._ext = ext;
		
		String[] paths = srcpath.split(";");
		_dirs = new Object[paths.length];
		
		int index = 0;
		for(String p: paths){
			p = p.trim();
			File file = new File(p);
			if(file.isDirectory()){
				_dirs[index] = file;
			}
			else if(file.exists()){				 
				try{
					if(file.getName().contains(".zip")){
						_dirs[index] = new ZipFile(file);
					}
					else if(file.getName().contains(".jar")){
						_dirs[index] = new JarFile(file);
					}
					else{
						throw new RuntimeException("Unsupported format: "+file);
					}
				}
				catch(IOException e){
					throw new RuntimeException(e.getMessage());
				}
			}
			
			index++;
		}
	}
	
	public InputStream findClass(String classname){
		try{
			for(Object o: _dirs){
				if(o==null){
					
				}
				else if(o instanceof File){
					File file = (File)o;
					File javafile = findFile(file,classname);
					if(javafile!=null){
						InputStream is = new FileInputStream(javafile);
						if(is!=null){
							return is;
						}
					}					
				}
				else if(o instanceof ZipFile){
					ZipFile zip = (ZipFile)o;				 
					InputStream is = findFile(zip,classname);
					if(is!=null){
						return is;
					}
				}
				else if(o instanceof JarFile){
					JarFile jar = (JarFile)o;				 
					InputStream is = findFile(jar,classname);
					if(is!=null){
						return is;
					}
				}
			}
		}
		catch(Exception e){}
		
		return null;
	}
	
	private File findFile(File directory, String target){
		StringBuffer path = new StringBuffer(target);
		int length = path.length();
		for(int i=0; i<length; i++){
			if(path.charAt(i)=='.'){
				path.setCharAt(i, File.separatorChar);
			}
		}
		
		String tgtPath = directory.getPath() + File.separatorChar + path + _ext;
		directory = new File(tgtPath);
		if(directory.exists()){
			return directory;
		}
		else{
			return null;
		}
		
		/*while(target!=null){
			int sp = target.indexOf('.');
			String fname = null;
			if(sp<0){
				fname = target + _ext;
				target = null;
			}
			else{
				fname = target.substring(0,sp);
				target = target.substring(sp+1);
			}
			
			File[] contents = directory.listFiles();
			File found = null;
			for(File f: contents){
				if(f.getName().equals(fname)){
					found = f;
				}
			}
			
			if(found==null){
				return null;
			}
			
			directory = found;
		}
		  
		return directory;*/
	}
	
	private InputStream findFile(ZipFile zip, String target){
		StringBuffer filename = new StringBuffer(target);
		for(int i=0; i<target.length(); i++){
			if(target.charAt(i)=='.'){
				filename.setCharAt(i, '/');
			}
		}
		
		filename.append(_ext);
		
		ZipEntry entry = zip.getEntry(filename.toString());
		if(entry==null){
			return null;
		}

		try{
			InputStream is = zip.getInputStream(entry);
			return is;
		}catch(IOException e){}
				
		return null;
	}
	
	/** Here only list classes with their names as the names of the .java file.
	 *  Inner classes and the second, third, .. classes share the same .java file with
	 *  other classes are not considered. 
	 */
	public Set<String> listClasses(){
		if(_classes!=null){
			return _classes;
		}
		
		_classes = new HashSet<String>();		
		try{
			for(Object o: _dirs){
				if(o==null){
					
				}
				else if(o instanceof File){
					File file = (File)o;
					listClasses(file,file,_classes);					 
				}
				else if(o instanceof ZipFile){
					ZipFile zip = (ZipFile)o;				 
					listClasses(zip, _classes);
				}
				else if(o instanceof JarFile){
					JarFile jar = (JarFile)o;				 
					listClasses(jar, _classes);
				}
			}
		}
		catch(Exception e){}

		return _classes;
	}
	 
	private void listClasses(File root, File file, Set<String> out) throws IOException{
		if(!file.isDirectory()){
			if(file.getName().endsWith(_ext)){
				String fullpath = file.getCanonicalPath();
				String rootpath;
				if(root.isDirectory()){
					rootpath = root.getCanonicalPath();
				}
				else{
					rootpath = root.getParentFile().getCanonicalPath();
				}
				rootpath = rootpath + File.separatorChar;
				String classpath = fullpath.substring(rootpath.length(),fullpath.length()-_ext.length());
				StringBuffer classname = new StringBuffer(classpath);
				for(int i=0; i<classpath.length();i++){
					if(classname.charAt(i)==File.separatorChar){
						classname.setCharAt(i, '.');
					}
				}
				
				out.add(classname.toString());
			}
		}
		else{
			File[] contents = file.listFiles();
			for(File f: contents){
				listClasses(root, f, out);
			}
		}
	}
	 
	 private void listClasses(ZipFile zip, Set<String> out) throws IOException{
		 for (Enumeration<?> e = zip.entries(); e.hasMoreElements() ;) {
			 ZipEntry entry = (ZipEntry)e.nextElement();
	         String name = entry.getName();
	         
	         if(!entry.isDirectory() && name.endsWith(_ext)){
	        	 StringBuffer namebuf = new StringBuffer(name);
	        	 namebuf.setLength(name.length()-_ext.length());
	        	 int length = namebuf.length();
	        	 for(int i=0; i<length; i++){
	        		 if(namebuf.charAt(i)=='/'){
	        			 namebuf.setCharAt(i, '.');
	        		 }
	        	 }
	        	 
	        	 out.add(namebuf.toString());
	         }
	     } 
	 }
}
