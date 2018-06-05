package com.gm.dowloader;

import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.gm.downloadlib.DataBaseFiledParams;
import com.gm.downloadlib.Document;
import com.gm.downloadlib.DownloadManager;

import java.io.File;
import java.util.List;

public class MainActivity extends AppCompatActivity {

	ProgressBar progressBar;
	Button btn;
	@Override
	protected void onCreate(Bundle savedInstanceState) {


		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		progressBar = findViewById(R.id.progressBar);
		btn = findViewById(R.id.ctl);
		final DownloadManager instance = DownloadManager.getInstance(getApplication());
		Document document = new Document();
//		document.setName("葫芦娃");
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

	}
}
