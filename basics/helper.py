

def days_tounits(day, unit):
    if(unit == 'hours'):
        return f"{day} days are {day * 24} {unit}"
    else:
        return f"{day} days are {day * 21*60} {unit}"


# Python Dictionaries
# Implementation

def calculate_execute(days, units):
    try:
        number = int(days)
        if number > 0:
            calculated = days_tounits(number, units)
            print(calculated)
        elif number == 0:
            print('you entered 0!')
        else:
            print('your entered negative number ')
    except:
        print('please enter digit value!!')
