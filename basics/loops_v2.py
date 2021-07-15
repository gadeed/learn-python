# In here we implement loops v2

unit = 'minutes'
unit_calculation = 24*60


def days_tounits(day):
    if(day > 0):
        return f"{day} days are {day * unit_calculation} {unit}"
    elif(day == 0):
        return 'you entered 0!'


 # Python Loops v2


def calculate_execute():
    try:
        number = int(element)
        if number > 0:
            calculated = days_tounits(number)
            print(calculated)
        elif number == 0:
            print('you entered 0!')
        else:
            print('your entered negative number ')
    except ValueError:
        print('please enter digit value!!')



user_input = input('Please provide number of days:')
print(user_input.split(", "))
for element in user_input.split(","):
    calculate_execute()
