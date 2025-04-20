# EdiMyDar ToDO android app with java
#### Video Demo:  <URL [HERE](https://youtu.be/mfIrvP7T0NM)>
#### Description (Explainig what each of the files you wrote for the project contains and does):
##### I.**package com.example.edimydar** :
###### 1.**Activites**:
###### 1.1.Home Activity:
- **Purpose**:The first activity shown to the user.
- **Functionality**:
    - A GetStarted button, that has an on-click listener.
    - this button triggers an intent (that launches another activity named Register which is the Activity responsible for Registring users).

###### 1.2.HomePage_MAIN:
- **Purpose**: The main home page activity has a navigation drawer and a bottom navigation bar. Handles switching between fragments.
- **Functionality**:
    - *(ACCESS ONLY BY AUTHENTICATED USERS)* it contains the 3 main fragments
        - My_Day
        - Tasks
        - AI Help
    - Uses ViewBinding.
    - Listener on nav View that manages the logic on how to navigate between fragments.
    - it uses a switch that replaces the current frame.
    - it checks and requests notification permission if the user accepts it guides the user to app settings to enable the permission.
    - it also update FireBase notification stat respectively.
    - By default, it replaces the current fragment with the my_day fragment;



###### 1.3.Login:
- **Purpose**: Handles user login. Validates credentials and navigates to the HomePage_MAIN activity upon successful login.
- **Functionality**:
    - it contains 3 button listeners:
        - on login
        - on forgotPassword
        - on sign-up
    - validates the inputs (empty, valid email format, valid password format,...)
    - login the user using fireBase.
    - if the user doesn't exist handle fireBase exceptions
    - also checks if the user is already logged in if so, redirect the user to The Main_Page

###### 1.4.Register:
- **Purpose**: Handles user registration. Validates inputs and saves user data to the database.
- **Functionality**:
    - it contains 2 button listens:
        - on Sign In
        - on Register
    - Validate fields :(empty, valid email and password format, and matching password and Confirmation password)
    - register the user in FireBase.
    - handling FireBase exceptions if the registration fails:
    - storing data in FireStore


###### 1.5.UsrProfileِActivity:
- **Purpose**: Manages user profile. Allows users to view and edit their profile details.
- **Functionality**:
    - it uses an image picker to pick the user in
    - img view listener to change the user profile:
        - Storing, loading this image as a Base64 in/from FireStore using the helper class Named: *ImageUtil*

    - using Glide to load this image into the image view;
    - loads user data from fireStore.
    - another listener on the update profile button that updates the user info in FireBase
    - listener on notification switch that requests the permissions and updates the notif stat  in fireStore
    - Re-authentification dialog if the user wants to change email
    - log out the user


###### 1.6 Other classes:
- **ForgotPWD**:
    - **Purpose**: Handles forogt password, send password reset link to the user email.
    - **Functionality**:
        - On Click listener on submit button.
        - checks email format.
        - if email exists send the user to success Activity and send them reset password link (FireBase handles reseting password)
        - else redirect the usr to failure Activity.
        - validate email format before proceding.
        - ensure input is not empty.

- **ForgotPWD_R_SUCCESS**: shows to the users steps how to reset the password, and password format, and also have an on click listener on return to login button

- **ForgotPWD_R_FAILURE**: shows to the users that the operation failed and give them the option to return to login using and on click listener on a button






###### 2.**Fragments**:

###### 2.1 myDayFragment:
- **Purpose**: Display and manage user Todays Tasks.
- **Functionality**:
    - fetchUserTasks from FireStore.
    - load Profile picture from fireStore.
    - onClickListener on the user-profile Block.
    - set greeting depends on the current time.
    - get current user data.
    - Handling recycler view that contains the tasks
    - show a dialog to add Today's tasks to FireStore and local data (custom dialog).
    - this dialog is triggered when clicking on + image view.
    - Going to the Register page if the user is not logged in.
    - Failure on loading user data results in logout except not user signed in results redirection to Register page.

