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

Race = Enum(["human_white", "human_black", "human_asian", "human_native", "human_mixed", "human_hispanic", "elf", "dwarf", "halforc", "gnome", "halfling"])
Orientation = Enum(["straight", "gay"])
Gender = Enum(["male", "female"])
	

def resolve_distribution(distribution):
	#Here we assume that we were passed a set of paired (race, probability) objects. Create a cumulative distribution function
	cdfs = []
	totalprobability = 0
	for race,probability in distribution:
		totalprobability += probability
		cdfs.append( (race,totalprobability) )
	#Now that we have a distribution, create a random number and find it in the distribution
	max = cdfs[-1][1]
	rand = random.random()*max
	for race,probability in cdfs:
		if probability>rand:
			return race
	return None
	
#Determine Household Primary Race
def gen_primary_race():
	#	This will return a human racial subtype. Probabilities are taken from American Demographics(Wikipedia)
	racial_pairs = (
		(Race.human_mixed	, 62.0	),
		(Race.elf					, 10.0	),
		(Race.dwarf				, 15.0	),
		(Race.halforc			, 3.0		),
		(Race.gnome				, 5.0		),
		(Race.halfling		, 5.0		),
	)
	race = resolve_distribution(racial_pairs)
	#If a human is rolled reroll to determine what subrace
	if race == Race.human_mixed:
		race = gen_human_race()

	return race

def gen_human_race():
	#	This will return a human racial subtype. Probabilities are taken from American Demographics(Wikipedia)
	racial_pairs = (
		(Race.human_white			, 63.7	),	#	Non-Hispanic White	63.7 %
		(Race.human_black			, 12.2	),	#	Non-Hispanic Black or African American	12.2 %
		(Race.human_asian			, 4.7		),	#	Non-Hispanic Asian	4.7 %
		(Race.human_native		, 0.7		),	#	Non-Hispanic American Indian or Alaska Native	0.7 %
		(Race.human_native		, 0.2		),	#	Non-Hispanic Native Hawaiian or other Pacific Islander	0.2 %
		(Race.human_mixed			, 0.2		),	#	Non-Hispanic some other race	0.2 %
		(Race.human_mixed			, 1.9		),	#	Non-Hispanic two or more races	1.9 %
		(Race.human_hispanic	, 16.4	)		#	Hispanic or Latino	16.4 %
	)
	
	return resolve_distribution(racial_pairs)	#This will supposidly get me a random element. Not sure if I am a believer.

#Name Retrevial Code
def get_human_last_name(race):	
	return "Add Last Name Code"

def get_elf_last_name(race):
	return "Add Last Name Code"

def get_dwarf_last_name(race):
	return "Add Last Name Code"

def get_halforc_last_name(race):
	return ""	#This is correct, half orcs don't get last names

def get_gnome_last_name(race):
	return "Add Last Name Code"

def get_halfling_last_name(race):
	return "Add Last Name Code"

#TODO: Subfunctions for each race
def get_last_name(race):
	namelookup = {
		Race.human_white		: get_human_last_name   ,
		Race.human_black		: get_human_last_name   ,
		Race.human_asian		: get_human_last_name   ,
		Race.human_native		: get_human_last_name   ,
		Race.human_native		: get_human_last_name   ,
		Race.human_mixed		: get_human_last_name   ,
		Race.human_hispanic	: get_human_last_name   ,
		Race.elf						: get_elf_last_name     ,
		Race.dwarf					: get_dwarf_last_name   ,
		Race.halforc				: get_halforc_last_name ,
		Race.gnome					: get_gnome_last_name   ,
		Race.halfling				: get_halfling_last_name
	}
	return namelookup[race](race)
	
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

#Character Generation assistent functions
def probablyGay(race):
	percent_ssa_elves = 0.5
	percent_ssa_others = 0.034
	to_return = percent_ssa_others
	if race == Race.elf:
		to_return = percent_ssa_elves
	return to_return

def isGay(race):
	return 1 if random.randrange(0,1000+1) < 1000*probablyGay(race) else 0

#Age range functions, need to update to support non-humans
def gen_adult_age(race):
	return random.randrange(18,40+1)
	
def gen_elderly_age(race):
	return random.randrange(41,100+1)
	
def gen_teenager_age(race):
	return random.randrange(12,17+1)
	
def gen_youth_age(race):
	return random.randrange(0,11+1)

#Character Generation Functions
def genRootCharacter(race,lastname):
	#Determine character orientation
	if isGay(race) == 0:
		orientation = Orientation.straight
		gender = Gender.male if random.randrange(0,100+1) != 0 else Gender.female #Male/Female select
	else:
		orientation = Orientation.gay
		gender = Gender.male if random.randrange(0,1+1) != 0 else Gender.female #Male/Female select
	return Character(race,gender,orientation, gen_adult_age(race))

def genComplimentCharacter(rootCharacter):
	#If the root character is gay, then this character is a copy of the root character.
	#If the root character is straight, then this character is the opposite sex, but may
	#still be gay.
	gender = rootCharacter.gender
	orientation = rootCharacter.orientation
	race = rootCharacter.race
	if rootCharacter.orientation == Orientation.straight:
		gender = Gender.male if rootCharacter.gender == Gender.female else Gender.female #Male/Female select
		orientation = Orientation.gay if isGay(race) == 1 else Orientation.straight
	return Character(rootCharacter.race,gender,orientation,gen_adult_age(race))

#TODO
def genDerivedCharacter(rootCharacter,complimentCharacter):
	race = rootCharacter.race
	age = gen_youth_age(race)
	return Character(rootCharacter.race,rootCharacter.gender,rootCharacter.orientation,age)

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
	def __init__(self,race,gender,orientation,age):
		self.race = race
		self.gender = gender
		self.orientation = orientation
		self.firstname = "FirstName"
		self.lastname = "LastName"
		self.age = age
		self.employment = "This will be a Employment Class"
		
class Employment:
	def __init__(self):
		self.jobtitle = "Persons Title"
		self.employer = "This will be a Character Class"
		self.employees = "This will be an array of Character Classes"

############################################################################################
## Main Execution Code
############################################################################################

if __name__ == '__main__':
	##### Step 1 Household Size and last Name
	## Set Target population
	target_population = 6000
	#Generate households until target is reached.
	households = []
	current_total_population = 0
	while(current_total_population < target_population):
		primaryrace = gen_primary_race()
		lastname = get_last_name(primaryrace)
		new_household = Household(primaryrace,lastname)
		households.append(new_household)
		current_total_population += len(new_household.residents)
		
	print "Total Population: " + str(current_total_population)
