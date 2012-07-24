/** 
 *  
 * 
 * 	@author Ju Qian {jqian@live.com}
 * 	@date 2011-8-8
 * 	@version
 */
package jqian.sootex.sideeffect;

import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import soot.Body;
import soot.Local;
import soot.RefLikeType;
import soot.SootMethod;
import soot.Type;
import soot.jimple.toolkits.typing.fast.BottomType;
import soot.Unit;
import soot.Value;
import soot.jimple.AnyNewExpr;
import soot.jimple.ArrayRef;
import soot.jimple.CastExpr;
import soot.jimple.Constant;
import soot.jimple.DefinitionStmt;
import soot.jimple.IdentityStmt;
import soot.jimple.InstanceFieldRef;
import soot.jimple.InvokeExpr;
import soot.jimple.ReturnStmt;
import soot.jimple.StaticFieldRef;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.shimple.PhiExpr;
import soot.toolkits.graph.HashMutableDirectedGraph;
import soot.toolkits.graph.MutableDirectedGraph;

import jqian.Global;
import jqian.sootex.Cache;
import jqian.sootex.util.SootUtils;
import jqian.sootex.util.callgraph.Callees;
import jqian.sootex.util.graph.GraphHelper;
import jqian.util.Utils;

/**
 * Implement the fresh method and variable analysis provided by Gay@CC 2000
 * A fresh method return a newly created objects (not exist before method call). 
 * NOTE that the object may already bean escaped or also return from parameters.
 * 
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class FreshAnalysis implements ILocalityQuery{
	// 纯鲜对象，不是传进来的
	protected boolean[] _mfresh;		
	protected Set<Local>[] _vnfresh;	//non fresh locals
	
	protected CallGraph _cg;
	
	public FreshAnalysis(CallGraph cg){
		this._cg = cg;	
	}  
	
	protected MutableDirectedGraph<Object> initVarConnectionGraph(SootMethod m){
		// build a variable connection graph
		HashMutableDirectedGraph varConn = new HashMutableDirectedGraph();
		Body body = m.retrieveActiveBody();
		
		varConn.addNode(Boolean.TRUE);
		varConn.addNode(Boolean.FALSE);
		
		for(Local l: body.getLocals()){
			Type t = l.getType();
			if(t instanceof RefLikeType){
				varConn.addNode(l);
			}
			else if(t instanceof BottomType){
				varConn.addNode(l);
			}
		}
		return varConn;
	} 
	

	protected void freshAnalysis(SootMethod m){
		Set<Local> returns = new HashSet<Local>();
		
		// build a constraint graph
		MutableDirectedGraph<Object> varConn = initVarConnectionGraph(m);
		Body body = m.retrieveActiveBody();
		
		for(Unit u: body.getUnits()){
			if(u instanceof ReturnStmt){			 
				Value v = ((ReturnStmt)u).getOp();		 
				if(v.getType() instanceof RefLikeType && v instanceof Local){
					returns.add((Local)v);
				}
			}
			else if(u instanceof DefinitionStmt){
				DefinitionStmt d = (DefinitionStmt)u;
				Value left =  d.getLeftOp();
				Value right = d.getRightOp();
				
				Type leftType = left.getType();
				
				//assignment on non-reference variables
				if(!(leftType instanceof RefLikeType) && !(leftType instanceof BottomType)){
					
				}							
				else if(left instanceof Local){
					//1. l = @param, l = @this
					if(d instanceof IdentityStmt){			 
						varConn.addEdge(Boolean.FALSE, left);
					}	
					//2. "l = new C", "l = constant"
					else if(right instanceof AnyNewExpr || right instanceof Constant){}
					//3. l = r
					else if(right instanceof Local){					
						varConn.addEdge(right, left);
					}
					//4. l = (cast)r
					else if(right instanceof CastExpr){
						CastExpr cast = (CastExpr)right;
						Value op = cast.getOp();
						if(op instanceof Local){
							varConn.addEdge(op, left);
						}
					}		
					//5. l = r.f
					else if(right instanceof InstanceFieldRef){	
						varConn.addEdge(Boolean.FALSE, left);
					}
					//6. l = r[]
					else if(right instanceof ArrayRef){	
						varConn.addEdge(Boolean.FALSE, left);
					}
					//7. l = g
					else if(right instanceof StaticFieldRef){
						varConn.addEdge(Boolean.FALSE, left);
					}//8. l=Phi();
					else if (right instanceof PhiExpr) {
						List<Value> args = ((PhiExpr) right).getValues();						
						for(Value a: args){
							varConn.addEdge(a, left);
							//varConn.addEdge(left, value);
						}
					} 
					else if(right instanceof InvokeExpr){						 
						Callees callees = new Callees(_cg, u);
						boolean allFresh = true;
						for(SootMethod tgt: callees.explicits()){
							if(!isFreshMethod(tgt)){
								allFresh = false;
								break;
							}
						}
						if(!allFresh){
							varConn.addEdge(Boolean.FALSE, left);
						}
					}
					else{
						if(!(leftType instanceof BottomType)){
						//	throw new RuntimeException();
						}
						else{
							System.out.println("BottomType unit: "+u);
						}
					}
				}
				//6. g = r
				else if(left instanceof StaticFieldRef){}			 
				//7. l.f = r
				else if(left instanceof InstanceFieldRef){}			
				//9. l[] = r
				else if(left instanceof ArrayRef){}
				//others
				else{
					throw new RuntimeException();
				}
			}
		}
		
		// solve constraints
		// XXX
		Set nonfresh = GraphHelper.getReachables(varConn, Boolean.FALSE);
		_vnfresh[m.getNumber()] = nonfresh;
			 
		boolean rfresh = true;
		for (Local r : returns) {
			if (nonfresh.contains(r)) {
				rfresh = false;
				break;
			}
		}
		_mfresh[m.getNumber()] = rfresh;
	}
	
	
	public void build() {
		Date startTime = new Date();
		
		_mfresh = new boolean[SootUtils.getMethodCount()];
		_vnfresh = new Set[SootUtils.getMethodCount()];
		
		List<?> rm = Cache.v().getTopologicalOrder();
		for (Iterator<?> it = rm.iterator(); it.hasNext();) {
			SootMethod m = (SootMethod) it.next();
			if(m.isConcrete()){				
				freshAnalysis(m);
			}
		}

		_cg = null;
		Date endTime = new Date();
		Global.v().out.println("[FreshAnalysis] " + rm.size() + " methods analyszed in " + Utils.getTimeConsumed(startTime, endTime));
	}
	
	public boolean isFreshMethod(SootMethod m){
		return _mfresh[m.getNumber()];
	} 
 
	public boolean isRefTgtLocal(SootMethod m, Local v) {
		return false;
	}

	public boolean isRefTgtFresh(SootMethod m, Local v) {
		Set<Local> nonfresh = _vnfresh[m.getNumber()];
		if(nonfresh!=null){
			return !nonfresh.contains(v);
		}
		else{
			return false;
		}
	}
}
