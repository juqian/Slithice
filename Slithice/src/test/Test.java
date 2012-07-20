package test;

import java.io.*;
import java.util.*;

import soot.*;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.CallGraphBuilder;
import soot.jimple.toolkits.pointer.DumbPointerAnalysis;
import soot.options.Options;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.graph.UnitGraph;
import jqian.sootex.CFGProvider;
import jqian.sootex.Cache;
import jqian.sootex.dependency.pdg.*;
import jqian.sootex.dependency.pdg.builder.AbstractPDGBuilder;
import jqian.sootex.dependency.pdg.builder.UnsafePDGBuilder;
import jqian.sootex.du.DUBuilder;
import jqian.sootex.du.IGlobalDUQuery;
import jqian.sootex.du.IReachingDUQuery;
import jqian.sootex.location.HeapAbstraction;
import jqian.sootex.ptsto.IPtsToQuery;
import jqian.sootex.sideeffect.*;
import jqian.sootex.util.CFGViewer;
import jqian.sootex.util.HammockCFG;
import jqian.sootex.util.SootUtils;
import jqian.sootex.util.callgraph.CallGraphRefiner;
import jqian.sootex.util.callgraph.DirectedCallGraph;
import jqian.sootex.util.graph.GraphHelper;
import jqian.util.Configurator;
import jqian.util.PathUtils;
import jqian.util.Utils;
import jqian.util.CollectionUtils;
import jqian.util.dot.DotViewer;
import jqian.util.dot.GrappaGraph;
import jqian.util.graph.Graph;
import jqian.util.jgraphx.GraphViewer;

/**
 * @author bruteforce
 *
 */
public class Test {
	public static Properties _config;
	public static boolean _debug;
	public static boolean _verbose;
	public static PrintStream out = System.out; 
	
	public final static String DEFAULT_CONFIG_FILE = "/config.xml";
	
	public static Properties loadConfig(String configFile){		
		Configurator conf = new Configurator();
		File file = new File(configFile);
 		if(file.isAbsolute()){
 			_config = conf.parse(configFile);
 		}
 		else{
 			InputStream is = Test.class.getResourceAsStream(configFile);	
 			_config = conf.parse(is);		
 		} 
		 	
		_debug = Boolean.valueOf(_config.getProperty("debug"));
		_verbose = Boolean.valueOf(_config.getProperty("verbose"));
		return _config;
	}
	
	
	public static void setDefaultSootOptions(Properties config, boolean isWholeProgramAnalysis, boolean inShimple){
		String jdkPath = _config.getProperty("jdk_path");
		String classpath = _config.getProperty("classpath");		
		String outputDir = "./output/soot"; 
		
		File file = new File(outputDir);
		file.mkdirs();
		//clearDirectory(outputDir);
		
		classpath  = PathUtils.getJreClasspath(jdkPath) + ";" + classpath;
		
		if(isWholeProgramAnalysis){
			if(inShimple){
				Options.v().set_whole_shimple(true);
				Options.v().set_via_shimple(true);
			}
			else{
				Options.v().set_whole_program(isWholeProgramAnalysis);
			}
		}		 
		 
		Options.v().set_verbose(false);
		Options.v().set_soot_classpath(classpath);
		Options.v().set_output_dir(outputDir);
		Options.v().set_output_format(Options.output_format_none);
		Options.v().set_keep_line_number(true);
		//Options.v().print_tags_in_output();
		Options.v().set_print_tags_in_output(true);	
		Options.v().setPhaseOption("jb", "use-original-names:true");
		PhaseOptions.v().setPhaseOption("jb", "use-original-names:true");		 
		//Options.v().set_validate(true);
	}
	 
	public static Collection<SootClass> loadClasses(boolean isWholeProgramAnalysis){
		Date startTime = new Date();
		
		setDefaultSootOptions(_config, isWholeProgramAnalysis, false);
		
		//load classes for analysis
		String entryClass = _config.getProperty("entry_class");	
		SootUtils.loadClassesForEntry(entryClass);
	 
		Date endTime=new Date();	
		Test.out.println("Load " + Scene.v().getClasses().size() + " soot classes in "+ Utils.getTimeConsumed(startTime,endTime));
		
		SootUtils.numberClassAndFields();
		return Scene.v().getClasses();
	} 
 
	
	public static void printCollection(Iterator<?> first,String separator){
	    String str = CollectionUtils.toString(first,separator);
	    Test.out.print(str);
	} 
	
	public static void dotView(String dotfilename){
		 DotViewer dotView = new DotViewer(_config.getProperty("dot"),dotfilename);
	     dotView.dotIt();
	     dotView.view(); 
	}
	
	
	public static void showGraph(Graph graph, String tool){
		if(tool.equals("dot")){			 
	        String dotfilename= "./output/dot/"+graph.getTitle()+".dot";        
	        dotfilename = PathUtils.toValidFilepath(dotfilename);
	        
	        GrappaGraph ggraph = new GrappaGraph(graph);
	        ggraph.saveToDot(dotfilename);
	   
	        DotViewer dotView = new DotViewer(_config.getProperty("dot"),dotfilename);
	        dotView.dotIt();
	        dotView.view();
		}
		else if(tool.equals("jgraph")){
			GraphViewer frame = new GraphViewer(graph);
			frame.setSize(1000, 700);
			frame.setVisible(true);
		}		
	}
	
