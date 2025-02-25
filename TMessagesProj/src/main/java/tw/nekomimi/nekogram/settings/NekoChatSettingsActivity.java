package tw.nekomimi.nekogram.settings;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.TextPaint;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.Emoji;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MediaController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.ThemeDescription;
import org.telegram.ui.Cells.EmptyCell;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.NotificationsCheckCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Cells.TextDetailSettingsCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.SeekBarView;
import org.telegram.ui.Components.UndoView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import cn.hutool.core.util.ReflectUtil;
import kotlin.Unit;
import tw.nekomimi.nekogram.NekoXConfig;
import tw.nekomimi.nekogram.ui.PopupBuilder;
import tw.nekomimi.nekogram.config.CellGroup;
import tw.nekomimi.nekogram.NekoConfig;
import tw.nekomimi.nekogram.config.cell.AbstractConfigCell;
import tw.nekomimi.nekogram.config.cell.ConfigCellCustom;
import tw.nekomimi.nekogram.config.cell.ConfigCellDivider;
import tw.nekomimi.nekogram.config.cell.ConfigCellHeader;
import tw.nekomimi.nekogram.config.cell.ConfigCellSelectBox;
import tw.nekomimi.nekogram.config.cell.ConfigCellTextCheck;
import tw.nekomimi.nekogram.config.cell.ConfigCellTextDetail;
import tw.nekomimi.nekogram.config.cell.ConfigCellTextInput;
import tw.nekomimi.nekogram.helpers.WhisperHelper;

@SuppressLint("RtlHardcoded")
public class NekoChatSettingsActivity extends BaseFragment implements NotificationCenter.NotificationCenterDelegate {

    private final CellGroup cellGroup = new CellGroup(this);

    // Sticker Size
    private final AbstractConfigCell header0 = cellGroup.appendCell(new ConfigCellHeader(LocaleController.getString(R.string.StickerSize)));
    private final AbstractConfigCell stickerSizeRow = cellGroup.appendCell(new ConfigCellCustom(ConfigCellCustom.CUSTOM_ITEM_StickerSize, true));
    private final AbstractConfigCell divider0 = cellGroup.appendCell(new ConfigCellDivider());

    // Chats
    private final AbstractConfigCell header1 = cellGroup.appendCell(new ConfigCellHeader(LocaleController.getString(R.string.Chat)));

