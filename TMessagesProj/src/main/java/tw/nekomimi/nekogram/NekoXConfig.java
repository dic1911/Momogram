package tw.nekomimi.nekogram;

import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Typeface;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BuildConfig;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.SharedConfig;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.ActionBar.Theme;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import tw.nekomimi.nekogram.database.NitritesKt;

public class NekoXConfig {

    //  public static String FAQ_URL = "https://telegra.ph/NekoX-FAQ-03-31";
    //  public static String FAQ_URL = "https://github.com/NekoX-Dev/NekoX#faq";
    public static String FAQ_URL = "https://github.com/dic1911/Momogram#faq";
    public static long releaseChannel = 2137047153;
    public static long[] officialChats = {
            1305127566, // NekoX Updates
            1151172683, // NekoX Chat
            1299578049, // NekoX Chat Channel
            1137038259, // NekoX APKs
            2137047153, // ghetto channel
            2037198618, // ghetto chat
    };

    public static long[] developers = {
            896711046, // nekohasekai
            380570774, // Haruhi
            150725478, // HenTaku
            487758521, // Banks
    };

    public static HashSet<Long> devSet = new HashSet<>();

    public static final int TITLE_TYPE_TEXT = 0;
    public static final int TITLE_TYPE_ICON = 1;
    public static final int TITLE_TYPE_MIX = 2;

    private static final String EMOJI_FONT_AOSP = "NotoColorEmoji.ttf";

    public static boolean loadSystemEmojiFailed = false;
    private static Typeface systemEmojiTypeface;


    public static SharedPreferences preferences = NitritesKt.openMainSharedPreference("nekox_config");

    public static boolean developerMode = preferences.getBoolean("developer_mode", false);

    public static boolean disableFlagSecure = preferences.getBoolean("disable_flag_secure", false);
    public static boolean disableScreenshotDetection = preferences.getBoolean("disable_screenshot_detection", false);

    public static boolean disableStatusUpdate = preferences.getBoolean("disable_status_update", false);
    public static boolean keepOnlineStatus = preferences.getBoolean("keepOnlineStatus", false);

    public static int autoUpdateReleaseChannel = preferences.getInt("autoUpdateReleaseChannel", 2);
//    public static String ignoredUpdateTag = preferences.getString("ignoredUpdateTag", "");
//    public static long nextUpdateCheck = preferences.getLong("nextUpdateCheckTimestamp", 0);

//    public static int customApi = preferences.getInt("custom_api", 0);
//    public static int customAppId = preferences.getInt("custom_app_id", 0);
//    public static String customAppHash = preferences.getString("custom_app_hash", "");

    static {
        for (long id : developers) devSet.add(id);
    }

    public static void toggleDeveloperMode() {
        preferences.edit().putBoolean("developer_mode", developerMode = !developerMode).apply();
        if (!developerMode) {
            preferences.edit()
                    .putBoolean("disable_flag_secure", disableFlagSecure = false)
                    .putBoolean("disable_screenshot_detection", disableScreenshotDetection = false)
                    .putBoolean("disable_status_update", disableStatusUpdate = false)
                    .apply();
        }
    }

    public static void toggleDisableFlagSecure() {
        preferences.edit().putBoolean("disable_flag_secure", disableFlagSecure = !disableFlagSecure).apply();
    }

    public static void toggleDisableScreenshotDetection() {
        preferences.edit().putBoolean("disable_screenshot_detection", disableScreenshotDetection = !disableScreenshotDetection).apply();
    }

    private static Boolean hasDeveloper = null;

    public static int currentAppId() {
        String idStr = NekoConfig.customApiId.String();
        try {
            return Integer.parseInt(idStr);
        } catch (Exception ignored) {}

        return BuildConfig.APP_ID;
    }

    private static HashSet<String> botWithWebView = null;
    public static boolean saveBotHasWebView(long id, boolean value) {
        if (botWithWebView == null) botWithWebView = new HashSet<>();
        if (value) botWithWebView.add(String.valueOf(id));
        else botWithWebView.remove(String.valueOf(id));
        return value;
    }

    public static boolean botHasWebView(long id) {
        if (botWithWebView == null) {
            botWithWebView = new HashSet<>();
        }

        return botWithWebView.contains(String.valueOf(id));
    }

