# HTS-ForgetPassword
A Lambda that will check if user is valid and allows user to set a new password if they had forgot.

# Responsibilities
- Take `username` input and check if it is present in the system. If present send out an OTP to their email, otherwise send response `user not found`.
- Take `username`, `password` & `otp`, and check if username is present and also code is not expired and password follows the password policy then save the new password;
else send appropriate response.

# Architecture Diagram
![Password Reset Architecture](https://user-images.githubusercontent.com/63947196/230845122-9a43b001-0662-468c-960f-c5fc40b03f13.jpg)
