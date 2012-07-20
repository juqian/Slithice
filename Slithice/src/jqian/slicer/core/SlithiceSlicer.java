package jqian.slicer.core;

import java.util.*;
import java.io.*;

import org.eclipse.swt.widgets.*;
import org.eclipse.jdt.core.*;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.core.resources.*;
import org.eclipse.ui.*;
import org.eclipse.ui.texteditor.ITextEditor;

import soot.*;
import soot.jimple.*;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.DirectedGraph;

import jqian.Global;
import jqian.util.*;
import jqian.util.dot.DotViewer;
import jqian.util.dot.GrappaGraph;
import jqian.util.eclipse.ConsoleUtil;
import jqian.util.eclipse.JDTUtils;
import jqian.util.graph.Graph;
import jqian.util.jgraphx.GraphViewer;
import jqian.slicer.view.global.*;
import jqian.slicer.util.*;
import jqian.slicer.plugin.view.*;
import jqian.slicer.plugin.*;
import jqian.sootex.dependency.pdg.ActualNode;
import jqian.sootex.dependency.pdg.DependenceEdge;
import jqian.sootex.dependency.pdg.DependenceGraphHelper;
import jqian.sootex.dependency.pdg.DependenceNode;
import jqian.sootex.dependency.pdg.EntryNode;
import jqian.sootex.dependency.pdg.JavaStmtNode;
import jqian.sootex.dependency.pdg.JimpleStmtNode;
import jqian.sootex.dependency.pdg.PDG;
import jqian.sootex.dependency.pdg.SDG;
import jqian.sootex.dependency.slicing.GlobalSlicer;
import jqian.sootex.dependency.slicing.JavaSlicingCriterion;
import jqian.sootex.dependency.slicing.JimpleSlicingCriterion;
import jqian.sootex.dependency.slicing.LocalSlicer;
import jqian.sootex.ptsto.IPtsToQuery;
import jqian.sootex.util.CFGViewer;
import jqian.sootex.util.SootUtils;
import jqian.sootex.util.callgraph.CallGraphDotter;


/**
 * Core slicer engine
 *
 */
public class SlithiceSlicer {	
	private static SlithiceSlicer _instance;
	
	public static SlithiceSlicer v(){
		if(_instance==null)
			_instance = new SlithiceSlicer();
		
		return _instance;
	}
	
	private String _entrySignature;
	private IJavaProject _project;
	private SlicerOptions _options;
	private volatile SDG _sdg;
	private IPtsToQuery _ptsto;
	private volatile boolean _inSlicing;
	private volatile ISliceDistribution _currentDistribution;
	
	private SlithiceSlicer(){	
		loadConfig();
	}
	
	public boolean inCurrentProject(IFile file){
		return true;
	}
	
	public boolean isConfigurated(){
		return true;
	}
	
	public SlicerOptions getConfiguration(){
		return _options;
	}
	
	public boolean useDependenceNavigator(){
		return _options.useDepNavigator;
	}
	
	public ISliceDistribution getCurrentSliceDistribution(){
		return _currentDistribution;
	}
	
	public void reconfig(SlicerOptions opt){
		//update configuration
		this._options = new SlicerOptions(opt);		 
		 
		//dump the new configuration to file
		String path = getConfigFilePath();
		Map<String,String> optMap = opt.toOptionMap();
		
		String content = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<Config>\n\n";
		
		for(Map.Entry<String,String> entry : optMap.entrySet()){
			String key = entry.getKey();
			content += "<"+key+">"+entry.getValue()+"</"+key+">\n";		
		}
		
		content += "\n</Config>\n";
		
		try{
			File file = new File(path);
			if(!file.exists()){
				File parent = file.getParentFile();
				if(!parent.exists()) 
					parent.mkdir();
				
				file.createNewFile();
			}
			
			PrintStream out = new PrintStream(new FileOutputStream(path));
			out.print(content);
			out.close();
		}
		catch(IOException e){	
			e.printStackTrace();
		}		
	}
	
	private String getConfigFilePath(){
		String path = PathUtil.getPyxisConfigurationPath();
		path += File.separator + "config.xml";
		return path;
	}
	
	private String getDefaultDotPath(){
		String prjroot = PathUtil.getPluginPath();		
		return prjroot + File.separator + "lib" + File.separator +"dot"+ File.separator + "dot.exe";
	}
	
