package test;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import area_constructors.BasicPathFinder;
import area_constructors.BasicShapeConstructor;
import area_constructors.BasicShapeModifier;
import area_constructors.Node;
import area_constructors.BasicWaterConstructor;
import map_structure.Group;
import map_structure.Layer;
import materials.MaterialPoint;
import materials.MaterialsCollection;

public class MainTestClass {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println("Hello again Java, my dear old friend!");
		//TestFillBlueBullseye();
		//TestDrawPattern();
		//TestFillBullseysWithSand();
		//TestFillBullseysWithGrass();
		//TestGetIntersectionPoint();
		//TestCreateSimpleRiver();
		//TestLine2DToNodes();
		//TestPathToNodes();
		//TestNodesToPaths();
		//TestDistortSquare();
		TestGetAreaPaths();
	}

	private static void TestGetAreaPaths(){
		Group map = new Group();
		Layer mapBase = new Layer(MaterialsCollection.Grass,0,0,150,100);
		List<Point2D> toConnect = new LinkedList<Point2D>();
		toConnect.add(new Point2D.Double(1, 1));
		toConnect.add(new Point2D.Double(149, 99));
		List<Path2D> paths = BasicPathFinder.getAreaPaths(toConnect, mapBase, 60, 65);
		map.add(mapBase);
		LinkedList<Point2D> points = new LinkedList<Point2D>(BasicShapeModifier.pathToNodes(paths.get(0), 15));
		Group waterLayer = BasicWaterConstructor.createSimpleRiver(points,10); 
		map.add(waterLayer);
		map.crop(mapBase);
		TestGUIManager gui = new TestGUIManager("TestGetAreaPaths");
		map.render(gui);
		gui.addPoints(points, 1, Color.black);
		
		//map.render(gui);
	}
	
	private static void TestCreateSimpleRiver(){
		Group map = new Group();
		Layer mapBase = new Layer(MaterialsCollection.Grass,0,0,150,100);
		map.add(mapBase);
		map.add(BasicWaterConstructor.createSimpleRiver(new double[]{75,0,25,75,100,25,75,100},10));
		map.crop(mapBase);
		
		TestGUIManager gui = new TestGUIManager("TestCreateSimpleRiver");
		map.render(gui);
	}


	private static void TestNodesToPaths(){
		TestGUIManager gui = new TestGUIManager("TestNodesToPath");
		List<Node> nodes = BasicShapeModifier.pathToNodes(new Path2D.Double(new Rectangle2D.Double(0, 0, 2, 2)),1);
		Node.paintNodes(nodes, gui, Color.CYAN, Color.DARK_GRAY);
		Iterator<Path2D> pathIter = BasicShapeModifier.nodesToPaths(nodes).iterator();
		int index = 0;
		while(pathIter.hasNext()){
			System.out.println("Path: " + index++);
			gui.addShape(new Area(pathIter.next()), Color.BLUE);
		}
	}
	
	private static void TestPathToNodes(){
		TestGUIManager gui = new TestGUIManager("TestPathToNodes");
		Node.paintNodes(BasicShapeModifier.pathToNodes(new Path2D.Double(new Rectangle2D.Double(0, 0, 2, 2)),1), gui, Color.CYAN, Color.DARK_GRAY);
	}
	
	private static void TestLine2DToNodes(){
		TestGUIManager gui = new TestGUIManager("TestLine2DToNodes");
		
		int testCount = 0;
		
		for(int xAdjust = -10; xAdjust <= 10; xAdjust += 10){
			for(int yAdjust = -10; yAdjust <= 10; yAdjust += 10){
				double x1 = (testCount%3)*25;
				double y1 = (testCount/3)*25;
				double x2 = x1 + xAdjust;
				double y2 = y1 + yAdjust;
				List<Node> nodes = BasicShapeModifier.line2DToNodes(new Line2D.Double(x1, y1, x2, y2), 1);
				System.out.println("\nRight to Left: " + Node.nodesToString(nodes));
				Node.paintNodes(nodes, gui, Color.CYAN, Color.DARK_GRAY);
				testCount++;
			}
		}
	}
	
	private static void TestDistortSquare(){
		TestGUIManager gui = new TestGUIManager("TestDistortSquare");
		gui.addShape(new Rectangle2D.Double(0, 0, 10, 10), Color.BLUE);
		gui.addShape(BasicShapeModifier.distortArea(new Area(new Rectangle2D.Double(0, 0, 2, 2)),1.0,1.0), Color.BLUE);
	}
	
	private static void TestFillBlueBullseye(){

		TestGUIManager gui = new TestGUIManager("TestFillBlueBullseye");
		gui.addShape(BasicShapeConstructor.createBullseye(new Point2D.Double(50,50) ,100, 4, true), Color.BLUE);
	}
	
	private static void TestDrawPattern(){

		TestGUIManager gui = new TestGUIManager("TestDrawPattern");
		Area pattern = new Area(new Rectangle2D.Double(0,0,200,200)); 
		pattern.subtract(BasicShapeConstructor.createBullseye(new Point2D.Double(50,50) ,90, 1, true));
		pattern.subtract(BasicShapeConstructor.createBullseye(new Point2D.Double(150,150) ,50, 4, false));
		List<Line2D> lines = BasicShapeConstructor.getAreaLines(pattern,0.1,true);
		List<Area> areas = new LinkedList<Area>();
		Iterator<Line2D> iter = lines.iterator();
		while(iter.hasNext()){
			areas.add(new Area((new BasicStroke(1)).createStrokedShape(iter.next())));
		}
		//gui.addShape((new BasicStroke(1)).createStrokedShape(pattern), Color.RED);
		gui.addShape(BasicShapeConstructor.combineAreasParallel(areas), Color.BLACK);
	}
	
	private static void TestFillBullseysWithSand(){
		TestGUIManager gui = new TestGUIManager("TestFillBullseysWithSand");
		gui.addShape(BasicShapeConstructor.createBullseye(new Point2D.Double(50,50) ,100, 2, true), Color.BLUE);
		MaterialsCollection.Sand.renderFill(gui, BasicShapeConstructor.createBullseye(new Point2D.Double(50,50) ,100, 2, true));
	}
	
	private static void TestFillBullseysWithGrass(){
		TestGUIManager gui = new TestGUIManager("TestFillBullseysWithGrass");
		gui.addShape(BasicShapeConstructor.createBullseye(new Point2D.Double(50,50) ,100, 4, true), Color.BLUE);
		MaterialsCollection.Grass.renderFill(gui, BasicShapeConstructor.createBullseye(new Point2D.Double(50,50) ,100, 4, true));
	}
	
	private static void TestGetIntersectionPoint(){
		double 	minX = 0,
				maxX = 10,
				minY = 0,
				maxY = 10,
				step = 1;
		for(double curX = minX; curX <= maxX; curX += step){
			Line2D vertical = new Line2D.Double(new Point2D.Double(curX,minY), new Point2D.Double(curX,maxY));
			for(double curY = minY; curY <= maxY; curY += step){
				Line2D horizontal = new Line2D.Double( new Point2D.Double(minX,curY), new Point2D.Double(maxX,curY));
				Point2D intersection = BasicShapeConstructor.getIntersectionPoint(horizontal, vertical);
				if(intersection == null){
					System.out.println("curX: " + curX + ", curY: " + curY + "; No Intersetion.");
				} else {
					System.out.println("curX: " + curX + ", curY: " + curY + " Result: " + intersection.toString());
				}
			}
		}
	}
	
}
