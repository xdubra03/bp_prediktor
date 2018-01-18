#!/usr/bin/python

import numpy as np
import pandas as pd
import random
import sys
import csv

def count_results(self):
	csv_file = open("majority_voting4_new.csv",'r')
	data_frame = csv.reader(csv_file,delimiter = ',')
	res = list()
	countNeg = 0
	countPos = 0
	for row in data_frame:
		for item in row:
			if(item == '-1'):
				countNeg +=1
			else:
				countPos +=1
		if(countPos > countNeg):
			res.append(1)
		elif(countPos < countNeg):
			res.append(-1)
		else:
			res.append(1)

		countNeg = 0
		countPos = 0

	print(res)
	frame = pd.read_csv(self)
	frame['predicted1'] = res
	frame.to_csv(self)
	print(res)

count_results(sys.argv[1])
