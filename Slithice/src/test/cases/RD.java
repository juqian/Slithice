package test.cases;

/**
 * Test reaching definition analysis or PDG construction
 */
public class RD {
	public static void main(String[] args){
		test_stack_rd();
		test_stack_rd2();
		test_heap_rd();
		test_array_dep();
		
		G = new Node();
		G._index = 0;
		test_global();
		
		test_on_entry(0,1);
		test_kill();	
		test_simple_call();
		test_single_call();
		test_virtual_call(1);	
	}
	
	public static Node G;
	
    public static class Node{
        public int _index;
        public Node _next;
        public void setNode(){
      	  _index=0;
        }
        
        public void kill(Node p,Node q,Node s){
        	p._next=null;
        	q._index=0;
        	s._next._index=2;
        }        
    }
    
    static class SpecialNode extends Node{
        public void setNode(){        	
     	   _next=null;
        }
        
        public void kill(Node p,Node q,Node s){
        	p._next=null;        	
        	s._index=2;
        } 
     }
    
    /**Test simple flow analysis without reference variable and field selectors*/
    private static void test_stack_rd(){
        int i=0,j=1,k;
        k=i+j;
        i=2;
        
        if(i==k){
            k=i;
        }else{
            k=j;
        } 
        
        k++;
    }
    
	private static void test_stack_rd2(){
		int sum = 0;
		int i = 1;
		while(i < 11){
			sum = sum + i;
			i = i + 1;
		}	
	}
  
    private static void test_heap_rd(){
    	 Node i,j,k;
         i=new Node();
         i._index=0;
         
         j=new Node();
         j._index=i._index;
         
         if(i!=null){
         	 k=new Node();
             k._index=2;
         }
         else{
         	k=new Node();
            k._index=2;
         }
         
         Node m=new Node();
         m._index=k._index;
    }
    
    private static void test_array_dep(){
    	int[] array = new int[10];
    	array[0]=1;
    	array[1]=2;
    	int i = 0;
    	int j = array[i];    	
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
    
    /**Test flow analyis with reference variable used*/
    private static void test_kill(){
        int i,j,k;
        i=1;
        j=i;
        if(i==j){
          i=2;        
          j=i;
          k=i;
          k=j;
        }        
    }    
   
    private static void test_global(){
    	int i = G._index;
    }
    
    private static void test_single_call(){
        Node p=new Node();
    	p._index=0;
    	clear(p);
    	Node q=p;
    	int i=p._index;
    	q=p._next;
    }

    private static void clear(Node p){
    	p._next=null;
    }

    private static void test_virtual_call(int x){
        Node p=null;
        if(x>0)	
        	p=new Node();
        else 
        	p=new SpecialNode();
        
    	p.setNode();
    	
    	Node q=p;
    	q._index=p._index;
    	q._next=p._next;
    }
}
