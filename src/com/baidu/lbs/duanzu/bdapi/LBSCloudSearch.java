package com.baidu.lbs.duanzu.bdapi;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.util.EntityUtils;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.baidu.lbs.duanzu.MainActivity;


/**
 * 百度云检索使用类
 * @author Lu.Jian
 *
 */
public class LBSCloudSearch {
	
	private static String mTAG = "NetWorkManager";
	
	//百度云检索API URI
	private static final String SEARCH_URI_NEARBY = "http://api.map.baidu.com/geosearch/v2/nearby?";
	private static final String SEARCH_URI_LOCAL = "http://api.map.baidu.com/geosearch/v2/local?";
	
	public static final int SEARCH_TYPE_NEARBY = 1;
	public static final int SEARCH_TYPE_LOCAL = 2;
	
	private static int currSearchType = 0;
	
	//云检索公钥
	private static String ak = "A4749739227af1618f7b0d1b588c0e85";
	
	private static String geotable_id = "30960";

	private static int TIME_OUT = 12000;
	private static int retry = 3;
	private static boolean IsBusy = false;
	
	/**
	 * 云检索访问
	 * @param filterParams	访问参数，key为filter时特殊处理。
	 * @param handler		数据回调Handler
	 * @param networkType	手机联网类型
	 * @return
	 */
	public static boolean request(final int searchType,final HashMap<String, String> filterParams,final Handler handler, final String networkType) {
		if (IsBusy || filterParams == null)
			return false;
		IsBusy = true;
		
		new Thread() {
			public void run() {
				int count = retry;
				while (count > 0){
					try {
						
						//根据过滤选项拼接请求URL
						String requestURL = "";
						if(searchType == -1){
							//沿用上次搜索保存的search type
							if(currSearchType == SEARCH_TYPE_NEARBY){
								requestURL = SEARCH_URI_NEARBY;
							}else if(currSearchType == SEARCH_TYPE_LOCAL){
								requestURL = SEARCH_URI_LOCAL;
							}
						}else{
							if(searchType == SEARCH_TYPE_NEARBY){
								requestURL = SEARCH_URI_NEARBY;
							}else if(searchType == SEARCH_TYPE_LOCAL){
								requestURL = SEARCH_URI_LOCAL;
							}
							currSearchType = searchType;
						}
						requestURL = requestURL   + "&"
										+ "ak=" + ak
										+ "&geotable_id=" + geotable_id; 
						
						String filter = null;
						Iterator iter = filterParams.entrySet().iterator();
						while (iter.hasNext()) {
							Map.Entry entry = (Map.Entry) iter.next();
							String key = entry.getKey().toString();
							String value = entry.getValue().toString();
							
							if(key.equals("filter")){
								filter = value;
							}else{
								if(key.equals("region") && currSearchType == SEARCH_TYPE_NEARBY){
									continue;
								}
								requestURL = requestURL + "&" + key + "=" + value;
							}
						}
						
						if(filter != null && !filter.equals("")){
							//substring(3) 为了去掉"|" 的encode  "%7C"
							requestURL = requestURL + "&filter=" + filter.substring(3);
						}
						
						Log.d("DuanZuLog", "request url:" + requestURL);
						
						HttpGet httpRequest = new HttpGet(requestURL);
						HttpClient httpclient = new DefaultHttpClient();
						httpclient.getParams().setParameter(
								CoreConnectionPNames.CONNECTION_TIMEOUT, TIME_OUT);
						httpclient.getParams().setParameter(
								CoreConnectionPNames.SO_TIMEOUT, TIME_OUT);
						
						HttpProtocolParams.setUseExpectContinue(httpclient.getParams(), false);
						
						if(networkType.equals("cmwap")){
							HttpHost proxy = new HttpHost("10.0.0.172", 80, "http");
							httpclient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY,
									proxy);
						}else if(networkType.equals("ctwap")){
							HttpHost proxy = new HttpHost("10.0.0.200", 80, "http");
							httpclient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY,
									proxy);
						}


						HttpResponse httpResponse = httpclient.execute(httpRequest);
						int status = httpResponse.getStatusLine().getStatusCode();
						if ( status == HttpStatus.SC_OK ) {
							
							String result = EntityUtils.toString(httpResponse
									.getEntity(), "utf-8");
							Header a = httpResponse.getEntity().getContentType();
							Message msgTmp = handler.obtainMessage(MainActivity.MSG_NET_SUCC);
							msgTmp.arg1 = searchType; // 标识是否沿用上次搜索结果 =-1：是 !=-1:否
							msgTmp.obj = result;
							msgTmp.sendToTarget();

							break;
						} else {
							httpRequest.abort();
							Message msgTmp = handler.obtainMessage(MainActivity.MSG_NET_STATUS_ERROR);
							msgTmp.obj = "HttpStatus error";
							msgTmp.sendToTarget();
						}
					} catch (Exception e) {
						Log.e("DuanZuLog", "网络异常，请检查网络后重试！");
						e.printStackTrace();
					}
					
					count--;
				}
				
				if ( count <= 0 && handler != null){
					Message msgTmp =  handler.obtainMessage(MainActivity.MSG_NET_TIMEOUT);
					msgTmp.sendToTarget();
				}
				
				IsBusy = false;
				
			}
		}.start();

		return true;
	}
	
}
