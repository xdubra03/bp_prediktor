#!/usr/bin/python

from __future__ import division
import sys
import os
from shutil import copyfile
import itertools
import math
from Bio.PDB import *
import copy
filename = sys.argv[1]
correlation_file = sys.argv[2]
name = sys.argv[3]
#index = int(sys.argv[4])

def createAlignementTable(self,file):
    """creates lookup table for sequence names and sequences"""
    with open(file,'r') as f:
        lines = iter(f.readlines())
        with open(self,'w') as corr:
            for line in lines:
                if(line.startswith('>')):
                    name = line.strip('>').strip('\n')
                    sequence = lines.next()
                    corr.write(sequence)
        corr.close()
    f.close()

def PDB_parse(name):
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

def compute_conservation(file,residue_start,index):

	handle = open(file,"r")
	lines = iter(handle.readlines())
	pos = 0
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
		 			#print(residue_start)
		 			#print(index)
		 			count_pos = residue_start
		 			#print(count_pos)
		 			for i in range(0,len(word),1):
		 				if(word[i] != '-'):
		 					count_pos +=1
		 					if(count_pos == residue_start+index):
		 						pos = i
		 						#print(word[i])
		 						break
		 		else:
		 			#print(residue_start)
		 			#print(index)
		 			count_pos = 0
		 			if(residue_start < 0):
		 				chain_res = index#+residue_start #+ abs(residue_start) + abs(residue_start) -1
		 			elif (residue_start == 1):
		 				chain_res= index+residue_start
		 			else:
		 				chain_res= index+residue_start-1

		 			for i in range(0,len(word),1):
		 				if(word[i] != '-'):
		 					count_pos +=1
		 					if(count_pos == chain_res):
		 						pos = i
		 						#print(pos)
		 						#print(word[i])
		 						break
	 	break
	return pos


if not os.path.exists(name):
    os.mkdir(name)
    copyfile(name+'clustal.fasta','./'+name+'/'+name+'clustal.fasta')
    copyfile(name+'.pdb','./'+name+'/'+name+'.pdb')
    copyfile(name+'_NEW.txt','./'+name+'/'+name+'_NEW.txt')
    createAlignementTable(correlation_file,filename)
    copyfile(name+'correlation.txt','./'+name+'/'+name+'correlation.txt')
residue_start = PDB_parse(name)
#column_index = compute_conservation(correlation_file,residue_start,index)
os.chdir(name)
arr = []
resArr = dict()
nucleoColumn1 = dict()
nucleoColumn2 = dict()
positions = []
with open(name+'_NEW.txt','r') as h:
    lines = h.readlines()
    for line in lines:
        positions.append(int(line[1:]))
h.close()

with open(correlation_file,'r') as f:
    lines = list(iter(f.readlines()))
    correlation_array = []
    #iterate through all colums in sequence
    print(positions)
    new_arr = dict()
    for position in positions:
        #fl = open(name+'_CORRELATION'+str(position)+'.txt','w')
        column_index = compute_conservation(correlation_file,residue_start,position)
        for index in range(len(lines[0])-1):
            for line in lines:
                #create arrays for mutated columns and other columns
                nucleoColumn1[line[index]] = 0
                nucleoColumn2[line[column_index]] = 0
                #array for storing pairs from 2 columns
                resArr[line[index]+line[column_index]] =0

            for line in lines:
                #count number of acids/pairs in columns
                nucleoColumn1[line[index]] += 1
                nucleoColumn2[line[column_index]] +=1
                resArr[line[index]+line[column_index]] +=1

            #new_arr = copy.deepcopy(resArr)
            #print(resArr)
            #count occurance of acid/pair in columns(in )
            for i in nucleoColumn1:
                nucleoColumn1[i] /= 250
            for i in nucleoColumn2:
                nucleoColumn2[i] /= 250
            for i in resArr:
                resArr[i] /= 250

            #print(nucleoColumn1)
            #print(nucleoColumn2)
            #print(resArr)
            correlation_score = 0
            for i in nucleoColumn1:
                for j in nucleoColumn2:
                    key = i+j
                    if(key in resArr.keys()):
                        value = resArr[key] / (nucleoColumn1[i] * nucleoColumn2[j])
                        correlation_score += resArr[key] * math.log(value,2)
            correlation_array.append(correlation_score)
            #fl.write(str(correlation_score)+'\n')
            #print(correlation_score)
            #new_arr = resArr
            correlation_score = 0
            resArr = dict()
            nucleoColumn1 = dict()
            nucleoColumn2 = dict()
        #print('------START--------')
        #print(resArr)
        #print('-------END---------')
        #resArr = dict()
        #fl.close()
f.close()
