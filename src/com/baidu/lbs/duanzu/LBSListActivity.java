package com.baidu.lbs.duanzu;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.baidu.lbs.duanzu.bdapi.LBSCloudSearch;

/**
 * 小猪短租tab列表类
 * @author Lu.Jian
 *
 */
public class LBSListActivity extends ListActivity implements OnScrollListener {

	private ContentAdapter adapter;
	private List<ContentModel> list = new ArrayList<ContentModel>();
	public View loadMoreView;
	public ProgressBar progressBar;
	private int visibleLastIndex = 0;   //最后的可视项索引 
	
	// 搜索结果记录的总条数，采用分页显示一次显示10条数据
	public int totalItem = -1;

	protected void onCreate(Bundle savedInstanceState) {
		System.out.println("LBSListActivity：onCreate");
		
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		loadMoreView = getLayoutInflater().inflate(R.layout.list_item_footer, null);
		progressBar = (ProgressBar)loadMoreView.findViewById(R.id.progressBar);
		
		loadMoreView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				loadMoreData();
				progressBar.setVisibility(View.VISIBLE);
			}
		});
		
		final ListView listView = getListView();
		listView.setItemsCanFocus(false);
//		listView.addFooterView(loadMoreView);
		listView.setOnScrollListener(this);
		
		adapter = new ContentAdapter(LBSListActivity.this, list);
		setListAdapter(adapter);

		DemoApplication app = (DemoApplication) getApplication();
		app.setList(list);
		app.setAdapter(adapter);
		app.setListActivity(this);
		
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();

	}


	/**
	 * 列表item点击回调
	 */
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		View popview = LayoutInflater.from(this).inflate(R.layout.marker_pop, null);
		popview.setDrawingCacheEnabled(true);
		//启用绘图缓存   
		popview.setDrawingCacheEnabled(true);        
        //调用下面这个方法非常重要，如果没有调用这个方法，得到的bitmap为null   
		popview.measure(MeasureSpec.makeMeasureSpec(256, MeasureSpec.EXACTLY),  
                MeasureSpec.makeMeasureSpec(256, MeasureSpec.EXACTLY));  
        //这个方法也非常重要，设置布局的尺寸和位置   
		popview.layout(0, 0, popview.getMeasuredWidth(), popview.getMeasuredHeight());  
        //获得绘图缓存中的Bitmap   
		popview.buildDrawingCache(); 
		
		super.onListItemClick(l, v, position, id);
		
		String webUrl = list.get(position).getWebUrl();
		System.out.println(webUrl);
		
		Intent intent= new Intent();       
	    intent.setAction("android.intent.action.VIEW");   
	    Uri content_url = Uri.parse(webUrl);  
	    intent.setData(content_url); 
	    startActivity(intent);
	    
	    //调用百度统计接口
	    DemoApplication.getInstance().callStatistics();
	}
	
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		int itemsLastIndex = adapter.getCount() - 1; // 数据集最后一项的索引
		int lastIndex = itemsLastIndex + 1;
		if (scrollState == OnScrollListener.SCROLL_STATE_IDLE
				&& visibleLastIndex == lastIndex) {
			// 如果是自动加载,可以在这里放置异步加载数据的代码
			loadMoreData();
			progressBar.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * 当listview或者grid控件已经完成滚动之后被调用
	 * @param view The view whose scroll state is being reported
	 * @param firstVisibleItem The index of the first visible cell (ignore if visibleItemCount == 0)
	 * @param visibleItemCount The number of visible cells
	 * @param totalItemCount The number of items in the list adaptor
	 */
	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		visibleLastIndex = firstVisibleItem + visibleItemCount - 1;

		// 判断当前页是否是为最后一页，如果是就要移除FooterView
		if (totalItemCount == totalItem) {			
			getListView().removeFooterView(loadMoreView);
		}
	}

	public class ContentAdapter extends BaseAdapter {
		private LayoutInflater mInflater;
		private List<ContentModel> items;

		private ViewHolder holder;

		public ContentAdapter(Context context, List<ContentModel> list) {
			mInflater = LayoutInflater.from(context);

			items = list;
		}

		@Override
		public int getCount() {
			System.out.println("getCount:" + items.size());
			return items.size();
		}

		@Override
		public Object getItem(int position) {
			System.out.println("getItem:" + position);
			return items.get(position);
		}

		@Override
		public long getItemId(int position) {
			System.out.println("getItemId:" + position);
			return position;
		}

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {
			System.out.println("getView:" + position);

			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.contentitem, null);
				holder = new ViewHolder();
				
				holder.name = (TextView) convertView.findViewById(R.id.name);
				holder.addr = (TextView) convertView.findViewById(R.id.addr);
				holder.distance = (TextView) convertView.findViewById(R.id.distance);
				holder.price = (TextView) convertView.findViewById(R.id.price);
				holder.icon = (ImageView) convertView.findViewById(R.id.icon);
				holder.leaseType = (TextView) convertView.findViewById(R.id.leaseType);
				
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			// holder.index.setText((String) items.get(position).getIndex());
			holder.addr.setText((String) items.get(position).getAddr() + "");
			holder.name.setText((String) items.get(position).getName() + "");
			holder.distance.setText((String) items.get(position).getDistance() + "");
			holder.price.setText((String) items.get(position).getPrice());
			holder.leaseType.setText((String) items.get(position).getLeaseType());
			holder.icon.setImageBitmap(items.get(position).getBitmap());
			return convertView;
		}

		/* class ViewHolder */
		private class ViewHolder {
			TextView addr;
			TextView name;
			TextView distance;
			TextView price;
			TextView leaseType;
			ImageView icon;
		}
	}

	public Bitmap getBitmapFromUrl(String imgUrl) {
		URL url;
		Bitmap bitmap = null;
		try {
			url = new URL(imgUrl);
			InputStream is = url.openConnection().getInputStream();
			BufferedInputStream bis = new BufferedInputStream(is);
			bitmap = BitmapFactory.decodeStream(bis);
			bis.close();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return bitmap;
	}
	
	/**
	 * 加载更多数据
	 */
	private void loadMoreData() {
		HashMap<String, String> filterParams = DemoApplication.getInstance()
				.getFilterParams();

		// 计算下一页的页索引
		int pageindex = list.size() / 10 + (((list.size() % 10) > 0) ? 1 : 0);
		filterParams.put("page_index", "" + pageindex);

		// search type 为 -1，将保持当前的搜索类型
		LBSCloudSearch.request(-1, filterParams, DemoApplication.getInstance()
				.getHandler(), DemoApplication.networkType);
	}

}
