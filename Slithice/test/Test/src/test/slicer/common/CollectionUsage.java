package test.slicer.common;

import java.util.*;

public class CollectionUsage {
	static void useList(){
		List<Integer> list = new LinkedList<Integer>();
		Integer x = new Integer(1);
		Integer y = new Integer(2);
		
		list.add(x);
		list.add(y);
		
		list.remove(x);
		list.contains(y);
		
		int z = list.size();
		z++;
		
		Integer p = list.iterator().next();
		p.toString();
		
		int sum = 0;
		for(Integer s: list){
			int a = s.intValue();
			sum += a;
		}
		 
		z = sum;
	}
	
	static void useSet(){
		Set<Integer> a = new HashSet<Integer>();
		Integer x = new Integer(1);
		Integer y = new Integer(2);
		
		a.add(x);
		a.add(y);	
		
		a.remove(x);
		a.contains(y);
	 
		int sum = 0;
		for(Integer s: a){
			int i = s.intValue();
			sum += i;
		}
		 
		sum++;
	}
	
	static void useMap(){
		Map<Integer,Object> map = new TreeMap<Integer,Object>();
		Integer x = new Integer(1);
		Integer y = new Integer(2);
		
		Collection<Object> c1 = new ArrayList<Object>(1);
		Collection<Object> c2 = new ArrayList<Object>(2);
		map.put(x, c1);
		map.put(y, c2);
	 
		int sum = 0;
		for(Map.Entry<Integer, Object> e: map.entrySet()){
			int i = e.getKey();
			sum += i;
		}
		 
		sum++;
	}
	
	static void useVector(){
		Vector<Integer> vec = new Vector<Integer>();
		Integer x = new Integer(1);
		Integer y = new Integer(2);
		
		vec.add(x);
		vec.add(y);
	 
		int sum = 0;
		for(int i: vec){			
			sum += i;
		}
		 
		sum++;
	}
	
	public static void main(String[] args){
		useList();
		useSet();
		useMap();
		useVector();
	}
   
    
	
	
	
	 
	
}
