#!/usr/bin/env python

#Code for Doing Math on Polygons

import sympy
from sympy import *
import math
import copy

#Custom rounding function
def custom_round(value,portion):
	mod = math.fmod(value,portion)
	if mod > portion/2:
		value += portion -mod
	else:
		value -= mod
	return value

def construct_polygon_graph(poly_list):
	return None
	
def list_connected_polygons(poly_graph):
	return None
	
def polygon_union(poly_list):
	if len(poly_list) == 0:
		return None
	return poly_list[0]

def polygon_intersection(poly_list):
	return None

def polygon_compliment(minuend_poly_list, subtrahend_poly_list):
	return None

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

def get_in_between_points(start_point,end_point,minstep):
	points = []
	aligned_start_point = Point(custom_round(start_point.x,minstep),custom_round(start_point.y,minstep))
	aligned_end_point   = Point(custom_round(  end_point.x,minstep),custom_round(  end_point.y,minstep))
	#Slope = rise/run
	slope = (aligned_end_point.y - aligned_start_point.y)/(aligned_end_point.x - aligned_start_point.x)
	#Now there are two options, either take minimum steps in the X direction and round the Y, or the reverse
	#Decide based on which gives the least error. The least amount of error should result when stepping along
	#the axis with the most steps? Pretty sure, it's intuitive at this point though.
	#Step on Y Axis
	if math.fabs(aligned_end_point.y - aligned_start_point.y) > math.fabs(aligned_end_point.x - aligned_start_point.x):
		if aligned_start_point.y < aligned_end_point.y: # Increasing Y
			#EEEEK Scary local optimizations! This actually really helps with filling polygons
			if aligned_end_point.x == aligned_start_point.x:
				cur_point = Point(aligned_start_point.x,aligned_start_point.y + minstep)
				while cur_point.y < aligned_end_point.y:
					points.append(cur_point)
					cur_point = Point(cur_point.x,cur_point.y + minstep)
			else:
				cur_point = Point(aligned_start_point.x + minstep/slope,aligned_start_point.y + minstep)
				while cur_point.y < aligned_end_point.y:
					points.append(Point(custom_round(cur_point.x,minstep),cur_point.y))
					cur_point = Point(cur_point.x + minstep/slope,cur_point.y + minstep)
		else: #Decreasing Y
			#EEEEK Scary local optimizations! This actually really helps with filling polygons
			if aligned_end_point.x == aligned_start_point.x:
				cur_point = Point(aligned_start_point.x,aligned_start_point.y - minstep)
				while cur_point.y > aligned_end_point.y:
					points.append(cur_point)
					cur_point = Point(cur_point.x,cur_point.y - minstep)
			else:
				cur_point = Point(aligned_start_point.x - minstep/slope,aligned_start_point.y - minstep)
				while cur_point.y > aligned_end_point.y:
					points.append(Point(custom_round(cur_point.x,minstep),cur_point.y))
					cur_point = Point(cur_point.x - minstep/slope,cur_point.y - minstep)
	else:
		if aligned_start_point.x < aligned_end_point.x: # Increasing X
			#EEEEK Scary local optimizations! This actually really helps with filling polygons
			if aligned_end_point.y == aligned_start_point.y:
				cur_point = Point(aligned_start_point.x + minstep,aligned_start_point.y)
				while cur_point.x < aligned_end_point.x:
					points.append(cur_point)
					cur_point = Point(cur_point.x + minstep,cur_point.y)
			else:
				cur_point = Point(aligned_start_point.x + minstep,aligned_start_point.y + minstep*slope)
				while cur_point.x < aligned_end_point.x:
					points.append(Point(cur_point.x,custom_round(cur_point.y,minstep)))
					cur_point = Point(cur_point.x + minstep,cur_point.y + minstep*slope)
		else: #Decreasing X
			#EEEEK Scary local optimizations! This actually really helps with filling polygons
			if aligned_end_point.y == aligned_start_point.y:
				cur_point = Point(aligned_start_point.x - minstep,aligned_start_point.y)
				while cur_point.x > aligned_end_point.x:
					points.append(cur_point)
					cur_point = Point(cur_point.x - minstep,cur_point.y)
			else:
				cur_point = Point(aligned_start_point.x - minstep,aligned_start_point.y - minstep*slope)
				while cur_point.x > aligned_end_point.x:
					points.append(Point(cur_point.x,custom_round(cur_point.y,minstep)))
					cur_point = Point(cur_point.x - minstep,cur_point.y - minstep*slope)
	return points

