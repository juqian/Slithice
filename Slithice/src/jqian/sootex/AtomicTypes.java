package jqian.sootex;

import soot.SootClass;

/**
 * AtomicTypes are usually immutable types 
 */
public class AtomicTypes {
	public static String[] ATOMIC_TYPES = {
		"java.lang.Boolean",
		"java.lang.Byte",
		"java.lang.Character",
		"java.lang.Double",
		"java.lang.Float",
		"java.lang.Integer",
		"java.lang.Long",
		"java.lang.Short",
		"java.lang.String",
		"java.lang.Void",
		"java.lang.Object",
		"java.lang.Class",
		"java.math.BigInteger"
	};
	
	public static boolean isAtomicType(String classname){
		for(String s: ATOMIC_TYPES){
			if(s.equals(classname)){
				return true;
			}
		}
		
		return false;
	}
	
	public static boolean isAtomicType(SootClass cls){
		String classname = cls.getName();
		return isAtomicType(classname);
	}
	
	/*
 			if (true){//ptions.isThrowablesAtomic()) {
				SootClass throwable = scene.getSootClass("java.lang.Throwable");
				Collection<SootClass> throwables = new HashSet();
				getAllSubClasses(throwable, throwables);
				throwables.add(throwable);
				for (SootClass c : throwables) {
					setAtomicType(c);
				}
			}
	 */
}
