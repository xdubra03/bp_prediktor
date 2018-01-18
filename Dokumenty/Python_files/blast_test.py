#!/usr/bin/python
import sys
import subprocess
from Bio.Blast import NCBIXML
from subprocess import call
#input 
protein = sys.argv[1]
prot_file = protein + ".fasta"
#file = open(prot_file,"r")
#output = open("output.fasta","w")

def run_blast(file):
	subprocess.call(['blastp','-query','%s'%prot_file,'-db','nr','-outfmt','0','-out','%s.txt'%protein,'-max_target_seqs','250','-remote'])
	#subprocess.call(['blastp','-query','%s'%prot_file,'-db','nr','-outfmt','6 qseqid sseqid sseq','-out','blast.txt','-remote'])
	#subprocess.call(['blastp','-query','%s'%prot_file,'-db','nr','-outfmt','7 std qseq sseq ','-out','blast.xml','-remote'])



run_blast(prot_file)



