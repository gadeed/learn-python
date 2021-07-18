
# To drow this image 

# *
# **
# ***
# ****
# *****

# pseudo code 
# 1 - use while loop and start 1
# 2 - print * and then increment the iteration

i =1
astr = ''

for i in range(6):
    for k in range(i):
        astr += '*'
    astr += '\n'    
    i += 1

print(astr)    
