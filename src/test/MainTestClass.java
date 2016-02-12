package test;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import area_constructors.BasicShapeConstructor;
import materials.MaterialPoint;
import materials.MaterialsCollection;

public class MainTestClass {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println("Hello again Java, my dear old friend!");
		//TestFillBlueBullseye();
		//TestDrawBlueBullseye();
		//TestFillBullseysWithSand();
		TestFillBullseysWithGrass();
		//TestGetIntersectionPoint();
	}

	private static void TestFillBlueBullseye(){

		TestGUIManager gui = new TestGUIManager("TestFillBlueBullseye");
		gui.addShape(BasicShapeConstructor.createBullseye(new Point2D.Double(50,50) ,100, 4, true), Color.BLUE);
	}
	
	private static void TestDrawBlueBullseye(){

		TestGUIManager gui = new TestGUIManager("TestDrawBlueBullseye");
		Area bullseye = BasicShapeConstructor.createBullseye(new Point2D.Double(50,50) ,100, 1, true);
		List<Line2D> lines = BasicShapeConstructor.MyGetAreaLines(bullseye,0.1,true);
		List<Area> areas = new LinkedList<Area>();
		Iterator<Line2D> iter = lines.iterator();
		while(iter.hasNext()){
			areas.add(new Area((new BasicStroke(1)).createStrokedShape(iter.next())));
		}
		//gui.addShape((new BasicStroke(1)).createStrokedShape(bullseye), Color.RED);
		gui.addShape(BasicShapeConstructor.combineAreasParallel(areas), Color.BLACK);
	}
	
	private static void TestFillBullseysWithSand(){
		TestGUIManager gui = new TestGUIManager("TestFillBullseysWithSand");
		gui.addShape(BasicShapeConstructor.createBullseye(new Point2D.Double(50,50) ,100, 4, true), Color.BLUE);
		MaterialsCollection.Sand.renderFill(gui, BasicShapeConstructor.createBullseye(new Point2D.Double(50,50) ,100, 4, true));
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
				Point2D intersection = MaterialPoint.getIntersectionPoint(horizontal, vertical);
				if(intersection == null){
					System.out.println("curX: " + curX + ", curY: " + curY + "; No Intersetion.");
				} else {
					System.out.println("curX: " + curX + ", curY: " + curY + " Result: " + intersection.toString());
				}
			}
		}
	}
	
}
