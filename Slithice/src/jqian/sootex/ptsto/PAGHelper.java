package jqian.sootex.ptsto;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import soot.PointsToSet;
import soot.jimple.AnyNewExpr;
import soot.jimple.spark.pag.AllocDotField;
import soot.jimple.spark.pag.AllocNode;
import soot.jimple.spark.pag.Node;
import soot.jimple.spark.pag.PAG;
import soot.jimple.spark.pag.SparkField;
import soot.jimple.spark.sets.P2SetVisitor;
import soot.jimple.spark.sets.PointsToSetInternal;
import soot.jimple.toolkits.pointer.Union;
import soot.util.ArrayNumberer;

/**
 * Helper class for PAG (Spark) points-to analysis
 */
public class PAGHelper {
	private static class MyVisitor extends P2SetVisitor {
		private final Set<AllocNode> _objs;	
		
		public MyVisitor(Set<AllocNode> objects){
			this._objs = objects;
		}
		
		public void visit(Node n) {
			if (n instanceof AllocNode) {
				AllocNode aNode = (AllocNode)n;
				_objs.add(aNode);
			}				
		}

		public boolean getReturnValue() {
			return true;
		}
	}
	
	public static void toAllocNodeSet(PointsToSet ptSt, Set<AllocNode> result) {
		if (ptSt instanceof PointsToSetInternal) {
			PointsToSetInternal ptStInternal = (PointsToSetInternal) ptSt;
			MyVisitor visitor = new MyVisitor((Set<AllocNode>)result);			
			ptStInternal.forall(visitor);
		}
		else if (ptSt instanceof Union) {
			 
		}
	}
	
	public static AnyNewExpr allocNodeToNewExpr(AllocNode node){
		Object expr = node.getNewExpr();
		//out.println(expr.getClass());        	    

		if(expr instanceof AnyNewExpr){
			return (AnyNewExpr)expr;
		}
		else{// if (expr instanceof Value || expr instanceof String) {					
		  	return null;	
		}
		//else {throw new RuntimeException("Unacceptable node type: "+node);}	
	}
	
	public static Set<AnyNewExpr> pointsToSetToNewExprs(PointsToSet ptSt){
		Set<AllocNode> nodes = new HashSet<AllocNode>();		
		Set<AnyNewExpr> objects = new HashSet<AnyNewExpr>();
		
		toAllocNodeSet(ptSt, nodes);
		for(AllocNode n: nodes){		 
			AnyNewExpr obj = allocNodeToNewExpr(n);
			if(obj!=null)
				objects.add(obj);
		}
		
		return objects;
	}
	
	 
	static Map<Object,AllocNode> buildNewExprToAllocNodeMap(PAG pag){	 
		ArrayNumberer numberer = pag.getAllocNodeNumberer();
		int allocNum = pag.getNumAllocNodes();
		Map<Object,AllocNode> newExpr2allocNode = new HashMap<Object, AllocNode>(allocNum * 2);

		for (int i = 1; i <= allocNum; i++) {// 0 - null pointer
			AllocNode node = (AllocNode) numberer.get(i);
			Object expr = node.getNewExpr();
			newExpr2allocNode.put(expr, node);
		}
		
		return newExpr2allocNode;
	} 
	
	public static PointsToSet getFieldPointsTo(AllocNode node, SparkField f){		 
		try {
			AllocDotField field = node.dot(f);
			return field.getP2Set();
		}
		catch (NullPointerException e) {
			return null;
		}   
	}
	
	public static Set<AllocNode> reachingObjectsOfAllFields(AllocNode n){
		Set<AllocNode> result = new HashSet<AllocNode>();

		Collection<?> fieldRefs = n.getAllFieldRefs();
		for (Iterator<?> it = fieldRefs.iterator(); it.hasNext();) {
			AllocDotField field = (AllocDotField) it.next();

			try {
				PointsToSet p2St = field.getP2Set();
				toAllocNodeSet(p2St, result);
			} catch (NullPointerException e) {
			}
		}
		return result;
	}
}
