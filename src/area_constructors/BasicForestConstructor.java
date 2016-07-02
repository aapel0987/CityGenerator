package area_constructors;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.awt.geom.Point2D;

import map_structure.Group;
import map_structure.AreaLayer;
import materials.Material;
import materials.MaterialsCollection;
import test.TestGUIManager;

public class BasicForestConstructor extends Constructor {

	private static final Material tree = MaterialsCollection.Tree;
	
	private double coverageTarget;
	private double targetRatio; 
	private int count;
	
	public BasicForestConstructor(double t, double r, int c){
		coverageTarget = t;
		targetRatio = r;
		count = c;
	}
	
	public Group construct(Area routeableArea, Group currentMap) {
		Area localRoutingArea = new Area(routeableArea);
		localRoutingArea.subtract(currentMap.blockingArea(this).getArea());
		/*TestGUIManager gui = new TestGUIManager("BasicForestConstructor_construct");
		gui.addShape(routeableArea, Color.GREEN);
		gui.addShape(currentMap.blockingArea(this).getArea(), Color.RED);*/
		return ConstructComplexForest(localRoutingArea);
	}
	
	private Group ConstructComplexForest(Area routeableArea){
		Group forest = new Group("ConstructComplexForest",this);
		AreaLayer trees = new AreaLayer(tree);
		int iterations = count;
		while(iterations-- > 0){
			//Create squares
			Area forestArea = BasicShapeConstructor.ConstructComplexConnectedSquares(routeableArea,coverageTarget,targetRatio);
			//Apply Distortion
			forestArea = BasicShapeModifier.distortArea(forestArea,10,10,5);
			forestArea.intersect(routeableArea);
			trees.add(forestArea);
		}
		forest.add("trees", trees);
		/*TestGUIManager gui = new TestGUIManager("ConstructComplexForest");
		gui.addShape(routeableArea, Color.GREEN);
		gui.addShape(trees, Color.DARK_GRAY);*/
		return forest;
	}

	public Group blockingArea(Constructor c, Group constructed) {
		return constructed;
	}

}
