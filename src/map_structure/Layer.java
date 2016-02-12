package map_structure;

import java.awt.geom.Area;

import materials.MaterialBase;
import test.TestGUIManager;

public class Layer extends Area implements Generateable {

	private MaterialBase material;

	public Layer(MaterialBase mat){
		super();
		material = mat;
	}

	public Layer(MaterialBase mat, Area a){
		super(a);
		material = mat;
	}

	public void render(TestGUIManager gui) {
		material.renderFill(gui, this);
	}

	public Layer clone(){
		return new Layer(material,new Area(this));
	}
}
