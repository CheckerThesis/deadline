package com.example.deadline

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.app.Dialog
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.example.deadline.R
import com.google.android.material.floatingactionbutton.FloatingActionButton

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings_activity)

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings_container, SettingsFragment())
            .commit()
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.preferences, rootKey)

            setPreferencesFromResource(R.xml.preferences, rootKey)

            val pdfSwipeHorizontalPreference: Preference? = findPreference("pdf_swipe_horizontal")
            pdfSwipeHorizontalPreference?.setOnPreferenceChangeListener { preference, newValue ->
                val isSwipeHorizontal = newValue as Boolean
                // Save the new value to SharedPreferences
                val sharedPreferences = preference.sharedPreferences
                if (sharedPreferences != null) {
                    sharedPreferences.edit().putBoolean("pdf_swipe_horizontal_value", isSwipeHorizontal).apply()
                }
                true
            }

            val pdfviewerLicense: Preference? = findPreference("android_pdfviewer_license")

            // Handle the ListPreference click event
            pdfviewerLicense?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                // Open the custom menu dialog
                (activity as? SettingsActivity)?.pdfviewerLicenseDialog()
                true
            }

            val preferenceLicense: Preference? = findPreference("androidx_preferences_license")

            // Handle the other Preference click event
            preferenceLicense?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                // Open the custom menu dialog
                (activity as? SettingsActivity)?.preferenceLicenseDialog()
                true
            }
        }
    }

    private fun pdfviewerLicenseDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_settings_menu, null)

        val dialog = Dialog(this)
        dialog.setContentView(dialogView)

        // Set the dialog size and position (you can customize this as needed)
        val layoutParams = dialog.window?.attributes
        layoutParams?.width = (resources.displayMetrics.widthPixels * 0.9).toInt() // 90% of the screen width
        layoutParams?.height = (resources.displayMetrics.heightPixels * 0.7).toInt() // 70% of the screen height
        dialog.window?.attributes = layoutParams

        // Find the TextView in the dialog layout
        val menuText: TextView = dialogView.findViewById(R.id.menuText)

        // Set the desired text
        val licenseText = """Android PdfViewer:
(https://github.com/barteksc/AndroidPdfViewer)
            
Copyright 2014-2021 Barteksc

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License."""

        menuText.text = licenseText

        // Find the Close button (Button) in the dialog layout
        val closeButton: Button = dialogView.findViewById(R.id.closeButton)

        // Handle close button click
        closeButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun preferenceLicenseDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_settings_menu, null)

        val dialog = Dialog(this)
        dialog.setContentView(dialogView)

        // Set the dialog size and position (you can customize this as needed)
        val layoutParams = dialog.window?.attributes
        layoutParams?.width = (resources.displayMetrics.widthPixels * 0.9).toInt() // 90% of the screen width
        layoutParams?.height = (resources.displayMetrics.heightPixels * 0.7).toInt() // 70% of the screen height
        dialog.window?.attributes = layoutParams

        // Find the TextView in the dialog layout
        val menuText: TextView = dialogView.findViewById(R.id.menuText)

        // Set the desired text
        val licenseText = """Androidx Preferences:
(https://mvnrepository.com/artifact/androidx.preference/preference-ktx/1.2.0)

Copyright 2023 The Android Open Source Project

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License."""

        menuText.text = licenseText

        // Find the Close button (Button) in the dialog layout
        val closeButton: Button = dialogView.findViewById(R.id.closeButton)

        // Handle close button click
        closeButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}