	private final void loadConfig(){
		try{
			String path = getConfigFilePath();
			Properties props = null;
			
			File file = new File(path);
			if(file.exists()){				 
				ConsoleUtil.println(ID.CONSOLE, "Load configuration from: "+path);
				
				Configurator conf = new Configurator();
				props = conf.parse(path);
				if(props!=null){
					Map<String, String> optMap = new HashMap<String,String>();
					for(Iterator<?> it = props.entrySet().iterator();it.hasNext();){
						Map.Entry<?,?> entry = (Map.Entry<?,?>)it.next();
						
						String key = (String)entry.getKey();
						String val = (String)entry.getValue();
						optMap.put(key, val);
					}        
			        
					_options = new SlicerOptions(optMap);
					
					 //append default configuration			        
			        if(_options.dotpath==null || _options.dotpath.equals("")){        	
			    		_options.dotpath = getDefaultDotPath();
			        }
				}
			}
			
			if(props==null){
				_options = defaultConfig();
			}
		}
		catch(Exception e){
			ConsoleUtil.printError(ID.CONSOLE, "Load config: " + e.getMessage());
			_options = defaultConfig();
		}
		
		//InputStream stream = Path.getFileByName(getClass(),_options.getFileOfSpecials());
		//BufferedReader in = new BufferedReader(new InputStreamReader(stream));
	}
	
	public SlicerOptions defaultConfig(){
		SlicerOptions options = new SlicerOptions();
		options.verbose = true;
		options.simplifyCallGraph = true;
		options.ignoreJreClinits = true;
		//options.showSliceInSDG = true; 		
		options.useDepNavigator = true;		 
		options.dotpath = getDefaultDotPath();
		return options;		 
	}
	
	public void setProject(IJavaProject project,String entry){
		if(!project.equals(this._project) ||
		   !entry.equals(this._entrySignature)){
			ConsoleUtil.print(ID.CONSOLE,"\n======================== Project Reset ============================");
			ConsoleUtil.print(ID.CONSOLE,"\nProject Entry: " + entry);
			
			this._project = project;
			this._entrySignature = entry;
			
			//clear original SDG
			this._sdg = null;
			
			//construct SDG in a new thread
			//TODO how to handle hot code modification
			String classpath = PathUtil.getClassPath(_project);
			String temppath = getCurPrjTemporalPath();
			SDGConstructor constructor = new SDGConstructor(this,_entrySignature,null,classpath,temppath,_options);
			Thread t= new Thread(constructor);
			t.start();
		}
	}
	
	public void doSlicing(Shell shell,String startMethod,int line,Collection<String> vars,
			              boolean postExecution,boolean sliceInGlobal){
		if(_sdg==null){
			MessageDialog.openInformation(shell,ID.DISPLAY_NAME, "No dependence graph avaliable"
					+"Right click on a project, and select a project entry from menu \"Program"
					+" Slicing\" -> \"Set project entry\" to force dependence graph construction");
			return;
		}
		
		if(_inSlicing){
			MessageDialog.openInformation(shell,ID.DISPLAY_NAME, "A slicing operation is already"
					+"in process, please wait for its finishing and then try again.");
			return;
		}
		
		//do slicing in a new thread
		JavaSlicingCriterion javaCriterion = new JavaSlicingCriterion(vars,startMethod,line,postExecution);
		Collection<JimpleSlicingCriterion> criteria = javaCriterion.toJimpleCriterion(_ptsto, _options.heapAbstraction);
		
		if(criteria==null || criteria.isEmpty()){
			MessageDialog.openInformation(shell,"Invalid Slicing Criterion", 
					"Make sure the specified line, the specified variables and the specified method exists. \n"
					+"Also make sure the specified point is reachable from your project entry.\n"
					+"\n(Compiler optimization may also cause problems for the tool. Please \n"
					+" carefully check whether the line number and variable name information\n"
					+" is kept in the classfile, and check whether the code could be eliminated\n"
					+" during optimization.)");
			return;
		}
		
		SlicingThread slicing = new SlicingThread(criteria,sliceInGlobal);
		Thread t= new Thread(slicing);
		t.start();
	}
	
	
	class SlicingThread implements Runnable{
		Collection<JimpleSlicingCriterion> _criteria;
		boolean _sliceInGlobal;
		
