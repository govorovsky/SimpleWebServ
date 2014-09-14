package com.govorovsky.webserver.http.util;

import org.junit.Test;

import java.net.URLDecoder;

import static org.junit.Assert.assertEquals;

public class HttpUtilsTest {

    @Test
    public void testDecodeStr() throws Exception {
        String decoded = "test+test%20test";
        assertEquals(URLDecoder.decode(decoded, "UTF-8"), HttpUtils.decodeStr(decoded));
    }
}