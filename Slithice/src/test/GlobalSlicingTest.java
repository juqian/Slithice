package test;

import java.util.*;

import jqian.sootex.dependency.pdg.*;
import jqian.sootex.dependency.slicing.GlobalSlicer;
import jqian.sootex.dependency.slicing.JavaSlicingCriterion;
import jqian.sootex.dependency.slicing.JimpleSlicingCriterion;
import jqian.sootex.location.HeapAbstraction;
import jqian.sootex.ptsto.IPtsToQuery;
import jqian.sootex.ptsto.SparkPtsToQuery;
import soot.*;
import soot.jimple.IdentityStmt;
import test.AllTestCases.SDGCases;


/**
 * 
 */
public class GlobalSlicingTest implements AllTestCases{	
    static void testEnumAllGlobalSlices(String _entry, String _testMethod){
    	SootMethod main = Scene.v().getMethod(_entry);
		DepGraphOptions opts = new DepGraphOptions(true, true, HeapAbstraction.FIELD_SENSITIVE);
		
		SDG sdg = SDGUtil.constructSDG(main,opts,true, -1);	
		sdg.buildSummaryEdges(main);
		sdg.connectPDGs();     	
    	SDGUtil.showDependenceGraph(sdg, "./output/img/sdg.dot");		
		GlobalSlicer slicer = new GlobalSlicer(sdg);
		
    	SootMethod m = Scene.v().getMethod(_testMethod);
		int i=0;		
		//test slicing with single statement as criterion
		for(Unit unit: m.getActiveBody().getUnits()){	 
			if(unit instanceof IdentityStmt)
				continue;
			
			JimpleSlicingCriterion criterion = new JimpleSlicingCriterion(m,unit,null,true);
			Collection<JimpleSlicingCriterion> lst = new LinkedList<JimpleSlicingCriterion>();
			lst.add(criterion);
			Set<DependenceNode> result = slicer.slice(lst);
			
			String filename = "./output/img/slice_"+i+".dot";	
			SDGUtil.showSlicingResult(filename,sdg,unit.toString(),result);
			i++;
		}	
    }
    
    static void testGlobalSliceOnCriterion(String entry, JavaSlicingCriterion criterion){  
    	SootMethod main = Scene.v().getMethod(entry);
    	DepGraphOptions opts = new DepGraphOptions(true, true, HeapAbstraction.FIELD_SENSITIVE);
		
		SDG sdg = SDGUtil.constructSDG(main,opts,true, -1);	
		sdg.buildSummaryEdges(main);
		sdg.connectPDGs();     	
    	///SDGUtil.showDependenceGraph(sdg, "./output/dot/sdg.dot");		
		GlobalSlicer slicer = new GlobalSlicer(sdg);
		
    	IPtsToQuery query = new SparkPtsToQuery();
    	Collection<JimpleSlicingCriterion> critera = criterion.toJimpleCriterion(query, HeapAbstraction.FIELD_SENSITIVE);
		Set<DependenceNode> result = slicer.slice(critera);

		String filename = "./output/dot/slice.dot";
		SDGUtil.showSlicingResult(filename,sdg,"",result);
    }
   
	
    /** Require three arguments
     *  java GlobalSlicerTest <slicing_type> <entry_method> <line> <var>
     *  
	 *  The slicing type could be: "enum" or "with_criterion"
     *  specifying where the slicing criterion starts. */
    /** Require two inputs, one is the line to slice, the other is the variable concerned.
     *  Examples aree:
     *        
     */
    public static void main(String[] args){	
    	JavaSlicingCriterion javaCriterion = null;
    	//String MAIN_CLASS = SLICE_MAIN_CLASS;
    	String MAIN_CLASS = SDGCases.CLASSES[2];
    	String entry = "";
    	
    	if(args.length==0){
    		Collection<String> vars = new LinkedList<String>();
    		vars.add("m");
    		//vars.add("x");
    		
    		javaCriterion = new JavaSlicingCriterion(vars,MAIN_CLASS,68,false);
    		//javaCriterion = new JavaSlicingCriterion(vars,MAIN_CLASS,48);
    	}
    	else if(args[0].equals("enum")){
    		//int i1 = Integer.parseInt(args[1]);    	
        	//int i2 = Integer.parseInt(args[2]); 
    	}
    	else if(args[0].equals("with_criterion")){
    		entry = args[1];
        	int line = Integer.parseInt(args[2]); 
        	String var = args[3];
        	
        	Collection<String> vars = new LinkedList<String>();
    		vars.add(var);
    		//javaCriterion = new JavaSlicingCriterion(vars,MAIN_CLASS,line,true);
    		javaCriterion = new JavaSlicingCriterion(vars,"test.dependency.cases.SDG3$B",line,true);
    	}
    	else{
			throw new RuntimeException("Bad program argument.");
		} 
    	
    	Properties options = Test.loadConfig("/test/dependency/config.xml"); 
       	options.put("entry_class", MAIN_CLASS);
       	
    	Test.loadClasses(true);     
    	Test.doFastSparkPointsToAnalysis();
    	Test.simplifyCallGraph();
    	
    	testGlobalSliceOnCriterion(entry, javaCriterion);
	}
}