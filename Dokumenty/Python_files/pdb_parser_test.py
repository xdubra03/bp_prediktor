#!/usr/bin/python

import Bio
from Bio import PDB
from Bio.PDB import Selection
#from Bio.PDB.StructureBuilder
from Bio.PDB.PDBParser import PDBParser
import sys

#from Bio.PDB.Selection import unfold_entities, entity_levels, uniqueify
#toto funguje,zistim zaciatocny index sekvencie z pdb suboru
#count = 0
#parser = PDBParser()
#structure = parser.get_structure("1mgr","1a2p.pdb")
#for model in structure:
#	for chain in model["A"]:
#		for residue in chain:
#			count +=1
#			resseq = residue.get_full_id()[3][1]
#			if count == 1:
#
#asi lepsie riesenie, mam list residui a zistim zaciatocnu poziciu prveho
#staci len prvy, ostatne nas nezaujimaju
#pri chybnych datach v PDB subore vypisuje WARNING
#parser = PDBParser()
#structure = parser.get_structure("1mgr","1otr.pdb")
#model = structure[0]
#chain = model['B']
#residue_list = Selection.unfold_entities(chain,'A')
#print(residue_list[0].get_full_id()[3][1])

	#structure = p.get_structure(name,name+".pdb")
p = PDBParser()
structure = p.get_structure("1mgr","1otr.pdb")
model = structure[0]
	#ma = False
	#mb = False
	#mc = False
	#mi = False
	#mx = False
	#pridat try na jednotlive chainy
try:
	chain = model['A']
	ma = True
	print(chain)
except KeyError as error:
	try:
		chain = model['C']
		mb = True
	except KeyError as error:
		try:
			chain = model['C']
			mc = True
		except KeyError as error:
			try:
				chain = model['I']
				mi = True
			except KeyError as error:
				try:
					chain = model['X']
					mx = True
				except KeyError as error:
					print("Cannot find this type of chain.")
					sys.exit(1)
				else:
					pass
			else:
				pass
		else:
			pass
	else:
		pass
else:
	pass
	
#	try:
#			chain = model['I']
		#except URLError as error:
	#		print(error.reason)
	#	else:
	#		pass
	#if(ma == True):
	#	chain = model['A']
	#elif(mb == True):
	#	chain = model['B']
	#elif(mc == True):
	#	chain = model['C']
#print(chain)
residue_list = Selection.unfold_entities(chain,'A')
print(residue_list[0].get_full_id()[3][1])
	#residue_start = residue_list[0].get_full_id()[3][1]
	#return residue_start
	