package jqian.util;

 
import java.net.URL;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.security.CodeSource;
import java.security.ProtectionDomain;

/**
 * @note It may not work for classes in a jar file. 
 * 		 Use Class.getResource() or ClassLoader.getResource() instead. 
 * 
 */	
public class PathUtils {
	 
	/** @return return null if the class is not defined.  */
	public static String getAbsolutePath(Class<?> cls) throws IOException {
		String path = null;
		if (cls == null) {
			throw new NullPointerException();
		}
		URL url = getClassLocationURL(cls);
		if (url != null) {
			path = url.getPath();
			if ("jar".equalsIgnoreCase(url.getProtocol())) {
				try {
						path = new URL(path).getPath();
				} catch (MalformedURLException e) {}
				int location = path.indexOf("!/");
				if (location != -1) {
					path = path.substring(0, location);
				}
			}
			File file = new File(path);
			path = file.getCanonicalPath();
		}
		return path;
	}

	/**
	 * Find the absolute path of a file or folder by its relative path to a class.
	 * e.g., if a file has relative path '../../resource/test.txt' to class Test,
	 * then by calling Path.getFullPathRelateClass("../../resource/test.txt",Test.class), 
	 * we can get the absolute path of test.txt
	 */
	public static String getFullPathRelateClass(String relativePath, Class<?> cls) throws IOException {
		String path = null;
		if (relativePath == null) {
			throw new NullPointerException();
		}
		String clsPath = getAbsolutePath(cls);
		File clsFile = new File(clsPath);
		String tempPath = clsFile.getParent() + File.separator + relativePath;
		File file = new File(tempPath);
		path = file.getCanonicalPath();
		return path;
	}

	public static InputStream getFileByName(final Class<?> cls,String name){		
		ClassLoader clsLoader = cls.getClassLoader();
		String file = cls.getName().replace('.', '/');
		int index = file.lastIndexOf('/');
		file = file.substring(0, index+1);
		file += name;
		
		return clsLoader.getResourceAsStream(file);
	}

	public static URL getClassLocationURL(final Class<?> cls) {
		URL result = null;
		final String clsAsResource = cls.getName().replace('.', '/').concat(".class");
		final ProtectionDomain pd = cls.getProtectionDomain();
		
		// java.lang.Class contract does not specify if 'pd' can ever be null;
		// it is not the case for Sun's implementations, but guard against null
		// just in case:
		if (pd != null) {
			final CodeSource cs = pd.getCodeSource();
			// 'cs' can be null depending on the classloader behavior:
			if (cs != null)
				result = cs.getLocation();

			if (result != null) {
				// Convert a code source location into a full class file location for some common cases:
				if ("file".equals(result.getProtocol())) {
					try {
						if (result.toExternalForm().endsWith(".jar")|| 
							result.toExternalForm().endsWith(".zip"))
							result = new URL("jar:".concat(result.toExternalForm()).concat("!/").concat(clsAsResource));
						else if (new File(result.getFile()).isDirectory())
							result = new URL(result, clsAsResource);
					} catch (MalformedURLException ignore) {
					}
				}
			}
		}

		if (result == null) {
			// Try to find 'cls' definition as a resource; 
			// this is not documented to be legal, but Sun's implementations seem to allow this:
			final ClassLoader clsLoader = cls.getClassLoader();
			result = clsLoader != null ? clsLoader.getResource(clsAsResource)
					: ClassLoader.getSystemResource(clsAsResource);
		}
		return result;
	}
	
	public static String getJreClasspath(String jdkRoot){
		return jdkRoot + "/jre/lib/rt.jar;" 
		        + jdkRoot + "/jre/lib/sunrsasign.jar;"  
		        + jdkRoot + "/jre/lib/jsse.jar;"
				+ jdkRoot + "/jre/lib/jce.jar;" 
				+ jdkRoot + "/jre/lib/charsets.jar;" 
				+ jdkRoot + "/jre/lib/ext/dnsns.jar;" 
				+ jdkRoot + "/jre/lib/ext/ldapsec.jar;" 
				+ jdkRoot + "/jre/lib/ext/localedata.jar;" 
				+ jdkRoot+ "/jre/lib/ext/sunjce_provider.jar";
	}
	
	/** Transfer a free style string into a string that is valid as a file path. */
	public static String toValidFilepath(String str){
        StringBuffer strBuf = new StringBuffer(str);
        for (int i = 0; i < strBuf.length(); i++) {
            if (strBuf.charAt(i) == '<')
                strBuf.setCharAt(i, '_');
            if (strBuf.charAt(i) == '>')
                strBuf.setCharAt(i, '_');
        }
        return strBuf.toString();
	}
}
