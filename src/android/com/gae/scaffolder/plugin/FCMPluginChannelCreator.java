package com.gae.scaffolder.plugin;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import org.apache.cordova.CallbackContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FCMPluginChannelCreator {
    private static int INVALID_INT_OPTION = -1000;
    private static final String TAG = FCMPlugin.TAG;
    private Context context;

    public FCMPluginChannelCreator(Context context) {
        this.context = context;
    }

    class ChannelConfig {
        public String id;
        public String name;
        public String description;
        public int importance;
        public int visibility;

        public ChannelConfig(final JSONObject channelConfigJson) throws JSONException {
            this.id = channelConfigJson.getString("id");
            this.name = channelConfigJson.getString("name");
            this.description = channelConfigJson.optString("description");
            this.importance = convertImportanceStringToInt(channelConfigJson.optString("importance"));
            this.visibility = convertVisibilityStringToInt(channelConfigJson.optString("visibility"));
        }

        private int convertImportanceStringToInt(String importance) {
            switch (importance) {
                case "none":
                    return NotificationManager.IMPORTANCE_NONE;
                case "min":
                    return NotificationManager.IMPORTANCE_MIN;
                case "low":
                    return NotificationManager.IMPORTANCE_LOW;
                case "default":
                    return NotificationManager.IMPORTANCE_DEFAULT;
                case "high":
                    return NotificationManager.IMPORTANCE_HIGH;
                default:
                    return NotificationManager.IMPORTANCE_UNSPECIFIED;
            }
        }

        private int convertVisibilityStringToInt(String visibility) {
            switch (visibility) {
                case "public":
                    return Notification.VISIBILITY_PUBLIC;
                case "private":
                    return Notification.VISIBILITY_PRIVATE;
                case "secret":
                    return Notification.VISIBILITY_SECRET;
                default:
                    return INVALID_INT_OPTION;
            }
        }

        public JSONObject toJSONObject() throws JSONException {
            JSONObject json = new JSONObject();
            json.put("id", this.id);
            json.put("name", this.name);
            json.put("description", this.description);
            json.put("importance", this.importance);
            json.put("visibility", this.visibility);
            return json;
        }
    }

    public void createNotificationChannel(final CallbackContext callbackContext, final JSONArray args) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            callbackContext.success();
            return;
        }
        try {
            Log.d(TAG, "Channel started with "+args.toString());
            ChannelConfig channelConfig = new ChannelConfig(args.getJSONObject(0));
            NotificationChannel channel = new NotificationChannel(channelConfig.id, channelConfig.name, channelConfig.importance);
            if(channelConfig.visibility != INVALID_INT_OPTION) {
                channel.setLockscreenVisibility(channelConfig.visibility);
            }
            if(!channelConfig.description.equals("")) {
                channel.setDescription(channelConfig.description);
            }
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = this.context
                    .getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
            callbackContext.success();
            Log.d(TAG, "Channel finished as "+channelConfig.toJSONObject().toString());
        } catch (Exception e) {
            Log.w(TAG, "createNotificationChannel: "+e.getMessage());
            callbackContext.error(e.getMessage());
        }
    }
}
