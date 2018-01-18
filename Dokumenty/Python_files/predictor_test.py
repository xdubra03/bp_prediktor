#!/usr/bin/python
#TODO treba upravit vystup CLUSTAL OMEGA a vypocitat konzervovanost
# mozno zadat na vstupe chain, inak treba rucne upravit fasta subor aky chain chceme spracovat

#vygenerovat parametre pre zaznam do prediktoru : 
#konzervovanost,polarity-change,charge-change,hydroindex-change,secondary-struc,asa,size-change,posledny zaznam je 
#to co sa bude pocitat - stabilita (hodnota realdgg)

#skompilovany binarny program Clustalo je potrebne mat v adresari spolocne so skriptom pre funkcnost Clustal Omega
#this is just test
from __future__ import division
from sklearn.ensemble import RandomForestClassifier
import pandas as pd
import Bio
from Bio.PDB import *
import urllib2
import os
import shutil
import sys
import subprocess
from Bio.Blast import NCBIWWW
from Bio.Blast import NCBIXML
from Bio import SeqIO
from Bio.Align.Applications import ClustalOmegaCommandline
#TODO create dictonaries for parameters: polarity change,...


#encoding dictonaries for RandomForest classifier
encode_sec_struc = {
	'C':'1','H':'2','S':'3'
}
encode_conservation = {
	'+':'1','-':'-1'
}
encode_charge = {
	'+':'1','-':'-1','0':'0'
}
encode_polarity = {
	'+':'1','-':'-1','0':'0'
}
encode_asa = {
	'burried':'10','partially_burried':'11','exposed':'12'
}

encode_size = {
	'+':'1','-':'-1','0':'0'
}


#dictionary for polarity of amino acids
# +  polar
# -  nonpolar
polarity = {'A':'-','R':'+','N':'+','D':'+','C':'-','E':'+',
            'Q':'+','G':'-','H':'+','I':'-','L':'-','K':'+',
            'M':'-','F':'-','P':'-','S':'+','T':'+','W':'-',
            'Y':'+','V':'-' 
}
#dictionary for charge of amino acids
# 0 neutral
# + positive
# - negative
charge = {  'A':'0','R':'+','N':'0','D':'-','C':'0','E':'-',
            'Q':'0','G':'0','H':'0','I':'0','L':'0','K':'+',
            'M':'0','F':'0','P':'0','S':'0','T':'0','W':'0',
            'Y':'0','V':'0' 
}
#dictionary for hydropathy index of amino acid
hydro_index = {'A':1.8,'R':-4.5,'N':-3.5,'D':-3.5,'C':2.5,
               'E':-3.5,'Q':-3.5,'G':-0.4,'H':-3.2,'I':4.5,
               'L':3.8,'K':-3.9,'M':1.9,'F':2.8,'P':-1.6,
               'S':-0.8,'T':-0.7,'W':-0.9,'Y':-1.3,'V':4.2 
}

size_change1 = {'G':75.1,'A':89.1,'S':105.1,'P':115.1, #first interval
				'V':117.1,'T':119.1,'C':121.2,'I':131.2,'L':131.2,'N':132.1,'D':133.1,
				'Q':146.1,'K':146.2,'E':147.1,'M':149.2,'H':155.2, #second interval
				'F':165.2,'R':174.2,'Y':181.2, #3rd interval
				'W':204.2 #last interval
}

#computes size change of original amino acid and mutated amino acid
# 0 - acids are in the same size group
# + change from small to big one
# - change from big to small one
def size_change(x,y):
	if(x in ['G','A','S','P']):
		if(y in ['V','T','C','I','L','N','D','Q','K','E','M','H']):
			return '+'
		elif(y in ['G','A','S','P']):
			return '0'
		elif(y in ['F','R','Y']):
			return '+'
		elif(y == 'W'):
			return '+'
	elif(x in ['V','T','C','I','L','N','D','Q','K','E','M','H']):
		if(y in['G','A','S','P']):	
			return '-'
		elif(y in ['F','R','Y','W']):
			return '+'
		elif(y in ['V','T','C','I','L','N','D','Q','K','E','M','H']):
			return '0'
	elif(x in ['F','R','Y']):
		if(y in ['G','A','S','P']):
			return '-'
		elif(y in ['V','T','C','I','L','N','D','Q','K','E','M','H']):
			return '-'
		elif(y in ['F','R','Y']):
			return '0'
		elif(y == 'W'):
			return '+'	
	elif(x == 'W'):
		if(y in ['G','A','S','P']):
			return '-'
		elif(y in ['V','T','C','I','L','N','D','Q','K','E','M','H']):
			return '-'
		elif(y in ['F','R','Y']):
			return '-'
		elif(y == 'W'):
			return '0'							
#polarity change
def compute_polarity(x,y):
	if polarity[x] == '-':
		if polarity[y] == '+':
			return '+'
		elif polarity[y] == '-':
			return '0'
	elif polarity[x] == '+':
		if polarity[y] == '+':
			return '0'
		elif polarity[y] == '-':
			return '-'
