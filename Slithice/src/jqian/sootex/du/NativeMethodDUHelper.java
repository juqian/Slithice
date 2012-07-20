package jqian.sootex.du;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jqian.sootex.location.AccessPath;
import soot.Local;
import soot.SootMethod;
import soot.Value;


/**
 *
 */
public class NativeMethodDUHelper {
	private static NativeMethodDUHelper _instance = new NativeMethodDUHelper();
	public static NativeMethodDUHelper v(){
		return _instance;
	}
	
	protected Map<String, NativeDU> cnameToSim;
	
	private NativeMethodDUHelper(){
		cnameToSim = new HashMap<String, NativeDU>();
		
		cnameToSim.put("java.lang.Object", new JavaLangObjectDU());
		cnameToSim.put("java.lang.System", new JavaLangSystemSync());
	    cnameToSim.put("java.lang.Runtime", new JavaLangRuntimeSync());
	    cnameToSim.put("java.lang.Shutdown", new JavaLangShutdownSync());
	   
/*	    cnameToSim.put("java.lang.String", new JavaLangStringNative(helper));
	    cnameToSim.put("java.lang.Float", new JavaLangFloatNative(helper));
	    cnameToSim.put("java.lang.Double", new JavaLangDoubleNative(helper));
	    cnameToSim.put("java.lang.StrictMath", new JavaLangStrictMathNative(helper));
	    cnameToSim.put("java.lang.Throwable", new JavaLangThrowableNative(helper));
	    cnameToSim.put("java.lang.Class", new JavaLangClassNative(helper));
	    cnameToSim.put("java.lang.Package", new JavaLangPackageNative(helper));
	    cnameToSim.put("java.lang.Thread", new JavaLangThreadNative(helper));
	    cnameToSim.put("java.lang.ClassLoader", new JavaLangClassLoaderNative(helper));
	    cnameToSim.put("java.lang.ClassLoader$NativeLibrary",
	                       new JavaLangClassLoaderNativeLibraryNative(helper));
	    cnameToSim.put("java.lang.SecurityManager",
	                       new JavaLangSecurityManagerNative(helper));

	    cnameToSim.put("java.lang.reflect.Field",
	                       new JavaLangReflectFieldNative(helper));
	    cnameToSim.put("java.lang.reflect.Array",
	                       new JavaLangReflectArrayNative(helper));
	    cnameToSim.put("java.lang.reflect.Method",
	                       new JavaLangReflectMethodNative(helper));
	    cnameToSim.put("java.lang.reflect.Constructor",
	                       new JavaLangReflectConstructorNative(helper));
	    cnameToSim.put("java.lang.reflect.Proxy",
	                       new JavaLangReflectProxyNative(helper));

	    cnameToSim.put("java.io.FileInputStream", 
	                       new JavaIoFileInputStreamNative(helper));
	    cnameToSim.put("java.io.FileOutputStream", 
	                       new JavaIoFileOutputStreamNative(helper));
	    cnameToSim.put("java.io.ObjectInputStream",
	                       new JavaIoObjectInputStreamNative(helper));
	    cnameToSim.put("java.io.ObjectOutputStream",
	                       new JavaIoObjectOutputStreamNative(helper));
	    cnameToSim.put("java.io.ObjectStreamClass",
	                       new JavaIoObjectStreamClassNative(helper));
	    cnameToSim.put("java.io.FileSystem", new JavaIoFileSystemNative(helper));
	    cnameToSim.put("java.io.FileDescriptor", new JavaIoFileDescriptorNative(helper));

	    cnameToSim.put("java.util.ResourceBundle",new JavaUtilResourceBundleNative(helper));
	    cnameToSim.put("java.util.TimeZone", new JavaUtilTimeZoneNative(helper));

	    cnameToSim.put("java.util.jar.JarFile",new JavaUtilJarJarFileNative(helper));
	        
	    cnameToSim.put("java.util.zip.CRC32",new JavaUtilZipCRC32Native(helper));
	    cnameToSim.put("java.util.zip.Inflater",new JavaUtilZipInflaterNative(helper));
	    cnameToSim.put("java.util.zip.ZipFile",new JavaUtilZipZipFileNative(helper));
	    cnameToSim.put("java.util.zip.ZipEntry",new JavaUtilZipZipEntryNative(helper));	        

	    cnameToSim.put("java.security.AccessController",new JavaSecurityAccessControllerNative(helper));

	    cnameToSim.put("java.net.InetAddress",new JavaNetInetAddressNative(helper));
	    cnameToSim.put("java.net.InetAddressImpl",new JavaNetInetAddressImplNative(helper));

	    cnameToSim.put("sun.misc.Signal",new SunMiscSignalNative(helper));
	    cnameToSim.put("sun.misc.NativeSignalHandler",new SunMiscSignalHandlerNative(helper));
	    cnameToSim.put("sun.misc.Unsafe",new SunMiscUnsafeNative(helper));
	    */
	}
	
	public Collection<AccessPath> getDef(SootMethod m, Value receiver, List<Value> params){
		String classname = m.getDeclaringClass().getName();
		NativeDU sync = cnameToSim.get(classname);
		if(sync!=null){
			return sync.getDef(m, receiver, params);
		}
		else{
			return Collections.emptyList();
		}
	}
	
