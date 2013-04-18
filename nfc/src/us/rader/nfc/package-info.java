/*
 * Copyright 2013 Kirk Rader

 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.

 */

/**
 * Boilerplate for {@link android.app.Activity} objects that read from or write to NFC tags
 * 
 * <p>
 * Any {@link android.app.Activity} that is used to prompt the user to scan a NFC tag
 * and take action based on its contents must follow a standard pattern of interaction between its
 * {@link android.app.Activity#onResume()}, {@link android.app.Activity#onPause()} and
 * {@link android.app.Activity#onNewIntent(android.content.Intent)} methods. That interaction
 * relies on cached data structures best created and initialized in
 * {@link android.app.Activity#onCreate(android.os.Bundle)}. This package contains <code>abstract</code>
 * classes that provide <code>final</code> overrides of those {@link android.app.Activity} methods.
 * The <code>final</code> methods rely, in turn, on their own <code>abstract</code> helper methods
 * to allow for app-specific functionality.
 * </p>
 * 
 * <p>
 * The goal is to allow {@link android.app.Activity} classes derived from the ones in this package
 * to focus on app-specific behaviors while inheriting the logic necessary to inter-operate correctly
 * with the {@link android.nfc.NfcAdapter} "foreground dispatch" mechanism as well as its API for
 * writing a {@link android.nfc.NdefMessage} to a {@link android.nfc.Tag}
 * </p>
 */
package us.rader.nfc;