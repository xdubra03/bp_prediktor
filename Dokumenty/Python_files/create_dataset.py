#!/usr/bin/python

import numpy as np
import pandas as pd
import random
import sys
import csv
def create_test_frame(self):
	data_frame = pd.read_csv("RF_dataset.csv")
	
	headers = ["conservation","polaritychange","chargechange","hydroindexchange","secondarystruc","asa","sizechange","class"]

	neg_data = data_frame.loc[data_frame['class'] == -1]
	pos_data = data_frame.loc[data_frame['class'] == 1]
	#print(neg_data)
	#print(len(neg_data))
	#print(pos_data)
	#print(len(pos_data))

	positive_values = data_frame['class'] == 1
	negative_values = data_frame['class'] == -1

	pos_frame = pos_data.ix[random.sample(pos_data.index,130)]
	neg_frame = neg_data.ix[random.sample(neg_data.index,100)]
	print(pos_frame)
	print(neg_frame)
	#pos_frame = pos_data.take(np.random.permutation(len(pos_data))[:80])
	#neg_frame = neg_data.take(np.random.permutation(len(neg_data))[:160])
	#headers = ["conservation","polaritychange","chargechange","hydroindexchange","secondarystruc","asa","sizechange","class"]
	pos_frame.to_csv(self,columns=headers)
	neg_frame.to_csv(self,mode='a',header=False)

def count_frame(self):
	data_frame = pd.read_csv(self)
	new_frame = data_frame[(data_frame['class'] == 1) & (data_frame['predicted1'] == 1)]
	neg_frame = data_frame[(data_frame['class'] == -1) & (data_frame['predicted1'] == -1)]
	#print(len(new_frame))
	#print(len(neg_frame))
	count = len(new_frame) + len(neg_frame)
	print(count)

def create_data(self):
	data_frame = pd.read_csv(sys.argv[1])
	#sluzi na vygenerovanie testovacieho datasetu o velkosti 250 zaznamov, zaznamy 
	#zmaze z povodneho suboru,vo vysledku ostane dataset o velkosti 1315 zaznamov
	#na tvorbu mensich datasetov
	
	sample_frame_indexes = random.sample(data_frame.index,250)
	sample_frame = data_frame.ix[sample_frame_indexes]
	print(sample_frame)
	new_frame = data_frame.drop(sample_frame_indexes)
	print(new_frame)



def count_results(self):
	csv_file = open("majority_voting3.csv",'r')
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
		else:
			res.append(-1)
		countNeg = 0
		countPos = 0

	frame = pd.read_csv(self)
	frame['predicted1'] = res
	frame.to_csv(self)
	print(res)

#create_test_frame(sys.argv[1])
#count_results(sys.argv[1])
create_data(sys.argv[1])