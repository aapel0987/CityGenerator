package materials;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Area;

import pyromancers_model.LocatorObject;
import test.TestGUIManager;

abstract public class Material {

	protected Color color;
	
	protected Material(Color c){
		color = c;
	}
	
	abstract public void renderFill(TestGUIManager gui, Area area);
	
	abstract public LocatorObject renderFill( Area area);
}
