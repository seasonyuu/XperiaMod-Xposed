package me.seasonyuu.xperiatools.netspeed;

import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by seasonyuu on 2017/2/24.
 */

public class TrafficStats {
    private static final String TAG = TrafficStats.class.getSimpleName();

    public static final int GRAVITY_BOX_WAY = 1;
    public static final int OLD_WAY = 0;

    public static final long[] getTotalBytes(final int way) {
        if (way == GRAVITY_BOX_WAY) {
            return getTotalRxTxBytesGB();
        } else {
            return getTotalBytesDefaults();
        }
    }

    private static long[] getTotalRxTxBytesGB() {
        String line;
        String[] segs;
        BufferedReader in = null;
        long[] bytes = new long[]{0, 0};
        try {
            FileReader fr = new FileReader("/proc/net/xt_qtaguid/iface_stat_fmt");
            in = new BufferedReader(fr);
            while ((line = in.readLine()) != null) {
                segs = line.split(" ");
                if (segs.length < 4)
                    throw new UnsupportedOperationException("Unsupported length of net params");

                if (isCountedInterface(segs[0])) {
//					if (BuildConfig.DEBUG)
//						XposedBridge.log("iface:" + segs[0] + "; RX=" + segs[1] + "; TX=" + segs[3]);
                    bytes[0] += tryParseLong(segs[1]);
                    bytes[1] += tryParseLong(segs[3]);
                }
            }
        } catch (Throwable t) {
//			if (BuildConfig.DEBUG) XposedBridge.log(t);
            // fallback to TrafficStats
            bytes = getTotalRxTxBytesFromStats();
        } finally {
            if (in != null) try {
                in.close();
            } catch (IOException e) {
            }
        }
        if (bytes[0] < 0) bytes[0] = 0;
        if (bytes[1] < 0) bytes[1] = 0;
        return bytes;
    }

    private static long tryParseLong(String obj) {
        try {
            return Long.parseLong(obj);
        } catch (Exception e) {
            return 0;
        }
    }

    private static boolean isCountedInterface(String iface) {
        return (iface != null &&
                !iface.equals("ifname") &&
                !iface.equals("lo") &&
                !iface.startsWith("tun"));
    }

    private static long[] getTotalRxTxBytesFromStats() {
        return new long[]{android.net.TrafficStats.getTotalRxBytes(),
                android.net.TrafficStats.getTotalTxBytes()};
    }

    private static final long[] getTotalBytesDefaults() {
        long totalBytesRx = -9; // not -1 because it conflicts with
        long totalBytesTx = -9; // not -1 because it conflicts with
        // TrafficStats.UNSUPPORTED
        BufferedReader brRx = null;
        BufferedReader brTx = null;
        BufferedReader br2Rx = null;
        BufferedReader br2Tx = null;

        try {
            brRx = new BufferedReader(new FileReader("/sys/class/net/lo/statistics/rx_bytes"));
            brTx = new BufferedReader(new FileReader("/sys/class/net/lo/statistics/tx_bytes"));

            // reading both together to reduce delay in between as much as
            // possible
            totalBytesTx = android.net.TrafficStats.getTotalTxBytes();
            totalBytesRx = android.net.TrafficStats.getTotalRxBytes();
            String lineRx = brRx.readLine();
            String lineTx = brTx.readLine();

            long loBytesRx = Long.parseLong(lineRx);
            long loBytesTx = Long.parseLong(lineTx);

            long tun0BytesRx = 0;
            long tun0BytesTx = 0;

            File tun0 = new File("/sys/class/net/tun0");
            if (tun0.exists()) {
                br2Rx = new BufferedReader(
                        new FileReader("/sys/class/net/tun0/statistics/rx_bytes"));
                br2Tx = new BufferedReader(
                        new FileReader("/sys/class/net/tun0/statistics/rx_bytes"));
                tun0BytesRx = Long.parseLong(br2Rx.readLine());
                tun0BytesTx = Long.parseLong(br2Tx.readLine());
            }

            totalBytesRx = totalBytesRx - loBytesRx - tun0BytesRx;
            totalBytesTx = totalBytesTx - loBytesTx - tun0BytesTx;

        } catch (Exception e) {
            Log.w(TAG, "Loopback exclusion failed: ", e);

        } finally {
            if (brRx != null) {
                try {
                    brRx.close();
                } catch (Exception e) {
                    // ignore
                }
            }
            if (brTx != null) {
                try {
                    brTx.close();
                } catch (Exception e) {
                    // ignore
                }
            }
            if (br2Rx != null) {
                try {
                    br2Rx.close();
                } catch (Exception e) {
                    // ignore
                }
            }
            if (br2Tx != null) {
                try {
                    br2Tx.close();
                } catch (Exception e) {
                    // ignore
                }
            }
        }

        if (totalBytesRx == -9) {
            totalBytesRx = android.net.TrafficStats.getTotalRxBytes();
        }
        if (totalBytesRx < 0)
            totalBytesRx = 0;
        if (totalBytesTx == -9) {
            totalBytesTx = android.net.TrafficStats.getTotalRxBytes();
        }
        if (totalBytesTx < 0)
            totalBytesTx = 0;

        return new long[]{totalBytesRx, totalBytesTx};
    }
}
