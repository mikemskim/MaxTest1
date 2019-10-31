package com.ste.maxtest1

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    var prefs: SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prefs = this.getSharedPreferences("login_prefs", 0)

        setContentView(R.layout.activity_main)

        editTextLoginPwd.setOnEditorActionListener(TextView.OnEditorActionListener { _, id, _ ->
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                attemptLogin()
                return@OnEditorActionListener true
            }
            false
        })

        val host = prefs!!.getString("hostport", null)
        val user = prefs!!.getString("username", null)

        if (host!=null) editTextHostPort.setText(host)
        if (user!=null) editTextLoginId.setText(user)

        loginButton.setOnClickListener { attemptLogin() }
    }

    private fun attemptLogin() {
        Log.d("APP","@@@@@@@@@@@@ AttemptLogin")
        // Reset errors.
        editTextHostPort.error = null
        editTextLoginId.error = null
        editTextLoginPwd.error = null

        // Store values at the time of the login attempt.
        val hostStr = editTextHostPort.text.toString()
        val userStr = editTextLoginId.text.toString()
        val passwordStr = editTextLoginPwd.text.toString()

        var cancel = false
        var focusView: View? = null


        // Check for a valid host port
        if (TextUtils.isEmpty(hostStr)) {
            editTextHostPort.error = getString(R.string.error_host_port)
            focusView = editTextHostPort
            cancel = true
        }

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(passwordStr)) {
            editTextLoginPwd.error = getString(R.string.error_invalid_password)
            focusView = editTextLoginPwd
            cancel = true
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(userStr)) {
            editTextLoginId.error = getString(R.string.error_field_required)
            focusView = editTextLoginId
            cancel = true
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView?.requestFocus()
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
//            showProgress(true)

            val portIdx = hostStr.indexOf(':')
            var host = hostStr.substring(0, portIdx)
            var port = Integer.parseInt(hostStr.substring(portIdx+1))
            Log.d("APP", "Connecting to $host:$port")
            Log.d("APP", "User : $userStr, Password : $passwordStr")

            MaximoAPI.INSTANCE.login(userStr, passwordStr, host, port, {
//                showProgress(false)
                Log.d("APP", "Logged In")

                // store host and user
                prefs!!.edit().putString("hostport", hostStr).putString("username", userStr).apply()

                val intent = Intent(this.baseContext, MenuActivity::class.java)
                var person = MaximoAPI.INSTANCE.loggedUser

                Log.d("APP", person.getString("displayname"))
                intent.putExtra("PersonName", person.getString("displayname", "Unknown User"))
                intent.putExtra("PersonEmail", person.getString("primaryemail", "No Email Set"))
                startActivity(intent)
            }, { t ->
                Log.d("APP", "Error", t)
//                showProgress(false)
                editTextLoginPwd.error = getString(R.string.error_incorrect_password)
                editTextLoginPwd.requestFocus()
            })
        }
    }
}
