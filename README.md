#KiiChat
KiiChat is simple chat application using the [KiiCloud](https://developer.kii.com/?locale=en).  
This demo project helps you learn how to use the KiiCloud.

##Requirements:

- android-support-v4.jar (rev. 20)
- android-support-v7-appcompat.jar (rev. 20)
- google-play-services.jar


##How to setup a project:
You need to import following project to your workspace if you use Eclipse.  

    {SDK-DIR}/extras/android/support/v7/appcompat
    {SDK-DIR}/extras/google/google_play_services/libproject/google-play-services_lib

KiiChat uses GCM(Google Cloud Messaging) in order to send push notification and GCM needs google account.  
So you need to set up the google account on your emulator.  

1. Make sure you are using emulator targetted on Google API
1. Add account on emulator as setting->account


<img src="screenshots/05.png">

##Kii Features used:

- User Management
	- Sign Up
	- Sign In
	- Integrating Facebook Account
- Group Management
	- Creating Groups
	- Adding Group Members
	- Listing Groups
- Data Management
	- Setting ACL to a Bucket
	- Creating/Retrieving Objects
	- Querying for Objects
	- Uploading Object Bodies
	- Downloading Object Bodies
	- Receiving "Push to App" Notifications
- Push Notifications
	- "Push to User" Notifications
	- "Push to App" Notifications
- Analytics
	- Event data analytics

##Support:
If you have any questions, please feel free to ask at [community](http://community.kii.com/).


##Screenshots:

<table border="0">
  <tr>
    <td><img src="screenshots/01.png"></td>
    <td><img src="screenshots/02.png"></td>
  </tr>
  <tr>
    <td><img src="screenshots/03.png"></td>
    <td><img src="screenshots/04.png"></td>
  </tr>
</talbe>