		public SlicingThread(Collection<JimpleSlicingCriterion> criteria,boolean sliceInGlobal){
			this._criteria = criteria;
			this._sliceInGlobal = sliceInGlobal;
		}
		
		public void run(){
			_inSlicing = true;
			
			Collection<DependenceNode>  result = null;
			if(_sliceInGlobal){
				GlobalSlicer slicer = new GlobalSlicer(_sdg);
				result = slicer.slice(_criteria);
			}
			else{
				JimpleSlicingCriterion c = _criteria.iterator().next();
				MethodOrMethodContext mc = c.context();
				PDG pdg = _sdg.getPDG(mc);
				if(pdg==null){
					ErrorPrinter.printError("No dependence graph found for: "+mc);
					return;
				}
				
				LocalSlicer slicer = new LocalSlicer(pdg);
				result = slicer.slice(_criteria);
			}			
			
			_currentDistribution = new SliceDistribution(_project.getProject(),_criteria,result);
			
			class ShowView implements Runnable{
				ISliceDistribution _distribution;
				
				public ShowView(ISliceDistribution distribution){
					this._distribution = distribution;
				}
				public void run(){					
					PluginSliceViewer view = (PluginSliceViewer)WorkbenchHelper.openView(ID.GLOBAL_VIEW_ID);  
					view.setProject(_project.getProject(),_distribution); 
					
					//highlight current editor
					PluginSliceViewer.showSliceInCurrentEditor(_distribution);
				}
			}
			
			//show global slice view
			IWorkbench workbench = PlatformUI.getWorkbench();
			workbench.getDisplay().syncExec(new ShowView(_currentDistribution));
			
			boolean showSlices = _options.showSliceInSDG;
			if(showSlices){				
				GrappaGraph sliceGraph = DependenceGraphHelper.toGrappaGraph(_sdg, result);
				String filename = getCurPrjTemporalPath() + "/slice.dot";
				sliceGraph.saveToDot(filename);
				
				ConsoleUtil.print(ID.CONSOLE,"\nDoting Slice file: "+filename+" ... ...");
  
				DotViewer dotView = new DotViewer(_options.dotpath,filename, "jpg");
				dotView.dotIt();
				
				ConsoleUtil.print(ID.CONSOLE,"OK\n");
				//dotView.view();
				ImageView.showImage(filename+".jpg");
			}
			
			ConsoleUtil.println(ID.CONSOLE, "\nSlicing result:");				 
			ConsoleUtil.println(ID.CONSOLE, CollectionUtils.toString(result.iterator(),"\n"));
			
			_inSlicing = false;
		}
	}
		
	
	public String getCurPrjTemporalPath(){
		String path = PathUtil.getProjectPath(_project);
		if(path==null){
			path = "/slice_temp";
		}
		else
			path += "/slice_temp";
		
		File file = new File(path);
		if(!file.exists())
		    file.mkdir();
		
		return path;
	}
	
	public String getEntryClass(){
		return _entrySignature;
	}
	
	public void reset(){
		if(_project!=null){
			//clear data in temp directory
			String tmppath = getCurPrjTemporalPath();
			File file = new File(tmppath);
			File[] contents = file.listFiles();
			for(int i=0;i<contents.length;i++){
				contents[i].delete();
			}
		}
		
		Global.v().reset();
		SootUtils.resetSoot();
		
		_entrySignature = null;
		_project = null;
		_sdg = null;
		_currentDistribution = null;
		_inSlicing = false;
	}
	
	static class PDGLabelFetcher extends DependenceGraphHelper.LabelProvider{
		ITextEditor _editor;
		
		public PDGLabelFetcher(ITextEditor editor){
			this._editor = editor;
		}
		
		public String getLabel(DependenceNode node){
			if(node instanceof JavaStmtNode){
				int line = ((JavaStmtNode)node).getLine();				
				String text = JDTUtils.getLineText(_editor, line-1,true,false);
				if(text.length()>40){
					text = text.substring(0,40)+"...";
				}
				
				return text;
			}
			else{
				return super.getLabel(node);
			}
		}
	}
	
