#!/usr/bin/env python

#Code for Doing Math on Polygons

import sympy
from sympy import *
import math
import copy

# Base Map Shapes
class BasicPoint(object):
	def __init__(self,x,y):
		self.type = self.__class__.__name__
		self.x = x
		self.y = y
		
	def get_point(self):
		return Point(self.x,self.y)
		
	def to_string(self):
		return "Point: [" + str(self.x) + "," + str(self.y) + "]\n"

def basicpoint_decoder(dictonary):
	if 'type' in dictonary and dictonary['type'] == 'BasicPoint':
		to_return = BasicPoint(dictonary['x'], dictonary['y'])
		return to_return
	return dictonary

class BasicPolygon(object):
	def __init__(self,vertices=None,basex=None,basey=None):
		if basex == None: basex = 0
		if basey == None: basey = 0
		if vertices == None: vertices = []
		self.type = self.__class__.__name__
		self.vertices = vertices
		self.basex = basex
		self.basey = basey
		
	def is_walkable(self):
		return False

	def get_polygon(self,minstep):
		vertices = []
		for point in self.vertices:
			vertices.append(point.get_point())
		return Polygon(*vertices)

def basicpolygon_decoder(dictonary):
	if 'type' in dictonary and dictonary['type'] == 'BasicPolygon':
		to_return = BasicPolygon(None,dictonary['basex'], dictonary['basey'])
		for point in dictonary['vertices']:
			to_return.vertices.append(basicpoint_decoder(point))
		return to_return
	return dictonary

class BasicCircle(object):
	def __init__(self,radius,basex=None,basey=None):
		if basex == None: basex = 0
		if basey == None: basey = 0
		self.type = self.__class__.__name__
		self.radius = radius
		self.basex = basex
		self.basey = basey
		
	def is_walkable(self):
		return False

	def get_polygon(self,minstep):
		points = []
		#Use law of the cosines to determine the angle of each step change
		angle_change = math.acos((math.pow(minstep,2)-2*math.pow(self.radius,2))/(-1*2*math.pow(self.radius,2)))
		current_angle = math.asin((self.radius*math.sin(angle_change))/minstep)
		#Generate the points on the circle
		cur_point = Point(self.basex - self.radius,self.basey)
		for count in range(int(2*math.pi/angle_change)):
			points.append(cur_point)
			cur_point = Point(cur_point.x + minstep*math.cos(current_angle),cur_point.y + minstep*math.sin(current_angle))
			current_angle -= angle_change
		return Polygon(*points)

def basiccircle_decoder(dictonary):
	if 'type' in dictonary and dictonary['type'] == 'BasicCircle':
		to_return = BasicCircle(dictonary['radius'],dictonary['basex'], dictonary['basey'])
		return to_return
	return dictonary

#Custom rounding function
def custom_round(value,portion):
	mod = math.fmod(value,portion)
	if mod > portion/2:
		value += portion -mod
	else:
		value -= mod
	return value

class GraphSegment(object):
	def __init__(self,node1,node2):
		self.type = self.__class__.__name__
		self.node1 = node1
		self.node2 = node2
		self.segment = Segment(self.node1.get_point(),self.node2.get_point())
		
	def get_opposite_node(self,node):
		if node != self.node1 and node != self.node2:
			print "Error provided node is not on the end of this segment! Returning None"
			return None
		elif node != self.node1:
			return node1
		return node2
		
	def intersection(self,graphsegment):
		return self.segment.intersection(graphsegment.segment)
		
	def contains(self,sympy_object):
		return self.segment.contains(sympy_object)

	def remove_connection(self):
		self.node1.remove_connection(self.node2)

	def to_string(self):
		return_string = "Node1: " + self.node1.to_string()
		return_string += "Node2: " + self.node2.to_string()
		return return_string

