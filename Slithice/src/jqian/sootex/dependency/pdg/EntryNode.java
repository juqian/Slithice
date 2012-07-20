package jqian.sootex.dependency.pdg;

import jqian.sootex.util.CFGEntry;
import soot.*;


/**
 * Model the entry node of a PDG. 
 */
public class EntryNode extends DependenceNode{
   public EntryNode(MethodOrMethodContext mc){
       super(mc);
   }
   
   public Object clone(){
       return new EntryNode(_mc);
   }
   
   /**Get the corresponding method*/
   public SootMethod getMethod(){
       return _mc.method();
   }
   
   public Object getBinding(){
	   return CFGEntry.v();   
   }
   
   public String toString(){
	   String out = "#"+_id+" EN ";
	   SootMethod m = _mc.method();
	   out += m.getDeclaringClass().getShortName();
	   out += "."+m.getName();
	   return out;
   }   
}
