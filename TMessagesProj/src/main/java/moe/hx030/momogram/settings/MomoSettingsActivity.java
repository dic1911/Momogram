package moe.hx030.momogram.settings;

import static org.telegram.messenger.LocaleController.getString;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.R;
import org.telegram.messenger.SendMessagesHelper;
import org.telegram.messenger.browser.Browser;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;
import org.telegram.ui.Components.UniversalRecyclerView;
import org.telegram.ui.DocumentSelectActivity;
import org.telegram.ui.LaunchActivity;
import org.telegram.ui.SettingsActivity;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

import kotlin.text.StringsKt;
import moe.hx030.momogram.MomoConfig;
import moe.hx030.momogram.utils.AlertUtil;
import moe.hx030.momogram.utils.FileUtil;
import moe.hx030.momogram.utils.GsonUtil;
import moe.hx030.momogram.utils.ShareUtil;
import moe.hx030.momogram.utils.StrUtil;
import moe.hx030.momogram.utils.TelegramUtil;

@SuppressLint("RtlHardcoded")
public class MomoSettingsActivity extends BaseFragment {

    private UniversalRecyclerView listView;

    @Override
    public boolean onFragmentCreate() {
        return super.onFragmentCreate();
    }


    private static final int backup_settings = 1;
    private static final int import_settings = 2;
    private static final int reset_settings = 3;

    @SuppressLint("NewApi")
    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setTitle(LocaleController.getString(MomoConfig.useOldName.Bool() ? R.string.NekoSettings : R.string.MomoSettings));

        ActionBarMenu menu = actionBar.createMenu();
        ActionBarMenuItem otherMenu = menu.addItem(0, R.drawable.ic_ab_other);
        otherMenu.addSubItem(backup_settings, LocaleController.getString(R.string.BackupSettings));
        otherMenu.addSubItem(import_settings, LocaleController.getString(R.string.ImportSettings));
        otherMenu.addSubItem(reset_settings, LocaleController.getString(R.string.ResetSettings));

