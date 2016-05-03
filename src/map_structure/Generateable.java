package map_structure;

import java.awt.Shape;
import java.awt.geom.Area;

import area_constructors.Constructor;
import pyromancers_model.MapListed;
import test.TestGUIManager;

public interface Generateable extends Cloneable {

	public void render(TestGUIManager gui);
	
	public void crop(Shape s);
	
	public Generateable clone();
	
	public MapListed render();
	
	public Area getArea();
}