	public static void showCFG(SootMethod m, DirectedGraph<Unit> ucfg, String tool){
	    CFGViewer viewer = new CFGViewer(m,ucfg);
        Graph cfg = viewer.makeJimpleCFG();	 
        cfg.setTitle("jimple_"+m.getName());        
        showGraph(cfg, tool);
	}
	
	public static void showCFG(SootMethod m, String tool){	
		Body body = m.getActiveBody();
		DirectedGraph<Unit> ucfg=new BriefUnitGraph(body);
		showCFG(m, ucfg, tool);    
	}
	
	public static void showCFG(SootMethod m){		 
		showCFG(m, "jgraph");    
	}	
	
	public static void printBody(Body body){
		for(Unit s: body.getUnits()){	
			int line = SootUtils.getLine(s);
			Test.out.println(""+line+": " + s);
		}		
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void showCallGraph(CallGraph cg){		
		List entries = Scene.v().getEntryPoints();
    	DirectedGraph dcg = new DirectedCallGraph(cg, entries);
    	Graph displayedCallgraph = GraphHelper.toDisplayGraph(dcg, "Call Graph");    	 
    	showGraph(displayedCallgraph, "jgraph");
	}
	
	public static void buildCHACallGraph(){ 
 		List<SootMethod> entries = EntryPoints.v().application();
		entries = entries.subList(0, 1);
		Scene.v().setEntryPoints(entries);

		//PhaseOptions.v().setPhaseOption("cg", "implicit-entry:false");
		//PhaseOptions.v().setPhaseOption("cg.spark", "simulate-natives:false");
		 
		Date startTime = new Date();		 
		
	    CallGraphBuilder cg = new CallGraphBuilder( DumbPointerAnalysis.v() );
	    cg.build();
	    
	    Date endTime=new Date();	    
		System.out.println("[Call Graph] Call graph built in "+ Utils.getTimeConsumed(startTime,endTime) 
				           + ", " + Scene.v().getReachableMethods().size() +" methods reachable.");    
    }
	
	
	public static void doFastSparkPointsToAnalysis() {
		Map<String,String> opt = new HashMap<String,String>();
		opt.put("simulate-natives","false");   
		opt.put("implicit-entry","false");
		SootUtils.doSparkPointsToAnalysis(opt);
	}
	
	public static PDG buildUnsafePDG(SootMethod m,IPtsToQuery pt2Query,IReachingDUQuery rd,boolean withCtrlDep){
   	    DepGraphOptions options = new DepGraphOptions(withCtrlDep,false,HeapAbstraction.FIELD_SENSITIVE);
   	    UnitGraph cfg = new HammockCFG(m.retrieveActiveBody());
		AbstractPDGBuilder pdgbd = new UnsafePDGBuilder(m,cfg,options,pt2Query, HeapAbstraction.FIELD_SENSITIVE, rd);	 
		pdgbd.build();
		PDG depGraph = pdgbd.getPDG();
		Graph graph = GraphHelper.toDisplayGraph(depGraph.toDirectedGraph(), "PDG_"+m.getName());	
		showGraph(graph, "jgraph");		
		
		return depGraph;
	}
	 
	public static ISideEffectAnalysis loadSideEffector(IPtsToQuery query){
		SideEffectAnalysis se = new SideEffectAnalysis(query,EntryPoints.v().application(), HeapAbstraction.FIELD_SENSITIVE);
    	se.build();
    	return se;
	}
	
	public static String getEntrySignature(String mainClass){
		return "<"+mainClass+": void main(java.lang.String[])>";
	}
	
	public static CallGraph simplifyCallGraph(){
		CallGraph cg = Scene.v().getCallGraph();				
		PointsToAnalysis ptsTo = Scene.v().getPointsToAnalysis();
		CallGraphRefiner refiner = new CallGraphRefiner(ptsTo, true);   	
		CallGraph newCg = refiner.refine(cg, new CallGraphRefiner.AggressiveCallGraphFilter());				    
		Scene.v().setCallGraph(newCg);				
	    Scene.v().setReachableMethods(null);   //update reachable methods 
	    return newCg;
	}
	
    
	static IGlobalDUQuery createDUQuery(IPtsToQuery ptsto, CFGProvider cfgProvider, boolean buildAll){
		ISideEffectAnalysis sideEffector = loadSideEffector(ptsto); 
         
        Date startTime = new Date(); 
        
        DUBuilder du = new DUBuilder(cfgProvider, ptsto, HeapAbstraction.FIELD_SENSITIVE,  sideEffector, true); 	    
    	IGlobalDUQuery duQuery = du.getGlobalDUQuery(); 
        
    	if(buildAll){
    		 List<MethodOrMethodContext> rm = Cache.v().getTopologicalOrder();             
    	    	for(Iterator<MethodOrMethodContext> it = rm.iterator();it.hasNext();){
    	    	     SootMethod m = (SootMethod)it.next();
    	    	     if(m.isConcrete()){
    	    	    	 duQuery.getRDQuery(m);
        	    	     duQuery.getRUQuery(m);
    	    	     }    	    	     
    	    	}
    	    	
    	    	Date endTime=new Date();	    
    		    Test.out.println("Test finish in "+ Utils.getTimeConsumed(startTime,endTime));	
    	}
    	
    	return duQuery;
	}
}
