#!/usr/bin/python3

import os
import sys

file = sys.argv[1]
f = open(file,"r+")

lines = f.readlines()
for line in lines:
	words = line.split()
	for word in words:
		if word[0][0] == ">":
			print (line)