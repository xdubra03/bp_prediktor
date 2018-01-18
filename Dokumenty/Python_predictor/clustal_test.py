#!/usr/bin/python
import Bio
from Bio.Align.Applications import ClustalOmegaCommandline
import subprocess

in_file = "1a23_1.fasta"
out_file = "1a23clustal.txt"
clustalomega_cline = ClustalOmegaCommandline(infile=in_file, outfile=out_file, verbose=True, auto=True)
print(clustalomega_cline)
subprocess.call(['./clustalo','-i','%s'%in_file,'--outfmt=vie','-o','%s'%out_file,'--auto','-v','--force'])