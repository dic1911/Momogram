
package moe.hx030.momogram.settings;

import static org.telegram.messenger.LocaleController.getString;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.text.TextPaint;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.apache.commons.lang3.StringUtils;
import org.openintents.openpgp.OpenPgpError;
import org.openintents.openpgp.util.OpenPgpApi;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.R;
import org.telegram.messenger.SharedConfig;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.SimpleTextView;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.ThemeDescription;
import org.telegram.ui.ArticleViewer;
import org.telegram.ui.Cells.EmptyCell;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.NotificationsCheckCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Cells.TextDetailSettingsCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.Components.AlertsCreator;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.SeekBarView;
import org.telegram.ui.Components.UndoView;
import org.telegram.ui.LauncherIconController;
import org.telegram.ui.web.SearchEngine;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import kotlin.Unit;
import moe.hx030.momogram.MomoConfig;
import moe.hx030.momogram.helpers.EvilLeakerKiller;
import moe.hx030.momogram.ui.BottomBuilder;
import moe.hx030.momogram.NekoXConfig;
import moe.hx030.momogram.ui.PopupBuilder;
import moe.hx030.momogram.transtale.Translator;
import moe.hx030.momogram.transtale.TranslatorKt;
import moe.hx030.momogram.util.ReflectUtil;
import moe.hx030.momogram.utils.AlertUtil;
import moe.hx030.momogram.utils.PGPUtil;

import moe.hx030.momogram.config.ConfigItem;
import moe.hx030.momogram.config.CellGroup;
import moe.hx030.momogram.config.cell.AbstractConfigCell;
import moe.hx030.momogram.config.cell.*;

@SuppressLint("RtlHardcoded")
public class MomoGeneralSettingsActivity extends MomoSettingsBaseActivity {

    private ValueAnimator statusBarColorAnimator;
    private final AbstractConfigCell checkUpdateRow = cellGroup.appendCell(new ConfigCellTextCheck(MomoConfig.checkUpdate));
    private final AbstractConfigCell allowTestingUpdateRow = cellGroup.appendCell(new ConfigCellTextCheck(MomoConfig.allowTestingUpdate));
    private final AbstractConfigCell divider0 = cellGroup.appendCell(new ConfigCellDivider());

    private final AbstractConfigCell headerTranslation = cellGroup.appendCell(new ConfigCellHeader(LocaleController.getString(R.string.Translate)));
    private final AbstractConfigCell translationProviderRow = cellGroup.appendCell(new ConfigCellCustom(CellGroup.ITEM_TYPE_TEXT_SETTINGS_CELL, true));
    private final AbstractConfigCell useTelegramTranslateInChatRow = cellGroup.appendCell(new ConfigCellTextCheck(MomoConfig.useTelegramTranslateInChat));
    private final AbstractConfigCell translateToLangRow = cellGroup.appendCell(new ConfigCellCustom(CellGroup.ITEM_TYPE_TEXT_SETTINGS_CELL, true));
    private final AbstractConfigCell translateInputToLangRow = cellGroup.appendCell(new ConfigCellCustom(CellGroup.ITEM_TYPE_TEXT_SETTINGS_CELL, true));
    private final AbstractConfigCell googleCloudTranslateKeyRow = cellGroup.appendCell(new ConfigCellTextDetail(MomoConfig.googleCloudTranslateKey, (view, position) -> {
        customDialog_BottomInputString(position, MomoConfig.googleCloudTranslateKey, LocaleController.getString(R.string.GoogleCloudTransKeyNotice), "Key");
    }, LocaleController.getString(R.string.UsernameEmpty)));
    private final AbstractConfigCell customLingvaApiEndpointRow = cellGroup.appendCell(new ConfigCellTextDetail(MomoConfig.customLingvaInstance, (view, position) -> {
        customDialog_BottomInputString(position, MomoConfig.customLingvaInstance, LocaleController.getString(R.string.LingvaInstanceNote), "https://lingva.example.com");
    }, LocaleController.getString(R.string.None)));
    private final AbstractConfigCell customDeepLXInstanceRow = cellGroup.appendCell(new ConfigCellTextDetail(MomoConfig.customDeepLXInstance, (view, position) -> {
        customDialog_BottomInputString(position, MomoConfig.customDeepLXInstance, LocaleController.getString(R.string.TranslatorInstanceNote), "https://dplx.xi-xu.me/deepl");
    }, LocaleController.getString(R.string.None)));
    private final AbstractConfigCell preferredTranslateTargetLangRow = cellGroup.appendCell(
            new ConfigCellTextInput(LocaleController.getString(R.string.PreferredTranslateTargetLang),
                    MomoConfig.preferredTranslateTargetLang, LocaleController.getString(R.string.PreferredTranslateTargetLangExample),
                    null, MomoConfig::updatePreferredTranslateTargetLangList));
    private final AbstractConfigCell trimCOTFromTranslateResultRow = cellGroup.appendCell(new ConfigCellTextCheck(MomoConfig.trimCOTFromTranslateResult));
    private final AbstractConfigCell dividerTranslation = cellGroup.appendCell(new ConfigCellDivider());

    private final AbstractConfigCell headerMap = cellGroup.appendCell(new ConfigCellHeader(LocaleController.getString(R.string.Map)));
    private final AbstractConfigCell useOSMDroidMapRow = cellGroup.appendCell(new ConfigCellTextCheck(MomoConfig.useOSMDroidMap));
    private final AbstractConfigCell mapDriftingFixForGoogleMapsRow = cellGroup.appendCell(new ConfigCellTextCheck(MomoConfig.mapDriftingFixForGoogleMaps));
    private final AbstractConfigCell mapPreviewRow = cellGroup.appendCell(new ConfigCellSelectBox(null, MomoConfig.mapPreviewProvider,
            new String[]{
                    LocaleController.getString(R.string.MapPreviewProviderTelegram),
                    LocaleController.getString(R.string.MapPreviewProviderYandex),
                    LocaleController.getString(R.string.MapPreviewProviderNobody)
            }, null));
    private final AbstractConfigCell dividerMap = cellGroup.appendCell(new ConfigCellDivider());

