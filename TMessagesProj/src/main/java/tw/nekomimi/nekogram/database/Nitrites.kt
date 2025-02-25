package tw.nekomimi.nekogram.database

import android.util.Log
import org.dizitart.no2.Nitrite
import org.dizitart.no2.mvstore.MVStoreModule
import org.telegram.messenger.ApplicationLoader
import tw.nekomimi.nekogram.transtale.ChatCCTarget
import tw.nekomimi.nekogram.transtale.ChatCCTargetConverter
import tw.nekomimi.nekogram.transtale.ChatLanguageConverter
import tw.nekomimi.nekogram.transtale.TransItemConverter
import tw.nekomimi.nekogram.utils.FileUtil
import java.io.File

@JvmOverloads
fun mkDatabase(name: String, delete: Boolean = false): Nitrite {

    val file = File("${ApplicationLoader.getDataDirFixed()}/databases/$name.db")
    FileUtil.initDir(file.parentFile!!)
    if (delete) {
        file.deleteRecursively()
    }

    fun create(): Nitrite {
        val storeModule: MVStoreModule = MVStoreModule.withConfig()
            .filePath(file)
            .compress(true)
            .build()

//        val db = Nitrite.builder()
//            .loadModule(storeModule)
//            .openOrCreate()
        val nitrite = Nitrite.builder().loadModule(storeModule)
                .registerEntityConverter(ChatCCTargetConverter())
                .registerEntityConverter(ChatLanguageConverter())
                .registerEntityConverter(TransItemConverter())
                .openOrCreate()!!

        val test = nitrite.openSharedPreference("shared_preferences")
        test.connection.close()

        return nitrite
    }

    runCatching {
        return create()
    }.onFailure {
        file.deleteRecursively()
    }

    return create()

}

fun Nitrite.openSharedPreference(name: String) = DbPref(getCollection(name))

private lateinit var mainSharedPreferencesDatabase: Nitrite

@JvmOverloads
fun openMainSharedPreference(name: String, delete: Boolean = false): DbPref {

    if (!::mainSharedPreferencesDatabase.isInitialized || delete) {

        mainSharedPreferencesDatabase = mkDatabase("shared_preferences", delete)

    }

    return try {
        mainSharedPreferencesDatabase.openSharedPreference(name)
    } catch (e: IllegalStateException) {
        Log.e("030-db", "failed to open", e)
        openMainSharedPreference(name, true)
    }

}