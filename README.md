# COVID-19 India Vaccination slot booking Application

## Contents
  - [Important Points](#important-points)
  - [Before you start](#before-you-start)
  - [Usage](#usage)
  - [API Support](#api-support)
  - [Troubleshoot](#troubleshoot)
  - [Further development ideas](#further-development-ideas)
  - [Developer Info](#developer-info)

## Important Points:
* <b>This is a proof of concept (POC) project. Use it at your own risk.</b>
* This application is an utility to use the Co-WIN Application platform. It does not seek to replace the plarform in any way.
* APIs used are referenced from Co-WIN APIs on API Setu. Link - https://apisetu.gov.in/public/api/cowin/
* This does not book slots automatically. It requires user authentication via OTP. (note: CAPTCHA is removed now)
* I used this utility as a personal interest and I do NOT endorse or condone, in any shape or form, automating any monitoring/booking tasks.
* There is no option to register new mobile or add beneficiaries. This can be used only after beneficiary has been added through the official app/site.
* User can check booking and vaccination status in Co-WIN portal or Arogya Setu and also download the certificate from there.
* Aim was to finish this ASAP to help with the process. As a result, code quality is not great. I will try and improve on it.


## Before you start
1. Check if you have java installed in your computer.
    * Open Command Prompt and run the below command:  
        `java -version`
    * If you do not have java installed, you can install it from <a href="https://www.oracle.com/in/java/technologies/javase-jre8-downloads.html" target="_blank">here</a>  
2. Download the application jar file from <a href="https://bit.ly/3z3LNYB" target="_blank">here</a>  

## Usage  
1. Copy the cowin-app-1.0.jar to a desired location on your computer.
2. Open Command Prompt and navigate to the directory location where you have saved the jar.  
3. Run the following command and keep the command prompt window open:  
      `java -jar covid19-vaccine-slot-booking-app.jar`
4. Verify version of application running: <a href="http://localhost:7747/api/app" target="_blank">Run in browser</a>    
5. To stop the application:
    * Open the command prompt window  
    * `Press Ctrl + C`
    * Close the command prompt window

## Ways in which you can use it  
1. Book appointment for a pin code or district with slots available
    - Using Pin Code:
        * Get beneficiary_reference_id using the GET beneficiary list API and pass them in the request body. 
        * If you want to book at a particular center, get center_id using the GET vaccine centers by Pin Code API
        * Use POST Book Appointment API with the above details and other required information to book your appointment slot.
    - Using District:     
        * Get beneficiary_reference_id using the GET beneficiary list API and pass them in the request body. 
        * Get district id using GET district details API by passing in state name 
        * If you want to book at a particular center, get center_id using the GET vaccine centers by District Id API
        * Use POST Book Appointment API with the above details and other required information to book your appointment slot.  
<br>        
2. Run a background job to search for centers and book appointment when a slot is available  
    * Use POST Schedule job API to place request.
    * Can add additional parameter to filter the search criteria.
    * When a slot is available, user is alerted with a notification sound and available slot details are displayed.
    * User will get maximum 20 seconds to make the selection. If no input, then current slot is skipped.
    * If a slot is selected, OTP is sent to mobile of user which needs to be entered when prompted.
    
## API Support
Refer to Postman documentation <a href="https://documenter.getpostman.com/view/16160319/TzY7eu5u" target="_blank">here</a>
  
Note: Enter OTP and press ENTER when prompted for OTP in the command prompt window.

## Troubleshoot
#### Problem 1:   
Application fails to start as port 7747 is already in use  
```
 ***************************
  APPLICATION FAILED TO START
  ***************************
  
  Description:
  
  Web server failed to start. Port 7747 was already in use.
``` 
##### Solution:  
Run the application with a different port number as below  
`java -Dserver.port=7748 -jar covid19-vaccine-slot-booking-app.jar`  
You will have to update the port number in the API requests.


## Further development ideas
* Support to download Appointment Slip
* Support to download Vaccination Certificate


## Developer Info
Rahul Pal  
Email: <rahulpal2109@gmail.com>  
<a href="https://github.com/rahulpal2109" target="_blank">GitHub</a>
<a href="https://www.linkedin.com/in/rahulpal91/" target="_blank">LinkedIn</a>
