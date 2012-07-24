package jqian.slicer.core;

import java.io.PrintStream;
import java.util.*;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

import jqian.Global;
import jqian.slicer.plugin.*;
import jqian.slicer.view.*;
import jqian.util.*;
import jqian.util.eclipse.ConsoleUtil;
import jqian.slicer.plugin.view.*;
import jqian.sootex.dependency.pdg.DependenceGraphHelper;
import jqian.sootex.dependency.pdg.DepGraphOptions;
import jqian.sootex.dependency.pdg.SDG;
import jqian.sootex.ptsto.IPtsToQuery;
import jqian.sootex.ptsto.NaivePtsToQuery;
import jqian.sootex.ptsto.PointsToAnalysisType;
import jqian.sootex.ptsto.SparkPtsToQuery;
import jqian.sootex.ptsto.TypeBasedPointsToAnalysis;
import jqian.sootex.ptsto.TypeBasedPtsToQuery;
import jqian.sootex.sideeffect.ISideEffectAnalysis;
import jqian.sootex.sideeffect.SideEffectAnalysis;
import jqian.sootex.sideeffect.SideEffectAnalysisEx;
import jqian.sootex.util.SootUtils;
import jqian.sootex.util.callgraph.CallGraphRefiner;
import soot.*;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.CallGraphBuilder;
import soot.jimple.toolkits.pointer.DumbPointerAnalysis;
import soot.options.Options;

public class SDGConstructor implements Runnable{		
	private String _prjEntry;
	private String _sdgEntry;
	private String _classpath;     
	private String _temppath;
	private SlicerOptions _options;
	private SlithiceSlicer _slicer;  
	    
 
	/**
	 * @param sdgEntry    Currently, this parameter is not actually used
	 */
	public SDGConstructor(SlithiceSlicer slicer,String prjEntry,String sdgEntry,
			              String classpath, String temppath,SlicerOptions options){			  
		this._prjEntry = prjEntry;	
		this._sdgEntry = sdgEntry;	
		this._slicer = slicer;
		this._classpath = classpath;
		this._temppath = temppath;
		this._options = options;
	} 
 
	
	private void loadSootClasses(){
		Date startTime = new Date();
 
		Options.v().set_whole_program(true);
		Options.v().set_app(true);
		Options.v().set_verbose(false);
		Options.v().set_soot_classpath(_classpath);
		Options.v().set_output_dir( _temppath);
		Options.v().set_output_format(Options.output_format_none);
		Options.v().set_keep_line_number(true);
		Options.v().set_print_tags_in_output(true);	
		Options.v().setPhaseOption("jb", "use-original-names:true");
		PhaseOptions.v().setPhaseOption("jb", "use-original-names:true");		 
		//Options.v().set_validate(true);
		
		//load classes for analysis		 
		SootUtils.loadClassesForEntry(_prjEntry);
		SootUtils.numberClassAndFields();
		
		Date endTime=new Date();	
		Global.v().out.println("["+ID.DISPLAY_NAME+"] Load " + Scene.v().getClasses().size() + 
				               " soot classes in "+ Utils.getTimeConsumed(startTime,endTime));
	} 
	
	
	private IPtsToQuery doPointsToAnalysis(){
		 // do points-to analysis
        if(_options.pointToAnalysis==PointsToAnalysisType.SPARK){
        	Date startTime = new Date();  
        	Map<String,String> opt = new HashMap<String,String>();
        	if(_options.simplifyCallGraph){
        		opt.put("simulate-natives","false");   
        		opt.put("implicit-entry","false");
        	}    		
    		SootUtils.doSparkPointsToAnalysis(opt);
    		Date endTime = new Date();
    	    Global.v().out.println("["+ID.DISPLAY_NAME+"] Finish points-to analysis in "+ Utils.getTimeConsumed(startTime,endTime));
    	        
    		// simplify call graph, ignore method not reachable from main entry
    		// ignore implicit calls (except thread calls)
    		CallGraph cg = Scene.v().getCallGraph();				
    		PointsToAnalysis ptsTo = Scene.v().getPointsToAnalysis();
    		CallGraphRefiner refiner = new CallGraphRefiner(ptsTo, false);   	
    		CallGraph newCg = refiner.refine(cg, new CallGraphRefiner.AggressiveCallGraphFilter());				    
    		Scene.v().setCallGraph(newCg);				
    	    Scene.v().setReachableMethods(null);   //update reachable methods 
    	    
    	    return new SparkPtsToQuery();
        }
        else if(_options.pointToAnalysis==PointsToAnalysisType.TYPE_BASED){
        	// build a CHA call graph
        	CallGraphBuilder cg = new CallGraphBuilder( DumbPointerAnalysis.v() );
    	    cg.build();
    	    
    	    // use type-based points-to analysis instead
    	    PointsToAnalysis pta = TypeBasedPointsToAnalysis.v(false);
    	    Scene.v().setPointsToAnalysis(pta);
    	    
    	    return new TypeBasedPtsToQuery(false);
        } 
        else if(_options.pointToAnalysis==PointsToAnalysisType.NAIVE){
        	PointsToAnalysis pta = DumbPointerAnalysis.v();
        	// build a CHA call graph
        	CallGraphBuilder cg = new CallGraphBuilder(pta);
    	    cg.build(); 
    	    Scene.v().setPointsToAnalysis(pta);
    	    
    	    return new NaivePtsToQuery();
        }
        else{
        	throw new RuntimeException("["+ID.DISPLAY_NAME+"] constructSDG() error: points-to analysis="+ _options.pointToAnalysis + " unsupported.");
        }
	}
	
