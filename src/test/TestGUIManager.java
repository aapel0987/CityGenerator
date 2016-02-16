package test;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Semaphore;

import javax.swing.JFrame;

import area_constructors.BasicShapeConstructor;

public class TestGUIManager {

	private CustomPaintComponent comp;
	private JFrame frame;
	
	public TestGUIManager() {
		this("");
	}
	
    public TestGUIManager(String name) {

	  // Create a frame
	  frame = new JFrame();
	
	  // Add a component with a custom paint method
	  comp = new CustomPaintComponent();
	  frame.add(comp);
	
	  // Display the frame
	  int frameWidth = 300;
	  int frameHeight = 300;
	  frame.setSize(frameWidth, frameHeight);
	  frame.setVisible(true);
	  frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	  frame.addComponentListener(comp);
	  frame.setTitle(name);
    }

    public void addShape(List<Shape> shapes, Color c){
    	Iterator<Shape> iter = shapes.iterator();
    	while(iter.hasNext()){
    		this.addShape(iter.next(), c);
    	}
    }
    
	public void addShape(Shape s, Color c){
		comp.addShape( s, c);
	}
	
	public void addPoints(Point2D points[], double radius, Color color){
		LinkedList<Area> pointAreas = new LinkedList<Area>();
		for(int index = 0; index < points.length; index++){
			pointAreas.add(new Area(new Ellipse2D.Double(points[index].getX() -radius, points[index].getY() -radius, 2*radius, 2*radius)));
		}
		addShape(BasicShapeConstructor.combineAreasParallel(pointAreas),color);
	}
	
    /**
     * To draw on the screen, it is first necessary to subclass a Component 
     * and override its paint() method. The paint() method is automatically called 
     * by the windowing system whenever component's area needs to be repainted.
     */
    
    static class CustomPaintComponent extends Component implements ComponentListener {
    	
    	static class shapeColorBundle{
    		Shape shape;
    		Color color;
    		protected shapeColorBundle(Shape s, Color c){
    			shape = s;
    			color = c;
    		}
    	}

    	private List<shapeColorBundle> areas;
    	private Semaphore areasSemaphore;
    	
    	protected CustomPaintComponent(){
    		super();
    		areas = new ArrayList<shapeColorBundle>();
    		areasSemaphore = new Semaphore(1);
    	}
    	
    	public void addShape(Shape s, Color c){
    		try{
    			areasSemaphore.acquire();
    			areas.add(new shapeColorBundle( s, c));
    			areasSemaphore.release();
    		} catch(Exception exception){
    			System.err.println("Failed to add area to areas. Terminating");
    			System.exit(1);
    		}
    		repaint();
    	}

    	public void paint(Graphics g) {

		    // Retrieve the graphics context; this object is used to paint shapes
		
		    Graphics2D g2d = (Graphics2D)g;
		
		    try {
			    areasSemaphore.acquire();
			    
			    Iterator<shapeColorBundle> iter = areas.iterator();
			    //First loop is to get the Min&Max X and Y values
			    double minX, minY, maxX, maxY;
			    if(iter.hasNext()){
			    	shapeColorBundle bundle = iter.next();
			    	Rectangle2D boundingBox = bundle.shape.getBounds2D();
			    	minX = boundingBox.getMinX();
			    	minY = boundingBox.getMinY();
			    	maxX = boundingBox.getMaxX();
			    	maxY = boundingBox.getMaxY();
				    while(iter.hasNext()){
				    	bundle = iter.next();
				    	boundingBox = bundle.shape.getBounds2D();
				    	if(minX > boundingBox.getMinX()){
					    	minX = boundingBox.getMinX();
				    	}
				    	if(minY > boundingBox.getMinY()){
				    		minY = boundingBox.getMinY();
				    	}
				    	if(maxX < boundingBox.getMaxX()){
				    		maxX = boundingBox.getMaxX();
				    	}
				    	if(maxY < boundingBox.getMaxY()){
				    		maxY = boundingBox.getMaxY();
				    	}
				    }
				    double min_scaler = (getSize().width-1)/(maxX-minX);
				    if ((getSize().height-1)/(maxY-minY) < min_scaler){
				    	min_scaler = (getSize().height-1)/(maxY-minY);
				    }
				    AffineTransform transform = AffineTransform.getScaleInstance(min_scaler, min_scaler);
				    transform.translate(-1*minX, -1*minY);
				    iter = areas.iterator();
				    g2d.setStroke(new BasicStroke(1));
				    while(iter.hasNext()){
				    	bundle = iter.next();
					    g2d.setPaint(bundle.color);
					    Area toRender = new Area(bundle.shape);
					    toRender.transform(transform);
					    g2d.fill(toRender);
				    }
			    }
			    
			    areasSemaphore.release();
		    } catch (Exception exception){

    			System.err.println("Paint Failed. Terminating");
    			System.exit(1);
		    }
		    //System.out.println("Painting, Width: " + getSize().width + ", Height: " + getSize().height);
    	}
    	
    	public void componentResized(ComponentEvent arg0) {
    	    //int W = 4;  
    	    //int H = 3;  
    	    //Rectangle b = arg0.getComponent().getBounds();
    	    //arg0.getComponent().setBounds(b.x, b.y, b.width, b.width);
    	    repaint();
    	}

		public void componentHidden(ComponentEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		public void componentMoved(ComponentEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		public void componentShown(ComponentEvent arg0) {
			// TODO Auto-generated method stub
			
		}
	}
}
