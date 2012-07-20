package test.cases;

import java.util.LinkedList;
import java.util.List;

/**
 * A test case of List use.
 * This could be a piece of code used in practice
 */
public class SDG5 {
	static void useList(){
		List<Integer> list = new LinkedList<Integer>();
		Integer x = new Integer(1);
		Integer y = new Integer(2);
		
		list.add(x);
		list.add(y);
		
		int z = list.size();
		z++;
		
		Integer p = list.iterator().next();
		p.toString();
		 
		for(Integer s: list){
			int a = s.intValue();
			z += a;
		}
	}	
	
	public static void main(String[] args) {
		useList();
	}
}
