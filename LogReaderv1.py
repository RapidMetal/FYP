import csv
import array as arr

def Avg(list):
    return sum(list)/len(list)

with open('MessageLog.csv') as csvfile:
    filereader = csv.reader(csvfile, dialect='excel')
    maxRows = 0
    resTimes = arr.array('i')

    for row in filereader:
        #print(','.join(row))
        if maxRows == 0 :
            maxRows += 1
            continue

        print(int(row[1]) - int(row[0]))
        resTimes.append(int(row[1]) - int(row[0]))
        maxRows += 1

    print("Average Response Time = " + str(Avg(resTimes)))
    

        