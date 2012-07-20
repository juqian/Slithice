package jqian.slicer.core;

import java.util.HashMap;
import java.util.Map;

import jqian.sootex.location.HeapAbstraction;
import jqian.sootex.ptsto.PointsToAnalysisType;

public class SlicerOptions {
	public String dotpath = "";
	public PointsToAnalysisType pointToAnalysis = PointsToAnalysisType.SPARK;
	public HeapAbstraction heapAbstraction = HeapAbstraction.FIELD_BASED;
	public HeapAbstraction sdgFormalActualOption = HeapAbstraction.FIELD_BASED;
	 
	public int libTracingDepth = -1;
	 
	public boolean showJimpleSDG;
	public boolean showSliceInSDG; 
	
	public boolean simplifyCallGraph; 	 
	public boolean ignoreJreClinits;
	public boolean distinguishDULocInDepEdges = false;
	
	public boolean verbose = true;
	public boolean useDepNavigator = true;
	
	public SlicerOptions(SlicerOptions o){
		if(o==null)
			return;
		
		this.dotpath = o.dotpath;
		this.pointToAnalysis = o.pointToAnalysis;
		this.heapAbstraction = o.heapAbstraction;
		this.sdgFormalActualOption = o.sdgFormalActualOption;
		this.libTracingDepth = o.libTracingDepth;
		this.showJimpleSDG = o.showJimpleSDG;
		this.verbose = o.verbose;
		this.simplifyCallGraph = o.simplifyCallGraph;
		this.ignoreJreClinits = o.ignoreJreClinits;
		this.distinguishDULocInDepEdges = o.distinguishDULocInDepEdges;
		this.showSliceInSDG = o.showSliceInSDG;
		this.useDepNavigator = o.useDepNavigator;
	}
	
	public SlicerOptions(){
		
	}
	
	public SlicerOptions(Map<String, String> opt){
		if(opt==null)
			return; 
		 
		String text;
		this.dotpath = opt.get("dot_path");
		
		text = opt.get("points_to_analysis");
		text = text.trim();		
		this.pointToAnalysis = PointsToAnalysisType.valueOf(text);
		
		text = opt.get("heap_abstraction");
		text = text.trim();
		this.heapAbstraction = HeapAbstraction.valueOf(text);
		
		text = opt.get("sdg_formal_actual_option");
		text = text.trim();
		this.sdgFormalActualOption = HeapAbstraction.valueOf(text);
		
		
		this.libTracingDepth = Integer.parseInt(opt.get("lib_trace_depth"));
		
		this.showJimpleSDG = Boolean.parseBoolean(opt.get("show_jimple_sdg")); 
		this.verbose = Boolean.parseBoolean(opt.get("verbose")); 
		this.simplifyCallGraph = Boolean.parseBoolean(opt.get("simplify_call_graph")); 
		this.ignoreJreClinits = Boolean.parseBoolean(opt.get("ignore_jre_clinits")); 
		this.distinguishDULocInDepEdges = Boolean.parseBoolean(opt.get("distinguish_du_locs_in_dep_edges")); 
		this.showSliceInSDG = Boolean.parseBoolean(opt.get("show_slice_in_sdg")); 
		this.useDepNavigator = Boolean.parseBoolean(opt.get("use_dep_navigator")); 
	}
	
	public Map<String, String> toOptionMap(){
		Map<String,String> options = new HashMap<String,String>();
		options.put("dot_path", dotpath);
		options.put("points_to_analysis", pointToAnalysis.toString());
		options.put("heap_abstraction", heapAbstraction.toString());
		options.put("sdg_formal_actual_option", sdgFormalActualOption.toString());
		options.put("lib_trace_depth", ""+libTracingDepth);
		options.put("show_jimple_sdg", ""+showJimpleSDG);
		options.put("verbose", ""+verbose);
		options.put("simplify_call_graph", ""+simplifyCallGraph); 
		options.put("ignore_jre_clinits", ""+ignoreJreClinits);  
		options.put("distinguish_du_locs_in_dep_edges", ""+distinguishDULocInDepEdges);
		options.put("show_slice_in_sdg", ""+showSliceInSDG);  
		options.put("use_dep_navigator", ""+useDepNavigator); 
		return options;
	}
}
