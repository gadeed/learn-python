# in functions we imporve the code in variables file
# becouse function reduce code redundunt
# def keyword is used to define a function or routine

unit = 'minutes'
unit_calculation = 24*60


def days_tounits(day):
    if(day > 0):
        return f"{day} days are {day * unit_calculation} {unit}"
    elif(day == 0):
        return 'you entered 0!'


def calculate_execute():
    if(user_input.isdigit()):
        calculated = days_tounits(int(user_input))
        print(calculated)
    else:
        print('please enter digit value!!')


user_input = input('Please provide number of days:')
calculate_execute()
