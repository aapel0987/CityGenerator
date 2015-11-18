#!/usr/bin/env python

#Code for Doing Math on Polygons

import sympy
from sympy import *
import math

#Custom rounding function
def custom_round(value,portion):
	mod = math.fmod(value,portion)
	if mod > portion/2:
		value += portion/2 -mod
	else:
		value -= mod
	return value

def construct_polygon_graph(poly_list):
	return None
	
def list_connected_polygons(poly_graph):
	return None
	
def polygon_union(poly_list):
	return poly_list[0]

def polygon_intersection(poly_list):
	return poly_list[0]

def polygon_compliment(minuend_poly_list, subtrahend_poly_list):
	return minuend_poly_list[0]

def next_point(side,cur_point,minstep,reverse):
	#If we are moving in the reverse direction
	if reverse:
		start_point = side.p2
		end_point   = side.p1
	else:
		start_point = side.p1
		end_point = side.p2
	slope = side.slope
	if math.fabs(slope) > 1:	#Large Slope
		if start_point.y < end_point.y: # Increasing Y
			return Point(cur_point.x + minstep/slope,cur_point.y + minstep)
		else: #Decreasing Y
			return Point(cur_point.x - minstep/slope,cur_point.y - minstep)
	else:
		if start_point.x < end_point.x: # Increasing X
			return Point(cur_point.x + minstep,cur_point.y + minstep*slope)
		else: #Decreasing X
			return Point(cur_point.x - minstep,cur_point.y - minstep*slope)
	
def get_perimiter(polygon,minstep):
	poly_points = []
	end_point = None
	for side in polygon.sides:
		#Determine Direction of Movement
		if end_point == None or end_point == side.p1:
			cur_point = side.p1
			end_point = side.p2
			reverse = False
		else:	#In this case the end of the last segment is the end of this one
			cur_point = side.p2
			end_point = side.p1
			reverse = True
		#Choose the start and end 
		while(side.contains(cur_point) or Segment(cur_point,end_point).length < minstep/2):
			rounded_point = Point(custom_round(cur_point.x,minstep),custom_round(cur_point.y,minstep))		
			if rounded_point not in poly_points:
				poly_points.append(rounded_point)
			cur_point = next_point(side,cur_point,minstep,reverse)
	return poly_points
	
def get_enclosed_points(polygon,minstep):
	points = []
	for side in polygon.sides:
		cur_point = side.p1
		while(side.contains(cur_point)):
			rounded_point = Point(custom_round(cur_point.x,minstep),custom_round(cur_point.y,minstep))
			if rounded_point not in points:
				points.append(rounded_point)
			cur_point = next_point(side,cur_point,minstep)
	return Polygon(*points)

#Test The Code
if __name__ == '__main__':
	print "\n\n\t\t** Starting Test **\n\n"
	poly0 = Polygon(Point(0,0),Point(1,0),Point(5,7),Point(0,1))
	print "\n\t\t** Point 1 **\n\n"
	points1 = get_perimiter(poly0,1)
	print points1
	segments1 = points2segments(points1)
	print segments1
	poly1 = Polygon(*segments1)
	print poly1.vertices
	points2 = get_perimiter(poly1,1)
	segments2 = points2segments(points2)
	poly2 = Polygon(*segments2)
	print "\n\t\t** Point 2 **\n\n"
	print points2
	print segments2
	print poly2.vertices
	if points1 != points2:
		print "Error! Mismatch in test 1."
		print points1
		print points2
	else:
		print "No Errors Detected"
