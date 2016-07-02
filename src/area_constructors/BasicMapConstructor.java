package area_constructors;

import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;

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
		System.out.println("Phase 0: Create Base Map");
		AreaLayer mapBase = new AreaLayer(MaterialsCollection.Grass,routeableArea);
		map.add("mapBase", mapBase);
		System.out.println("Phase 1: Add Large Rivers");
		BasicWaterConstructor waterConstructor = new BasicWaterConstructor();
		map.add("large_rivers", waterConstructor.getConstructedGroup());
		waterConstructor.createComplexRiver(routeableArea, map, 15);
		System.out.println("Phase 2: Add Large Mountains");
		System.out.println("Phase 3: Add Small Rivers and Lakes");
		//waterConstructor.addLakes(mapBase, map, 4, 0.3, 0.5);
		//waterConstructor.connectWaterBodies(mapBase, map,4);
		System.out.println("Phase 4: Add Large Roads");
		BasicLineCrossingConstructor stoneBridgeConstructor = new BasicLineCrossingConstructor(2.5,MaterialsCollection.Stone,waterConstructor);
		BasicRoadConstructor dirtRoadConstructor = new BasicRoadConstructor(2,MaterialsCollection.MuddyGrass,stoneBridgeConstructor);
		map.add("large_roads", dirtRoadConstructor.construct(mapBase, map));
		System.out.println("Phase 5: Add Urban Items");
		System.out.println("Phase 6: Add Forests");
		map.add("forests",(new BasicForestConstructor(0.25, 0.05, 3)).construct(mapBase,map));
		System.out.println("Phase 7: Add trees & ground cover");
		map.add("ground_cover",(new BasicFieldConstructor(0.01)).construct(mapBase, map));
		System.out.println("Phase 8: Crop the whole thing");
		map.crop(mapBase);
		return map;
	}
	
	private void executePhase(String name, Constructor constructor, Area routeableArea, Group map){
		
	}
	
	public Group blockingArea(Constructor c, Group constructed) {
		return constructed.getBlockingAreaOfAllMembers(c);
	}

}
