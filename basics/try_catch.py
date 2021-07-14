unit = 'minutes'
unit_calculation = 24*60


def days_tounits(day):
    return f"{day} days are {day * unit_calculation} {unit}"


# Python Try and Catch
def calculate_execute():
    try:
        number = int(user_input)
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
calculate_execute()