    private final AbstractConfigCell headerConnection = cellGroup.appendCell(new ConfigCellHeader(LocaleController.getString(R.string.Connection)));
    private final AbstractConfigCell useIPv6Row = cellGroup.appendCell(new ConfigCellTextCheck(MomoConfig.useIPv6));
    private final AbstractConfigCell hideProxyByDefaultRow = cellGroup.appendCell(new ConfigCellTextCheck(MomoConfig.hideProxyByDefault));
    private final AbstractConfigCell useSystemDNSRow = cellGroup.appendCell(new ConfigCellTextCheck(MomoConfig.useSystemDNS));
    private final AbstractConfigCell useAdGuardDNSRow = cellGroup.appendCell(new ConfigCellTextCheck(MomoConfig.useAdGuardDNS));
    private final AbstractConfigCell customDoHRow = cellGroup.appendCell(new ConfigCellTextInput(null, MomoConfig.customDoH, "https://1.0.0.1/dns-query", null));
//    private final AbstractConfigCell customPublicProxyIPRow = cellGroup.appendCell(new ConfigCellTextDetail(MomoConfig.customPublicProxyIP, (view, position) -> {
//        customDialog_BottomInputString(position, MomoConfig.customPublicProxyIP, LocaleController.getString("customPublicProxyIPNotice"), "IP");
//    }, LocaleController.getString(R.string.UsernameEmpty)));
    private final AbstractConfigCell dividerConnection = cellGroup.appendCell(new ConfigCellDivider());

    private final AbstractConfigCell headerFolder = cellGroup.appendCell(new ConfigCellHeader(LocaleController.getString(R.string.Folder)));
    private final AbstractConfigCell openArchiveOnPullRow = cellGroup.appendCell(new ConfigCellTextCheck(MomoConfig.openArchiveOnPull));
    private final AbstractConfigCell disablePullDownSearchRow = cellGroup.appendCell(new ConfigCellTextCheck(MomoConfig.disablePullDownSearch));
    private final AbstractConfigCell unarchiveOnSwipeRow = cellGroup.appendCell(new ConfigCellTextCheck(MomoConfig.unarchiveOnSwipe));
    private final AbstractConfigCell swipeActionInTopicListRow = cellGroup.appendCell(new ConfigCellTextCheck(MomoConfig.swipeActionInTopicList));
    private final AbstractConfigCell ignoreFilterEmoticonUpdateRow = cellGroup.appendCell(new ConfigCellTextCheck(MomoConfig.ignoreFilterEmoticonUpdate));
    private final AbstractConfigCell dividerFolder = cellGroup.appendCell(new ConfigCellDivider());

    private final AbstractConfigCell header_notification = cellGroup.appendCell(new ConfigCellHeader(LocaleController.getString(R.string.NekoGeneralNotification)));
    private final AbstractConfigCell enableUnifiedPushRow = cellGroup.appendCell(new ConfigCellTextCheck(MomoConfig.enableUnifiedPush));
    private final AbstractConfigCell disableNotificationBubblesRow = cellGroup.appendCell(new ConfigCellTextCheck(MomoConfig.disableNotificationBubbles));
    private final AbstractConfigCell divider_notification = cellGroup.appendCell(new ConfigCellDivider());

    private final AbstractConfigCell header3 = cellGroup.appendCell(new ConfigCellHeader(LocaleController.getString(R.string.OpenKayChain)));
    private final AbstractConfigCell pgpAppRow = cellGroup.appendCell(new ConfigCellCustom(CellGroup.ITEM_TYPE_TEXT_SETTINGS_CELL, true));
    private final AbstractConfigCell keyRow = cellGroup.appendCell(new ConfigCellTextDetail(MomoConfig.openPGPKeyId, (view, position) -> {
        requestKey(new Intent(OpenPgpApi.ACTION_GET_SIGN_KEY_ID));
    }, "0"));
    private final AbstractConfigCell divider3 = cellGroup.appendCell(new ConfigCellDivider());

    private final AbstractConfigCell header4 = cellGroup.appendCell(new ConfigCellHeader(LocaleController.getString(R.string.DialogsSettings)));
    private final AbstractConfigCell sortMenuRow = cellGroup.appendCell(new ConfigCellSelectBox(LocaleController.getString(R.string.SortMenu), null, null, () -> {
        showSortMenuAlert();
    }));
    private final AbstractConfigCell recentChatFolderSizeRow = cellGroup.appendCell(new ConfigCellCustom(CellGroup.ITEM_TYPE_TEXT_SETTINGS_CELL, true));
    private final AbstractConfigCell divider4 = cellGroup.appendCell(new ConfigCellDivider());

    private final AbstractConfigCell header6 = cellGroup.appendCell(new ConfigCellHeader(LocaleController.getString(R.string.PrivacyTitle)));
    private final AbstractConfigCell disableSystemAccountRow = cellGroup.appendCell(new ConfigCellTextCheck(MomoConfig.disableSystemAccount));
    private final AbstractConfigCell disableAutoWebLoginRow = cellGroup.appendCell(new ConfigCellTextCheck(MomoConfig.disableAutoWebLogin));
    private final AbstractConfigCell divider6 = cellGroup.appendCell(new ConfigCellDivider());

