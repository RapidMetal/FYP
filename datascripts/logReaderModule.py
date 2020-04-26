import numpy as np
import pandas as pd
from pathlib import Path
import csv

def Avg(list):
    return sum(list)/len(list)

def ReadLog():
    #region *** Calc Node Busy Time; EventLog.csv ***
    # Get Event Log Path
    eventLogPath = Path('.').parent / 'EventLog.csv'

    # Read Event Log into Pandas Dataframe
    eventLog = pd.read_csv(eventLogPath, delimiter=',')
    simulationDuration = eventLog.iloc[-1, 0]

    # Extract needed data
    data = eventLog.iloc[:, [1,2,3,5]]
    # indices 0 -> InitSrc, 1 -> FinalDest, 2 -> MsgSrc, 3 -> MsgDest

    # Get number of nodes
    nodeCount = data.iloc[:, 0].values.max() + 1

    # Make Array for storing node busy times
    nodeArray = []
    for i in range(0,nodeCount):
        nodeArray.append(0)

    # Iterate through DataFrame
    # Delays:
    # Request Generation Delay: 40
    # Request Process Delay: 100
    # Request Forward Delay: 20
    for i in range(0, len(data)):
        #Add delay for sender
        if data.iloc[i,0] == data.iloc[i,1]:
            #Direct Node Processing
            nodeArray[data.iloc[i,0]] += data.iloc[i,2]
        elif data.iloc[i,0] == data.iloc[i,2]:
            #Request Generation
            nodeArray[data.iloc[i,2]] += 40
        else:
            #Request Forwarding
            nodeArray[data.iloc[i,2]] += 20
        
        #Add delay for receiver if processing
        if data.iloc[i,1] == data.iloc[i,3]:
            nodeArray[data.iloc[i,3]] += 100
    #endregion

    #region *** Calc Throughput and Response time; EventLog.csv ***
    messageLogPath = Path('.').parent / 'MessageLog.csv'
    with open(messageLogPath) as csvfile:
        filereader = csv.reader(csvfile, dialect='excel')
        maxRows = 0
        finTime = 0
        minTime = 10**6
        maxTime = 0
        resTimes = []

        for row in filereader:
            #Skip the first row
            if maxRows == 0 :
                maxRows += 1
                continue
            #Add response times to the response time list
            resTimes.append(int(row[1]) - int(row[0]))
            
            ## Modify min and max time values
            if resTimes[-1] > maxTime:
                maxTime = resTimes[-1]
            if resTimes[-1] < minTime:
                minTime = resTimes[-1]

            #Find the finish time
            if int(row[1]) > finTime :
                finTime = int(row[1])

            maxRows += 1
    #endregion

    #Calculate Response Time and throughput
    #Convert from ms to s
    avgResponseTime = Avg(resTimes)/1000
    minResponseTime = minTime/1000
    maxResponseTime = maxTime/1000
    throughput = (maxRows-1)/(finTime/1000)

    #Calculate Average and Max
    avgBusyTime = Avg(nodeArray) / simulationDuration * 100
    maxBusyTime = max(nodeArray) / simulationDuration * 100
    minBusyTime = min(nodeArray) / simulationDuration * 100

    #Print Response Time(s), Throughput(s), avg Busy Time(%), max Busy time(%)
    return str(round(avgResponseTime,2)) + ', ' + str(round(minResponseTime,2)) + ', ' + str(round(maxResponseTime,2)) + ', ' + str(round(throughput,2)) + ', ' + str(round(avgBusyTime,2)) + ', ' + str(round(minBusyTime,2)) + ', ' + str(round(maxBusyTime,2)) + '\n'