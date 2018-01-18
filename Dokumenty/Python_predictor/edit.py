#!/usr/bin/python2
import sys

file = sys.argv[1]
start_position = int(sys.argv[2])
dlzka = int(sys.argv[3])
f = open(file,"r+")
position = start_position
gap = 0
tmp = 0
#i = start_position
count = 0
i = start_position

lines = f.readlines()
for line in lines:
	words = line.split()
	for word in words:
		while count < dlzka:
			if word[i-2] != "-":
				count +=1
			i+=1
			position+=1        		

print i
print count 
print "position:%d" %int(position-1)



"""		for i in range(start_position,start_position+dlzka):
				if word[i] == "-":
					tmp +=1				
for line in lines:
	words = line.split()
	for word in words:
		for i in range(start_position,start_position+dlzka+tmp):
				if word[i] == "-":
					gap +=1		
print gap				
result = start_position + dlzka +gap
print result -1 """