#!/usr/bin/python

from Bio import Phylo
import pylab
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

names = ['1h7m','1ihb','1io2','1iro','1jiw','1kfw','3run','1lhm','1mjc','3snf','2qmt',
'1rbp','1rro','1rwy','3ua7','1u06','1sup','1pml','1urp','4n0k','1yea','1yu5','1zym','2hbb','2hip','2rn2',
'2trx','3d2c','3sil','451c','4blm']

"""'1a23','3a9j','1ag2','1aj3','3wc8','1am7','1arr','4hil','1a2p','1bpi','1a6m','1c2r','1cdc',
'1clw','3pf4','1cyo','1el1','1rcf','3b0k','1hue','1ig5','2nvh','1k9q','2nwd','1mbg','1mgr','1otr','2qmt',
'1poh','3ne4','1lni','1rn1','2ijk','1kf5','1ra9','4wor','1gvp','1wq5','1hb6','1rg8','1aky','2ci2','2hpr',
'3fa0','2cle','2hds','1hwg','3q27','3ssi','2vb1',"""

"""'1gvp','1wq5','1hb6','1rg8','1aky','2ci2','2hpr',
'3fa0','2cle','2hds','1hwg','3q27','3ssi','2vb1','5cro','4jzk','2bt6','1azp','1c52','1c9o','1chk','1cyo',
'1v0l','1f6r','4wf5','1gw5', problem s 1gw5, '1el1','3b0k','1ig5','1k9q','1mbg','1otr','2qmt','3ne4','2ijk','1kf5','2ci2','1hwg','2vb1',
'1azp','3run','3snf','2qmt','1rro','1rwy','1pml'"""
def get_fasta(name):
	fasta_file = name + ".fasta"
	fasta_output = open(fasta_file,"w")
	url_f = "http://www.rcsb.org/pdb/download/downloadFastaFiles.do?structureIdList=%s&compressionType=uncompressed"%name
	try:
		h = urllib2.urlopen(url_f)
	except URLError as error:
		print(error.reason)
		sys.exit(1)
	fasta_output.write(h.read())
	fasta_output.close()
	i = 0
	fasta_cleaned = open(name+"FASTA"+".fasta","w")
	handle = open(fasta_file,"r")
	lines = iter(handle.readlines())
	
	for line in lines:
		if(line.startswith('>')):
			i+=1
		if(i < 2):
			fasta_cleaned.write(line)
	fasta_cleaned.close() 


def parse_XML(name):
	i=0
	output = open(name+".txt","w")
	f = open(name+".xml","r")
	blast = NCBIXML.parse(f)
	names = []
	protein = ''
	for record in blast:
		for align in record.alignments:
			for hsp in align.hsps:
				i+= 1
				protein = '>'+align.hit_id+align.hit_def
				if(protein in names):
					break
				else:
					names.append(protein)
					output.write('>'+align.hit_id+align.hit_def+'\n')
				output.write(hsp.sbjct+'\n') #find out 
				

	f.close()
	output.close()	

def run_blast(name):
	subprocess.call(['./blastp','-query','%sFASTA.fasta'%name,'-db','nr','-outfmt','5','-out','%s.xml'%name,'-max_target_seqs','250','-remote'])

def run_clustal(name):
	in_file = name + ".txt"
	out_file = name +"clustal.fasta"
	clustalomega_cline = ClustalOmegaCommandline(infile=in_file, outfile=out_file, verbose=True, auto=True)
	subprocess.call(['./clustalo','-i','%s'%in_file,'--outfmt=vie','-o','%s'%out_file,'--auto','-v','--force'])	
#tree = Phylo.read("tree_test","newick") 

#Phylo.draw_graphviz(tree)
#pylab.show()
#terminals = tree.find_clades(order='postorder')
#for t in terminals:
	#print (t)
#print(tree)
#tree.rooted = True
#Phylo.draw(tree)
"""'1h7m','1ihb','1io2','1iro','1jiw','1kfw','3run','1lhm','1mjc','3snf','2qmt',
'1rbp','1rro','1rwy','3ua7','1u06','1sup','1pml','1urp','4n0k','1yea','1yu5','1zym','2hbb','2hip','2rn2',
'2trx','3d2c','3sil','451c','4blm', '1a23','3a9j','1ag2','1aj3','3wc8','1am7','1arr','4hil','1a2p','1bpi','1bpi','1a6m','1c2r','1cdc',
'1clw','3pf4','1cyo','1el1','1rcf','3b0k','1hue','1ig5','2nvh','1k9q','2nwd','1mbg','1mgr','1otr','2qmt',
'1poh','3ne4','1lni','1rn1','2ijk','1kf5','1ra9','4wor','1gvp','1wq5','1hb6','1rg8','1aky','2ci2','2hpr',
'3fa0','2cle','2hds','1hwg','3q27','3ssi','2vb1','1gvp','1wq5','1hb6','1rg8','1aky','2ci2','2hpr',
'3fa0','2cle','2hds','1hwg','3q27','3ssi','2vb1','5cro','4jzk','2bt6','1azp','1c52','1c9o','1chk','1cyo',
'1v0l','1f6r','4wf5','1el1','3b0k','1ig5','1k9q','1mbg','1otr','2qmt','3ne4','2ijk','1kf5','2ci2','1hwg','2vb1'"""
names = ['1otr']
for name in names:
	#out = name+'output'
	#tree = Phylo.read(out,"newick") 
	#tree.rooted = True
	#Phylo.draw(tree)
	#get_fasta(name)
	run_blast(name)
	parse_XML(name)
	run_clustal(name)
	protein = name+'clustal.fasta'
	output = name+'output'
	subprocess.call(['./FastTree','-out','%s'%output,'%s'%protein])

