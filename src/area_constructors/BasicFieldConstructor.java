package area_constructors;

import java.awt.geom.Area;

import map_structure.Group;
import map_structure.PointLayer;
import materials.MaterialPoint;
import materials.MaterialsCollection;

public class BasicFieldConstructor extends Constructor {

	private static final MaterialPoint brush = MaterialsCollection.Brush;
	private double density;
	
	BasicFieldConstructor(double _density){
		density = _density;
	}
	
	@Override
	public Group blockingArea(Constructor c, Group constructed) {
		return constructed;
	}

	@Override
	public Group construct(Area routeableArea, Group currentMap) {
		Area localRoutingArea = new Area(routeableArea);
		localRoutingArea.subtract(currentMap.blockingArea(this).getArea());
		Group forest = new Group("BasicFieldConstructor_construct",this);
		PointLayer groundCover = new PointLayer(brush,density,localRoutingArea);
		forest.add("brush", groundCover);
		return forest;
	}

}