	public boolean dotJavaPDG(final ITextEditor editor,String methodSignature,String dotfile){
		if(_sdg==null)
			return false;
		
		try{
			SootMethod m = Scene.v().getMethod(methodSignature);
			final PDG pdg = _sdg.getPDG(m);	
			if(pdg==null){
				ErrorPrinter.printError("No dependence graph found for method: "+methodSignature);
				return false;
			}
			
			final String dotpath = _options.dotpath;	
			String tmppath = getCurPrjTemporalPath();
			
			if(tmppath==null)
				tmppath = "";
			
			final String filepath = tmppath + '/' +dotfile;
			
			class DotThread extends Thread{
				public void run(){
					try{
						PDGLabelFetcher fetcher = new PDGLabelFetcher(editor);
						PDG jpdg = pdg.toJavaStmtDepGraph();
						DependenceGraphHelper.toGrappaGraph(jpdg, Collections.EMPTY_LIST, fetcher).saveToDot(filepath);
						
						ConsoleUtil.print(ID.CONSOLE,"\nDoting Java level PDG file: "+filepath+" ... ...");
						
						DotViewer dotView = new DotViewer(dotpath,filepath, "jpg");
						dotView.dotIt();
						
						ConsoleUtil.print(ID.CONSOLE,"OK\n");
						ImageView.showImage(filepath+".jpg");
					}
					catch(Exception e){
						ConsoleUtil.printError(ID.CONSOLE, e.getMessage());
					}
				}
			}			
			
			DotThread thread = new DotThread();
			thread.start();
			
			return true;
		}
		catch(Exception e){
			ConsoleUtil.printError(ID.CONSOLE,e.getMessage());
		}
		
		return false;
	}
	
	
	public boolean dotJimplePDG(final ITextEditor editor,String methodSignature,String dotfile){
		if(_sdg==null)
			return false;
		
		try{
			SootMethod m = Scene.v().getMethod(methodSignature);
			final PDG pdg = _sdg.getPDG(m);	
			if(pdg==null){
				ErrorPrinter.printError("No dependence graph found for method: "+methodSignature);
				return false;
			}
			
			final String dotpath = _options.dotpath;	
			String tmppath = getCurPrjTemporalPath();
			
			if(tmppath==null)
				tmppath = "";
			
			final String filepath = tmppath + '/' +dotfile;
			
			class DotThread extends Thread{
				public void run(){
					try{
						PDGLabelFetcher fetcher = new PDGLabelFetcher(editor);					 
						DependenceGraphHelper.toGrappaGraph(pdg, Collections.EMPTY_LIST, fetcher).saveToDot(filepath);
						
						ConsoleUtil.print(ID.CONSOLE,"\nDoting Java level PDG file: "+filepath+" ... ...");
						
						DotViewer dotView = new DotViewer(dotpath,filepath, "jpg");
						dotView.dotIt();
						
						ConsoleUtil.print(ID.CONSOLE,"OK\n");
						ImageView.showImage(filepath+".jpg");
					}
					catch(Exception e){
						ConsoleUtil.printError(ID.CONSOLE, e.getMessage());
					}
				}
			}			
			
			DotThread thread = new DotThread();
			thread.start();
			
			return true;
		}
		catch(Exception e){
			ConsoleUtil.printError(ID.CONSOLE,e.getMessage());
		}
		
		return false;
	}
	
	public boolean showJimpleCFG(String methodSignature){
		try{
			SootMethod m = Scene.v().getMethod(methodSignature);
			Body body = m.getActiveBody();
			DirectedGraph<Unit> ucfg = new BriefUnitGraph(body);
			CFGViewer viewer = new CFGViewer(m, ucfg);
			Graph cfg = viewer.makeJimpleCFG();
			cfg.setTitle("jimple_" + m.getName());			 
			GraphViewer frame = new GraphViewer(cfg);
			frame.setSize(1000, 700);
			frame.setVisible(true);			
			return true;
		}
		catch(Exception e){
			ConsoleUtil.printError(ID.CONSOLE,e.getMessage());
		}
		
		return false;
	}
	