    private final AbstractConfigCell autoTranslateRow = cellGroup.appendCell(new ConfigCellTextCheck(NekoConfig.autoTranslate));
    private final AbstractConfigCell autoTranslateProviderRow = cellGroup.appendCell(new ConfigCellTextCheck(NekoConfig.useCustomProviderForAutoTranslate));
    private final AbstractConfigCell transcribeProviderRow = cellGroup.appendCell(new ConfigCellSelectBox(LocaleController.getString(R.string.TranscribeProvider),
            NekoConfig.transcribeProvider, NekoConfig.transcribeOptions, null));
    private final AbstractConfigCell cfCredentialsRow = cellGroup.appendCell(new ConfigCellCustom(CellGroup.ITEM_TYPE_TEXT_SETTINGS_CELL, true));
    private final AbstractConfigCell unreadBadgeOnBackButton = cellGroup.appendCell(new ConfigCellTextCheck(NekoConfig.unreadBadgeOnBackButton));
    private final AbstractConfigCell sendCommentAfterForwardRow = cellGroup.appendCell(new ConfigCellTextCheck(NekoConfig.sendCommentAfterForward));
    private final AbstractConfigCell smallerEmojiInChooserRow = cellGroup.appendCell(new ConfigCellTextCheck(NekoConfig.smallerEmojiInChooser));
    private final AbstractConfigCell useChatAttachMediaMenuRow = cellGroup.appendCell(new ConfigCellTextCheck(NekoConfig.useChatAttachMediaMenu, LocaleController.getString(R.string.UseChatAttachEnterMenuNotice)));
    private final AbstractConfigCell disableLinkPreviewByDefaultRow = cellGroup.appendCell(new ConfigCellTextCheck(NekoConfig.disableLinkPreviewByDefault, LocaleController.getString(R.string.DisableLinkPreviewByDefaultNotice)));
    private final AbstractConfigCell takeGIFasVideoRow = cellGroup.appendCell(new ConfigCellTextCheck(NekoConfig.takeGIFasVideo));
    private final AbstractConfigCell chooseBestVideoQualityByDefaultRow = cellGroup.appendCell(new ConfigCellTextCheck(NekoConfig.chooseBestVideoQualityByDefault));
    private final AbstractConfigCell mediaPreviewRow = cellGroup.appendCell(new ConfigCellTextCheck(NekoConfig.mediaPreview));
    private final AbstractConfigCell showSeconds = cellGroup.appendCell(new ConfigCellTextCheck(NekoConfig.showSeconds));
    private final AbstractConfigCell showBottomActionsWhenSelectingRow = cellGroup.appendCell(new ConfigCellTextCheck(NekoConfig.showBottomActionsWhenSelecting));
    private final AbstractConfigCell labelChannelUserRow = cellGroup.appendCell(new ConfigCellTextCheck(NekoConfig.labelChannelUser, LocaleController.getString(R.string.labelChannelUserDetails)));
    private final AbstractConfigCell hideSendAsChannelRow = cellGroup.appendCell(new ConfigCellTextCheck(NekoConfig.hideSendAsChannel));
    private final AbstractConfigCell hideChannelBottomMuteUnmuteRow = cellGroup.appendCell(new ConfigCellTextCheck(NekoConfig.hideChannelBottomMuteUnmute));
    private final AbstractConfigCell showSpoilersDirectlyRow = cellGroup.appendCell(new ConfigCellTextCheck(NekoConfig.showSpoilersDirectly));
    private final AbstractConfigCell showEditTimeInPopupMenuRow = cellGroup.appendCell(new ConfigCellTextCheck(NekoConfig.showEditTimeInPopupMenu));
    private final AbstractConfigCell showForwardTimeInPopupMenuRow = cellGroup.appendCell(new ConfigCellTextCheck(NekoConfig.showForwardTimeInPopupMenu));
    private final AbstractConfigCell marqueeForLongChatTitlesRow = cellGroup.appendCell(new ConfigCellTextCheck(NekoConfig.marqueeForLongChatTitles));
    private final AbstractConfigCell messageMenuRow = cellGroup.appendCell(new ConfigCellSelectBox(LocaleController.getString(R.string.MessageMenu), null, null, this::showMessageMenuAlert));
    private final AbstractConfigCell profileMenuRow = cellGroup.appendCell(new ConfigCellSelectBox(LocaleController.getString(R.string.ProfileMenu), null, null, this::showProfileMenuAlert));
    private final AbstractConfigCell disableCustomWallpaperUserRow = cellGroup.appendCell(new ConfigCellTextCheck(NekoConfig.disableCustomWallpaperUser));
    private final AbstractConfigCell disableCustomWallpaperChannelRow = cellGroup.appendCell(new ConfigCellTextCheck(NekoConfig.disableCustomWallpaperChannel));
    private final AbstractConfigCell appendOriginalTimestampRow = cellGroup.appendCell(new ConfigCellTextCheck(NekoConfig.appendOriginalTimestamp));
    private final AbstractConfigCell alwaysShowBotCommandButtonRow = cellGroup.appendCell(new ConfigCellTextCheck(NekoConfig.alwaysShowBotCommandButton));
    private final AbstractConfigCell alwaysHideBotCommandButtonRow = cellGroup.appendCell(new ConfigCellTextCheck(NekoConfig.alwaysHideBotCommandButton));
    private final AbstractConfigCell alwaysUseSpoilerForMediaRow = cellGroup.appendCell(new ConfigCellTextInput(null, NekoConfig.alwaysUseSpoilerForMedia, LocaleController.getString(R.string.AlwaysUseSpoilerForMediaDesc), null, NekoConfig::updateUseSpoilerMediaChatList));
    private final AbstractConfigCell forceHideShowAsListRow = cellGroup.appendCell(new ConfigCellTextCheck(NekoConfig.forceHideShowAsList));
    private final AbstractConfigCell largerImageMessageRow = cellGroup.appendCell(new ConfigCellTextCheck(NekoConfig.imageMessageSizeTweak));
    private final AbstractConfigCell keepBlockedBotChatHistoryRow = cellGroup.appendCell(new ConfigCellTextCheck(NekoConfig.keepBlockedBotChatHistory));
    private final AbstractConfigCell dontSendStartCmdOnUnblockBotRow = cellGroup.appendCell(new ConfigCellTextCheck(NekoConfig.dontSendStartCmdOnUnblockBot));
    private final AbstractConfigCell alwaysLoadStickerSetFromServerRow = cellGroup.appendCell(new ConfigCellTextCheck(NekoConfig.alwaysLoadStickerSetFromServer));
    private final AbstractConfigCell showChannelMsgFwdCountRow = cellGroup.appendCell(new ConfigCellTextCheck(NekoConfig.showChannelMsgFwdCount));
    private final AbstractConfigCell dividerChat = cellGroup.appendCell(new ConfigCellDivider());

