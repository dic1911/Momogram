package org.telegram.ui.Components;

import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.LocaleController.getString;

import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.R;
import org.telegram.messenger.Utilities;
import org.telegram.tgnet.TLObject;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.CollapseTextCell;
import org.telegram.ui.Components.Premium.boosts.cells.selector.SelectorBtnCell;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import tw.nekomimi.nekogram.NekoConfig;

public class BlacklistUrlQueryBottomSheet extends BottomSheetWithRecyclerListView {
    private UniversalAdapter adapter;

    final private SelectorBtnCell buttonContainer;
    final private TextView actionButton;

    private String url = null;

    private boolean[] checks;

    private ArrayList<Action> queries = new ArrayList<>();

    private Set<String> queryKeys, existingKeys = getCurrentBlacklistedStrings();

    private class Action {
        int id;
        String query;

        ArrayList<TLObject> options;
        boolean checked;
        boolean[] filter;
        boolean collapsed;
        int totalCount;
        int filteredCount;
        int selectedCount;

        Action(int id, String query) {
            this.id = id;
            this.query = query;
        }

        int getCount() {
            if (filter != null) {
                return filteredCount;
            } else {
                return totalCount;
            }
        }

        boolean isExpandable() {
            return getCount() > 1;
        }

        void collapseOrExpand() {
            collapsed = !collapsed;
            adapter.update(true);
        }

        void forEach(Utilities.IndexedConsumer<TLObject> action) {
            for (int i = 0; i < totalCount; i++) {
                if (filter == null || filter[i]) {
                    action.accept(options.get(i), i);
                }
            }
        }
    }

    public BlacklistUrlQueryBottomSheet(BaseFragment fragment, String url) {
        super(fragment.getContext(), fragment, false, false, false, true, ActionBarType.SLIDING, fragment.getResourceProvider());
        this.url = url;
        setShowHandle(true);
        fixNavigationBar();
        this.takeTranslationIntoAccount = true;
        recyclerListView.setPadding(backgroundPaddingLeft, headerTotalHeight, backgroundPaddingLeft, dp(68));
        recyclerListView.setOnItemClickListener((view, position, x, y) -> {
            UItem item = adapter.getItem(position - 1);
            if (item == null) return;
            onClick(item, view, position, x, y);
        });
        this.takeTranslationIntoAccount = true;
        DefaultItemAnimator itemAnimator = new DefaultItemAnimator() {
            @Override
            protected void onMoveAnimationUpdate(RecyclerView.ViewHolder holder) {
                super.onMoveAnimationUpdate(holder);
                containerView.invalidate();
            }
        };
        itemAnimator.setSupportsChangeAnimations(false);
        itemAnimator.setDelayAnimations(false);
        itemAnimator.setInterpolator(CubicBezierInterpolator.EASE_OUT_QUINT);
        itemAnimator.setDurations(350);
        recyclerListView.setItemAnimator(itemAnimator);

        buttonContainer = new SelectorBtnCell(getContext(), resourcesProvider, null);
        buttonContainer.setClickable(true);
        buttonContainer.setOrientation(LinearLayout.VERTICAL);
        buttonContainer.setPadding(dp(10), dp(10), dp(10), dp(10));
        buttonContainer.setBackgroundColor(Theme.getColor(Theme.key_dialogBackground, resourcesProvider));

        actionButton = new TextView(getContext());
        actionButton.setLines(1);
        actionButton.setSingleLine(true);
        actionButton.setGravity(Gravity.CENTER_HORIZONTAL);
        actionButton.setEllipsize(TextUtils.TruncateAt.END);
        actionButton.setGravity(Gravity.CENTER);
        actionButton.setTextColor(Theme.getColor(Theme.key_featuredStickers_buttonText));
        actionButton.setTypeface(AndroidUtilities.bold());
        actionButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        actionButton.setText(getString(R.string.Blacklist));
        actionButton.setBackground(Theme.AdaptiveRipple.filledRect(Theme.getColor(Theme.key_featuredStickers_addButton), 6));
        actionButton.setOnClickListener(e -> proceed());
        buttonContainer.addView(actionButton, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 48, Gravity.BOTTOM | Gravity.FILL_HORIZONTAL));
        containerView.addView(buttonContainer, LayoutHelper.createFrameMarginPx(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.BOTTOM | Gravity.FILL_HORIZONTAL, backgroundPaddingLeft, 0, backgroundPaddingLeft, 0));

