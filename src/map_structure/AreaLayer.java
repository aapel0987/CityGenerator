package map_structure;

import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import materials.Material;
import test.TestGUIManager;

public class AreaLayer extends Area implements Generateable {

	private Material material;

	public AreaLayer(Material mat){
		super();
		material = mat;
	}

	public AreaLayer(Material mat, Point2D upLeft, double width, double height){
		this(mat,upLeft.getX(),upLeft.getY(),width,height);
	}
	
	public AreaLayer(Material mat, double upLeftX, double upLeftY, double width, double height){
		this(mat,new Area(new Rectangle2D.Double(upLeftX,upLeftY,width,height)));
	}
	
	public AreaLayer(Material mat, Area a){
		super(a);
		material = mat;
	}

	public void render(TestGUIManager gui) {
		material.renderFill(gui, this);
	}

	public Generateable clone(){
		return new AreaLayer(material,new Area(this));
	}

	public void crop(Shape s) {
		this.intersect(new Area(s));
	}

	public Area getArea(){
		return this;
	}
	
	public Material getMaterial(){
		return material;
	}
}
