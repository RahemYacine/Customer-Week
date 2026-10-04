package dz.ooredoo.customerweek

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.webkit.JavascriptInterface
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.core.content.FileProvider
import java.io.File

class MainActivity : Activity() {
    private lateinit var web: WebView
    private var chooserCb: ValueCallback<Array<Uri>>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.BLACK
        window.navigationBarColor = Color.BLACK
        web = WebView(this)
        web.setBackgroundColor(Color.BLACK)
        setContentView(web)
        web.settings.javaScriptEnabled = true
        web.settings.domStorageEnabled = true
        web.addJavascriptInterface(Bridge(), "Android")
        web.webChromeClient = object : WebChromeClient() {
            override fun onShowFileChooser(w: WebView, cb: ValueCallback<Array<Uri>>, p: FileChooserParams): Boolean {
                chooserCb?.onReceiveValue(null)
                chooserCb = cb
                return try { startActivityForResult(p.createIntent(), 1); true }
                catch (e: Exception) { chooserCb = null; false }
            }
        }
        web.loadUrl("file:///android_asset/index.html")
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 1) {
            chooserCb?.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, data))
            chooserCb = null
        } else super.onActivityResult(requestCode, resultCode, data)
    }

    inner class Bridge {
        /** Sends [Master Visual, employee photo] + prompt to the ChatGPT app through the Android share sheet. */
        @JavascriptInterface
        fun shareToChatGpt(masterB64: String, photoB64: String, prompt: String): Boolean {
            return try {
                val dir = File(cacheDir, "shared").apply { mkdirs(); listFiles()?.forEach { it.delete() } }
                val m = File(dir, "1_master_visual.png").apply { writeBytes(Base64.decode(masterB64, Base64.DEFAULT)) }
                val p = File(dir, "2_employee_photo.jpg").apply { writeBytes(Base64.decode(photoB64, Base64.DEFAULT)) }
                val uris = arrayListOf(m, p).map { FileProvider.getUriForFile(this@MainActivity, "$packageName.fileprovider", it) }
                runOnUiThread {
                    (getSystemService(CLIPBOARD_SERVICE) as ClipboardManager)
                        .setPrimaryClip(ClipData.newPlainText("prompt", prompt))
                }
                val i = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                    type = "image/*"
                    putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(uris))
                    putExtra(Intent.EXTRA_TEXT, prompt)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    clipData = ClipData.newRawUri("", uris[0]).also { cd -> cd.addItem(ClipData.Item(uris[1])) }
                }
                if (packageManager.getLaunchIntentForPackage("com.openai.chatgpt") != null) {
                    i.setPackage("com.openai.chatgpt")
                    startActivity(i)
                } else {
                    startActivity(Intent.createChooser(i, "Send to ChatGPT"))
                }
                true
            } catch (e: Exception) { false }
        }
    }
}
