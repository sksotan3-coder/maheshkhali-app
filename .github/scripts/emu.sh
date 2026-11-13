#!/usr/bin/env bash
PKG=com.st.sotan.amarmaheshkhali
echo "== Android version: $(adb shell getprop ro.build.version.release) (API $(adb shell getprop ro.build.version.sdk))"
adb install -r app/build/outputs/apk/release/app-release.apk || { echo INSTALL_FAILED; exit 1; }
adb logcat -c
echo "== Cold start time"
adb shell am start -W -n $PKG/.MainActivity | grep -E "Status|TotalTime|WaitTime"
sleep 8
echo "== Memory after start"
adb shell dumpsys meminfo $PKG | grep -E "TOTAL PSS|TOTAL:|Java Heap|Native Heap" | head -5
echo "== Monkey (3000 random events)"
adb shell monkey -p $PKG --throttle 120 --pct-syskeys 0 --ignore-crashes --monitor-native-crashes -s 7 -v 3000 > monkey.txt 2>&1
tail -6 monkey.txt
echo "== Crashes in logcat"
adb logcat -d -b crash | head -60
adb logcat -d | grep -E "FATAL EXCEPTION|ANR in|Seeder" | head -20
echo "== Memory after monkey"
adb shell dumpsys meminfo $PKG | grep -E "TOTAL PSS|TOTAL:" | head -3
echo "== Offline check: INTERNET permission"
adb shell dumpsys package $PKG | grep -E "permission|INTERNET" | head -10
echo "== Instrumented UI test"
adb uninstall $PKG >/dev/null
gradle connectedDebugAndroidTest --stacktrace
echo UI_TEST_EXIT=$?
echo "== Player log"
adb logcat -d | grep -E "VideoPlayer|MediaPlayer|MediaCodec|VideoView|Codec2" | head -60
