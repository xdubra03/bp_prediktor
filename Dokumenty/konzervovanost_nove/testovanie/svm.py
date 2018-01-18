#!/usr/bin/python
from __future__ import division
import numpy as np
import pandas as pd
import sys
from sklearn import svm
from sklearn.ensemble import RandomForestClassifier
import csv


#train_file = sys.argv[1]
train_frame0 = pd.read_csv("train200_200_new1.csv")
train_frame1 = pd.read_csv("train250_250_new1.csv")
#train_frame2 = pd.read_csv("train200_250_new1.csv")
train_frame3 = pd.read_csv("test261_300.csv")
train_frame4 = pd.read_csv("train100_150_new1.csv")
train_frame5 = pd.read_csv("train130_100_new1.csv")


test_file = sys.argv[1]
test_frame = pd.read_csv(test_file)



#test_file1 = "RF_data"
#test_frame1 = pd.read_csv(test_file)


cols = ['correlation','conservation','polaritychange','chargechange','hydroindexchange','secondarystruc','asa','sizechange']
cols1 = ['correlation','conservation','polaritychange','hydroindexchange','secondarystruc','asa','sizechange']
cols2 = ['correlation','conservation','polaritychange','chargechange','hydroindexchange','secondarystruc','sizechange']
cols3 = ['correlation','conservation','polaritychange','chargechange','secondarystruc','sizechange']

colsRes = ['class']
#dataset 261,300 vsetky parametre
#dataset 150 150 vsetky parametre
trainArr0 = train_frame3.as_matrix(cols)
trainRes0 = train_frame3.as_matrix(colsRes)
trainRes0 = trainRes0.ravel()

#dataset 100 150 vsetky parametre
trainArr1 = train_frame4.as_matrix(cols)
trainRes1 = train_frame4.as_matrix(colsRes)
trainRes1 = trainRes1.ravel()

#dataset 130 100 vsetky parametre
trainArr2 = train_frame5.as_matrix(cols)
trainRes2 = train_frame5.as_matrix(colsRes)
trainRes2 = trainRes2.ravel()

#dataset 150 200 bez hydroindexu
trainArr3 = train_frame5.as_matrix(cols1)
trainRes3 = train_frame5.as_matrix(colsRes)
trainRes3 = trainRes3.ravel()

#datset 261 300 bez asa
trainArr4 = train_frame3.as_matrix(cols2)
trainRes4 = train_frame3.as_matrix(colsRes)
trainRes4 = trainRes4.ravel()

#200 200 bez asa/hydro
trainArr5 = train_frame0.as_matrix(cols3)
trainRes5 = train_frame0.as_matrix(colsRes)
trainRes5 = trainRes5.ravel()

#daatset 250 250 bez asa/hydro
trainArr6 = train_frame1.as_matrix(cols3)
trainRes6 = train_frame1.as_matrix(colsRes)
trainRes6 = trainRes6.ravel()

#daatset 250 250
trainArr8 = train_frame1.as_matrix(cols)
trainRes8 = train_frame1.as_matrix(colsRes)
trainRes8 = trainRes8.ravel()

#261 300 bez hydro/asa
trainArr7 = train_frame3.as_matrix(cols3)
trainRes7 = train_frame3.as_matrix(colsRes)
trainRes7 = trainRes7.ravel()

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


#correct = 0 {1: .43, -1: .57 }
classifier = svm.SVC(kernel = 'linear',class_weight={1: .43, -1: .57 })
classifier.fit(trainArr1, trainRes1)
results0 = classifier.predict(testArr)
#{1: .47, -1: .53 }
classifier = svm.SVC(kernel = 'linear',class_weight={1: .47, -1: .53 })
classifier.fit(trainArr0, trainRes0)
results1 = classifier.predict(testArr)

"""classifier = svm.SVC(kernel = 'linear',class_weight={1: .43, -1: .57 })
classifier.fit(trainArr2, trainRes2)
results2 = classifier.predict(testArr)"""
#res_frame['predicted1'] = results
#res_frame.to_csv("majority_voting.csv")
#print(results)

rf = RandomForestClassifier(max_features=0.4,n_estimators=1000,n_jobs=1,min_samples_leaf=50)
rf.fit(trainArr2,trainRes2)
result0 = rf.predict(testArr)

rf = RandomForestClassifier(max_features=0.4,n_estimators=1000,n_jobs=1,min_samples_leaf=50)
rf.fit(trainArr4,trainRes4)
result2 = rf.predict(testArr2)

rf = RandomForestClassifier(max_features=0.4,n_estimators=1000,n_jobs=1,min_samples_leaf=50)
rf.fit(trainArr5,trainRes5)
result3 = rf.predict(testArr3)

rf = RandomForestClassifier(max_features=0.4,n_estimators=1000,n_jobs=1,min_samples_leaf=50)
rf.fit(trainArr6,trainRes6)
result4 = rf.predict(testArr3)

rf = RandomForestClassifier(max_features=0.4,n_estimators=1000,n_jobs=1,min_samples_leaf=50)
rf.fit(trainArr7,trainRes7)
result5 = rf.predict(testArr3)

#rf = RandomForestClassifier(max_features=0.3,n_estimators=400,n_jobs=1,min_samples_leaf=50)
#rf.fit(trainArr8,trainRes8)
#result7 = rf.predict(testArr)



#test_frame['predicted4'] = result
#test_frame.to_csv(sys.argv[2])

with open('majority_voting4_new.csv', 'w') as f:
	writer = csv.writer(f, delimiter=',')
	writer.writerows(zip(results0,results1,result0,result2,result3,result4,result5))

#classifier = svm.SVC(kernel = 'linear',class_weight={1: .5, -1: .5 })
#classifier.fit(trainArr0, trainRes0)
#results = classifier.predict(testArr)
#rf = RandomForestClassifier(max_features=0.3,n_estimators=400,n_jobs=1,min_samples_leaf=50)
#rf.fit(trainArr0,trainRes0)
#results = rf.predict(testArr)
#test_frame['predicted1'] = results
#test_frame.to_csv(sys.argv[1])
#print(test_frame)
