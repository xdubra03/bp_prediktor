#!/usr/bin/python

import numpy as np
import pandas as pd
import random
import sys
import csv

def create_data(self):
	data_frame = pd.read_csv(sys.argv[1])
	#sluzi na vygenerovanie testovacieho datasetu o velkosti 250 zaznamov, zaznamy
	#zmaze z povodneho suboru,vo vysledku ostane dataset o velkosti 1315 zaznamov
	#na tvorbu mensich datasetov

	sample_frame_indexes = random.sample(data_frame.index,250)
	sample_frame = data_frame.ix[sample_frame_indexes]
	print(sample_frame)
	sample_frame.to_csv('test250_data_new.csv')
	new_frame = data_frame.drop(sample_frame_indexes)
	print(new_frame)
	new_frame.to_csv('train_data_cleaned_new.csv')

create_data(sys.argv[1])
#create_test_frame(sys.argv[1])
#count_frame(sys.argv[1])
#edit_data(sys.argv[1])
#create_data(sys.argv[1])
#count_results(sys.argv[1])
