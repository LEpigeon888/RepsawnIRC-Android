package com.franckrj.respawnirc.jvctopic.jvctopicviewers;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.franckrj.respawnirc.NetworkBroadcastReceiver;
import com.franckrj.respawnirc.R;
import com.franckrj.respawnirc.jvctopic.jvctopicgetters.JVCTopicModeForumGetter;
import com.franckrj.respawnirc.utils.ThemeManager;
import com.franckrj.respawnirc.utils.JVCParser;
import com.franckrj.respawnirc.utils.PrefsManager;
import com.franckrj.respawnirc.utils.Utils;

import java.util.ArrayList;

public class ShowTopicModeForumFragment extends AbsShowTopicFragment {
    private JVCTopicModeForumGetter getterForTopic = null;
    private boolean clearMessagesOnRefresh = true;
    private boolean autoScrollIsEnabled = true;

    private final JVCTopicModeForumGetter.NewMessagesListener listenerForNewMessages = new JVCTopicModeForumGetter.NewMessagesListener() {
        @Override
        public void getNewMessages(ArrayList<JVCParser.MessageInfos> listOfNewMessages, boolean itsReallyEmpty) {
            if (!listOfNewMessages.isEmpty()) {
                boolean scrolledAtTheEnd = false;
                isInErrorMode = false;

                if (adapterForTopic.getCount() > (adapterForTopic.getShowSurvey() ? 1 : 0)) {
                    scrolledAtTheEnd = listIsScrolledAtBottom();
                }

                adapterForTopic.removeAllItems();

                for (JVCParser.MessageInfos thisMessageInfo : listOfNewMessages) {
                    adapterForTopic.addItem(thisMessageInfo, true);
                }

                adapterForTopic.updateAllItems();

                if (autoScrollIsEnabled && (scrolledAtTheEnd || goToBottomAtPageLoading) && adapterForTopic.getCount() > 0) {
                    if (smoothScrollIsEnabled && scrolledAtTheEnd) { //s'il y avait des messages affichés avant et qu'on était en bas de page, smoothscroll
                        jvcMsgList.smoothScrollToPosition(adapterForTopic.getCount() - 1);
                    } else {
                        jvcMsgList.setSelection(adapterForTopic.getCount() - 1);
                    }
                    goToBottomAtPageLoading = false;
                }
            } else {
                if (!isInErrorMode) {
                    getterForTopic.reloadTopic(true);
                    isInErrorMode = true;
                } else {
                    Toast.makeText(getActivity(), R.string.errorDownloadFailed, Toast.LENGTH_SHORT).show();
                }
            }
        }
    };