def align_polygon(polygon,minstep):
	points = []
	for point in polygon.vertices:
		points.append(Point(custom_round(point.x,minstep),custom_round(point.y,minstep)))
	return Polygon(*points)

#Sort points first based on x axis position
def point_sort_x_axis(p0,p1):
	return int(p0.x - p1.x)
	
def get_perimiter_points(polygon,minstep):
	poly_points = []
	end_point = None
	aligned_polygon = align_polygon(polygon,minstep)
	print "Aligned Polygon: " + str(aligned_polygon)
	#Now that the polygon is aligned to the minimum step each side will be an integer multiple of steps in X&Y direction
	for index in range(len(aligned_polygon.vertices)):
		#Determine Direction of Movement
		start_point = aligned_polygon.vertices[index-1]
		end_point = aligned_polygon.vertices[index]
		#Create the intermediate points the start and end 
		poly_points.append(start_point)
		in_between_points = get_in_between_points(start_point,end_point,minstep)
		if len(in_between_points) > 0:
			poly_points += in_between_points
	return poly_points
	
def get_polygon_points__worker(polygon,minstep,add_perimiter_points):
	print polygon
	print minstep
	perimiter_points = get_perimiter_points(polygon,minstep)
	print "perimiter_points: " + str(perimiter_points)
	enclosed_points = []
	if add_perimiter_points:
		enclosed_points += perimiter_points
	#So now we have an enclosing set of points for the perimiter, what we need to do is select a point,
	#then find the next point where they exist on the same horizontal or vertcal position. For purposes
	#of this implementation we will use the vertical position. First we pick a point. Then we find all
	#points in the list with that Y value, deleting them from the original list as we go. Points are
	#binned based on if they could cause a internal line start, stop, either, or niether. If a point
	#could cause both, IE start and stop simultainously, it is binned twice.
	needs_analysis = [True]*len(perimiter_points)
	for initial_point_index in range(len(perimiter_points)):
		open_or_close_list = []
		close_list = []
		open_list = []
		if needs_analysis[initial_point_index]:
			y_coord = perimiter_points[initial_point_index].y #Choose the Y coordinate that we are filling
			#Handle the general case for middle elements
			for current_point_index in [x-1 for x in range(len(perimiter_points))]:	#So thirlled about how clever this is, handle the last element in the array first
				curr_point = perimiter_points[current_point_index]
				if curr_point.y == y_coord:#Only get points on this Y coordinate
					prev_point = perimiter_points[current_point_index-1]
					next_point = perimiter_points[current_point_index+1]
					if (prev_point.y > curr_point.y and next_point.y > curr_point.y) or (prev_point.y < curr_point.y and next_point.y < curr_point.y): #Detect Bump/Intersection, add twice
						open_or_close_list.append(curr_point)
						open_or_close_list.append(curr_point)
					elif (prev_point.y > curr_point.y and curr_point.y > next_point.y) or (prev_point.y < curr_point.y and curr_point.y < next_point.y): #Detect crossing, add once
						open_or_close_list.append(curr_point)
					#If we get this far then prev or next have the same Y value as curr. Since it is possible for points to be directly above or below eachother we cannot use the non-equal
					#point to determine if this is an open, close, or niether point. If both prev and next are Y equal, then this is a niether point and is discarded. If the Y-equal point
					#is to the left of the current point, then this is a open point. If the Y-equal point is to the right of the current point, then this is a close point.
					else:
						if next_point.y != curr_point.y :#Determine inequality first, then left/right status
							if prev_point.x < curr_point.x:#The lower X is, the further to the left it is
								open_list.append(curr_point)
							else:
								close_list.append(curr_point)
						elif prev_point.y != curr_point.y :#Determine inequality first, then left/right status
							if next_point.x < curr_point.x:#The lower X is, the further to the left it is
								open_list.append(curr_point)
							else:
								close_list.append(curr_point)
						#If we get to the else case, then all three have equal Y values
					#Regardless of how the point is used remove from remaining points
					needs_analysis[current_point_index] = False
		#So at this point we have three lists, each representing a point that a line starts at, ends at, or either. We need to sort all three by X values, lowest to highest.
		open_or_close_list.sort(cmp=point_sort_x_axis)
		open_list.sort(cmp=point_sort_x_axis)
		close_list.sort(cmp=point_sort_x_axis)
		#Now it's important to remember here that all of these points have the same Y value, so if any have the same X then they are the same point, which CAN happen,
		#but only in the open_or_close_list queue. What we do is determine the start and end of segments. We determine which of these three lists has the lowest X
		#value, pop it, and decide if we are starting a line. If we pop from the close queue, then we do not start a segment, and move on as if nothing is happening.
		#If we pop from either of the others then we pop the next smallest X value, which will constitute a line. We then add all intermediate points to the enclosed points.
		#BUT: What to do if any of these lists are empty? It is certianly possible, especially in small polygons. If that happens then we can safely ignore that list,
		#but how? Without overcomplication preferably.
		while len(close_list) + len(open_or_close_list) + len(open_list) > 1:	#Run until these are empty
			#Determine the current X value of each list
			both_x  = float('inf') if len(open_or_close_list) == 0 else open_or_close_list[0].x
			open_x  = float('inf') if len(open_list) == 0 else open_list[0].x
			close_x = float('inf') if len(close_list) == 0 else close_list[0].x
			#Now determine which is the least, and proceed based on the relavant number
			if close_x < open_x and close_x < both_x:	#If this happens first, then we have found a perimiter line segment and need to remove it
				del close_list[0]
			else:	#If this is the case, then it's time to make a segment!
				if open_x < both_x:#Find the segment start
					start_point = open_list[0]
					del open_list[0]
				else:
					start_point = open_or_close_list[0]
					del open_or_close_list[0]
					both_x  = float('inf') if len(open_or_close_list) == 0 else open_or_close_list[0].x
				#Segment start identified, find the segment end.
				if close_x < both_x:
					end_point = close_list[0]
					del close_list[0]
				else:
					end_point = open_or_close_list[0]
					del open_or_close_list[0]
				#Start and end located! Add the points and move on!
				enclosed_points += get_in_between_points(start_point,end_point,minstep)
	return enclosed_points

