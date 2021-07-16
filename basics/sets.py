# In here we implement loops v2

unit = 'minutes'
unit_calculation = 24*60


def days_tounits(day):
    if(day > 0):
        return f"{day} days are {day * unit_calculation} {unit}"
    elif(day == 0):
        return 'you entered 0!'

 # Python Sets 


# def calculate_execute():
#     try:
#         number = int(element)
#         if number > 0:
#             calculated = days_tounits(number)
#             print(calculated)
#         elif number == 0:
#             print('you entered 0!')
#         else:
#             print('your entered negative number ')
#     except ValueError:
#         print('please enter digit value!!')


# user_input = input('Please provide number of days:')

# print(user_input.split(", "))
# print(type(user_input.split(", ")))
# print(set(user_input.split(", ")))
# print(type(set(user_input.split(", "))))

# for element in set(user_input.split(",")):
#     calculate_execute()

my_list1 = ['January', 'Febraury', 'March','January']
my_list2 = {'January', 'Febraury', 'March', 'January','January'}


print(my_list1)
print(my_list2)

# print(set(my_list))

# my_set = {'Sabti','Axad','Isniin','Talaada'}

# print(my_set)

# my_set.add('Jimce')

# print(my_set)