    private final SwipeRefreshLayout.OnRefreshListener listenerForRefresh = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            if (!reloadAllTopic()) {
                swipeRefresh.setRefreshing(false);
            }
        }
    };

    public static boolean getShowNavigationButtons() {
        return true;
    }

    private boolean reloadAllTopic() {
        isInErrorMode = false;
        if (clearMessagesOnRefresh) {
            getterForTopic.resetDirectlyShowedInfos();
            adapterForTopic.disableSurvey();
            adapterForTopic.removeAllItems();
            adapterForTopic.updateAllItems();
        }
        return getterForTopic.reloadTopic();
    }

    @Override
    public void setPageLink(String newTopicLink) {
        isInErrorMode = false;

        getterForTopic.stopAllCurrentTask();
        getterForTopic.resetDirectlyShowedInfos();
        adapterForTopic.disableSurvey();
        adapterForTopic.removeAllItems();
        adapterForTopic.updateAllItems();
        getterForTopic.startGetMessagesOfThisPage(newTopicLink);
    }

    @Override
    protected void initializeGetterForMessages() {
        getterForTopic = new JVCTopicModeForumGetter();
        absGetterForTopic = getterForTopic;
    }

    @Override
    protected void initializeAdapter() {
        int showAvatarAdv;

        try {
            showAvatarAdv = Integer.valueOf(PrefsManager.getString(PrefsManager.StringPref.Names.SHOW_AVATAR_MODE_FORUM));
        } catch (Exception e) {
            showAvatarAdv = 2;
        }

        cardDesignIsEnabled = PrefsManager.getBool(PrefsManager.BoolPref.Names.ENABLE_CARD_DESIGN_MODE_FORUM);

        if (showAvatarAdv == 0 || (showAvatarAdv == 1 && NetworkBroadcastReceiver.getIsConnectedToInternet() && NetworkBroadcastReceiver.getIsConnectedWithWifi())) {
            if (cardDesignIsEnabled) {
                adapterForTopic.setIdOfLayoutToUse(R.layout.jvcmessages_avatars_card_rowforum);
            } else {
                adapterForTopic.setIdOfLayoutToUse(R.layout.jvcmessages_avatars_rowforum);
            }
            adapterForTopic.setShowAvatars(true);
            adapterForTopic.setMultiplierOfLineSizeForFirstLine(1.25f);
        } else {
            if (cardDesignIsEnabled) {
                adapterForTopic.setIdOfLayoutToUse(R.layout.jvcmessages_card_rowforum);
            } else {
                adapterForTopic.setIdOfLayoutToUse(R.layout.jvcmessages_rowforum);
            }
            adapterForTopic.setShowAvatars(false);
        }

        adapterForTopic.setAlternateBackgroundColor(PrefsManager.getBool(PrefsManager.BoolPref.Names.TOPIC_ALTERNATE_BACKGROUND_MODE_FORUM));
        adapterForTopic.setShowSignatures(PrefsManager.getBool(PrefsManager.BoolPref.Names.SHOW_SIGNATURE_MODE_FORUM));
    }

    @Override
    protected void initializeSettings() {
        super.initializeSettings();
        showRefreshWhenMessagesShowed = true;
        currentSettings.firstLineFormat = "<b><%PSEUDO_COLOR_START%><%PSEUDO_PSEUDO%><%PSEUDO_COLOR_END%></b><small><%MARK_FOR_PSEUDO%><br>Le <%DATE_COLOR_START%><%DATE_FULL%><%DATE_COLOR_END%></small>";
        currentSettings.colorPseudoUser = Utils.resColorToString(ThemeManager.getColorRes(ThemeManager.ColorName.COLOR_PSEUDO_USER), getActivity());
        currentSettings.colorPseudoOther = Utils.resColorToStringWithAlpha(ThemeManager.getColorRes(ThemeManager.ColorName.COLOR_PSEUDO_OTHER_MODE_FORUM), getActivity());
        currentSettings.colorPseudoModo = Utils.resColorToString(ThemeManager.getColorRes(ThemeManager.ColorName.COLOR_PSEUDO_MODO), getActivity());
        currentSettings.colorPseudoAdmin = Utils.resColorToString(ThemeManager.getColorRes(ThemeManager.ColorName.COLOR_PSEUDO_ADMIN), getActivity());
        currentSettings.secondLineFormat = "<%MESSAGE_MESSAGE%><%EDIT_ALL%>";
        currentSettings.addBeforeEdit = "<br /><br /><small><i>";
        currentSettings.addAfterEdit = "</i></small>";
        currentSettings.applyMarkToPseudoAuthor = PrefsManager.getBool(PrefsManager.BoolPref.Names.MARK_AUTHOR_PSEUDO_MODE_FORUM);
        clearMessagesOnRefresh = PrefsManager.getBool(PrefsManager.BoolPref.Names.TOPIC_CLEAR_ON_REFRESH_MODE_FORUM);
        autoScrollIsEnabled = PrefsManager.getBool(PrefsManager.BoolPref.Names.ENABLE_AUTO_SCROLL_MODE_FORUM);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        swipeRefresh.setOnRefreshListener(listenerForRefresh);
        getterForTopic.setListenerForNewMessages(listenerForNewMessages);

        if (getActivity() instanceof JVCTopicModeForumGetter.NewNumbersOfPagesListener) {
            getterForTopic.setListenerForNewNumbersOfPages((JVCTopicModeForumGetter.NewNumbersOfPagesListener) getActivity());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        isInErrorMode = false;

        if (adapterForTopic.getAllItems().isEmpty()) {
            getterForTopic.reloadTopic();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_showtopicforum, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_reload_topic_showtopicforum:
                reloadAllTopic();
                return true;
            case R.id.action_switch_to_IRC_mode_showtopicforum:
                if (listenerForNewModeNeeded != null) {
                    listenerForNewModeNeeded.newModeRequested(MODE_IRC);
                }
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
