package com.sr105.wifi;

import android.net.wifi.WifiConfiguration;
import android.os.Parcel;
import android.util.Log;

import org.rdm.util.ReflectionObject;
import org.rdm.util.ReflectionUtils;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;

// inspired by http://stackoverflow.com/a/10309323/47078
public class WifiConfigurationWrapper
{
    private static final String TAG = WifiConfigurationWrapper.class.getSimpleName();

    public WifiConfigurationWrapper()
    {
        this(new WifiConfiguration());
    }


    public WifiConfigurationWrapper(WifiConfiguration wifiConfiguration)
    {
        mWifiConf = wifiConfiguration;
    }

    public WifiConfiguration getConfig()
    {
        return mWifiConf;
    }

    @Override
    public String toString()
    {
        return mWifiConf.toString();
    }

    // TODO: can we write a runtime reflection test that once passed tells us that we
    //       don't have to worry about reflection-related exceptions?

    /**
     * ******** Copied constants **********
     */

    // TODO: find a way using reflection to copy the source enums at runtime?
    // If we simply maintain parity in the names, we can link them by string name.
    // http://niceideas.ch/roller2/badtrash/entry/java_create_enum_instances_dynamically
    // http://stackoverflow.com/questions/4750965/java-reflection-to-obtain-an-enum


    // from WifiConfiguration
    public enum IpAssignment
    {
        /* Use statically configured IP settings. Configuration can be accessed
         * with linkProperties */
        STATIC,
        /* Use dynamically configured IP settigns */
        DHCP,
        /* no IP details are assigned, this is used to indicate
         * that any existing IP settings should be retained */
        UNASSIGNED
    }

    // from WifiConfiguration, don't use UNASSIGNED or PAC for now
    public enum ProxySettings
    {
        /* No proxy is to be used. Any existing proxy settings
         * should be cleared. */
        NONE,
        /* Use statically configured proxy. Configuration can be accessed
         * with linkProperties */
        STATIC,
        /* no proxy details are assigned, this is used to indicate
         * that any existing proxy settings should be retained */
        UNASSIGNED,
        /* Use a Pac based proxy.
         */
        PAC
    }

    /**
     * ******** Getters **********
     */

    public String getPrintableSsid()
    {
        return (String) ReflectionUtils.invokeMethodWithoutThrowing(mWifiConf, "getPrintableSsid");
    }

    public IpAssignment getIpAssignment()
    {
        try
        {
            return IpAssignment.valueOf(ReflectionObject.getEnumField(mWifiConf, "ipAssignment"));
        }
        catch (Exception e)
        {
            Log.e(TAG, "", e);
        }
        return IpAssignment.UNASSIGNED;
    }

    public Collection<LinkAddressWrapped> getIpAddresses()
    {
        Collection<LinkAddressWrapped> linkAddressWrapped = new ArrayList<>(1);
        try
        {
            //noinspection ConstantConditions
            for (Object linkAddress : getLinkProperties().getLinkAddresses())
            {
                linkAddressWrapped.add(new LinkAddressWrapped(linkAddress));
            }
        }
        catch (Exception e)
        {
            Log.e(TAG, "", e);
        }
        return linkAddressWrapped;
    }

    public InetAddress getGateway()
    {
        InetAddress inetAddress = null;
        try
        {
            inetAddress = InetAddress.getByName(null);
            for (RouteInfoWrapped routeInfoWrapped : getRoutes())
            {
                if (routeInfoWrapped.isDefaultRoute())
                {
                    inetAddress = routeInfoWrapped.getGateway();
                    break;
                }
            }
        }
        catch (Exception e)
        {
            Log.e(TAG, "", e);
        }

        return inetAddress;
    }

    public Collection<RouteInfoWrapped> getRoutes()
    {
        try
        {
            //noinspection ConstantConditions
            return getLinkProperties().getRoutes();
        }
        catch (Exception e)
        {
            Log.e(TAG, "", e);
        }
        return new ArrayList<>();
    }

    public Collection<InetAddress> getDNS()
    {
        try
        {
            //noinspection ConstantConditions
            return getLinkProperties().getDnses();
        }
        catch (Exception e)
        {
            Log.e(TAG, "", e);
        }
        return new ArrayList<>();
    }