    private final AbstractConfigCell header7 = cellGroup.appendCell(new ConfigCellHeader(LocaleController.getString(R.string.General)));
    private final AbstractConfigCell customSavePathRow = cellGroup.appendCell(new ConfigCellTextInput(null, MomoConfig.customSavePath,
            LocaleController.getString(R.string.customSavePathHint), null, null,
            (input) -> input.matches("^[A-za-z0-9.]{1,255}$") || input.isEmpty() ? input : (String) MomoConfig.customSavePath.defaultValue));
    private final AbstractConfigCell alwaysSaveToDownloadsRow = cellGroup.appendCell(new ConfigCellTextCheck(MomoConfig.alwaysSaveToDownloads));
    private final AbstractConfigCell disableUndoRow = cellGroup.appendCell(new ConfigCellTextCheck(MomoConfig.disableUndo));
    private final AbstractConfigCell inappCameraRow = cellGroup.appendCell(new ConfigCellTextCheck(MomoConfig.inappCamera));
    private final AbstractConfigCell useCamera2Row = cellGroup.appendCell(new ConfigCellCustom(CellGroup.ITEM_TYPE_TEXT_CHECK, true));
    private final AbstractConfigCell hideProxySponsorChannelRow = cellGroup.appendCell(new ConfigCellTextCheck(MomoConfig.hideProxySponsorChannel));
    private final AbstractConfigCell hideSponsoredMessageRow = cellGroup.appendCell(new ConfigCellTextCheck(MomoConfig.hideSponsoredMessage));
    private final AbstractConfigCell autoPauseVideoRow = cellGroup.appendCell(new ConfigCellTextCheck(MomoConfig.autoPauseVideo, LocaleController.getString(R.string.AutoPauseVideoAbout)));
    private final AbstractConfigCell dontAutoPlayNextMessageRow = cellGroup.appendCell(new ConfigCellTextCheck(MomoConfig.dontAutoPlayNextMessage));
    private final AbstractConfigCell openAvatarInsteadOfExpandRow = cellGroup.appendCell(new ConfigCellTextCheck(MomoConfig.openAvatarInsteadOfExpand, LocaleController.getString(R.string.OpenAvatarInsteadOfExpandDesc)));
    private final AbstractConfigCell alwaysShowDownloadsRow = cellGroup.appendCell(new ConfigCellTextCheck(MomoConfig.alwaysShowDownloads));
    private final AbstractConfigCell showSharedMediaOnOpeningProfileRow = cellGroup.appendCell(new ConfigCellTextCheck(MomoConfig.showSharedMediaOnOpeningProfile));
    private final AbstractConfigCell disableSetBirthdayReminderRow = cellGroup.appendCell(new ConfigCellTextCheck(MomoConfig.disableAddBirthdayReminder));
    private final AbstractConfigCell disableBirthdayReminderRow = cellGroup.appendCell(new ConfigCellTextCheck(MomoConfig.disableBirthdayReminder));
    private final AbstractConfigCell dontShareNumberWhenAddContactByDefaultRow = cellGroup.appendCell(new ConfigCellTextCheck(MomoConfig.dontShareNumberWhenAddContactByDefault));
    private final AbstractConfigCell allowBotInDirectShareRow = cellGroup.appendCell(new ConfigCellTextCheck(MomoConfig.allowBotInDirectShare));
    private final AbstractConfigCell scanQrCodeFromChatListRow = cellGroup.appendCell(new ConfigCellTextCheck(MomoConfig.scanQrCodeFromChatList));
    private final AbstractConfigCell customSearchEngineRow = cellGroup.appendCell(new ConfigCellTextInput(null, MomoConfig.customSearchEngine, null, null, SearchEngine::refreshSearchEngines));
    private final AbstractConfigCell searchBlacklistRow = cellGroup.appendCell(new ConfigCellTextInput(null, MomoConfig.searchBlacklist, null, null, MomoConfig::applySearchBlacklist));
    private final AbstractConfigCell overridePerformanceClassRow = cellGroup.appendCell(new ConfigCellSelectBox(LocaleController.getString(R.string.OverridePerformanceClass),
            MomoConfig.perfClassOverride, MomoConfig.perfClassOverrideOptions, null));
    private final AbstractConfigCell checkMemLeakRow = cellGroup.appendCell(new ConfigCellTextCheck(MomoConfig.checkMemLeak));
    private final AbstractConfigCell memLeakThresholdRow = cellGroup.appendCell(new ConfigCellCustom(CellGroup.ITEM_TYPE_TEXT_SETTINGS_CELL, true));
    private final AbstractConfigCell useOldNameRow = cellGroup.appendCell(new ConfigCellTextCheck(MomoConfig.useOldName, LocaleController.getString(R.string.UseOldAppNameDesc)));
    private final AbstractConfigCell noForwardToStoriesRow = cellGroup.appendCell(new ConfigCellTextCheck(MomoConfig.noForwardToStories));
    private final AbstractConfigCell disableSessionCheckerRow = cellGroup.appendCell(new ConfigCellTextCheck(MomoConfig.disableSessionChecker));
    private final AbstractConfigCell switchAccountsFromChatListMenuRow = cellGroup.appendCell(new ConfigCellTextCheck(MomoConfig.switchAccountsFromChatListMenu));
    private final AbstractConfigCell alwaysTryHevcCodecRow = cellGroup.appendCell(new ConfigCellTextCheck(MomoConfig.alwaysTryHevcCodec));

    private final AbstractConfigCell customApiIdRow = cellGroup.appendCell(new ConfigCellTextDetail(MomoConfig.customApiId, (view, position) -> {
        customDialog_BottomInputString(position, MomoConfig.customApiId, LocaleController.getString(R.string.UseCustomApiNotice), "api_id");
    }, LocaleController.getString(R.string.None)));
    private final AbstractConfigCell customApiHashRow = cellGroup.appendCell(new ConfigCellTextDetail(MomoConfig.customApiHash, (view, position) -> {
        customDialog_BottomInputString(position, MomoConfig.customApiHash, LocaleController.getString(R.string.UseCustomApiNotice), "api_hash");
    }, LocaleController.getString(R.string.None)));

    private final AbstractConfigCell divider7 = cellGroup.appendCell(new ConfigCellDivider());

    private final String instantViewAndBots = String.format("%s / %s", LocaleController.getString(R.string.ChannelBots), LocaleController.getString(R.string.OpenInstantView));
    private final AbstractConfigCell header8 = cellGroup.appendCell(new ConfigCellHeader(instantViewAndBots));
    private final AbstractConfigCell alwaysDisableSafeBrowsingInWebViewRow = cellGroup.appendCell(new ConfigCellTextCheck(MomoConfig.alwaysDisableSafeBrowsingInWebView));
    private final AbstractConfigCell closeWebViewWithoutConfirmationRow = cellGroup.appendCell(new ConfigCellTextCheck(MomoConfig.closeWebViewWithoutConfirmation));
    private final AbstractConfigCell openWebViewTabWithoutBotRow = cellGroup.appendCell(new ConfigCellTextCheck(MomoConfig.openWebViewTabWithoutBot));
    private final AbstractConfigCell disableWebViewGeolocationRow = cellGroup.appendCell(new ConfigCellTextCheck(MomoConfig.disableWebViewGeolocation));
    private final AbstractConfigCell hideWebViewTabOverlayWhenSharingRow = cellGroup.appendCell(new ConfigCellTextCheck(MomoConfig.hideWebViewTabOverlayWhenSharing));
    private final AbstractConfigCell hideWebViewTabOverlayInChatRow = cellGroup.appendCell(new ConfigCellTextCheck(MomoConfig.hideWebViewTabOverlayInChat));
    private final AbstractConfigCell preventPullDownWebviewRow = cellGroup.appendCell(new ConfigCellTextCheck(MomoConfig.preventPullDownWebview));
    private final AbstractConfigCell useBotWebviewForGamesRow = cellGroup.appendCell(new ConfigCellTextCheck(MomoConfig.useBotWebviewForGames));
    private final AbstractConfigCell confirmOpenLinkInWebViewRow = cellGroup.appendCell(new ConfigCellTextCheck(MomoConfig.confirmOpenLinkInWebView));
    private final AbstractConfigCell forceExternalBrowserForBotsRow = cellGroup.appendCell(new ConfigCellTextCheck(MomoConfig.forceExternalBrowserForBots, LocaleController.getString(R.string.ForceExternalBrowserForBotsDesc)));
    private final AbstractConfigCell openNotificationOnWebViewRow = cellGroup.appendCell(new ConfigCellTextCheck(MomoConfig.openChatOnWebView));
    private final AbstractConfigCell articleViewerBottomActionBar = cellGroup.appendCell(new ConfigCellTextCheck(MomoConfig.articleViewerBottomActionBar));
    private final AbstractConfigCell hideCocoonAISummaryRow = cellGroup.appendCell(new ConfigCellTextCheck(MomoConfig.hideCocoonAISummary));
    private final AbstractConfigCell divider8 = cellGroup.appendCell(new ConfigCellDivider());