class GraphNode(BasicPoint):
	def __init__(self,point,connections=None):
		super(GraphNode,self).__init__(point.x,point.y)
		self.type = self.__class__.__name__
		if connections == None:
			self.connections = []
		else:
			self.connections = connections
		self.coverage = 0
	
	def to_string(self):
		return_string = super(GraphNode,self).to_string()
		return_string += "\tCoverage: " + str(self.coverage) + "\n"
		if len(self.connections) > 0:
			return_string += "\tConnects to:\n"
			for node in self.connections:
				return_string += "\t\t" + super(GraphNode,node).to_string() #Just get the point information
		else:
			return_string += "\tNo Connections\n"
		return return_string
		
	def is_connected_to(self,node):
		if node in self.connections:
			return True
		return False
		
	def add_connection(self,*nodes):
		for node in nodes:
			if node != self:
				if not self.is_connected_to(node):
					self.connections.append(node)
				if not node.is_connected_to(self):
					node.connections.append(self)

	def remove_connection(self,*nodes):
		for node in nodes:
			if self.is_connected_to(node):
				del self.connections[self.connections.index(node)]
			if node.is_connected_to(self):
				del node.connections[node.connections.index(self)]
		
	def get_graph_segments__worker(self,include_covered):
		segments = []
		for node in self.connections:
			if ((not node.is_covered()) or include_covered) and node.is_valid():
				segments.append(GraphSegment(self,node))
		return segments

	def get_graphsegments(self):
		return self.get_graph_segments__worker(True)

	def get_uncovered_graphsegments(self):
		return self.get_graph_segments__worker(False)

	def is_covered(self,level=0):
		if self.coverage > level:
			return True
		return False
		
	def is_valid(self):
		return self.is_covered(-1)

	def is_point(self,point):
		if self.x == point.x and self.y == point.y:
			return True
		return False

	def subsume_node(self,node):
		if self != node: #Cannot subsume self
			#Consume all connections
			while(len(node.connections) > 0):
				#Make sure that all connections are moved to the current from the old one
				self.add_connection(node.connections[0])
				node.remove_connection(node.connections[0])
			#Then indicate that the old node is no longer valid
			node.coverage = -1
	
	def invalidate(self):
		#Remove all connections
		while(len(self.connections) > 0):
			self.remove_connection(self.connections[0])
		#Then indicate that this node is no longer valid
		self.coverage = -1
	
	
	def get_distance(self,node):
		return math.sqrt(math.pow(self.x - node.x,2) + math.pow(self.y - node.y,2))

	def add_closer_node(self,*nodes):
		if len(nodes) > 0:
			closest_node = nodes[0]
			distance = self.get_distance(closest_node)
			for node in nodes:
				comp_distance = self.get_distance(node)
				if comp_distance < distance:
					distance = comp_distance
					closest_node = node
			self.add_connection(closest_node)
		
#In this method we accept a single polygon and convert it to a polygon graph
def convert_polygon_graph(polygon):
	constructed_graph = []
	previous_node = None
	for point in polygon.vertices:
		new_node = GraphNode(point)
		if previous_node != None:
			new_node.add_connection(previous_node)
		constructed_graph.append(new_node)
		previous_node = new_node
	#Connect the last node
	if len(constructed_graph) > 1:
		constructed_graph[0].add_connection(previous_node)
	return constructed_graph

#In this method we accept an array of polygons and convert it to an array of polygon graphs
def convert_polygon_graphs(poly_list):
	graph_set = []
	for polygon in poly_list:
		graph_set += convert_polygon_graph(polygon)
	return graph_set

def uncover_graph(graph,level=0):
	for node in graph:
		if node.coverage >= level:
			node.coverage = 0

def find_matching_node(point,node_list):
	return node_list[0]
			