        if (AndroidUtilities.isTablet()) {
            actionBar.setOccupyStatusBar(false);
        }
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                } else if (id == backup_settings) {
                    backupSettings();
                } else if (id == import_settings) {
                    DocumentSelectActivity fragment = new DocumentSelectActivity(false);
                    fragment.setMaxSelectedFiles(1);
                    fragment.setAllowPhoto(false);
                    fragment.setDelegate(new DocumentSelectActivity.DocumentSelectActivityDelegate() {
                        @Override
                        public void didSelectFiles(DocumentSelectActivity activity, ArrayList<String> files, String caption, boolean notify, int scheduleDate) {
                            activity.finishFragment();
                            importSettings(getParentActivity(), new File(files.get(0)));
                        }

                        @Override
                        public void didSelectPhotos(ArrayList<SendMessagesHelper.SendingMediaInfo> photos, boolean notify, int scheduleDate) {
                        }

                        @Override
                        public void startDocumentSelectActivity() {
                        }
                    });
                    presentFragment(fragment);
                } else if (id == reset_settings) {
                    new AlertDialog.Builder(context)
                            .setTitle(LocaleController.getString(R.string.ResetSettings))
                            .setMessage(LocaleController.getString(R.string.ResetSettingsDesc))
                            .setPositiveButton(LocaleController.getString(R.string.OK), (__, ___) -> {
                                MomoConfig.resetModConfig();
                                promptRestartApp(context);
                            })
                            .setNegativeButton(LocaleController.getString(R.string.Cancel), null)
                            .show();
                }
            }
        });

        fragmentView = new FrameLayout(context);
        fragmentView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
        FrameLayout frameLayout = (FrameLayout) fragmentView;

        listView = new UniversalRecyclerView(this, this::fillItems, this::onClick, null);
        listView.setSections();
        frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.TOP | Gravity.LEFT));

        return fragmentView;
    }

    private void backupSettings() {

        try {
            DateFormat df = new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.getDefault());
            Date today = Calendar.getInstance().getTime();
            File cacheFile = new File(ApplicationLoader.applicationContext.getCacheDir(), df.format(today) + ".momo-settings.json");
            FileUtil.writeUtf8String(backupSettingsJson(), cacheFile);
            ShareUtil.shareFile(getParentActivity(), cacheFile);
        } catch (JSONException e) {
            AlertUtil.showSimpleAlert(getParentActivity(), e);
        }

    }

    private String backupSettingsJson() throws JSONException {

        JSONObject configJson = new JSONObject();

        ArrayList<String> userconfig = new ArrayList<>();
        userconfig.add("saveIncomingPhotos");
        userconfig.add("passcodeHash");
        userconfig.add("passcodeType");
        userconfig.add("passcodeHash");
        userconfig.add("autoLockIn");
        userconfig.add("useFingerprint");
        spToJSON("userconfing", configJson, userconfig::contains);

        ArrayList<String> mainconfig = new ArrayList<>();
        mainconfig.add("saveToGallery");
        mainconfig.add("autoplayGifs");
        mainconfig.add("autoplayVideo");
        mainconfig.add("mapPreviewType");
        mainconfig.add("raiseToSpeak");
        mainconfig.add("customTabs");
        mainconfig.add("directShare");
        mainconfig.add("shuffleMusic");
        mainconfig.add("playOrderReversed");
        mainconfig.add("inappCamera");
        mainconfig.add("repeatMode");
        mainconfig.add("fontSize");
        mainconfig.add("bubbleRadius");
        mainconfig.add("ivFontSize");
        mainconfig.add("allowBigEmoji");
        mainconfig.add("streamMedia");
        mainconfig.add("saveStreamMedia");
        mainconfig.add("smoothKeyboard");
        mainconfig.add("pauseMusicOnRecord");
        mainconfig.add("streamAllVideo");
        mainconfig.add("streamMkv");
        mainconfig.add("suggestStickers");
        mainconfig.add("sortContactsByName");
        mainconfig.add("sortFilesByName");
        mainconfig.add("noSoundHintShowed");
        mainconfig.add("directShareHash");
        mainconfig.add("useThreeLinesLayout");
        mainconfig.add("archiveHidden");
        mainconfig.add("distanceSystemType");
        mainconfig.add("loopStickers");
        mainconfig.add("keepMedia");
        mainconfig.add("noStatusBar");
        mainconfig.add("lastKeepMediaCheckTime");
        mainconfig.add("searchMessagesAsListHintShows");
        mainconfig.add("searchMessagesAsListUsed");
        mainconfig.add("stickersReorderingHintUsed");
        mainconfig.add("textSelectionHintShows");
        mainconfig.add("scheduledOrNoSoundHintShows");
        mainconfig.add("lockRecordAudioVideoHint");
        mainconfig.add("disableVoiceAudioEffects");
        mainconfig.add("chatSwipeAction");

        mainconfig.add("theme");
        mainconfig.add("selectedAutoNightType");
        mainconfig.add("autoNightScheduleByLocation");
        mainconfig.add("autoNightBrighnessThreshold");
        mainconfig.add("autoNightDayStartTime");
        mainconfig.add("autoNightDayEndTime");
        mainconfig.add("autoNightSunriseTime");
        mainconfig.add("autoNightCityName");
        mainconfig.add("autoNightSunsetTime");
        mainconfig.add("autoNightLocationLatitude3");
        mainconfig.add("autoNightLocationLongitude3");
        mainconfig.add("autoNightLastSunCheckDay");

        mainconfig.add("lang_code");

        spToJSON("mainconfig", configJson, mainconfig::contains);
        spToJSON("themeconfig", configJson, null);

        spToJSON("nkmrcfg", configJson, null);

        return configJson.toString(4);
    }

    private static void spToJSON(String sp, JSONObject object, Function<String, Boolean> filter) throws JSONException {
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences(sp, Activity.MODE_PRIVATE);
        JSONObject jsonConfig = new JSONObject();
        for (Map.Entry<String, ?> entry : preferences.getAll().entrySet()) {
            String key = entry.getKey();
            if (filter != null && !filter.apply(key)) continue;
            if (entry.getValue() instanceof Long) {
                key = key + "_long";
            } else if (entry.getValue() instanceof Float) {
                key = key + "_float";
            }
            jsonConfig.put(key, entry.getValue());
        }
        object.put(sp, jsonConfig);
    }

    public static void importSettings(Context context, File settingsFile) {

        AlertUtil.showConfirm(context,
                LocaleController.getString(R.string.ImportSettingsAlert),
                R.drawable.baseline_security_24,
                LocaleController.getString(R.string.Import),
                true,
                () -> importSettingsConfirmed(context, settingsFile));

    }

    public static void importSettingsConfirmed(Context context, File settingsFile) {

        try {
            JsonObject configJson = GsonUtil.toJsonObject(FileUtil.readUtf8String(settingsFile));
            importSettings(configJson);
            promptRestartApp(context);
        } catch (Exception e) {
            AlertUtil.showSimpleAlert(context, e);
        }

    }

    private static void promptRestartApp(Context context) {
        AlertDialog restart = new AlertDialog(context, 0);
        restart.setTitle(StrUtil.getAppName());
        restart.setMessage(LocaleController.getString(R.string.RestartAppToTakeEffect));
        restart.setPositiveButton(LocaleController.getString(R.string.OK), (__, ___) -> {
            TelegramUtil.restartApp(false);
        });
        restart.show();
    }

    @SuppressLint("ApplySharedPref")
    public static void importSettings(JsonObject configJson) throws JSONException {
        boolean hasCustomTitle = false;

        for (Map.Entry<String, JsonElement> element : configJson.entrySet()) {
            SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences(element.getKey(), Activity.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            for (Map.Entry<String, JsonElement> config : ((JsonObject) element.getValue()).entrySet()) {
                String key = config.getKey();
                JsonPrimitive value = (JsonPrimitive) config.getValue();
                if (value.isBoolean()) {
                    editor.putBoolean(key, value.getAsBoolean());
                } else if (value.isNumber()) {
                    boolean isLong = false;
                    boolean isFloat = false;
                    if (key.endsWith("_long")) {
                        key = StringsKt.substringBeforeLast(key, "_long", key);
                        isLong = true;
                    } else if (key.endsWith("_float")) {
                        key = StringsKt.substringBeforeLast(key, "_float", key);
                        isFloat = true;
                    }
                    if (isLong) {
                        editor.putLong(key, value.getAsLong());
                    } else if (isFloat) {
                        editor.putFloat(key, value.getAsFloat());
                    } else {
                        editor.putInt(key, value.getAsInt());
                    }
                } else {
                    String val = value.getAsString();
                    if (!hasCustomTitle) {
                        if (key.equals("CustomTitleText")) {
                            hasCustomTitle = true;
                            if (StringUtils.isBlank(val))
                                val = StrUtil.getAppName();
                        }
                    }
                    if (key.equals("cachePath")) {
                        if (val != null && val.contains("nekox.messenger.broken")) {
                            val = val.replace("nekox.messenger.broken", LaunchActivity.instance.getPackageName());
                        }
                    }
                    editor.putString(key, val);
                }
            }
            editor.commit();
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        if (listView != null) {
            listView.adapter.update(true);
        }
    }

    private void fillItems(ArrayList<UItem> items, UniversalAdapter adapter) {
        items.add(UItem.asSpace(AndroidUtilities.dp(4)));
        items.add(UItem.asHeader(LocaleController.getString(R.string.Categories)));
        items.add(SettingsActivity.SettingCell.Factory.of(1, 0xFF1CA5ED, 0xFF1488E1, R.drawable.msg_filled_general, getString(R.string.General)));
        items.add(SettingsActivity.SettingCell.Factory.of(2, 0xFF32C0CE, 0xFF1D9CC6, R.drawable.menu_feature_color_profile, getString(R.string.AppearanceSettings)));
        items.add(SettingsActivity.SettingCell.Factory.of(3, 0xFFF09F1B, 0xFFE18A11, R.drawable.ic_chat_bubble_white_24dp, getString(R.string.Chat)));
        items.add(SettingsActivity.SettingCell.Factory.of(4, 0xFFF45255, 0xFFDF3955, R.drawable.menu_contacts, getString(R.string.Account)));
        items.add(SettingsActivity.SettingCell.Factory.of(5, 0xFFC46EF4, 0xFF9F55DF, R.drawable.warning_sign, getString(R.string.Experiment)));
        items.add(UItem.asShadow(null));

        items.add(UItem.asHeader(LocaleController.getString(R.string.About)));
        items.add(UItem.asSettingsCell(6, LocaleController.getString(R.string.OfficialChannel), "@momogram_update"));
        items.add(UItem.asSettingsCell(7, LocaleController.getString(R.string.SourceCode), ""));
        items.add(UItem.asSettingsCell(8, LocaleController.getString(R.string.TransSite), ""));
        items.add(UItem.asShadow(null));
    }

    private void onClick(UItem item, View view, int position, float x, float y) {
        switch (item.id) {
            case 1:
                presentFragment(new MomoGeneralSettingsActivity());
                break;
            case 2:
                presentFragment(new MomoAppearanceSettingsActivity());
                break;
            case 3:
                presentFragment(new MomoChatSettingsActivity());
                break;
            case 4:
                presentFragment(new MomoAccountSettingsActivity());
                break;
            case 5:
                presentFragment(new MomoExperimentalSettingsActivity());
                break;
            case 6:
                MessagesController.getInstance(currentAccount).openByUserName("momogram_update", this, 1);
                break;
            case 7:
                Browser.openUrl(getParentActivity(), "https://github.com/im030/Momogram");
                break;
            case 8:
                Browser.openUrl(getParentActivity(), "https://hosted.weblate.org/engage/nekox_030/");
                break;
        }
    }
}
