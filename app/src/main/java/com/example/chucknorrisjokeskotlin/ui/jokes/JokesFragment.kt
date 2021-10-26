package com.example.chucknorrisjokeskotlin.ui.jokes

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.chucknorrisjokeskotlin.R
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL


class JokesFragment : Fragment() {

    var jokes = StringBuffer("")
    val NoConnection = "NO CONNECTION"
    var noconnection = false
    var handler: Handler = Handler()
    var root: View? = null
    var ReloadBtn: Button? = null
    var JokesListLL: LinearLayout? = null
    var progressBar: ProgressBar? = null
    var contextthis: Context? = null
    var jokesArrList: ArrayList<String>? = ArrayList()

    override fun onCreateView (
            inflater: LayoutInflater,
            container: ViewGroup?, savedInstanceState: Bundle?
        ): View?
    {
        root = inflater.inflate(R.layout.fragment_jokes, container, false)
        ReloadBtn = root!!.findViewById<View>(R.id.ReloadBtn) as Button
        progressBar = root!!.findViewById(R.id.progressBar)
        contextthis = requireActivity().applicationContext
        //restore jokes
        if (savedInstanceState != null)
        {
            jokesArrList = savedInstanceState.getStringArrayList("jokesArrList")
        }
        JokesListLL = root!!.findViewById<View>(R.id.JokesListLL) as LinearLayout
        //print jokes if they exist
        for (i in 0 until jokesArrList!!.size)
        {
            AddNewTextView(i)
        }
        root!!.findViewById<View>(R.id.ReloadBtn).setOnClickListener(View.OnClickListener
        {
            val count = (root!!.findViewById<View>(R.id.CountET) as EditText).text.toString()
            if (count.length == 0)
            {
                return@OnClickListener
            }
            noconnection = false
            //ui off
            progressBar!!.setVisibility(View.VISIBLE)
            ReloadBtn!!.setEnabled(false)
            ReloadBtn!!.setBackgroundColor(resources.getColor(R.color.gray))
            JokesListLL!!.removeAllViews()
            try
            {
                Thread {
                    try
                    {
                        val s : String = "http://api.icndb.com/jokes/random/$count?escape=javascript"
                        jokes = StringBuffer(getContent(s))
                    } catch (e: IOException) {noconnection = true}
                    if (noconnection)
                    {
                        handler.post(Runnable {
                            //ui on
                            progressBar!!.setVisibility(View.INVISIBLE)
                            ReloadBtn!!.setEnabled(true)
                            ReloadBtn!!.setBackgroundColor(resources.getColor(R.color.colorPrimary))
                            Toast.makeText(contextthis, NoConnection, Toast.LENGTH_SHORT).show()
                        })
                    }
                    else
                    {
                        PrintJokes()
                        handler.post(Runnable {
                            //ui on
                            ReloadBtn!!.setEnabled(true)
                            ReloadBtn!!.setBackgroundColor(resources.getColor(R.color.colorPrimary))
                            progressBar!!.setVisibility(View.INVISIBLE)
                        })
                    }
                }.start()
            } catch (e: Exception) {Log.v("gCC", e.message.toString())}
        })
        //if press "done" on keyboard
        root!!.findViewById<View>(R.id.CountET)
            .setOnKeyListener(View.OnKeyListener { view, keyCode, event ->
                if (event.action === KeyEvent.ACTION_DOWN &&
                    keyCode == KeyEvent.KEYCODE_ENTER)
                {
                    ReloadBtn!!.callOnClick()
                    return@OnKeyListener true
                }
                false
            })
        return root
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        savedInstanceState.putStringArrayList("jokesArrList", jokesArrList)
        super.onSaveInstanceState(savedInstanceState)
    }

    @Throws(IOException::class)
    private fun getContent(path: String): String?
    {
        var reader: BufferedReader? = null
        var stream: InputStream? = null
        var connection: HttpURLConnection? = null
        return try
        {
            val url = URL(path)
            connection = url.openConnection() as HttpURLConnection
            connection.setRequestMethod("GET")
            connection.setReadTimeout(10000)
            connection.connect()
            stream = connection.getInputStream()
            reader = BufferedReader(InputStreamReader(stream))
            val buf = StringBuilder()
            var line: String? = ""
            while (reader.readLine().also({ line = it }) != null)
            {
                buf.append(line).append("\n")
            }
            buf.toString()
        }
        finally
        {
            if (reader != null) {reader.close()}
            if (stream != null) {stream.close()}
            if (connection != null) {connection.disconnect()}
        }
    }


    fun PrintJokes()
    {
        var startindex = -1
        var endindex: Int
        jokesArrList!!.clear()
        while (true)
        {
            startindex = jokes.indexOf("\"joke\":")
            if (startindex == -1)
            {
                return
            }
            startindex += 9
            endindex = jokes.indexOf("\", \"categories\"")
            val chars = CharArray(endindex - startindex + 1)
            jokes.getChars(startindex, endindex, chars, 0)
            var s = String(chars)
            while (s.indexOf("\\") != -1)
            {
                s = s.replace("\\", "")
            }
            jokesArrList!!.add(s)
            val index: Int = jokesArrList!!.size - 1
            handler.post(Runnable { AddNewTextView(index) })
            jokes.delete(0, endindex + 26)
        }
    }

    fun AddNewTextView(index: Int)
    {
        val tv = TextView(contextthis)
        tv.text = jokesArrList!![index]
        tv.setTextIsSelectable(true)
        if (index % 2 == 0)
        {
            tv.setBackgroundColor(resources.getColor(R.color.textback1))
        }
        else
        {
            tv.setBackgroundColor(resources.getColor(R.color.textback2))
        }
        tv.setTextColor(resources.getColor(R.color.darkgray))
        tv.textSize = ReloadBtn!!.getTextSize() * 1.3f / resources.displayMetrics.density
        val layoutParams = LinearLayout.LayoutParams(-1, -1)
        layoutParams.setMargins(0, 0, 0, 15)
        JokesListLL!!.addView(tv, layoutParams)
    }
}