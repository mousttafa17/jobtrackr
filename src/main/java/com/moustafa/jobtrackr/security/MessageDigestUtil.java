package com.moustafa.jobtrackr.security;

import java.security.MessageDigest;

final class MessageDigestUtil {

    private MessageDigestUtil() {
    }

    static boolean isEqual(byte[] expected, byte[] actual) {
        return MessageDigest.isEqual(expected, actual);
    }
}