def merge_graph_segments(graphseg0,graphseg1):
	#Getting their intersetion will indicate what to do next, based on type and value.
	intersection_arr = graphseg0.intersection(graphseg1)
	new_nodes = []
	#If there are no intersections then no processing is needed here. Otherwise continue.
	if len(intersection_arr) > 0:
		#Step 1: Get intersection type and delete existing connections
		intersection = intersection_arr[0]	#Clever thing this. Basically when you intersect two line segments you will either get a new segment or a single point. Either way there is only one element in this array.
		graphseg0.node1.remove_connection(graphseg0.node2)
		graphseg1.node1.remove_connection(graphseg1.node2)
		if isinstance(intersection,Segment):
			#Sweet! Overlapping lines, not a booring old point intersection.
			#Here's some important notes:
			#	1) There are several cases that need to be handeled here, but generally speaking we need to determine how many nodes will be subsumed as a result of this event.
			# 2) Each end of the returned segment has a point that matches at least one and possibly two points in the original two segments
			#	3) We do not need new nodes for this merge step
						
			#Step 2: Determine overlap type: overlap or subsumption
			if graphseg0.segment == intersection:	#Graphseg0 is internal intersection
				graphseg0.node1.add_connection(graphseg0.node2)
				graphseg1.node1.add_closer_node(graphseg0.node1,graphseg0.node2)
				graphseg1.node2.add_closer_node(graphseg0.node1,graphseg0.node2)
			elif graphseg1.segment == intersection:	#Graphseg1 is internal intersection
				graphseg1.node1.add_connection(graphseg1.node2)
				graphseg0.node1.add_closer_node(graphseg1.node1,graphseg1.node2)
				graphseg0.node2.add_closer_node(graphseg1.node1,graphseg1.node2)
			else: #Partial overlap condition
				#In this scenario one point from each original segment bisects the other segment.
				#These bisecting nodes connect to both of the nodes in the segment bisected, and to eachother, completing the intersection
				if graphseg0.node1.is_point(intersection.p1):
					subsegnode1 = graphseg0.node1
					subsegnode1.add_connection(graphseg1.node1,graphseg1.node2)
				elif graphseg0.node2.is_point(intersection.p1):	
					subsegnode1 = graphseg0.node2
					subsegnode1.add_connection(graphseg1.node1,graphseg1.node2)
				elif graphseg1.node1.is_point(intersection.p1):
					subsegnode1 = graphseg1.node1
					subsegnode1.add_connection(graphseg0.node1,graphseg0.node2)
				else:
					subsegnode1 = graphseg1.node2
					subsegnode1.add_connection(graphseg0.node1,graphseg0.node2)

				if graphseg0.node1.is_point(intersection.p2):
					subsegnode2 = graphseg0.node1
					subsegnode2.add_connection(graphseg1.node1,graphseg1.node2)
				elif graphseg0.node2.is_point(intersection.p2):	
					subsegnode2 = graphseg0.node2
					subsegnode2.add_connection(graphseg1.node1,graphseg1.node2)
				elif graphseg1.node1.is_point(intersection.p2):
					subsegnode2 = graphseg1.node1
					subsegnode2.add_connection(graphseg0.node1,graphseg0.node2)
				else:
					subsegnode2 = graphseg1.node2
					subsegnode2.add_connection(graphseg0.node1,graphseg0.node2)
				#Now that we have the subsegment nodes, connect them
				subsegnode1.add_connection(subsegnode2)
				#And that's it, we handle segments. Redundent/overlapping point merging happens at the end
		else:#This would have to be a Point, if not I'm sure there will be lots of errors
			#Next thing to do here is get either the end point at which the intersection happens, or create a new node
			if graphseg0.node1.is_point(intersection):
				intersection_node = graphseg0.node1
			elif graphseg0.node2.is_point(intersection):
				intersection_node = graphseg0.node2
			elif graphseg1.node1.is_point(intersection):
				intersection_node = graphseg1.node1
			elif graphseg1.node2.is_point(intersection):
				intersection_node = graphseg1.node2
			else:#If this happens we truely need a new node. Create it and add it to the return list
				intersection_node = GraphNode(intersection)
				new_nodes.append(intersection_node)
			#Now that we have the intersection node, everything connects to it
			intersection_node.add_connection(graphseg0.node1,graphseg0.node2,graphseg1.node1,graphseg1.node2)
		#Very last step in all of this is to merge any redundent nodes.
		#Awesome note: We should be able to ignore the intersection node here because we only create it if it is not redundent
		nodes = [graphseg0.node1,graphseg0.node2,graphseg1.node1,graphseg1.node2]
		points = [graphseg0.node1.get_point(),graphseg0.node2.get_point(),graphseg1.node1.get_point(),graphseg1.node2.get_point()]
		for current_point_index in range(len(points)):
			current_point = points[current_point_index]
			current_node = nodes[current_point_index]
			if current_node.is_valid():
				for compare_point_index in range(len(points) - (current_point_index +1)):
					compare_point = points[compare_point_index + (current_point_index +1)]
					current_node = nodes[current_point_index]
					compare_node = nodes[compare_point_index + (current_point_index +1)]
					if current_point == compare_point and current_node != compare_node:
						current_node.subsume_node(compare_node)
	return new_nodes

def remove_invalid_graph_nodes(graph):
	new_graph = []
	for node in graph:
		if node.is_valid():
			new_graph.append(node)
	return new_graph
	
def remove_disconnected_graph_nodes(graph):
	new_graph = []
	for node in graph:
		if len(node.connections) > 0:
			new_graph.append(node)
	return new_graph
	
	#In this method we create a graph of the polygons provided. The Graph is an array of GraphNodes, which is a point and a list of other graph nodes
