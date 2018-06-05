# downloader
安卓下载器 支持断点续传
```
final DownloadManager instance = DownloadManager.getInstance(getApplication());
		Document document = new Document();
		document.setName("葫芦娃");
		document.setId(1);
		document.setUrl("http://apk500.bce.baidu-mgame.com/game/1338000/1338942/20180413120409_oem_5500044.apk?r=1");
		File externalCacheDir = getExternalCacheDir();

		File file = new File(externalCacheDir,"download");
		if(!file.exists()){
			file.mkdirs();
		}
		document.setFilePath(file.getPath());
		instance.addDownloadListener(new DownloadManager.DownloadListener() {
			@Override
			public void onUpdateProgress(Document info) {
				progressBar.setProgress(info.getDownloadProgress());
			}

			@Override
			public void onDownloadCompleted(Document info) {
				Toast.makeText(MainActivity.this,info.getName()+"下载完成",Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onDownloadFailed(Document info) {
				Toast.makeText(MainActivity.this,info.getName()+"下载失败",Toast.LENGTH_SHORT).show();
			}
		});
		instance.download(document);
		btn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				List<Document> allTasks = instance.getAllTasks();
				Document document1 = allTasks.get(0);
				if(document1.getStatus()==DataBaseFiledParams.LOADING){
					instance.pause(document1);
				}else{
					instance.download(document1);
				}

			}
		});
```
