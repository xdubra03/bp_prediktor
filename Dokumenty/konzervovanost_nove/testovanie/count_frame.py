#!/usr/bin/python

import numpy as np
import pandas as pd
import random
import sys
import csv
from sklearn.metrics import matthews_corrcoef
from sklearn.metrics import classification_report

def count_frame(self):
	data_frame = pd.read_csv(self)
	new_frame = data_frame[(data_frame['class'] == 1) & (data_frame['predicted1'] == 1)]
	neg_frame = data_frame[(data_frame['class'] == -1) & (data_frame['predicted1'] == -1)]
	#print(len(new_frame))
	#print(len(neg_frame))
	count = len(new_frame) + len(neg_frame)
	print(count)

def compute_mcc(self):
	data_frame = pd.read_csv(self)
	#print(data_frame['class'])
	testarr = data_frame['class'].values
	trainarr = data_frame['predicted1'].values
	mcc = matthews_corrcoef(testarr, trainarr)
	target_names = ['1', '-1']
	print(classification_report(testarr,trainarr, target_names=target_names))
	print(mcc)


compute_mcc(sys.argv[1])
#count_frame(sys.argv[1])