        Uri uri = null;
        try {
            uri = Uri.parse(url);
        } catch (Exception ex) {
            Log.e("030-url", "Error parsing url", ex);
            return;
        }
        queryKeys = uri.getQueryParameterNames();

        adapter.fillItems = this::fillItems;
        adapter.update(false);
        actionBar.setTitle(getTitle());
    }

    @Override
    protected CharSequence getTitle() {
        return getString(R.string.BlacklistUrlQueryTitle);
    }

    @Override
    protected RecyclerListView.SelectionAdapter createAdapter(RecyclerListView listView) {
        return adapter = new UniversalAdapter(listView, getContext(), currentAccount, getBaseFragment().getClassGuid(), true, null, resourcesProvider);
    }

    @Override
    public void show() {
        if (this.url == null) return;
        super.show();
        Bulletin.hideVisible();
    }

    @Override
    protected boolean canHighlightChildAt(View child, float x, float y) {
        return !(child instanceof CollapseTextCell);
    }

    private void fillItems(ArrayList<UItem> items, UniversalAdapter adapter) {
        if (queries.isEmpty()) {
            for (String q : queryKeys) {
                Action a = new Action(queries.size(), q);
                a.checked = existingKeys.contains(q);
                queries.add(a);
            }
        }

        boolean first = (checks == null);

        for (int i = 0; i < queries.size(); ++i){
            Action a = queries.get(i);
            if (!first) a.checked = checks[i];
            items.add(UItem.asRoundCheckbox(a.id, a.query).setChecked(a.checked));
        }

        if (first) {
            checks = new boolean[items.size()];
            for (int i = 0; i < queries.size(); ++i) {
                checks[i] = queries.get(i).checked;
            }
        }
    }

    private void onClick(UItem item, View view, int position, float x, float y) {
        checks[position - 1] = !checks[position - 1];
        adapter.update(false);
    }

    private Set<String> getCurrentBlacklistedStrings() {
        Set<String> blacklistSet = new HashSet<>();
        String oldBlacklistString = NekoConfig.customGetQueryBlacklist.String();
        if (!oldBlacklistString.trim().isEmpty()) {
            blacklistSet.addAll(Arrays.asList(oldBlacklistString.split(",")));
        }
        return blacklistSet;
    }

    private void proceed() {
        dismiss();

        StringBuilder sb = new StringBuilder();
        Set<String> newBlacklistSet = getCurrentBlacklistedStrings();

        int added = 0, removed = 0;

        for (int i = 0; i < checks.length; ++i) {
            String q = queries.get(i).query.trim();
            if (q.isEmpty()) continue;
            if (checks[i]) {
                sb.append(q).append(", ");
                if (newBlacklistSet.add(q)) ++added;
            } else if (newBlacklistSet.remove(q)) {
                ++removed;
            }
        }
        if (sb.indexOf(", ") > -1) sb.setLength(sb.length() - 2);

        NekoConfig.replaceCustomGetQueryBlacklist(newBlacklistSet);
        if (added > 0)
            BulletinFactory.of(getBaseFragment()).createSimpleBulletin(R.raw.done, getString(R.string.BlacklistedQuery), sb.toString()).show();
        else if (removed > 0)
            BulletinFactory.of(getBaseFragment()).createSimpleBulletin(R.raw.done, getString(R.string.BlacklistedQueryRemoved)).show();
    }
}
