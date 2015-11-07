#!/usr/bin/env python

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

#Race Enumeration
class Enum(set):
	def __getattr__(self, name):
		if name in self:
			return name
		raise AttributeError

Race = Enum(["human-white", "human-black", "human-asian", "human-native", "human-hispanic", "human-hispanic", "elf", "dwarf", "halforc", "gnome", "halfling"])
	
#Generate Household Size Function
def gen_household_size():
	#Equation for household population: highest of 3d7-7 and 1
	household_die_size = 7
	household_die_count = 3
	household_size_modifier = -7
	household_size_minimum = 1
	to_return = household_size_modifier
	for die_count in range(0,household_die_count):
		to_return += random.randrange(1,household_die_size+1)
	#Set minimum household size
	if to_return < household_size_minimum:
		to_return = 1
	return to_return

	
#Function to generate an array of Characters, representing a household/family
# Each family has a root character, which defines a lot about the family.
# The root character is not based on any previous characters.
# The next character is a 'complimentary' character, usually representing a spouse. This caracters attributs will be indipendent of the root characters, but
#		race, gender, age, and orientation bay be related to the root character. NOTE: This character may not be added to the final household, representing a disceased character
# All subsequent characters are derived from the first two characters. These are usually children, but may be grandparents or siblings of the first two characters

#Character Generation Parameters
def probablyGay(race):
	percent_ssa_elves = 0.5
	percent_ssa_others = 0.034
	to_return = percent_ssa_others
	if race == Race.elf:
		to_return = percent_ssa_elves
	return to_return

def isGay(race):
	return 1 if random.randrange(0,1000+1) < 1000*probablyGay(race) else 0
	
def genRootCharacter(race,lastname):
	#Determine character orientation
	if isGay(race) == 0:
		orientation = "Straight"
		gender = "Male" if random.randrange(0,100+1) != 0 else "Female" #Male/Female select
	else:
		orientation = "Gay"
		gender = "Male" if random.randrange(0,1+1) != 0 else "Female" #Male/Female select
	return Character(race,gender,orientation)

#TODO
def genComplimentCharacter(rootCharacter):
	return Character(rootCharacter.race,rootCharacter.gender,rootCharacter.orientation)

#TODO
def genDerivedCharacter(rootCharacter,complimentCharacter):
	return Character(rootCharacter.race,rootCharacter.gender,rootCharacter.orientation)

def genfamily(familySize,primaryrace,lastname):
	#Population Parameters
	percent_1_parent_homes = 0.10
	percent_no_parent_homes = 0.03
	family = []
	rootCharacter = genRootCharacter(primaryrace,lastname)
	if(familySize == 1):
		family.append(rootCharacter)
	else:	#More than one character in this family, full generation proceeds
		complimentCharacter = genComplimentCharacter(rootCharacter)
		#Now determine how many of the important characters are still around
		home_type = random.randrange(1,100+1)
		if home_type > 100*(percent_1_parent_homes+percent_no_parent_homes):	#Two Provider Home
			family.append(rootCharacter)
			family.append(complimentCharacter)
		elif home_type > 100*(percent_no_parent_homes):	#One Provider Home
			family.append(complimentCharacter)
	#Add characters until family is full
	while(len(family) < familySize):
		newCharacter = genDerivedCharacter(rootCharacter,complimentCharacter)
		family.append(newCharacter)
	return family
		
#Class for households
class Household:

	def __init__(self,primaryrace,lastname):
		self.primaryrace = primaryrace
		self.lastname = lastname
		self.homesize = -1
		self.residents = genfamily(gen_household_size(),primaryrace,lastname)
	
class Character:
	def __init__(self,race,gender,orientation):
		self.race = race
		self.gender = gender
		self.orientation = orientation
		self.firstname = "FirstName"
		self.lastname = "LastName"
		self.age = -1
		self.employment = "This will be a Employment Class"
		
class Employment:
	def __init__(self):
		self.jobtitle = "Persons Title"
		self.employer = "This will be a Character Class"
		self.employees = "This will be an array of Character Classes"


if __name__ == '__main__':
	##### Step 1 Household Size and last Name
	## Set Target population
	target_population = 6000
	#Generate households until target is reached.
	households = []
	current_total_population = 0
	while(current_total_population < target_population):
		primaryrace = "Need code to select race here. Function of proportions. Don't forget that there is a Race enumerated type above!"
		lastname = "Need code to select last name here. Function of Primary Race."
		new_household = Household(primaryrace,lastname)
		households.append(new_household)
		current_total_population += len(new_household.residents)
	
		
	print "Total Population: " + str(current_total_population)


##############################################################
#### Legacy code to be eliminated as it is replaced
##############################################################

print "Generating Households"

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
	#All Population Parameters
	percent_ssa_adults = 0.034
	percent_1_parent_homes = 0.10
	percent_no_parent_homes = 0.03
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

def old_gen():
	#Generate Households function
	def gen_household( home_size_min, home_size_max):
		household_size = gen_household_size()
		home_size = random.randrange(home_size_min,home_size_max+1)
		sentence = "Household Size:\t" + str(household_size) + "\tHome Size:\t" + str(home_size) + gen_persons(household_size)
		#print(sentence)
		return household_size
	
	#Generate Households function
	def gen_households(target_total_population, home_size_min, home_size_max):
		total_population = 0
		while target_total_population > total_population:
			total_population += gen_household( home_size_min, home_size_max)
		return total_population
	
	#Generate Poor Homes
	poor_population = gen_households(target_poor_households_ratio*target_population,poor_home_size_min,poor_home_size_max)
	print "Legacy Total Poor Population: " + str(poor_population)
	
	#Generate Middle Homes
	middle_population = gen_households(target_middle_households_ratio*target_population,middle_home_size_min,middle_home_size_max)
	print "Legacy Total Middle Population: " + str(middle_population)
	
	
	#Generate Rich Homes
	rich_population = gen_households(target_rich_households_ratio*target_population,rich_home_size_min,rich_home_size_max)
	print "Legacy Total Rich Population: " + str(rich_population)
	print "Legacy Total Population: " + str(rich_population+middle_population+poor_population)