	public boolean dotCallGraph(final String method,final int depth,final String filepath){		
		class DotThread extends Thread{
			public void run(){
				try{
					SootMethod entry = Scene.v().getMethod(method);
					
					ConsoleUtil.print(ID.CONSOLE,"\nDoting Call Graph file: "+filepath+" ... ...");
					CallGraphDotter.dot(Scene.v().getCallGraph(), entry, depth, _options.dotpath,filepath);
					
					ConsoleUtil.print(ID.CONSOLE,"OK\n");
					
					ImageView.showImage(filepath+".jpg");
				}
				catch(Exception e){
					ErrorPrinter.printError(e);
				}
			}
		}			
		
		DotThread thread = new DotThread();
		thread.start();
		return true;
	}
	
	public boolean dotSDG(){		
		class DotThread extends Thread{
			public void run(){
				try{
					String tmppath = getCurPrjTemporalPath();					
					if(tmppath==null)
						tmppath = "";
					
					String filepath = tmppath + "/sdg.dot";					
					SDG sdg = getSDG();
					ConsoleUtil.print(ID.CONSOLE,"\nDoting SDG (can be very slow): "+filepath+" ... ...");
					DependenceGraphHelper.dotDependenceGraph(sdg,_options.dotpath, filepath, false);
					ConsoleUtil.print(ID.CONSOLE,"OK\n");
					
					ImageView.showImage(filepath+".jpg");
				}
				catch(Exception e){
					ErrorPrinter.printError(e);
				}
			}
		}			
		
		DotThread thread = new DotThread();
		thread.start();
		return true;
	}
	
	public boolean showSDG(){		
		class DotThread extends Thread{
			public void run(){
				try{
					String tmppath = getCurPrjTemporalPath();					
					if(tmppath==null)
						tmppath = "";
					
					String filepath = tmppath + "/sdg.dot";					
					File f = new File(filepath);
					if(f.exists()){
						ImageView.showImage(filepath+".jpg");
					}
					else{
						ErrorPrinter.printError("Can not find SDG image in: " + filepath);
					}
				}
				catch(Exception e){
					ErrorPrinter.printError(e);
				}
			}
		}			
		
		DotThread thread = new DotThread();
		thread.start();
		return true;
	}
	
	
	public SDG getSDG(){
		return _sdg;
	}
	
	void setSDG(SDG sdg){
		this._sdg = sdg;
	}
	
	public IPtsToQuery getPtsToQuery() {
		return _ptsto;
	}

	public void setPtsToQuery(IPtsToQuery ptsto) {
		this._ptsto = ptsto;
	}
	
	public Collection<Integer> getDependedLines(String method,int methodStartLine,int line){
		Set<Integer> lines = new HashSet<Integer>();		
		if(_sdg==null)
			return lines;
		
		SootMethod m;
		try{
		    m = Scene.v().getMethod(method);
		    if(m==null)
		    	return lines;
		}catch(Exception e){
			return lines;
		}
		
		Collection<Unit> units = new LinkedList<Unit>();
		JavaSlicingCriterion.unitsInLine(m, line, units);
				
		PDG pdg = _sdg.getPDG(m);
		
		for(Unit u: units){
			boolean hasCall = false;
			if(u instanceof Stmt){
				hasCall = ((Stmt)u).containsInvokeExpr();
			}			
			
			DependenceNode node = pdg.getStmtBindingNode(u);
			if(node==null)
				continue;
			
			collectDependedLine(methodStartLine,node,lines);
			
			//collect depended lines from the actual ins.
			if(hasCall){
				LinkedList<ActualNode> actuals = new LinkedList<ActualNode>();
				pdg.getActualIns(actuals, u);
				for(ActualNode an: actuals){
					collectDependedLine(methodStartLine,an,lines);
				}
			}
		}
		
		return lines;
	}
	
	private void collectDependedLine(int methodStartLine,DependenceNode node,Collection<Integer> lines){
		Collection<?> edges = _sdg.edgesInto(node);
		for(Iterator<?> eIt=edges.iterator();eIt.hasNext();){
			DependenceEdge e = (DependenceEdge)eIt.next();
			DependenceNode depended = e.getFrom();
			if(depended instanceof JimpleStmtNode){
				Unit d = (Unit)depended.getBinding();
				lines.add(SootUtils.getLine(d));	
			}
			else if(depended instanceof ActualNode){
				ActualNode actual = (ActualNode)depended;
				Unit d = actual.getCallSite();
				lines.add(SootUtils.getLine(d));
			}
			else if(depended instanceof EntryNode){
				lines.add(methodStartLine+1);
			}
		}
	}
}


