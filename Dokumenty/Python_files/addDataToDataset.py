__author__ = 'xodar'

from Bio.PDB import *
import glob, shutil, re


input = open("/home/xodar/PycharmProjects/Protherm_refined/dataset_cleaned_pokus3.csv", "r")
output = open("/home/xodar/PycharmProjects/Protherm_refined/dataset_cleaned_final.csv", "w")

for line in input.readlines():
    line = line[0:-1]
    parts = line.split(",")
    p = PDBParser()
    structure = p.get_structure(parts[0], "/home/xodar/PycharmProjects/Protherm_refined/PDB/new/" + parts[0] + ".pdb")
    model = structure[0]
    dssp = DSSP(model, "/home/xodar/PycharmProjects/Protherm_refined/PDB/new/" + parts[0] + ".pdb")
    try:
        a_key = list(dssp.keys())[int(parts[2]) - 1]
        asa = str(dssp[a_key][3])
        sec = str(dssp[a_key][2])
        output.write(line + "," + sec + "," + asa + "\n")
    except:
        output.write(line + ",,\n")

