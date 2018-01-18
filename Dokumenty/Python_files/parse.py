#!/usr/bin/python
import sys
from Bio.Blast import NCBIXML
#count = 0
h = open("out.txt","w")
#alignment - hit_id, hit_def,hsps
count = 0
file = sys.argv[1]
f = open(file,"r")
blast = NCBIXML.parse(f)
for record in blast:
	for align in record.alignments:
		count +=1
		for hsp in align.hsps:
			h.write('>'+align.hit_id+align.hit_def+'\n')
			h.write(hsp.query+'\n')

print count