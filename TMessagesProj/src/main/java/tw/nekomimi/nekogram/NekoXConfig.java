package tw.nekomimi.nekogram;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;

import org.apache.commons.lang3.StringUtils;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BuildConfig;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MediaController;
import org.telegram.messenger.MediaDataController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.SharedConfig;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.Theme;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import moe.hx030.momogram.util.ArrayUtil;
import tw.nekomimi.nekogram.database.NitritesKt;
import tw.nekomimi.nekogram.helpers.CustomStatusHelper;
import tw.nekomimi.nekogram.utils.StrUtil;

public class NekoXConfig {

    //  public static String FAQ_URL = "https://telegra.ph/NekoX-FAQ-03-31";
    //  public static String FAQ_URL = "https://github.com/NekoX-Dev/NekoX#faq";
    public static String FAQ_URL = "https://github.com/dic1911/Momogram#faq";
    public static long releaseChannel = 2137047153;
    public static Long[] officialChats = {
            1305127566L, // NekoX Updates
            1151172683L, // NekoX Chat
            1299578049L, // NekoX Chat Channel
            1137038259L, // NekoX APKs
            2137047153L, // ghetto channel
            2037198618L, // ghetto chat
    };

    public static Long[] developers = {
            896711046L, // nekohasekai
            380570774L, // Haruhi
            150725478L, // HenTaku
    };

    public static HashSet<Long> devSet = new HashSet<>();
    public static final HashMap<Long, CustomEmojiStatusText> customStatus = new HashMap<>();

    public static final int TITLE_TYPE_TEXT = 0;
    public static final int TITLE_TYPE_ICON = 1;
    public static final int TITLE_TYPE_MIX = 2;

    private static final String EMOJI_FONT_AOSP = "NotoColorEmoji.ttf";

    public static boolean loadSystemEmojiFailed = false;
    private static Typeface systemEmojiTypeface;


    public static SharedPreferences preferences = ApplicationLoader.applicationContext
            .getSharedPreferences("nekox_cfg", Context.MODE_PRIVATE);

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
    public static AtomicInteger loginApiType = new AtomicInteger(0);

