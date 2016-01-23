package org.zywx.wbpalmstar.plugin.uexTaskSubmit.JSONBean;

/**
 * 创建资源请求Bean类
 * 
 * @author waka
 *
 */
public class CreateResourcePost {
	public String file = "";
	public String parentId = "";
	public String projectId = "";

	@Override
	public String toString() {
		return "CreateResourcePost [file=" + file + ", parentId=" + parentId + ", projectId=" + projectId + "]";
	}

}
