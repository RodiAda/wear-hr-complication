# Heart rate complication for Wear OS

Samsung Health's heart rate complication only reports the value to watch faces listed as safe in its manifest (none),
so third-party faces (e.g. patched Pixel faces) get the label "Heart rate" instead of a number. This app provides a
`RANGED_VALUE` / `SHORT_TEXT` heart rate complication fed by Health Services passive monitoring, with a 30 s sensor
measurement as fallback when data is older than 10 minutes.

## Build

```
echo "sdk.dir=$PWD/tools/sdk" > local.properties
tools/gradle-9.9.0/bin/gradle assembleRelease
adb install -r app/build/outputs/apk/release/app-release.apk
for p in android.permission.health.READ_HEART_RATE android.permission.health.READ_HEALTH_DATA_IN_BACKGROUND \
         android.permission.BODY_SENSORS android.permission.BODY_SENSORS_BACKGROUND; do
  adb shell pm grant io.github.rodiada.hrcomplication $p
done
```

`tools/` holds Android SDK command-line tools (platform 36, build-tools 36.0.0) and Gradle 9.9.0, not committed.
Signing key: `keys/hr.jks`, alias `hr`, password from `KS_PASS` (stored in Bitwarden).
