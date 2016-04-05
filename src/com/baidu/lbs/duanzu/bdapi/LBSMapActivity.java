package com.baidu.lbs.duanzu.bdapi;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.lbs.duanzu.ContentModel;
import com.baidu.lbs.duanzu.DemoApplication;
import com.baidu.lbs.duanzu.R;
import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.MKGeneralListener;
import com.baidu.mapapi.map.ItemizedOverlay;
import com.baidu.mapapi.map.LocationData;
import com.baidu.mapapi.map.MKEvent;
import com.baidu.mapapi.map.MapController;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationOverlay;
import com.baidu.mapapi.map.OverlayItem;
import com.baidu.mapapi.map.PopupClickListener;
import com.baidu.mapapi.map.PopupOverlay;
import com.baidu.platform.comapi.basestruct.GeoPoint;

/**
 * С�����tab��ͼ��
 * 
 * @author Lu.Jian
 * 
 */
public class LBSMapActivity extends Activity {
	private Context context;

	public BMapManager mBMapManager = null;
	public MapView mMapView = null;
	public static final String strKey = "63418012748CD126610D926A0546374D0BFC86D5";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		System.out.println("LBSMapActivity��onCreate");
		super.onCreate(savedInstanceState);
		context = this;

		initEngineManager(this);
		
		setContentView(R.layout.map);
		
		initMapView();

