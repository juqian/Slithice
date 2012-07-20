/**
 * Swing UI to show a picture
 *  
 * TODO:
 * 1. 边界没有处理好
 * 2. zoom mechanism
 * 3. drag
 */

package jqian.util.ui;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;


public class PictureViewer extends JFrame {
	private static final long serialVersionUID = 3546242586537550622L;
	private javax.swing.JPanel jContentPane = null;	
	private final int MAX_HEIGHT;
	private final int MAX_WIDTH;	
	protected String _path;
	protected String _title;
	
	public PictureViewer(String title,String path) {
		super();		
		
		this._title = title;
		this._path = path;
		
		Toolkit kit = Toolkit.getDefaultToolkit();
		Dimension screenSize = kit.getScreenSize();

		MAX_HEIGHT = screenSize.height - 65;
		MAX_WIDTH = screenSize.width; 
   	 
		initialize();
	}
	
	public PictureViewer(String path,int maxHeight,int maxWidth) {
		super();		
		
		this._path = path;
		this.MAX_HEIGHT = maxHeight;
		this.MAX_WIDTH = maxWidth;
		initialize();
	}	

	private void initialize() {		
	    File file=new File(_path);
	    JPanel imgPanel = new ScrollImgPanel(file);	    
	    Dimension dimension = imgPanel.getPreferredSize();
	    int height = (int)dimension.getHeight();
	    height = height > MAX_HEIGHT? MAX_HEIGHT: height;
	    int width = (int)dimension.getWidth();
	    width =width > MAX_WIDTH? MAX_WIDTH:width;		
		
	    this.setContentPane(getJContentPane());
	    jContentPane.setLayout(new FlowLayout());
	    jContentPane.add(imgPanel);
	    jContentPane.setPreferredSize(new Dimension(width,height));
	    this.pack();	   
	    
		this.setTitle(_title);		
		//this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent e){ 
            	//jContentPane.getToolkit().
            	PictureViewer.this.dispose();//.setVisible(false);
            }
        });		
	}
	
	/**
	 * This method initializes jContentPane	
	 * @return javax.swing.JPanel
	 */
	private javax.swing.JPanel getJContentPane() {
		if(jContentPane == null) {
			jContentPane = new javax.swing.JPanel();
			jContentPane.setLayout(new java.awt.BorderLayout());
		}
		return jContentPane;
	}
	
	public void display(){
		setVisible(true);
	}
	
 	public static void main(String[] args) throws Exception{
    	PictureViewer cgView = new PictureViewer("test","./output/img/sdg.dot.jpg");    	
    	cgView.setVisible(true);   
    	
    	while(true){    		
    		Thread.sleep(1000);
    	}
    }
 }



class ScrollImgPanel extends JPanel{   
	private static final long serialVersionUID = -5412696500498137404L;

    private ScrollableImgPanel img=null;
	private int MAX_HEIGHT;
	private int MAX_WIDTH; 

	private void setDiemension(){
		 Toolkit kit = Toolkit.getDefaultToolkit();   
    	 Dimension screenSize = kit.getScreenSize();
    	  
    	 MAX_HEIGHT = screenSize.height - 70;
    	 MAX_WIDTH = screenSize.width - 10; 
	 }
	 
     //constructors
     public ScrollImgPanel(Image image) {    	 
    	 setDiemension();
         img = new ScrollableImgPanel(image);
         initialize();
     }

     public ScrollImgPanel(File file) {         
         try{             
        	 setDiemension();
             img = new ScrollableImgPanel(ImageIO.read(file));
         }catch(IOException ex) {
        	 ex.printStackTrace(System.err);
         }
         
         initialize();
     }

     public ScrollImgPanel(String string) {  
    	 setDiemension();
         URL url = null;
         try {
             url = new URL(string);
         }catch (MalformedURLException ex) {
         }
         Image image = Toolkit.getDefaultToolkit().getImage(url);
         MediaTracker tracker = new MediaTracker(this);
         tracker.addImage(image, 0);
         try {
             tracker.waitForID(0);
         } catch (InterruptedException ie) {
         }
         img = new ScrollableImgPanel(image);
         initialize();
     }

