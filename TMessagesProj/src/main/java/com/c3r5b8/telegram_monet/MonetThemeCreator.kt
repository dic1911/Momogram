package com.c3r5b8.telegram_monet

import android.content.Context
import android.util.Log
import androidx.annotation.RequiresApi
import tw.nekomimi.nekogram.utils.ShareUtil
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

object MonetThemeCreator {
    // Files Names
    const val INPUT_FILE_TELEGRAM_LIGHT = "monet_light.attheme"
    const val INPUT_FILE_TELEGRAM_DARK = "monet_dark.attheme"

    const val OUTPUT_FILE = "m0m0net.attheme"

    @RequiresApi(31)
    fun createTheme(
        context: Context,
        isLight: Boolean,
        isAmoled: Boolean,
        isGradient: Boolean,
        isAvatarGradient: Boolean,
        isNicknameColorful: Boolean,
        isAlterOutColor: Boolean
    ) {
        val inputFileName = if (isLight) INPUT_FILE_TELEGRAM_LIGHT else INPUT_FILE_TELEGRAM_DARK
        val reader = BufferedReader(InputStreamReader(context.assets.open(inputFileName)))
        var themeImport = ""
        val listMain = reader.readLines().toMutableList()
        reader.close()

        if (isGradient)
            listMain.forEachIndexed { index, value ->
                listMain[index] = value.replace("noGradient", "chat_outBubbleGradient")
            }

        if (isAlterOutColor) {
            val inputSecondFileName =
                if (inputFileName == INPUT_FILE_TELEGRAM_LIGHT) INPUT_FILE_TELEGRAM_DARK else INPUT_FILE_TELEGRAM_LIGHT
            val reader2 = BufferedReader(InputStreamReader(context.assets.open(inputSecondFileName)))
            val listSecond = reader2.readLines()
            reader.close()

            ListToReplaceNewThemeTelegram.forEach { value ->
                val index1 = listMain.indexOfFirst { it.startsWith("$value=") }
                val index2 = listMain.indexOfFirst { it.startsWith("$value=") }
                if (index1 < 0 || index2 < 0) {
                    Log.d("New Theme", "$value: $index1 $index2")
                } else {
                    listMain[index1] = listSecond[index2]
                }
            }
        }

        listMain.forEach { themeImport += it + "\n" }

        if (isAmoled)
            themeImport = themeImport.replace("n1_900", "n1_1000")

        if (isNicknameColorful)
            themeImport = themeImport.replace(
                "\nend",
                "\navatar_nameInMessageBlue=a1_400\n" +
                        "avatar_nameInMessageCyan=a1_400\n" +
                        "avatar_nameInMessageGreen=a1_400\n" +
                        "avatar_nameInMessageOrange=a1_400\n" +
                        "avatar_nameInMessagePink=a1_400\n" +
                        "avatar_nameInMessageRed=a1_400\n" +
                        "avatar_nameInMessageViolet=a1_400\nend"
            )
        if (isAvatarGradient) {
            themeImport = themeImport
                .replace("avatar_backgroundBlue=n2_800", "avatar_backgroundBlue=n2_700")
                .replace("avatar_backgroundCyan=n2_800", "avatar_backgroundCyan=n2_700")
                .replace("avatar_backgroundGreen=n2_800", "avatar_backgroundGreen=n2_700")
                .replace("avatar_backgroundOrange=n2_800", "avatar_backgroundOrange=n2_700")
                .replace("avatar_backgroundPink=n2_800", "avatar_backgroundPink=n2_700")
                .replace("avatar_backgroundRed=n2_800", "avatar_backgroundRed=n2_700")
                .replace("avatar_backgroundSaved=n2_800", "avatar_backgroundSaved=n2_700")
                .replace("avatar_backgroundViolet=n2_800", "avatar_backgroundViolet=n2_700")
        }

        val generatedTheme = changeTextTelegram(themeImport, context)
        val f = File(context.cacheDir, OUTPUT_FILE)
        f.writeText(text = generatedTheme)
        ShareUtil.shareFile(context, f)
    }
}