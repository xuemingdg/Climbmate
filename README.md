# Climbmate

**Climbmate** is a small demo Android application with functionalities which climbers may find handy in their daily activity of mountain climbing.  It has a compass that based on magnetic and accelerometer sensor. Also,  current pressure and altitude will be demonstrated if the mobile device has a built-in pressure sensor. Moreover, it has a pedometer that based on [jiahongfei's open source library](https://github.com/jiahongfei/TodayStepCounter). The step count is cleared on daily basis. The user login system is simulated with a local database via SQLite. First-time login will finish the register process.



---

**3rd party libraries**

- Butterknife
- Immersionbar
- StackBlur
- TodayStepCounter

**Butterknife** was used to bind views and click events with ease. **Immersionbar** provides a better full screen experience where the background of the toolbar is blended with the that of the application's user interface. **StackBlur** was imported to apply Gaussian Blur to the background of the application. **TodayStepCounter** is a comparatively stable pedometer project that is compatible with most of the mobile device manufacturers.



*Note: this application was run and tested on Meizu 6T in Android version 7.0.*