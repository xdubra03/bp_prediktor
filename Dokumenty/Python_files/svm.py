#!/usr/bin/python
from __future__ import division
import numpy as np
import pandas as pd
import sys
from sklearn import svm
from sklearn.ensemble import RandomForestClassifier
import csv


#train_file = sys.argv[1] 
train_frame0 = pd.read_csv("train200_200.csv")
train_frame1 = pd.read_csv("train250_250.csv")
train_frame2 = pd.read_csv("train200_250.csv")
train_frame3 = pd.read_csv("train150_150.csv")
train_frame4 = pd.read_csv("train100_130.csv")


test_file = sys.argv[1]
test_frame = pd.read_csv(test_file)



#test_file1 = "RF_data"
#test_frame1 = pd.read_csv(test_file)


cols = ['conservation','polaritychange','chargechange','hydroindexchange','secondarystruc','asa','sizechange']
cols1 = ['conservation','polaritychange','hydroindexchange','secondarystruc','asa','sizechange']
cols2 = ['conservation','polaritychange','chargechange','hydroindexchange','secondarystruc','sizechange']
cols3 = ['conservation','polaritychange','chargechange','secondarystruc','sizechange']

colsRes = ['class']
trainArr0 = train_frame0.as_matrix(cols)
trainRes0 = train_frame0.as_matrix(colsRes)
trainRes0 = trainRes0.ravel()

trainArr1 = train_frame1.as_matrix(cols)
trainRes1 = train_frame1.as_matrix(colsRes)
trainRes1 = trainRes1.ravel()

trainArr2 = train_frame2.as_matrix(cols)
trainRes2 = train_frame2.as_matrix(colsRes)
trainRes2 = trainRes2.ravel()

trainArr3 = train_frame4.as_matrix(cols1)
trainRes3 = train_frame4.as_matrix(colsRes)
trainRes3 = trainRes3.ravel()
#200 200 bez asa
trainArr4 = train_frame0.as_matrix(cols2)
trainRes4 = train_frame0.as_matrix(colsRes)
trainRes4 = trainRes4.ravel()
#150 150 bez asa/hydro
trainArr5 = train_frame3.as_matrix(cols3)
trainRes5 = train_frame3.as_matrix(colsRes)
trainRes5 = trainRes5.ravel()

testArr = test_frame.as_matrix(cols)
testRes = test_frame.as_matrix(colsRes)
testRes = testRes.ravel()

testArr1 = test_frame.as_matrix(cols1)
testRes1 = test_frame.as_matrix(colsRes)
testRes1 = testRes1.ravel()

testArr2 = test_frame.as_matrix(cols2)
testRes2 = test_frame.as_matrix(colsRes)
testRes2 = testRes2.ravel()

testArr3 = test_frame.as_matrix(cols3)
testRes3 = test_frame.as_matrix(colsRes)
testRes3 = testRes3.ravel()

print("train ")
#print(trainArr)
print ()
print ("train features:")
#print (trainRes)


#correct = 0
classifier = svm.SVC(kernel = 'linear',class_weight={1: .5, -1: .5 })
classifier.fit(trainArr0, trainRes0)
results0 = classifier.predict(testArr)

classifier = svm.SVC(kernel = 'linear',class_weight={1: .5, -1: .5 })
classifier.fit(trainArr1, trainRes1)
results1 = classifier.predict(testArr)

classifier = svm.SVC(kernel = 'linear',class_weight={1: .45, -1: .55 })
classifier.fit(trainArr2, trainRes2)
results2 = classifier.predict(testArr)
#res_frame['predicted1'] = results
#res_frame.to_csv("majority_voting.csv")
#print(results)

rf = RandomForestClassifier(max_features=0.3,n_estimators=400,n_jobs=1,min_samples_leaf=50)
rf.fit(trainArr0,trainRes0)
result0 = rf.predict(testArr)

rf = RandomForestClassifier(max_features=0.3,n_estimators=400,n_jobs=1,min_samples_leaf=50)
rf.fit(trainArr2,trainRes2)
result2 = rf.predict(testArr)

rf = RandomForestClassifier(max_features=0.3,n_estimators=400,n_jobs=1,min_samples_leaf=50)
rf.fit(trainArr3,trainRes3)
result3 = rf.predict(testArr1)

rf = RandomForestClassifier(max_features=0.3,n_estimators=400,n_jobs=1,min_samples_leaf=50)
rf.fit(trainArr4,trainRes4)
result4 = rf.predict(testArr2)

rf = RandomForestClassifier(max_features=0.3,n_estimators=400,n_jobs=1,min_samples_leaf=50)
rf.fit(trainArr5,trainRes5)
result5 = rf.predict(testArr3)



#test_frame['predicted4'] = result
#test_frame.to_csv(sys.argv[2])

with open('majority_voting3.csv', 'w') as f:
	writer = csv.writer(f, delimiter=',')
	writer.writerows(zip(results0,results1,results2,result0,result2,result3,result4,result5))

#classifier = svm.SVC(kernel = 'linear',class_weight={1: .45, -1: .55 })
#classifier.fit(trainArr, trainRes)
#results = classifier.predict(testArr)
#test_frame['predicted2'] = results
#test_frame.to_csv(sys.argv[2])
#print(test_frame)


