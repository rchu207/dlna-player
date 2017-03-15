package tw.idv.rchu.dlnaplayer;

import android.net.wifi.WifiInfo;
import android.os.Build;
import android.widget.ImageButton;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Formatter;
import java.util.Locale;

public class Utils {
    public static boolean hasKitkat() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
    }

    public static void updateButtonStatus(ImageButton button, boolean enabled) {
        if (button.isEnabled() != enabled) {
            button.setEnabled(enabled);
            button.setClickable(enabled);

            button.setImageAlpha(enabled ? 255 : 77);
        }
    }

    public static String stringForTime(int timeMs) {
        StringBuilder formatBuilder = new StringBuilder();
        Formatter formatter = new Formatter(formatBuilder, Locale.getDefault());

        int totalSeconds = timeMs / 1000;
        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;
        formatBuilder.setLength(0);
        if (hours > 0) {
            return formatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return formatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }

    public static String getNetworkIp(WifiInfo info) {
        if (info != null) {
            return getNetworkIp(info.getIpAddress());
        } else {
            return getNetworkIp(0);
        }
    }

    private static String getNetworkIp(int hostAddress) {
        if (hostAddress == 0) {
            return "0.0.0.0";
        }

        byte[] addressByte = {
                (byte)(0xff & hostAddress),
                (byte)(0xff & (hostAddress >> 8)),
                (byte)(0xff & (hostAddress >> 16)),
                (byte)(0xff & (hostAddress >> 24))
        };
        try {
            InetAddress address = InetAddress.getByAddress(addressByte);
            return address.getHostAddress();
        } catch (UnknownHostException e) {
            return "0.0.0.0";
        }
    }

}