     public ScrollImgPanel(ImageIcon icon) {
    	 setDiemension();
         img = new ScrollableImgPanel(icon.getImage()); 
         initialize();
     }

     public ScrollImgPanel(URL url) { 
    	 setDiemension();
         ImageIcon icon = new ImageIcon(url);
         img = new ScrollableImgPanel(icon.getImage()); 
         initialize();
     }
    
     private void initialize(){
         JScrollPane imgScrollPane = new JScrollPane(img);
         Dimension dimension = img.getPreferredSize();  
         this.setBorder(BorderFactory.createEmptyBorder());         
         
         int height = (int)dimension.getHeight()+5;
 	     if(height> MAX_HEIGHT){
 	         height=MAX_HEIGHT; 	        
 	     }
 	     
 	     int width = (int)dimension.getWidth()+5;
 	     if(width > MAX_WIDTH){
 	         width=MAX_WIDTH;		
 	     }

 	     dimension=new Dimension(width,height);
         
         imgScrollPane.setPreferredSize(dimension);         
         imgScrollPane.setViewportBorder(
                 BorderFactory.createLineBorder(Color.black));
         add(imgScrollPane);
         this.setPreferredSize(dimension);
     }

    


    public class ScrollableImgPanel extends JPanel implements Scrollable,MouseMotionListener {
        private Image image = null;
        private int maxUnitIncrement = 1;
        private Dimension preferredDimension;
        
  		private static final long serialVersionUID = -5374829869279840507L;
		public ScrollableImgPanel(Image image){			 
            this.image = image;
            int height = image.getHeight(null);
            int width = image.getWidth(null);
            preferredDimension = new Dimension(width,height);
            maxUnitIncrement = 1;

            //Let the user scroll by dragging to outside the window.
            setAutoscrolls(true); //enable synthetic drag events
            addMouseMotionListener(this); //handle mouse drags
        }

        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            
            Graphics2D g2d = (Graphics2D) g;
            if (image != null) {
                g2d.drawImage(image, 0, 0, this);
            }
          /*  float zoom=0;
            if (image != null) {
                int iw = image.getWidth(this);
                int ih = image.getHeight(this);
                int siw = (int) Math.ceil( (float) iw * zoom);
                int sih = (int) Math.ceil( (float) ih * zoom);
                // int siw = (int) ( (float) iw * zoom);
                // int sih = (int) ( (float) ih * zoom);
                Dimension sz = this.getSize();
                int ofx = (sz.width - siw) / 2;
                int ofy = (sz.height - sih) / 2;
                g.drawImage(image, ofx, ofy, ofx + siw, ofy + sih, 0, 0, iw, ih, this);
               // g.drawImage(img,1,1,img.getWidth(this),img.getHeight(this),this);
              }*/
            
            
        }

        //Methods required by the MouseMotionListener interface:
        public void mouseMoved(MouseEvent e) {
        }

        public void mouseDragged(MouseEvent e) {
            //The user is dragging us, so scroll!
            Rectangle r = new Rectangle(e.getX(), e.getY(), 1, 1);
            scrollRectToVisible(r);
        }

        public Dimension getPreferredSize() {
            //return super.getPreferredSize();
            return preferredDimension;
        }

        public Dimension getPreferredScrollableViewportSize() {
        	return getPreferredSize();
        }

        public int getScrollableUnitIncrement(Rectangle visibleRect,
                int orientation, int direction) {
            //Get the current position.
            int currentPosition = 0;
            if (orientation == SwingConstants.HORIZONTAL) {
                currentPosition = visibleRect.x;
            } else {
                currentPosition = visibleRect.y;
            }

            //Return the number of pixels between currentPosition
            //and the nearest tick mark in the indicated direction.
            if (direction < 0) {
                int newPosition = currentPosition
                        - (currentPosition / maxUnitIncrement)
                        * maxUnitIncrement;
                return (newPosition == 0) ? maxUnitIncrement : newPosition;
            } else {
                return ((currentPosition / maxUnitIncrement) + 1)
                        * maxUnitIncrement - currentPosition;
            }
        }

