from __future__ import division
import numpy as np
import pandas as pd
import sys
from sklearn import svm
from sklearn.ensemble import RandomForestClassifier
import csv

def count_frame(self):
	data_frame = pd.read_csv(self)
	new_frame = data_frame[(data_frame['class'] == 1) & (data_frame['predicted1'] == 1)]
	neg_frame = data_frame[(data_frame['class'] == -1) & (data_frame['predicted1'] == -1)]
	#print(len(new_frame))
	#print(len(neg_frame))
	count = len(new_frame) + len(neg_frame)
	print(count)

def count_results(self):
	csv_file = open("voting.csv",'r')
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


train_frame0 = pd.read_csv(sys.argv[1])
test_file = sys.argv[2]
test_frame = pd.read_csv(test_file)


cols = ['conservation','polaritychange','chargechange','hydroindexchange','secondarystruc','asa','sizechange']

colsRes = ['class']
trainArr0 = train_frame0.as_matrix(cols)
trainRes0 = train_frame0.as_matrix(colsRes)
trainRes0 = trainRes0.ravel()



testArr = test_frame.as_matrix(cols)
testRes = test_frame.as_matrix(colsRes)
testRes = testRes.ravel()

rf = RandomForestClassifier(max_features=0.3,n_estimators=400,n_jobs=1,min_samples_leaf=50)
rf.fit(trainArr0,trainRes0)
result0 = rf.predict(testArr)

with open('voting.csv', 'w') as f:
	writer = csv.writer(f, delimiter=',')
	writer.writerows(zip(results0))

count_results(sys.argv[1])
count_frame(sys.argv[1])
