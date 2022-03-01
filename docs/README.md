# Know bugs

None

# Recording locations

Android has several storage locations:

  * /data/data/ - internal user storage on new android
  * /storage/self/primary/ - internal storage
  * /storage/emulated/0/ - external storage (sdcard formated as internal or emulated external)
  * /storage/1D13-0F08/ - sdcard formated as portable

Some Androids can have more:

/storage/sdcard - emulated external sdcard
/data/user/0/ - internal user storage
/sdcard - link for default extenral storage /storage/emulated/0/

Plus application / user specified folders (all combinations of):

  * .../Android/data/com.github.axet.audiorecorder/files
  * .../com.github.axet.audiorecorder/files
  * .../Audio Recorder

For example:

  * /data/data/com.github.axet.audiorecorder/files
  * /data/user/0/com.github.axet.audiorecorder/files
  * /sdcard/Android/data/com.github.axet.audiorecorder/files
  * /sdcard/Audio Recorder

# Raw format

  * Signed 16-bit PCM or float (depends on user settings)
  * Big Endian
  * 1 or 2 channels (depends on user settings). First 2 bytes for left channel, Second 2 bytes for right channel.
  * 16hz to 48hz Sample Rate / Frequincy (depends on user settings)

Android supports 16-bit PCM format or PCM float. Android recomends to use PCM float over 24-bit PCM format or 16-bit PCM if possible.

* https://developer.android.com/reference/android/media/AudioFormat#encoding

float mantisa is 23 bits (plus sign bit and float point bits) persition in range from -1..1 can hold about 2130706431 unique numbers which is equivalent to 31 bits integer. When 24-bit PCM only gives you 2^24=16,777,216 unique values.

# Adb commands

    # adb shell am start -n com.github.axet.audiorecorder/.activities.RecordingActivity

    # adb shell am broadcast -a com.github.axet.audiorecorder.STOP_RECORDING