    // Interactions
    private final AbstractConfigCell headerInteractions = cellGroup.appendCell(new ConfigCellHeader(LocaleController.getString(R.string.InteractionSettings)));
    private final AbstractConfigCell hideKeyboardOnChatScrollRow = cellGroup.appendCell(new ConfigCellTextCheck(NekoConfig.hideKeyboardOnChatScroll));
    private final AbstractConfigCell rearVideoMessagesRow = cellGroup.appendCell(new ConfigCellTextCheck(NekoConfig.rearVideoMessages));
    private final AbstractConfigCell disableInstantCameraRow = cellGroup.appendCell(new ConfigCellTextCheck(NekoConfig.disableInstantCamera));
    private final AbstractConfigCell hideCameraInAttachMenuRow = cellGroup.appendCell(new ConfigCellTextCheck(NekoConfig.hideCameraInAttachMenu));
    private final AbstractConfigCell disableVibrationRow = cellGroup.appendCell(new ConfigCellTextCheck(NekoConfig.disableVibration));
    private final AbstractConfigCell disableProximityEventsRow = cellGroup.appendCell(new ConfigCellTextCheck(NekoConfig.disableProximityEvents));
    private final AbstractConfigCell disableTrendingRow = cellGroup.appendCell(new ConfigCellTextCheck(NekoConfig.disableTrending));
    private final AbstractConfigCell disableSwipeToNextRow = cellGroup.appendCell(new ConfigCellTextCheck(NekoConfig.disableSwipeToNext));
    private final AbstractConfigCell disablePhotoSideActionRow = cellGroup.appendCell(new ConfigCellTextCheck(NekoConfig.disablePhotoSideAction));
    private final AbstractConfigCell disableRemoteEmojiInteractionsRow = cellGroup.appendCell(new ConfigCellTextCheck(NekoConfig.disableRemoteEmojiInteractions));
    private final AbstractConfigCell rememberAllBackMessagesRow = cellGroup.appendCell(new ConfigCellTextCheck(NekoConfig.rememberAllBackMessages));
    private final AbstractConfigCell confirmToSendCommandByClickRow = cellGroup.appendCell(new ConfigCellTextCheck(NekoConfig.confirmToSendCommandByClick));
    private final AbstractConfigCell dontSendRightAfterTranslatedRow = cellGroup.appendCell(new ConfigCellTextCheck(NekoConfig.dontSendRightAfterTranslated));
    private final AbstractConfigCell hideOriginalTextAfterTranslateRow = cellGroup.appendCell(new ConfigCellTextCheck(NekoConfig.hideOriginalTextAfterTranslate));
    private final AbstractConfigCell autoSendMessageIfBlockedBySlowModeRow = cellGroup.appendCell(new ConfigCellTextCheck(NekoConfig.autoSendMessageIfBlockedBySlowMode, LocaleController.getString(R.string.AutoSendMessageIfBlockedBySlowModeDesc)));
    private final AbstractConfigCell replyAsQuoteByDefaultRow = cellGroup.appendCell(new ConfigCellTextCheck(NekoConfig.replyAsQuoteByDefault));

    private final AbstractConfigCell increasedMaxPhotoResolutionRow = cellGroup.appendCell(new ConfigCellTextCheck(NekoConfig.increasedMaxPhotoResolution));
    private final AbstractConfigCell enhancedVideoBitrateRow = cellGroup.appendCell(new ConfigCellTextCheck(NekoConfig.enhancedVideoBitrate, LocaleController.getString(R.string.EnhancedVideoBitrateInfo)));
    private final AbstractConfigCell dividerInteractions = cellGroup.appendCell(new ConfigCellDivider());

    // Sticker
    private final AbstractConfigCell headerSticker = cellGroup.appendCell(new ConfigCellHeader(LocaleController.getString(R.string.StickerSettings)));
    private final AbstractConfigCell dontSendGreetingStickerRow = cellGroup.appendCell(new ConfigCellTextCheck(NekoConfig.dontSendGreetingSticker));
    private final AbstractConfigCell hideTimeForStickerRow = cellGroup.appendCell(new ConfigCellTextCheck(NekoConfig.hideTimeForSticker));
    private final AbstractConfigCell hideGroupStickerRow = cellGroup.appendCell(new ConfigCellTextCheck(NekoConfig.hideGroupSticker));
    private final AbstractConfigCell disablePremiumStickerAnimationRow = cellGroup.appendCell(new ConfigCellTextCheck(NekoConfig.disablePremiumStickerAnimation));
    private final AbstractConfigCell maxRecentStickerCountRow = cellGroup.appendCell(new ConfigCellCustom(CellGroup.ITEM_TYPE_TEXT_SETTINGS_CELL, true));
    private final AbstractConfigCell maxRecentEmojiCountRow = cellGroup.appendCell(new ConfigCellCustom(CellGroup.ITEM_TYPE_TEXT_SETTINGS_CELL, true));
    private final AbstractConfigCell dividerSticker = cellGroup.appendCell(new ConfigCellDivider());