#charge change
def compute_charge(x,y):
	if charge[x] == '+':
		if charge[y] == '-':
			return '-'
		elif charge[y] == '0':
			return '-'
		elif charge[y] == '+':
			return '0'
	elif charge[x] == '0':
		if charge[y] == '-':
			return '-'
		elif charge[y] == '0':
			return '0'
		elif charge[y] == '+':
			return '+'
	elif charge[x] == '-':
		if charge[y] == '-':
			return '0'
		elif charge[y] == '0':
			return '+'
		elif charge[y] == '+':
			return '+'
#change of hydro index
def compute_hydro_index(x,y):
	if hydro_index[x] > hydro_index[y]:
		return hydro_index[y] - hydro_index[x]
	elif hydro_index[x] < hydro_index[y]:
		return hydro_index[y] - hydro_index[x]
	elif hydro_index[x] == hydro_index[y]:
		return 0

#encode secondary structure to one of three options 
#H,G,I   H(helix)
#E,B    S(sheet)
#T,S,-  C(coil)
def sec_struc_code(id):
	if(id == 'H' or id == 'G' or id == 'I'):
		sec_struc_res = 'H'
	elif(id == 'E' or id == 'B'):
		sec_struc_res = 'S'
	elif(id == 'T' or id == 'S' or id == '-'):
		sec_struc_res = 'C'	
	return sec_struc_res

#get FASTA file for input protein
def get_fasta(name):
	fasta_file = protein + ".fasta"
	fasta_output = open(fasta_file,"w")
	url_f = "http://www.rcsb.org/pdb/download/downloadFile.do?fileFormat=FASTA&compression=NO&structureId=%s"%name
	try:
		h = urllib2.urlopen(url_f)
	except URLError as error:
		print(error.reason)
		sys.exit(1)
	fasta_output.write(h.read())
	fasta_output.close()

#get PDB file for input protein
def get_pdb(name):
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

#runs locally installed BLAST on input FASTA file,output of BLAST is in xml format
def run_blast(name):
	subprocess.call(['blastp','-query','%s.fasta'%name,'-db','nr','-outfmt','5','-out','%s.xml'%name,'-max_target_seqs','250','-remote'])

#parse BLAST XML output and write informations to FASTA like format file
#need to check if i should write hsp.query as an output and not something else
def parse_XML(name):
	i=0
	output = open(name+".txt","w")
	f = open(name+".xml","r")
	blast = NCBIXML.parse(f)
	for record in blast:
		for align in record.alignments:
			for hsp in align.hsps:
				if(i == 0):
					output.write('>|query'+'\n')
					output.write(hsp.query+'\n')
				i+=1
				output.write('>'+align.hit_id+align.hit_def+'\n')
				output.write(hsp.sbjct+'\n') #find out 

	f.close()
	output.close()			

#runs locally installed clustal omega (for this moment binary file is in the same directory as the script)
# i use vienna format for output
#output file is in name + "clustal.txt" file
#from this file (have to edit it at first) i should be able to compute conservation of amino acid
def run_clustal(name):
	in_file = name + ".txt"
	out_file = name +"clustal.fasta"
	clustalomega_cline = ClustalOmegaCommandline(infile=in_file, outfile=out_file, verbose=True, auto=True)
	subprocess.call(['./clustalo','-i','%s'%in_file,'--outfmt=vie','-o','%s'%out_file,'--auto','-v','--force'])

#parse PDB file and gives the start position of the residue 
#this is for our need to calculate conservation of amino acid on certain position
#for invalid PDB data gives warnings
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

#computing ASA value and secondary structure 
def compute_ASA(name):
	p = PDBParser()
	structure = p.get_structure(protein,name+".pdb")
	model = structure[0]
	dssp = DSSP(model,name+".pdb")
	#index is position of mutation
	#zatial je asi potrebne mat residue_start+1 aby sedela hodnota ASA
	a_key = list(dssp.keys())[index -residue_start+1-1]
	asa = str(dssp[a_key][3])
	sec_structure = str(dssp[a_key][2])
	sec_struc_id = sec_struc_code(sec_structure)
	#print("Skratka pre sek strukturu:%s" %sec_struc_id)
	#print("Secondary structure %s" %dssp[a_key][2])
	#print("ASA %.8f" %dssp[a_key][3])
	return sec_struc_id,asa
#divide asa values into 3 groups for predictor
def divide_asa(asa):
	d_asa = float(asa)
	if(d_asa < 0.25 ):
		return "burried"
	elif (d_asa >= 0.25 and d_asa <= 0.5):
		return "partially_burried"
	elif (d_asa > 0.5):
		return "exposed"

