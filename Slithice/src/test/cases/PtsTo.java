package test.cases;


public class PtsTo {
    private int i;
    protected float f;
    private static int _made;
    public Object _ref;
    public PtsTo _recr;
    
    public static void main(String[] args){
        PtsTo a=new PtsTo();    
        a.test_flow_function("bingo",a);
        a.test_merge();
        a.test_switch();
        test_branches(true,false);
        
        a.test_interproc(null,null,true);
    }
    
    public void test_flow_function(String s,PtsTo tta){
        try{
            PtsTo test=null;   //NullConstant
            test=new PtsTo();  //NewExpr, Local
            test=null;           //reoccurence of null
            
            String str="hello world!";  //StringConstant
            char ch="hi".charAt(0);     //String constant field ref           
            str=s;
            s="hello world!";        //StringConstant reoccurence
            
            int intArr[]=new int[10]; //NewArrayExpr
            intArr[1]=1;             //ArrayRef            
            Object objArr[]=new Object[100];
            objArr[0]=null;
            Object x=objArr[objArr.length-1];
            
            boolean[][] multiArray=new boolean[10][10];
            multiArray[1][1]=multiArray[0][0];
          
            test=new PtsTo();
            test.i=0;                //InstanceFieldRef
            int xy=test.i;
            
            PtsTo._made=5;       //StaticFieldRef     
            float f=PtsTo._made;
            
            xy=(int)f;
            f=(float)xy;
            
            test_merge();
            
            Object y=(Object)test;//CastExpr
            ((Object)test).equals(null);            
            
        }catch(Exception e){ //JCaughtExceptionRef
            System.out.print("ah");                   
        }
    }
    
    public void test_merge(){
        PtsTo i,j,k;
        i=new PtsTo();
        j=new PtsTo();        
        k=new PtsTo();
        
        //a simple merge
        boolean b=true;
        if(b==true){
            PtsTo tmp=k;
            k=j;
            j=tmp;
        }
        
        if(b==true){
            i=new PtsTo();
        }else{
            i=null;
        }
        
        if(b==true){
            j=i;
        }else{
            i=new PtsTo();
            if(b==false){                
                i=null; 
            }
            
            k=i;            
        }
        
        
        while(b){
            PtsTo tmp=i;
            i=j;
            j=k;
            k=i;            
        }
    }
    
    public void test_switch(){
        PtsTo i,j,k;
        i=new PtsTo();
        j=new PtsTo();        
        k=new PtsTo();
        
        //a simple merge
        boolean b=true;
        if(b==true){
            PtsTo tmp=k;
            k=j;
            j=tmp;
        }
        
        if(b==true){
            i=new PtsTo();
        }else{
            i=null;
        }
        
        if(b==true){
            j=i;
        }else{
            i=new PtsTo();
            if(b==false){                
                i=null; 
            }
            
            k=i;            
        }
        
        int x=4*10%4;
        switch(x){
        case 1:  i=null;break;
        case 2:  j=null;break;
        case 3:  k=null;break;  
        default: i=j=k=null;break;           
        }
    }
    
    //test the branched analysis
    public static void test_branches(boolean b,boolean c){
        PtsTo i,j,k;
        i=new PtsTo();
        j=new PtsTo();        
        k=new PtsTo();
        
        //point to all three targets
        while(b==true){
            PtsTo tmp=k;
            k=j;
            j=i;
            i=tmp;
            
            if(c) i=null;
        }
        
        if(i==null){
            j=i;
        }else{
            j=i;
        }
        
        k=j;        
    }
    
    public void test_interproc(String s,PtsTo tta,boolean con){
        
    	PtsTo a=null,b=null;   
        if(con){
            a=new PtsTo();
        }
        else{
            b=new PtsTo();          
        }
        
        PtsTo c=switch_ptr(a,b);
        System.out.println(c.toString());
        Object obj=a._ref;
        Object o2=c;
        
    }
    public static PtsTo _iptr;
    private PtsTo switch_ptr(PtsTo a,PtsTo b){
        a._ref=b._ref;
        a._recr=b._recr;
        _iptr=new PtsTo();
        return _iptr;
    }
}


class PtrCase {
    static class A{
    	A ref;
    	
    	void m(){}
    }
    
    static class B extends A{
    	
    }
    
    static class C extends A{
    	
    }
	
	public static void main(String[] args) {
		simple();
        reference();
        array();
	}
	
	static void simple(){
		A a = new A();
		A b1 = new B();
		B b2 = new B();
		A c1 = new C();
		C c2 = new C();
		
		a.m();
		b1.m();
		b2.m();
		c1.m();
		c2.m();
	}
	
	static void reference(){
		A a = new A();
		a.ref = new B();
	}

	static void array(){
		A[] a1 = new B[10]; 
		A[][] a2 = new C[10][];
		
		a1[1].m();
		a2[0][0].m();
	}
}