    private final AbstractConfigCell headerAutoDownload = cellGroup.appendCell(new ConfigCellHeader(LocaleController.getString(R.string.AutoDownload)));
    private final AbstractConfigCell mapMobileDataSaverToRoamingRow = cellGroup.appendCell(new ConfigCellTextCheck(MomoConfig.mapMobileDataSaverToRoaming, LocaleController.getString(R.string.MapMobileDataSaverToRoamingNote)));
    private final AbstractConfigCell win32Row = cellGroup.appendCell(new ConfigCellTextCheck(MomoConfig.disableAutoDownloadingWin32Executable));
    private final AbstractConfigCell archiveRow = cellGroup.appendCell(new ConfigCellTextCheck(MomoConfig.disableAutoDownloadingArchive));
    private final AbstractConfigCell noPreloadTrackIfRepeatOneRow = cellGroup.appendCell(new ConfigCellTextCheck(MomoConfig.noPreloadTrackIfRepeatOne));
    private final AbstractConfigCell dividerAutoDownload = cellGroup.appendCell(new ConfigCellDivider());

    private ChatBlurAlphaSeekBar chatBlurAlphaSeekbar;
    private UndoView restartTooltip;

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();

        updateRows(true);

        return true;
    }

    @SuppressLint("NewApi")
    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setTitle(LocaleController.getString(R.string.General));

        if (AndroidUtilities.isTablet()) {
            actionBar.setOccupyStatusBar(false);
        }
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });

        listAdapter = new ListAdapter(context);

        fragmentView = new FrameLayout(context);
        fragmentView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
        FrameLayout frameLayout = (FrameLayout) fragmentView;

        // Before listAdapter
        setCanNotChange();

        listView = new RecyclerListView(context);
        listView.setVerticalScrollBarEnabled(false);
        listView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        if (listView.getItemAnimator() != null) {
            ((DefaultItemAnimator) listView.getItemAnimator()).setSupportsChangeAnimations(false);
        }
        frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.TOP | Gravity.LEFT));
        listView.setAdapter(listAdapter);

        // Fragment: Set OnClick Callbacks
        listView.setOnItemClickListener((view, position, x, y) -> {
            AbstractConfigCell a = cellGroup.rows.get(position);
            if (a instanceof ConfigCellTextCheck) {
                ((ConfigCellTextCheck) a).onClick((TextCheckCell) view);
            } else if (a instanceof ConfigCellSelectBox) {
                ((ConfigCellSelectBox) a).onClick(view);
            } else if (a instanceof ConfigCellTextInput) {
                ((ConfigCellTextInput) a).onClick();
            } else if (a instanceof ConfigCellTextDetail) {
                RecyclerListView.OnItemClickListener o = ((ConfigCellTextDetail) a).onItemClickListener;
                if (o != null) {
                    try {
                        o.onItemClick(view, position);
                    } catch (Exception e) {
                    }
                }
            } else if (a instanceof ConfigCellCustom) { // Custom OnClick
                if (position == cellGroup.rows.indexOf(pgpAppRow)) {
                    PopupBuilder builder = new PopupBuilder(view);

                    builder.addSubItem(0, LocaleController.getString(R.string.None));

                    LinkedList<String> appsMap = new LinkedList<>();
                    appsMap.add("");

                    Intent intent = new Intent(OpenPgpApi.SERVICE_INTENT_2);
                    List<ResolveInfo> resInfo = getParentActivity().getPackageManager().queryIntentServices(intent, 0);

                    if (resInfo != null) {
                        for (ResolveInfo resolveInfo : resInfo) {
                            if (resolveInfo.serviceInfo == null) {
                                continue;
                            }

                            String packageName = resolveInfo.serviceInfo.packageName;
                            String simpleName = String.valueOf(resolveInfo.serviceInfo.loadLabel(getParentActivity().getPackageManager()));

                            builder.addSubItem(appsMap.size(), simpleName);
                            appsMap.add(packageName);

                        }
                    }

                    builder.setDelegate((i) -> {
                        MomoConfig.openPGPApp.setConfigString(appsMap.get(i));
                        MomoConfig.openPGPKeyId.setConfigLong(0L);
                        listAdapter.notifyItemChanged(cellGroup.rows.indexOf(pgpAppRow));
                        listAdapter.notifyItemChanged(cellGroup.rows.indexOf(keyRow));

                        if (i > 0) PGPUtil.recreateConnection();
                    });

                    builder.show();
                } else if (position == cellGroup.rows.indexOf(translationProviderRow)) {
                    PopupBuilder builder = new PopupBuilder(view);

                    builder.setItems(new String[]{
                            LocaleController.getString(R.string.ProviderGoogleTranslate),
                            LocaleController.getString(R.string.ProviderGoogleTranslateCN),
                            LocaleController.getString(R.string.ProviderYandexTranslate),
                            LocaleController.getString(R.string.ProviderLingocloud),
                            LocaleController.getString(R.string.ProviderMicrosoftTranslator),
                            LocaleController.getString(R.string.ProviderYouDao),
                            LocaleController.getString(R.string.ProviderDeepLXTranslate),
                            LocaleController.getString(R.string.ProviderTelegramAPI),
                            LocaleController.getString(R.string.ProviderLingva),
                            LocaleController.getString(R.string.ProviderFirefox)
                    }, (i, __) -> {
                        boolean needReset = MomoConfig.translationProvider.Int() - 1 != i && (MomoConfig.translationProvider.Int() == 1 || i == 0);
                        MomoConfig.translationProvider.setConfigInt(i + 1);
                        if (needReset) {
                            updateRows(true);
                        } else {
                            listAdapter.notifyItemChanged(position);
                        }
                        return Unit.INSTANCE;
                    });
                    builder.show();
                } else if (position == cellGroup.rows.indexOf(translateToLangRow) || position == cellGroup.rows.indexOf(translateInputToLangRow)) {
                    Translator.showTargetLangSelect(view, position == cellGroup.rows.indexOf(translateInputToLangRow), (locale) -> {
                        if (position == cellGroup.rows.indexOf(translateToLangRow)) {
                            MomoConfig.translateToLang.setConfigString(TranslatorKt.getLocale2code(locale));
                        } else {
                            MomoConfig.translateInputLang.setConfigString(TranslatorKt.getLocale2code(locale));
                        }
                        listAdapter.notifyItemChanged(position);
                        return Unit.INSTANCE;
                    });
                } else if (position == cellGroup.rows.indexOf(useCamera2Row)) {
                    SharedConfig.toggleUseCamera2(currentAccount);
                    if (view instanceof TextCheckCell)
                        ((TextCheckCell) view).setChecked(SharedConfig.isUsingCamera2(currentAccount));
                } else if (position == cellGroup.rows.indexOf(recentChatFolderSizeRow)) {
                    final int[] counts = {0, 10, 20, 30, 50, Integer.MAX_VALUE};
                    List<String> types = Arrays.stream(counts)
                            .mapToObj(String::valueOf)
                            .collect(Collectors.toList());
                    PopupBuilder builder = new PopupBuilder(view);
                    builder.setItems(types, (i, str) -> {
                        MomoConfig.recentChatFolderSize.setConfigInt(Integer.parseInt(str.toString()));
                        listAdapter.notifyItemChanged(position);
                        return Unit.INSTANCE;
                    });
                    builder.show();
                } else if (position == cellGroup.rows.indexOf(memLeakThresholdRow)) {
                    final Float[] values = {1.2F, 1.5F, 1.8F, 2F};
                    List<String> options = Arrays.stream(values)
                            .map(String::valueOf)
                            .collect(Collectors.toList());
                    PopupBuilder builder = new PopupBuilder(view);
                    builder.setItems(options, (i, __) -> {
                        MomoConfig.memLeakThreshold.setConfigInt(EvilLeakerKiller.setThreshold(values[i]));
                        listAdapter.notifyItemChanged(position);
                        return Unit.INSTANCE;
                    });
                    builder.show();
                }
            }
        });
        listView.setOnItemLongClickListener((view, position, x, y) -> {
            if (position == cellGroup.rows.indexOf(checkMemLeakRow)) {
                if (EvilLeakerKiller.getInstance(null) != null)
                    AlertsCreator.createMemLeakDialog(getParentActivity(), EvilLeakerKiller.getInstance(null).checkRamUsage()).show();
                return true;
            }
            AbstractConfigCell a = cellGroup.rows.get(position);
            String key = null;
            if (ReflectUtil.hasField(a.getClass(), "bindConfig")) {
                Field cfgField = ReflectUtil.getField(a.getClass(), "bindConfig");
                try {
                    if (cfgField != null) {
                        ConfigItem cfg = (ConfigItem) cfgField.get(a);
                        key = (cfg == null) ? null : cfg.key;
                    }
                } catch (IllegalAccessException e) {
                    Log.e("030-cfg", "failed to get config field", e);
                }
            }
            if (key == null) {
                if (position == cellGroup.rows.indexOf(recentChatFolderSizeRow)) {
                    key = MomoConfig.recentChatFolderSize.key;
                } else if (position == cellGroup.rows.indexOf(memLeakThresholdRow)) {
                    key = MomoConfig.memLeakThreshold.key;
                }
            }
            Log.d("030-cfg", String.format("key = %s", String.valueOf(key)));
            if (key == null) return false;
            if (AndroidUtilities.addToClipboard(String.format("https://t.me/momosettings/?k=%s", key))) {
                BulletinFactory.of(this)
                        .createCopyBulletin(getString(R.string.LinkCopied))
                        .show();
            }
            return false;
        });

        // Cells: Set OnSettingChanged Callbacks
        cellGroup.callBackSettingsChanged = (key, newValue) -> {
            if (key.equals(MomoConfig.useIPv6.getKey())) {
                for (int a : SharedConfig.activeAccounts) {
                    if (UserConfig.getInstance(a).isClientActivated()) {
                        ConnectionsManager.native_setIpStrategy(a, ConnectionsManager.getIpStrategy());
                    }
                }
            } else if (key.equals(MomoConfig.inappCamera.getKey())) {
                SharedConfig.setInappCamera((boolean) newValue);
                restartTooltip.showWithAction(0, UndoView.ACTION_NEED_RESTART, null, null);
            } else if (key.equals(MomoConfig.transparentStatusBar.getKey())) {
                restartTooltip.showWithAction(0, UndoView.ACTION_NEED_RESTART, null, null);
            } else if (key.equals(MomoConfig.hideProxySponsorChannel.getKey())) {
                for (int a : SharedConfig.activeAccounts) {
                    if (UserConfig.getInstance(a).isClientActivated()) {
                        MessagesController.getInstance(a).checkPromoInfo(true);
                    }
                }
            } else if (key.equals(MomoConfig.actionBarDecoration.getKey())) {
                restartTooltip.showWithAction(0, UndoView.ACTION_NEED_RESTART, null, null);
            } else if (key.equals(MomoConfig.tabletMode.getKey())) {
                // default or enable = set force disable to false, otherwise true
                if ((MomoConfig.tabletMode.Int() < 2 && SharedConfig.forceDisableTabletMode) ||
                        (MomoConfig.tabletMode.Int() == 2 && !SharedConfig.forceDisableTabletMode)) {
                    SharedConfig.toggleForceDisableTabletMode();
                }
                restartTooltip.showWithAction(0, UndoView.ACTION_NEED_RESTART, null, null);
            } else if (key.equals(MomoConfig.newYear.getKey())) {
                restartTooltip.showWithAction(0, UndoView.ACTION_NEED_RESTART, null, null);
            } else if (key.equals(MomoConfig.disableSystemAccount.getKey())) {
                NekoXConfig.ensureSystemAccountState(currentAccount, (boolean) newValue);
            } else if (MomoConfig.useOSMDroidMap.getKey().equals(key)) {
                boolean enabled = (Boolean) newValue;
                ((ConfigCellTextCheck) mapDriftingFixForGoogleMapsRow).setEnabled(!enabled);
                listAdapter.notifyItemChanged(cellGroup.rows.indexOf(mapDriftingFixForGoogleMapsRow));
            } else if (key.equals(MomoConfig.useTelegramTranslateInChat.getKey())) {
                TextSettingsCell cell = (TextSettingsCell) (listView.findViewHolderForAdapterPosition(cellGroup.rows.indexOf(translationProviderRow)).itemView);
                if (MomoConfig.useTelegramTranslateInChat.Bool()) {
                    MomoConfig.translationProvider.setConfigInt(Translator.providerTelegram);
                    ((ConfigCellCustom) translationProviderRow).setEnabled(false);
                    cell.setEnabled(false);
                } else {
                    ((ConfigCellCustom) translationProviderRow).setEnabled(true);
                    cell.setEnabled(true);
                }
                listAdapter.notifyItemChanged(cellGroup.rows.indexOf(translationProviderRow));
            } else if (key.equals(MomoConfig.articleViewerBottomActionBar.getKey())) {
                ArticleViewer.BOTTOM_ACTION_BAR = (boolean) newValue;
            } else if (key.equals(MomoConfig.perfClassOverride.getKey())) {
                MomoConfig.applyPerformanceClassOverride((Integer) newValue);
            } else if (key.equals(MomoConfig.nameAsTitleText.getKey())) {
                restartTooltip.showWithAction(0, UndoView.ACTION_NEED_RESTART, null, null);
            } else if (key.equals(MomoConfig.useOldName.getKey())) {
                LauncherIconController.switchAppName((boolean) newValue);
            } else if (key.equals(MomoConfig.enableUnifiedPush.getKey())) {
                boolean enabled = ((boolean) newValue);
                // start original service if enabled
                SharedPreferences accountSettings = MessagesController.getNotificationsSettings(currentAccount);
                boolean service = accountSettings.getBoolean("pushService", false);
                boolean bgConn = accountSettings.getBoolean("pushConnection", false);
                ApplicationLoader.startPushService();
                ConnectionsManager.getInstance(currentAccount).setPushConnectionEnabled(!enabled && bgConn);
            }
        };

        //Cells: Set ListAdapter
        cellGroup.setListAdapter(listView, listAdapter);

        restartTooltip = new UndoView(context);
        frameLayout.addView(restartTooltip, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.BOTTOM | Gravity.LEFT, 8, 0, 8, 8));

        scheduleScrollToIndex();

        return fragmentView;
    }

    @Override
    protected int findScrollToIndex() {
        if (scrollToString == null && scrollToKey == null) {
            return -1;
        }
        if (scrollToKey != null) {
            if (scrollToKey.equals(MomoConfig.recentChatFolderSize.key)) {
                return cellGroup.rows.indexOf(recentChatFolderSizeRow);
            } else if (scrollToKey.equals(MomoConfig.memLeakThreshold.key)) {
                return cellGroup.rows.indexOf(memLeakThresholdRow);
            }
        }
        return super.findScrollToIndex();
    }

    private void requestKey(Intent data) {

        PGPUtil.post(() -> PGPUtil.api.executeApiAsync(data, null, null, result -> {

            switch (result.getIntExtra(OpenPgpApi.RESULT_CODE, OpenPgpApi.RESULT_CODE_ERROR)) {

                case OpenPgpApi.RESULT_CODE_SUCCESS: {

                    long keyId = result.getLongExtra(OpenPgpApi.EXTRA_SIGN_KEY_ID, 0L);
                    MomoConfig.openPGPKeyId.setConfigLong(keyId);

                    listAdapter.notifyItemChanged(cellGroup.rows.indexOf(keyRow));

                    break;
                }
                case OpenPgpApi.RESULT_CODE_USER_INTERACTION_REQUIRED: {

                    PendingIntent pi = result.getParcelableExtra(OpenPgpApi.RESULT_INTENT);
                    try {
                        Activity act = (Activity) getParentActivity();
                        act.startIntentSenderFromChild(
                                act, pi.getIntentSender(),
                                114, null, 0, 0, 0);
                    } catch (IntentSender.SendIntentException e) {
                        Log.e(OpenPgpApi.TAG, "SendIntentException", e);
                    }
                    break;
                }
                case OpenPgpApi.RESULT_CODE_ERROR: {
                    OpenPgpError error = result.getParcelableExtra(OpenPgpApi.RESULT_ERROR);
                    AlertUtil.showToast(error.getMessage());
                    break;
                }
            }

        }));


    }

    @Override
    public void onActivityResultFragment(int requestCode, int resultCode, Intent data) {
        if (requestCode == 114 && resultCode == Activity.RESULT_OK) {
            requestKey(data);
        }
    }

    private static class OpenPgpProviderEntry {
        private String packageName;
        private String simpleName;
        private Intent intent;

        OpenPgpProviderEntry(String packageName, String simpleName) {
            this.packageName = packageName;
            this.simpleName = simpleName;
        }

        OpenPgpProviderEntry(String packageName, String simpleName, Intent intent) {
            this(packageName, simpleName);
            this.intent = intent;
        }

        @Override
        public String toString() {
            return simpleName;
        }
    }


    private void showSortMenuAlert() {
        if (getParentActivity() == null) {
            return;
        }
        Context context = getParentActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(LocaleController.getString(R.string.SortMenu));

        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        LinearLayout linearLayoutInviteContainer = new LinearLayout(context);
        linearLayoutInviteContainer.setOrientation(LinearLayout.VERTICAL);
        linearLayout.addView(linearLayoutInviteContainer, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        int count = 4;
        for (int a = 0; a < count; a++) {
            TextCheckCell textCell = new TextCheckCell(context);
            switch (a) {
                case 0: {
                    textCell.setTextAndCheck(LocaleController.getString(R.string.SortByUnread), MomoConfig.sortByUnread.Bool(), false);
                    break;
                }
                case 1: {
                    textCell.setTextAndCheck(LocaleController.getString(R.string.SortByUnmuted), MomoConfig.sortByUnmuted.Bool(), false);
                    break;
                }
                case 2: {
                    textCell.setTextAndCheck(LocaleController.getString(R.string.SortByUser), MomoConfig.sortByUser.Bool(), false);
                    break;
                }
                case 3: {
                    textCell.setTextAndCheck(LocaleController.getString(R.string.SortByContacts), MomoConfig.sortByContacts.Bool(), false);
                    break;
                }
            }
            textCell.setTag(a);
            textCell.setBackgroundDrawable(Theme.getSelectorDrawable(false));
            linearLayoutInviteContainer.addView(textCell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
            textCell.setOnClickListener(view -> {
                Integer tag = (Integer) view.getTag();
                switch (tag) {
                    case 0: {
                        MomoConfig.sortByUnread.toggleConfigBool();
                        if (view instanceof TextCheckCell) {
                            ((TextCheckCell) view).setChecked(MomoConfig.sortByUnread.Bool());
                        }
                        break;
                    }
                    case 1: {
                        MomoConfig.sortByUnmuted.toggleConfigBool();
                        if (view instanceof TextCheckCell) {
                            ((TextCheckCell) view).setChecked(MomoConfig.sortByUnmuted.Bool());
                        }
                        break;
                    }
                    case 2: {
                        MomoConfig.sortByUser.toggleConfigBool();
                        if (view instanceof TextCheckCell) {
                            ((TextCheckCell) view).setChecked(MomoConfig.sortByUser.Bool());
                        }
                        break;
                    }
                    case 3: {
                        MomoConfig.sortByContacts.toggleConfigBool();
                        if (view instanceof TextCheckCell) {
                            ((TextCheckCell) view).setChecked(MomoConfig.sortByContacts.Bool());
                        }
                        break;
                    }
                }
            });
        }
        builder.setPositiveButton(LocaleController.getString(R.string.OK), null);
        builder.setView(linearLayout);
        showDialog(builder.create());
    }

    @Override
    public void onResume() {
        super.onResume();
        if (listAdapter != null) {
            listAdapter.notifyDataSetChanged();
        }
    }

    private void updateRows(boolean notify) {
        if (notify && listAdapter != null) {
            listAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public ArrayList<ThemeDescription> getThemeDescriptions() {
        ArrayList<ThemeDescription> themeDescriptions = new ArrayList<>();
        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_CELLBACKGROUNDCOLOR, new Class[]{EmptyCell.class, TextSettingsCell.class, TextCheckCell.class, HeaderCell.class, TextDetailSettingsCell.class, NotificationsCheckCell.class}, null, null, null, Theme.key_windowBackgroundWhite));
        themeDescriptions.add(new ThemeDescription(fragmentView, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_windowBackgroundGray));

        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_avatar_backgroundActionBarBlue));
        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_LISTGLOWCOLOR, null, null, null, null, Theme.key_avatar_backgroundActionBarBlue));
        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_ITEMSCOLOR, null, null, null, null, Theme.key_avatar_actionBarIconBlue));
        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_TITLECOLOR, null, null, null, null, Theme.key_actionBarDefaultTitle));
        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SELECTORCOLOR, null, null, null, null, Theme.key_avatar_actionBarSelectorBlue));
        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SUBMENUBACKGROUND, null, null, null, null, Theme.key_actionBarDefaultSubmenuBackground));
        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SUBMENUITEM, null, null, null, null, Theme.key_actionBarDefaultSubmenuItem));

        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_SELECTOR, null, null, null, null, Theme.key_listSelector));

        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{View.class}, Theme.dividerPaint, null, null, Theme.key_divider));

        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_BACKGROUNDFILTER, new Class[]{ShadowSectionCell.class}, null, null, null, Theme.key_windowBackgroundGrayShadow));

        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{TextSettingsCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText));
        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{TextSettingsCell.class}, new String[]{"valueTextView"}, null, null, null, Theme.key_windowBackgroundWhiteValueText));

        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{NotificationsCheckCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText));
        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{NotificationsCheckCell.class}, new String[]{"valueTextView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayText2));
        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{NotificationsCheckCell.class}, new String[]{"checkBox"}, null, null, null, Theme.key_switchTrack));
        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{NotificationsCheckCell.class}, new String[]{"checkBox"}, null, null, null, Theme.key_switchTrackChecked));

        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{TextCheckCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText));
        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{TextCheckCell.class}, new String[]{"valueTextView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayText2));
        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{TextCheckCell.class}, new String[]{"checkBox"}, null, null, null, Theme.key_switchTrack));
        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{TextCheckCell.class}, new String[]{"checkBox"}, null, null, null, Theme.key_switchTrackChecked));

        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{HeaderCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlueHeader));

        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{TextDetailSettingsCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText));
        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{TextDetailSettingsCell.class}, new String[]{"valueTextView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayText2));

        return themeDescriptions;
    }

    //impl ListAdapter
    private class ListAdapter extends BaseListAdapter {


        public ListAdapter(Context context) {
            super(context);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            AbstractConfigCell a = cellGroup.rows.get(position);
            SimpleTextView textView = null;
            String currentText = null;
            if (a != null) {
                if (a instanceof ConfigCellCustom) {
                    // Custom binds
                    if (holder.itemView instanceof TextSettingsCell) {
                        TextSettingsCell textCell = (TextSettingsCell) holder.itemView;
                        if (position == cellGroup.rows.indexOf(translationProviderRow)) {
                            String value;
                            switch (MomoConfig.translationProvider.Int()) {
                                case Translator.providerGoogle:
                                    value = LocaleController.getString(R.string.ProviderGoogleTranslate);
                                    break;
                                case Translator.providerGoogleCN:
                                    value = LocaleController.getString(R.string.ProviderGoogleTranslateCN);
                                    break;
                                case Translator.providerYandex:
                                    value = LocaleController.getString(R.string.ProviderYandexTranslate);
                                    break;
                                case Translator.providerLingo:
                                    value = LocaleController.getString(R.string.ProviderLingocloud);
                                    break;
                                case Translator.providerMicrosoft:
                                    value = LocaleController.getString(R.string.ProviderMicrosoftTranslator);
                                    break;
                                case Translator.providerYouDao:
                                    value = LocaleController.getString(R.string.ProviderYouDao);
                                    break;
                                case Translator.providerDeepLX:
                                    value = LocaleController.getString(R.string.ProviderDeepLXTranslate);
                                    break;
                                case Translator.providerTelegram:
                                    value = LocaleController.getString(R.string.ProviderTelegramAPI);
                                    break;
                                case Translator.providerLingva:
                                    value = LocaleController.getString(R.string.ProviderLingva);
                                    break;
                                case Translator.providerFirefox:
                                    value = LocaleController.getString(R.string.ProviderFirefox);
                                    break;
                                default:
                                    value = "Unknown";
                            }
                            textCell.setTextAndValue(LocaleController.getString(R.string.TranslationProvider), value, true);
                            if (MomoConfig.useTelegramTranslateInChat.Bool()) textCell.setEnabled(false);
                        } else if (position == cellGroup.rows.indexOf(pgpAppRow)) {
                            textCell.setTextAndValue(LocaleController.getString(R.string.OpenPGPApp), NekoXConfig.getOpenPGPAppName(), true);
                        } else if (position == cellGroup.rows.indexOf(translateToLangRow)) {
                            textCell.setTextAndValue(LocaleController.getString(R.string.TransToLang), NekoXConfig.formatLang(MomoConfig.translateToLang.String()), true);
                        } else if (position == cellGroup.rows.indexOf(translateInputToLangRow)) {
                            textCell.setTextAndValue(LocaleController.getString(R.string.TransInputToLang), NekoXConfig.formatLang(MomoConfig.translateInputLang.String()), true);
                        } else if (position == cellGroup.rows.indexOf(recentChatFolderSizeRow)) {
                            textCell.setTextAndValue(LocaleController.getString(R.string.RecentChatFolderSize),
                                    NekoXConfig.formatLang(MomoConfig.recentChatFolderSize.String()), true);
                        } else if (position == cellGroup.rows.indexOf(memLeakThresholdRow)) {
                            textCell.setTextAndValue(LocaleController.getString(R.string.MemLeakThreshold),
                                    String.format(Locale.US, "%.1fGB", ((float) MomoConfig.memLeakThreshold.Int() / 1024576)), true);
                        }
                        textView = textCell.getTextView();
                        currentText = textView.getText().toString();
                    } else if (holder.itemView instanceof TextCheckCell) {
                        TextCheckCell checkCell = (TextCheckCell) holder.itemView;
                        if (position == cellGroup.rows.indexOf(useCamera2Row)) {
                            checkCell.setTextAndCheck(LocaleController.getString(R.string.UseCamera2API),
                                    SharedConfig.isUsingCamera2(currentAccount), true);
                        }
                        textView = checkCell.getTextView();
                        currentText = textView.getText().toString();
                    }
                    if (currentText != null && currentText.equals(scrollToString)) {
                        int index = holder.getAdapterPosition();
                        if (index != scrollToIndex) {
                            setScrollToIndex(index, true);
                        }
                    }
                } else {
                    // Default binds
                    a.onBindViewHolder(holder);
                }
                // Other things
                checkScrollTo(position, holder, textView, currentText);
            }
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = null;
            switch (viewType) {
                case CellGroup.ITEM_TYPE_DIVIDER:
                    view = new ShadowSectionCell(mContext);
                    break;
                case CellGroup.ITEM_TYPE_TEXT_SETTINGS_CELL:
                    view = new TextSettingsCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case CellGroup.ITEM_TYPE_TEXT_CHECK:
                    view = new TextCheckCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case CellGroup.ITEM_TYPE_HEADER:
                    view = new HeaderCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case CellGroup.ITEM_TYPE_TEXT_DETAIL:
                    view = new TextDetailSettingsCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case CellGroup.ITEM_TYPE_TEXT:
                    view = new TextInfoPrivacyCell(mContext);
                    // view.setBackground(Theme.getThemedDrawable(mContext, R.drawable.greydivider, Theme.key_windowBackgroundGrayShadow));
                    break;
                case ConfigCellCustom.CUSTOM_ITEM_CharBlurAlpha:
                    view = chatBlurAlphaSeekbar = new ChatBlurAlphaSeekBar(mContext);
                    chatBlurAlphaSeekbar.setEnabled(MomoConfig.forceBlurInChat.Bool());
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
            }
            //noinspection ConstantConditions
            view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
            return new RecyclerListView.Holder(view);
        }
    }

    private void setCanNotChange() {
        if (!NekoXConfig.isDeveloper()) {
            cellGroup.rows.remove(hideProxySponsorChannelRow);
            cellGroup.rows.remove(hideSponsoredMessageRow);
        }

        if (!BuildVars.isGServicesCompiled) {
            MomoConfig.useOSMDroidMap.setConfigBool(true);
            ((ConfigCellTextCheck) useOSMDroidMapRow).setEnabled(false);
            cellGroup.rows.remove(mapDriftingFixForGoogleMapsRow);
        } else {
            if (MomoConfig.useOSMDroidMap.Bool())
                ((ConfigCellTextCheck) mapDriftingFixForGoogleMapsRow).setEnabled(false);
        }

        if (MomoConfig.useTelegramTranslateInChat.Bool())
            ((ConfigCellCustom) translationProviderRow).setEnabled(false);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            ((ConfigCellTextCheck) mapMobileDataSaverToRoamingRow).setEnabled(false);
        }
    }

    //Custom dialogs

    private void customDialog_BottomInputString(int position, ConfigItem bind, String subtitle, String hint) {
        BottomBuilder builder = new BottomBuilder(getParentActivity());

        String title = null;
        if (bind.getId() != 0) title = LocaleController.getString(bind.getId());
        else title = LocaleController.getString(bind.getKey());

        builder.addTitle(title, subtitle);

        EditText keyField = builder.addEditText(hint);

        if (StringUtils.isNotBlank(bind.String())) {
            keyField.setText(bind.String());
        }

        builder.addCancelButton();

        builder.addOkButton((it) -> {

            String key = keyField.getText().toString();

            if (StringUtils.isBlank(key)) key = null;

            bind.setConfigString(key);

            listAdapter.notifyItemChanged(position);

            return Unit.INSTANCE;

        });

        builder.show();

        keyField.requestFocus();
        AndroidUtilities.showKeyboard(keyField);
    }

    private class ChatBlurAlphaSeekBar extends FrameLayout {

        private final SeekBarView sizeBar;
        private final TextPaint textPaint;
        private boolean enabled = true;

        public ChatBlurAlphaSeekBar(Context context) {
            super(context);

            setWillNotDraw(false);

            textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            textPaint.setTextSize(AndroidUtilities.dp(16));

            sizeBar = new SeekBarView(context);
            sizeBar.setReportChanges(true);
            sizeBar.setDelegate(new SeekBarView.SeekBarViewDelegate() {
                @Override
                public void onSeekBarDrag(boolean stop, float progress) {
                    MomoConfig.chatBlurAlphaValue.setConfigInt(Math.min(255, (int) (255 * progress)));
                    invalidate();
                }

                @Override
                public void onSeekBarPressed(boolean pressed) {

                }
            });
            sizeBar.setOnTouchListener((v, event) -> !enabled);
            sizeBar.setProgress(MomoConfig.chatBlurAlphaValue.Int());
            addView(sizeBar, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 38, Gravity.LEFT | Gravity.TOP, 9, 5, 43, 11));
        }

        @Override
        protected void onDraw(Canvas canvas) {
            textPaint.setColor(Theme.getColor(Theme.key_windowBackgroundWhiteValueText));
            canvas.drawText(String.valueOf(MomoConfig.chatBlurAlphaValue.Int()), getMeasuredWidth() - AndroidUtilities.dp(39), AndroidUtilities.dp(28), textPaint);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            sizeBar.setProgress((MomoConfig.chatBlurAlphaValue.Int() / 255.0f));
        }

        @Override
        public void invalidate() {
            super.invalidate();
            sizeBar.invalidate();
        }

        @Override
        public void setEnabled(boolean enabled) {
            super.setEnabled(enabled);
            this.enabled = enabled;
            sizeBar.setAlpha(enabled ? 1.0f : 0.5f);
            textPaint.setAlpha((int) ((enabled ? 1.0f : 0.3f) * 255));
            this.invalidate();
        }
    }
}