def construct_polygon_graph(poly_list):
	#This gets us an array of graph nodes that may or may not have overlapping edges
	#The goal is to detect those overlapping edges and add nodes and connections as needed
	constructed_graph = convert_polygon_graphs(poly_list)
	intersection_nodes = []
	for current_node in constructed_graph:		#This is the current node we are attempting to cover
		if current_node.is_valid():
			current_node.coverage = 1	#Cover the current node
			current_edges = current_node.get_uncovered_graphsegments()
			for comparison_node in constructed_graph:
				#There are two reasons not to compare a node in the graph
				# 1) The comparison and current nodes are the same
				# 2) The comparison node is already covered
				# Note: The only reason we compare nodes that are connected to the current node is because it is possible that the current node is on one of the comparison nodes segments
				if comparison_node != current_node and not comparison_node.is_covered():
					#Now we know that the current node is ready to be reviewed. Lets cover it and then start reviewing its edges.
					comparison_node.coverage = 2
					comparison_edges = comparison_node.get_uncovered_graphsegments()
					#Comparison time. We have two lists of segments that we know do not connect the current and comparison nodes
					#Check each for intersections
					for current_edge in current_edges:
						for comparison_edge in comparison_edges:
							#Now we have two edges/segments. Run Merge and record new nodes
							intersection_nodes += merge_graph_segments(current_edge,comparison_edge)
			#Now that all nodes have been compared against the edges of the current node, uncover those that we covered for comparison purposes only
			uncover_graph(constructed_graph,2)
	#Add the new intersections to the graph
	constructed_graph += intersection_nodes
	uncover_graph(constructed_graph)
	return remove_invalid_graph_nodes(constructed_graph)

def graph_to_string(graph):
	return_string = "Graph Size: " + str(len(graph)) + " nodes.\n"
	for node_index in range(len(graph)):
		return_string += "Node Index: " + str(node_index) + "\n" + graph[node_index].to_string() + "\n"
	return return_string
	
def graph_to_polygons(graph):
	poly_list = []
	for node in graph:
		if not node.is_covered():
			#Setup to start iterating over points
			previous_node = node
			previous_node.coverage = 1	#Cover the starting node
			current_node = previous_node.connections[0]
			current_polygon = [previous_node.get_point()]
			#Iterate around the polygon, adding points as we go around
			while not current_node.is_covered():
				#Add this node to the polygon and cover it
				current_polygon.append(current_node.get_point())
				current_node.coverage = 1
				#Determine the next node
				if previous_node != current_node.connections[0]:
					previous_node = current_node
					current_node = current_node.connections[0]
				else:
					previous_node = current_node
					current_node = current_node.connections[1]
			#Add the collected points to the list of polygons captured
			poly_list.append(Polygon(*current_polygon))
	return poly_list
	
def polygon_union(poly_list):
	#Construct the Graph
	graph = construct_polygon_graph(poly_list)
	#Now that we have the graph, start removing nodes that are inside of a polygon but not on the edges
	for node in graph:
		point = node.get_point()
		for polygon in poly_list:
			if polygon.encloses_point(point):
				node.invalidate()
				break
	#Then remove segments that interset a polygon at two points or are on the edge of multiple polygons
	for node in graph:		#This is the current node we are attempting to cover
		if node.is_valid():
			node.coverage = 1	#Cover the current node
			edges = node.get_uncovered_graphsegments()
			for edge in edges:
				polygon_edge_found = False
				for polygon in poly_list:
					intersections = polygon.intersection(edge.segment)
					if len(intersections) > 0:	#If an intersection is found
						is_segment = False
						for intersection in intersections:
							if type(intersection) is Segment:
								is_segment = True
						#Determine the type of intersection
						if is_segment: #Segments
							if polygon_edge_found:
								edge.remove_connection()
								break
							polygon_edge_found = True
						elif len(intersections) > 1: 	# This means that this segment intersects this polygon at Multiple Points, and is therefore an interior edge.
							edge.remove_connection()		# A single point would indicate that the segment is between the edge of this polygon and a point on the edge
							break												# of a different polygon
	uncover_graph(graph)
	#Then remove any nodes that have no connections
	graph = remove_disconnected_graph_nodes(graph)
	#Convert the resulting graph into an array of polygons and return
	return graph_to_polygons(graph)

def polygon_intersection(poly_list):
	#Construct the Graph
	graph = construct_polygon_graph(poly_list)
	#Now that we have the graph, start removing all nodes that are not interior or edge of every polygon. Step through each operation to save on computational power
	for node in graph:
		point = node.get_point()
		for polygon in poly_list:
			if not polygon.encloses_point(point):
				intersections = polygon.intersection(point)
				if len(intersections) == 0:
					node.invalidate()
					break
	#Then remove any nodes that have no connections
	graph = remove_disconnected_graph_nodes(graph)
	#Convert the resulting graph into an array of polygons and return
	return graph_to_polygons(graph)

