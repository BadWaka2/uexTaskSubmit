package org.zywx.wbpalmstar.plugin.uexTaskSubmit;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.cookie.SM;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.zywx.wbpalmstar.base.BUtility;
import org.zywx.wbpalmstar.base.ResoureFinder;
import org.zywx.wbpalmstar.engine.EUtil;
import org.zywx.wbpalmstar.plugin.uexTaskSubmit.JSONBean.CreateResourcePost;
import org.zywx.wbpalmstar.plugin.uexTaskSubmit.JSONBean.CreateResourcePostResponse;
import org.zywx.wbpalmstar.plugin.uexTaskSubmit.JSONBean.CreateTaskPost;
import org.zywx.wbpalmstar.plugin.uexTaskSubmit.JSONBean.CreateTaskPostResponse;
import org.zywx.wbpalmstar.plugin.uexTaskSubmit.JSONBean.MobilePrjMembers;
import org.zywx.wbpalmstar.plugin.uexTaskSubmit.JSONBean.MobileProcess;
import org.zywx.wbpalmstar.plugin.uexTaskSubmit.JSONBean.PrjId;
import org.zywx.wbpalmstar.plugin.uexTaskSubmit.JSONBean.UserLoginResponse;
import org.zywx.wbpalmstar.plugin.uexTaskSubmit.util.HttpClientUtility;
import org.zywx.wbpalmstar.plugin.uexTaskSubmit.util.PEncryption;
import org.zywx.wbpalmstar.plugin.uexTaskSubmit.util.TaskSubmitUtils;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

/**
 * Http请求类，请勿在UI线程中调用
 * 
 * @author yajun.Duan,waka
 *
 */
public class HttpRequest {
	private static final String TAG = "uexTaskSubmit_HttpRequest";// TAG标记，用于Log调试

	private static final int TIME_OUT = 1 * 30 * 1000; // 超时时间
	private static final String CHARSET = "utf-8"; // 设置编码
	private static String LOGIN_URL = EUExTaskSubmit.configLoginURL;// "http://192.168.4.168:8080";sso登录路径
	private static String BASE_URL = EUExTaskSubmit.configBaseURL;// "http://192.168.1.83:9083";根路径

	// 通用地址
	private static final String URL_MOBILE_EMM_LOGIN = "/emmLogin";
	private static final String URL_MOBILE_USER_LOGIN = "/appcanUserLogin";
	private static final String URL_MOBILE_TICKETS = "/v1/tickets/";
	// private static final String URL_MOBILE_SERVICE_VALIDATE =
	// "/serviceValidate"; //登录验证
	private static final String URL_MOBILE_PROCESS = "/process/mobileProcess";
	private static final String URL_MOBILE_PRJMEMBERS = "/project/mobilePrjMembers";
	private static final String URL_MOBILE_FINDPRJID = "/app/findPrjId";
	private static final String URL_MOBILE_CREATE_TASK = "/task/mobileTaskCreate";
	private static final String URL_MOBILE_CREATE_RES = "/resource/create";

	private static String ticket = "";

	public static String getBaseUrl() {
		return BASE_URL;
	}

	public static void setBaseUrl(String baseUrl) {
		BASE_URL = baseUrl;
	}

	/**
	 * login
	 * 
	 * @param context
	 * @param username
	 * @param password
	 * @return
	 */
	public static UserLoginResponse login(Context context, String username, String password) {
		UserLoginResponse loginResponse = null;
		if (BASE_URL.contains("coopDevelopment_online")) {
			loginResponse = loginPopular(context, username, password);
		} else {
			loginResponse = loginEnterprise(context, username, password);
		}
		return loginResponse;
	}

