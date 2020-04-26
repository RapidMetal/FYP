from pathlib import Path
import fileinput
import logReaderModule
import os

attributesFilePath = Path('.').parent / 'SimulatorAttributes.java'
resultArray = ["AvgRes, MinRes, MaxRes, Throughput, AvgLoad, MinLoad, MaxLoad\n"]
#region Single Run for debug
# currentvalue = 10
#
# for line in fileinput.input(attributesFilePath,inplace=True):
#     line = line.rstrip('\r\n')
#     if line.startswith('    public static final int simulationRunningTime'):
#         print('    public static final int simulationRunningTime = ' + str(currentvalue) + ';')
#     else:
#         print(line)
#
# os.system('javac P2PSimulator.java')
# os.system('java P2PSimulator')
#
# currentResult = logReaderModule.ReadLog()
# resultArray.append(currentResult)
# print("Current Run Finished")
#endregion

lowerBound = 8
upperBound = 8
increment = 2
currentValue = lowerBound
iterationCount = int((upperBound-lowerBound) / increment) + 1

print("Running for", iterationCount, "iterations...")

for iteration in range(iterationCount):
    for line in fileinput.input(attributesFilePath,inplace=True):
        line = line.rstrip('\r\n')
        if line.startswith('    public static final int deviceCount'):
            print('    public static final int deviceCount = ' + str(currentValue) + ';')
        else:
            print(line)

    # os.system('javac ConsensusSimulator.java')
    # os.system('java ConsensusSimulator')

    currentResult = logReaderModule.ReadLog()
    resultArray.append(currentResult)
    print("Run", iteration+1, "finished.")
    currentValue = currentValue + increment

with open('output.csv','w') as outputFile:
    outputFile.writelines(iter(resultArray))

print("Completed successfully.")