def polygon_compliment(minuend_poly_list, subtrahend_poly_list):
	#Create super array for use later
	all_polygons = minuend_poly_list + subtrahend_poly_list
	#Construct the Graph
	graph = construct_polygon_graph(all_polygons)
	#Now remove any points on the interior of any set, so that all points are on the edge of at least one polygon
	for node in graph:
		point = node.get_point()
		for polygon in all_polygons:
			if polygon.encloses_point(point):
				node.invalidate()
				break
	
	#Then remove any nodes that have no connections
	graph = remove_disconnected_graph_nodes(graph)
	#Convert the resulting graph into an array of polygons and return
	return graph_to_polygons(graph)

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
	print "Perimiter Points: " + str(get_perimiter_points(small_diamond,1))
	print "Enclosed Points: " + str(get_enclosed_points(small_diamond,1))
	print "All Points: " + str(get_polygon_points(small_diamond,1))
	
	open_down = Polygon(Point(1,2),Point(1,4),Point(4,4),Point(4,1),Point(3,1),Point(3,3),Point(2,3),Point(2,2))
	print "\n\t\t** open_down **\n\n"
	print "Polygon: " + str(open_down)
	print "Polygon Verticies: " + str(open_down.vertices)
	print "Polygon Sides: " + str(open_down.sides)
	print "Perimiter Points: " + str(get_perimiter_points(open_down,.5))
	print "Enclosed Points: " + str(get_enclosed_points(open_down,.5))
	print "All Points: " + str(get_polygon_points(open_down,.5))

	print "\n\t\t** Graph Intersection **\n\n"
	p1 = Point(-1,0)
	p2 = Point(1,0)
	p3 = Point(0,-1)
	p4 = Point(0,1)
	n1 = GraphNode(p1)
	n2 = GraphNode(p2)
	n3 = GraphNode(p3)
	n4 = GraphNode(p4)
	gseg_horiz = GraphSegment(n1,n2)
	gseg_vert = GraphSegment(n3,n4)
	intersections = gseg_horiz.intersection(gseg_vert)
	print "[0,0] intersection: " + str(intersections)
	print "\n\t\t** Should have no connections here **\n\n"
	print "n1:\n" + n1.to_string()
	print "n2:\n" + n2.to_string()
	print "n3:\n" + n3.to_string()
	print "n4:\n" + n4.to_string()
	print "\n\t\t** Connect n1 to n2 **\n\n"
	n1.add_connection(n2)
	print "n1:\n" + n1.to_string()
	print "n2:\n" + n2.to_string()
	print "\n\t\t** Connect n3 to n4 **\n\n"
	n3.add_connection(n4)
	print "n3:\n" + n3.to_string()
	print "n4:\n" + n4.to_string()
	print "\n\t\t** Now Merge them all into a graph and print the result **\n\n"
	graph = [n1,n2,n3,n4]
	graph += merge_graph_segments(gseg_horiz,gseg_vert)
	print graph_to_string(graph)
	print "\n\t\t** Create a single polygon graph and print it **\n\n"
	
	outer_square = Polygon((0,0),(0,10),(10,10),(10,0))
	current_graph = construct_polygon_graph([outer_square])
	current_graph = construct_polygon_graph([outer_square])
	print graph_to_string(current_graph)
	
	print "\n\t\t** Put a smaller square into the prevous square and print that **\n\n"
	inner_square0 = Polygon((1,1),(1,5),(5,5),(5,1))
	current_graph = construct_polygon_graph([outer_square,inner_square0])
	print graph_to_string(current_graph)
	
	print "\n\t\t** Add another small intersecting square into the prevous square and print that **\n\n"
	inner_square1 = Polygon((4,4),(4,9),(9,9),(9,4))
	current_graph = construct_polygon_graph([outer_square,inner_square0,inner_square1])
	print graph_to_string(current_graph)
	
	print "\n\t\t** Merge all three polygons and print the resulting union, should be square **\n\n"
	polygons = polygon_union([outer_square,inner_square0,inner_square1])
	for polygon in polygons:
		print polygon
	
	print "\n\t\t** Merge inner polygons and print the resulting union, should be two overlapping squares **\n\n"
	polygons = polygon_union([inner_square0,inner_square1])
	for polygon in polygons:
		print polygon
	
	print "\n\t\t** The intersection of all three polygons should be a small square in the center **\n\n"
	polygons = polygon_intersection([outer_square,inner_square0,inner_square1])
	for polygon in polygons:
		print polygon
	