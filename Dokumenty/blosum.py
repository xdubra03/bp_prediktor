#module for handling operations with matrices

from __future__ import division
import sys

class BlosumMatrix:
	"""class for working with BLOSUM matrix"""
	def __init__(self,matrix):
		self.load_matrix(matrix)
	
	#load BLOSUM matrix from file and create dictionary representing matrix	
	def load_matrix(self,matrix):
		with open(matrix,'r') as blosum_file:
			blosum_matrix = blosum_file.read()

		lines = blosum_matrix.strip().split('\n')
		header = lines.pop(0)
		columns = header.split()
		blosum_matrix = {}

		for row in lines:
			entries = row.split()
			row_name = entries.pop(0)
			blosum_matrix[row_name] = {}

			for column_name in columns:
				blosum_matrix[row_name][column_name] = entries.pop(0)

		self._blosum_matrix = blosum_matrix
		#print(self._blosum_matrix)

	#find score of 2 amino acids according to BLOSUM matrix
	def find_score(self,acid1,acid2):
		acid1 = acid1.upper()
		acid2 = acid2.upper()

		if acid1 not in self._blosum_matrix or acid2 not in self._blosum_matrix[acid1]:
			print('Error')
		return self._blosum_matrix[acid1][acid2]


class ProbabilityMatrix:
	"""creates probability matrix from multiple alignements"""
	def __init__(self,filename,matrix):
		alignements = self.store_alignement(filename)
		self.create_probability_matrix(matrix,alignements)
	
	#stores alignements from file	
	def store_alignement(self,filename):
		alignements = list()
		with open(filename,'r') as t:
			for line in t.readlines():
				if not line.startswith('>'):
					seq1 = line.strip('\n')
					alignements.append(seq1)
		return alignements
	#creates probability matrix as a dictionary, key value is alignement itself
	def create_probability_matrix(self,matrix,alignements):
		score = 0
		max_score = 0
		probability_matrix = {}
		for i in alignements:
			for c,c1 in zip(i,i):
				max_score += int(matrix.find_score(c,c1))
			print('Max score je:' + str(max_score))
			probability_matrix[i] = {}
			for j in alignements:
				for c,c1 in zip(i,j):
					score += int(matrix.find_score(c,c1))
				probability_matrix[i][j] = score / max_score
				m = score / max_score
				print('Score je:' + str(m))
				score = 0
			max_score = 0

		self._probability_matrix = probability_matrix
		#print(self._probability_matrix)


	def find_pair(self,seq1,seq2):
		return self._probability_matrix[seq1][seq2]

