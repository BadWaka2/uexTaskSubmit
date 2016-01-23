package org.zywx.wbpalmstar.plugin.uexTaskSubmit.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.zywx.wbpalmstar.acedes.ACEDes;
import org.zywx.wbpalmstar.base.BUtility;
import org.zywx.wbpalmstar.plugin.uexTaskSubmit.JSONBean.CreateResourcePostResponse;
import org.zywx.wbpalmstar.plugin.uexTaskSubmit.JSONBean.CreateTaskPostResponse;
import org.zywx.wbpalmstar.plugin.uexTaskSubmit.JSONBean.LoginCASBean;
import org.zywx.wbpalmstar.plugin.uexTaskSubmit.JSONBean.MobilePrjMembers;
import org.zywx.wbpalmstar.plugin.uexTaskSubmit.JSONBean.MobileProcess;
import org.zywx.wbpalmstar.plugin.uexTaskSubmit.JSONBean.PrjId;
import org.zywx.wbpalmstar.plugin.uexTaskSubmit.JSONBean.UserLoginResponse;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;

public class TaskSubmitUtils {

	private static final String TAG = "uexTaskSubmit_Utils";

	/**
	 * 获取当前网络类型
	 */
	public static int getNetworkType(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
		if (networkInfo == null) {
			return -1;
		}
		return networkInfo.getType();
	}

