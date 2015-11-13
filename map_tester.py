#!/usr/bin/env python
# Test the map generator file
import random
from map_generator import *

#Test File IO
if __name__ == '__main__':
	map = Map(100,100,0.1)
	ground = Layer(1,1,0.1,':')
	ground.shapes.append(BasicPolygon((BasicPoint(0,0),BasicPoint(0,100),BasicPoint(100,100),BasicPoint(100,0))))
	map.layers.append(ground)
	#water = Layer(2,2,0.1,'~','#')
	#map.layers.append(water)
	print map.tostring()
	map.dump('map_sources/testfile')
	map2 = loadmap('map_sources/testfile')
	print map2.tostring()
