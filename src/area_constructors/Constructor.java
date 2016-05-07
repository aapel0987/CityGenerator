package area_constructors;

import java.awt.Rectangle;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.Random;

import map_structure.Generateable;
import map_structure.Group;

public abstract class Constructor {

	protected final static double GOLDEN_RATIO = 1.61803399;
	protected final static Random random = new Random();
	
	protected static Point2D getRandomPoint(Rectangle bounds){
		return new Point2D.Double(random.nextDouble()*bounds.getWidth() + bounds.getMinX(), random.nextDouble()*bounds.getHeight() + bounds.getMinY());
	}
		
	public abstract Group blockingArea(Constructor c, Group constructed );
	
	public abstract Group construct(Area routeableArea, Group currentMap);
	
	public Group construct(Area routeableArea){
		return construct(routeableArea,new Group("Constructor_construct_no_group_arg",this));
	}
	
	protected Group conditionalGetBlockingArea(String name, Constructor c, Group constructed ){
		Generateable retreievedMember = constructed.get(name);
		if(retreievedMember instanceof Group) return ((Group) retreievedMember).blockingArea(c);
		else return new Group("conditionalGetCrossableArea_null", this);
	}
	
	protected Area mergeGroupsToArea(Collection<Generateable> mapItems){
		Area toReturn = new Area();
		for(Generateable mapItem : mapItems) toReturn.add(mapItem.getArea());
		return toReturn;
	}
	
	protected static double getRandomNormal(double maximum){
		return getRandomNormal(0,maximum);
	}
	
	protected static double getRandomNormal(double minimum, double maximum){
		return getRandomNormal(minimum,maximum,(minimum+maximum)/2);
	}
	
	protected static double getRandomNormal(double minimum, double maximum, double mean){
		double standardDeviation = Math.max((mean-minimum)/3, (maximum-mean)/3);
		return getRandomNormal(minimum,maximum,mean,standardDeviation);
	}
	
	protected static double getRandomNormal(double minimum, double maximum, double mean, double standardDeviation){
		return Math.min(Math.max(random.nextGaussian()*standardDeviation + mean,minimum),maximum);
	}
}
