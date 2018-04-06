# ![Liftim icon](app_icon.png) Open Liftim for Android

[![wercker status](https://app.wercker.com/status/c00052419a83c64f42b3e23fc22b9fdb/s/master "wercker status")](https://app.wercker.com/project/byKey/c00052419a83c64f42b3e23fc22b9fdb)

> *L*iftim *i*s *F*unctional *Ti*metable *M*anager

Liftim is a place to share information on your class. For example, timetable of
next class day, assignment, school event and so on.

If you aren't a developer, all feature of Liftim is available on [Lifti.me](https://lifti.me/)(only supports Japanese), official website for Liftim.
If you're a developer, feel free to contribute to this project by submitting issue
or opening pull request.

## Download

#### Release
Official release build is available on [Google Play Store](https://play.google.com/store/apps/details?id=com.chronoscoper.android.classschedule2).
Currently, release build can't be downloaded from countries but Japan.

#### Debug build
Distributed on DeployGate.  
[<img src="https://dply.me/0n2prq/button/large" alt="Try it on your device via DeployGate">](https://dply.me/0n2prq#install)  
Debug build is also ProGuarded in case release build fail.
ProGuard mapping file for latest CI automated build is [here](https://www.dropbox.com/s/ior5nlbprciy06j/liftim_mapping.txt?dl=0).  
Please note that the ProGuard mapping file is overwritten by next deploy and old mapping
file is not stored.

## FAQ

Q. What is the different between open source edition and official release build?  
A. In the open source version, server-side infrastructure is NOT provided by Chronoscope.  
  
Q. Where can I try it?  
A. Server-side system for testing exists on `http://lidev.starfree.jp/`.
Please note that contents there will be frequently cleared.

## Roadmap

- Import timetable from weekly timetable. <- IMPORTANT!
- Show registered timetable when timetable's date picked.
- Delete info completely after 1 month passed.

If you have skill to implement these and you intend to implement,
please ping me and implement!

## License

All files but noted are licensed under the Apache License Version 2.
Please see [LICENSE](LICENSE) file for more details.
