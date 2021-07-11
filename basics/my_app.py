from os import error
from typing import ClassVar

cars = ['waaraad', 'dhayne', 'xaajiyad', 'booyad', 'cabdibile']

print(cars)
for car in cars:
    if(car == 'booyad'):
        print(car.upper())
    else:
        print(car.capitalize())

print('while loop...')

number =1
while number <= 10 :
    print(number)
    number +=1

