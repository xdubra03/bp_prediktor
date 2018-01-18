#!/usr/bin/python

import numpy as np
import pandas as pd
import random
import sys
import csv
def create_test_frame(self):
	data_frame = pd.read_csv(sys.argv[1])

	headers = ["correlation","conservation","polaritychange","chargechange","hydroindexchange","secondarystruc","asa","sizechange","class"]

	neg_data = data_frame.loc[data_frame['class'] == -1]
	pos_data = data_frame.loc[data_frame['class'] == 1]
	#print(neg_data)
	print(len(neg_data))
	#print(pos_data)
	print(len(pos_data))

	positive_values = data_frame['class'] == 1
	negative_values = data_frame['class'] == -1

	pos_frame = pos_data.ix[random.sample(pos_data.index,261)]
	neg_frame = neg_data.ix[random.sample(neg_data.index,300)]
	print(pos_frame)
	print(neg_frame)
	#pos_frame = pos_data.take(np.random.permutation(len(pos_data))[:80])
	#neg_frame = neg_data.take(np.random.permutation(len(neg_data))[:160])
	#headers = ["conservation","polaritychange","chargechange","hydroindexchange","secondarystruc","asa","sizechange","class"]
	pos_frame.to_csv(self,columns=headers)
	neg_frame.to_csv(self,mode='a',header=False)

create_test_frame(sys.argv[2])