	public ISideEffectAnalysis doSideEffectAnalysis(IPtsToQuery ptsto, SootMethod entry){
    	Collection<SootMethod> entries = new ArrayList<SootMethod>(1);
		entries.add(entry);
		
        // collect method side-effects
		SideEffectAnalysis se = new SideEffectAnalysis(ptsto,entries, _options.heapAbstraction);
		//SideEffectAnalysisEx se = new SideEffectAnalysisEx(ptsto,entries, _options.heapAbstraction);
		se.build();
		return se;
	}
	
	private SDG constructSDG(){
		// get entry method for SDG construction
		SootMethod entry = null;	 
		if(_sdgEntry!=null){
			entry = Scene.v().getMethod(_sdgEntry);
		}
		else{
			entry = (SootMethod)Scene.v().getEntryPoints().get(0);
		}
		
		Global.v().out.println("\n["+ID.DISPLAY_NAME+"] Constructing system dependence graph(SDG) ... ");   
		
        Date startTime = new Date();  
        
        // points-to analysis
        IPtsToQuery ptsto = doPointsToAnalysis();
        _slicer.setPtsToQuery(ptsto);
        
        // side-effect analysis
        ISideEffectAnalysis se = doSideEffectAnalysis(ptsto, entry);
        
        // dependence graph construction
		DepGraphOptions opts = new DepGraphOptions(true, _options.distinguishDULocInDepEdges, _options.sdgFormalActualOption);
		SDG sdg = DependenceGraphHelper.constructSDG(ptsto, _options.heapAbstraction, se, entry, opts, true, _options.libTracingDepth);
		
        Date endTime = new Date();
        Global.v().out.println("["+ID.DISPLAY_NAME+"] SDG successfully constructed in "+ Utils.getTimeConsumed(startTime,endTime));
        
        return sdg;
	}
	
	private void showSDGReadyMessage(){
		class ShowMsg implements Runnable{
			public void run(){
				Shell shell = WorkbenchHelper.getActiveShell();
				BalloonMessage.showMessage(shell,ID.DISPLAY_NAME,"Dependence graph ready, slicing can be performed now.");				
			}
		}
		
		IWorkbench workbench = PlatformUI.getWorkbench();
		workbench.getDisplay().syncExec(new ShowMsg());
	}
	
	private void showSDG(SDG sdg){
		String filename = _temppath + "/sdg.dot";
		String dotpath = _options.dotpath;
		
		Global.v().out.print("\n["+ID.DISPLAY_NAME+"] Doting SDG file (can be very slow for large SDG): "+filename+" ... ...");
		DependenceGraphHelper.dotDependenceGraph(sdg, dotpath, filename, false);
		//DependenceGraphHelper.showDependenceGraphByJGraph(sdg, "SDG");
		Global.v().out.print("OK\n"); 
		
		ImageView.showImage(filename+".jpg");
	}
	
	
	public void run(){
		//clean soot
        SootUtils.resetSoot();
 
		// redirect output to the specified console
		PrintStream out = ConsoleUtil.getConsoleOutputStream(ID.CONSOLE);
		Global.v().out = out;  
        
		loadSootClasses();
		
		// resetting previous analysis results
		Global.v().reset(); 
		Global.v().out = out;  
		
		SDG sdg = constructSDG();
		
		//assign the constructed SDG to the slicer
		_slicer.setSDG(sdg);		
		
		//show SDG ready
		showSDGReadyMessage();

        //show SDG
		boolean showSDG = _options.showJimpleSDG;
        if(showSDG){
        	showSDG(sdg);
        }
	}
}