    public ProxySettings getProxySettings()
    {
        try
        {
            return ProxySettings.valueOf(ReflectionObject.getEnumField(mWifiConf, "proxySettings"));
        }
        catch (Exception e)
        {
            Log.e(TAG, "", e);
        }
        return ProxySettings.UNASSIGNED;
    }

    public ProxyPropertiesWrapped getProxyProperties()
    {
        try
        {
            //noinspection ConstantConditions
            return getLinkProperties().getHttpProxy();
        }
        catch (Exception e)
        {
            Log.e(TAG, "", e);
        }
        return null;
    }

    public String getDisableReason()
    {
        try
        {
            final int NOT_DISABLED = -1;
            // Copied from WifiConfiguration
            final int DISABLED_UNKNOWN_REASON = 0;
            final int DISABLED_DNS_FAILURE = 1;
            final int DISABLED_DHCP_FAILURE = 2;
            final int DISABLED_AUTH_FAILURE = 3;
            final int DISABLED_ASSOCIATION_REJECT = 4;
            switch (ReflectionObject.getIntField(mWifiConf, "disableReason"))
            {
                case NOT_DISABLED:
                    return "Enabled";
                default:
                case DISABLED_UNKNOWN_REASON:
                    return "Unknown";
                case DISABLED_DNS_FAILURE:
                    return "DNS";
                case DISABLED_DHCP_FAILURE:
                    return "DHCP";
                case DISABLED_AUTH_FAILURE:
                    return "Auth";
                case DISABLED_ASSOCIATION_REJECT:
                    return "Association Reject";
            }
        }
        catch (Exception e)
        {
            Log.e(TAG, "", e);
        }
        return "Unknown";
    }

    /**
     * ******** Setters **********
     */

    public void setIpAssignment(IpAssignment value)
            throws SecurityException, IllegalArgumentException, NoSuchFieldException, IllegalAccessException
    {
        ReflectionObject.setEnumField(mWifiConf, value.name(), "ipAssignment");
    }

    public void setIpAddress(InetAddress inetAddress, int prefixLength)
            throws IllegalAccessException, NoSuchFieldException, ClassNotFoundException
    {
        //noinspection ConstantConditions
        getLinkProperties().addLinkAddress(new LinkAddressWrapped(inetAddress, prefixLength));
    }

    public void setGateway(InetAddress inetAddress)
            throws IllegalAccessException, NoSuchFieldException, ClassNotFoundException
    {
        //noinspection ConstantConditions
        getLinkProperties().addRoute(new RouteInfoWrapped(inetAddress));
    }

    public void setDNS(InetAddress dns)
            throws SecurityException, IllegalArgumentException, NoSuchFieldException, IllegalAccessException, NullPointerException, ClassNotFoundException
    {
        //noinspection ConstantConditions
        getLinkProperties().addDns(dns);
    }

    public void setProxySettings(ProxySettings value)
            throws SecurityException, IllegalArgumentException, NoSuchFieldException, IllegalAccessException
    {
        ReflectionObject.setEnumField(mWifiConf, value.name(), "proxySettings");
    }

    public void setProxyProperties(ProxyPropertiesWrapped proxyPropertiesWrapped)
            throws IllegalAccessException, NoSuchFieldException, ClassNotFoundException
    {
        //noinspection ConstantConditions
        getLinkProperties().setHttpProxy(proxyPropertiesWrapped);
    }

    /**
     * ****** Wrapped helper classes ********
     */

    public static class LinkPropertiesWrapped extends ReflectionObject
    {
        public String className() { return "android.net.LinkProperties"; }

        public LinkPropertiesWrapped(Object object) throws ClassNotFoundException
        {
            super(object);
        }

        void setHttpProxy(ProxyPropertiesWrapped proxyPropertiesWrapped)
        {
            invokeVoidMethod("setHttpProxy", proxyPropertiesWrapped.getObject());
        }

        void addLinkAddress(LinkAddressWrapped linkAddressWrapped)
        {
            invokeVoidMethod("addLinkAddress", linkAddressWrapped.getObject());
        }

        void addRoute(RouteInfoWrapped routeInfoWrapped)
        {
            invokeVoidMethod("addRoute", routeInfoWrapped.getObject());
        }

