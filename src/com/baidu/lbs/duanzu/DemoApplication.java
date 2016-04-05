package com.baidu.lbs.duanzu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;

import com.baidu.lbs.duanzu.LBSListActivity.ContentAdapter;
import com.baidu.lbs.duanzu.bdapi.LBSLocation;
import com.baidu.lbs.duanzu.bdapi.LBSMapActivity;
import com.baidu.location.BDLocation;

/**
 * С�����Ӧ���࣬��������λ������б����ͼ�����������
 * 
 * @author Lu.Jian
 * 
 */
public class DemoApplication extends Application {

	private static DemoApplication mInstance = null;

	public static final String strKey = "63418012748CD126610D926A0546374D0BFC86D5";

	// ��λ���
	public BDLocation currlocation = null;

	// �������
	private List<ContentModel> list = new ArrayList<ContentModel>();

	// ���ڸ��¼��������ˢ���б�
	private ContentAdapter adapter;

	public static String networkType;

	private Handler handler;
	
	//�Ƽ�������
	private HashMap<String, String> filterParams;	
	
	private LBSListActivity listActivity;
	private LBSMapActivity mapActivity;

	@Override
	public void onCreate() {
		super.onCreate();
		mInstance = this;

		networkType = setNetworkType();
		// ������λ
		LBSLocation.getInstance(this).startLocation();
	}

	public static DemoApplication getInstance() {
		return mInstance;
	}

	public LBSMapActivity getMapActivity() {
		return mapActivity;
	}

	public void setMapActivity(LBSMapActivity mapActivity) {
		this.mapActivity = mapActivity;
	}

	public LBSListActivity getListActivity() {
		return listActivity;
	}

	public void setListActivity(LBSListActivity listActivity) {
		this.listActivity = listActivity;
	}

	public ContentAdapter getAdapter() {
		return adapter;
	}

	public void setAdapter(ContentAdapter adapter) {
		this.adapter = adapter;
	}

	public void setList(List<ContentModel> list) {
		this.list = list;
	}

	public List<ContentModel> getList() {
		return list;
	}

	public Handler getHandler() {
		return handler;
	}

	public void setHandler(Handler handler) {
		this.handler = handler;
	}

	public HashMap<String, String> getFilterParams() {
		return filterParams;
	}

	public void setFilterParams(HashMap<String, String> filterParams) {
		this.filterParams = filterParams;
	}

	/**
	 * �����ֻ��������ͣ�wifi��cmwap��ctwap��������������ѡ��
	 * @return
	 */
	static String setNetworkType() {
		String networkType = "wifi";
		ConnectivityManager manager = (ConnectivityManager) mInstance
				.getSystemService(Context.CONNECTIVITY_SERVICE);

		NetworkInfo netWrokInfo = manager.getActiveNetworkInfo();
		if (netWrokInfo == null || !netWrokInfo.isAvailable()) {
			// ��ǰ���粻����
			return "";
		}

		String info = netWrokInfo.getExtraInfo();
		if ((info != null)
				&& ((info.trim().toLowerCase().equals("cmwap"))
						|| (info.trim().toLowerCase().equals("uniwap"))
						|| (info.trim().toLowerCase().equals("3gwap")) || (info
						.trim().toLowerCase().equals("ctwap")))) {
			// ������ʽΪwap
			if (info.trim().toLowerCase().equals("ctwap")) {
				// ����
				networkType = "ctwap";
			} else {
				networkType = "cmwap";
			}

		}
		return networkType;
	}
	
	public void callStatistics(){
		StatisticsTask task = new StatisticsTask(); 
		task.execute("http://api.map.baidu.com/images/blank.gif?t=92248538&platform=android&logname=lbsyunduanzu");
	}

	/*
	 * �ٶ�ͳ��
	 */
	class StatisticsTask extends AsyncTask<String, Integer, String> { 
		 
        // �ɱ䳤�������������AsyncTask.exucute()��Ӧ 
        @Override 
        protected String doInBackground(String... params) { 
            try { 
                HttpClient client = new DefaultHttpClient(); 
                // params[0] �������ӵ�url 
                HttpGet get = new HttpGet(params[0]); 
                HttpResponse response = client.execute(get); 
                
				int status = response.getStatusLine().getStatusCode();
                if(status == HttpStatus.SC_OK){
                }
                // ���ؽ�� 
                return null; 
            } catch (Exception e) { 
                e.printStackTrace(); 
            } 
            return null; 
        } 
        @Override 
        protected void onCancelled() { 
            super.onCancelled(); 
        } 
        @Override 
        protected void onPostExecute(String result) { 
        } 
        @Override 
        protected void onPreExecute() { 
        } 
        @Override 
        protected void onProgressUpdate(Integer... values) { 
        } 
    }
}