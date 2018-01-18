#!/usr/bin/python
#import Bio
#from Bio.Blast import NCBIXML
#def read_XML():
#	handle = open("blast.xml","r")
#	blast_records = NCBIXML.parse(handle)
#	save_file = open("seq.fasta","w")
#
#	for i,blast_record in enumerate(blast_records):
#		if i==10: break
#		for alignment in blast_record.alignments:
#			for hsp in alignment.hsps:
#				save_file.write('>%s\n'%(hsp.query))
#read_XML() 

import xml.etree.ElementTree as etree
tree = etree.parse('blast.xml')
root = tree.getroot()
root
print(root.tag)
print(len(root))
for child in root[8]:
    for child in child:
        for ch in[4]:
            print(ch.tag,ch.attrib) 
#print(root.attrib)
#print(root[4])
#for child in root[8]:
#    print(child[2].id)      	
