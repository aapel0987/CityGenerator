#Code for Generating basic city information
# Goals:
#		Characters can be moved to NPC Sheets in Roll20
#			Have Names
#			Have Gender
#			Have a Race
#			Have Attributes
#			Have Equipment
#		Characters have 2 places to be during a day
#			At Home-Household
#			At a job-Employment
#		Characters have some basic interesting attributes
#			Sexual Orientation
#			Marital Status-Determined by other members of household
#
#		Planned Information flow
#		1. Determine Household Population Size & Lastname
#		2. Generate people
#				Age, Gender, Orientation->Attributes
#		3. Based on the Age, Gender, Orientation, and Attributes, assign jobs
#		4. Based on assigned job, determine household income bracket and size/area
#			
import random

#Class for households
class Household:
	def __init__(self):
		self.lastname = "Last Name"
		self.homesize = -1
		self.residents = []
	
class Character:
	def __init__(self):
		self.gender = "Male or Female, may have Trans prefix."
		self.firstname = "FirstName"
		self.lastname = "LastName"
		self.age = -1
		self.employment = "This will be a Employment Class"
		
class Employment:
	def __init__(self):
		self.jobtitle = "Persons Title"
		self.employer = "This will be a Character Class"
		self.employees = "This will be an array of Character Classes"














##############################################################
#### Legacy code to be eliminated as it is replaced
##############################################################

print "Generating Households"
#All Population Parameters
total_population = 6000
percent_ssa_adults = 0.034
percent_1_parent_homes = 0.10
percent_no_parent_homes = 0.03
#Equation for household population: highest of 3d7-7 and 1
household_die_size = 7
household_die_count = 3
household_size_modifier = -7
household_size_minimum = 1
#Rich Households Parameters
target_rich_households_ratio = 300.0/6000.0
rich_home_size_min = 34
rich_home_size_max = 140
#Middle Households Parameters
target_middle_households_ratio = 1700.0/6000.0
middle_home_size_min = 34
middle_home_size_max = 50
#Poor Households Parameters
target_poor_households_ratio = 4000.0/6000.0
poor_home_size_min = 0
poor_home_size_max = 50

#Age range functions
def gen_adult_age():
	return random.randrange(18,40+1)
	
def gen_elderly_age():
	return random.randrange(41,100+1)
	
def gen_teenager_age():
	return random.randrange(12,17+1)
	
def gen_youth_age():
	return random.randrange(0,11+1)

#Person Generation Functions
def gen_provider(gender,orientation):
	return str(gender) + ", " + str(orientation)

def gen_special_provider(gender,orientation):
	return str(gender) + ", " + str(orientation)

def gen_partner(gender,orientation):
	return str(gender) + ", " + str(orientation)

def gen_dependent_provider():
	#All dependants are straight, usually due to age
	orientation = "Straight"
	gender = "Male" if random.randrange(0,1+1) != 0 else "Female" #Male/Female select
	return gen_special_provider(gender,orientation)

def gen_dependent():
	return "gen_dependent"

#Generate Persons Function, use to determine gender, orientation, age, occupation, name
def gen_persons(household_size):
	home_type = random.randrange(1,100+1)
	to_return = ""
	#Two adult provider homes, very typical
	if home_type > 100*(percent_1_parent_homes+percent_no_parent_homes):	#Two Provider Home
		#Determine provider orientation
		if random.randrange(0,1000+1) > 1000*percent_ssa_adults:
			orientation = "Straight"
			gender = "Male" if random.randrange(0,100+1) != 0 else "Female" #Male/Female select
		else:
			orientation = "Gay"
			gender = "Male" if random.randrange(0,1+1) != 0 else "Female" #Male/Female select
		to_return += "\t" + gen_provider(gender,orientation)
		household_size -= 1
		#If there is more than one member, generate a partner
		if household_size > 0:
			#If the first partner is straight re-determine orientation invert gender
			if orientation == "Straight":
				orientation = "Straight" if random.randrange(0,1000+1) > 1000*percent_ssa_adults else "Gay"
				gender = "Male" if gender == "Female" else "Female"
			to_return += "\t" + gen_partner(gender,orientation)
			household_size -= 1
	# One adult provider homes, less typical
	elif home_type > 100*(percent_no_parent_homes):	#One Provider Home
		#Determine provider orientation
		if random.randrange(0,1000+1) > 1000*percent_ssa_adults:
			orientation = "Straight"
			gender = "Male" if random.randrange(0,10+1) == 0 else "Female" #Male/Female select, most are widowers
		else:
			orientation = "Gay"
			gender = "Male" if random.randrange(0,1+1) != 0 else "Female" #Male/Female select
		to_return += "\t" + gen_provider(gender,orientation)
		household_size -= 1
	# No adult provider homes, very special cases, always straight
	else:
		orientation = "Straight"
		gender = "Male" if random.randrange(0,100+1) != 0 else "Female" #Male/Female select
		to_return += "\t" + gen_special_provider(gender,orientation)
		household_size -= 1
	
	while household_size > 0:
		if random.randrange(0,100+1) == 0:
			to_return += "\t" + gen_dependent_provider()
		else:
			to_return += "\t" + gen_dependent()
		household_size -= 1
	
	return to_return

#Generate Household Size Function
def gen_household_size():
	to_return = household_size_modifier
	for die_count in range(0,household_die_count):
		to_return += random.randrange(1,household_die_size+1)
	#Set minimum household size
	if to_return < household_size_minimum:
		to_return = 1
	return to_return

#Generate Households function
def gen_household( home_size_min, home_size_max):
	household_size = gen_household_size()
	home_size = random.randrange(home_size_min,home_size_max+1)
	sentence = "Household Size:\t" + str(household_size) + "\tHome Size:\t" + str(home_size) + gen_persons(household_size)
	print(sentence)
	return household_size

#Generate Households function
def gen_households(target_population, home_size_min, home_size_max):
	total_population = 0
	while target_population > total_population:
		total_population += gen_household( home_size_min, home_size_max)
	return total_population

#Generate Poor Homes
poor_population = gen_households(target_poor_households_ratio*total_population,poor_home_size_min,poor_home_size_max)
print "Total Poor Population: " + str(poor_population)

#Generate Middle Homes
middle_population = gen_households(target_middle_households_ratio*total_population,middle_home_size_min,middle_home_size_max)
print "Total Middle Population: " + str(middle_population)


#Generate Rich Homes
rich_population = gen_households(target_rich_households_ratio*total_population,rich_home_size_min,rich_home_size_max)
print "Total Rich Population: " + str(rich_population)
print "Total Population: " + str(rich_population+middle_population+poor_population)

