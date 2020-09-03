package tw.com.syscode.smartphone;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import org.linphone.LinphoneManager;
import org.linphone.LinphonePreferences;
import org.linphone.LinphoneService;
import org.linphone.LinphoneUtils;
import org.linphone.core.LinphoneAddress;
import org.linphone.core.LinphoneAuthInfo;
import org.linphone.core.LinphoneCall;
import org.linphone.core.LinphoneCallLog;
import org.linphone.core.LinphoneCore;
import org.linphone.core.LinphoneCoreException;
import org.linphone.core.LinphoneCoreFactory;
import org.linphone.core.LinphoneProxyConfig;

import tw.com.hokei.kiosk2g.WebService;

/**
 * Created by hsu on 2017/7/5.
 */

public class SmartPhoneManager extends LinphoneManager {
    private static final String TAG = "SmartPhoneManager";

    private Context context = null;
    private final int MAX_CALLS = 1;

    public SmartPhoneManager(Context context) {
        super(context);
        this.context = context;
    }

    //----------------------------------------------------------------------

    public static void initAccount(String domain, String username, String password) throws Exception {
        LinphoneAuthInfo lAuthInfo =  LinphoneCoreFactory.instance().createAuthInfo(username, password, null, domain);
        getLc().addAuthInfo(lAuthInfo);

        String identity = "sip:" + username +"@" + domain;
        String proxy = "sip:" + domain;

        LinphoneAddress proxyAddr = LinphoneCoreFactory.instance().createLinphoneAddress(proxy);
        proxyAddr.setTransport(LinphoneAddress.TransportType.LinphoneTransportTls);

        LinphoneProxyConfig proxycon = getLc().createProxyConfig(identity, proxyAddr.asStringUriOnly(), proxyAddr.asStringUriOnly(), true);
        getLc().addProxyConfig(proxycon);
        getLc().setDefaultProxyConfig(proxycon);

        LinphoneProxyConfig lDefaultProxyConfig = getLc().getDefaultProxyConfig();
        if (lDefaultProxyConfig != null) {
            //escape +
            lDefaultProxyConfig.setDialEscapePlus(false);
        } else if (LinphoneService.isReady()) {

            getLc().addListener(SmartPhoneManager.getInstance());
            SmartPhoneManager.getInstance().registrationState(getLc(), lDefaultProxyConfig, LinphoneCore.RegistrationState.RegistrationNone, null);
        }
    }

    public static boolean isExitingAccount(String username, String domain) {
        boolean result = false;
        int count = LinphonePreferences.instance().getAccountCount();

        if (count > 0) {
            LinphoneProxyConfig[] prxCfgs = getLc().getProxyConfigList();
            for (int i = 0; i < prxCfgs.length; i++) {
                LinphoneAddress address = prxCfgs[i].getAddress();
                if (address != null) {
                    if (domain.equals(address.getDomain()) && username.equals(address.getUserName())) {
                        result = true;
                        break;
                    }
                }
            }
        }

        return result;
    }

    public static void saveCreatedAccount(String username, String password, String domain, String proxy) {
        username = LinphoneUtils.getDisplayableUsernameFromAddress(username);
        domain = LinphoneUtils.getDisplayableUsernameFromAddress(domain);

        String identity = "sip:" + username + "@" + domain;
        if (!TextUtils.isEmpty(proxy)) {
            identity = "sip:" + username + "@" + proxy;
        }
        final String userid = null; // must be null !!!
        final String ha1 = null;
        final String prefix = null;
        final LinphoneAddress.TransportType transport = LinphoneAddress.TransportType.LinphoneTransportUdp;
        LinphoneAddress address = null;

        try {
            address = LinphoneCoreFactory.instance().createLinphoneAddress(identity);
        } catch (LinphoneCoreException e) {
            e.printStackTrace();
        }

        LinphonePreferences.AccountBuilder builder = new LinphonePreferences.AccountBuilder(LinphoneManager.getLc())
                .setUsername(username)
                .setDomain(domain)
                .setHa1(ha1)
                .setDisplayName(username)
                .setUserId(userid)
                .setPassword(password);

        if (prefix != null) {
            builder.setPrefix(prefix);
        }

        String forcedProxy = proxy;
        if (!TextUtils.isEmpty(forcedProxy)) {
            builder.setProxy(forcedProxy)
                    .setOutboundProxyEnabled(true)
                    .setAvpfRRInterval(5);
        }
        if (transport != null) {
            builder.setTransport(transport);
        }

        try {
            builder.saveNewAccount();
            /*
            if (!newAccount) {
                displayRegistrationInProgressDialog();
            }
            */
        } catch (LinphoneCoreException e) {
            e.printStackTrace();
        }
    }

    public static void deleteAccount(int n) {
        LinphonePreferences.instance().deleteAccount(n);
    }

    public static void invite(String to, String displayName) {
        if (LinphoneManager.getInstance() != null) {
            LinphoneManager.getInstance().newOutgoingCall(to, displayName);
        }
    }

    public static void acceptCurrentCall() {
        LinphoneCall call = LinphoneManager.getLc().getCurrentCall();

        if (call != null) {
            Log.i(TAG, call.toString());
            LinphoneManager.getInstance().acceptCall(call);
        }
    }

    public static void declineCurrentCall() {
        if (LinphoneManager.getLc() != null) {
            LinphoneCall[] calls = LinphoneManager.getLc().getCalls();

            if (calls != null) {
                for (LinphoneCall call : calls) {
                    LinphoneManager.getLc().terminateCall(call);
                }
            }
        }
    }

    public static void accept(LinphoneCall call) {
        LinphoneManager.getInstance().acceptCall(call);
    }
    //----------------------------------------------------------------------

    public static LinphoneCallLog[] getCallLog() {
        LinphoneCallLog[] callLog = null;

        try {
            callLog =  LinphoneManager.getLc().getCallLogs();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return callLog;
    }
}
