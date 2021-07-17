import helper


# or we can import using
#  1- from helper import *
#  2- from helper import specific_method,specific_variable

# Implementation of helper file
# This method called Modulaizing Code

user_input = input(
    'Please enter the number of days, and units you want to convert:')
number_days_units = user_input.split(":")
print(number_days_units)

days_units = { 'days': number_days_units[0], 'unit': number_days_units[1]}

helper.calculate_execute(days_units['days'],days_units['unit'])

