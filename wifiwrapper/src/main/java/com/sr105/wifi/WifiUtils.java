package com.sr105.wifi;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.net.InetAddress;
import java.util.List;

public class WifiUtils {
    private static final String TAG = WifiUtils.class.getSimpleName();

    public static Context sContext;

    public static void setWifiConfig(WifiConfigurationWrapper wifiConfigWrapper,
                                     boolean deleteOthers) throws Exception {
        WifiManager wifiManager = getWifiManager();

        // this also updates an already existing config for the same network
        int networkId = wifiManager.addNetwork(wifiConfigWrapper.getConfig());
        if (networkId == -1) {
            Log.e(TAG, "Failed to add/update wifi config: " + wifiConfigWrapper.getConfig().toString());
            return;
        }

        wifiManager.enableNetwork(networkId, false);

        if (deleteOthers) {
            deleteOtherNetworks(wifiManager, networkId);
        }
        wifiManager.saveConfiguration();
    }

    public static void deleteOtherNetworks(WifiManager wifiManager, int networkId) {
        // Delete all other configs
        List<WifiConfiguration> configuredNetworks = wifiManager.getConfiguredNetworks();
        for (WifiConfiguration conf : configuredNetworks) {
            if (conf.networkId != networkId) {
                wifiManager.removeNetwork(conf.networkId);
            }
        }
    }

    public static void logCurrentSettings() {
        WifiConfigurationWrapper current = getCurrentWifiConfig();
        loge(logWifi(current, "Current"));
    }