    // Reaction
    private final AbstractConfigCell headerReaction = cellGroup.appendCell(new ConfigCellHeader(LocaleController.getString(R.string.ReactionSettings)));
    private final AbstractConfigCell reactionsRow = cellGroup.appendCell(new ConfigCellSelectBox(LocaleController.getString(R.string.doubleTapAndReactions),
            NekoConfig.reactions, NekoConfig.reactionsOptions, null));
    private final AbstractConfigCell disableReactionsWhenSelectingRow = cellGroup.appendCell(new ConfigCellTextCheck(NekoConfig.disableReactionsWhenSelecting));
    private final AbstractConfigCell ignoreAllReactionsRow = cellGroup.appendCell(new ConfigCellTextCheck(NekoConfig.ignoreAllReactions));
    private final AbstractConfigCell dividerReaction = cellGroup.appendCell(new ConfigCellDivider());

    // Operation Confirmatation
    private final AbstractConfigCell headerConfirms = cellGroup.appendCell(new ConfigCellHeader(LocaleController.getString(R.string.ConfirmSettings)));
    private final AbstractConfigCell askBeforeCallRow = cellGroup.appendCell(new ConfigCellTextCheck(NekoConfig.askBeforeCall));
    private final AbstractConfigCell skipOpenLinkConfirmRow = cellGroup.appendCell(new ConfigCellTextCheck(NekoConfig.skipOpenLinkConfirm));
    private final AbstractConfigCell confirmAVRow = cellGroup.appendCell(new ConfigCellTextCheck(NekoConfig.confirmAVMessage));
    private final AbstractConfigCell repeatConfirmRow = cellGroup.appendCell(new ConfigCellTextCheck(NekoConfig.repeatConfirm));
    private final AbstractConfigCell dividerConfirms = cellGroup.appendCell(new ConfigCellDivider());

    // Instant View
    private final AbstractConfigCell headerInstantView = cellGroup.appendCell(new ConfigCellHeader(LocaleController.getString(R.string.OpenInstantView)));
    private final AbstractConfigCell autoAttemptInstantViewRow = cellGroup.appendCell(new ConfigCellTextCheck(NekoConfig.autoAttemptInstantView));
    private final AbstractConfigCell useExtBrowserOnIVAttemptFailRow = cellGroup.appendCell(new ConfigCellTextCheck(NekoConfig.useExtBrowserOnIVAttemptFail));
    private final AbstractConfigCell saveIVFailDomainsRow = cellGroup.appendCell(new ConfigCellTextCheck(NekoConfig.saveIVFailDomains));
    private final AbstractConfigCell resetIVFailDomainsRow = cellGroup.appendCell(new ConfigCellSelectBox(LocaleController.getString(R.string.ResetIVFailDomains), null, null, this::resetIVFailDomains));
    private final AbstractConfigCell dividerInstantView = cellGroup.appendCell(new ConfigCellDivider());

    // Story
    private final AbstractConfigCell headerStory = cellGroup.appendCell(new ConfigCellHeader(LocaleController.getString(R.string.Story)));
    private final AbstractConfigCell disableStoriesRow = cellGroup.appendCell(new ConfigCellTextCheck(NekoConfig.disableStories));
    private final AbstractConfigCell disableSendReadStoriesRow = cellGroup.appendCell(new ConfigCellTextCheck(NekoConfig.disableSendReadStories));
    private final AbstractConfigCell dividerStory = cellGroup.appendCell(new ConfigCellDivider());

    private final AbstractConfigCell headerLinks = cellGroup.appendCell(new ConfigCellHeader(LocaleController.getString(R.string.Links)));
    private final AbstractConfigCell forceAllowChooseBrowserRow = cellGroup.appendCell(new ConfigCellTextCheck(NekoConfig.forceAllowChooseBrowser, LocaleController.getString(R.string.ForceAllowChooseBrowserDesc)));
    private final AbstractConfigCell patchAndCleanupLinksRow = cellGroup.appendCell(new ConfigCellTextCheck(NekoConfig.patchAndCleanupLinks, LocaleController.getString(R.string.PatchAndCleanupLinksDesc)));
    private final AbstractConfigCell customGetQueryBlacklistRow = cellGroup.appendCell(new ConfigCellTextInput(null, NekoConfig.customGetQueryBlacklist, null, null, NekoConfig::applyCustomGetQueryBlacklist));
    private final AbstractConfigCell dividerLinks = cellGroup.appendCell(new ConfigCellDivider());