	/**
	 * Enterprise Edition
	 * 
	 * @param context
	 * @param username
	 * @param password
	 * @return
	 */
	private static UserLoginResponse loginEnterprise(Context context, String username, String password) {
		UserLoginResponse userLoginResponse = new UserLoginResponse();
		HttpClient httpClient = initHttpClient(context, EUExTaskSubmit.appId, BASE_URL);
		httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, TIME_OUT);// 请求超时
		httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, TIME_OUT);// 读取超时
		try {
			String emmurl = BASE_URL + URL_MOBILE_EMM_LOGIN;
			HttpPost httpPost = new HttpPost(emmurl);
			List<NameValuePair> nvps = new ArrayList<NameValuePair>();
			nvps.add(new BasicNameValuePair("account", username));
			nvps.add(new BasicNameValuePair("password", password));
			httpPost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
			HttpResponse httpResponse = httpClient.execute(httpPost);
			if (httpResponse.getStatusLine().getStatusCode() != 200) {
				Log.i(TAG, "loginEnterprise Failure StatusCode " + httpResponse.getStatusLine().getStatusCode());
				userLoginResponse.status = "Failure";
				return null;
			}
			String data = EntityUtils.toString(httpResponse.getEntity());// 返回的数据1，是一个html字符串
			// Log.i(TAG, "data " + data);
			handleCookie(BASE_URL, httpResponse);
			userLoginResponse = TaskSubmitUtils.analysisEMMLogin(data);
			return userLoginResponse;
		} catch (Exception e) {
			Log.e(TAG, "Exception");
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Popular Edition
	 * 
	 * @param context
	 * @param username
	 * @param password
	 * @return
	 */
	private static UserLoginResponse loginPopular(Context context, String username, String password) {
		HttpClient httpClient = initHttpClient(context, EUExTaskSubmit.appId, LOGIN_URL);
		httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, TIME_OUT);// 请求超时
		httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, TIME_OUT);// 读取超时
		try {
			/** 第一次请求POST **/
			String newUrl = LOGIN_URL + URL_MOBILE_TICKETS;
			HttpPost httpPost = new HttpPost(newUrl);
			List<NameValuePair> nvps = new ArrayList<NameValuePair>();
			nvps.add(new BasicNameValuePair("username", username));
			nvps.add(new BasicNameValuePair("password", password));
			httpPost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
			HttpResponse httpResponse = httpClient.execute(httpPost);
			String data1 = EntityUtils.toString(httpResponse.getEntity());// 返回的数据1，是一个html字符串
			// Log.i(TAG, "data1 " + data1);
			Document doc = Jsoup.parse(data1);// 使用Jsoup解析html字符串，获得action
			Elements form = doc.getElementsByTag("form");
			String action = form.attr("action");
			if (TextUtils.isEmpty(action)) {
				Log.i(TAG, "action isEmpty");
				return null;
			}
			/** 以action为目标url再次请求，第二次请求POST **/
			HttpPost httpPost2 = new HttpPost(action);
			String userUrl = BASE_URL + URL_MOBILE_USER_LOGIN;
			List<NameValuePair> nvps2 = new ArrayList<NameValuePair>();
			nvps2.add(new BasicNameValuePair("service", userUrl));
			httpPost2.setEntity(new UrlEncodedFormEntity(nvps2, HTTP.UTF_8));
			HttpResponse httpResponse2 = httpClient.execute(httpPost2);
			if (httpResponse2.getStatusLine().getStatusCode() != 200) {
				Log.i(TAG, "action post Failure StatusCode " + httpResponse2.getStatusLine().getStatusCode());
				return null;
			}
			String data2 = EntityUtils.toString(httpResponse2.getEntity());// 返回的数据2，是一个ticket
			ticket = data2;// 获得ticket 仅可使用一次
			// Log.i(TAG, "data2 " + data2);

			/** 以特定地址为目标url再次请求，第三次请求GET **/
			/*
			 * List<NameValuePair> nvps3 = new ArrayList<NameValuePair>();
			 * nvps3.add(new BasicNameValuePair("ticket", ticket));
			 * nvps3.add(new BasicNameValuePair("service", userUrl)); String
			 * urlCAS = LOGIN_URL + URL_MOBILE_SERVICE_VALIDATE + "?" +
			 * URLEncodedUtils.format(nvps3, HTTP.UTF_8); HttpGet httpGet3 = new
			 * HttpGet(urlCAS); HttpResponse httpResponse3 =
			 * httpClient.execute(httpGet3); if
			 * (httpResponse3.getStatusLine().getStatusCode() != 200) {
			 * Log.i(TAG, "CAS check Failure StatusCode " +
			 * httpResponse3.getStatusLine().getStatusCode()); return null; }
			 * String data3 = EntityUtils.toString(httpResponse3.getEntity());//
			 * 返回的数据3，是一个xml字符串 handleCookie(BASE_URL, httpResponse); Log.i(TAG,
			 * "data3 " + data3); LoginCASBean casBean =
			 * TaskSubmitUtils.analysisXML(data3);
			 * if(!casBean.state.contains("Success")){ Log.i(TAG,
			 * "CAS check Failure"); return null; }
			 */
			return userLogin(context);
		} catch (IllegalStateException e) {
			Log.e(TAG, "IllegalStateException");
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			Log.e(TAG, "UnsupportedEncodingException");
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			Log.e(TAG, "ClientProtocolException");
			e.printStackTrace();
		} catch (IOException e) {
			Log.e(TAG, "IOException");
			e.printStackTrace();
		} catch (Exception e) {
			Log.e(TAG, "Exception");
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * userLogin
	 * 
	 * @return
	 */
	public static UserLoginResponse userLogin(Context context) {
		UserLoginResponse userLoginResponse = new UserLoginResponse();
		HttpClient httpClient = initHttpClient(context, EUExTaskSubmit.appId, BASE_URL);
		httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, TIME_OUT);// 请求超时
		httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, TIME_OUT);// 读取超时
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("ticket", ticket));
		String userLoginUrl = BASE_URL + URL_MOBILE_USER_LOGIN + "?" + URLEncodedUtils.format(nvps, HTTP.UTF_8);
		userLoginUrl = BASE_URL + URL_MOBILE_USER_LOGIN + "?ticket=" + ticket;
		HttpGet httpGet = new HttpGet(userLoginUrl);
		try {
			HttpResponse httpResponse = httpClient.execute(httpGet);
			if (httpResponse.getStatusLine().getStatusCode() != 200) {
				Log.i(TAG, "user login Failure StatusCode " + httpResponse.getStatusLine().getStatusCode());
				return null;
			}
			String s = EntityUtils.toString(httpResponse.getEntity());
			// Log.i(TAG, "userLogin " + s);
			handleCookie(BASE_URL, httpResponse);
			userLoginResponse = TaskSubmitUtils.analysisJSONForUserLoginResponse(s);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return userLoginResponse;
	}

	/**
	 * 获取流程列表,静态方法，GET请求
	 * 
	 * @param appId
	 */
	public static MobileProcess getMobileProcess(Context context, String appId) {
		MobileProcess mobileProcess = new MobileProcess();
		HttpClient httpClient = initHttpClient(context, EUExTaskSubmit.appId, BASE_URL);
		httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, TIME_OUT);// 请求超时
		httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, TIME_OUT);// 读取超时
		HttpGet httpGet = new HttpGet(BASE_URL + URL_MOBILE_PROCESS + "?" + "appId=" + appId);
		String cookie = getCookie(BASE_URL);
		if (cookie != null) {
			httpGet.setHeader(new BasicHeader("Cookie", cookie));
		} else {
			Log.i(TAG, "cookie null");
		}
		// Log.i(TAG + ".getMobileProcess" + ".URL:", BASE_URL +
		// URL_MOBILE_PROCESS + "?appId=" + appId);
		try {
			HttpResponse httpResponse = httpClient.execute(httpGet);
			if (httpResponse.getStatusLine().getStatusCode() != 200) {
				Log.i(TAG, "getMobileProcess Failure StatusCode " + httpResponse.getStatusLine().getStatusCode());
				return null;
			}
			String s = EntityUtils.toString(httpResponse.getEntity());
			Log.i(TAG + ".getMobileProcess", s);
			mobileProcess = TaskSubmitUtils.analysisJSONForGetMobileProcess(s);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return mobileProcess;
	}

	/**
	 * 获取项目正式成员,静态方法，GET请求
	 * 
	 * @param appId
	 */
	public static MobilePrjMembers getMobilePrjMembers(Context context, String appId) {
		MobilePrjMembers mobilePrjMembers = new MobilePrjMembers();
		// HttpClient httpClient = new DefaultHttpClient();
		HttpClient httpClient = initHttpClient(context, EUExTaskSubmit.appId, BASE_URL);
		httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, TIME_OUT);// 请求超时
		httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, TIME_OUT);// 读取超时
		HttpGet httpGet = new HttpGet(BASE_URL + URL_MOBILE_PRJMEMBERS + "?appId=" + appId + "&ticket=" + ticket);
		String cookie = getCookie(BASE_URL);
		if (cookie != null) {
			httpGet.setHeader(new BasicHeader("Cookie", cookie));
		} else {
			Log.i(TAG, "cookie null");
		}
		// Log.i(TAG + ".getMobilePrjMembers" + ".URL:", BASE_URL +
		// URL_MOBILE_PRJMEMBERS + "?appId=" + appId);
		try {
			HttpResponse httpResponse = httpClient.execute(httpGet);
			if (httpResponse.getStatusLine().getStatusCode() != 200) {
				Log.i(TAG, "getMobilePrjMembers Failure StatusCode " + httpResponse.getStatusLine().getStatusCode());
				return null;
			}
			String s = EntityUtils.toString(httpResponse.getEntity());
			Log.i(TAG + ".getMobilePrjMembers", s);
			mobilePrjMembers = TaskSubmitUtils.analysisJSONForGetMobilePrjMembers(s);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return mobilePrjMembers;
	}

	/**
	 * 获取对应的项目ID,静态方法，GET请求
	 * 
	 * @param appId
	 */
	public static PrjId getPrjId(Context context, String appId) {
		PrjId prjId = new PrjId();
		HttpClient httpClient = initHttpClient(context, EUExTaskSubmit.appId, BASE_URL);
		httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, TIME_OUT);// 请求超时
		httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, TIME_OUT);// 读取超时
		HttpGet httpGet = new HttpGet(BASE_URL + URL_MOBILE_FINDPRJID + "?appId="
				+ appId /* + "&ticket=" + ticket */);
		// Log.i(TAG + ".getPrjId" + ".URL:", BASE_URL + URL_MOBILE_FINDPRJID +
		// "?appId=" + appId);
		String cookie = getCookie(BASE_URL);
		if (cookie != null) {
			httpGet.setHeader(new BasicHeader("Cookie", cookie));
		} else {
			Log.i(TAG, "cookie null");
		}
		try {
			HttpResponse httpResponse = httpClient.execute(httpGet);
			if (httpResponse.getStatusLine().getStatusCode() != 200) {
				Log.i(TAG, "getPrjId Failure StatusCode " + httpResponse.getStatusLine().getStatusCode());
				return null;
			}
			String s = EntityUtils.toString(httpResponse.getEntity());
			Log.i(TAG + ".getMobilePrjMembers", s);
			prjId = TaskSubmitUtils.analysisJSONForGetPrjId(s);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return prjId;
	}

	/**
	 * 创建任务，静态方法，POST请求
	 * 
	 * @param createTaskPost
	 * @return CreateTaskPostResponse
	 */
	public static CreateTaskPostResponse createTask(Context context, CreateTaskPost createTaskPost) {
		CreateTaskPostResponse createTaskPostResponse = new CreateTaskPostResponse();
		HttpClient httpClient = initHttpClient(context, EUExTaskSubmit.appId, BASE_URL);
		httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, TIME_OUT);// 请求超时
		httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, TIME_OUT);// 读取超时
		HttpPost httpPost = new HttpPost(BASE_URL + URL_MOBILE_CREATE_TASK);
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("name", createTaskPost.name));
		nvps.add(new BasicNameValuePair("detail", createTaskPost.detail));
		nvps.add(new BasicNameValuePair("processId", createTaskPost.processId));
		nvps.add(new BasicNameValuePair("appId", createTaskPost.appId));
		nvps.add(new BasicNameValuePair("priority", createTaskPost.priority));
		nvps.add(new BasicNameValuePair("repeatable", createTaskPost.repeatable));
		nvps.add(new BasicNameValuePair("tagNameList", createTaskPost.tag));
		nvps.add(new BasicNameValuePair("resourceIdList", createTaskPost.resource));
		nvps.add(new BasicNameValuePair("leaderUserId", createTaskPost.leader));
		nvps.add(new BasicNameValuePair("memberUserIdList", createTaskPost.member));
		nvps.add(new BasicNameValuePair("deadline", createTaskPost.deadline));
		// nvps.add(new BasicNameValuePair("progress", String.valueOf(0)));//
		// 任务进度（progress）默认为0
		// nvps.add(new BasicNameValuePair("status", "WAITING"));//
		// 任务状态（status）默认为WAITING
		// nvps.add(new BasicNameValuePair("ticket", ticket));
		String cookie = getCookie(BASE_URL);
		if (cookie != null) {
			httpPost.setHeader(new BasicHeader("Cookie", cookie));
		} else {
			Log.i(TAG, "cookie null");
		}
		try {
			httpPost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
			HttpResponse httpResponse = httpClient.execute(httpPost);
			if (httpResponse.getStatusLine().getStatusCode() != 200) {
				Log.i(TAG, "createTask Failure StatusCode " + httpResponse.getStatusLine().getStatusCode());
				return null;
			}
			String strResult = EntityUtils.toString(httpResponse.getEntity());// 返回的JSON数据
			Log.i(TAG, "createTask " + strResult);
			createTaskPostResponse = TaskSubmitUtils.analysisJSONForCreateTaskPostResponse(strResult);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return createTaskPostResponse;
	}

	/**
	 * 创建资源，静态方法，POST请求
	 * 
	 * @param createResourcePost
	 * @return CreateResourcePostResponse
	 */
	public static CreateResourcePostResponse createResource(Context context, CreateResourcePost createResourcePost) {
		CreateResourcePostResponse createResourcePostResponse = new CreateResourcePostResponse();
		InputStream is = null;
		DataOutputStream outStream = null;
		HttpURLConnection conn = null;
		String inFilePath = createResourcePost.file;
		String fileName = "";
		if (inFilePath == null || inFilePath.length() == 0) {
			return null;

		}
		if (inFilePath.startsWith(BUtility.F_FILE_SCHEMA)) {
			inFilePath = inFilePath.substring(BUtility.F_FILE_SCHEMA.length());
		}
		try {
			if (inFilePath.startsWith("/")) {
				File file = new File(inFilePath);
				if (!file.exists()) {
					return null;
				}
				is = new FileInputStream(file);
			}
			fileName = inFilePath.substring(inFilePath.lastIndexOf("/") + 1);
			String inInputName = "file";
			String BOUNDARY = UUID.randomUUID().toString(); // 边界标识 随机生成
			String PREFIX = "--", LINE_END = "\r\n";
			String CONTENT_TYPE = "multipart/form-data"; // 内容类型

			URL url = new URL(BASE_URL + URL_MOBILE_CREATE_RES);
			if (BASE_URL.startsWith(BUtility.F_HTTP_PATH)) {
				conn = (HttpURLConnection) url.openConnection();
			} else {
				conn = initHttpURLConnection(context, EUExTaskSubmit.appId, url);
				if (conn == null) {
					conn = (HttpURLConnection) url.openConnection();
				}
			}
			String cookie = getCookie(BASE_URL);
			if (null != cookie) {
				conn.setRequestProperty("Cookie", cookie);
			} else {
				Log.i(TAG, "cookie null");
			}
			conn.setReadTimeout(TIME_OUT);
			conn.setConnectTimeout(TIME_OUT);
			conn.setDoInput(true); // 允许输入流
			conn.setDoOutput(true); // 允许输出流
			conn.setUseCaches(false); // 不允许使用缓存
			conn.setRequestMethod("POST"); // 请求方式
			conn.setRequestProperty("Charset", CHARSET); // 设置编码
			conn.setRequestProperty("connection", "keep-alive");
			conn.setRequestProperty("Content-Type", CONTENT_TYPE + ";boundary=" + BOUNDARY);

			DataOutputStream dos = new DataOutputStream(conn.getOutputStream());

			/**
			 * 当文件不为空，把文件包装并且上传
			 */
			StringBuffer sb = new StringBuffer();
			sb.append(PREFIX);
			sb.append(BOUNDARY);
			sb.append(LINE_END);
			/**
			 * 这里重点注意： name里面的值为服务器端需要key 只有这个key 才可以得到对应的文件
			 * filename是文件的名字，包含后缀名的 比如:abc.png
			 */
			sb.append("Content-Disposition: form-data; name=\"" + inInputName + "\"; filename=\"" + fileName + "\""
					+ LINE_END);
			sb.append("Content-Type: application/octet-stream; charset=" + CHARSET + LINE_END);
			sb.append(LINE_END);
			dos.write(sb.toString().getBytes());
			// int l;
			// int upload = 0;
			// int fileSize = is.available();
			byte[] bytes = new byte[4096];
			int len = 0;
			try {
				while ((len = is.read(bytes)) != -1) {
					dos.write(bytes, 0, len);
				}
			} catch (OutOfMemoryError e) {
				e.printStackTrace();
				return null;
			}
			dos.write(LINE_END.getBytes());

			/**
			 * 文件传输完毕,传递String参数对应的key value
			 */
			// writeStringParams("file", fileName, BOUNDARY, dos);
			writeStringParams("parentId", createResourcePost.parentId, BOUNDARY, dos);
			writeStringParams("projectId", createResourcePost.projectId, BOUNDARY, dos);

			byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINE_END).getBytes();
			dos.write(end_data);

			int res = conn.getResponseCode();
			Log.i(TAG, "Upload result res ===> " + res);
			if (res == 200) {
				is = conn.getInputStream();
				int ch;
				StringBuilder b = new StringBuilder();
				while ((ch = is.read()) != -1) {
					b.append((char) ch);
				}
				/**
				 * 返回的服务器内容 失败
				 * {\"msg\":{\"message\":\"文件读取失败\"},\"status\":\"failure\"}
				 * 
				 * 成功 {\"status\":\"success\", \
				 * "msg\":{\"filelist\":[{\"filepath\":\"D:\/testappcan\/1436953052282.png\"
				 * }]} }
				 * 
				 **/
				Log.i(TAG, "Upload result ===> " + b.toString());
				createResourcePostResponse = TaskSubmitUtils.analysisJSONForCreateResourcePostResponse(b.toString());
				// JSONObject resultObj = new JSONObject(b.toString());
				// if ("success".equals(resultObj.optString("status"))) {
				// Log.i(TAG, "Upload success !!!");
				// String serverUrl = resultObj.getJSONObject("msg")
				// .getJSONArray("filelist").getJSONObject(0)
				// .getString("filepath");
				// } else {
				// Log.i(TAG,
				// "Upload result ===> "
				// + resultObj.getString("msg").toString());
				// }
			}
			is.close();
			dos.flush();
			dos.close();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (is != null) {
					is.close();
				}
				if (outStream != null) {
					outStream.close();
				}
				if (conn != null) {
					conn.disconnect();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			is = null;
			outStream = null;
			conn = null;
		}
		return createResourcePostResponse;
	}

	/**
	 * 
	 * @param name
	 * @param value
	 * @param boundary
	 * @param ds
	 * @throws Exception
	 */
	private static void writeStringParams(String name, String value, String boundary, DataOutputStream ds)
			throws Exception {
		ds.writeBytes("--" + boundary + "\r\n");
		ds.writeBytes("Content-Disposition: form-data; name=\"" + name + "\"\r\n");
		ds.writeBytes("\r\n");
		ds.writeBytes(URLEncoder.encode(value, "UTF-8") + "\r\n");
	}

	private static void handleCookie(String url, HttpResponse response) {
		Header[] setCookie = response.getHeaders(SM.SET_COOKIE);
		for (Header cookie : setCookie) {
			String str = cookie.getValue();
			setCookie(url, str);
		}
		Header[] Cookie = response.getHeaders(SM.COOKIE);
		for (Header cokie : Cookie) {
			String str = cokie.getValue();
			setCookie(url, str);
		}
		Header[] Cookie2 = response.getHeaders(SM.COOKIE2);
		for (Header cokie : Cookie2) {
			String str = cokie.getValue();
			setCookie(url, str);
		}
	}

	/**
	 * 添加相应url的cookie.
	 * 
	 * @param inUrl
	 *            url地址.
	 * @param cookie
	 *            cookie值.
	 */
	public static final void setCookie(String inUrl, String cookie) {
		if (null == cookie) {
			return;
		}
		/**
		 * String tmp = new String(cookie).trim().toLowerCase(); int index =
		 * tmp.indexOf("domain"); if(index <= 0){ try{ Uri i = Uri.parse(inUrl);
		 * String host = i.getHost(); cookie = cookie + "; Domain=" + host +
		 * ";"; }catch (Exception e) { ; } }
		 **/
		CookieManager.getInstance().setCookie(inUrl, cookie);
		CookieSyncManager.getInstance().sync();
	}

	/**
	 * 根据url获取对应的cookie.
	 * 
	 * @param inUrl
	 *            url地址.
	 * @return 对应的cookie或者null.
	 */
	public static final String getCookie(String inUrl) {

		return CookieManager.getInstance().getCookie(inUrl);
	}

	/**
	 * 初始化https请求的证书状态
	 * 
	 * @param context
	 * @param appId
	 */
	private static boolean isInitialedHttpClient = false;
	private static boolean isCertificate = false;
	private static boolean isUpdateWidget = false;
	private static String hexStr = "0123456789ABCDEF";

	private static HttpClient initHttpClient(Context context, String appId, String url) {
		initHttpCertificateStatus(context, appId);
		return HttpClientUtility.getNewHttpClient(context, url);
	}

	private static HttpURLConnection initHttpURLConnection(Context context, String appId, URL url) {
		initHttpCertificateStatus(context, appId);
		return HttpClientUtility.getNewHttpURLConnection(context, url);
	}

	private static void initHttpCertificateStatus(Context context, String appId) {
		if (isInitialedHttpClient) {
			return;
		}
		checkAppStatus(context, appId);
		// TODO 测试
		// isCertificate = true;
		if (isCertificate) {
			String cPath = null;
			if (isUpdateWidget) {
				cPath = context.getFilesDir().getPath() + "/" + "widget/wgtRes/clientCertificate.p12";
			} else {
				cPath = "file:///android_asset/widget/wgtRes/clientCertificate.p12";
			}
			HttpClientUtility.setCertificate(isCertificate, EUtil.getCertificatePsw(context, appId), cPath);
		} else {
			HttpClientUtility.setCertificate(isCertificate, null, null);
		}
		isInitialedHttpClient = true;
	}

	private static void checkAppStatus(Context inActivity, String appId) {
		try {
			String appstatus = ResoureFinder.getInstance().getString(inActivity, "appstatus");
			byte[] appstatusToByte = HexStringToBinary(appstatus);
			String appstatusDecrypt = new String(
					PEncryption.os_decrypt(appstatusToByte, appstatusToByte.length, appId));
			// appstatusDecrypt = "1,1,1,1,1,1,1,0,0,1,1,0,0";
			String[] appstatuss = appstatusDecrypt.split(",");

			if (appstatuss == null || appstatuss.length == 0) {
				return;
			}
			if ("1".equals(appstatuss[8])) {
				isCertificate = true;
			}
			if ("1".equals(appstatuss[9])) {
				isUpdateWidget = true;
			}
			if ("1".equals(appstatuss[10])) {
				// mdmStatus = true;
			}
		} catch (Exception e) {
			// LogUtils.oe("checkAppStatus", e);
		}

	}

	/**
	 * 
	 * @param hexString
	 * @return 将十六进制转换为字节数组
	 */
	public static byte[] HexStringToBinary(String hexString) {
		// hexString的长度对2取整，作为bytes的长度
		int len = hexString.length() / 2;
		byte[] bytes = new byte[len];
		byte high = 0;// 字节高四位
		byte low = 0;// 字节低四位

		for (int i = 0; i < len; i++) {
			// 右移四位得到高位
			high = (byte) ((hexStr.indexOf(hexString.charAt(2 * i))) << 4);
			low = (byte) hexStr.indexOf(hexString.charAt(2 * i + 1));
			bytes[i] = (byte) (high | low);// 高地位做或运算
		}
		return bytes;
	}
}
