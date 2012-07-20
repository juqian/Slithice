package test;

import java.util.*;
import jqian.sootex.Cache;
import jqian.sootex.dependency.pdg.DependenceGraph;
import jqian.sootex.dependency.pdg.PDG;
import jqian.sootex.dependency.pdg.DepGraphOptions;
import jqian.sootex.dependency.pdg.SDG;
import jqian.sootex.dependency.pdg.builder.SDGBuilder;
import jqian.sootex.location.HeapAbstraction;
import jqian.sootex.ptsto.PointsToAnalysisType;
import soot.*;


public class DependenceGraphTest implements AllTestCases{
    @SuppressWarnings({ "unchecked", "rawtypes" })
	static void testPDG(String entryMethodSignature, DepGraphOptions opts,Collection<String> testedMethods,  
    		 boolean buildAll, boolean showSDG, boolean toJavaDepGraph) {
    	SootMethod entry = Scene.v().getMethod(entryMethodSignature);
    	List entries = new ArrayList(1);
    	entries.add(entry);
    	 
    	PointsToAnalysisType ptsto = PointsToAnalysisType.SPARK;
    	HeapAbstraction sideEffectOpt = HeapAbstraction.FIELD_SENSITIVE;
    	int javaLibDepth = -1;
    	SDGBuilder sdgBuilder = SDGUtil.constructSDGBuilder(ptsto, sideEffectOpt, entries, opts, true, javaLibDepth);
    	sdgBuilder.preBuild();
		if(buildAll){
		    sdgBuilder.buildAll();
		}
		
		SDG sdg = sdgBuilder.getSDG();
		
		List<?> rm = Cache.v().getTopologicalOrder();            
    	for(Iterator<?> it = rm.iterator();it.hasNext();){
    	     SootMethod m = (SootMethod)it.next();
    	     if(testedMethods.contains(m.getSignature())){
    	    	 Test.out.println("\nTest on method "+m);
    	    	 
    	    	 Test.showCFG(m); 
    	    	 
    	    	 if(!buildAll){
    	    		 sdgBuilder.buildMethodPDG(m);
    	    	 }
    	    	 
    	    	 PDG pdg = sdg.getPDG(m);
    	    	 
    	    	 Test.out.println("\nStatistics "+ pdg.statistcToString());   
    	    	 DependenceGraph depGraph = pdg;
    	         if(toJavaDepGraph){
    	         	depGraph = pdg.toJavaStmtDepGraph();        	
    	         }       
    	         
    	         SDGUtil.showDependenceGraph(depGraph, "./output/dot/dep_"+m.getName()+".dot");    	 
    	     }
    	}
    	
    	sdgBuilder.postBuild();
    	
    	if(showSDG){
    		SDGUtil.showDependenceGraph(sdg,"./output/dot/sdg.dot");	
    	}
    } 
	
    
    static void testSDG(String entryMethodSignature, DepGraphOptions _options, boolean showPDGs) {
    	SootMethod main = Scene.v().getMethod(entryMethodSignature);    	
		SDG sdg = SDGUtil.constructSDG(main,_options,true, 2);
		sdg.buildSummaryEdges(main);
		sdg.connectPDGs();
		sdg.compact();
    	
		//show PDG of each method
		if(showPDGs){
			List<?> rm = Cache.v().getTopologicalOrder();             
	    	for(Iterator<?> it = rm.iterator();it.hasNext();){
	    	     SootMethod m = (SootMethod)it.next();
	    	     PDG pdg = sdg.getPDG(m);
	    	     if(pdg!=null){	    	    	 
	    	    	 String filename = "./output/dot/pdg_"+m.hashCode()+".dot";		
	    	    	 SDGUtil.showDependenceGraph(pdg,filename);   	
	    	     }
	    	}
		}		
    	
	
		SDGUtil.showDependenceGraph(sdg,"./output/dot/sdg.dot");			
		//DependenceGraph dep = sdg.toJavaStmtDepGraph();
		//SDGUtil.showDependenceGraph(dep,"./output/dot/javasdg.dot"); 		
    }  

    /** Require two string inputs.
     *  Usage: java CLASS <class_name> <entry_method_signature> [-showpdg]
     *  
     *  Examples:
     *  java CLASS test.slicing.cases.SDGCase "<test.slicing.cases.SDGCase: void test7()>"
     */
    public static void main(String[] args) {
    	//String mainClass = PDG_MAIN_CLASS;
    	String mainClass = SDGCases.CLASSES[5];
    	
    	Properties options = Test.loadConfig("/test/dependency/config.xml"); 
       	options.put("entry_class", mainClass);
       	
    	Test.loadClasses(true);     
    	Test.doFastSparkPointsToAnalysis();
    	Test.simplifyCallGraph();
    	 
    	HeapAbstraction locAbstraction = HeapAbstraction.FIELD_SENSITIVE;
    	DepGraphOptions pdgOptions = new DepGraphOptions(true, false, locAbstraction);
    	//String entryMethod = PDG[0]; //PDG_METHODS[1]; //PDG_METHODS[1];//
    	String entryMethod = SDGCases.SDG_ENTRIES[5][0];
    	
    	
    	Set<String> testedMethods = new HashSet<String>(); 
    	testedMethods.add(entryMethod);
    	
    	//testPDG(entryMethod, pdgOptions, testedMethods, false, false, false); 
    	testSDG(entryMethod, pdgOptions, false); 
    }     
}

