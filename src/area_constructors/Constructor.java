package area_constructors;

import java.awt.geom.Area;

import map_structure.Generateable;
import map_structure.Group;

public abstract class Constructor {

	boolean isCrossable(Constructor c){
		return crossingConstructor(c) != null;
	}
	
	abstract Constructor crossingConstructor(Constructor c);
	
	abstract Group crossArea(Area crossing, Generateable constructed );
}