    public static void toggleDisableStatusUpdate() {
        preferences.edit().putBoolean("disable_status_update", disableStatusUpdate = !disableStatusUpdate).apply();
        NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.updateUserStatus, (Object) null);
    }

    public static void toggleKeepOnlineStatus() {
        preferences.edit().putBoolean("keepOnlineStatus", keepOnlineStatus = !keepOnlineStatus).apply();
        NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.updateUserStatus, (Object) null);
    }

    public static void setAutoUpdateReleaseChannel(int channel) {
        preferences.edit().putInt("autoUpdateReleaseChannel", autoUpdateReleaseChannel = channel).apply();
    }

    public static String currentAppHash() {
        String hashStr = NekoConfig.customApiHash.String();
        return StrUtil.isNotBlank(hashStr) ? hashStr : BuildConfig.APP_HASH;
    }

    public static boolean isDeveloper() {
        if (hasDeveloper != null)
            return hasDeveloper;
        hasDeveloper = false;
        // if (BuildVars.DEBUG_VERSION) hasDeveloper = true;
        for (int acc : SharedConfig.activeAccounts) {
            long myId = UserConfig.getInstance(acc).clientUserId;
            if (ArrayUtil.contains(NekoXConfig.developers, myId)) {
                hasDeveloper = true;
                break;
            }
        }
        return hasDeveloper;
    }

    public static String getOpenPGPAppName() {
        if (StrUtil.isNotBlank(NekoConfig.openPGPApp.String())) {
            try {
                PackageManager manager = ApplicationLoader.applicationContext.getPackageManager();
                ApplicationInfo info = manager.getApplicationInfo(NekoConfig.openPGPApp.String(), PackageManager.GET_META_DATA);
                return (String) manager.getApplicationLabel(info);
            } catch (PackageManager.NameNotFoundException e) {
                NekoConfig.openPGPApp.setConfigString("");
            }
        }
        return LocaleController.getString(R.string.None);
    }

    public static String formatLang(String name) {
        if (name == null || name.isEmpty()) {
            return LocaleController.getString(R.string.Default);
        } else {
            if (name.contains("-")) {
                return new Locale(StrUtil.subBefore(name, "-", false), StrUtil.subAfter(name, "-", false)).getDisplayName(LocaleController.getInstance().currentLocale);
            } else {
                return new Locale(name).getDisplayName(LocaleController.getInstance().currentLocale);
            }
        }
    }

    public static Typeface getSystemEmojiTypeface() {
        if (!loadSystemEmojiFailed && systemEmojiTypeface == null) {
            try {
                Pattern p = Pattern.compile(">(.*emoji.*)</font>", Pattern.CASE_INSENSITIVE);
                BufferedReader br = new BufferedReader(new FileReader("/system/etc/fonts.xml"));
                String line;
                while ((line = br.readLine()) != null) {
                    Matcher m = p.matcher(line);
                    if (m.find()) {
                        systemEmojiTypeface = Typeface.createFromFile("/system/fonts/" + m.group(1));
                        FileLog.d("emoji font file fonts.xml = " + m.group(1));
                        break;
                    }
                }
                br.close();
            } catch (Exception e) {
                FileLog.e(e);
            }
            if (systemEmojiTypeface == null) {
                try {
                    systemEmojiTypeface = Typeface.createFromFile("/system/fonts/" + EMOJI_FONT_AOSP);
                    FileLog.d("emoji font file = " + EMOJI_FONT_AOSP);
                } catch (Exception e) {
                    FileLog.e(e);
                    loadSystemEmojiFailed = true;
                }
            }
        }
        return systemEmojiTypeface;
    }

    public static int getNotificationColor() {
        int color = 0;
        Configuration configuration = ApplicationLoader.applicationContext.getResources().getConfiguration();
        boolean isDark = (configuration.uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
        if (isDark) {
            color = 0xffffffff;
        } else {
            if (Theme.getActiveTheme().hasAccentColors()) {
                color = Theme.getActiveTheme().getAccentColor(Theme.getActiveTheme().currentAccentId);
            }
            if (Theme.getActiveTheme().isDark() || color == 0) {
                color = Theme.getColor(Theme.key_actionBarDefault);
            }
            // too bright
            if (AndroidUtilities.computePerceivedBrightness(color) >= 0.721f) {
                color = Theme.getColor(Theme.key_windowBackgroundWhiteBlueHeader) | 0xff000000;
            }
        }
        return color;
    }


    public static void setChannelAlias(long channelID, String name) {
        preferences.edit().putString(NekoConfig.channelAliasPrefix + channelID, name).apply();
    }

    public static void emptyChannelAlias(long channelID) {
        preferences.edit().remove(NekoConfig.channelAliasPrefix + channelID).apply();
    }

    public static String getChannelAlias(long channelID) {
        return preferences.getString(NekoConfig.channelAliasPrefix + channelID, null);
    }

    public static void setChatNameOverride(long chatId, String name) {
        preferences.edit().putString(NekoConfig.chatNameOverridePrefix + chatId, name).apply();
        MessagesController.overrideNameCache.put(chatId, name);
    }

    public static void emptyChatNameOverride(long chatId) {
        preferences.edit().remove(NekoConfig.chatNameOverridePrefix + chatId).apply();
        MessagesController.overrideNameCache.put(chatId, "");
    }

    public static String getChatNameOverride(long chatId) {
        return preferences.getString(NekoConfig.chatNameOverridePrefix + chatId, null);
    }

    private final static String instantViewFailedDomainKey = "iv_failed_domains";
    private static HashSet<String> instantViewFailedDomainSet = null;
    public static void addInstantViewFailedDomain(String host) {
        if (instantViewFailedDomainSet == null) {
            Set<String> s = preferences.getStringSet(instantViewFailedDomainKey, null);
            instantViewFailedDomainSet = (s == null) ? new HashSet<>() : new HashSet<>(s);
        }
        instantViewFailedDomainSet.add(host);
        preferences.edit().putStringSet(instantViewFailedDomainKey, instantViewFailedDomainSet).apply();
    }
    public static void resetInstantViewFailedDomains() {
        if (instantViewFailedDomainSet != null) instantViewFailedDomainSet.clear();
        preferences.edit().putStringSet(instantViewFailedDomainKey, Set.of()).apply();
    }
    public static boolean isInstantViewFailedDomain(String host) {
        if (instantViewFailedDomainSet == null) {
            Set<String> s = preferences.getStringSet(instantViewFailedDomainKey, null);
            instantViewFailedDomainSet = (s == null) ? new HashSet<>() : new HashSet<>(s);
        }
        return instantViewFailedDomainSet.contains(host);
    }
}