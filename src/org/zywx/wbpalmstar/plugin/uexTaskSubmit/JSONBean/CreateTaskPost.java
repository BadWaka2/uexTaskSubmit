package org.zywx.wbpalmstar.plugin.uexTaskSubmit.JSONBean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 创建任务请求Bean类,实现Parcelable接口，使Intent能够传递CreateTaskPost
 * 
 * @author yajun.duan,waka
 *
 */
public class CreateTaskPost implements Parcelable {
	public String name = "";
	public String detail = "";
	public String processId = "";
	public String appId = "";
	public String priority = "";
	public String repeatable = "";
	public String tag = "";
	public String resource = "";
	public String leader = "";
	public String member = "";
	public String deadline = "";

	public CreateTaskPost() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public String toString() {
		return "CreateTaskPost [name=" + name + ", detail=" + detail + ", processId=" + processId + ", appId=" + appId
				+ ", priority=" + priority + ", repeatable=" + repeatable + ", tag=" + tag + ", resource=" + resource
				+ ", leader=" + leader + ", member=" + member + ", deadline=" + deadline + "]";
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(name);
		dest.writeString(detail);
		dest.writeString(processId);
		dest.writeString(appId);
		dest.writeString(priority);
		dest.writeString(repeatable);
		dest.writeString(tag);
		dest.writeString(resource);
		dest.writeString(leader);
		dest.writeString(member);
		dest.writeString(deadline);
	}

	public CreateTaskPost(Parcel parcel) {
		this.name = parcel.readString();
		this.detail = parcel.readString();
		this.processId = parcel.readString();
		this.appId = parcel.readString();
		this.priority = parcel.readString();
		this.repeatable = parcel.readString();
		this.tag = parcel.readString();
		this.resource = parcel.readString();
		this.leader = parcel.readString();
		this.member = parcel.readString();
		this.deadline = parcel.readString();
	}

	public static final Creator<CreateTaskPost> CREATOR = new Creator<CreateTaskPost>() {

		@Override
		public CreateTaskPost[] newArray(int size) {
			return new CreateTaskPost[size];
		}

		@Override
		public CreateTaskPost createFromParcel(Parcel source) {
			return new CreateTaskPost(source);
		}

	};

}
