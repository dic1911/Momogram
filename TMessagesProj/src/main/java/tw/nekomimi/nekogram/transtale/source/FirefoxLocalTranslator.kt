package tw.nekomimi.nekogram.transtale.source

import android.content.ComponentName
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.os.Messenger
import android.util.Log
import kotlinx.coroutines.InternalCoroutinesApi
import org.telegram.ui.LaunchActivity
import tw.nekomimi.nekogram.transtale.Translator
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

object FirefoxLocalTranslator : Translator {

    @OptIn(InternalCoroutinesApi::class)
    override suspend fun doTranslate(from: String, to: String, query: String): String {

        return suspendCoroutine {
            val handler = object : Handler(Looper.getMainLooper()) {
                override fun handleMessage(msg: Message) {
                    Log.d("030-tx", "handleMessage -> ${msg.data.getString("translated_text")}")
                    val translated = msg.data.getString("translated_text")
                    translated?.let { txt ->
                        it.resume(txt)
                        return
                    }
                    it.resumeWithException(RuntimeException("Failed to translate by FirefoxLocalTranslator"))
                }
            }
            val messenger = Messenger(handler)

            val intent = Intent().apply {
                action = "dev.davidv.translator.action.TRANSLATE_TEXT"
                component = ComponentName(
                    "dev.davidv.translator",
                    "dev.davidv.translator.BackgroundTranslationService"
                )
            }
            intent.putExtra("text_to_translate", query)
            intent.putExtra("result_receiver", messenger)
            intent.putExtra("from_language", from)
            intent.putExtra("to_language", to)
            LaunchActivity.instance.startService(intent)
        }
    }
}
