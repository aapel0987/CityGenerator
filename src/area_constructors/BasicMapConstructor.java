package area_constructors;

import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;

import map_structure.Generateable;
import map_structure.Group;
import map_structure.AreaLayer;
import materials.MaterialsCollection;

public class BasicMapConstructor extends Constructor {

	private double width;
	private double height;
	
	public BasicMapConstructor(double w, double h){
		width = w;
		height = h;
	}
	
	public Group constructMap(){
		Area mapBase = new Area(new Rectangle2D.Double(0,0,width,height));
		return construct(mapBase);
	}
	
	public Group construct(Area routeableArea, Group currentMap) {
		Area localRoutingArea = new Area(routeableArea);
		localRoutingArea.subtract(currentMap.blockingArea(this).getArea());
		return this.createSimpleMap(routeableArea);
	}
	
	public Group createSimpleMap(Area routeableArea){
		//Here Maps are built in phases
		Group map = new Group("createSimpleMap",this);
		//Phase 0: Create Base Map
		AreaLayer mapBase = new AreaLayer(MaterialsCollection.Grass,routeableArea);
		map.add("mapBase", mapBase);
		//Phase 1: Add Large Rivers
		map.add("large_rivers", (new BasicWaterConstructor(15)).construct(mapBase,map));
		//Phase 2: Add Large Mountains
		//Phase 3: Add Small Rivers and Lakes
		//Phase 4: Add Large Roads
		//Phase 5: Add Urban Items
		//Phase 6: Add Forests
		map.add("forests",(new BasicForestConstructor(mapBase, 0.25, 0.05, 3)).construct(mapBase,map));
		//Phase 7: Add trees & ground cover
		map.add("ground_cover",(new BasicFieldConstructor(0.01)).construct(mapBase, map));
		//Phase 8: Crop the whole thing
		map.crop(mapBase);
		return map;
	}
	
	private void executePhase(String name, Constructor constructor, Area routeableArea, Group map){
		
	}
	
	public Group blockingArea(Constructor c, Group constructed) {
		Group blockingArea = new Group("BasicMapConstructor_blockingArea",this);
		blockingArea.add(this.conditionalGetBlockingArea("large_rivers", c, constructed));
		blockingArea.add(this.conditionalGetBlockingArea("forests", c, constructed));
		return blockingArea;
	}

}
