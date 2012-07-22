package test;

import java.util.*;

import jqian.sootex.CFGProvider;
import jqian.sootex.HammockCFGProvider;
import jqian.sootex.dependency.pdg.*;
import jqian.sootex.dependency.pdg.builder.SDGBuilder;
import jqian.sootex.dependency.slicing.JavaSlicingCriterion;
import jqian.sootex.du.DUBuilder;
import jqian.sootex.location.HeapAbstraction;
import jqian.sootex.ptsto.IPtsToQuery;
import jqian.sootex.ptsto.NaivePtsToQuery;
import jqian.sootex.ptsto.PointsToAnalysisType;
import jqian.sootex.ptsto.PtsToHelper;
import jqian.sootex.ptsto.SparkPtsToQuery;
import jqian.sootex.ptsto.TypeBasedPtsToQuery;
import jqian.sootex.sideeffect.SideEffectAnalysis;
import jqian.sootex.util.CFGViewer;
import jqian.sootex.util.HammockCFG;
import jqian.util.dot.GrappaGraph;
import jqian.util.graph.Graph;
import soot.*;
import soot.baf.BafBody;
import soot.jimple.toolkits.typing.fast.SingletonList;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.graph.InverseGraph;
import test.Test;

public class SDGUtil implements AllTestCases{  
	public static SDG constructSDG(SootMethod entry,DepGraphOptions opts,boolean verbose, int javaLibDepth){
		Collection entries = new LinkedList();  
		entries.add(entry);		
		
		SDG sdg = constructSDG(PointsToAnalysisType.SPARK, HeapAbstraction.FIELD_SENSITIVE, entries,opts,verbose, javaLibDepth);
		return sdg;
	}
	
    /** Construct a SDG without building the summary edges and without perform global linking. 
     * @param pointerAnalysis can be "spark" or "type-based"
     * */
    public static SDG constructSDG(PointsToAnalysisType ptstoType, HeapAbstraction sideEffectOpt, 
    		Collection<?> entries,DepGraphOptions opts, boolean verbose, int javaLibDepth){
    	SDGBuilder sdgBuilder = constructSDGBuilder(ptstoType,sideEffectOpt, 
    				entries, opts, verbose, javaLibDepth);
		sdgBuilder.preBuild();
		sdgBuilder.buildAll();	
		sdgBuilder.postBuild();		
		
		SDG sdg = sdgBuilder.getSDG();	
		
		return sdg;
    }
     
    
	public static SDGBuilder constructSDGBuilder(PointsToAnalysisType ptstoType, 
			HeapAbstraction heabAbstraction, Collection<?> entries, DepGraphOptions opts, 
			boolean verbose, int javaLibDepth) {
		IPtsToQuery ptsto = PtsToHelper.createPointsToQuery(ptstoType);
		SideEffectAnalysis se = new SideEffectAnalysis(ptsto,entries, heabAbstraction);
		se.build();

		CFGProvider cfgProvider = new HammockCFGProvider();
		DUBuilder rdb = new DUBuilder(cfgProvider, ptsto, heabAbstraction, se, verbose);

		SDGBuilder sdgBuilder = new SDGBuilder(entries, opts, cfgProvider, 
				ptsto, heabAbstraction, rdb.getGlobalDUQuery(), se, verbose, javaLibDepth); 

		return sdgBuilder;
	}
    
    
    public static void showSlicingResult(String filename,DependenceGraph sdg,String criteria,Set slice){
    	GrappaGraph sliceGraph = null;
    	if(sdg instanceof PDG){
    		sliceGraph = DependenceGraphHelper.toGrappaGraph((PDG)sdg, slice);
    	}
    	else if(sdg instanceof SDG){
    		sliceGraph = DependenceGraphHelper.toGrappaGraph((SDG)sdg, slice);
    	}
     
    	sliceGraph.saveToDot(filename);
		Test.dotView(filename);

		System.out.println("\nSlicing result for "+criteria+" :");
		Test.printCollection(slice.iterator(), "\n");
    } 
    
    
    public static void showDependenceGraph(DependenceGraph depGraph,String dotfile){
    	GrappaGraph graph = null;
    	if(depGraph instanceof PDG){
    		graph = DependenceGraphHelper.toGrappaGraph((PDG)depGraph);
    	}
    	else if(depGraph instanceof SDG){
    		graph = DependenceGraphHelper.toGrappaGraph((SDG)depGraph);
    	}
   
    	graph.saveToDot(dotfile);
        Test.dotView(dotfile);
    }
    
    public static void main(String[] args){	
    	//String MAIN_CLASS = SLICE_MAIN_CLASS;
    	//String MAIN_CLASS = SDGCases.CLASSES[7];
    	String MAIN_CLASS = SDGCases.CLASSES[5];
    	
        	
    	Properties options = Test.loadConfig("/test/config.xml"); 
       	options.put("entry_class", MAIN_CLASS);
       	
    	Test.loadClasses(true);     
    	Test.doFastSparkPointsToAnalysis();
    	Test.simplifyCallGraph();
    	
    	//SootMethod m = Scene.v().getMethod("<sun.misc.CharacterDecoder: void decodeBuffer(java.io.InputStream,java.io.OutputStream)>");
    	//HammockCFG cfg = new HammockCFG(m.getActiveBody()); 
    	//Test.showCFG(m, cfg, "jgraph");
    	//Test.showCFG(m, new InverseGraph(cfg), "dot");
    	//jqian.sootex.dependency.DependencyHelper.calcCtrlDependences(cfg);
    	
    		    
    	
    	
    	//PointsToAnalysisType ptstoType = PointsToAnalysisType.SPARK;
    	//PointsToAnalysisType ptstoType = PointsToAnalysisType.TYPE_BASED;
    	PointsToAnalysisType ptstoType = PointsToAnalysisType.NAIVE;
    	
    	//HeapAbstraction seOpt = HeapAbstraction.FIELD_SENSITIVE;
    	//HeapAbstraction seOpt = HeapAbstraction.FIELD_BASED;
    	//HeapAbstraction seOpt = HeapAbstraction.TYPE_BASED;
    	HeapAbstraction seOpt = HeapAbstraction.NO_DISTINGUISH;
    	
    	SootMethod main = Scene.v().getMainMethod();
    	//SootMethod main = Scene.v().getMethod("<test.cases.SDG8: void test1()>");
    	
    	boolean withDepReason = false;
    	
    	//HeapAbstraction locAbstraction = HeapAbstraction.FIELD_SENSITIVE;
    	//HeapAbstraction locAbstraction = HeapAbstraction.FIELD_BASED;
    	//HeapAbstraction locAbstraction = HeapAbstraction.TYPE_BASED;
    	HeapAbstraction locAbstraction = HeapAbstraction.NO_DISTINGUISH;
    	
		DepGraphOptions opts = new DepGraphOptions(true, withDepReason, locAbstraction);
		int javaLibDepth = -1;
		//int javaLibDepth = 1;
		
    	SDG sdg = constructSDG(ptstoType, seOpt, new SingletonList(main), opts, true, javaLibDepth);
    	sdg.buildSummaryEdges(main);
		sdg.connectPDGs();  
		
		//SootMethod check = Scene.v().getMethod("<test.cases.SDG8: test.cases.SDG8$Int add(test.cases.SDG8$Int,test.cases.SDG8$Int)>");
		//Test.showCFG(check);
		//PDG pdg = sdg.getPDG(check);
        //SDGUtil.showDependenceGraph(pdg, "./output/dot/pdg.dot");    	 
		
		//SDGUtil.showDependenceGraph(sdg,"./output/dot/sdg.dot");     
	}
}
