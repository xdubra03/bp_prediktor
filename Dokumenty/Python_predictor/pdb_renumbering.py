#!/usr/bin/env python
#
#
# Jan-Philip Gehrcke -- June 2011
# 
# USAGE: renumber_pdb_residues input.pdb > output.pdb
#
# This script renumbers the residues in a PDB file -- starting from OFFSET
# and with INCREMENT as increment (you can define these parameters  below).
# Output is written to stdout. All input lines are maintained. The order of 
# input lines is also maintained, assuming that each residue (defined by
# its residue number) is contained within one continuous block of lines in
# the input file.

from __future__ import with_statement
import os
import sys

OFFSET = 1
INCREMENT = 1

def main():
    if len(sys.argv) < 2:
        sys.exit("first argument must be a path to a file")
    inputfilepath = sys.argv[1]
    if not os.path.isfile(inputfilepath):
        sys.exit("first argument is not a valid file")
    
    p = PDBResidueRenumberer(inputfilepath, INCREMENT, OFFSET)
    p.print_renumbered()
    

class PDBResidueRenumberer(object):
    def __init__(self, pdbpath, increment, offset):
        self.pdbpath = pdbpath
        self.offset = offset
        self.increment = increment
    
    def print_renumbered(self):
        """
        Iterate over residue chunks. A residue chunk is a list containing lines
        from the PDB file belonging to the same residue (based on its number).
        During this iteration, lines other than ATOM/HETATOM get printed
        to stdout (this is done by the residue_chunks() generator).
        Here, all residue chunks are printed with a modified residue
        number .. starting from OFFSET with an increment of 1.
        """
        res_number = self.offset
        for res_chunk in self.residue_chunks():
            # Although residue_chunks() is proposed to only yield lists of lines
            # describing atoms in one residue, there is one exception: TER
            # lines are also yielded, as they have to be re-numbered properly.
            if isinstance(res_chunk, basestring) and self.is_ter_line(res_chunk):
                print self.set_resnum_in_line(
                    res_chunk, res_number-1).rstrip()
                continue
            for residue_line in res_chunk:
                print self.set_resnum_in_line(
                    residue_line, res_number).rstrip()
                # outcomment the line before and uncomment the next line 
                # to test this script, via e.g. diff -wu
                # print residue_line.rstrip()
            res_number += self.increment                  

    def residue_chunks(self):
        """
        Iterate over all lines of a PDB file. Yield a chunk of lines belonging
        to the same residue, based on the original residue number. Print the
        rest of the lines in a fashion that keeps the order of the input file 
        (under the assumption that residue blocks are never interrupted by other
        lines)
        """
        last_resnum = self.get_first_residue_number()
        residue_block_started = False 
        residue_chunk = []
        with open(self.pdbpath) as f:
            for l in f:
                if not self.is_atom_line(l):
                    # Assume that there is no gap between lines for one residue.
                    # At this point, we detected a gap -> Yield what is in the
                    # chunk so far.
                    if residue_block_started:
                        yield residue_chunk
                        residue_chunk = []
                    residue_block_started = False
                    # The current line was identified to not belong to a residue.
                    # We flushed out the last residue chunk and can print this
                    # line now -- this keeps the order from the input file.
                    # But it turns out that we have to deal with TER lines, too,
                    # because they also have to be numbered correctly.
                    if self.is_ter_line(l):
                        yield l
                    else:
                        print l.rstrip()   
                    continue
                else:
                    residue_block_started = True
                    cur_resnum = self.get_resnum_from_line(l)
                    if last_resnum == cur_resnum:
                        residue_chunk.append(l)
                        continue
                    else:
                        last_resnum = cur_resnum
                        if len(residue_chunk):
                            yield residue_chunk
                        residue_chunk = [l]

    def set_resnum_in_line(self, line, residue_number):
        return ''.join([line[0:22], "%4i" % residue_number, line[26:]])   

    # Iterate over the PDB file to identify the first residue number
    def get_first_residue_number(self):
        with open(self.pdbpath) as f:
            for line in f:
                if self.is_atom_line(line):
                    return self.get_resnum_from_line(line)

    # http://www.wwpdb.org/documentation/format32/sect9.html
    # 23 - 26   Integer   resSeq       Residue sequence number.
    # first index: 0 -- slicing requires +1 on upper boundary
    def get_resnum_from_line(self, line):
        try:
            return int(line[22:26])
        except:       
            sys.exit("Residue index parsing error in line %s" % line)

    def is_atom_line(self, line):
        if line.startswith("ATOM") or line.startswith("HETATM"):
            return 1
        return 0
    
    def is_ter_line(self, line):
        if line.startswith("TER"):
            return 1
        return 0


if __name__ == "__main__":
    main()    

