# in this python file
# we will implement User Clas
class User:
    def __init__(self,email,name,phone,job):
        self.email = email
        self.name = name
        self.phone = phone
        self.job = job

    def change_pass(self,newemail):
        self.email = newemail
        
    def change_job(self,newjob):
        self.job = newjob

    def get_user_information(self):
        return f' User {self.name} has Job of {self.job}'


user1 = User('user1@email','Abdi ahmed','898989','Developer')
print(user1.get_user_information())

user1.change_job('Teacher')
print(user1.get_user_information())