	public Collection<AccessPath> getUse(SootMethod m, Value receiver, List<Value> params){
		String classname = m.getDeclaringClass().getName();
		NativeDU sync = cnameToSim.get(classname);
		if(sync!=null){
			return sync.getUse(m, receiver, params);
		}
		else{
			return Collections.emptyList();
		}
	} 
 

	static interface NativeDU{
		public Collection<AccessPath> getDef(SootMethod m, Value receiver, List<Value> params);
		public Collection<AccessPath> getUse(SootMethod m, Value receiver, List<Value> params);
	}

	/**
	 * java.lang.Object clone()
	 * void notify()
	 * void notifyAll()
	 * void wait(long)
	 * void registerNatives()
	 * java.lang.Class getClass()		 * 
	 * int hashCode()
	 */
	static class JavaLangObjectDU implements NativeDU{
		public Collection<AccessPath> getDef(SootMethod m, Value receiver, List<Value> params){
			return Collections.emptyList();
		}
		
		public Collection<AccessPath> getUse(SootMethod m, Value receiver, List<Value> params){
			return Collections.emptyList();
		}
	}
	
	/**
	 * void arraycopy(java.lang.Object,int,java.lang.Object,int,int)
	 * void setIn0(java.io.InputStream)
	 * void setOut0(java.io.PrintStream)
	 * void setErr0(java.io.PrintStream)
	 * java.util.Properties initProperties(java.util.Properties)
	 * java.lang.String mapLibraryName(java.lang.String)
	 * java.lang.Class getCallerClass()
	 */
	static class JavaLangSystemSync implements NativeDU{
		public Collection<AccessPath> getDef(SootMethod m, Value receiver, List<Value> params){
			String subsig = m.getSubSignature();
			if(subsig.equals("void arraycopy(java.lang.Object,int,java.lang.Object,int,int)")){
				Collection<AccessPath> result = new ArrayList<AccessPath>(1);
				Value dest = params.get(2);
				if(dest instanceof Local){
					AccessPath ap = AccessPath.valueToAccessPath(null, null, dest);
					ap = ap.appendArrayRef();
					result.add(ap);
				}
				
				return result;
			}
			else if(subsig.equals("void setIn0(java.io.InputStream)")){				
			}
			else if(subsig.equals("void setOut0(java.io.PrintStream)")){				
			}
			else if(subsig.equals("void setErr0(java.io.PrintStream)")){				
			}
			else if(subsig.equals("java.util.Properties initProperties(java.util.Properties)")){				
			}
			else if(subsig.equals("java.lang.String mapLibraryName(java.lang.String)")){				
			}
			else if(subsig.equals("java.lang.Class getCallerClass()")){				
			} 
			
			return Collections.emptyList();
		}
		
		public Collection<AccessPath> getUse(SootMethod m, Value receiver, List<Value> params){
			String subsig = m.getSubSignature();
			if(subsig.equals("void arraycopy(java.lang.Object,int,java.lang.Object,int,int)")){
				Collection<AccessPath> result = new ArrayList<AccessPath>(1);
				Value dest = params.get(0);
				if(dest instanceof Local){
					AccessPath ap = AccessPath.valueToAccessPath(null, null, dest);
					ap = ap.appendArrayRef();
					result.add(ap);
				}
				
				return result;
			}
			else if(subsig.equals("void setIn0(java.io.InputStream)")){				
			}
			else if(subsig.equals("void setOut0(java.io.PrintStream)")){				
			}
			else if(subsig.equals("void setErr0(java.io.PrintStream)")){				
			}
			else if(subsig.equals("java.util.Properties initProperties(java.util.Properties)")){
			}
			else if(subsig.equals("java.lang.String mapLibraryName(java.lang.String)")){			
			}
			else if(subsig.equals("java.lang.Class getCallerClass()")){			
			} 
			
			return Collections.emptyList();
		}
	}
	
	/**
	 * java.lang.Process execInternal(java.lang.String[],java.lang.String[],java.lang.String)
	 * public native long freeMemory(); 
	 * public native long totalMemory(); 
	 * public native void gc(); 
	 * private static native void runFinalization0(); 
	 * public native void traceInstructions(boolean); 
	 * public native void traceMethodCalls(boolean);
	 */
	static class JavaLangRuntimeSync implements NativeDU{	
		public Collection<AccessPath> getDef(SootMethod m, Value receiver, List<Value> params){
			return Collections.emptyList();
		}
		
		public Collection<AccessPath> getUse(SootMethod m, Value receiver, List<Value> params){
			return Collections.emptyList();
		}
	}
	
	/**
	 * static native void halt(int);
	 * private static native void runAllFinalizers();
	 */
	static class JavaLangShutdownSync implements NativeDU{	
		public Collection<AccessPath> getDef(SootMethod m, Value receiver, List<Value> params){
			return Collections.emptyList();
		}
		
		public Collection<AccessPath> getUse(SootMethod m, Value receiver, List<Value> params){
			return Collections.emptyList();
		}
	}
}
