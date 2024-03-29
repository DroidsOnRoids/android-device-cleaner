# android-device-cleaner

<img src="logo.webp" width="60%" height="60%" />

Tool for cleaning up Android devices.
Designed for restoring state of devices used for QA on [Device Farmer](https://devicefarmer.io/).
By default STF uninstalls apps installed during session only on clean end of usage. 

## Performed actions
* unneeded apps uninstallation, except those listed in file pointed by `EXCLUDED_PACKAGES_LIST_PATH` environment variable
* external storage (`/sdcard/`) wiping
* temporary directory (`/data/local/tmp`) wiping
* force stopping all apps

## Project components

### `device-cleaner`
Contains all the logic, can be used without Device Farmer.

### `stf-app`
Interface to [Device Farmer / Open STF Connect step](https://github.com/DroidsOnRoids/bitrise-step-openstf-connect) on [Bitrise](https://bitrise.io).

## Required environment variables
* `ANDROID_HOME` - should point to Android SDK root dir containing valid platform-tools, provided by Android stack on Bitrise
* `STF_DEVICE_SERIAL_LIST` - should contain devices serial numbers as a JSON array, provided by Open STF Connect step
* `EXCLUDED_PACKAGES_LIST_PATH` - should be a path to file with packages excluded from uninstallation (one per line) 

## Usage
`./gradlew run` 
