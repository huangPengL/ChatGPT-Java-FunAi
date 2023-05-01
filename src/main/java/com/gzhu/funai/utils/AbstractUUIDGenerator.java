package com.gzhu.funai.utils;

import java.net.InetAddress;

/**
 * @Author :wuxiaodong
 * @Date: 2023/4/2 23:01
 * @Description:
 */
abstract class AbstractUUIDGenerator {

    private static final int IP;
    static {
        int ipadd;
        try {
            ipadd = BytesHelper.toInt( InetAddress.getLocalHost().getAddress() );
        }
        catch (Exception e) {
            ipadd = 0;
        }
        IP = ipadd;
    }

    private static short counter = (short) 0;
    private static final int JVM = (int) ( System.currentTimeMillis() >>> 8 );

    AbstractUUIDGenerator() {
    }

    static int getJVM() {
        return JVM;
    }

    static short getCount() {
        synchronized(AbstractUUIDGenerator.class) {
            if ( counter < 0 ) {
                counter=0;
            }
            return counter++;
        }
    }

    static int getIP() {
        return IP;
    }

    static short getHiTime() {
        return (short) ( System.currentTimeMillis() >>> 32 );
    }

    static int getLoTime() {
        return (int) System.currentTimeMillis();
    }
}
