package com.skp.di.rake.client.api;

import android.content.Context;
import android.content.SharedPreferences;

import com.skp.di.rake.client.android.SystemInformation;
import com.skp.di.rake.client.core.RakeCore;
import com.skp.di.rake.client.protocol.ShuttleProtocol;
import com.skp.di.rake.client.utils.RakeLogger;
import com.skp.di.rake.client.utils.RakeLoggerFactory;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

public class Rake {
    private RakeCore core;
    private RakeUserConfig config;
    private SystemInformation sysInfo;

    private RakeLogger logger;

    public Rake(RakeUserConfig config, RakeCore core, Context context, SystemInformation sysInfo) {
        this.config = config;
        this.core = core;
        this.sysInfo = sysInfo;
        this.superProperties = new JSONObject();
        this.logger = RakeLoggerFactory.getLogger(this.getClass(), config);

        legacySupport(context, config);
    }

    // no synchronized needed. since caller doesn't care consistency thanks to Um.
    public void track(JSONObject shuttle) {
        if (null == shuttle) return;
        if (shuttle.toString().equals("{\"\":\"\"}")) return;

        JSONObject trackable = null;

        try {
            JSONObject defaultProperties = sysInfo.getDefaultProperties(config);
            trackable = ShuttleProtocol.getTrackable(shuttle, superProperties, defaultProperties);
        } catch (JSONException e) {
            logger.e("Can't build trackable", e);
        } catch (Exception e) {
            logger.e("Can't build trackable. Due to invalid shuttle", e);
        }

        if (null != trackable) core.track(trackable);
    }

    public void flush() {
        core.flush();
    }

    public void setFlushInterval(long milliseconds) {
        core.setFlushInterval(milliseconds);
    }
























    /* Legacy API */
    private JSONObject superProperties;

    public void setFlushInterval(Context context, long milliseconds) {
        setFlushInterval(milliseconds);
    }
    public void setDebug(Boolean debug) {}
    public void setRakeServer(Context onctext, String server) {}

    public boolean hasSuperProperty(String superPropertyName) {
        synchronized (this.superProperties) {
            return this.superProperties.has(superPropertyName);
        }
    }

    public Object getSuperPropertyValue(String superPropertyName) throws JSONException {
        synchronized (this.superProperties) {
            return this.superProperties.get(superPropertyName);
        }
    }

    public void registerSuperProperties(JSONObject superProperties) {
        logger.i("add super-properties: \n" + superProperties);
        addSuperProperties(superProperties, false);
    }

    public void registerSuperPropertiesOnce(JSONObject superProperties) {
        logger.i("add super-properties once: \n" + superProperties);
        addSuperProperties(superProperties, true);
    }

    private void addSuperProperties(JSONObject superProps, boolean once) {
        synchronized (this.superProperties) {
            try {
                Iterator<String> iter = superProps.keys();

                while(iter.hasNext()) {
                    String key = iter.next();

                    if (once && this.superProperties.has(key)) continue;

                    this.superProperties.put(key, superProps.get(key));
                }
            } catch (JSONException e) {
                logger.e("Can't add super property", e);
            }
        }

        logger.i("total super-properties: \n" + this.superProperties);
        savePreferences();
    }

    public void unregisterSuperProperties(String superPropertyName) {
        synchronized (this.superProperties) {
            this.superProperties.remove(superPropertyName);
        }

        logger.i("unregister super-property: " + superPropertyName);
        logger.i("total super-properties: \n" + this.superProperties);
        savePreferences();
    }

    public void clearSuperProperties() {
        synchronized (this.superProperties) {
            this.superProperties = new JSONObject();
        }

        logger.i("clear all super-properties, now super-properties: \n" + this.superProperties);
        clearPreferences();
    }

    private SharedPreferences sharedPref;

    private void legacySupport(Context context, RakeUserConfig config) {
        String path = "com.skp.di.rake.client.api.Rake_" + config.getToken();
        sharedPref = context.getSharedPreferences(path, Context.MODE_PRIVATE);

        readPreferences();
    }

    private void readPreferences() {
        String props = sharedPref.getString("super_properties", "{}");

        synchronized (this.superProperties) {
            try {
                superProperties = new JSONObject(props);
            } catch (JSONException e) {
                logger.e("Cannot parse stored superProperties", e);
                superProperties = new JSONObject();
                clearPreferences();
            }
        }

        logger.i("read super-properties from SharedPref, now super-properties: \n" + this.superProperties);
    }

    private void savePreferences() {
        synchronized (this.superProperties) {
            String props = superProperties.toString();
            SharedPreferences.Editor prefsEditor = sharedPref.edit();
            prefsEditor.putString("super_properties", props);
            prefsEditor.commit();
        }
    }

    private void clearPreferences() {
        SharedPreferences.Editor prefsEdit = sharedPref.edit();
        prefsEdit.clear().commit();
    }
}
