# in functions we imporve the code in variables file
# becouse function reduce code redundunt
# def keyword is used to define a function or routine

unit = 'minutes'
unit_calculation = 24*60


def days_tounits(day):
    print(f"{day} days are {day * unit_calculation} {unit}")


days_tounits(20)
days_tounits(30)
days_tounits(90)