	/**
	 * 检查当前的网络状态
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isNetworkAvailable(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (cm == null) {
			return false;
		}
		NetworkInfo netinfo = cm.getActiveNetworkInfo();
		if (netinfo == null) {
			return false;
		}
		if (netinfo.isConnected()) {
			return true;
		}
		return false;
	}

	// 解析流程列表JSON,返回值为MobileProcess
	public static MobileProcess analysisJSONForGetMobileProcess(String strJSON) {
		MobileProcess mobileProcess = new MobileProcess();
		try {
			JSONObject jsonObject = new JSONObject(strJSON);
			mobileProcess.status = jsonObject.getString("status");
			// 如果返回success，则解析
			if (mobileProcess.status.equals("success")) {
				JSONArray messages = (JSONArray) jsonObject.get("message");
				for (int i = 0; i < messages.length(); i++) {
					JSONObject message = (JSONObject) messages.get(i);
					MobileProcess.Message message_new = new MobileProcess.Message();// 新建内部类接受数据
					message_new.id = message.getInt("id");
					message_new.createdAt = message.getString("createdAt");
					message_new.updatedAt = message.getString("updatedAt");
					message_new.del = message.getString("del");
					message_new.name = message.getString("name");
					message_new.detail = message.getString("detail");
					message_new.weight = message.getInt("weight");
					message_new.startDate = message.getString("startDate");
					message_new.endDate = message.getString("endDate");
					message_new.projectId = message.getInt("projectId");
					message_new.progress = message.getInt("progress");
					message_new.resourceTotal = message.getInt("resourceTotal");
					message_new.memberTotal = message.getInt("memberTotal");
					message_new.taskTotal = message.getInt("taskTotal");
					message_new.createdAtStr = message
							.getString("createdAtStr");
					message_new.updatedAtStr = message
							.getString("updatedAtStr");
					mobileProcess.messageVector.add(message_new);// 将message_new添加进向量中
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return mobileProcess;
	}

	// 解析项目正式成员JSON,返回值为MobilePrjMembers
	public static MobilePrjMembers analysisJSONForGetMobilePrjMembers(
			String strJSON) {
		MobilePrjMembers mobilePrjMembers = new MobilePrjMembers();
		try {
			JSONObject jsonObject = new JSONObject(strJSON);
			mobilePrjMembers.status = jsonObject.getString("status");
			// 如果返回success，则解析
			if (mobilePrjMembers.status.equals("success")) {
				JSONArray messages = (JSONArray) jsonObject.get("message");
				for (int i = 0; i < messages.length(); i++) {
					JSONObject message = (JSONObject) messages.get(i);
					MobilePrjMembers.Message message_new = new MobilePrjMembers.Message();// 新建内部类接受数据
					message_new.id = message.optInt("id");
					message_new.createdAt = message.optString("createdAt");
					message_new.updatedAt = message.optString("updatedAt");
					message_new.del = message.optString("del");
					message_new.account = message.optString("account");
					message_new.icon = message.optString("icon");
					message_new.status = message.optString("status");
					message_new.type = message.optString("type");
					message_new.cellphone = message.optString("cellphone");
					message_new.qq = message.optString("qq");
					// message_new.address = message.getString("address");
					message_new.email = message.optString("email");
					// message_new.gender = message.getString("gender");
					message_new.joinPlat = message.optString("joinPlat");
					message_new.receiveMail = message.optString("receiveMail");
					message_new.userName = message.optString("userName");
					message_new.userlevel = message.optString("userlevel");
					// message_new.remark = message.getString("remark");
					message_new.createdAtStr = message
							.optString("createdAtStr");
					message_new.updatedAtStr = message
							.optString("updatedAtStr");
					mobilePrjMembers.messageVector.add(message_new);// 将message_new添加进向量中
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return mobilePrjMembers;
	}

	// 解析对应的项目IDJSON,返回值为PrjId
	public static PrjId analysisJSONForGetPrjId(String strJSON) {
		PrjId prjId = new PrjId();
		try {
			JSONObject jsonObject = new JSONObject(strJSON);
			prjId.status = jsonObject.getString("status");
			// 如果返回success，则解析
			if (prjId.status.equals("success")) {
				prjId.message = jsonObject.getInt("message");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return prjId;
	}

	// 解析CreateTaskPost请求的返回JSON数据
	public static CreateTaskPostResponse analysisJSONForCreateTaskPostResponse(
			String strJSON) {
		Log.i(TAG, "CreateTaskPost返回的------>>>" + strJSON);
		CreateTaskPostResponse createTaskPostResponse = new CreateTaskPostResponse();
		try {
			JSONObject jsonObject = new JSONObject(strJSON);
			createTaskPostResponse.status = jsonObject.getString("status");
			// 如果返回success，则解析
			if (createTaskPostResponse.status.equals("success")) {
				JSONObject message = new JSONObject(
						jsonObject.getString("message"));
				createTaskPostResponse.message.id = message.getString("id");
				createTaskPostResponse.message.createdAt = message
						.getString("createdAt");
				createTaskPostResponse.message.updatedAt = message
						.getString("updatedAt");
				createTaskPostResponse.message.del = message.getString("del");
				createTaskPostResponse.message.name = message.getString("name");
				createTaskPostResponse.message.detail = message
						.getString("detail");
				createTaskPostResponse.message.processId = message
						.getString("processId");
				createTaskPostResponse.message.appId = message
						.getString("appId");
				createTaskPostResponse.message.priority = message
						.getString("priority");
				createTaskPostResponse.message.repeatable = message
						.getString("repeatable");
				createTaskPostResponse.message.priority = message
						.getString("priority");
				createTaskPostResponse.message.status = message
						.getString("status");
				createTaskPostResponse.message.lastStatusUpdateTime = message
						.getString("lastStatusUpdateTime");
				createTaskPostResponse.message.progress = message
						.getString("progress");
				createTaskPostResponse.message.deadline = message
						.getString("deadline");
				createTaskPostResponse.message.resourceTotal = message
						.getString("resourceTotal");
				createTaskPostResponse.message.commentTotal = message
						.getString("commentTotal");
				createTaskPostResponse.message.processName = message
						.getString("processName");
				createTaskPostResponse.message.projectName = message
						.getString("projectName");
				createTaskPostResponse.message.appName = message
						.getString("appName");
				createTaskPostResponse.message.projectId = message
						.getString("projectId");
				createTaskPostResponse.message.createdAtStr = message
						.getString("createdAtStr");
				createTaskPostResponse.message.updatedAtStr = message
						.getString("updatedAtStr");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return createTaskPostResponse;
	}

	// 解析CreateResourcePost请求的返回JSON数据
	public static CreateResourcePostResponse analysisJSONForCreateResourcePostResponse(
			String strJSON) {
		CreateResourcePostResponse createResourcePostResponse = new CreateResourcePostResponse();
		try {
			JSONObject jsonObject = new JSONObject(strJSON);
			createResourcePostResponse.status = jsonObject.getString("status");
			// 如果返回success，则解析
			if (createResourcePostResponse.status.equals("success")) {
				JSONObject message = new JSONObject(
						jsonObject.getString("message"));
				createResourcePostResponse.message.id = message.getString("id");
				createResourcePostResponse.message.createdAt = message
						.getString("createdAt");
				createResourcePostResponse.message.updatedAt = message
						.getString("updatedAt");
				createResourcePostResponse.message.del = message
						.getString("del");
				createResourcePostResponse.message.name = message
						.getString("name");
				createResourcePostResponse.message.type = message
						.getString("type");
				createResourcePostResponse.message.parentId = message
						.getString("parentId");
				createResourcePostResponse.message.userId = message
						.getString("userId");
				createResourcePostResponse.message.userName = message
						.getString("userName");
				createResourcePostResponse.message.projectId = message
						.getString("projectId");
				createResourcePostResponse.message.fileSize = message
						.getString("fileSize");
				createResourcePostResponse.message.filePath = message
						.getString("filePath");
				createResourcePostResponse.message.sizeStr = message
						.getString("sizeStr");
				createResourcePostResponse.message.createdAtStr = message
						.getString("createdAtStr");
				createResourcePostResponse.message.updatedAtStr = message
						.getString("updatedAtStr");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return createResourcePostResponse;
	}

	public static UserLoginResponse analysisEMMLogin(String data) {
		UserLoginResponse userLoginResponse = new UserLoginResponse();
		userLoginResponse = analysisJSONForUserLoginResponse(data);
		return userLoginResponse;
	}

	// 解析UserLogin请求的返回JSON数据
	public static UserLoginResponse analysisJSONForUserLoginResponse(
			String strJSON) {
		UserLoginResponse userLoginResponse = new UserLoginResponse();
		try {
			JSONObject jsonObject = new JSONObject(strJSON);
			userLoginResponse.status = jsonObject.getString("status");
			// 如果返回success，则解析
			if (userLoginResponse.status.equals("success")) {
				// 解析message
				JSONObject message = new JSONObject(
						jsonObject.getString("message"));
				userLoginResponse.message.object.id = message.optString("id");
				if (message.has("userid")) {
					userLoginResponse.message.object.id = message
							.optString("userid");
				}
				userLoginResponse.message.object.createdAt = message
						.optString("createdAt");
				userLoginResponse.message.object.updatedAt = message
						.optString("updatedAt");
				userLoginResponse.message.object.del = message.optString("del");
				userLoginResponse.message.object.account = message
						.optString("account");
				userLoginResponse.message.object.icon = message
						.optString("icon");
				userLoginResponse.message.object.status = message
						.optString("status");
				userLoginResponse.message.object.type = message
						.optString("type");
				userLoginResponse.message.object.cellphone = message
						.optString("cellphone");
				userLoginResponse.message.object.qq = message.optString("qq");
				userLoginResponse.message.object.email = message
						.optString("email");
				userLoginResponse.message.object.joinPlat = message
						.optString("joinPlat");
				userLoginResponse.message.object.receiveMail = message
						.optString("receiveMail");
				userLoginResponse.message.object.userName = message
						.optString("userName");
				userLoginResponse.message.object.userlevel = message
						.optString("userlevel");
				userLoginResponse.message.object.createdAtStr = message
						.optString("createdAtStr");
				userLoginResponse.message.object.updatedAtStr = message
						.optString("updatedAtStr");
				// 解析permissions
				JSONObject permissions = new JSONObject(
						jsonObject.optString("permissions"));
				userLoginResponse.message.permissions.team_create = permissions
						.optString("team_create");
				userLoginResponse.message.permissions.project_create = permissions
						.optString("project_create");
			} else {
				userLoginResponse.statusInfo = jsonObject.optString("message");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return userLoginResponse;
	}

	/**
	 * CAS 验证 XML解析
	 * 
	 * @param xmlString
	 * @return
	 */
	public static LoginCASBean analysisXML(String xmlString) {
		LoginCASBean loginCASBean = new LoginCASBean();
		try {
			XmlPullParser parser = Xml.newPullParser();// 解析该xml文件
			parser.setInput(new StringReader(xmlString));
			int event = parser.getEventType();
			while (event != XmlPullParser.END_DOCUMENT) {// 只要文档不结束，循环遍历
				if (event == XmlPullParser.START_TAG) {// 标签开始
					if ("authenticationFailure".equals(parser.getName())) {// 判断验证是否失败
						loginCASBean.state = parser.getName();
						event = parser.next();
						break;
					}
					if ("authenticationSuccess".equals(parser.getName())) {// 判断验证是否成功
						loginCASBean.state = parser.getName();
						event = parser.next();
						continue;
					}
					if ("username".equals(parser.getName())) {// 判断开始标签元素是否是username
						loginCASBean.username = parser.nextText();
						event = parser.next();
						continue;
					}
					if ("regip".equals(parser.getName())) {// 判断开始标签元素是否是regip
						loginCASBean.regip = parser.nextText();
						event = parser.next();
						continue;
					}
					if ("nickname".equals(parser.getName())) {// 判断开始标签元素是否是nickname
						loginCASBean.nickname = parser.nextText();
						event = parser.next();
						continue;
					}
					if ("tenants".equals(parser.getName())) {// 判断开始标签元素是否是tenants
						loginCASBean.tenants = parser.nextText();
						event = parser.next();
						continue;
					}
					if ("userid".equals(parser.getName())) {// 判断开始标签元素是否是userid
						loginCASBean.userid = parser.nextText();
						event = parser.next();
						continue;
					}
					if ("tenantid".equals(parser.getName())) {// 判断开始标签元素是否是tenantid
						loginCASBean.tenantid = parser.nextText();
						event = parser.next();
						continue;
					}
					if ("user_pic".equals(parser.getName())) {// 判断开始标签元素是否是user_pic
						loginCASBean.user_pic = parser.nextText();
						event = parser.next();
						continue;
					}
				}
				event = parser.next();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return loginCASBean;
	}
	
	/**
	 * 获得config文件中指定标签的值,传入指定的标签名,返回该标签的值
	 * 
	 * @param context
	 * @param label 要解析的字段
	 * @return
	 */
	public static String getConfigLabelValue(Context context, String label) {
		String value = "";
		// 如果传入的标签不为空（包含null和""两种情况）
		if (!TextUtils.isEmpty(label)) {
			String configFile = context.getFilesDir() + "/widget/config.xml";// 获得沙箱路径,getFilesDir()用于获取/data/data//files目录
			InputStream inputStream = null;
			InputStream is1 = null;
			InputStream is2 = null;
			File file = new File(configFile);
			// 若果从沙箱中读取不到，则再从assets文件夹中读
			if (!file.exists()) {
				try {
					inputStream = context.getAssets().open("widget/config.xml");
				} catch (IOException e) {
					Log.i(TAG, "getAssets IOException");
					e.printStackTrace();
				}
			} else {
				try {
					inputStream = new FileInputStream(file);
				} catch (FileNotFoundException e) {
					Log.i(TAG, "FileNotFoundException");
					e.printStackTrace();
				}
			}
			
			// 正戏开始了，如果输入流不为空
			if (inputStream != null) {
				try {
					XmlPullParser xmlPullParser = Xml.newPullParser();// 使用Xml的静态方法生成语法分析器
					// 先判断是否加密
					ByteArrayOutputStream baos = new ByteArrayOutputStream();

		            byte[] buffer = new byte[1024];
		            int len;
		            while ((len = inputStream.read(buffer)) > -1) {
		                baos.write(buffer, 0, len);
		            }
		            baos.flush();

		            is1 = new ByteArrayInputStream(baos.toByteArray());
		            is2 = new ByteArrayInputStream(baos.toByteArray());

		            boolean isV = ACEDes.isEncrypted(is1);

		            if (isV) {
		                InputStream resStream = null;
		                byte[] data = null;
		                String fileName = "config";
		                String result = null;

		                data = BUtility.transStreamToBytes(is2, is2.available());
		                result = ACEDes.htmlDecode(data, fileName);
		                resStream = new ByteArrayInputStream(result.getBytes());
		                xmlPullParser.setInput(resStream, "utf-8");
		            } else {
		            	xmlPullParser.setInput(is2, "utf-8");
		            }
					
					int eventType = xmlPullParser.getEventType();// 获得解析到的事件类别，这里有开始文档，结束文档，开始标签，结束标签，文本等等事件
					// 循环直到文档结束
					boolean needContinue = true;
					while (needContinue) {
						switch (eventType) {
						// 事件若是开始标签
						case XmlPullParser.START_TAG:
							// 如果该标签是传入的标签，则value=该标签的值
							if (xmlPullParser.getName().equals(label)) {
								try {
									value = xmlPullParser.nextText();
									needContinue = false;
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
							break;
						case XmlPullParser.END_DOCUMENT:
							needContinue = false;
							break;
						default:
							break;
						}
						try {
							eventType = xmlPullParser.next();// 获取下一个事件
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				} catch (XmlPullParserException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (Exception e){
					e.printStackTrace();
				} finally {
					// 如果inputStream不为空，释放掉
					if (inputStream != null) {
						try {
							inputStream.close();
							inputStream = null;
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				} // end finally
			} // end if inputStream
		} // end if label
		return value;
	}

}
