package com.gm.downloadlib;


import android.app.Application;
import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicReference;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class DownloadManager {

	private static final AtomicReference<DownloadManager> INSTANCE = new AtomicReference<>();
	private HashMap<Document, Call> downCalls;//用来存放各个下载的请求

	private static DataBaseHelper dataBaseHelper;

	private OkHttpClient mClient;//OKHttpClient;
	private Timer timer;
	private boolean bool = false;


	private DownloadObserver downloadObserver = new DownloadObserver() {
		@Override
		public void onComplete() {

		}

		@Override
		public void onNext(Document downloadInfo) {
			long fileSize = downloadInfo.getFileSize() == 0 ? 1 : downloadInfo.getFileSize();
			long l = downloadInfo.getCompletedSize() * 100 / fileSize;
			downloadInfo.setDownloadProgress((int) l);
			if (downloadInfo.getStatus() == DataBaseFiledParams.FAILED) {
				notifyAllListnerFailed(downloadInfo);
			} else if (bool || fileSize == downloadInfo.getCompletedSize()) {
				notifyAllListner(downloadInfo);
			}
			super.onNext(downloadInfo);
		}

		@Override
		public void onError(Throwable e) {

			super.onError(e);
		}

		@Override
		public void onSubscribe(Disposable d) {
			super.onSubscribe(d);
		}
	};

	//获得一个单例类
	public static DownloadManager getInstance(Application context) {
		for (; ; ) {
			DownloadManager current = INSTANCE.get();
			if (current != null) {
				return current;
			}
			current = new DownloadManager();
			dataBaseHelper = new DataBaseHelper(context);
			if (INSTANCE.compareAndSet(null, current)) {
				return current;
			}
		}
	}

	private DownloadManager() {
		downCalls = new HashMap<>();
		mClient = new OkHttpClient.Builder().build();
	}

	private void notifyAllListnerFailed(Document downloadInfo) {
		for (DownloadListener listner : downloadListeners) {
			listner.onUpdateProgress(downloadInfo);
			if (downloadInfo.getCompletedSize() == downloadInfo.getFileSize()) {
				listner.onDownloadCompleted(downloadInfo);
			}
		}
		downloadListeners.removeAll(removedListeners);
		removedListeners.clear();

	}


	private List<Document> notifyList = new ArrayList<>();

	private void notifyAllListner(Document downloadInfo) {
		for (DownloadListener listner : downloadListeners) {
			listner.onUpdateProgress(downloadInfo);
			if (downloadInfo.getCompletedSize() == downloadInfo.getFileSize()) {
				downloadInfo.setStatus(DataBaseFiledParams.DONE);
				downloadInfo.setDownloadProgress(100);
				dataBaseHelper.updateValue(downloadInfo);
				removedListeners.add(listner);
				if (!notifyList.contains(downloadInfo)) {

					listner.onDownloadCompleted(downloadInfo);
					notifyList.add(downloadInfo);
				}

			}
		}
		downloadListeners.removeAll(removedListeners);
		removedListeners.clear();
	}

	public void deleteById(String courseId) {

		dataBaseHelper.deleteByRealId(courseId);

	}

	public void updateDoc(Document info) {
		dataBaseHelper.updateValue(info);
	}


	/**
	 * 下载监听回调接口
	 *
	 * @author liuguangmao
	 */
	public interface DownloadListener {
		/**
		 * 进度更新
		 *
		 * @param info
		 */
		void onUpdateProgress(Document info);

		/**
		 * 下载完成
		 *
		 * @param info
		 */
		void onDownloadCompleted(Document info);

		/**
		 * 下载失败
		 *
		 * @param info
		 */
		void onDownloadFailed(Document info);
	}

	private List<DownloadListener> downloadListeners = new ArrayList<>();
	private List<DownloadListener> removedListeners = new ArrayList<>();

	public void addDownloadListener(DownloadListener l) {
		downloadListeners.add(l);
		//listener = l;
	}
	public void removeDownloadListener(DownloadListener l) {

		removedListeners.add(l);
		//listener = l;
	}


	/**
	 * 开始下载
	 *
	 * @param document 下载的文件
	 */
	public void download(final Document document) {

		notifyList.remove(document);
		Observable.just(document)
				.filter(new Predicate<Document>() {
					@Override
					public boolean test(Document s) throws Exception {
						return !downCalls.containsKey(s);
					}
				})//call的map已经有了,就证明正在下载,则这次不下载
				.flatMap(new Function<Document, ObservableSource<? extends Document>>() {
					@Override
					public ObservableSource<? extends Document> apply(Document s) throws Exception {
						return Observable.just(getDocDataFromDB(s));
					}
				})
				.flatMap(new Function<Document, ObservableSource<? extends Document>>() {
					@Override
					public ObservableSource<? extends Document> apply(Document s) throws Exception {
						return Observable.just(DownloadManager.this.createDownInfo(s));
					}
				})
				.map(new Function<Document, Document>() {
					@Override
					public Document apply(Document downloadInfo1) throws Exception {
						return DownloadManager.this.getRealFileName(downloadInfo1);
					}
				})
				.flatMap(new Function<Document, ObservableSource<? extends Document>>() {
					@Override
					public ObservableSource<? extends Document> apply(Document downloadInfo) throws Exception {
						return Observable.create(new DownloadSubscribe(downloadInfo));//下载
					}
				}).observeOn(AndroidSchedulers.mainThread())//在主线程回调
				.subscribeOn(Schedulers.io())//在子线程执行
				.subscribe(downloadObserver);//添加观察者
		startTimer();
	}


	private Document getDocDataFromDB(Document s) {
		Document info = dataBaseHelper.getInfo(s.getId());
		s.setStatus(DataBaseFiledParams.WAITING);
		if (info == null) {
			info = s;
			dataBaseHelper.insertValue(s);
		} else {
			dataBaseHelper.updateValue(s);
			dataBaseHelper.updateVersion(s);
			info.setUrl(s.getUrl());
		}

		return info;
	}

	public void pause(Document document) {
		Call call = downCalls.get(document);
		if (call != null) {
			call.cancel();//取消
		}
		Document info = dataBaseHelper.getInfo(document.getId());
		if (info != null) {
			info.setStatus(DataBaseFiledParams.PAUSING);
		}
		dataBaseHelper.updateValue(info);
		downCalls.remove(document);
	}


	public List<Document> getAllTasks() {
		return dataBaseHelper.getInfos();
	}


	/**
	 * 创建DownInfo
	 *
	 * @param document 请求网址
	 * @return DownInfo
	 */
	private Document createDownInfo(Document document) {
		long contentLength = getContentLength(document.getUrl());//获得文件大小
		document.setFileSize(contentLength);
		return document;
	}

	private Document getRealFileName(Document downloadInfo) {


		String fileName = downloadInfo.getName();
		long downloadLength = 0, contentLength = downloadInfo.getFileSize();
		File file = new File(downloadInfo.getFilePath(), fileName);
		File file1 = new File(downloadInfo.getFilePath());
		if (!file1.exists()) {
			file1.mkdirs();
		}
		if (file.exists()) {
			//找到了文件,代表已经下载过,则获取其长度
			downloadLength = file.length();
		}
		//之前下载过,需要重新来一个文件
		int i = 1;
		downloadInfo.setCompletedSize(downloadLength);
		downloadInfo.setName(file.getName());
		if (downloadLength >= contentLength) {

			File newFile = new File(downloadInfo.getFilePath(), fileName);
			newFile.delete();
			downloadInfo.setCompletedSize(0);
//			int dotIndex = fileName.lastIndexOf(".");
//			String fileNameOther;
//			if (dotIndex == -1) {
//				fileNameOther = fileName + "(" + i + ")";
//			} else {
//				fileNameOther = fileName.substring(0, dotIndex)
//						+ "(" + i + ")" + fileName.substring(dotIndex);
//			}
//			 newFile = new File(downloadInfo.getFilePath(), fileNameOther);
//			file = newFile;
//			downloadLength = newFile.length();
//			i++;
		}
		//设置改变过的文件名/大小

		return downloadInfo;
	}


	private class DownloadSubscribe implements ObservableOnSubscribe<Document> {
		private Document downloadInfo;

		public DownloadSubscribe(Document downloadInfo) {
			this.downloadInfo = downloadInfo;
		}


		@Override
		public void subscribe(ObservableEmitter<Document> e) {
			String url = downloadInfo.getUrl();
			long downloadLength = downloadInfo.getCompletedSize();//已经下载好的长度
			long contentLength = downloadInfo.getFileSize();//文件的总长度
			//初始进度信息
			e.onNext(downloadInfo);
			Request request = new Request.Builder()
					//确定下载的范围,添加此头,则服务器就可以跳过已经下载好的部分
					.addHeader("RANGE", "bytes=" + downloadLength + "-" + contentLength)
					.url(url)
					.build();
			Call call = mClient.newCall(request);
			downCalls.put(downloadInfo, call);//把这个添加到call里,方便取消
			InputStream is = null;
			FileOutputStream fileOutputStream = null;
			downloadInfo.setStatus(DataBaseFiledParams.LOADING);
			try {
				Response response = call.execute();
				String filePath = downloadInfo.getFilePath();
				File downloadDirectory = new File(filePath);
				if (!downloadDirectory.exists()) {
					downloadDirectory.mkdirs();
				}
				File file = new File(filePath, downloadInfo.getName());
				is = response.body().byteStream();
				fileOutputStream = new FileOutputStream(file, true);
				byte[] buffer = new byte[2048];//缓冲数组2kB
				int len;
				while ((len = is.read(buffer)) != -1) {
					fileOutputStream.write(buffer, 0, len);
					downloadLength += len;
					downloadInfo.setCompletedSize(downloadLength);
					dataBaseHelper.updateValue(downloadInfo);
					e.onNext(downloadInfo);
				}
				fileOutputStream.flush();
				downCalls.remove(downloadInfo);
			} catch (Exception ioException) {
				downloadInfo.setStatus(DataBaseFiledParams.FAILED);
				e.onNext(downloadInfo);
				downCalls.remove(downloadInfo);
				dataBaseHelper.updateValue(downloadInfo);
				e.onError(ioException);

			} finally {
				//关闭IO流
				IOUtil.closeAll(is, fileOutputStream);

			}
			e.onComplete();//完成
		}
	}

	/**
	 * 获取下载长度
	 *
	 * @param downloadUrl
	 * @return
	 */
	private long getContentLength(String downloadUrl) {
		Request request = new Request.Builder()
				.url(downloadUrl)
				.build();
		try {
			Response response = mClient.newCall(request).execute();
			if (response != null && response.isSuccessful()) {
				long contentLength = response.body().contentLength();
				response.close();
				return contentLength == 0 ? Document.TOTAL_ERROR : contentLength;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return Document.TOTAL_ERROR;
	}


	/**
	 * 开始计时器
	 */
	private void startTimer() {
		timer = new Timer();
		/* 定时任务，定时打开开关，向界面发送下载进度 */
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				bool = true;
			}
		};
		timer.schedule(task, 200, 2000);
	}

}
