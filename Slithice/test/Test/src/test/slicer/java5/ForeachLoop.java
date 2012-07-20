package test.slicer.java5;

import java.util.Iterator;

public class ForeachLoop {
	static int foreach1() {   
		int[] n = new int[10];
        int sum = 0;
        for(int option : n) {
           sum += option;
        }
        
        return sum;
    } 
	 
	
	
	static class List<Type> implements Iterable<Type>{
		int size = 0;
		Type cur;
    	Object[] data = new Object[10];
    	
    	public void add(Type o){
    		data[size] = o;
    		cur = o;
    		size++;
    	}
    	
    	public Iterator<Type> iterator() {
    		 return new MyIterator<Type>(data);
    	}
    	
    	static class MyIterator<Type> implements Iterator<Type>{
    		Object[] data;
    		int i;
    		
    		public MyIterator(Object[] data){
    			this.data = data;
    		}
    		public boolean hasNext(){
    			return true;
    		}
             
    		public Type next(){
    			Object o = data[i];
    			i++;    			 
    			return (Type)o;    			
    		}
            
    		public void remove(){	   
    		}
    	}
    }	
	
	static int foreach2(){
		List<Integer> list = new List<Integer>();
		Integer q = new Integer(1);
		list.add(q);
		 
		int sum = 0;
		for(Integer s: list){
			int a = s.intValue();
			sum += a;
		}
		
		return sum;		
	}
	
	public static void main(String[] args) {
		foreach1();
		foreach2();
	}

}
