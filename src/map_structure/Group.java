package map_structure;

import java.awt.Shape;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import area_constructors.Constructor;
import test.TestGUIManager;

public class Group implements Generateable {

	private List<Generateable> members;
	
	private Constructor constructor;
	
	public Group(){
		members = new LinkedList<Generateable>();
	}
	
	public Group(List<Generateable> mems){
		this();
		this.addAll(mems);
	}
	
	public Group(Generateable mem){
		this();
		this.add(mem);
	}
	
	public boolean add(Generateable mem){
		return members.add(mem);
	}
	
	public boolean addAll(List<Generateable> mems){
		return members.addAll(mems);
	}
	
	public void render(TestGUIManager gui) {
		Iterator<Generateable> iter = members.iterator();
		while(iter.hasNext()){
			iter.next().render(gui);
		}
	}

	public Generateable clone(){
		Group toReturn = new Group();
		Iterator<Generateable> iter = members.iterator();
		while(iter.hasNext()){
			toReturn.add(iter.next().clone());
		}
		return toReturn;
	}
	
	public void crop(Shape s) {
		Iterator<Generateable> iter = members.iterator();
		while(iter.hasNext()){
			iter.next().crop(s);
		}
	}

}
