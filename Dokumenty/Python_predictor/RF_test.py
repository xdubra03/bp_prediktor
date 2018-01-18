#!/usr/bin/python
from sklearn import preprocessing 
from numpy import genfromtxt, savetxt
import pandas as pd
from sklearn.ensemble import RandomForestClassifier
from sklearn import preprocessing, svm
from sklearn.preprocessing import OneHotEncoder
from sklearn.externals import joblib
import random


def RandomForest(file):
	train = pd.read_csv(file)
	test =  pd.read_csv("RF_testsample1.csv")

	cols = ['conservation','polaritychange','chargechange','hydroindexchange','secondarystruc','asa','sizechange']
	colsRes = ['class']

	trainArr = train.as_matrix(cols)
	trainRes = train.as_matrix(colsRes)
	trainRes = trainRes.ravel()
	rf = RandomForestClassifier(n_estimators=150,n_jobs=1,min_samples_leaf=50)
	rf.fit(trainArr,trainRes)

	testArr = test.as_matrix(cols)
	result = rf.predict(testArr)
	test['predicted'] = result
	
	print(test)
	

RandomForest("RF_testsample5.csv")
	#if(result == -1):
	#	print('Destabilizing mutation')
	#else:
	#	print('Stabilizing mutation')





#lines =list(open('RF_samples.csv'))
#random_choice = random.sample(lines,670)
#with open("RF_testsample5.csv", "wb") as sink:
#    sink.write("".join(random_choice))

#linesCopy = [line for idx, line in enumerate(lines) if idx not in random_choice]
#lines[:] = linesCopy
#with open("RF_samples.csv", "wb") as sink:
#    sink.write("".join(lines))

#with open("RF_samples.csv", 'w') as output_file:
#	output_file.writelines(line  for line in lines if line not in random_choice)



#line = random_line("RF_data.csv")
#print(line)
#	train = pd.read_csv("RF_data.csv")
#	test =  pd.read_csv("mutation_record.csv")
##
#	cols = ['conservation','polaritychange','chargechange','hydroindexchange','secondarystruc','asa','sizechange']
#	colsRes = ['class']
#
#	trainArr = train.as_matrix(cols)
#	trainRes = train.as_matrix(colsRes)
#	trainRes = trainRes.ravel()
#	rf = RandomForestClassifier(n_estimators=100,n_jobs=1,min_samples_leaf=50)
#	rf.fit(trainArr,trainRes)

#	testArr = test.as_matrix(cols)
#	result = rf.predict(testArr)
#	if(result == -1):
#		print('Destabilizing mutation')
#	else:
#		print('Stabilizing mutation')
#
#