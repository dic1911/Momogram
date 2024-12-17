package tw.nekomimi.nekogram;

import android.util.Log;

import org.telegram.messenger.AccountInstance;
import org.telegram.messenger.BuildConfig;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.SharedConfig;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.TLRPC;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import tw.nekomimi.nekogram.utils.FileUtil;

//TODO use UpdateAppAlertDialog / BlockingUpdateView?

public class MomoUpdater {

    static final int UPDATE_METADATA_START_FROM = 0;
    static final int MAX_READ_COUNT = 20;
    static final long CHANNEL_ID = 2137047153;
    public static final String CHANNEL_NAME = "momogram_update";

    static void retrieveUpdateMetadata(retrieveUpdateMetadataCallback callback) {
        final int localVersionCode = SharedConfig.buildVersion();
        AccountInstance accountInstance = AccountInstance.getInstance(UserConfig.selectedAccount);
        TLRPC.TL_messages_getHistory req = new TLRPC.TL_messages_getHistory();
        req.peer = accountInstance.getMessagesController().getInputPeer(-CHANNEL_ID);
        req.offset_id = 0;
        req.limit = MAX_READ_COUNT;
        Runnable sendReq = () -> accountInstance.getConnectionsManager().sendRequest(req, (response, error) -> {
            if (error != null) {
                FileLog.e("Error when retrieving update metadata from channel " + error);
                callback.apply(null, true);
                return;
            }
            try {
                TLRPC.messages_Messages res = (TLRPC.messages_Messages) response;
                List<UpdateMetadata> metas = new ArrayList<>();
                for (TLRPC.Message message : res.messages) {
                    if (!(message instanceof TLRPC.TL_message)) continue;
                    if (!message.message.contains("#release")) continue;
                    UpdateMetadata metaData = new UpdateMetadata(message.id, message.message);
                    metas.add(metaData);
                    if (BuildVars.DEBUG_PRIVATE_VERSION || metaData.versionCode > localVersionCode) {
                        metaData.updateLog = message.message;
                        metaData.updateLogEntities = message.entities;
                    }
                }
                Collections.sort(metas, (o1, o2) -> o2.versionCode - o1.versionCode); // versionCode Desc
                if (metas.isEmpty() || (metas.get(0).versionCode < localVersionCode)) {
                    Log.d("030-upd", "Cannot find Update Metadata");
                    callback.apply(null, false);
                    return;
                }
                Log.d("030-upd", "Found Update Metadata " + metas.get(0).versionName + " " + metas.get(0).versionCode);
                callback.apply((BuildVars.DEBUG_PRIVATE_VERSION || metas.get(0).versionCode > localVersionCode) ? metas.get(0) : null, false);
            } catch (Exception e) {
                FileLog.e(e);
                callback.apply(null, true);
            }
        });
        if (req.peer.access_hash != 0) sendReq.run();
        else {
            TLRPC.TL_contacts_resolveUsername resolve = new TLRPC.TL_contacts_resolveUsername();
            resolve.username = CHANNEL_NAME;
            accountInstance.getConnectionsManager().sendRequest(resolve, (response1, error1) -> {
                if (error1 != null) {
                    Log.e("030-upd", "Error when checking update, unable to resolve metadata channel " + error1.text);
                    callback.apply(null, true);
                    return;
                }
                if (!(response1 instanceof TLRPC.TL_contacts_resolvedPeer)) {
                    Log.e("030-upd", "Error when checking update, unable to resolve metadata channel, unexpected responseType " + response1.getClass().getName());
                    callback.apply(null, true);
                    return;
                }
                TLRPC.TL_contacts_resolvedPeer resolvedPeer = (TLRPC.TL_contacts_resolvedPeer) response1;
                accountInstance.getMessagesController().putUsers(resolvedPeer.users, false);
                accountInstance.getMessagesController().putChats(resolvedPeer.chats, false);
                accountInstance.getMessagesStorage().putUsersAndChats(resolvedPeer.users, resolvedPeer.chats, false, true);
                if ((resolvedPeer.chats == null || resolvedPeer.chats.size() == 0)) {
                    Log.e("030-upd", "Error when checking update, unable to resolve metadata channel, unexpected resolvedChat ");
                    callback.apply(null, true);
                    return;
                }
                req.peer = new TLRPC.TL_inputPeerChannel();
                req.peer.channel_id = resolvedPeer.chats.get(0).id;
                req.peer.access_hash = resolvedPeer.chats.get(0).access_hash;
                sendReq.run();
            });
        }
    }

