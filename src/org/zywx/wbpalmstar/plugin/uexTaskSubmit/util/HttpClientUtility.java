package org.zywx.wbpalmstar.plugin.uexTaskSubmit.util;

import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.zywx.wbpalmstar.plugin.uexTaskSubmit.util.https.Http;

import android.content.Context;
import android.text.TextUtils;

public class HttpClientUtility {
	private final static int TIME_OUT = 20 * 1000;

	private static boolean mIsCertificate = false;
	private static String mPassWord = null;
	private static String mPath = null;

	public static HttpClient getNewHttpClient(Context ctx, String url) {
		if (url.startsWith("https") && mIsCertificate) {
			return getHttpsClient(ctx);
		} else {
			return getHttpClient(url);
		}
	}

	private static HttpClient getHttpClient(String url) {
		try {
			if (TextUtils.isEmpty(url)) {
				return new DefaultHttpClient();
			} else if (url.startsWith("https")) {
				return Http.getHttpsClient(TIME_OUT);
			} else {
				return Http.getHttpClient(TIME_OUT);
			}
		} catch (Exception e) {
			return new DefaultHttpClient();
		}
	}

	private static HttpClient getHttpsClient(Context ctx) {
		try {
			return Http.getHttpsClientWithCert(mPassWord, mPath, TIME_OUT, ctx);
		} catch (Exception e) {
			return new DefaultHttpClient();
		}
	}

	public static HttpURLConnection getNewHttpURLConnection(Context ctx, URL url) {
		if ("https".equals(url.getProtocol()) && mIsCertificate) {
			return getHttpsURLConnection(ctx, url);
		} else {
			return getHttpURLConnection(url);
		}
	}

	private static HttpURLConnection getHttpURLConnection(URL url) {
		try {
			if (TextUtils.isEmpty(url.getProtocol())) {
				return (HttpURLConnection) url.openConnection();
			} else if ("https".equals(url.getProtocol())) {
				return Http.getHttpsURLConnection(url);
			} else {
				return (HttpURLConnection) url.openConnection();
			}
		} catch (Exception e) {
		}
		return null;
	}

	private static HttpURLConnection getHttpsURLConnection(Context ctx, URL url) {
		try {
			return Http.getHttpsURLConnectionWithCert(url, mPassWord, mPath, ctx);
		} catch (Exception e) {
			return null;
		}
	}

	public static void setCertificate(boolean isCertificate, String cPassWord, String cPath) {
		mIsCertificate = isCertificate;
		mPassWord = cPassWord;
		// TODO 测试
		// mPassWord = "" + 123456;
		mPath = cPath;
	}
}