		DemoApplication.getInstance().setMapActivity(this);
	}

	@Override
	// ��������app���˳�֮ǰ����mapadpi��destroy()�����������ظ���ʼ��������ʱ������
	public void onDestroy() {
		// TODO Auto-generated method stub
		if (mBMapManager != null) {
			mBMapManager.destroy();
			mBMapManager = null;
		}
		super.onDestroy();
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}

	@Override
	protected void onPause() {
		mMapView.onPause();
		if (mBMapManager != null) {
			mBMapManager.stop();
		}
		removeAllMarker();
		super.onPause();
	}

	@Override
	protected void onResume() {
		mMapView.onResume();
		if (mBMapManager != null) {
			mBMapManager.start();
		}
		addAllMarker();
		super.onResume();
	}

	public void initEngineManager(Context context) {
		if (mBMapManager == null) {
			mBMapManager = new BMapManager(context);
		}

		if (!mBMapManager.init(strKey, new MyGeneralListener())) {
			Toast.makeText(context, "BMapManager  ��ʼ������!", Toast.LENGTH_LONG)
					.show();
		}
	}

	// �����¼���������������ͨ�������������Ȩ��֤�����
	class MyGeneralListener implements MKGeneralListener {

		@Override
		public void onGetNetworkState(int iError) {
			if (iError == MKEvent.ERROR_NETWORK_CONNECT) {
				Toast.makeText(context, "���������������", Toast.LENGTH_LONG).show();
			} else if (iError == MKEvent.ERROR_NETWORK_DATA) {
				Toast.makeText(context, "������ȷ�ļ���������", Toast.LENGTH_LONG).show();
			}
		}

		@Override
		public void onGetPermissionState(int iError) {
			if (iError == MKEvent.ERROR_PERMISSION_DENIED) {
				// ��ȨKey����
				Toast.makeText(context, "���� DemoApplication.java�ļ�������ȷ����ȨKey��",
						Toast.LENGTH_LONG).show();
			}
		}
	}

	private void initMapView() {

		mMapView = (MapView) findViewById(R.id.bmapView);
		mMapView.setBuiltInZoomControls(true);
		// �����������õ����ſؼ�
		MapController mMapController = mMapView.getController();
		// �õ�mMapView�Ŀ���Ȩ,�����������ƺ�����ƽ�ƺ�����
		GeoPoint point = new GeoPoint((int) (39.915 * 1E6),
				(int) (116.404 * 1E6));
		// �ø����ľ�γ�ȹ���һ��GeoPoint����λ��΢�� (�� * 1E6)
		mMapController.setCenter(point);// ���õ�ͼ���ĵ�
		mMapController.setZoom(13);// ���õ�ͼzoom����

	}

	/**
	 * ɾ�����б��
	 */
	public void removeAllMarker() {
		mMapView.getOverlays().clear();
		mMapView.refresh();

	}

	/**
	 * ������б��
	 */
	public void addAllMarker() {
		DemoApplication app = (DemoApplication) getApplication();
		List<ContentModel> list = app.getList();
		mMapView.getOverlays().clear();
		OverlayIcon ov = new OverlayIcon(null, this);
		for (ContentModel content : list) {
			int latitude = (int) (content.getLatitude() * 1000000);
			int longitude = (int) (content.getLongitude() * 1000000);

			Drawable d = getResources().getDrawable(R.drawable.icon_marka);
			OverlayItem item = new OverlayItem(
					new GeoPoint(latitude, longitude), content.getName(),
					content.getAddr());
			item.setMarker(d);

			ov.addItem(item);
		}
		mMapView.getOverlays().add(ov);
		mMapView.refresh();
		
		//�����λ�ɹ�����ӵ�ǰ�����
		if (app.currlocation != null)
    	{
			MyLocationOverlay myLocationOverlay = new MyLocationOverlay(mMapView);
			LocationData locData = new LocationData();
			//�ֶ���λ��Դ��Ϊ�찲�ţ���ʵ��Ӧ���У���ʹ�ðٶȶ�λSDK��ȡλ����Ϣ��Ҫ��SDK����ʾһ��λ�ã���Ҫ
			//		ʹ�ðٶȾ�γ�����꣨bd09ll��
			locData.latitude = app.currlocation.getLatitude();
			locData.longitude = app.currlocation.getLongitude();
			locData.direction = 2.0f;
			myLocationOverlay.setData(locData);
			mMapView.getOverlays().add(myLocationOverlay);
			mMapView.refresh();
    	}


		// ���������ģ��޶�λʱ�ĵ�ͼ����
		int cLat = 39909230;
		int cLon = 116397428;
		if (app.currlocation == null) {
			mMapView.getController().setCenter(new GeoPoint(cLat, cLon));
		} else if (list != null && list.size() >= 1) {
			ContentModel c = (ContentModel)list.get(0);
			int currLat = (int) (c.getLatitude() * 1000000);
			int currLon = (int) (c.getLongitude() * 1000000);
			mMapView.getController().setCenter(new GeoPoint(currLat, currLon));
		}
	}

	/**
	 * ��ͼ�����������ʾ���
	 *
	 */
	class OverlayIcon extends ItemizedOverlay<OverlayItem> {
		public List<OverlayItem> mGeoList = new ArrayList<OverlayItem>();
		private Context mContext = null;
		PopupOverlay pop = null;

		final List<ContentModel> list = DemoApplication.getInstance().getList();

		private int clickedTapIndex = -1;

		public OverlayIcon(Drawable marker, Context context) {
			super(marker);
			this.mContext = context;
			pop = new PopupOverlay(mMapView, new PopupClickListener() {

				/*
				 * ��ǵĵ����򱻵����ص�
				 * (non-Javadoc)
				 * @see com.baidu.mapapi.map.PopupClickListener#onClickedPopup()
				 */
				@Override
				public void onClickedPopup() {
					String webUrl = list.get(clickedTapIndex).getWebUrl();

					Intent intent = new Intent();
					intent.setAction("android.intent.action.VIEW");
					Uri content_url = Uri.parse(webUrl);
					intent.setData(content_url);
					startActivity(intent);
					
				    //���ðٶ�ͳ�ƽӿ�
				    DemoApplication.getInstance().callStatistics();

				}
			});
			populate();

		}

		/*
		 * ���������ص�
		 * (non-Javadoc)
		 * @see com.baidu.mapapi.map.ItemizedOverlay#onTap(int)
		 */
		protected boolean onTap(int index) {
			if(index >= list.size()){
				//����Լ�λ��marker�������κδ���
				return true;
			}
			clickedTapIndex = index;
			View popview = LayoutInflater.from(mContext).inflate(
					R.layout.marker_pop, null);
			TextView textV = (TextView) popview.findViewById(R.id.text_pop);
			String text = list.get(index).getName();
			textV.setText(text);

			pop.showPopup(convertViewToBitMap(popview), mGeoList.get(index)
					.getPoint(), 28);
			super.onTap(index);
			return false;
		}

		/*
		 * �����������������ص� 
		 */
		public boolean onTap(GeoPoint pt, MapView mapView) {
			if (pop != null) {
				clickedTapIndex = -1;
				pop.hidePop();
			}
			super.onTap(pt, mapView);
			return false;
		}

		@Override
		protected OverlayItem createItem(int i) {
			return mGeoList.get(i);
		}

		@Override
		public int size() {
			return mGeoList.size();
		}

		public void addItem(OverlayItem item) {
			mGeoList.add(item);
			populate();
		}

		public void removeItem(int index) {
			mGeoList.remove(index);
			populate();
		}

		private Bitmap convertViewToBitMap(View v) {
			// ���û�ͼ����
			v.setDrawingCacheEnabled(true);
			// ����������������ǳ���Ҫ�����û�е�������������õ���bitmapΪnull
			v.measure(MeasureSpec.makeMeasureSpec(210, MeasureSpec.EXACTLY),
					MeasureSpec.makeMeasureSpec(120, MeasureSpec.EXACTLY));
			// �������Ҳ�ǳ���Ҫ�����ò��ֵĳߴ��λ��
			v.layout(0, 0, v.getMeasuredWidth() +20, v.getMeasuredHeight());
			// ��û�ͼ�����е�Bitmap
			v.buildDrawingCache();
			return v.getDrawingCache();
		}

	}

}
