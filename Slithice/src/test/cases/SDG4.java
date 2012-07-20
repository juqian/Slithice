package test.cases;

import java.util.Iterator;
import static java.lang.Math.random;

/** Testing Java 1.5 features. */
public class SDG4 {
	static Integer boxUnbox(){
		int i = 0;
		Integer j = 2; //box
		
		int k = i + j;
		k++;
		return k;
	}	
	
    static void test1(){
    	int a = boxUnbox();	
    	a++;
    }
    
	static class List<Type> implements Iterable<Type>{
    	int index;
    	Type data;
    	
    	
    	public void add(Object o){
    		index ++;
    	}
    	
    	public Iterator<Type> iterator() {
    		 return null;
    	}
    }	
    
    static void generic(){
    	List<Integer> n = new List<Integer>();
    	
    	n.data = new Integer(1);
    	
    	int x = n.data;
    	x++;
    } 
    
    static void test2(){
    	generic();
    }
	
    static int vararg_callee(int... n) {  //传过来n为一个int型数组
        int tempSum=0;
        for(int option : n) {
           tempSum+=option;
        }
        
        return tempSum;
    } 
    
    static void test3(){
        int x = vararg_callee(1,2);
        
        int y = 10;
        x = x + y;
    }    
    
    static double staticMethodRef(){
        double x = random();
        return x;
    } 
    
    static void test4(){
    	staticMethodRef();
    }  
    
    public enum MyColors {
        red,
        black,
        blue
    }
    
    static void test5(){
        MyColors color = MyColors.red;        
        for(MyColors option : MyColors.values()) {
            option.hashCode();
        }    

        switch(color) {
        case red: 
        	color = MyColors.red; 
        	break;
        case black: 
        	color = MyColors.black; 
        	break;
        default:  break;
        }
    }
    
	static void foreach1(){
		List<Integer> list = new List<Integer>();
		Integer q = new Integer(1);
		list.add(q);
		 
		for(Integer s: list){
			int a = s.intValue();
			a++;
		}
	}
	
	static int foreach2() {
		int[] n = new int[10];
        int sum = 0;
        for(int option : n) {
           sum += option;
        }
        
        return sum;
	}
	
    static void test6(){		
    	foreach1();
		foreach2();
    }  
    
	public static void main(String[] args){
		test1();
		test2();
		test3();
		test4();
		test5();
		test6();
	}	
}
