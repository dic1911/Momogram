package tw.nekomimi.nekogram.transtale.source

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Messenger
import android.util.Log
import kotlinx.coroutines.InternalCoroutinesApi
import org.telegram.messenger.AndroidUtilities
import org.telegram.ui.LaunchActivity
import tw.nekomimi.nekogram.transtale.Translator
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

import dev.davidv.translator.*

object FirefoxLocalTranslator : Translator {

    @OptIn(InternalCoroutinesApi::class)
    override suspend fun doTranslate(from: String, to: String, query: String): String {

        if (!isBound) {
            bind()
        }
        try {
            if (isBound) {
                return suspendCoroutine {
                    translationService?.translate(query, from, to, object : ITranslationCallback.Stub() {
                        override fun onTranslationResult(translatedText: String?) {
                            it.resume(translatedText ?: "")
                        }

                        override fun onTranslationError(errorMessage: String?) {
                            it.resumeWithException(RuntimeException(errorMessage))
                        }
                    })
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error calling translate", e)
        }

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
     private val TAG = "TranslatorClient"

     /** The package name of the app providing the translation service. */
     private const val SERVICE_APP_PACKAGE = "dev.davidv.translator"

     private var translationService: ITranslationService? = null
     private var isBound = false

     private val connection = object : ServiceConnection {
         override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
             Log.d(TAG, "Service connected")
             translationService = ITranslationService.Stub.asInterface(service)
             isBound = true
         }

         override fun onServiceDisconnected(name: ComponentName?) {
             Log.d(TAG, "Service disconnected, attempting to rebind.")
             translationService = null
             isBound = false
         }
     }

     fun bind() {
         if (isBound) return
         val intent = Intent("dev.davidv.translator.ITranslationService")
         intent.setPackage(SERVICE_APP_PACKAGE)
         try {
             LaunchActivity.instance.applicationContext.bindService(intent, connection, Context.BIND_AUTO_CREATE)
         } catch (e: SecurityException) {
             Log.e(TAG, "Failed to bind to service. Is the other app installed and does it have the correct service declaration?", e)
         }
     }

     fun unbind() {
         if (isBound) {
             LaunchActivity.instance.unbindService(connection)
             isBound = false
             translationService = null
         }
     }
}