        public int getScrollableBlockIncrement(Rectangle visibleRect,
                int orientation, int direction) {
            if (orientation == SwingConstants.HORIZONTAL) {
                return visibleRect.width - maxUnitIncrement;
            } else {
                return visibleRect.height - maxUnitIncrement;
            }
        }

        public boolean getScrollableTracksViewportWidth() {
            return false;
        }

        public boolean getScrollableTracksViewportHeight() {
            return false;
        }

        public void setMaxUnitIncrement(int pixels) {
            maxUnitIncrement = pixels;
        }

        /*
         * public ScrollImgPanel(JPanel panel){ super(new BorderLayout()); //Put
         * the drawing area in a scroll pane. JScrollPane scroller = new
         * JScrollPane(panel); scroller.setPreferredSize(new
         * Dimension(200,200)); //Lay out this demo. add(scroller,
         * BorderLayout.CENTER); }
         */
    }
    /*
     * import java.awt.*;
// import java.awt.event.WindowAdapter;
// import java.awt.event.WindowEvent;
// import java.awt.MediaTracker;
import javax.swing.*;
import javax.swing.border.*;

public class ImagePanel extends JPanel {
  private Image img;
  float zoom;

  public ImagePanel()
  {

  }
  public ImagePanel(Image image) {
    img=image;
    try{
      jbInit();
    }
    catch(Exception ex){
      ex.printStackTrace();
    }
  }
  public void setImage(Image image){
    img=image;
    try{
      jbInit();
    }
    catch(Exception ex){
      ex.printStackTrace();
    }

  }

  public void paint(Graphics g){
    super.paint(g);
    if (img != null) {
      int iw = img.getWidth(this);
      int ih = img.getHeight(this);
      int siw = (int) Math.ceil( (float) iw * zoom);
      int sih = (int) Math.ceil( (float) ih * zoom);
      // int siw = (int) ( (float) iw * zoom);
      // int sih = (int) ( (float) ih * zoom);
      Dimension sz = this.getSize();
      int ofx = (sz.width - siw) / 2;
      int ofy = (sz.height - sih) / 2;
      g.drawImage(img, ofx, ofy, ofx + siw, ofy + sih, 0, 0, iw, ih, this);
     // g.drawImage(img,1,1,img.getWidth(this),img.getHeight(this),this);
    }

  }
  public void setBorder(Border border){
    super.setBorder(border);
  }

  private void jbInit() throws Exception {
    int PanelHeight=this.getHeight()-2;  // 2边Border的宽度
    int PanelWidth=this.getWidth()-2;
    int ImageHeight=img.getHeight(this);
    int ImageWidth=img.getWidth(this);
    int WidthDifferenceValue,HeightDifferenceValue;
    float HeightZoom=0,WidthZoom=0;
    WidthDifferenceValue=ImageWidth-PanelWidth;
    HeightDifferenceValue=ImageHeight-PanelHeight;
    if(WidthDifferenceValue>0) WidthZoom=(float)PanelWidth/ImageWidth;
    else if(WidthDifferenceValue==0) WidthZoom=1;
    else if(WidthDifferenceValue<0) WidthZoom=(float)ImageWidth/PanelWidth;
    if(HeightDifferenceValue>0) HeightZoom=(float)PanelHeight/ImageHeight;
    else if(HeightDifferenceValue==0) HeightZoom=1;
    else if(HeightDifferenceValue<0) HeightZoom=(float)ImageHeight/PanelHeight;
    if(HeightZoom>=WidthZoom)
      zoom=WidthZoom;
    else
      zoom=HeightZoom;
  }
  public float ZoomRate(){
    return zoom;
  }
  public int ZoomPercent(){
    float zoomRatePercent=zoom*100;
    return (int)zoomRatePercent;
  }
}
     */
}

