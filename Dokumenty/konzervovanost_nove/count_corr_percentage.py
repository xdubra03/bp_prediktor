#!/usr/bin/python

from __future__ import division
import sys
import os
import math
import numpy as np

sequence_names = sys.argv[1]

names = []
with open(sequence_names,'r') as f:
    lines = f.readlines()
    for name in lines:
        names.append(name.strip('\n'))
f.close()

threshold = 1
for name in names:
    files = []
    for filename in os.listdir('./'+name):
        if filename.startswith(name+'_CORRELATION'):
            files.append(filename)

    for corr_file in files:
        with open('./'+name+'/'+corr_file,'r') as corr:
            items = []
            above_threshold = 0
            #above_percentage = 0
            #under_threshold = 0
            #under_percentage = 0
            #mean_value = 0
            lines = corr.readlines()
            for line in lines:
                items.append(float(line))
            for i in items:
                if(i >= threshold):
                    above_threshold +=1
            if(above_threshold > 0):
                is_conserved = 'korelovane'
            else:
                is_conserved = 'nekorelovane'
            #above_percentage = above_threshold / (len(items))
            #under_percentage = under_threshold / (len(items))
            print(corr_file+'  :'+ is_conserved)

        corr.close()