        void addDns(InetAddress inetAddress)
        {
            invokeVoidMethod("addDns", inetAddress);
        }

        @SuppressWarnings("unchecked")
        public Collection<InetAddress> getAddresses()
        {
            return (Collection<InetAddress>) invokeObjectMethod("getAddresses");
        }

        @SuppressWarnings("unchecked")
        // Returns Collection<android.net.LinkAddress>
        public Collection getLinkAddresses()
        {
            return (Collection) invokeObjectMethod("getLinkAddresses");
        }

        public ProxyPropertiesWrapped getHttpProxy() throws ClassNotFoundException
        {
            Object o = invokeObjectMethod("getHttpProxy");
            if (o == null)
            {
                return null;
            }
            return new ProxyPropertiesWrapped(o);
        }

        @SuppressWarnings("unchecked")
        public ArrayList<InetAddress> getDnses()
        {
            // Make a new list so it can be compared to another list of DNS addresses
            ArrayList<InetAddress> dnses = new ArrayList<>();
            for (InetAddress address : (Collection<InetAddress>) invokeObjectMethod("getDnses"))
            {
                dnses.add(address);
            }
            return dnses;
        }

        public Collection<RouteInfoWrapped> getRoutes()
        {
            ArrayList<RouteInfoWrapped> routes = new ArrayList<>(1);
            try
            {
                for (Object route : (Collection) invokeObjectMethod("getRoutes"))
                {
                    routes.add(new RouteInfoWrapped(route));
                }
            }
            catch (Exception e)
            {
                Log.e(TAG, "", e);
            }
            return routes;
        }
    }

    public static class ProxyPropertiesWrapped extends ReflectionObject
    {
        public final String className() { return "android.net.ProxyProperties"; }

        public ProxyPropertiesWrapped(Object object) throws ClassNotFoundException
        {
            super(object);
        }

        public ProxyPropertiesWrapped(String host, int port,
                                      String exclusionList) throws ClassNotFoundException
        {
            super(new Class[] {String.class, int.class, String.class}, host, port, exclusionList);
        }

        public String getHost()
        {
            return invokeStringMethod("getHost");
        }

        public int getPort()
        {
            return invokeIntMethod("getPort");
        }
    }

    public static class LinkAddressWrapped extends ReflectionObject
    {
        public final String className() { return "android.net.LinkAddress"; }

        public LinkAddressWrapped(Object object) throws ClassNotFoundException
        {
            super(object);
        }

        public LinkAddressWrapped(InetAddress inetAddress,
                                  int prefixLength) throws ClassNotFoundException
        {
            super(new Class[] {InetAddress.class, int.class}, inetAddress, prefixLength);
        }

        public InetAddress getAddress()
        {
            return invokeInetAddressMethod("getAddress");
        }

        public int getNetworkPrefixLength()
        {
            return invokeIntMethod("getNetworkPrefixLength");
        }
    }

    public static class RouteInfoWrapped extends ReflectionObject
    {
        public final String className() { return "android.net.RouteInfo"; }

        public RouteInfoWrapped(Object object) throws ClassNotFoundException
        {
            super(object);
        }

        public RouteInfoWrapped(InetAddress inetAddress) throws ClassNotFoundException
        {
            super(new Class[] {InetAddress.class}, inetAddress);
        }

        public InetAddress getGateway()
        {
            return invokeInetAddressMethod("getGateway");
        }

        public boolean isDefaultRoute()
        {
            return invokeBooleanMethod("isDefaultRoute");
        }
    }

    /**
     * ******** Implementation **********
     */

    private final WifiConfiguration mWifiConf;

    private LinkPropertiesWrapped getLinkProperties()
    {
        try
        {
            return new LinkPropertiesWrapped(ReflectionObject.getField(mWifiConf, "linkProperties"));
        }
        catch (Exception e)
        {
            Log.e(TAG, "", e);
        }
        return null;
    }

    @Override
    public int hashCode()
    {
        Parcel p = Parcel.obtain();
        mWifiConf.writeToParcel(p, 0);
        return new String(p.createByteArray()).hashCode();
    }

    @Override
    public boolean equals(Object o)
    {
        return this == o
                || (o instanceof WifiConfigurationWrapper
                && ((WifiConfigurationWrapper) o).hashCode() == hashCode());
    }
}
