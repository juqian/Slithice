package test.cases;

/**
 * Test reaching definition analysis or PDG construction
 */
public class RU {
	public static void main(String[] args){
		test_stack_ru();
		test_heap_ru();
		test_array_ru();
		
		G = new Node();
		G._index = 0;
		test_global();
		
		test_on_entry(0,1);
		test_simple_call();
		test_single_call();
		test_virtual_call(1);	
	}
	
	public static Node G;
	
    public static class Node{
        public int _index;
        public Node _next;
        public int getIndex(){
      	  return _index;
        }
    }    
    static class SpecialNode extends Node{
        public int getIndex(){        	
     	   Object o = _next;
     	   return o.hashCode();
        }
     }
    
    /** test gen/kill on stack variables */
    private static void test_stack_ru(){
        int i=0,j=1,k;
        k = i + j;
        i = k + k;
        
        if(i==k){
            k=i;  //kill use of k
            i = 0;
        }
        
        k++;
    }
  
    private static void test_heap_ru(){
    	 Node i,j,k;
         i = new Node();
         int x = i._index;
         
         j=new Node();
         j._index = x;
         
         if(x>0){
         	 k = new Node();
             x = k._index;
         }
         else{
         	k = new Node();
            x = k._index;
         }
         
         Node m=new Node();
         m._index = x;
    }
    
    private static void test_array_ru(){
    	int[] array = new int[10];
    	array[0] = 1;     
    	int i = array[1];
    	array[2] = 2;
    	int j = array[i];
    	j++;
    }
    
    private static int test_on_entry(int i,int j){
    	int k = i+j;
    	return k;
    }
    
 
    private static void test_simple_call(){
    	int a = 0;
    	int b = 1;
    	int c = test_on_entry(a,b);
    	c++;
    } 
   
    private static void test_global(){
    	int i = G._index;
    	i++;
    }
    
    private static void test_single_call(){
        Node p=new Node();
    	p._index=0;
    	clear(p);
    	Node q = p;
    	int i = p._index;
    	q = p._next;
    }

    private static Node clear(Node p){
    	return p._next;
    }

    private static void test_virtual_call(int x){
        Node p=null;
        if(x>0)	
        	p=new Node();
        else 
        	p=new SpecialNode();
        
    	p.getIndex();
    	
    	Node q=p;
    	q._index=p._index;
    	q._next=p._next;
    }
}