    @SuppressWarnings("StringConcatenationInsideStringBufferAppend")
    public static String logWifi(WifiConfigurationWrapper wrapper, String name) {
        if (wrapper == null) {
            return "logWifi: Failed to find wifi config: " + name;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(name + " wifi connection = " + wrapper.getConfig().toString() + "\n");

        sb.append("ip config = " + wrapper.getIpAssignment() + "\n");
        for (WifiConfigurationWrapper.LinkAddressWrapped address : wrapper.getIpAddresses()) {
            sb.append("ip = " + address.toString() + "\n");
        }
        for (WifiConfigurationWrapper.RouteInfoWrapped routeInfoWrapped : wrapper.getRoutes()) {
            sb.append("route = " + routeInfoWrapped.toString() + "\n");
        }
        for (InetAddress address : wrapper.getDNS()) {
            sb.append("dns = " + address.getHostAddress() + "\n");
        }
        sb.append("proxy settings = " + wrapper.getProxySettings() + "\n");
        sb.append("proxy = " + wrapper.getProxyProperties() + "\n");
        return sb.toString();
    }

    public static void loge(String msg) {
        Log.e(TAG, msg);
    }

    public static void loge(String msg, Exception e) {
        Log.e(TAG, msg, e);
    }

    public static void loge(Exception e) {
        Log.e(TAG, "", e);
    }

    /**
     * Make sure all networks are enabled. Android will disable them
     * if network connections are "poor". The problem with that is
     * it assumes that a user exists to take action using the UI.
     */
    private static void enableAllNetworks() {
        try {
            WifiManager wifiManager = getWifiManager();
            List<WifiConfiguration> configuredNetworks = wifiManager.getConfiguredNetworks();
            for (WifiConfiguration conf : configuredNetworks) {
                if (conf.status == WifiConfiguration.Status.DISABLED) {
                    Log.i(TAG, "Network " + conf.SSID + " was disabled: " + new WifiConfigurationWrapper(conf).getDisableReason());
                    wifiManager.enableNetwork(conf.networkId, false);
                }
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * Turns on hiddenSSID if not set.
     *
     * @param wrapper wifi configuration to check
     * @return true if hiddenSSID was set, false if already set
     */
    private static boolean turnOnHiddenSsid(WifiConfigurationWrapper wrapper) {
        try {
            if (wrapper == null || wrapper.getConfig().hiddenSSID) {
                return false;
            }

            wrapper.getConfig().hiddenSSID = true;
            setWifiConfig(wrapper, false);
        } catch (Exception e) {
            Log.e(TAG, "", e);
        }
        return true;
    }

    public static void setWifiConfigWithoutSaving(WifiManager wifiManager,
                                                  WifiConfiguration wifiConfiguration) {
        int networkId = wifiManager.addNetwork(wifiConfiguration);
        if (networkId != -1) {
            wifiManager.enableNetwork(networkId, false);
        }
    }

    public static boolean isWifiConnected() {
        try {
            // TODO: we should only check the NetworkInfo for a Wifi interface
            ConnectivityManager connectivityManager = (ConnectivityManager) sContext.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            return networkInfo != null && networkInfo.isConnectedOrConnecting();
        } catch (Exception e) {
            loge(e);
        }
        return false;
    }

    private static WifiManager sWifiManager;

    public static WifiManager getWifiManager() throws Exception {
        if (sWifiManager == null) {
            sWifiManager = (WifiManager) sContext.getSystemService(Context.WIFI_SERVICE);
        }
        // TODO: throw if we can't use wifiManager (wpa_supplicant isn't ready/running)
        if (!sWifiManager.isWifiEnabled()) {
            sWifiManager.setWifiEnabled(true);
        }
        return sWifiManager;
    }

    private static WifiConfiguration newWifiConfiguration(String ssid, String preSharedKey) {
        WifiConfiguration wifiConfiguration = new WifiConfiguration();
        wifiConfiguration.SSID = quotedString(ssid);
        wifiConfiguration.preSharedKey = getWPASupplicantCompatibleWirelessWPAKey(preSharedKey);
        wifiConfiguration.hiddenSSID = true;
        return wifiConfiguration;
    }

    public static WifiConfigurationWrapper getCurrentWifiConfigForSsid(String ssid) {
        WifiConfigurationWrapper wrapper = null;
        try {
            WifiManager wifiManager = getWifiManager();
            List<WifiConfiguration> configuredNetworks = wifiManager.getConfiguredNetworks();
            for (WifiConfiguration conf : configuredNetworks) {
                if (conf.SSID.equals(ssid)) {
                    wrapper = new WifiConfigurationWrapper(conf);
                    break;
                }
            }
        } catch (Exception e) {
            loge(e);
        }
        return wrapper;
    }

    public static WifiConfigurationWrapper getCurrentWifiConfigOrMatchingSsidOf(
            WifiConfigurationWrapper config) {
        WifiConfigurationWrapper current = getCurrentWifiConfig();
        // current will be null if disconnected, see if Android has an entry matching config
        if (current == null && config != null) {
            current = getCurrentWifiConfigForSsid(config.getConfig().SSID);
        }
        return current;
    }

    public static WifiConfigurationWrapper getCurrentWifiConfig() {
        WifiConfigurationWrapper wrapper = null;
        try {
            WifiManager wifiManager = getWifiManager();
            WifiInfo connectionInfo = wifiManager.getConnectionInfo();
            if (connectionInfo.getNetworkId() == -1) {
                return null;
            }
            List<WifiConfiguration> configuredNetworks = wifiManager.getConfiguredNetworks();
            for (WifiConfiguration conf : configuredNetworks) {
                if (conf.networkId == connectionInfo.getNetworkId()) {
                    wrapper = new WifiConfigurationWrapper(conf);
                    break;
                }
            }
        } catch (Exception e) {
            loge(e);
        }
        return wrapper;
    }

    // code lifted and modified from Settings app WifiConfigController
    public static String getWPASupplicantCompatibleWirelessWPAKey(String key) {
        // android
        if (key == null || key.isEmpty() || key.matches("[0-9A-Fa-f]{64}")) {
            return key;
        }

        return quotedString(key);
    }

    // code lifted and modified from Settings app WifiConfigController
    public static String getWPASupplicantCompatibleWirelessWEPKey(String key) {
        if (key == null || key.isEmpty()) {
            return key;
        }

        int length = key.length();
        // WEP-40, WEP-104, and 256-bit WEP (WEP-232?)
        if ((length == 10 || length == 26 || length == 58) &&
                key.matches("[0-9A-Fa-f]*")) {
            return key;
        }

        return quotedString(key);
    }

    public static String quotedString(String value) {
        return '"' + value + '"';
    }
}
