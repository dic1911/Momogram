package tw.nekomimi.nekogram.transtale.source

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import kotlinx.coroutines.InternalCoroutinesApi
import org.telegram.ui.LaunchActivity
import tw.nekomimi.nekogram.transtale.Translator
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

import dev.davidv.translator.*
import kotlinx.coroutines.suspendCancellableCoroutine
import org.telegram.messenger.LocaleController
import org.telegram.messenger.R

object FirefoxLocalTranslator : Translator {

    @OptIn(InternalCoroutinesApi::class)
    override suspend fun doTranslate(from: String, to: String, query: String): String {

        if (!isBound) {
            bind(true)
        }
        if (isBound) {
            return suspendCoroutine {
                translationService?.translate(query, from, to, object : ITranslationCallback.Stub() {
                    override fun onTranslationResult(translatedText: String) {
                        it.resume(translatedText)
                    }

                    override fun onTranslationError(errorMessage: TranslationError) {
                        Log.e("030-tx", "ff err: ${errorMessage.type} ${errorMessage.message}")
                        val msg =
                            if (errorMessage != null && !"null".equals(errorMessage.message))
                                "${ErrorEnum.from(errorMessage.type.toInt())!!.name} - ${errorMessage.message}"
                            else ErrorEnum.from(errorMessage.type.toInt())!!.name
                        it.resumeWithException(RuntimeException(msg))
                    }
                })
            }
        }
        throw RuntimeException(LocaleController.getString(R.string.FirefoxAidlSvcFailed))
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

     suspend fun bind(block: Boolean) {
         if (isBound) return
         val intent = Intent("dev.davidv.translator.ITranslationService")
         intent.setPackage(SERVICE_APP_PACKAGE)
         try {
             if (block) {
                 LaunchActivity.instance.applicationContext.awaitBindService(
                     intent,
                     connection,
                     Context.BIND_AUTO_CREATE
                 )
             } else {
                 LaunchActivity.instance.applicationContext.bindService(
                     intent,
                     connection,
                     Context.BIND_AUTO_CREATE
                 )
             }
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

    suspend fun Context.awaitBindService(intent: Intent, connection: ServiceConnection? = null, flags: Int = Context.BIND_AUTO_CREATE): IBinder =
        suspendCancellableCoroutine { cont ->
            val connection = connection ?: object : ServiceConnection {
                override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                    if (service != null && cont.isActive) {
                        cont.resume(service)
                    } else {
                        cont.resumeWithException(IllegalStateException("Service is null"))
                    }
                }

                override fun onServiceDisconnected(name: ComponentName?) {
                    // Optional: handle disconnection if needed
                }
            }

            if (!bindService(intent, connection, flags)) {
                cont.resumeWithException(IllegalStateException("bindService returned false"))
            }

            // Unbind automatically if coroutine is cancelled
            cont.invokeOnCancellation { unbindService(connection) }
        }

    enum class ErrorEnum(val value: Int) {
        COULD_NOT_DETECT_LANGUAGE(ErrorType.COULD_NOT_DETECT_LANGUAGE.toInt()),
        DETECTED_BUT_UNAVAILABLE(ErrorType.DETECTED_BUT_UNAVAILABLE.toInt()),
        UNEXPECTED(ErrorType.UNEXPECTED.toInt());

        companion object {
            fun from(value: Int): ErrorEnum? =
                values().find { it.value == value }
        }
    }

}
