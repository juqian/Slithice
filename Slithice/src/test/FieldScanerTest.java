package test;

import java.util.*;
import jqian.sootex.sideeffect.FieldScaner;
import soot.*;


/**
 * 
 */
public class FieldScanerTest implements AllTestCases {
    static void test( String methodSignature ){
    	FieldScaner.v();
    	
    	SootMethod method = Scene.v().getMethod(methodSignature);
        Test.out.println(method);                
        testFieldAccess(method); 
    }
    
    /** Test the accessed fields. */
    static void testFieldAccess(SootMethod m){
    	Set<SootField> instFields = new HashSet<SootField>();
    	Set<SootField> globals = new HashSet<SootField>();
    	Set<ArrayType> arrays = new HashSet<ArrayType>();
    	
    	System.out.println("\nDefined instance fields:");
    	Test.printCollection(FieldScaner.v().getModInstanceFields(m).iterator(),"\n");
    	
    	System.out.println("\nUsed instance fields:");    
    	Test.printCollection(FieldScaner.v().getUseInstanceFields(m).iterator(),"\n");
    	
    	FieldScaner.v().getAccessedInstanceFields(m,instFields);
    	System.out.println("\nAccessed instance fields:");    	
    	Test.printCollection(instFields.iterator(),"\n");
    	
    	
    	System.out.println("\nDefined static fields:");
    	Set<SootField> modGlobals = FieldScaner.v().getModGlobals(m);
    	//testSet(modGlobals);
    	Test.printCollection(modGlobals.iterator(),"\n");
    	
    	System.out.println("\nUsed static fields:");
    	Test.printCollection(FieldScaner.v().getUseGlobals(m).iterator(),"\n");
    	
    	FieldScaner.v().getAccessedGlobals(m,globals);
    	System.out.println("\nAccessed globals:");    	
    	Test.printCollection(globals.iterator(),"\n");
    	
    	
    	System.out.println("\nDefined array types:");    	
    	Test.printCollection(FieldScaner.v().getModArrayTypes(m).iterator(),"\n");  
    	
    	System.out.println("\nUsed array types:");    	
    	Test.printCollection(FieldScaner.v().getUseArrayTypes(m).iterator(),"\n"); 
    
    	FieldScaner.v().getAccessedArrayTypes(m,arrays);
    	System.out.println("\nAccessed array types:");    	
    	Test.printCollection(arrays.iterator(),"\n");    	
    	
    	System.out.println();
    }
 
    static void testSet(Set<SootField> set){
    	for(SootField f: set){
    		if(f.getName().equals("_x") || f.getName().equals("_zar")){
    			throw new RuntimeException();
    		}
    	}    	 
    }
    
    /**
     * Two string input parameters:
     * Usage: java FieldScanerTest <checked_method>
     * Examples:
     *     java FieldScanerTest "<test.sideeffect.cases.SideEffectCase: void testFieldScaner()>"
     */
    public static void main(String[] args){ 
    	Test.loadConfig("/test/sideeffect/config.xml"); 
    	Test.loadClasses(true);
    	Test.buildCHACallGraph();
    	Test.simplifyCallGraph();
    
        if(args.length>=1){
            test(args[0]);     
        }
    }
}