def get_enclosed_points(polygon,minstep):
	return get_polygon_points__worker(polygon,minstep,False)
	
def get_polygon_points(polygon,minstep):
	return get_polygon_points__worker(polygon,minstep,True)

#Test The Code
if __name__ == '__main__':
	print "\n\n\t\t** Starting Test **\n\n"
	small_diamond = Polygon(Point(1,0),Point(0,1),Point(1,2),Point(2,1))
	print "\n\t\t** small_diamond **\n\n"
	print "Polygon: " + str(small_diamond)
	print "Polygon Verticies: " + str(small_diamond.vertices)
	print "Polygon Sides: " + str(small_diamond.sides)
	print "Perimiter Points: " + str(get_perimiter_points(small_diamond,.1))
	print "Enclosed Points: " + str(get_enclosed_points(small_diamond,.1))
	print "All Points: " + str(get_polygon_points(small_diamond,.1))
	
	open_down = Polygon(Point(1,2),Point(1,4),Point(4,4),Point(4,1),Point(3,1),Point(3,3),Point(2,3),Point(2,2))
	print "\n\t\t** open_down **\n\n"
	print "Polygon: " + str(open_down)
	print "Polygon Verticies: " + str(open_down.vertices)
	print "Polygon Sides: " + str(open_down.sides)
	print "Perimiter Points: " + str(get_perimiter_points(open_down,.5))
	print "Enclosed Points: " + str(get_enclosed_points(open_down,.5))
	print "All Points: " + str(get_polygon_points(open_down,.5))

