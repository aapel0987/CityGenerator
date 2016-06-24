package test;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import area_constructors.BasicForestConstructor;
import area_constructors.BasicMapConstructor;
import area_constructors.BasicShapeConstructor;
import area_constructors.BasicShapeModifier;
import area_constructors.BasicWaterConstructor;
import area_constructors.DefaultPathConstructor;
import map_structure.Group;
import map_structure.AreaLayer;
import materials.MaterialPoint;
import materials.MaterialsCollection;
import pyromancers_model.Location;
import pyromancers_model.Packs;
import pyromancers_model.PyromancersMapFactory;
import pyromancers_model.TextureTree;
import pyromancers_model.UtilityBase;

public class MainTestClass {

	public static void main(String[] args) {
		System.out.println("Hello again Java, my dear old friend!");
		final long startTime = System.currentTimeMillis();
		//TestFillBlueBullseye();
		//TestDrawPattern();
		//TestFillBullseysWithSand();
		//TestFillBullseysWithGrass();
		//TestGetIntersectionPoint();
		//TestCreateSimpleRiverPoints();
		//TestCreateSimpleRiverPath();
		//TestLine2DToNodes();
		//TestPathToNodes();
		//TestNodesToPaths();
		//TestDistortSquare();
		//TestGetBestPath();
		//TestGsonBuilder();
		//TestPyromancersModel();
		//TestPackConstructor();
		//TestComplexRiverPath();
		//TestDefaultPathConstructor();
		TestMapConstructor();
		

		final long endTime = System.currentTimeMillis();
		System.out.println("Tests Complete.");
		System.out.println("Total execution time: " + (endTime - startTime) );
	}

	private static void TestMapConstructor(){
		
		System.out.println("Building Map");
		BasicMapConstructor mapConstructor = new BasicMapConstructor(150,100);
		Group map = mapConstructor.constructMap();
		
		System.out.println("Rendering Preview");
		TestGUIManager gui = new TestGUIManager("TestMapConstructor");
		map.render(gui);
		
		System.out.println("Writing to File");
		String filename = "C:\\Users\\Alex\\Google Drive\\CodeProjects\\CityGenerator\\map_examples\\TestMapConstructor.rdm";
		//PyromancersMapFactory.writeToFile(map, filename);;
	}
	
	private static void TestDefaultPathConstructor(){
		TestGUIManager gui = new TestGUIManager("TestDefaultPathConstructor");
		DefaultPathConstructor constructor = new DefaultPathConstructor();
		double size = 3500/5;
		Area base = new Area(new Rectangle2D.Double(0, 0, size, size));
		gui.addShape(base, Color.GREEN);
		
		Random random = new Random();
		
		
		Area blockers = new Area();
		for(int count = random.nextInt(10) +1; count > 0; count--){
			Point2D center = new Point2D.Double(random.nextDouble()*size, random.nextDouble()*size);
			double radius = random.nextDouble()*size/2;
			blockers.add(BasicShapeConstructor.basicCircle(center, radius));
		}
		blockers.intersect(base);
		gui.addShape(blockers, Color.RED);

		Area routeableArea = new Area(base);
		routeableArea.subtract(blockers);
		
		int pointsToKeep = random.nextInt(6 -2 +1) +2;
		System.out.println("Initial Points: " + pointsToKeep);
		ArrayList<Point2D> pointOptions = new ArrayList<Point2D>(BasicShapeConstructor.getAreaEdgePoints(routeableArea,0.01));
		ArrayList<Point2D> selectedPoints = new ArrayList<Point2D>();
		
		while(selectedPoints.size() < pointsToKeep) selectedPoints.add(pointOptions.remove(random.nextInt(pointOptions.size())));
		gui.addPoints(selectedPoints, size * (0.25/15) , Color.DARK_GRAY);

		Path2D path = constructor.getPath(routeableArea, new Group("null", constructor), new HashSet<Point2D>(selectedPoints), 1.0, 0.1, 8);
		
		BasicStroke stroke = new BasicStroke((float) (size * (0.05/15)) );
		gui.addShape(stroke.createStrokedShape(path), Color.BLACK);
	}
	
	private static void TestDistortSquare(){
		TestGUIManager gui = new TestGUIManager("TestDistortSquare");
		gui.addShape(new Rectangle2D.Double(0, 0, 10, 10), Color.BLUE);
		gui.addShape(BasicShapeModifier.distortArea(new Area(new Rectangle2D.Double(0, 0, 100, 100)),1.0,1.0), Color.BLUE);
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
	
    private static void TestGsonBuilder() {
        Gson gson = new GsonBuilder().create();
        gson.toJson("Hello", System.out);
        gson.toJson(123, System.out);
    }
    
    private static void TestPackConstructor(){
    	System.out.println(Packs.getMapItem(MaterialsCollection.Grass).toString());
    	System.out.println(Packs.getMapItem(MaterialsCollection.Sand).toString());
    	System.out.println(Packs.getMapItem(MaterialsCollection.Water).toString());
    }
	
}
