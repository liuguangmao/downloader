package com.gm.downloadlib;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


import java.util.ArrayList;
import java.util.List;

/**
 * 数据库操作类 操作下载历史表
 *
 * @author 刘广茂
 */
public class DataBaseHelper extends SQLiteOpenHelper {

	/**
	 * 数据库锁
	 */
	private static String Lock = "dblock";

	private static final int DB_VERSION = 8;

	public DataBaseHelper(Context context) {
		super(context, DataBaseFiledParams.DB_FILE_NAME, null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE "
				+ DataBaseFiledParams.DOWNlOAD_HISTORY_TABLE_NAME + "("
				+ DataBaseFiledParams.ID + " INTEGER,"
				+ DataBaseFiledParams.HAS_DONE + " INTEGER,"
				+ DataBaseFiledParams.DOWNLOAD_STATUS + " INTEGER,"
				+ DataBaseFiledParams.VERSION + " INTEGER,"
				+ DataBaseFiledParams.INSERT_TIME + " TEXT,"
				+ DataBaseFiledParams.FILE_NAME + " TEXT,"
				+ DataBaseFiledParams.FILE_PATH + " TEXT,"
				+ DataBaseFiledParams.DOWNLOAD_SIZE + " LONG,"
				+ DataBaseFiledParams.FILE_URL + " TEXT,"
				+ DataBaseFiledParams.FILE_SIZE + " LONG,"
				+ DataBaseFiledParams.DOWNLOAD_PROGRESS + " INTEGER,"
				+ DataBaseFiledParams.IS_CHECKED + " INTEGER,"
				+ DataBaseFiledParams.EXTRA + " INTEGER,"
				+ DataBaseFiledParams.EXTRA_STRING + " TEXT,"
				+ DataBaseFiledParams.REAL_FILE_ID + " TEXT,"
				+ DataBaseFiledParams.PID + " INTEGER);");
	}

	/**
	 * 插入单条数据
	 *
	 * @param info
	 */
	public void insertValue(Document info) {
		if (info != null) {
			synchronized (Lock) {
				SQLiteDatabase db = getWritableDatabase();
				ContentValues values = new ContentValues();
				String curentTime = Long.toString(System.currentTimeMillis());
				values.put(DataBaseFiledParams.ID, info.getId());
				values.put(DataBaseFiledParams.FILE_NAME, info.getName());
				values.put(DataBaseFiledParams.FILE_PATH, info.getFilePath());
				values.put(DataBaseFiledParams.FILE_URL, info.getUrl());
				values.put(DataBaseFiledParams.PID, info.getPid());
				values.put(DataBaseFiledParams.FILE_SIZE, info.getFileSize());
				values.put(DataBaseFiledParams.VERSION, info.getVersion());
				values.put(DataBaseFiledParams.DOWNLOAD_SIZE,
						info.getCompletedSize());
				values.put(DataBaseFiledParams.DOWNLOAD_STATUS,
						info.getStatus());
				values.put(DataBaseFiledParams.HAS_DONE,
						info.isHasDone() ? DataBaseFiledParams.DONE
								: DataBaseFiledParams.UNDONE);
				values.put(DataBaseFiledParams.INSERT_TIME, curentTime);
				values.put(DataBaseFiledParams.EXTRA, info.getExtra());
				values.put(DataBaseFiledParams.REAL_FILE_ID, info.getRealFileId());
				values.put(DataBaseFiledParams.EXTRA_STRING, info.getExtraString());
				db.insert(DataBaseFiledParams.DOWNlOAD_HISTORY_TABLE_NAME,
						null, values);
			}
		}
	}


	/**
	 * 删除单条数据
	 *
	 * @param info
	 */
	public void deleteValue(Document info) {
		if (info != null) {
			synchronized (Lock) {
				SQLiteDatabase db = getWritableDatabase();
				db.delete(DataBaseFiledParams.DOWNlOAD_HISTORY_TABLE_NAME,
						DataBaseFiledParams.ID + "=?",
						new String[]{info.getId() + ""});
			}
		}
	}

