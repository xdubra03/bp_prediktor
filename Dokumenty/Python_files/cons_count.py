#!/usr/bin/python3

import os
import sys
import subprocess
from subprocess import Popen, PIPE

file = sys.argv[1]
original_acid = sys.argv[2]
mutated_acid = sys.argv[3]
position = sys.argv[4]
index = int(position)
index = index -1-1
f = open(file,"r+")
count_mezera = 0
count_basic_acid = 0
count_mutated_acid = 0
count_else=0
all_count = 0

lines = f.readlines()
for line in lines:
	all_count += 1
	words = line.split()
	for word in words:
		print (word[index])
		if word[index] == "-":
			count_mezera += 1
		elif word[index] == original_acid:
			count_basic_acid +=1
		elif word[index] == mutated_acid:
			count_mutated_acid += 1
		else:
			count_else +=1			 

print ("---------------\n")
print ("file: %s" %file)
print ("original acid: %s" %original_acid)
print ("mutated acid: %s" %mutated_acid)
print ("at position: %s\n" %position)
print ("Statistics:\n")
print ("No all proteins: %d" % all_count)
print ("Is same at position: count = %d" %count_mezera)
print ("No. basic acids at position: %d" %count_basic_acid)
print ("No. mutated acids at position: %d" %count_mutated_acid)
print ("No. else acids at position: %d" %count_else)
percentage = (float((count_basic_acid) / (all_count))) *100
percentage_mutated = (float(count_mutated_acid) / (all_count))*100  
print ("Originals : %f" %percentage)
print ("Mutated : %f" %percentage_mutated)
