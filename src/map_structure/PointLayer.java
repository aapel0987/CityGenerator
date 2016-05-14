package map_structure;

import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import materials.Material;
import materials.MaterialPoint;
import test.TestGUIManager;

public class PointLayer extends Area implements Generateable {

	private LinkedList<Point2D> points;
	private MaterialPoint material;
	
	public PointLayer(MaterialPoint mat, double density, Area area){
		this(mat,selectPointSubset(mat,density,area),area);
	}

	private static ArrayList<Point2D> selectPointSubset(MaterialPoint mat, double density, Area area){
		ArrayList<Point2D> allPoints = mat.getFillPoints(area);
		int requiredPoints = (int) Math.ceil(allPoints.size()*density);
		if(requiredPoints <= 0) allPoints.clear();
		while(allPoints.size() > requiredPoints){
			allPoints.remove(random.nextInt(allPoints.size()));
		}
		return allPoints;
	}
	
	public PointLayer(MaterialPoint mat, Collection<Point2D> _points, Area area){
		super(area);
		points = new LinkedList<Point2D>(_points);
		material = mat;
	}
	
	@Override
	public void render(TestGUIManager gui) {
		material.renderFill(gui, points);
	}

	@Override
	public void crop(Shape s) {
		this.intersect(new Area(s));
		Iterator<Point2D> iterator = points.iterator();
		while(iterator.hasNext()) if(!this.contains(iterator.next())) iterator.remove();
	}

	@Override
	public Generateable clone() {
		return new PointLayer(material,points,this);
	}

	@Override
	public Area getArea() {
		return this;
	}

	public Material getMaterial() {
		return material;
	}

	public ArrayList<Point2D> getPoints() {
		return new ArrayList<Point2D>(points);
	}

}
