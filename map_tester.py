#!/usr/bin/env python
# Test the map generator file
import random
from map_generator import *

def overlay_text_on_matrix_points(input_matrix,character,minstep,points):
	for point in points:
		if 0 <= int(point.x/minstep) and int(point.x/minstep) < len(input_matrix):
			if 0 <= int(point.y/minstep) and int(point.y/minstep) < len(input_matrix[int(point.x/minstep)]):
				input_matrix[int(point.x/minstep)][int(point.y/minstep)] = character
	return input_matrix


#Test File IO
if __name__ == '__main__':
	map = Map(100,100,1)
	ground = Layer(0,0,1,':')
	ground.shapes.append(BasicPolygon((BasicPoint(0,0),BasicPoint(0,100),BasicPoint(100,100),BasicPoint(100,0))))
	map.layers.append(ground)
	water = Layer(2,2,1,'~','#')
	water.shapes.append(BasicPolygon((BasicPoint(25,25),BasicPoint(25,75),BasicPoint(75,75),BasicPoint(75,25))))
	map.layers.append(water)
	island = Layer(2,2,1,'#')
	island.shapes.append(BasicCircle(20,50,50))
	map.layers.append(island)
	print map.tostring()
	map.dump('map_sources/testfile')
	map2 = loadmap('map_sources/testfile')
	print map2.tostring()
	
	
	#Let's make an ASCII Map! Allocate a Matrix
	spaceMatrix = [[' ' for x in range(int(map.height/map.minstep))] for x in range(int(map.width/map.minstep))] 
	for layer in map.layers:
		if layer.edge_material == None and layer.core_material != None : #Core everywhere
			overlay_text_on_matrix_points(spaceMatrix,layer.core_material,map.minstep,get_polygon_points(layer.get_polygon(map.minstep),map.minstep))
		else:
			if layer.edge_material != None:
				overlay_text_on_matrix_points(spaceMatrix,layer.edge_material,map.minstep,get_perimiter_points(layer.get_polygon(map.minstep),map.minstep))
			if layer.core_material != None :
				overlay_text_on_matrix_points(spaceMatrix,layer.core_material,map.minstep,get_enclosed_points(layer.get_polygon(map.minstep),map.minstep))
				
	#Now let's print this to a file
	f = open('test_map', 'w')
	for y in range(len(spaceMatrix[0][:])):
		line = ''
		for x in range(len(spaceMatrix[:][0])):
			line += spaceMatrix[x][y]
		line += '\n'
		f.write(line)
	f.flush()
	f.close()
	
	#And let's see what we get!