    public static void checkUpdate(checkUpdateCallback callback) {
        NekoConfig.nextPromptUpdateTime.setConfigLong(0L);
        AccountInstance accountInstance = AccountInstance.getInstance(UserConfig.selectedAccount);
        retrieveUpdateMetadata((metadata, err) -> {
            if (metadata == null) {
                Log.d("030-upd", "null metadata, err: " + err);
                callback.apply(null, err);
                return;
            }

            TLRPC.TL_messages_getHistory req = new TLRPC.TL_messages_getHistory();
            req.peer = accountInstance.getMessagesController().getInputPeer(-CHANNEL_ID);
            req.min_id = metadata.messageID - 4;
            req.limit = MAX_READ_COUNT;

            Runnable sendReq = () -> accountInstance.getConnectionsManager().sendRequest(req, (response, error) -> {
                if (error != null) {
                    Log.e("030-upd", "Error when getting update document " + error.text);
                    callback.apply(null, true);
                    return;
                }
                try {
                    TLRPC.messages_Messages res = (TLRPC.messages_Messages) response;
                    Log.e("030-upd", "Retrieve update messages, size:" + res.messages.size());
                    final boolean isArm = FileUtil.getAbi().startsWith("arm");
                    final String versionStr = String.format("%s-%s", metadata.versionName, metadata.versionHash);
                    Log.d("030-upd", String.format("searching for %s, isArm: %s", versionStr, isArm));
                    for (int i = 0; i < res.messages.size(); i++) {
                        if (res.messages.get(i).media == null) continue;

                        TLRPC.Document apkDocument = res.messages.get(i).media.document;
                        if (apkDocument.attributes == null) continue;
                        String fileName = apkDocument.attributes.size() == 0 ? "" : apkDocument.attributes.get(0).file_name;
                        if (!fileName.contains(versionStr) || (isArm && fileName.contains("x86")) || (!isArm && !fileName.contains("x86")))
                            continue;
                        TLRPC.TL_help_appUpdate update = new TLRPC.TL_help_appUpdate();
                        update.version = metadata.versionName;
//                        update.document = apkDocument;
                        update.url = String.format("https://t.me/%s/%d", CHANNEL_NAME, res.messages.get(i).id);
                        update.can_not_skip = false;
                        update.flags |= 2;
                        if (metadata.updateLog != null) {
                            update.text = metadata.updateLog;
                            update.entities = metadata.updateLogEntities;
                        }
                        callback.apply(update, false);
                        return;
                    }
                    Log.d("030-upd", "no file");
                    callback.apply(null, false);
                } catch (Exception e) {
                    FileLog.e(e);
                    callback.apply(null, true);
                }
            });
            if (req.peer.access_hash != 0) sendReq.run();
            else {
                TLRPC.TL_contacts_resolveUsername resolve = new TLRPC.TL_contacts_resolveUsername();
                resolve.username = CHANNEL_NAME;
                accountInstance.getConnectionsManager().sendRequest(resolve, (response1, error1) -> {
                    if (error1 != null) {
                        Log.e("030-upd", "Error when checking update, unable to resolve metadata channel " + error1);
                        callback.apply(null, true);
                        return;
                    }
                    if (!(response1 instanceof TLRPC.TL_contacts_resolvedPeer)) {
                        Log.e("030-upd", "Error when checking update, unable to resolve metadata channel, unexpected responseType " + response1.getClass().getName());
                        callback.apply(null, true);
                        return;
                    }
                    TLRPC.TL_contacts_resolvedPeer resolvedPeer = (TLRPC.TL_contacts_resolvedPeer) response1;
                    accountInstance.getMessagesController().putUsers(resolvedPeer.users, false);
                    accountInstance.getMessagesController().putChats(resolvedPeer.chats, false);
                    accountInstance.getMessagesStorage().putUsersAndChats(resolvedPeer.users, resolvedPeer.chats, false, true);
                    if ((resolvedPeer.chats == null || resolvedPeer.chats.size() == 0)) {
                        Log.e("030-upd", "Error when checking update, unable to resolve metadata channel, unexpected resolvedChat ");
                        callback.apply(null, true);
                        return;
                    }
                    req.peer = new TLRPC.TL_inputPeerChannel();
                    req.peer.channel_id = resolvedPeer.chats.get(0).id;
                    req.peer.access_hash = resolvedPeer.chats.get(0).access_hash;
                    sendReq.run();
                });
            }
        });


    }

    public interface retrieveUpdateMetadataCallback {
        void apply(UpdateMetadata metadata, boolean error);
    }

    public interface checkUpdateCallback {
        void apply(TLRPC.TL_help_appUpdate resp, boolean error);
    }

    static class UpdateMetadata {
        int messageID;
        String versionName;
        String versionHash;
        int versionCode;
        String updateLog = null;
        ArrayList<TLRPC.MessageEntity> updateLogEntities = null;

        UpdateMetadata(int messageID, String text) {
            this.messageID = messageID;
            String[] lines = text.split("\n");
            try {
                String[] split = lines[0].split(" ");
                versionName = split[1];
                versionHash = split[2];
                versionCode = Integer.parseInt(split[3].split("r")[1]) * 10 + 9;
                updateLog = text.replace(lines[0] + "\n\n", "");
            } catch (Exception e) {
                FileLog.e(e);
            }
        }
    }

}