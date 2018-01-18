#!/usr/bin/python
from sklearn import preprocessing 
from numpy import genfromtxt, savetxt
import pandas as pd
from sklearn.ensemble import RandomForestClassifier
from sklearn import preprocessing, svm
from sklearn.preprocessing import OneHotEncoder
from sklearn.externals import joblib
import random
import sys

def RandomForest(file):
	train = pd.read_csv(file)
	test =  pd.read_csv("RF_testsample.csv")

	cols = ['conservation','polaritychange','chargechange','hydroindexchange','secondarystruc','asa','sizechange']
	colsRes = ['class']

	trainArr = train.as_matrix(cols)
	trainRes = train.as_matrix(colsRes)
	trainRes = trainRes.ravel()
	rf = RandomForestClassifier(max_features=0.3,n_estimators=100,n_jobs=1,min_samples_leaf=50)
	rf.fit(trainArr,trainRes)

	testArr = test.as_matrix(cols)
	result = rf.predict(testArr)
	test['predicted4'] = result
	test.to_csv(sys.argv[2])
	print(test)
	

RandomForest(sys.argv[1])