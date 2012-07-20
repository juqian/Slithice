package test;

import java.util.*;

import jqian.sootex.location.Location;
import jqian.sootex.location.HeapAbstraction;
import jqian.sootex.ptsto.*;
import jqian.sootex.sideeffect.*;
import soot.*;


/**
 */
public class SideEffectTest implements AllTestCases{    
    static void test(ISideEffectAnalysis sideEffect, String methodSignature){
    	SootMethod method = Scene.v().getMethod(methodSignature);
		Test.out.println("\nTesting method: " + method);
		testOnLocations(sideEffect, method);
    }   
    
    static void testOnLocations(ISideEffectAnalysis sideEffect,SootMethod m){
         Collection<Location> modLocs = sideEffect.getModHeapLocs(m);        
         Test.out.print("\nModification set: ");
         Test.printCollection(modLocs.iterator(),"\n");   
    	   
         Collection<Location> useLocs = sideEffect.getUseHeapLocs(m);        
         Test.out.print("\nUse set: ");
         Test.printCollection(useLocs.iterator(),"\n");
         
         useLocs = sideEffect.getModGlobals(m);        
         Test.out.print("\nModified gloal set: ");
         Test.printCollection(useLocs.iterator(),"\n");
         
         useLocs = sideEffect.getUseGlobals(m);        
         Test.out.print("\nUsed gloal set: ");
         Test.printCollection(useLocs.iterator(),"\n");
    }   
    
	@SuppressWarnings({ "rawtypes" })
	public static ISideEffectAnalysis loadSideEffector(){
		//GlobalPtsToQuery query = new GlobalPtsToQuery.Default(SparkPtsToQuery.v());
		//GlobalPtsToQuery query = new GlobalPtsToQuery.Default(NaivePtsToQuery.v());
		IPtsToQuery query = new TypeBasedPtsToQuery(false);
		//SootMethod e = Scene.v().getEntryPoints().get(0);
    	//Collection entries = new LinkedList();
    	//entries.add(e);
		Collection entries = Scene.v().getEntryPoints();
		HeapAbstraction opt = HeapAbstraction.FIELD_SENSITIVE;
		//MemoryDistinguishment opt = MemoryDistinguishment.FIELD_BASED;
		//MemoryDistinguishment opt = MemoryDistinguishment.TYPE_BASED;
		//MemoryDistinguishment opt = MemoryDistinguishment.NO_DISTINGUISH;
    	SideEffectAnalysis se = new SideEffectAnalysis(query,entries, opt);
    	se.build();
    	return se;
	}
    
    
    public static void main(String[] args) {
    	Test.loadConfig("/test/config.xml"); 
    	Test.loadClasses(true);
    	
    	//SootUtils.doSparkPointsToAnalysis(Collections.EMPTY_MAP);
    	Test.doFastSparkPointsToAnalysis();

    	//ISideEffector sideEffect = Test.loadSideEffector();
    	ISideEffectAnalysis sideEffect = loadSideEffector();
    	String method = SideEffect.SIDE_EFFECT_CASES[2];
    	test(sideEffect, method); 
    }    
}

