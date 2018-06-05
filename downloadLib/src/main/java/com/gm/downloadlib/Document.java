package com.gm.downloadlib;

import java.io.Serializable;


/**
 * 文档信息类
 * @author 刘广茂
 *
 */
public class Document implements Serializable {

	private static final long serialVersionUID = 5081610806660863176L;
	public static final long TOTAL_ERROR = -1;

	/** 文档名称 */
	private String name;

	/** 文档路径 */
	private String url;

	/** 文档id */
	private int id;

	/** 文档上级id */
	private int pid;

	/** 文档路径 */
	private String filePath;

	/** 是否是文件夹 */
	private boolean isDirectoty;

	/** 是否下载完成 */
	private boolean hasDone = false;

	/**
	 * 下载进度
	 */
	private int downloadProgress;

	/**
	 * 已完成大小
	 */
	private long completedSize;

	/**
	 * 文件大小
	 */
	private long fileSize;

	/**
	 * 文件大小
	 */
	private int version;

	private int extra;

	private String realFileId;

	private String extraString;


	/**
	 * 下载速度
	 */
	private double speed;

	/**
	 * 下载状态
	 */
	private int status = DataBaseFiledParams.WAITING;

	/**
	 * @param name
	 *            名称
	 * @param url
	 *            下载路径
	 * @param id
	 *            文件或文件夹id
	 * @param pid
	 *            所在的文件夹id
	 * @param filePath
	 *            路径
	 * @param isDirectory
	 *            是否是文件目录
	 */
	public Document(String name, String url, int id, int pid, String filePath,
	                boolean isDirectory) {
		super();
		this.name = name;
		this.url = url;
		this.id = id;
		this.pid = pid;
		this.filePath = filePath;
		this.isDirectoty = isDirectory;
	}

	public Document() {

	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getPid() {
		return pid;
	}

	public void setPid(int pid) {
		this.pid = pid;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public boolean isDirectoty() {
		return isDirectoty;
	}

	public void setDirectoty(boolean isDirectoty) {
		this.isDirectoty = isDirectoty;
	}

	/**
	 * @return the hasDone
	 */
	public boolean isHasDone() {
		return hasDone;
	}

	/**
	 * 
	 * @param hasDone
	 *            the hasDone to set
	 */
	public void setHasDone(boolean hasDone) {
		this.hasDone = hasDone;
	}

	/**
	 * @return the downloadProgress
	 */
	public int getDownloadProgress() {
		return downloadProgress;
	}

	/**
	 * @param downloadProgress
	 *            the downloadProgress to set
	 */
	public void setDownloadProgress(int downloadProgress) {
		this.downloadProgress = downloadProgress;
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}
		if (object == null) {
			return false;
		}
		if (object instanceof Document) {
			Document info = (Document) object;
			return this.id == info.getId();
		}
		return super.equals(object);
	}

	@Override
	public int hashCode() {
		return this.id;
	}

	/**
	 * @return the completedSize
	 */
	public long getCompletedSize() {
		return completedSize;
	}

	/**
	 * @param completedSize
	 *            the completedSize to set
	 */
	public void setCompletedSize(long completedSize) {
		this.completedSize = completedSize;
	}

	/**
	 * @return the fileSize
	 */
	public long getFileSize() {
		return fileSize;
	}

	/**
	 * @param fileSize
	 *            the fileSize to set
	 */
	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}

	/**
	 * @return the status
	 */
	public int getStatus() {
		return status;
	}

	/**
	 * @param status
	 *            the status to set
	 */
	public void setStatus(int status) {
		this.status = status;
	}



	/**
	 * @return the speed
	 */
	public double getSpeed() {
		return speed;
	}

	/**
	 * @param speed the speed to set
	 */
	public void setSpeed(double speed) {
		this.speed = speed;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public int getExtra() {
		return extra;
	}

	public void setExtra(int extra) {
		this.extra = extra;
	}

	public String getRealFileId() {
		return realFileId;
	}

	public void setRealFileId(String realFileId) {
		this.realFileId = realFileId;
	}

	public String getExtraString() {
		return extraString;
	}

	public void setExtraString(String extraString) {
		this.extraString = extraString;
	}
}