    static {
        for (long id : developers) devSet.add(id);
        customStatus.put(150725478L, new CustomEmojiStatusText(3833041, 2077096, 2026694, 16769475, "Momogram dev 030", true));
        customStatus.put(487758521L, new CustomEmojiStatusText("Banks ;)"));
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
        return StringUtils.isNotBlank(hashStr) ? hashStr : BuildConfig.APP_HASH;
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
        if (StringUtils.isNotBlank(NekoConfig.openPGPApp.String())) {
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
                String sub = StrUtil.getSubString(name, null, "-");
                return new Locale(sub, sub).getDisplayName(LocaleController.getInstance().currentLocale);
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

    public static synchronized CustomEmojiStatusText getCustomStatusText(Long id) {
        if (id == null) return null;
        CustomEmojiStatusText status = customStatus.get(id);
        if (status == null) {
            if (devSet.contains(id)) {
                return new CustomEmojiStatusText("NekoX dev");
            }
            return null;
        }

        return status;
    }

    public static synchronized boolean hasCustomStatusParticle(Long id) {
        if (id == null) return false;
        CustomEmojiStatusText status = customStatus.get(id);
        return status != null && status.has_particle;
    }

    private static boolean checkedStatusUpdate = false;
    public static synchronized void checkCustomStatusUpdate() {
        long t = System.currentTimeMillis();
        if (checkedStatusUpdate && t < NekoConfig.nextCheckCustomStatusTime.Long()) return;
        try {
            CustomStatusHelper.updateCustomStatus();
            checkedStatusUpdate = true;
        } catch (Exception e) {
            Log.e("030-status", "updateCustomStatus err", e);
        }
        NekoConfig.nextCheckCustomStatusTime.setConfigLong(t + (30 * 60 * 1000));
    }

    public static class CustomEmojiStatusText extends TLRPC.TL_emojiStatusCollectible {

        public long id;
        public boolean has_particle;

        public CustomEmojiStatusText(String t) {
            title = t;
            center_color = CustomStatusHelper.DEFAULT_BACKGROUND_COLOR;
        }

        public CustomEmojiStatusText(int center, int edge, int pattern, int color, String txt) {
            this(center, edge, pattern, color, txt, false);
        }

        public CustomEmojiStatusText(int center, int edge, int pattern, int color, String txt, boolean particle) {
            center_color = center;
            edge_color = edge;
            pattern_color = pattern;
            text_color = color;
            title = txt;
            has_particle = particle;
        }
    }

    private final static String LAST_PLAYING_MSG_DIALOG_ID = "last_playing_message_dialog_id";
    private final static String LAST_PLAYING_MSG_ID = "last_playing_message_id";
    private final static String LAST_PLAYING_MSG_PAUSED = "last_playing_message_paused";
    private final static String LAST_PLAYING_MSG_PROGRESS = "last_playing_message_progress";
    private final static String LAST_PLAYING_MSG_PROGRESS_MS = "last_playing_message_progress_ms";
    private final static String LAST_PLAYING_MSG_PROGRESS_SEC = "last_playing_message_progress_sec";
    @SuppressLint("ApplySharedPref")
    public static void saveMusicPlaybackState(Runnable callback) {
        MessageObject msg = MediaController.getInstance().getPlayingMessageObject();
        if (msg == null) {
            if (callback != null) callback.run();
            return;
        }

        Log.d("030-music", String.format("save playback %d %d", msg.getDialogId(), msg.getId()));
        {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putLong(LAST_PLAYING_MSG_DIALOG_ID, msg.getDialogId());
            editor.putLong(LAST_PLAYING_MSG_ID, msg.getId());
            editor.putFloat(LAST_PLAYING_MSG_PROGRESS, msg.audioProgress);
            editor.putInt(LAST_PLAYING_MSG_PROGRESS_MS, msg.audioProgressMs);
            editor.putInt(LAST_PLAYING_MSG_PROGRESS_SEC, msg.audioProgressSec);
            editor.putBoolean(LAST_PLAYING_MSG_PAUSED, MediaController.getInstance().isMessagePaused());

            boolean success = editor.commit();
            if (!success) {
                Log.e("030-music", "commit failed");
            }
        }
        if (callback != null) AndroidUtilities.runOnUIThread(callback);
    }

    public static void restoreMusicPlaybackState(int currentAccount) {
        if (!NekoConfig.resumeAudioPlaybackOnLaunch.Bool()) return;

        long dialogId = preferences.getLong(LAST_PLAYING_MSG_DIALOG_ID, -1L);
        if (dialogId == -1L) {
            Log.d("030-music", "no saved state to restore");
            return;
        }

        MediaDataController mediaDataController =  MediaDataController.getInstance(currentAccount);
        long lastPlayingMessageId = preferences.getLong(LAST_PLAYING_MSG_ID, -1L);
        Log.d("030-music", String.format("restore playback %d %d", dialogId, lastPlayingMessageId));
        mediaDataController.loadMusic(dialogId, lastPlayingMessageId + 1, lastPlayingMessageId - 1);
    }

    public static long getLastMusicMessageId() {
        long value = preferences.getLong(LAST_PLAYING_MSG_ID, -1L);
        preferences.edit().putLong(LAST_PLAYING_MSG_ID, -1L).commit();
        return value;
    }

    public static Bundle getLastMusicPlaybackProgress() {
        long dialogId = preferences.getLong(LAST_PLAYING_MSG_DIALOG_ID, -1L);
        if (dialogId == -1L) {
            Log.d("030-music", "no saved progress");
            return null;
        }

        Bundle ret = new Bundle();
        ret.putBoolean("paused", preferences.getBoolean(LAST_PLAYING_MSG_PAUSED, true));
        ret.putFloat("progress", preferences.getFloat(LAST_PLAYING_MSG_PROGRESS, 0));
        ret.putInt("ms", preferences.getInt(LAST_PLAYING_MSG_PROGRESS_MS, 0));
        ret.putInt("sec", preferences.getInt(LAST_PLAYING_MSG_PROGRESS_SEC, 0));
        return ret;
    }

    public static void doneRestoreMusicPlaybackState() {
        Log.d("030-music", "remove last playback state flag");
        preferences.edit()
                .remove(LAST_PLAYING_MSG_DIALOG_ID)
                .commit();
    }

    private static final String MUTED_ACCOUNTS = "muted_accounts";
    private static Set<Integer> mutedAccountSet;
    public static boolean toggleMuteCurrentAccount() {
        boolean ret;
        int acc = UserConfig.selectedAccount;
        StringBuilder sb = new StringBuilder();

        if (mutedAccountSet == null) {
            isAccountMuted(0); // dum
        }

        if (!(ret = mutedAccountSet.add(acc))) mutedAccountSet.remove(acc);
        mutedAccountSet.forEach(i -> {
            sb.append(i).append(" ");
        });

        preferences.edit().putString(MUTED_ACCOUNTS, sb.toString().trim()).commit();

        return ret;
    }

    public static boolean isAccountMuted(int account) {
        if (mutedAccountSet == null) {
            String str = preferences.getString(MUTED_ACCOUNTS, "");
            String[] idStr = str.split(" ");
            mutedAccountSet = new HashSet<>();
            for (String id : idStr) mutedAccountSet.add(Integer.parseInt(id));
        }
        return mutedAccountSet.contains(account);
    }
}