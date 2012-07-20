package test;

import java.util.*;

import jqian.sootex.Cache;
import jqian.sootex.ptsto.IPtsToQuery;
import jqian.sootex.ptsto.TypeBasedPointsToAnalysis;
import jqian.sootex.ptsto.TypeBasedPtsToQuery;
import soot.*;

/**
 * 
 */
public class TestTypeBasedPtsToAnalysis{
	static void test(){  
		TypeBasedPointsToAnalysis.v(false);
		
		// test reference typed array 
		Type t1 = Scene.v().getRefType("test.ptsto.ITypeBasedPtsToCase");
		Type t11 = ArrayType.v(t1,1); 
		TypeBasedPointsToAnalysis.v(false).reachingObjects(t11);
		
		// rest prime typed array
		Type t2 =  IntType.v();
		t2 = ArrayType.v(t2, 1);
		TypeBasedPointsToAnalysis.v(false).reachingObjects(t2);
		
		// test root class
		Type t3 = Scene.v().getRefType("java.lang.Object");		
		TypeBasedPointsToAnalysis.v(false).reachingObjects(t3);
		
		// test normal reference
		Type t4 = Scene.v().getRefType("test.ptsto.ITypeBasedPtsToCase");		
		TypeBasedPointsToAnalysis.v(false).reachingObjects(t4);
		
		// multiple dimension array
		Type t5 = ArrayType.v(t1,2); 
		TypeBasedPointsToAnalysis.v(false).reachingObjects(t5);
		
		
    	IPtsToQuery query = new TypeBasedPtsToQuery(false);
    	
    	List<MethodOrMethodContext> rm = Cache.v().getTopologicalOrder();             
    	for(Iterator<MethodOrMethodContext> it = rm.iterator();it.hasNext();){
    	     SootMethod m = (SootMethod)it.next();    	    
    	     //PtsToTester.testPtsToQuery(System.out,m,pts2Query);    	     
    	}
	}        
 
	
	public static void main(String[] args) {
		String entryClass = "test.ptsto.TypeBasedPtsToCaseImpl";
		Properties conf = Test.loadConfig("/config.xml"); 
		conf.put("entry_class", entryClass);
    	Test.loadClasses(true);
    	test();
	}
}

/////////////////////////////// Test cases //////////////////////////////////
interface ITypeBasedPtsToCase{
	
}

interface ITypeBasedPtsToCase2 extends ITypeBasedPtsToCase{
	
}

class TypeBasedPtsToCaseImpl implements ITypeBasedPtsToCase{
	public static void main(String[] args){
		ITypeBasedPtsToCase[] a = new TypeBasedPtsToCaseImpl[2];
		ITypeBasedPtsToCase[][] b = new ITypeBasedPtsToCase[2][];
		int[] c = new int[10];
		TypeBasedPtsToCaseImpl2 d = new TypeBasedPtsToCaseImpl2();
		TypeBasedPtsToCaseImpl3 e = new TypeBasedPtsToCaseImpl3();
		
		a.hashCode();
		b.hashCode();
		c.hashCode();
		d.hashCode();
		e.hashCode();
	}
}

class TypeBasedPtsToCaseImpl2 extends TypeBasedPtsToCaseImpl{
	
}

class TypeBasedPtsToCaseImpl3 implements ITypeBasedPtsToCase2{
	
}