	public void deleteByRealId(String courseId) {

		if (courseId != null) {
			try {
				synchronized (Lock) {
					SQLiteDatabase db = getWritableDatabase();
					db.delete(DataBaseFiledParams.DOWNlOAD_HISTORY_TABLE_NAME,
							DataBaseFiledParams.REAL_FILE_ID + "=?",
							new String[]{courseId});
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 获取所有下载过的信息
	 */
	public List<Document> getInfos() {
		SQLiteDatabase db = getReadableDatabase();
		String sql = "select * from "
				+ DataBaseFiledParams.DOWNlOAD_HISTORY_TABLE_NAME
				+ " order by " + DataBaseFiledParams.INSERT_TIME + " DESC";
		List<Document> infos = getDataFromCursor(db, sql);
		return infos;
	}

	/**
	 * 获取下载未完成数据
	 *
	 * @return
	 */
	public List<Document> getUndoneInfos() {
		SQLiteDatabase db = getReadableDatabase();
		String sql = "select * from "
				+ DataBaseFiledParams.DOWNlOAD_HISTORY_TABLE_NAME + " where "
				+ DataBaseFiledParams.HAS_DONE + "="
				+ DataBaseFiledParams.UNDONE + " order by "
				+ DataBaseFiledParams.INSERT_TIME + " DESC";
		List<Document> infos = getDataFromCursor(db, sql);
		return infos;
	}

	/**
	 * 根据id获取文档信息
	 *
	 * @param id
	 * @return
	 */
	public Document getInfo(int id) {
		Document info = null;
		SQLiteDatabase db = getReadableDatabase();
		String sql = "select * from "
				+ DataBaseFiledParams.DOWNlOAD_HISTORY_TABLE_NAME + " where "
				+ DataBaseFiledParams.ID + "=" + id;
		List<Document> infos = getDataFromCursor(db, sql);
		if (infos != null && infos.size() != 0) {
			info = infos.get(0);
		}
		return info;
	}

	/**
	 * 从查询游标内获取数据
	 *
	 * @param db
	 * @param sql
	 * @return
	 */
	private List<Document> getDataFromCursor(SQLiteDatabase db, String sql) {
		Cursor cursor;
		cursor = db.rawQuery(sql, null);
		List<Document> infos = new ArrayList<Document>();
		while (cursor != null && cursor.moveToNext()) {
			Document info = new Document();
			info.setId(cursor.getInt(cursor
					.getColumnIndex(DataBaseFiledParams.ID)));
			info.setPid(cursor.getInt(cursor
					.getColumnIndex(DataBaseFiledParams.PID)));
			info.setName(cursor.getString(cursor
					.getColumnIndex(DataBaseFiledParams.FILE_NAME)));
			info.setFilePath(cursor.getString(cursor
					.getColumnIndex(DataBaseFiledParams.FILE_PATH)));
			info.setHasDone(transferJudgment(cursor
					.getColumnIndex(DataBaseFiledParams.HAS_DONE)));
			info.setFileSize(cursor.getLong(cursor
					.getColumnIndex(DataBaseFiledParams.FILE_SIZE)));
			info.setVersion(cursor.getInt(cursor
					.getColumnIndex(DataBaseFiledParams.VERSION)));
			info.setUrl(cursor.getString(cursor
					.getColumnIndex(DataBaseFiledParams.FILE_URL)));
			info.setDownloadProgress(cursor.getInt(cursor
					.getColumnIndex(DataBaseFiledParams.DOWNLOAD_PROGRESS)));
			info.setStatus(cursor.getInt(cursor
					.getColumnIndex(DataBaseFiledParams.DOWNLOAD_STATUS)));
			info.setCompletedSize(cursor.getLong(cursor
					.getColumnIndex(DataBaseFiledParams.DOWNLOAD_SIZE)));
			info.setExtra(cursor.getInt(cursor
					.getColumnIndex(DataBaseFiledParams.EXTRA)));
			info.setExtraString(cursor.getString(cursor
					.getColumnIndex(DataBaseFiledParams.EXTRA_STRING)));
			info.setRealFileId(cursor.getString(cursor
					.getColumnIndex(DataBaseFiledParams.REAL_FILE_ID)));
			infos.add(info);
		}
		cursor.close();
		return infos;
	}

	/**
	 * 将数据内作为判断的int类型标识转换为bool类型
	 *
	 * @param intValue
	 * @return
	 */
	private boolean transferJudgment(int intValue) {
		boolean bool = false;
		if (intValue == 1) {
			bool = true;
		}
		return bool;
	}

	/**
	 * 将数据属性内的bool类型转换为入库的int类型
	 *
	 * @param bool
	 * @return
	 */
	private int transferJudgment(boolean bool) {
		int result = 0;
		if (bool) {
			result = 1;
		}
		return result;
	}

	/**
	 * 批量删除数据
	 *
	 * @param infos
	 */
	public void deleteValues(List<Document> infos) {
		for (Document docInfo : infos) {
			this.deleteValue(docInfo);
		}
	}

	/**
	 * 删除所有数据
	 */
	public void deleteAll() {
		SQLiteDatabase db = this.getWritableDatabase();
		String sql = "delete from "
				+ DataBaseFiledParams.DOWNlOAD_HISTORY_TABLE_NAME;
		db.execSQL(sql);
	}

	/**
	 * 更新数据
	 *
	 * @param info
	 */
	public void updateValue(Document info) {
		if (info == null) {
			return;
		}
		synchronized (Lock) {
			SQLiteDatabase db = this.getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put(DataBaseFiledParams.HAS_DONE,
					transferJudgment(info.isHasDone()));
			values.put(DataBaseFiledParams.DOWNLOAD_STATUS, info.getStatus());
			values.put(DataBaseFiledParams.DOWNLOAD_PROGRESS,
					info.getDownloadProgress());
			values.put(DataBaseFiledParams.DOWNLOAD_SIZE,
					info.getCompletedSize());
			values.put(DataBaseFiledParams.FILE_URL,
					info.getUrl());
			values.put(DataBaseFiledParams.EXTRA_STRING,
					info.getExtraString());
			values.put(DataBaseFiledParams.EXTRA,
					info.getExtra());
			values.put(DataBaseFiledParams.REAL_FILE_ID,
					info.getRealFileId());
			values.put(DataBaseFiledParams.FILE_SIZE, info.getFileSize());
			int update = db.update(DataBaseFiledParams.DOWNlOAD_HISTORY_TABLE_NAME, values,
					DataBaseFiledParams.ID + "=?", new String[]{info.getId()
							+ ""});
		}
	}

	public void updateVersion(Document info) {
		if (info == null) {
			return;
		}
		synchronized (Lock) {
			SQLiteDatabase db = this.getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put(DataBaseFiledParams.VERSION,
					info.getVersion());
			int update = db.update(DataBaseFiledParams.DOWNlOAD_HISTORY_TABLE_NAME, values,
					DataBaseFiledParams.ID + "=?", new String[]{info.getId()
							+ ""});
			System.out.println("update:" + update + " rows has been affected");
		}
	}


	/**
	 * 批量更新数据
	 *
	 * @param infos
	 */
	public void updateValues(List<Document> infos) {
		for (Document docInfo : infos) {
			updateValue(docInfo);
		}
	}

	/**
	 * 检查数据是否已经存在
	 *
	 * @param info
	 * @return
	 */
	public boolean getHasInserted(Document info) {
		boolean bool = false;
		SQLiteDatabase db = getReadableDatabase();
		int id = info.getId();
		String sql = "select * from "
				+ DataBaseFiledParams.DOWNlOAD_HISTORY_TABLE_NAME + " where "
				+ DataBaseFiledParams.ID + "=" + id;
		Cursor cursor = db.rawQuery(sql, null);
		if (cursor.moveToNext()) {
			bool = true;
		}
		cursor.close();
		return bool;
	}

	/**
	 * 取消全选
	 */
	public void deselectAll() {
		synchronized (Lock) {
			SQLiteDatabase db = this.getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put(DataBaseFiledParams.IS_CHECKED, 0);
			db.update(DataBaseFiledParams.DOWNlOAD_HISTORY_TABLE_NAME, values,
					DataBaseFiledParams.IS_CHECKED + "=?",
					new String[]{1 + ""});
		}
	}

	/**
	 * 全部选中
	 */
	public void selectAll() {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(DataBaseFiledParams.IS_CHECKED, 1);
		db.update(DataBaseFiledParams.DOWNlOAD_HISTORY_TABLE_NAME, values,
				DataBaseFiledParams.IS_CHECKED + "=?", new String[]{0 + ""});
	}

	/**
	 * 删除选中条目
	 */
	public void deleteSelected() {
		synchronized (Lock) {
			SQLiteDatabase db = this.getWritableDatabase();
			db.delete(DataBaseFiledParams.DOWNlOAD_HISTORY_TABLE_NAME,
					DataBaseFiledParams.IS_CHECKED, new String[]{1 + ""});
		}
	}

	/**
	 * 重置登录状态 将由于程序异常退出重启后而造成的下载状态错误进行重置 主要将未完成的下载任务状态修改为暂停
	 */
	public void resetDownloadStatus() {
		synchronized (Lock) {
			SQLiteDatabase db = this.getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put(DataBaseFiledParams.DOWNLOAD_STATUS,
					DataBaseFiledParams.PAUSING);
			db.update(DataBaseFiledParams.DOWNlOAD_HISTORY_TABLE_NAME, values,
					DataBaseFiledParams.HAS_DONE + "=?",
					new String[]{DataBaseFiledParams.UNDONE + ""});
		}

	}


	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		String sql = "ALTER TABLE "
				+ DataBaseFiledParams.DOWNlOAD_HISTORY_TABLE_NAME + " ADD column " + DataBaseFiledParams.EXTRA + " Integer";
		if (oldVersion < 4) {
			db.execSQL(sql);
		}

		if (oldVersion < 5) {
			String addExtraSql = "ALTER TABLE "
					+ DataBaseFiledParams.DOWNlOAD_HISTORY_TABLE_NAME + " ADD column " + DataBaseFiledParams.EXTRA + " Integer";
			db.execSQL(addExtraSql);
		}

		if (oldVersion < 7) {
			try {
				String addExtraStrSql = "ALTER TABLE "
						+ DataBaseFiledParams.DOWNlOAD_HISTORY_TABLE_NAME + " ADD column " + DataBaseFiledParams.EXTRA_STRING + " Text";
				db.execSQL(addExtraStrSql);
			} catch (SQLException e) {
				e.printStackTrace();
			}

			try {
				String addRealFileIdSql = "ALTER TABLE "
						+ DataBaseFiledParams.DOWNlOAD_HISTORY_TABLE_NAME + " ADD column " + DataBaseFiledParams.REAL_FILE_ID + " Text";
				db.execSQL(addRealFileIdSql);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		if (oldVersion < 8) {
			try {
				String addExtraSql = "ALTER TABLE "
						+ DataBaseFiledParams.DOWNlOAD_HISTORY_TABLE_NAME + " ADD column " + DataBaseFiledParams.EXTRA + " Integer";
				db.execSQL(addExtraSql);
			} catch (SQLException e) {
				e.printStackTrace();
			}

		}
	}


}
