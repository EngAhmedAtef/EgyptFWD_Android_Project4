# Location Reminder

A Todo list app with location reminders that remind the user to do something when he reaches a specific location. The app will require the user to create an account and login to set and access reminders.

## Login and Register
Allowed the user to login and register using FirebaseUI Authentication. 

## Reminders Screen
Created a screen that shows the user all of his reminders using a recycler view. The user can interact with any reminder by clicking and it will show further details of the chosen reminder in a different screen.

## Adding Reminders
The user can add a reminder by specifying the Title, Description and picking a location on the map using Google Maps Platform.
All necessary permissions were handled in addition to checking the location settings.

## Geofence
After successfully entering all the data the user can save the reminder. Upon saving, a geofence is created with a PendingIntent that is sent to a JobIntentService.
Whenever the user triggers the geofence by, for example, entering a location previously saved, a notification will be sent to the user.

## Testing
Wrote tests for the whole application; ViewModels, Coroutines, LiveData, Repository, DAO and Database.
Applied the concept of Doubles and Dependency Injection using Koin. 
Wrote JUnit tests, instrumented tests using Espresso, tested navigation using Mockito and wrote an End-To-End test for the process of adding a new reminder.