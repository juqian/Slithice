package test;

import java.util.*;

import jqian.sootex.Cache;
import jqian.sootex.HammockCFGProvider;
import jqian.sootex.dependency.pdg.*;
import jqian.sootex.dependency.slicing.JavaSlicingCriterion;
import jqian.sootex.dependency.slicing.JimpleSlicingCriterion;
import jqian.sootex.dependency.slicing.LocalSlicer;
import jqian.sootex.du.DUBuilder;
import jqian.sootex.du.IGlobalDUQuery;
import jqian.sootex.du.IReachingDUQuery;
import jqian.sootex.location.HeapAbstraction;
import jqian.sootex.ptsto.IPtsToQuery;
import jqian.sootex.ptsto.SparkPtsToQuery;
import jqian.sootex.ptsto.TypeBasedPtsToQuery;
import jqian.sootex.sideeffect.*;
import soot.*;


/**
 * 
 */
public class LocalSlicingTest implements AllTestCases{	
	static void testEnumAllLocalSlices(Collection<String> testedMethods,boolean _useSpark, boolean _ignoreCalls){
		//get points-to analysis
		IPtsToQuery ptsto = null;
		if(_useSpark){
			ptsto = new SparkPtsToQuery(); 
		}else{
			ptsto = new TypeBasedPtsToQuery(false);
		}
		       
        //get side effect analysis
        ISideEffectAnalysis sideEffect=null;
        if(!_ignoreCalls){        	
        	sideEffect = Test.loadSideEffector(ptsto);
        }
        
        //Collection entries = new LinkedList();
        //entries.add(Scene.v().getEntryPoints().get(0));
        HammockCFGProvider cfgProvider = new HammockCFGProvider(); 
        DUBuilder rdb = new DUBuilder(cfgProvider, ptsto, HeapAbstraction.FIELD_SENSITIVE, sideEffect,true);
    	IGlobalDUQuery gbRdQuery = rdb.getGlobalDUQuery();
    	
    	List<?> rm = Cache.v().getTopologicalOrder();            
    	for(Iterator<?> it = rm.iterator();it.hasNext();){
    	     SootMethod m = (SootMethod)it.next();
    	     if(testedMethods.contains(m.getSignature())){
    	    	 IReachingDUQuery query = gbRdQuery.getRDQuery(m);    	    	 
    	    	 
    	    	 PDG pdg = Test.buildUnsafePDG(m,ptsto,query,true);  
    	    	 LocalSlicer slicer = new LocalSlicer(pdg);
    	 		
    	    	 int i=0;		
    	    	 //test slicing with single statement as criterion 
    	    	 for(Unit unit: m.getActiveBody().getUnits()){		
    	 			JimpleSlicingCriterion criterion = new JimpleSlicingCriterion(m,unit,null,true);
    	 			Collection<JimpleSlicingCriterion> lst = new LinkedList<JimpleSlicingCriterion>();
    	 			lst.add(criterion);
    	 			Set<DependenceNode> result = slicer.slice(lst);
    	 			
    	 			String filename = "./output/img/slice_"+i+".dot";			
    	 			SDGUtil.showSlicingResult(filename, pdg, unit.toString(),result);
    	 			
    	 			i++;
    	    	 }
    	     }
    	}
	} 
	
	static void testLocalSlicingOnCriterion(JavaSlicingCriterion criterion){
		//initialize SDG builder
		DepGraphOptions opts = new DepGraphOptions(true, false, HeapAbstraction.FIELD_SENSITIVE);
		SootMethod entry = Scene.v().getEntryPoints().get(0);		 
		SDG sdg = SDGUtil.constructSDG(entry,opts,true, -1);	
		
		//get the concerned PDG
		String scope = criterion.getEnclosingScope();
		SootMethod domain = Scene.v().getMethod(scope);   	 
   	 	PDG pdg = sdg.getPDG(domain); 
   	    LocalSlicer slicer = new LocalSlicer(pdg);
   	 
   	    IPtsToQuery query = new SparkPtsToQuery();
   	    Collection<JimpleSlicingCriterion> criteria = criterion.toJimpleCriterion(query, HeapAbstraction.FIELD_SENSITIVE);
		if(criteria!=null){
			Set<DependenceNode> result = slicer.slice(criteria);
			
			String filename = "./output/dot/java_slice.dot";	
			SDGUtil.showSlicingResult(filename, pdg, "",result);
		}
	}  
   
    
	/**
	 * Command line format
	 * <test_type> [<line> <variable>]
	 * 
	 * A test_type can be "no_criterion" or "with_criterion".
	 * If the test_type is "with_criterion", then the line and variable should be specified
	 * 
	 * Example test cases: (you can copy and paste it to the command line)
	 *    with_criterion 86   k
	 *    with_criterion 85   k
	 *    with_criterion 86   x
	 */
 
	public static void main(String[] args){	
		String method = SLICING_START[7]; 
		 
    	String mainClass = SLICE_MAIN_CLASS;
		JavaSlicingCriterion javaCriterion = null;
		
		if(args[0].equals("no_criterion")){
			
		}
		else if(args[0].equals("with_criterion")){			 
			String var = args[2];
			int line = Integer.parseInt(args[1]);
			Collection<String> vars = new LinkedList<String>();
			vars.add(var);
			
			javaCriterion = new JavaSlicingCriterion(vars, method, line,true);
		}
		else{
			throw new RuntimeException("Bad program argument.");
		} 
		
    	Properties options = Test.loadConfig("/test/dependency/config.xml"); 
       	options.put("entry_class", mainClass);
       	
    	Test.loadClasses(true);     
    	Test.doFastSparkPointsToAnalysis();
    	Test.simplifyCallGraph();
    	
    	testLocalSlicingOnCriterion(javaCriterion);
	} 
}