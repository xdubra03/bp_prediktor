#!/usr/bin/python


from __future__ import division
from ete3 import Tree
import sys
import copy
from Bio import Phylo
import pylab
from Bio import pairwise2
from Bio import SeqIO
from blosum import *

tree_test = sys.argv[1] 
 
matrix = BlosumMatrix('blosum62.txt')
probability_matrix = ProbabilityMatrix(tree_test,matrix)
print(probability_matrix)

