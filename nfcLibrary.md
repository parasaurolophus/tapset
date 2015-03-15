# `nfc` Library #

The `nfc` library contains three abstract classes:

  * [NfcReaderActivity](https://code.google.com/p/tapset/source/browse/nfc/src/us/rader/nfc/NfcReaderActivity.java)

  * [NdefReaderActivity](https://code.google.com/p/tapset/source/browse/nfc/src/us/rader/nfc/NdefReaderActivity.java)

  * [NdefWriterActivity](https://code.google.com/p/tapset/source/browse/nfc/src/us/rader/nfc/NdefWriterActivity.java)

`NfcReaderActivity` is derived from the standard Android [Activity](http://developer.android.com/reference/android/app/Activity.html) class. It provides the boilerplate code necessary for any `Activity` that is used to prompt the user to scan an NFC tag and take some action as a result. `NdefReaderActivity` extends `NfcReaderActivity`, adding the ability to parse a NDEF message from the tag that was detected while `NdefWriterActivity`, in turn, adds the ability to write an NDEF message to a tag.

Specifically, `NfcReaderActivity` provides overrides of the `Activity` life-cycle event handling methods necessary to make use of the Android [NfcAdapter](http://developer.android.com/reference/android/nfc/NfcAdapter.html) "foreground dispatch" mechanism to wait for and take action as a result of the user tapping a NFC tag. It declares its own methods for derived classes to take some app-specific action at the critical points in that life-cycle. `NdefReaderActivity` inherits all of the `NfcAdapter` interaction from `NfcReaderActivity`, overriding the `NfcReaderActivity` method to process the tag with an implementation that extrans an instance of [NdefMessage](http://developer.android.com/reference/android/nfc/NdefMessage.html) from it.

`NdefWriterActivity` extends this behavior one step further, adding the ability to write a NDEF message to the tag. It obtains the `NdefMessage` by way of another abstract method that it declares on behalf of its own derived classes, e.g. the <i>tapset</i> app-specific [WriteTagActivity](https://code.google.com/p/tapset/source/browse/tapset/src/us/rader/tapset/WriteTagActivity.java).

The net result is that by declaring your own `Activity` that extends `NfcReaderActivity` directly or through either of its descendants provided in the `nfc` library, you can easily read and write NFC tags simply by overriding a handful of methods that are focused on the tag contents without having to re-implement all of the set up, tear down and asynchronous interaction with `NfcAdapter`.

There is one aspect to the approach taken here that is worth noting for tutorial purposes. Some of the `Activity` methods overridden by `NfcReaderActivity` are declared `final` with, in some cases, corresponding overridable methods declared in their place for derived classes to provide their own implementation to be invoked at the corresponding points in the `Activity` life-cycle. This is to help protect derived classes from accidentally subverting the tightly coupled behavior of the `NfcReaderActivity` methods.

Another approach would be to let derived classes override those same `Activity` methods with stern admonitions in the overridden methods' documentation that the derived class <i>must</i> invoke `super` at just the right times and in just the right ways. The approach taken here has the same effect, assuming that all derived classes are correctly implemented, but is much more bullet proof in case the author of a derived class overlooks such constraints in the documentation or forgets about them in the heat of the moment. This should be regarded as a general principle for good objected oriented design and implementation.