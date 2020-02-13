# android_packages_apps_MacRandomizer

Add to system/sepolicy/system_app.te
```
allow system_app nvdata_file:dir { search write add_name };
allow system_app nvdata_file:file { open read getattr create write };
allow system_app nvram_data_file:lnk_file { read };
```

Add to system/sepolicy/file_contexts
```
/system/bin/install-mac.sh u:object_r:update_mac_exec:s0
```

Add to init.*.rc
```
service update_mac /system/bin/install-mac.sh
    class main
    disabled
    oneshot

on property:init.svc.nvram_daemon=stopped
    start update_mac
```

Place install-mac.sh in /system/bin/install-mac.sh

Copy update_mac.te to system/sepolicy/