###### 2.2 TaskFragment:
- **Purpose**: Manage general tasks, allow users to add/remove tasks and schedule notification(reminders) about tasks.
- **Functionality**:
    - show a dialog to add a Normal Task to FireStore and local data (custom dialog).
    - this dialog is triggered when clicking on + image View.
    - schedule notification based on time selected(Date and Time pickers in the custom dialog)
    - Handels alarm permission (check, request)
    - send notification
    - Handels Recycler View(tasks elements)
    - fetchUserData :(nbr of tasks, username,tasks) from fireStore
    - UpdatesTasks counter
    - Adding tasks cases :
        - Default: (no date nor time) : add it to Today Tasks without time
        - Today without time - > no notif
        - Today with time -> date: today | time: time specified
        - Date only -> date:Date specified | time:00.00
        - Time only-> date:today | time :time specified
        - Both exist -> date:Date specified | time: time specified
        - No Date no Time -> date is today no notif


###### 2.3 Ai Help
- **Purpose**:implements gemini api to help users to manage their tasks.
- **Functionality**:
    - REST API implmentations using OkHttp.
    - Recyler View to display chat messages (From Bot and user also)
    - on click listener to interact with the api









###### 3.**Adapters**:

###### 3.1 MessageAdapter:
- **Purpose**: Adapter for chat messages
- **Functionality**:
    - binds chat data to the Recycler View
    - implements the logic for every element in the recycler view
        - if the msg is sent by the user: make rightChat visible and leftChat Gone and set the user msg with the sent msg;
        - else make rightChat Gone and leftChat Visible and set the set the ai response to the bot msg;

###### 3.2 TaskRecylerViewAdapter:
- **Purpose**: Adapter for Todays Tasks recycler view
- **Functionality**:
    - binds tasks data to the Recycler View.
    - set the listener for checkBox.
        - if it's checked Delete the task, notify the adapter and updates fireStore data, and local data

###### 3.3 normalTaskRecycler_V_Adapter:
- **Purpose**: Adapter for Normal Tasks recycler view.
- **Functionality**:
    - create an interface that allows the Tasks fragment to updates the tasks counter when items are deleted
    - binds normal tasks data to the recycler view.
    - set the listener for checkBox.
        - if it's checked Delete the task, notify the adapter and updates fireStore data, and local data.



###### 4.**Models**:

###### 4.1  Message:
- Represents messages with attributes like:
    - String msg.
    - String sentByWho.
    - 2 final attributes :
        - String SENT_BY_USR = "usr"
        - String SENT_BY_BOT= "bot"


###### 4.2  DailyTask:
- Represents Todays Tasks with attributes like:
    - String title.
    - boolean checked.

###### 4.3 NormalTask(Extends DailyTask):
- Represents Normal Tasks with attributes like:
    - String title.
    - boolean checked.

    - String dueDate.
    - String dueTime.


###### 5.**Utils**:

###### 5.1 ImageUtils:
- Helps convertion from Base64 to String and vis-versa


###### 5.2 NotificationReceiver:
- BroadCast Receiver for handling tasks notifications, triggers notifications at specified times.(scheduled times )



###### 5. **Resources**:
- **Drawable**: containes :
    - icons.
    - images.
    - custom shapes.
    - cutom components like (Custom checkBox)

- **Layout**: containes (XML):
    - custom xml layout for every Activity/Fragment.
    - custom dialog.
    - custom recycler view.
    - custom chat item.

- **Menu**: Containes custom menu nav bar (XML)

- **MipMap**: Containes app icon.

- **Values**: Containes:
    - Colors, dimentions,strings,themes.
    - colors and diments and string added to avoid hardcoding stuff.
    - themes containes:
        - Custom Active Indicator.
        - Custom Button Style.
        - Custom CheckBox Style.

        - 3 themes :
            - app with action Bar.
            - app without action Bar.
            - Base theme




###### 6. **Local Properties**
- Containes api key

###### 7. **Dependecies**:
- google secrets gradle plugin.
- OkHttp
- ImagePicker
- Glide
- FireBase:
    - auth
    - FireStore
    - Analysis
    - BOM

###### 8. **Manifest permissions**:
- android.permission.INTERNET
- android.permission.POST_NOTIFICATIONS
- android.permission.SCHEDULE_EXACT_ALARM
