from datetime import datetime

# Calculate the number of days left in your 
# deadline to learn python language

user_input = input('pleaese enter youlaer goal and deadline : ')
input_list = user_input.split(":")

goal = input_list[0]
deadline = input_list[1]

deadline_date = datetime.strptime(deadline,'%d/%m/%Y')
current_date = datetime.today()

print(f' U have {(deadline_date - current_date).days} days to learn python')