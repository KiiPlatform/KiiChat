#KiiChat
KiiChat is simple chat application using the [KiiCloud](https://developer.kii.com/?locale=en).  
This demo project helps you learn how to use the KiiCloud.

##Requirements:
- Android Studio

##How to setup a project:

1. Go to [developer console](https://developer.kii.com) and create account/ app.
2. Replace APP_ID, APP_KEY, APP_SITE in ApplicationConst.java with yours created in step 1.

### Setup Push Notification
Please read this [guide][push-guide] and Setup GCM for you app.
Replace the SENDER_ID in ApplicationConst.java with yours.

[push-guide]:http://docs.kii.com/en/guides/android/quickstart/adding-kii-push-notification-to-your-application/adding-push-notification-gcm

In order to test push notification on emulater, please check following steps.

1. Make sure you are using emulator targetted on Google API
1. Add account on emulator as setting->account

<img src="screenshots/05.png">

### Setup Facebook integration
Please read this [guide][fb-guide] and configure Facebook appID/ appSecret in developer console.

[fb-guide]:http://docs.kii.com/en/guides/android/managing-users/social-network-integration/facebook

### Setup Analytics
Please read this [guide][analytics-guide] and replace AGGREGATION_RULE_ID in ApplicationConst.java with yours.

[analytics-guide]:http://documentation.kii.com/en/guides/android/managing-analytics/flex-analytics/analyze-event-data

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

##Resources:
APP Icon:  
[Free 3D Social Icons](https://www.iconfinder.com/icons/54521/about_balloon_baloon_bubble_chat_comment_comments_forum_help_hint_knob_mandarin_mandarine_orange_pin_snap_speech_tack_talk_tangerine_icon) - [By Aha-Soft](http://www.aha-soft.com/)  
[Creative Commons (Attribution-Share Alike 3.0 Unported)](http://creativecommons.org/licenses/by-sa/3.0/)  

Icons:  
https://github.com/Templarian/MaterialDesign  
  
Chat bubble 9pach:  
http://www.codeproject.com/Tips/897826/Designing-Android-Chat-Bubble-Chat-UI  

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



