package jqian.sootex;

import jqian.sootex.util.HammockCFG;
import jqian.sootex.util.SootUtils;
import soot.Body;
import soot.SootMethod;
import soot.baf.BafBody;
import soot.toolkits.graph.UnitGraph;

/**
 * TODO: 需要过程间异常分析以建立较为精确的异常流边.
 *       目前若用ExceptinalUnitGraph太粗糙
 */
public class HammockCFGProvider implements CFGProvider{
	private UnitGraph[] _cfgs;
	
	public HammockCFGProvider(){
		_cfgs = new UnitGraph[SootUtils.getMethodCount()];
	}
  
	public UnitGraph getCFG(SootMethod m){
		int id = m.getNumber();
    	UnitGraph cfg = _cfgs[id];
    	
    	if(cfg==null){
    		 Body body=m.getActiveBody();        
    	     if(body instanceof BafBody){
    	    	 throw new RuntimeException("BafBody is not supported.");
    	     }
    	     cfg = new HammockCFG(body); 
    	     _cfgs[id] = cfg;
    	}
       
    	return cfg;
	}
	
	public void release(SootMethod m){
		int id = m.getNumber();
    	_cfgs[id] = null;
	}
}
