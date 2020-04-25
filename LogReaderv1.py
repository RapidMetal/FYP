from decimal import *
import math
import csv
import array as arr

def Avg(list):
    return Decimal(sum(list))/Decimal(len(list))

#Set the decimal precision
getcontext().prec = 4

with open('MessageLog.csv') as csvfile:
    filereader = csv.reader(csvfile, dialect='excel')
    maxRows = 0
    finTime = Decimal(0)
    resTimes = arr.array('d')

    for row in filereader:
        #Skip the first row
        if maxRows == 0 :
            maxRows += 1
            continue

        #Add response times to the response time list
        resTimes.append(Decimal(row[1]) - Decimal(row[0]))

        #Find the finish time
        if Decimal(row[1]) > finTime :
            finTime = Decimal(row[1])

        maxRows += 1

    print("Average Response Time = " + str(Decimal(Avg(resTimes)/1000)))
    print("Finish time = " + str(Decimal(finTime/1000)))
    
    #Finding throughput
    throughput = Decimal(maxRows-1)/Decimal(finTime/1000)
    print("Throughput per sec = " + str(Decimal(throughput)))
    

        