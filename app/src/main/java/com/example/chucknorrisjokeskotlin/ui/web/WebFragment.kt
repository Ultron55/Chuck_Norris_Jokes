package com.example.chucknorrisjokeskotlin.ui.web

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import com.example.chucknorrisjokeskotlin.MainActivity
import com.example.chucknorrisjokeskotlin.R


class WebFragment : Fragment() {

    var webView: WebView? = null
    var progressBar: ProgressBar? = null
    var handler: Handler = Handler()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View?
    {
        requireActivity().title = "API info"
        val root: View = inflater.inflate(R.layout.fragment_web, container, false)
        progressBar = root.findViewById(R.id.WebProgressBar)
        webView = root.findViewById<View>(R.id.webView) as WebView
        webView!!.webViewClient = object : WebViewClient()
        {
            override fun shouldOverrideUrlLoading(webView: WebView, url: String): Boolean
            {
                if (url.contains("http://www.icndb.com"))
                {
                    return false
                }
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                activity!!.startActivity(intent)
                return true
            }

            override fun onPageFinished(view: WebView, url: String)
            {
                super.onPageFinished(view, url)
                Thread{handler.post(Runnable{progressBar!!.setVisibility(View.INVISIBLE)})}.start()
            }
        }
        webView!!.settings.javaScriptEnabled = true
        (activity as MainActivity?)!!.webView = webView
        webView!!.loadUrl("http://www.icndb.com/api/")
        root.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            if (event.action !== KeyEvent.ACTION_DOWN) return@OnKeyListener true
            if (keyCode == KeyEvent.KEYCODE_BACK && webView!!.canGoBack())
            {
                webView!!.goBack()
                return@OnKeyListener true
            }
            false
        })
        return root
    }

    override fun onSaveInstanceState(outState: Bundle)
    {
        super.onSaveInstanceState(outState)
        val bundle = Bundle()
        webView!!.saveState(bundle)
        outState.putBundle("webViewState", bundle)
    }


}