    private final AbstractConfigCell ignoreBlockedRow = cellGroup.appendCell(new ConfigCellTextCheck(NekoConfig.ignoreBlocked, LocaleController.getString(R.string.IgnoreBlockedAbout)));
    private final AbstractConfigCell disableChatActionRow = cellGroup.appendCell(new ConfigCellTextCheck(NekoConfig.disableChatAction));
    private final AbstractConfigCell disableChoosingStickerRow = cellGroup.appendCell(new ConfigCellTextCheck(NekoConfig.disableChoosingSticker));
    private final AbstractConfigCell dividerEnd = cellGroup.appendCell(new ConfigCellDivider());



    private RecyclerListView listView;
    private ListAdapter listAdapter;
    private ActionBarMenuItem menuItem;
    private StickerSizeCell stickerSizeCell;
    private UndoView tooltip;

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();

//        NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.emojiDidLoad);
        updateRows();

        return true;
    }

    @SuppressLint("NewApi")
    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setTitle(LocaleController.getString(R.string.Chat));

        if (AndroidUtilities.isTablet()) {
            actionBar.setOccupyStatusBar(false);
        }

        ActionBarMenu menu = actionBar.createMenu();
        menuItem = menu.addItem(0, R.drawable.ic_ab_other);
        menuItem.setContentDescription(LocaleController.getString(R.string.AccDescrMoreOptions));
        menuItem.addSubItem(1, R.drawable.msg_reset, LocaleController.getString(R.string.ResetStickerSize));
        menuItem.setVisibility(NekoConfig.stickerSize.Float() != 14.0f ? View.VISIBLE : View.GONE);

        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                } else if (id == 1) {
                    NekoConfig.stickerSize.setConfigFloat(14.0f);
                    menuItem.setVisibility(View.GONE);
                    stickerSizeCell.invalidate();
                }
            }
        });

        // Before listAdapter
        if (!NekoXConfig.isDeveloper()) {
            cellGroup.rows.remove(disableChatActionRow);
            cellGroup.rows.remove(disableChoosingStickerRow);
            // cellGroup.rows.remove(ignoreBlockedRow);
            // cellGroup.rows.remove(dividerEnd);
            NekoConfig.disableChatAction.setConfigBool(false);
            NekoConfig.disableChoosingSticker.setConfigBool(false);
            // NekoConfig.ignoreBlocked.setConfigBool(false);
        }

        listAdapter = new ListAdapter(context);

        fragmentView = new FrameLayout(context);
        fragmentView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
        FrameLayout frameLayout = (FrameLayout) fragmentView;

        listView = new RecyclerListView(context);
        listView.setVerticalScrollBarEnabled(false);
        listView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
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
            } else if (a instanceof ConfigCellCustom) { // Custom onclick
                if (position == cellGroup.rows.indexOf(maxRecentStickerCountRow)) {
                    final int[] counts = {20, 30, 40, 50, 80, 100, 120, 150, 180, 200};
                    List<String> types = Arrays.stream(counts)
                            .filter(i -> i <= getMessagesController().maxRecentStickersCount)
                            .mapToObj(String::valueOf)
                            .collect(Collectors.toList());
                    PopupBuilder builder = new PopupBuilder(view);
                    builder.setItems(types, (i, str) -> {
                        NekoConfig.maxRecentStickerCount.setConfigInt(Integer.parseInt(str.toString()));
                        listAdapter.notifyItemChanged(position);
                        return Unit.INSTANCE;
                    });
                    builder.show();
                } else if (position == cellGroup.rows.indexOf(maxRecentEmojiCountRow)) {
                    final int[] counts = {24, 36, 48, 60, 72, 84, 96, 108, 120};
                    List<String> types = Arrays.stream(counts)
                            .mapToObj(String::valueOf)
                            .collect(Collectors.toList());
                    PopupBuilder builder = new PopupBuilder(view);
                    builder.setItems(types, (i, str) -> {
                        int n = Integer.parseInt(str.toString());
                        NekoConfig.maxRecentEmojiCount.setConfigInt(n);
                        Emoji.MAX_RECENT_EMOJI_COUNT = n;
                        listAdapter.notifyItemChanged(position);
                        return Unit.INSTANCE;
                    });
                    builder.show();
                } else if (position == cellGroup.rows.indexOf(cfCredentialsRow)) {
                    WhisperHelper.showCfCredentialsDialog(this);
                }
            }
        });

        // Cells: Set OnSettingChanged Callbacks
        cellGroup.callBackSettingsChanged = (key, newValue) -> {
            if (key.equals(NekoConfig.tabsTitleType.getKey())) {
                getNotificationCenter().postNotificationName(NotificationCenter.dialogFiltersUpdated);
            } else if (key.equals(NekoConfig.disableProximityEvents.getKey())) {
                MediaController.getInstance().recreateProximityWakeLock();
            } else if (key.equals(NekoConfig.showSeconds.getKey())) {
                tooltip.showWithAction(0, UndoView.ACTION_NEED_RESTART, null, null);
            }
        };

        //Cells: Set ListAdapter
        cellGroup.setListAdapter(listView, listAdapter);

        tooltip = new UndoView(context);
        frameLayout.addView(tooltip, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.BOTTOM | Gravity.LEFT, 8, 0, 8, 8));

        if (scrollToIndex > -1) {
            AndroidUtilities.runOnUIThread(() -> listView.post(() -> {
                listView.smoothScrollToPosition(scrollToIndex);
            }));
        }

        return fragmentView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (listAdapter != null) {
            listAdapter.notifyDataSetChanged();
        }
    }

    private void updateRows() {
        if (listAdapter != null) {
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

    private int scrollToIndex = -1;
    public NekoChatSettingsActivity setScrollTo(String str) {
        if (str == null) return this;
        for (int i = 0; i < cellGroup.rows.size(); ++i) {
            AbstractConfigCell c = cellGroup.rows.get(i);
            if (!ReflectUtil.hasField(c.getClass(), "title")) continue;
            String cmp = (String) ReflectUtil.getFieldValue(c, "title");
            if (str.equals(cmp)) {
                scrollToIndex = i;
                return this;
            }
        }
        return this;
    }

    private void showMessageMenuAlert() {
        if (getParentActivity() == null) {
            return;
        }
        Context context = getParentActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(LocaleController.getString(R.string.MessageMenu));

        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        LinearLayout linearLayoutInviteContainer = new LinearLayout(context);
        linearLayoutInviteContainer.setOrientation(LinearLayout.VERTICAL);
        linearLayout.addView(linearLayoutInviteContainer, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        int count = 12;
        for (int a = 0; a < count; a++) {
            TextCheckCell textCell = new TextCheckCell(context);
            switch (a) {
                case 0: {
                    textCell.setTextAndCheck(LocaleController.getString(R.string.DeleteDownloadedFile), NekoConfig.showDeleteDownloadedFile.Bool(), false);
                    break;
                }
                case 1: {
                    textCell.setTextAndCheck(LocaleController.getString(R.string.AddToSavedMessages), NekoConfig.showAddToSavedMessages.Bool(), false);
                    break;
                }
                case 2: {
                    textCell.setTextAndCheck(LocaleController.getString(R.string.Repeat), NekoConfig.showRepeat.Bool(), false);
                    break;
                }
                case 3: {
                    textCell.setTextAndCheck(LocaleController.getString(R.string.ViewHistory), NekoConfig.showViewHistory.Bool(), false);
                    break;
                }
                case 4: {
                    textCell.setTextAndCheck(LocaleController.getString(R.string.Translate), NekoConfig.showTranslate.Bool(), false);
                    break;
                }
                case 5: {
                    textCell.setTextAndCheck(LocaleController.getString(R.string.ReportChat), NekoConfig.showReport.Bool(), false);
                    break;
                }
                case 6: {
                    textCell.setTextAndCheck(LocaleController.getString(R.string.EditAdminRights), NekoConfig.showAdminActions.Bool(), false);
                    break;
                }
                case 7: {
                    textCell.setTextAndCheck(LocaleController.getString(R.string.ChangePermissions), NekoConfig.showChangePermissions.Bool(), false);
                    break;
                }
                case 8: {
                    textCell.setTextAndCheck(LocaleController.getString(R.string.Hide), NekoConfig.showMessageHide.Bool(), false);
                    break;
                }
                case 9: {
                    textCell.setTextAndCheck(LocaleController.getString(R.string.ShareMessages), NekoConfig.showShareMessages.Bool(), false);
                    break;
                }
                case 10: {
                    textCell.setTextAndCheck(LocaleController.getString(R.string.MessageDetails), NekoConfig.showMessageDetails.Bool(), false);
                    break;
                }
                case 11: {
                    textCell.setTextAndCheck(LocaleController.getString(R.string.CopyPhotoSticker), NekoConfig.showCopyPhoto.Bool(), false);
                }
            }
            textCell.setTag(a);
            textCell.setBackground(Theme.getSelectorDrawable(false));
            linearLayoutInviteContainer.addView(textCell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
            textCell.setOnClickListener(v2 -> {
                Integer tag = (Integer) v2.getTag();
                switch (tag) {
                    case 0: {
                        textCell.setChecked(NekoConfig.showDeleteDownloadedFile.toggleConfigBool());
                        break;
                    }
                    case 1: {
                        textCell.setChecked(NekoConfig.showAddToSavedMessages.toggleConfigBool());
                        break;
                    }
                    case 2: {
                        textCell.setChecked(NekoConfig.showRepeat.toggleConfigBool());
                        break;
                    }
                    case 3: {
                        textCell.setChecked(NekoConfig.showViewHistory.toggleConfigBool());
                        break;
                    }
                    case 4: {
                        textCell.setChecked(NekoConfig.showTranslate.toggleConfigBool());
                        break;
                    }
                    case 5: {
                        textCell.setChecked(NekoConfig.showReport.toggleConfigBool());
                        break;
                    }
                    case 6: {
                        textCell.setChecked(NekoConfig.showAdminActions.toggleConfigBool());
                        break;
                    }
                    case 7: {
                        textCell.setChecked(NekoConfig.showChangePermissions.toggleConfigBool());
                        break;
                    }
                    case 8: {
                        textCell.setChecked(NekoConfig.showMessageHide.toggleConfigBool());
                        break;
                    }
                    case 9: {
                        textCell.setChecked(NekoConfig.showShareMessages.toggleConfigBool());
                        break;
                    }
                    case 10: {
                        textCell.setChecked(NekoConfig.showMessageDetails.toggleConfigBool());
                        break;
                    }
                    case 11: {
                        textCell.setChecked(NekoConfig.showCopyPhoto.toggleConfigBool());
                        break;
                    }
                }
            });
        }
        builder.setPositiveButton(LocaleController.getString(R.string.OK), null);
        builder.setView(linearLayout);
        showDialog(builder.create());
    }

    private void showProfileMenuAlert() {
        if (getParentActivity() == null) {
            return;
        }
        Context context = getParentActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(LocaleController.getString(R.string.ProfileMenu));

        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        LinearLayout linearLayoutInviteContainer = new LinearLayout(context);
        linearLayoutInviteContainer.setOrientation(LinearLayout.VERTICAL);
        linearLayout.addView(linearLayoutInviteContainer, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        int count = 6;
        for (int a = 0; a < count; a++) {
            TextCheckCell textCell = new TextCheckCell(context);
            switch (a) {
                case 0: {
                    textCell.setTextAndCheck(String.format("%s/%s",
                                    LocaleController.getString(R.string.LinkedChannel),
                                    LocaleController.getString(R.string.LinkedGroupChat)),
                            NekoConfig.profileShowLinkedChat.Bool(), false);
                    break;
                }
                case 1: {
                    textCell.setTextAndCheck(LocaleController.getString(R.string.FilterAddTo), NekoConfig.profileShowAddToFolder.Bool(), false);
                    break;
                }
                case 2: {
                    textCell.setTextAndCheck(LocaleController.getString(R.string.EventLog), NekoConfig.profileShowRecentActions.Bool(), false);
                    break;
                }
                case 3: {
                    textCell.setTextAndCheck(LocaleController.getString(R.string.ClearCache), NekoConfig.profileShowClearCache.Bool(), false);
                    break;
                }
                case 4: {
                    textCell.setTextAndCheck(LocaleController.getString(R.string.SearchBlacklistShort), NekoConfig.profileShowBlockSearch.Bool(), false);
                    break;
                }
                case 5: {
                    textCell.setTextAndCheck(LocaleController.getString(R.string.SpoilerOnAllMedia), NekoConfig.profileShowSpoilerOnAllMedia.Bool(), false);
                    break;
                }
            }
            textCell.setTag(a);
            textCell.setBackground(Theme.getSelectorDrawable(false));
            linearLayoutInviteContainer.addView(textCell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
            textCell.setOnClickListener(v2 -> {
                Integer tag = (Integer) v2.getTag();
                switch (tag) {
                    case 0: {
                        textCell.setChecked(NekoConfig.profileShowLinkedChat.toggleConfigBool());
                        break;
                    }
                    case 1: {
                        textCell.setChecked(NekoConfig.profileShowAddToFolder.toggleConfigBool());
                        break;
                    }
                    case 2: {
                        textCell.setChecked(NekoConfig.profileShowRecentActions.toggleConfigBool());
                        break;
                    }
                    case 3: {
                        textCell.setChecked(NekoConfig.profileShowClearCache.toggleConfigBool());
                        break;
                    }
                    case 4: {
                        textCell.setChecked(NekoConfig.profileShowBlockSearch.toggleConfigBool());
                        break;
                    }
                    case 5: {
                        textCell.setChecked(NekoConfig.profileShowSpoilerOnAllMedia.toggleConfigBool());
                        break;
                    }
                }
            });
        }
        builder.setPositiveButton(LocaleController.getString(R.string.OK), null);
        builder.setView(linearLayout);
        showDialog(builder.create());
    }

    private void resetIVFailDomains() {
        Context context = getParentActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(LocaleController.getString(R.string.OpenInstantView));
        builder.setMessage(LocaleController.getString(R.string.ResetIVFailDomainsConfirm));
        builder.setPositiveButton(LocaleController.getString(R.string.OK), (__, ___) -> {
            NekoXConfig.resetInstantViewFailedDomains();
            BulletinFactory.of(this)
                    .createSimpleBulletin(R.raw.info, LocaleController.getString(R.string.DataCleared))
                    .show();
        });
        builder.setNegativeButton(LocaleController.getString(R.string.Cancel), null);
        builder.show();
    }

    @Override
    public void didReceivedNotification(int id, int account, Object... args) {
       /* if (id == NotificationCenter.emojiDidLoad) {
            if (listView != null) {
                listView.invalidateViews();
            }
        }*/
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
//        NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.emojiDidLoad);
    }

    private class StickerSizeCell extends FrameLayout {

        private final StickerSizePreviewMessagesCell messagesCell;
        private final SeekBarView sizeBar;
        private final int startStickerSize = 2;
        private final int endStickerSize = 20;

        private final TextPaint textPaint;

        public StickerSizeCell(Context context) {
            super(context);

            setWillNotDraw(false);

            textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            textPaint.setTextSize(AndroidUtilities.dp(16));

            sizeBar = new SeekBarView(context);
            sizeBar.setReportChanges(true);
            sizeBar.setDelegate(new SeekBarView.SeekBarViewDelegate() {
                @Override
                public void onSeekBarDrag(boolean stop, float progress) {
                    NekoConfig.stickerSize.setConfigFloat(startStickerSize + (endStickerSize - startStickerSize) * progress);
                    StickerSizeCell.this.invalidate();
                    menuItem.setVisibility(View.VISIBLE);
                }

                @Override
                public void onSeekBarPressed(boolean pressed) {

                }
            });
            addView(sizeBar, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 38, Gravity.LEFT | Gravity.TOP, 9, 5, 43, 11));

            messagesCell = new StickerSizePreviewMessagesCell(context, parentLayout);
            addView(messagesCell, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.TOP, 0, 53, 0, 0));
        }

        @Override
        protected void onDraw(Canvas canvas) {
            textPaint.setColor(Theme.getColor(Theme.key_windowBackgroundWhiteValueText));
            canvas.drawText("" + Math.round(NekoConfig.stickerSize.Float()), getMeasuredWidth() - AndroidUtilities.dp(39), AndroidUtilities.dp(28), textPaint);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            sizeBar.setProgress((NekoConfig.stickerSize.Float() - startStickerSize) / (float) (endStickerSize - startStickerSize));
        }

        @Override
        public void invalidate() {
            super.invalidate();
            messagesCell.invalidate();
            sizeBar.invalidate();
        }
    }

    //impl ListAdapter
    private class ListAdapter extends RecyclerListView.SelectionAdapter {

        private final Context mContext;

        public ListAdapter(Context context) {
            mContext = context;
        }

        @Override
        public int getItemCount() {
            return cellGroup.rows.size();
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            int position = holder.getAdapterPosition();
            AbstractConfigCell a = cellGroup.rows.get(position);
            if (a != null) {
                return a.isEnabled();
            }
            return true;
        }

        @Override
        public int getItemViewType(int position) {
            AbstractConfigCell a = cellGroup.rows.get(position);
            if (a != null) {
                return a.getType();
            }
            return CellGroup.ITEM_TYPE_TEXT_DETAIL;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            AbstractConfigCell a = cellGroup.rows.get(position);
            if (a != null) {
                if (a instanceof ConfigCellCustom) {
                    // Custom binds
                    if (holder.itemView instanceof TextSettingsCell) {
                        TextSettingsCell textCell = (TextSettingsCell) holder.itemView;
                        if (position == cellGroup.rows.indexOf(maxRecentStickerCountRow)) {
                            textCell.setTextAndValue(LocaleController.getString(R.string.maxRecentStickerCount), String.valueOf(NekoConfig.maxRecentStickerCount.Int()), true);
                        } else if (position == cellGroup.rows.indexOf(maxRecentEmojiCountRow)) {
                            textCell.setTextAndValue(LocaleController.getString(R.string.maxRecentEmojiCount), String.valueOf(NekoConfig.maxRecentEmojiCount.Int()), true);
                        } else if (position == cellGroup.rows.indexOf(cfCredentialsRow)) {
                            textCell.setTextAndValue(LocaleController.getString(R.string.CloudflareCredentials), "", true);
                        }
                    }
                } else {
                    // Default binds
                    a.onBindViewHolder(holder);
                }
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
                case ConfigCellCustom.CUSTOM_ITEM_StickerSize:
                    view = stickerSizeCell = new StickerSizeCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
            }
            //noinspection ConstantConditions
            view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
            return new RecyclerListView.Holder(view);
        }
    }
}
