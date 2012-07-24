package jqian.sootex;

import java.util.HashSet;
import java.util.Set;
import soot.*;

/**
 * Some too huge methods that should better be analyzed in a conservative way.
 * 
 */
public class ConservativelyHandledEntities {
	static String[] methodSignatures = {
		"<sun.io.CharacterEncoding: void installAll()>",
	};
	
	static String[] classSigs = {
		"sun.security.pkcs.PKCS9Attribute",
	};
	
	private static Set<String> methods;
	private static Set<String> classes;
	
	static{
		methods = new HashSet<String>();
		for(String sig: methodSignatures){
			methods.add(sig);
		}
		
		classes = new HashSet<String>();
		for(String sig: classSigs){
			classes.add(sig);
		}
	}
    
    public static boolean isConservativelyHandledMethod(SootMethod m){
        String s = m.toString();
        if(methods.contains(s)){
        	return true;
        }
        
        return false;
    }
    
    public static boolean isConservativelyHandledClass(SootClass c){
        String s = c.getName();
        if(classes.contains(s)){
        	return true;
        }
        
        return false;
    }
}
