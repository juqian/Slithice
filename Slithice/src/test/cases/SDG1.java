package test.cases;

/**
 * Normal PDG and SDG test case
 * Test the handling of variables statements
 */
public class SDG1 {
	private static class Node{
		int f;
		
		public void decrease(){
			f--;
		}
		
		public int step(){
			return f++;
		}
	}
	
	private static class XNode extends Node{
		public int step(){
			return f--;
		}
	}
	
	private static int result;
	private static Node NODE;
	
	public static void main(String[] args){
		NODE = new Node();
		test1();
		test2();
		test3();
		test4();
		test5();
		test6();
		test7();
	}
	
	/** Simple test */
	private static void test1(){
		int a = 0;
		int b = 1;
		
		a = sum(a,b);	
		a++;
	}
	
	private static int sum(int x,int y){
		return x+y;
	}
	
	/** With globals */
	private static void test2(){
		int a = 1;
		int b = 2;
		product(a,b);
		result++;
	}
	
	private static void product(int x,int y){
		result = x * y;
	}
	
	/** With globals as reference */
	private static void test3(){
		int a = 1;
		int b = 2;
		product2(a,b);
		NODE.f++;
	}
	
	private static void product2(int x,int y){
		NODE.f = x * y;
	}
	
	/** With globals only in use. */
	private static void test4(){
		result = 0;
		read();
	}
	
	private static void read(){
		int x = result;
		x++;
	}
	
	
	/** With references as parameter. */
	private static void test5(){
		Node p = new Node();
		inc(p);
		
		int x = p.f;
		x++;
	}
	
	private static void inc(Node n){
		n.f++;
	}
	
	
	/** With non-static method call." */
	private static void test6(){
		Node p = new Node();
		p.decrease();
		int x = p.f;
		x++;
	}
	
	/** With dynamic dispatching. */
	private static void test7(){
		boolean b = 1<2;
		Node p;
		if(b)
		   p = new Node();
		else
		   p = new XNode();
		
		int i = p.step();
		i--;
	}
}
