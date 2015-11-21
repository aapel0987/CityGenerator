#!/usr/bin/env python

#Code for Generating maps
# Goals:
#		Accept a file that specifies map geometery
#			Height/Width
#			Step Size
#			Layers
#				Shapes
#					Required:
#						Rectangle
#							Start Center, Inner Right Edge, Outer Right Edge; Optional: Inner Left Edge, Outer Right Edge, End Offset
#							End Center; Optional: Inner Right Edge, Outer Right Edge, Inner Left Edge, Outer Right Edge, End Offset
#						Circle
#							Center, Inner Radius, Outer Radius, Step Size
#					Optional:
#						Polygon
#							Coorinates of points
#						Ellipse
#							Start, Inner Radius, Outer Radius, Step Size, Length, Angle
#				Terrain Type
#					Point
#					Polygon
#					Identifier
#				Wanderer Information
#					Reason - How heavily the current destination is weighted
#					Momentum - How heavily the last few steps are weighted
#					Repulsion - The forcefullness of the borders of the geometery
#
#		Convert map geometerys to polygons to be traversed by wanderer, including
#			Converting round-ish objects to polygons
#			Merging Polygons
#
#		Create a 'wanderer' to traverse geometery based on provided information

import random
import json
from json import *
import sympy
import polygon_math
from polygon_math import *
import polygon_walker
from polygon_walker import *

class ComplexEncoder(json.JSONEncoder):
	def default(self, obj):
		return obj.__dict__

# Base Map Shapes
class BasicPoint:
	def __init__(self,x,y):
		self.type = self.__class__.__name__
		self.x = x
		self.y = y

def basicpoint_decoder(dictonary):
	if 'type' in dictonary and dictonary['type'] == 'BasicPoint':
		to_return = BasicPoint(dictonary['x'], dictonary['y'])
		return to_return
	return dictonary

class BasicPolygon:
	def __init__(self,points=None,basex=None,basey=None):
		if basex == None: basex = 0
		if basey == None: basey = 0
		if points == None: points = []
		self.type = self.__class__.__name__
		self.points = points
		self.basex = basex
		self.basey = basey
		
	def is_walkable(self):
		return False

	def get_polygon(self):
		points = []
		for point in self.points:
			points.append(Point(point.x,point.y))
		return Polygon(*points)

def basicpolygon_decoder(dictonary):
	if 'type' in dictonary and dictonary['type'] == 'BasicPolygon':
		to_return = BasicPolygon(None,dictonary['basex'], dictonary['basey'])
		for point in dictonary['points']:
			to_return.points.append(basicpoint_decoder(point))
		return to_return
	return dictonary

		
class WalkableRectangle:
	def __init__(self,minstep,startx,starty,endx,endy,st_o_r_len,st_i_r_len=None,st_o_l_len=None,st_i_l_len=None,st_offest=None,en_o_r_len=None,en_i_r_len=None,en_o_l_len=None,en_i_l_len=None, en_offest=None):
		#Based on what's missing fill in the gaps
		if st_i_r_len == None:	st_i_r_len = st_o_r_len -2*minstep
		if st_o_l_len == None:	st_o_l_len = st_o_r_len
		if st_i_l_len == None:	st_i_l_len = st_i_r_len
		if st_offest  == None:	st_offest  = 2*minstep
		if en_o_r_len == None:	en_o_r_len = st_o_r_len
		if en_i_r_len == None:	en_i_r_len = st_i_r_len
		if en_o_l_len == None:	en_o_l_len = en_o_r_len
		if en_i_l_len == None:	en_i_l_len = en_i_r_len
		if en_offest  == None:	en_offest  = st_offest
		self.type = self.__class__.__name__
		self.minstep    = minstep
		self.startx     = startx
		self.starty     = starty
		self.endx       = endx
		self.endy       = endy
		self.st_o_r_len = st_o_r_len
		self.st_i_r_len = st_i_r_len
		self.st_o_l_len = st_o_l_len
		self.st_i_l_len = st_i_l_len
		self.st_offest  = st_offest
		self.en_o_r_len = en_o_r_len
		self.en_i_r_len = en_i_r_len
		self.en_o_l_len = en_o_l_len
		self.en_i_l_len = en_i_l_len
		self.en_offest  = en_offest
		
	def is_walkable(self):
		return True

