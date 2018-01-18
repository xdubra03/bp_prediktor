#!/usr/bin/python

from Bio import Phylo
import pylab


names = ['5cro','4jzk','2bt6','1azp','1c52','1c9o','1chk','1cyo',
'1v0l','1f6r','4wf5','1gw5','1h7m','1ihb','1io2','1iro','1jiw','1kfw','3run','1lhm','1mjc','3snf','2qmt',
'1rbp','1rro','1rwy','3ua7','1u06','1sup','1pml','1urp','4n0k','1yea','1yu5','1zym','2hbb','2hip','2rn2',
'2trx','3d2c','3sil','451c','4blm']

"""'1a23','3a9j','1ag2','1aj3','3wc8','1am7','1arr','4hil','1a2p','1bpi','1a6m','1c2r','1cdc',
'1clw','3pf4','1cyo','1el1','1rcf','3b0k','1hue','1ig5','2nvh','1k9q','2nwd','1mbg','1mgr','1otr','2qmt',
'1poh','3ne4','1lni','1rn1','2ijk','1kf5','1ra9','4wor','1gvp','1wq5','1hb6','1rg8','1aky','2ci2','2hpr',
'3fa0','2cle','2hds','1hwg','3q27','3ssi','2vb1',"""


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





#tree = Phylo.read("tree_test","newick") 

#Phylo.draw_graphviz(tree)
#pylab.show()
#terminals = tree.find_clades(order='postorder')
#for t in terminals:
	#print (t)
#print(tree)
#tree.rooted = True
#Phylo.draw(tree)