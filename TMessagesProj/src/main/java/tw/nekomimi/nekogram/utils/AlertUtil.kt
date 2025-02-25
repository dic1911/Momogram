package tw.nekomimi.nekogram.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.LinearLayout
import android.widget.Toast
import org.telegram.messenger.AndroidUtilities
import org.telegram.messenger.ApplicationLoader
import org.telegram.messenger.LocaleController
import org.telegram.messenger.R
import org.telegram.tgnet.TLRPC
import org.telegram.ui.ActionBar.AlertDialog
import org.telegram.ui.Components.EditTextBoldCursor
import org.telegram.ui.Components.NumberPicker
import tw.nekomimi.nekogram.ui.BottomBuilder
import tw.nekomimi.nekogram.ui.PopupBuilder
import tw.nekomimi.nekogram.NekoConfig
import java.util.*
import java.util.concurrent.atomic.AtomicReference

object AlertUtil {

    @JvmStatic
    fun copyAndAlert(text: String) {

        AndroidUtilities.addToClipboard(text)

        AlertUtil.showToast(LocaleController.getString(R.string.TextCopied))

    }

    @JvmStatic
    fun copyLinkAndAlert(text: String) {

        AndroidUtilities.addToClipboard(text)

        AlertUtil.showToast(LocaleController.getString(R.string.LinkCopied))

    }

    @JvmStatic
    fun call(number: String) {

        runCatching {

            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:+" + number))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            ApplicationLoader.applicationContext.startActivity(intent)

        }.onFailure {

            showToast(it)

        }

    }

    @JvmStatic
    fun showToast(e: Throwable) = showToast(e.message ?: e.javaClass.simpleName)

    @JvmStatic
    fun showToast(e: TLRPC.TL_error?) {
        if (e == null) return
        showToast("${e.code}: ${e.text}")
    }

    @JvmStatic
    fun showToast(text: String) = UIUtil.runOnUIThread(Runnable {
        Toast.makeText(
                ApplicationLoader.applicationContext,
                text.takeIf { it.isNotBlank() }
                        ?: "喵 !",
                Toast.LENGTH_LONG
        ).show()
    })

    @JvmStatic
    fun showSimpleAlert(ctx: Context?, error: Throwable) {

        showSimpleAlert(ctx, null, error.message ?: error.javaClass.simpleName)

    }

    @JvmStatic
    @JvmOverloads
    fun showSimpleAlert(ctx: Context?, text: String, listener: ((AlertDialog.Builder) -> Unit)? = null) {

        showSimpleAlert(ctx, null, text, listener)

    }

    @JvmStatic
    @JvmOverloads
    fun showSimpleAlert(ctx: Context?, title: String?, text: String, listener: ((AlertDialog.Builder) -> Unit)? = null) = UIUtil.runOnUIThread(Runnable {

        if (ctx == null) return@Runnable

        val builder = AlertDialog.Builder(ctx)

        builder.setTitle(title ?: StrUtil.getAppName())
        builder.setMessage(text)

        builder.setPositiveButton(LocaleController.getString(R.string.OK)) { _, _ ->

            builder.dismissRunnable?.run()
            listener?.invoke(builder)

        }

        builder.show()

    })

    @JvmStatic
    fun showCopyAlert(ctx: Context, text: String) = UIUtil.runOnUIThread(Runnable {

        val builder = AlertDialog.Builder(ctx)

        builder.setTitle(LocaleController.getString(R.string.Translate))
        builder.setMessage(text)

        builder.setNegativeButton(LocaleController.getString(R.string.Copy)) { _, _ ->

            AndroidUtilities.addToClipboard(text)

            AlertUtil.showToast(LocaleController.getString(R.string.TextCopied))

            builder.dismissRunnable.run()

        }

        builder.setPositiveButton(LocaleController.getString(R.string.OK)) { _, _ ->

            builder.dismissRunnable.run()

        }

        builder.show()

    })

    @JvmOverloads
    @JvmStatic
    fun showProgress(ctx: Context, text: String = LocaleController.getString(R.string.Loading)): AlertDialog {

        return AlertDialog.Builder(ctx, AlertDialog.ALERT_TYPE_MESSAGE).apply {

            setMessage(text)

        }.create()

    }

