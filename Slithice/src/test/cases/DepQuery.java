package test.cases;

public class DepQuery {
	public static void main(String[] args){
		test_ctrl_dep1(0);
		test_ctrl_dep2(0);
		test_ctrl_dep3(0);
		test_ctrl_dep4(0);
		test_ctrl_dep5(0);
		test_ctrl_dep6(0);
		
		test_flow_dep1(0);
		test_flow_dep2(0);
		test_flow_dep3(0);
		test_flow_dep4(0);
		test_flow_dep5(0);
		
		test_anti_dep1(0);
		test_anti_dep2(0);
		test_anti_dep3(0);
		
		test_output_dep1(0);
		test_output_dep2(0);
	}
	
	//control flow dependences
	static int test_ctrl_dep1(int x){
		// test exception
		if (x == 0) {
			throw new RuntimeException();
		}
		
		return 1;	
	}
	
	//control flow dependences
	static int test_ctrl_dep2(int x){
		// test return
		if (x == 1) {
			return 1;
		}
	 
		int y = x;		
		return y;
	}
	
	// test normal if statement
	static int test_ctrl_dep3(int x){		 
		if (x == 1) {
			x = x + 1;
		}
		
		if(x==3){
			x = x * 2;
		}
		else{
			x = x *3;
		}
	 
		int y = x;		
		return y;
	}
	
	// test normal loops
	static int test_ctrl_dep4(int x){
		for(int i=0;i<10; i++){
			x = x + 1;
		}
	 
		int y = x;		
		return y;
	}
	
	// test normal loops
	static int test_ctrl_dep5(int x){		
		int y = x;	
		while(y<20){
			y = y * 3;
		}
		return y;
	}
	
	//control flow dependences
	static int test_ctrl_dep6(int x){	
		// test while
		for(int i=x; i<10; i++){
			x += i;
			
			// test break
			if(x>11){
				break;
			}
			
			x = 2 * x;
		}
		
		int y = x;		
		return y;
	}
	
	//flow dependences 
	static int test_flow_dep1(int x){
		int i = 0;
		int j = 1;
		
		// use in if
		if(x > 0){
			i++;
		}
		
		// multiple choice of definition
		x = i;
		j = x;
		return j;
	}
	
	static int test_flow_dep2(int x){
		// dependence in loop
		for(int i=0; i<10; i++){
			x = x + i;
		}
	 
		int y = x;		
		return y;
	}
	
	static int test_flow_dep3(int x){
		int[] a = new int[10];
		
		// array element access
		a[1] = 0;		
		int y = a[0];		
		
		// length of
		x = a.length;
		
		// cast
		char z = (char)x;
		z++;
		
		return y;
	}
	
	
	static int G;
	
	static class Node{
		int x;
		
		Node(){
			x = 0;
		}
		
		void set(int i){
			x = i;
		}
		
		int get(){
			return x;
		}
	}
	
	static class NodeEx extends Node{
		NodeEx(){
			x = 2;
		}
		
		void set(int i){
			x = i + i;
		}
	}
 
	// static fields & instance fields & instanceof
	static int test_flow_dep4(int p){
		G = 1;
		p = G;
		
		Node m = new Node();
		m.x = p;
		p = m.x;
		
		if(m instanceof Node){
			return p;
		}
		else{
			return 1;
		}
	}
	
	// method call
	static int test_flow_dep5(int p){
		Node m = null;
		if(p>0){
			m = new Node();
		}
		else{
			m = new NodeEx();
		}
		 
		m.set(p);
		
		return m.x;
	}
	
	// anti dependence caused by stack locations
	static int test_anti_dep1(int x){
		int i = 0;
		int j = i;
		
		// use in if
		if(x > 0){
			i = i + j;
		}
		
		// merge use of i here
		if(x > 0){
			j = i + 1;
		}
		
		i = x;
		return j;
	}
	
	//anti dependences caused by heap access
	static int test_anti_dep2(int x){
		Node p = new Node();
		int i = p.x;
		x++;
		p.x = 1;
		
		return i;
	}
	
	// anti dependence caused by method call
	static int test_anti_dep3(int x){
		Node p = new Node();
		int i = p.get();
		x++;
		p.x = 1;
		
		p.set(x);
		return i;
	}
	
	//output dependences
	static int test_output_dep1(int x){
		int i = 0;
		int j = i;
		
		// use in if
		if(x > 0){
			i = i + j;
		}
		
		// merge use of i here
		if(x > 0){
			j = i + 1;
		}
		
		i = x;
		return j;
	}
	
	static int test_output_dep2(int x){
		Node p = new Node();
		p.x = 0;
		x++;
		p.x = 1;
		
		p.set(x);
		
		int[] a = new int[10];
		a[0] = 1;
		x = a[1];
		a[2] = 5;
		
		return x;		 
	}
}