#calculates conservation of amino acid on given position
#conservation is calculating in file with multiple sequence alignment 
#multiple sequence alignment file is output from standalone Clustal Omega tool
#file needs to be edited before computing conservation
#may not work in all cases :)
def compute_conservation(file):
	count_mezera = 0
	count_basic_acid = 0
	count_mutated_acid = 0
	count_else=0
	all_count =0
	count_pos=0
	start_position = 1 #meni sa
	pos = 0
	handle = open(file+"output.txt","r")
	for line in handle.readlines(): 			
		for word in line.split():
 			#if(word[0] == '-'):
 			#	break
 			if(word[0] == 'M'):
 					count_pos -=1
 			if(index > len(word)):
 				for i in range(0,len(word),1):
 					if(word[i] != '-'):
 						count_pos +=1
 						if(count_pos == index-residue_start+1):
 							pos = i
 							print(pos)
 							print(word[i])
 							break
 			else:
 				for i in range(0,len(word),1):
 					if(word[i] != '-'):
 						count_pos +=1
 						if(count_pos == index):
 							pos = i
 							print(pos)
 							print(word[i])
 							break
 		break				
	handle.close()
	handle = open(file+"output.txt","r")
	for line in handle.readlines():
		all_count += 1
		for word in line.split():
			if word[pos] == "-":
				count_mezera += 1
			elif word[pos] == original_acid:
				count_basic_acid +=1
			elif word[pos] == mutated_acid:
				count_mutated_acid += 1
			else:
				count_else +=1				

	handle.close()
	percentage = (float((count_basic_acid) / (all_count))) *100
	percentage_mutated = (float(count_mutated_acid) / (all_count))*100
	percentage_else = (float(count_else) / (all_count))*100
	print('Originals: %f'%percentage)
	print('Mutated: %f'%percentage_mutated)
	print('Others: %f'%percentage_else)
	# amino acid is conservated
	if(percentage >= float(60)):
		return '+'
	#amino acid is conservated
	elif(percentage_else >= float(60)):
		return '+'
	#nonconservated amino acid
	else:
		return '-'

#predincting mutation to be stabilizing or destabilizing
#using RandomForest classifier 
#mutation record is input for prediction
def RandomForest():
	train = pd.read_csv("RF_data.csv")
	test =  pd.read_csv("mutation_record.csv")

	cols = ['conservation','polaritychange','chargechange','hydroindexchange','secondarystruc','asa','sizechange']
	colsRes = ['class']

	trainArr = train.as_matrix(cols)
	trainRes = train.as_matrix(colsRes)
	trainRes = trainRes.ravel()
	rf = RandomForestClassifier(n_estimators=100,n_jobs=1,min_samples_leaf=50)
	rf.fit(trainArr,trainRes)

	testArr = test.as_matrix(cols)
	result = rf.predict(testArr)
	if(result == -1):
		print('Destabilizing mutation')
	else:
		print('Stabilizing mutation')





#input data from command line: protein name, original acid, position of mutation,mutated acid
protein = sys.argv[1]
original_acid = sys.argv[2]
position = sys.argv[3]
mutated_acid = sys.argv[4]
index = int(position) 
x = original_acid
y = mutated_acid

get_fasta(protein)
get_pdb(protein)
#parse PDB to get start position of residue
residue_start = PDB_parse(protein)
#parameters: secondary structure and ASA value
struc_id,asa = compute_ASA(protein)
run_blast(protein)
parse_XML(protein)
run_clustal(protein)

o = open(protein+"output.txt","w")
for seq_record in SeqIO.parse(protein+"clustal.fasta","fasta"):
	o.write(str(seq_record.seq)+'\n')
o.close()
#copy_f = protein+"output.txt"
#shutil.copy2('%s'%copy_f,'results/%s'%copy_f)
#parameters: conservation,polarity,charge,hydro_index,size
conservation = compute_conservation(protein)
conservation = encode_conservation[conservation]
f_polarity = compute_polarity(x,y)
f_polarity = encode_polarity[f_polarity]
f_charge = compute_charge(x,y)
f_charge = encode_charge[f_charge]
f_hydro_index = str(compute_hydro_index(x,y))
size = size_change(x,y)
size = encode_size[size]
asa_val = divide_asa(asa)
asa_val = encode_asa[asa_val]
struc_id = encode_sec_struc[struc_id]
#save inforrmations as a record for predictor
#record is stored in file pred_record.txt
record = open("mutation_record.csv","w")
record.write('%s,%s,%s,%s,%s,%s,%s\n' %("conservation","polaritychange","chargechange","hydroindexchange","secondarystruc","asa","sizechange"))
record.write('%s,%s,%s,%s,%s,%s,%s' %(conservation,f_polarity,f_charge,f_hydro_index,struc_id,asa_val,size))
record.close()



print("-------Alignment status------")
print("protein %s" %protein)
print("original acid %s" %original_acid)
print("position %d" %index)
print("mutated acid %s" %mutated_acid)
print("Polarity change: %s" %compute_polarity(x,y))
print("Charge change: %s" %compute_charge(x,y))
print("Hydrophobicity change: %s" %compute_hydro_index(x,y))
print("Size change: %s"%size_change(x,y))
print("ASA value: %s" %asa)
print("ASA for dataset:%s" %divide_asa(asa))
print("Secondary structure: %s"%struc_id)
print("Conservation on index:%s"%conservation)
print("Start position is:%d\n"%int(residue_start))


RandomForest()

