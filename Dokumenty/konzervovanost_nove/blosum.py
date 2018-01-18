#module for handling operations with matrices

from __future__ import division
import sys
import os
import numpy as np
from scipy import stats


class BlosumMatrix:
	"""class for working with BLOSUM matrix"""
	def __init__(self,matrix):
		self.load_matrix(matrix)

	def load_matrix(self,matrix):
		"""load BLOSUM matrix from file and create dictionary representing matrix"""
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

	def find_score(self,acid1,acid2):
		"""find score of 2 amino acids according to BLOSUM matrix"""
		acid1 = acid1.upper()
		acid2 = acid2.upper()

		if acid1 not in self._blosum_matrix or acid2 not in self._blosum_matrix[acid1]:
			print(acid1)
			print(acid2)
			sys.stderr.write('Error')
			sys.exit(1)
		return self._blosum_matrix[acid1][acid2]


class ProbabilityMatrix:
	"""creates probability matrix from multiple alignements"""
	def __init__(self,filename,matrix):
		alignements = self.store_alignement(filename)
		self.create_probability_matrix(matrix,alignements)

	def store_alignement(self,filename):
		"""#stores alignements from file"""
		alignements = list()
		with open(filename,'r') as t:
			for line in t.readlines():
				if not line.startswith('>'):
					seq1 = line.strip('\n')
					alignements.append(seq1)
		return alignements

	def create_probability_matrix(self,matrix,alignements):
		"""creates probability matrix as a dictionary, key value is alignement itself"""
		score = 0
		max_score = 0
		probability_matrix = {}
		probabilities = []

		for i in alignements:
			for acid,acid1 in zip(i,i):
				max_score += int(matrix.find_score(acid,acid1))
			probability_matrix[i] = {}

			for j in alignements:
				for acid,acid1 in zip(i,j):
					score += int(matrix.find_score(acid,acid1))
					probability = score / max_score
					probabilities.append(probability)
					probability_matrix[i][j] = probability

				score = 0
			max_score = 0

		self._probability_matrix = probability_matrix

		#print(probabilities)

		#z_array = np.array(probabilities)
		#z_result = stats.zscore(z_array)

		#k = 0
		#for i in alignements:
		#	for j in alignements:
		#		probability_matrix[i][j] = z_result[k]
		#		k+=1





	def find_pair(self,seq1,seq2):
		"""find probability for entered sequnces"""
		return self._probability_matrix[seq1][seq2]
