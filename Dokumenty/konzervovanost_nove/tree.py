#!/usr/bin/python

from __future__ import division
from ete3 import Tree
import sys
import copy
from blosum import *
from Bio.PDB import *
import urllib2
tree_test = sys.argv[1]
newick_file = sys.argv[2]
name = sys.argv[3]

matrix = BlosumMatrix('./blosum62.txt')
probability_matrix = ProbabilityMatrix(tree_test,matrix)


class Etree(Tree):

	_names = []
	alignements = dict()
	_identificators = []
	_IDs = dict()
	_idArray = dict()


	def get_pdb(self,name):
		protein_file = name + ".pdb"
		pdb_output  = open(protein_file, "w")
		url_pdb = "https://files.rcsb.org/download/%s.pdb" %name
		try:
			handle = urllib2.urlopen(url_pdb)
		except URLError as error:
			print(error.reason)
			sys.exit(1)
		pdb_output.write(handle.read())
		pdb_output.close()


	def PDB_parse(self,name):
		p = PDBParser()
		structure = p.get_structure(name,name+".pdb")
		model = structure[0]
		#pridat try na jednotlive chainy
		try:
			chain = model['A']
		except KeyError as error:
			try:
				chain = model['B']
			except KeyError as error:
				try:
					chain = model['C']
				except KeyError as error:
					try:
						chain = model['I']
					except KeyError as error:
						try:
							chain = model['X']
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
		#always returns position of first chain which could no be correct
		residue_list = Selection.unfold_entities(chain,'A')
		#print(residue_list[0].get_full_id()[3][1])
		residue_start = residue_list[0].get_full_id()[3][1]
		return residue_start

	def compute_conservation(self,file,residue_start,index,weightsArray,acid1):
		count_mezera = 0
		count_basic_acid = 0
		count_mutated_acid = 0
		count_else=0
		all_count =0
		count_pos=0
		start_position = 1 #meni sa
		pos = 0
		handle = open(file,"r")

		lines = iter(handle.readlines())
		for line in lines:
			if(line.startswith('>')):
				continue
			else:
				for word in line.split():
	 			#if(word[0] == '-'):
	 			#	break
		 			#if(word[0] == 'M'):
		 			#		count_pos -=1#-residue_start+1

		 			if(residue_start > len(word)):
		 				print(residue_start)
		 				print(index)
		 				count_pos = residue_start
		 				print(count_pos)
		 				for i in range(0,len(word),1):
		 					if(word[i] != '-'):
		 						count_pos +=1
		 						if(count_pos == residue_start+index):
		 							pos = i
		 							print(word[i])
		 							break
		 			else:
		 				print(residue_start)
		 				print(index)
		 				count_pos = residue_start
		 				if(residue_start < 0):
		 					chain_res = index#+residue_start #+ abs(residue_start) + abs(residue_start) -1
		 				elif (residue_start == 1):
		 					chain_res= index+residue_start
		 				else:
		 					chain_res= index+residue_start

		 				for i in range(0,len(word),1):
		 					if(word[i] != '-'):
		 						count_pos +=1
		 						if(count_pos == chain_res):
		 							pos = i
		 							#print(pos)
		 							print(word[i])
		 							break
	 		break
	 	print(pos)
	 	conservation_value = 0
	 	base_acid = 0
	 	weights = 0
	 	for name in self._names:
	 		sequence = self._idArray[name]
	 		acid = sequence[pos]
	 		if(acid == acid1):
	 			base_acid = 1
	 		else:
	 			base_acid= 0
	 		weights += weightsArray[name]
	 		conservation_value += weightsArray[name] * base_acid

	 	accuracy = conservation_value/ weights
	 	return accuracy


	def create_ID_table(self):
		"""create table where key is node name and value is sequence to speed up lookup"""
		for name in self._names:
			key1 = self._IDs.get(name)
			seq1 = self.alignements[key1]
			self._idArray[name] = seq1



	def create_alignement_table(self,file):
		"""creates lookup table for sequence names and sequences"""
		with open(file,'r') as f:
			lines = iter(f.readlines())
			for line in lines:
				if(line.startswith('>')):
					name = line.strip('>').strip('\n')
					sequence = lines.next().strip('\n')
					self.alignements[name] = sequence


	def create_names_table(self,file):
		"""create lookup table for complete sequence ID according to its abbrevation"""
		with open(file,'r') as f:
			lines = iter(f.readlines())
			for line in lines:
				if(line.startswith('>')):
					self._identificators.append(line.strip('>').strip('\n'))

		for item in self._identificators:
			for name in self._names:
				if(name in item):
					self._IDs[name] = item


	def get_table_value(self,value):
		"""get value from alignements table"""
		return self.alignements[value]


	def get_names(self):
		"""get all leaf names in the tree and stores them in _names array"""
		for leaf in self:
			if(leaf.is_leaf()):
				self._names.append(leaf.name)

	def print_names(self):
		"""function for printing leafs names"""
		for name in self._names:
			print(name)

	def create_array(self):
		"""creates array of weights and fills it with value according to its node"""
		self.weightsArray = dict()
		for name in self._names:
			self.weightsArray[name] = 0
		if self.name != '':
			self.weightsArray[self.name] = 1

	def add_node_array(self):
		"""adds weights array to every node in the tree"""
		for node in self.traverse('postorder'):
			node.create_array()


	def calculate_weights(self):
		"""calculates the values in weights array in each node"""

		#fudge factor constant to prevent 0 in the weights array
		fugde_factor = 0.1

		#traverse the tree and compute values in each node
		for node in t.traverse('postorder'):

			#get children nodes of actual node
			children = node.get_children()

			#if no children found, continue with next node
			if not children:
				continue
			else:

				i = 0
				#array where value of multiplication for each item in array is stored
				vals = [1]*250

				#calculate value for each child
				for child in children:

					for parentItem in node._names:
						result = 0
						seq2 = node._idArray[parentItem]

						for childItem in child._names:

							#calculate probability of changing child sequence to parent sequence
							seq1 = child._idArray[childItem]
							probability = probability_matrix.find_pair(seq1,seq2)

							#vzorec Pi*Li*t
							result += probability * child.weightsArray[childItem] * (child.dist + fugde_factor)

						#value from each child needs to be multiplicated
						vals[i] *= result
						#store actual value to weightsArray item in parent node
						node.weightsArray[parentItem] = vals[i]

						i+=1
					i = 0

			#print(node.weightsArray.values())



		return t.get_tree_root().weightsArray



#t = Tree(newick_file)
#print(t)
t = Etree(newick_file)
t.create_alignement_table(tree_test)

R = t.get_midpoint_outgroup()
t.set_outgroup(R)

t.getNames()

t.add_node_array()
t.create_names_table(tree_test)

t.create_ID_table()
rootWeightsArray = t.calculate_weights()

#for name in names:
t.get_pdb(name)
start_pos = t.PDB_parse(name)
f = open(name+'_NEW.txt','r')
out = open(name+'_conservation_results1.txt','w')
for line in f.readlines():
	original_acid = line[0]
	out.write(original_acid+ " ")
	position = int(line[1:])
	out.write(str(position)+ ' ')
	conservation_score = t.compute_conservation(tree_test,start_pos,position,rootWeightsArray,original_acid)
 	out.write(str(conservation_score)+ '\n')