    fun showInput(ctx: Context, title: String, hint: String, onInput: (AlertDialog.Builder, String) -> String) = UIUtil.runOnUIThread(Runnable {

        val builder = AlertDialog.Builder(ctx)

        builder.setTitle(title)

        builder.setView(EditTextBoldCursor(ctx).apply {

            setHintText(hint)

        })

    })

    @JvmStatic
    @JvmOverloads
    fun showConfirm(ctx: Context, title: String, text: String? = null, icon: Int, button: String, red: Boolean, listener: Runnable) = UIUtil.runOnUIThread(Runnable {

        /*

        val builder = AlertDialog.Builder(ctx)

        builder.setTitle(title)

        builder.setMessage(AndroidUtilities.replaceTags(text))
        builder.setPositiveButton(button, listener)

        builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null)
        val alertDialog = builder.show()

        if (red) {

            (alertDialog.getButton(DialogInterface.BUTTON_POSITIVE) as TextView?)?.setTextColor(Theme.getColor(Theme.key_dialogTextRed2))

        }

         */

        val builder = BottomBuilder(ctx)

        if (text != null) {

            builder.addTitle(title, text)

        } else {

            builder.addTitle(title)

        }

        builder.addItem(button, icon, red) {

            listener.run()

        }

        builder.addCancelItem()

        builder.show()

    })

    @JvmStatic
    fun showTransFailedDialog(ctx: Context, noRetry: Boolean, message: String, exception: Throwable?, retryRunnable: Runnable) = UIUtil.runOnUIThread(Runnable {
        if (exception != null) {
            Log.e("nx-trans-failed", "exception occurred..", exception)
        } else {
            Log.e("nx-trans-failed", "error occurred, $message")
        }
        ctx.setTheme(R.style.Theme_TMessages)

        val builder = AlertDialog.Builder(ctx)

        builder.setTitle(LocaleController.getString(R.string.TranslateFailed))

        builder.setMessage(message)

        val reference = AtomicReference<AlertDialog>()

        builder.setNeutralButton(LocaleController.getString(R.string.ChangeTranslateProvider)) {

            _, _ ->

            val view = reference.get().getButton(AlertDialog.BUTTON_NEUTRAL)

            val popup = PopupBuilder(view, true)

            val items = LinkedList<String>()

            items.addAll(arrayOf(
                    LocaleController.getString(R.string.ProviderGoogleTranslate),
                    LocaleController.getString(R.string.ProviderGoogleTranslateCN),
                    LocaleController.getString(R.string.ProviderYandexTranslate),
                    LocaleController.getString(R.string.ProviderLingocloud),
                    LocaleController.getString(R.string.ProviderMicrosoftTranslator),
                    LocaleController.getString(R.string.ProviderYouDao),
                    LocaleController.getString(R.string.ProviderDeepLTranslate),
                    LocaleController.getString(R.string.ProviderTelegramAPI),
                    LocaleController.getString(R.string.ProviderLingva)
            ))

            popup.setItems(items.toTypedArray()) { item, _ ->

                reference.get().dismiss()

                NekoConfig.translationProvider.setConfigInt(item + 1)

                retryRunnable.run()

            }

            popup.show()

        }

        if (noRetry) {

            builder.setPositiveButton(LocaleController.getString(R.string.Cancel)) { _, _ ->

                reference.get().dismiss()

            }

        } else {

            builder.setPositiveButton(LocaleController.getString(R.string.Retry)) { _, _ ->

                reference.get().dismiss()

                retryRunnable.run()

            }

            builder.setNegativeButton(LocaleController.getString(R.string.Cancel)) { _, _ ->

                reference.get().dismiss()

            }

        }

        reference.set(builder.create().apply {

            setDismissDialogByButtons(false)
            show()

        })

    })

    fun showTimePicker(ctx: Context, title: String, callback: (Long) -> Unit) {

        ctx.setTheme(R.style.Theme_TMessages)

        val builder = AlertDialog.Builder(ctx)

        builder.setTitle(title)

        builder.setView(LinearLayout(ctx).apply {

            orientation = LinearLayout.HORIZONTAL

            addView(NumberPicker(ctx).apply {

                minValue = 0
                maxValue = 60

            }, LinearLayout.LayoutParams(-2, -2).apply {

                weight = 1F

            })

            addView(NumberPicker(ctx).apply {

                minValue = 0
                maxValue = 60

            }, LinearLayout.LayoutParams(-2, -2).apply {

                weight = 1F

            })

        })

    }

}