def walkablerectangle_decoder(dictonary):
	if 'type' in dictonary and dictonary['type'] == 'WalkableRectangle':
		to_return = WalkableRectangle(dictonary['minstep'], dictonary['startx'], dictonary['starty'], dictonary['endx'], dictonary['endy'], dictonary['st_o_r_len'], dictonary['st_i_r_len'], dictonary['st_o_l_len'], dictonary['st_i_l_len'], dictonary['st_offest'], dictonary['en_o_r_len'], dictonary['en_i_r_len'], dictonary['en_o_l_len'], dictonary['en_i_l_len'], dictonary['en_offest'])
	return dictonary

class WalkableCircle:
	def __init__(self,minstep,basex,basey,outer_radius,inner_radius=None):
		if inner_radius == None: inner_radius = outer_radius - 2*minstep
		self.type = self.__class__.__name__
		self.minstep = minstep
		self.basex = basex
		self.basey = basey
		self.outer_radius = outer_radius
		self.inner_radius = inner_radius
		
	def is_walkable(self):
		return True

def walkablecircle_decoder(dictonary):
	if 'type' in dictonary and dictonary['type'] == 'WalkableCircle':
		to_return = WalkableCircle(dictonary['minstep'], dictonary['basex'], dictonary['basey'], dictonary['outer_radius'], dictonary['inner_radius'])
		return to_return
	return dictonary

def shape_decoder(dictonary):
	if 'type' in dictonary and dictonary['type'] == 'WalkableRectangle':
		return walkablerectangle_decoder(dictonary)
	elif 'type' in dictonary and dictonary['type'] == 'WalkableCircle':
		return walkablecircle_decoder(dictonary)
	elif 'type' in dictonary and dictonary['type'] == 'BasicPolygon':
		return basicpolygon_decoder(dictonary)
	return dictonary

class Layer:
	def __init__(self,basex,basey,minstep, core_material, edge_material=None):
		self.type = self.__class__.__name__
		self.basex = basex
		self.basey = basey
		self.minstep = minstep
		self.core_material = core_material
		self.edge_material = edge_material
		self.shapes = []		

	def get_polygon(self):
		single_polygons = []
		internal_polygons = []
		external_polygons = []
		for shape in self.shapes:
			if shape.is_walkable():
				internal_polygons.append(shape.get_internal_polygon())
				external_polygons.append(shape.get_external_polygon())
			else:
				single_polygons.append(shape.get_polygon())
		single_polygons.append(walk_polygons(polygon_union(external_polygons),polygon_union(internal_polygons)))
		return polygon_union(single_polygons)
		
	def tostring(self):
		to_return = "basex: " + str(self.basex) + ", basey: " + str(self.basey) + ", minstep: " + str(self.minstep)
		return to_return

def layer_decoder(dictonary):
	if 'type' in dictonary and dictonary['type'] == 'Layer':
		to_return = Layer(dictonary['basex'], dictonary['basey'], dictonary['minstep'], dictonary['core_material'], dictonary['edge_material'])
		for shape in dictonary['shapes']:
			to_return.shapes.append(shape_decoder(shape))
		return to_return
	return dictonary

class Map:
	
	def __init__(self,height,width,minstep):
		self.type = self.__class__.__name__
		self.height = height
		self.width = width
		self.minstep = minstep
		self.layers = []

	def load(self, fname):
		self = loadmap(fname)
		
	def tostring(self):
		to_return = "height: " + str(self.height) + ", width: " + str(self.width) + ", minstep: " + str(self.minstep)
		for layer in self.layers:
			to_return += "\n\tLayer: " + layer.tostring()
		return to_return
	
	def dump(self, fname, compressed=False):
		fout = open(fname, 'w+')
		if compressed:
			json.dump(self, fout, indent=None, separators=(',',':'), cls=ComplexEncoder)
		else:
			json.dump(self, fout, indent=4, separators=(',',': '), cls=ComplexEncoder)
		fout.flush()
		fout.close()

def map_decoder(dictonary):
	if 'type' in dictonary and dictonary['type'] == 'Map':
		to_return = Map(dictonary['height'], dictonary['width'], dictonary['minstep'])
		for layer in dictonary['layers']:
			to_return.layers.append(layer_decoder(layer))
		return to_return
	return dictonary

def loadmap(fname):
	fin = open(fname)
	to_return = json.load(fin, object_hook=map_decoder)
	fin.close()
	return to_return

