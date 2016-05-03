package map_structure;

import java.awt.Shape;
import java.awt.geom.Area;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import area_constructors.Constructor;
import pyromancers_model.Bunch;
import pyromancers_model.Location;
import pyromancers_model.MapListed;
import test.TestGUIManager;

public class Group implements Generateable {

	private LinkedHashMap<String,Generateable> members;
	private String name;
	private int nameless_additions;
	
	private Constructor constructor;
	
	public Group(String _name, Constructor c){
		constructor = c;
		name = _name;
		members = new LinkedHashMap<String,Generateable>();
		nameless_additions = 0;
	}
	
	public Group(String _name, Constructor c, List<Generateable> mems){
		this(_name,c);
		this.addAll(mems);
	}
	
	public Group(String _name, Constructor c, Generateable mem){
		this(_name,c);
		this.add(mem);
	}

	public boolean add(Generateable mem){
		return add("Group_add_no_name_argument_" + nameless_additions++,mem);
	}
	
	public boolean add(String name, Generateable mem){
		members.put(name,mem);
		return true;
	}
	
	public boolean addAll(Collection<Generateable> mems){
		for(Generateable member : mems) add(member);
		return true;
	}
	
	public Generateable get(String name){
		return members.get(name);
	}
	
	public void render(TestGUIManager gui) {
		for(Generateable member : members.values()) member.render(gui);
	}

	public Generateable clone(){
		Group toReturn = new Group(name,constructor);
		for(Generateable member : members.values()) toReturn.add(member.clone());
		return toReturn;
	}
	
	public void crop(Shape s) {
		for(Generateable member : members.values()) member.crop(s);
	}
	
	public Location renderMap(){
		return new Location(this);
	}

	public MapListed render() {
		return new Bunch(name,members.values());
	}
	
	public Collection<Generateable> getMembers(){
		return members.values();
	}
	
	public Area getArea(){
		Area toReturn = new Area();
		for(Generateable member : members.values()) toReturn.add(member.getArea());
		return toReturn;
	}

	public Group blockingArea(Constructor c){
		return constructor.blockingArea(c, this);
	}
}
