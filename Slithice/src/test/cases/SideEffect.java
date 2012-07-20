package test.cases;

public class SideEffect {
	public static void main(String[] args){
		Node n = new Node();
		n._foo =  new Foo();
		n._foo.fn = new Node();
		Node._zar = new Zar();
		
		simpleTest();
		directRecursionTest();
		indirectRecursionTest();		
		iterativeComputingTest();
		simpleSideEffectTest(n);
		arrayAccessTest();
		test7();
		
		crossThreadTest();
		t1 = new MyThread();
		t2 = new Thread(new MyRunnable());
		
		testFilterNonEscapable();
    }
	
	/** simple test */
	private static void simpleTest(){
		Node n = new Node();
		m1(n);
	}
	
	private static void m1(Node n){
		n._foo = null;
		m2(n);
	}
	
	private static void m2(Node n){
		Object x = n._bar; 
		m3(null);
		
		x.hashCode();
	}
	
	private static void m3(Node n){
		Node._zar = null;
	}
	
	/** test simple recursion methods. */
	private static void directRecursionTest(){
		Node._zar = null;
		Node n = new Node();
		n._foo = null;
		
		directRecursionTest();		
	}
	
	/** test indirect recursion 
	 *  Methods: m6, m7, m11, m3
	 *  MOD
	 *     instance fields: _bar, _foo
	 *     static fields: _zar
	 *  USE
	 *     instance fields:  
	 *     static fields:  
	 */
	private static void indirectRecursionTest(){
		Node._zar = null;
		Node n = new Node();
		m6(n);	
		
	}
	
	private static void m6(Node n){
		n._bar = null;
		
		m7(n);	
		m11(n);
	}
	
	private static void m7(Node n){
		n._foo = null;
		
		m6(n);
		m3(n);
	}
	
	/** This case can not be correctly handled by the previous version.
	 *  The method is analysis in topological order:
	 *      m10, m9, m8, iterativeComputingTest
	 *  round 1:
	 *      m10->{foo,bar} m9->{foo,bar}, m8->{foo,bar,zar}, iterativeComputingTest->{foo,bar,zar}
	 *  round 2:
	 *      m10->{foo,bar} m9->{foo,bar,zar} m8->{...} iterativeComputingTest->{...}
	 *      
	 *  MOD
	 *     instance fields: _bar, _foo
	 *     static fields: _zar
	 *  USE
	 *     instance fields:  
	 *     static fields:   
	 */
	private static void iterativeComputingTest(){
		Node._zar = null;		
		m8();
	}
	
	private static void m8(){
		iterativeComputingTest();
		
		Node n = new Node();
		m9(n);
	}
	
	private static void m9(Node n){
		//n._foo = null;
		m8();
		m10(n);
	}
	
	private static void m10(Node n){
		n._foo = null;
		n._bar = null;
		
		m9(n);		
	}
	
	private static void m11(Node n){
		n._bar = null;		
	}
	
	/////////// case for side effects /////////////////
	private static void simpleSideEffectTest(Node p){
		p._bar = null;
		m4(p._foo);
	}
	
	private static void m4(Foo f){
		f.fn = null;
		m5();
	}
	
	private static void m5(){
		Zar zar = Node._zar;
		zar._zn = new Node();
	}
	
	/** test case for array element access. */
	static void arrayAccessTest(){
		Node n = new Node();
		
		//
		n._foo = new Foo();
		Bar b = n._bar;
		
		Node._x = null;
		
		int[] vector = new int[10];
		boolean[] flags = new boolean[10];
		
		test6callee(n,vector,flags);
	}
	
	private static void test6callee(Node node, int arr[],boolean[] stat){
		node._def = null;
		Foo f = node._use;
		
		if(stat[0]){
			Zar z = Node._zar;
			
			arr[0] = 0;
		}
	}
	
	static void test7(){
		Node n = new Node();
		test7callee(n);
	}
	
	private static void test7callee(Node node){
		int i = 0;
		
		if(i>0){
			node._def = null;
			i = 1;
		}
		else{
			node._use = null;
			i = 2;
		}
		
		
		int k = i;
		k++;
	}
	

	/** Test case for multi-thread usage. */	
	static void crossThreadTest(){
		Node n = new Node();
		test7callee(n);
		
		t1.start();
		t2.start();
	}
	
	static Thread t1;
	static Thread t2;
	
	static class MyThread extends Thread{
		public void run(){
			Node._x = null;
		}
	}
	
	static class MyRunnable implements Runnable{
		public void run(){
			Node._zar = null;
		}
	}
	
	static void testFilterNonEscapable(){
		Node n = new Node();
		n._foo = null;
		Object x = n._bar;
		x.hashCode();
	}
}

class Foo{
	Node fn;
}

class Bar{}

class Zar{
	Node _zn;
}

class Node{
	Foo _foo;
	Bar _bar;
	
	Foo _def;
	Foo _use;
	
	static Zar _zar;	
	static Zar _x;
}
