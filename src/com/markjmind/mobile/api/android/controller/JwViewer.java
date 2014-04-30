package com.markjmind.mobile.api.android.controller;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationSet;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.markjmind.mobile.api.hub.Store;


/**
 * start : 2013.11.17<br>
 * <br>
 * Base Viewer 클래스<br>
 * 일반 Viewer을 정의하는 클래스는<br>
 * BaseLayout 를 상속하여 view_init메소드에 화면을 정의한다. 
 * 
 * @author 오재웅
 * @phone 010-2898-7850
 * @email markjmind@gmail.com
 * @version 2013.11.17
 */

public class JwViewer {
	
	private static Store<Refresh> asyncTaskPool = new Store<Refresh>();
	private static ViewerXmlMapper vxm;
	private Context context;
	private Activity activity;
	private Dialog dialog;
	private View viewer;
	private ViewGroup parentView;
	private OnClickListenerReceiver oclReceiver;
	private String id;
	private int layoutId;
	private Store<Object> loadingParam;
	private Store<Object> ViewerParam;
	
	public static final int INSTANCE_NEW=0;
	public static final int INSTANCE_SAVE=2;
	
	public TYPE_MODE mode;
	enum TYPE_MODE{
		ACTIVITY,
		DIALOG
	}
	
	public static Store<Object> asyncStore = new Store<Object>();
	private boolean preload = false;
	
	/**
	 * 기본생성자
	 */
	protected JwViewer(){
		ViewerParam = new Store<Object>();
	}
	
	/**
	 * 네트워크등 Thread 처리 관련 내용을 정의한다.
	 * @return
	 */
	public boolean loading(){
		return true;
	}
	
	/**
	 * loading() 리턴값이 true 일때 호출된다.<br>
	 * Viewer를 재정의해야할 함수<br>
	 * Viewer에 데이터값이나 셋팅하거나 view표현되어야할 값들 셋팅한다.
	 */
	public void view_init(){
		
	}
	
	/**
	 * loading() 리턴값이 false일때 호출된다.<br>
	 * 로딩이 실패했을 경우 표현해야할 화면을 정의한다.
	 */
	public void view_fail(){
		
	}
	
	/**
	 * Viewer를 초기화 한
	 * @param activity 가 속해있는 activity
	 * @param layout_id Viewer의 Layout ID
	 */
	public void init(Activity activity,int layout_id){
		setLayoutId(layout_id);
		mode = TYPE_MODE.ACTIVITY;
		if(!activity.equals(this.activity)){
			this.activity = activity;
			this.context = activity;
		}
	}
	/**
	 * Viewer를 초기화 한다.
	 * @param dialog Viewer가 속해있는 dialog
	 * @param layout_id Viewer의 Layout ID
	 */
	public void init(Dialog dialog,int layout_id){
		setLayoutId(layout_id);
		mode = TYPE_MODE.DIALOG;
		if(!dialog.equals(this.dialog)){
			this.dialog = dialog;
			this.context = dialog.getContext();
		}
	}
	
	
/********************************** Activity, Dialog 분별 ***************************************************/
	/**
	 * context가 Activity인지 Dialog인지 분별하여<br>
	 * 해당뷰어를 자신과 같은 context로 init한다.
	 * @param viewer
	 * @param R_layout_id
	 */
	private void initToContext(JwViewer viewer, int R_layout_id){
		if(mode==TYPE_MODE.DIALOG){
			viewer.init(dialog, R_layout_id);
		}else{
			viewer.init(activity, R_layout_id);
		}
	}
	
	/**
	 * context가 Activity인지 Dialog인지 분별하여<br>
	 * ID에 해당하는 Parents를 GroupView를 리턴
	 * @param R_id_parents
	 * @return
	 */
	private ViewGroup getParentsToContext(int R_id_parents){
		ViewGroup parents;
		if(mode==TYPE_MODE.DIALOG){
			parents = (ViewGroup)Jwc.getView(R_id_parents, dialog);
		}else{
			parents = (ViewGroup)Jwc.getView(R_id_parents, activity);
		}
		return parents;
	}
	
	/**
	 * context가 Activity인지 Dialog인지 분별하여<br>
	 * ID에 해당하는 View를 리턴
	 * @param R_id_view
	 * @return
	 */
	private View getViewToContext(int R_id_view){
		if(mode.equals(TYPE_MODE.ACTIVITY)){
			return activity.findViewById(R_id_view);
		}else if(mode.equals(TYPE_MODE.DIALOG)){
			return dialog.findViewById(R_id_view);
		}else{
			return viewer.findViewById(R_id_view);
		}
	}
/*************************************************************************************/
	/**
	 * Viewer가 속한 Activity를 가져온다.
	 * @return Activity
	 */
	public Activity getActivity(){
		return activity;
	}
	
	/**
	 * Viewer가 속한 Dialog를 가져온다.
	 * @return Dialog
	 */
	public Dialog getDialog(){
		return dialog;
	}
	
	/**
	 * 다른 Viewer로 넘기는 파라미터를 설정한다.
	 * @param key Parameter Key
	 * @param value Parameter Value
	 */
	public JwViewer addParam(String key, Object value){
		ViewerParam.add(key, value);
		return this;
	}
	
	/**
	 * 다른 Viewer로 전달한 파라미터를 받는다.
	 * @param key Parameter Key
	 * @return Parameter Value
	 */
	public Object getParam(String key){
		return ViewerParam.get(key);
	}
	
	/**
	 * 다른 Viewer로 전달한 파라미터를 받는다.<br>
	 * key에 대응하는 value가 없으면 defalut값을 리턴한다.
	 * @param key Parameter Key
	 * @return Parameter Value
	 */
	public Object optParam(String key, Object defalut){
		Object result = getParam(key);
		if(result==null)
			result = defalut;
		return result;
	}
	
	/**
	 * 다른 Viewer로 전달한 파라미터 Store를 받는다.
	 * @return Parameter store
	 */
	public Store<Object> getParamStore(){
		return ViewerParam;
	}
	
	/**
	 * Loading에서 설정한 Key값에 대응하는 파라미터를 설정한다.
	 * @param key Parameter Key
	 * @param value Parameter value
	 */
	public void setLoadingParam(String key, Object value){
		loadingParam.add(key, value);
	}
	
	/**
	 * Loading에서 설정한 Key값에 대응하는 파라미터를 가져온다.
	 * @param key Parameter Key
	 * @return Parameter value
	 */
	public Object getLoadParam(String key){
		if(loadingParam==null){
			return null;
		}
		return loadingParam.get(key);
	}
	
	/**
	 * Loading에서 설정한 Key값에 대응하는 파라미터를 가져온다.<br>
	 * 값이 없을때 defalut값을 리턴한다.
	 * @param key Parameter Key
	 * @param defalut defalut Parameter value
	 * @return Parameter value
	 */
	public Object optLoadParam(String key, Object defalut){
		Object result = getLoadParam(key);
		if(result==null)
			result = defalut;
		return result;
	}
	
	/**
	 * Loading에서 설정한 Key값에 대응하는 파라미터를 가져온다.<br>
	 * 값이 없을때 defalut값을 리턴한다.
	 * @param key Parameter Key
	 * @param defalut defalut Parameter value
	 * @return Parameter value
	 */
	public Store<Object> getLoadParamStore(){
		return loadingParam;
	}
	
	/**
	 * Activity에 LinearLayout을 ContentView로 등록한다. 
	 * @param activity ContentView를 등록할 Activity
	 * @return 등록된 LinearLayout
	 */
	public static LinearLayout setContentLinear(Activity activity){
		LinearLayout layout = new LinearLayout(activity);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT);
		layout.setLayoutParams(params);
		activity.setContentView(layout);
		return layout;
	}
	
	/**
	 * Activity에 FrameLayout을 ContentView로 등록한다. 
	 * @param activity ContentView를 등록할 Activity
	 * @return 등록된 FrameLayout
	 */
	public static FrameLayout setContentFrame(Activity activity){
		FrameLayout layout = new FrameLayout(activity);
		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,FrameLayout.LayoutParams.MATCH_PARENT);
		layout.setLayoutParams(params);
		activity.setContentView(layout);
		return layout;
	}
	
	/**
	 * Viewer의 Layout을 초기화한다.
	 */
	public void viewerInit(){
		viewer = (ViewGroup)Jwc.getViewInfalter(layoutId,context);
	}
	
	/**
	 * 매핑을 위해 XML을 로드한다.
	 * @param R_raw_xml 로드할 xml id
	 * @param app Application
	 */
	public static void xmlLoad(int R_raw_xml, Application app){
		if(vxm == null){
			vxm = new ViewerXmlMapper(app);
			vxm.load(R_raw_xml);
		}
	}
	
	/**
	 * Viewer를 등록한다.
	 * @param id Viewer ID name
	 * @param R_layout_id layout ID
	 * @param viewerClass Viewer Class
	 */
	public static void addViewer(String id, int R_layout_id, Class viewerClass){
		JwViewer.addViewer(id, R_layout_id, "",  viewerClass);
	}
	/**
	 * Viewer를 등록한다.
	 * @param id Viewer ID name
	 * @param R_layout_id  layout ID
	 * @param desc 설명
	 * @param viewerClass Viewer Class
	 */
	public static void addViewer(String id, int R_layout_id, String desc, Class viewerClass){
		vxm.add(id, R_layout_id, desc, null, viewerClass);
	}
	/**
	 * Viewer를 등록한다. 
	 * @param id id Viewer ID name
	 * @param jwViewer JwViewer 객체
	 */
	public static void addViewer(String id, JwViewer jwViewer){
		jwViewer.setId(id);
		vxm.add(id, jwViewer.getLayoutId(), "", jwViewer, jwViewer.getClass());
	}

	/**
	 * 현재 Viewer와 같은 context로 jwViewerClass의<br>
	 * 새로은 instance를 생성하여 Viewer를 만들고 리턴해준다.
	 * @param R_layout_id 새로만들 Viewer의 layout ID
	 * @param jwViewerClass 새로만들 Viewer 클래스
	 * @return 해당 Viewer
	 */
	public JwViewer getViewer(int R_layout_id, Class<?> jwViewerClass){
		try {
			JwViewer viewer = (JwViewer)jwViewerClass.newInstance();
			initToContext(viewer, R_layout_id);
			return viewer;
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * Viewer를 정의한 Xml의 id에 해당하는 Viewer를 반환한다.
	 * @param id Viewer ID
	 * @param activity activity
	 * @return Viewer
	 */
	public static JwViewer getViewer(int R_layout_id, Class<?> jwViewerClass, Activity activity){
		try {
			JwViewer viewer = (JwViewer)jwViewerClass.newInstance();
			viewer.init(activity, R_layout_id);
			return viewer;
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Viewer를 정의한 Xml의 id에 해당하는 Viewer를 반환한다.
	 * @param id Viewer ID
	 * @param dialog dialog
	 * @return Viewer
	 */
	public static JwViewer getViewer(int R_layout_id, Class jwViewerClass, Dialog dialog){
		try {
			JwViewer viewer = (JwViewer)jwViewerClass.newInstance();
			viewer.init(dialog, R_layout_id);
			return viewer;
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Viewer를 정의한 Xml의 id에 해당하는 Viewer를 반환한다.
	 * @param id Viewer ID
	 * @param activity activity
	 * @return Viewer
	 */
	public static JwViewer getViewer(String id, Activity activity){
		return vxm.getViewer(id, activity);
	}
	/**
	 * Viewer를 정의한 Xml의 id에 해당하는 Viewer를 반환한다.
	 * @param id Viewer ID
	 * @param dialog dialog
	 * @return Viewer
	 */
	public static JwViewer getViewer(String id, Dialog dialog){
		return vxm.getViewer(id, dialog);
	}
	
	
	
	
//*************************************************** binder *********************************************//
	
	public final static String TASK_ACV="acv";
	public final static String TASK_APUT="aput";
	private String task;
	private boolean async=true;

	public JwViewer setPreLoad(boolean isPreload){
		this.preload = isPreload;
		return this;
	}
	
	public String getTaskName(){
		return task;
	}
	
	public JwViewer setAsync(boolean isAsync){
		this.async = isAsync;
		return this;
	}
	
	/**
	 * index에 해당하는 부모 ViewGroup아래 JwViewer를 추가한다.
	 * @param parents 부모 ViewGroup
	 * @param index index
	 * @return
	 */
	public JwViewer add(ViewGroup parents, int index) {
		parentView = parents;
		if(async){
			excute(TASK_APUT);
		}else{
			cancelTaskAcv();
			viewerInit();
			if(index==-1){
				parents.addView(viewer,  parents.getLayoutParams());
			}else{
				parents.addView(viewer,index,parents.getLayoutParams());
			}
			view_init();
		}
		return this;
	}
	
	/**
	 * index에 해당하는 부모 ViewGroup아래 JwViewer를 추가한다.
	 * @param parents 부모 ViewGroup ID
	 * @param index index
	 * @return
	 */
	public JwViewer add(int R_id_parents, int index) {
		ViewGroup parents = getParentsToContext(R_id_parents);
		return add(parents, index);
	}
	
	/**
	 * 부모 ViewGroup아래 JwViewer를 추가한다.
	 * @param parents 부모 ViewGroup
	 * @return
	 */
	public JwViewer add(ViewGroup parents) {
		return add(parents,-1);
	}
	
	/**
	 * 부모 ViewGroup아래 JwViewer를 추가한다.
	 * @param R_id_parents 부모 ViewGroup ID
	 * @return
	 */
	public JwViewer add(int R_id_parents) {
		ViewGroup parents = getParentsToContext(R_id_parents);
		return add(parents);
	}
	
	
	
	/**
	 * 부모 ViewGroup 아래의 View들을 모두 지우고<br>
	 * 현재 Viewer로 변경한다.
	 * @param parents 부모 ViewGroup
	 * @return
	 */
	public JwViewer change(ViewGroup parents){
		parentView = parents;
		if(async){
			excute(TASK_ACV);
		}else{
			cancelTaskAcv();
			viewerInit();
			View view = Jwc.changeLayout(viewer, parents);
			view_init();
		}
		return this;
	}
	
	/**
	 * 부모 ViewGroup 아래의 View들을 모두 지우고<br>
	 * 현재 Viewer로 변경한다.
	 * @param parents 부모 ViewGroup ID
	 * @return
	 */
	public JwViewer change(int R_id_parents){
		ViewGroup parents = getParentsToContext(R_id_parents);
		return change(parents);
	}

	//	/**
//	 * 부모 ViewGroup 아래의 View들을 모두 지우고<br>
//	 * 현재 Viewer로 변경한다.
//	 * @param parents 부모 ViewGroup
//	 * @return 변경된 JwViewer
//	 */
//	public JwViewer cv(ViewGroup parents) {
//		cancelTaskAcv();
//		viewerInit();
//		View view = Jwc.changeLayout(viewer, parents);
//		parentView = parents;
//		view_init();
//		return this;
//	}
//	
//	/**
//	 * 부모 ViewGroup 아래의 View들을 모두 지우고
//	 * @param R_id_parents 부모 ViewGroup ID
//	 * @return JwViewer
//	 */
//	public JwViewer cv(int R_id_parents) {
//		ViewGroup parents = getParentsToContext(R_id_parents);
//		return cv(parents);
//	}
//	/**
//	 * 
//	 * @param R_id_parents
//	 */
//	public JwViewer acv(int R_id_parents){
//		acv(R_id_parents,false);
//		return this;
//	}
//	/**
//	 * 
//	 * @param parentsView
//	 */
//	public JwViewer acv(ViewGroup parentsView){
//		return acv(parentsView,false);
//	}
//	/**
//	 * 
//	 * @param R_id_parents
//	 * @param preload
//	 * @return
//	 */
//	public JwViewer acv(int R_id_parents, boolean preload) {
//		ViewGroup parents = getParentsToContext(R_id_parents);
//		return acv(parents,preload);
//	}
//	/**
//	 * 
//	 * @param parents
//	 * @param preload
//	 * @return
//	 */
//	public JwViewer acv(ViewGroup parents, boolean preload) {
//		parentView = parents;
//		this.preload = preload;
//		excute(TASK_ACV);
//		return this;
//	}
	
	public String getTaskKey(String command){
		String taskKey = command+"_"+parentView.hashCode();
		return taskKey;
	}
	
	public static void cancelTask(String taskKey){
		Refresh ref = asyncTaskPool.get(taskKey);
		if(ref!=null){
			ref.stop();
		}
	}
	
	public static void cancelTaskAcv(View parents){
		String taskKey = TASK_ACV+"_"+parents.hashCode();
		cancelTask(taskKey);
	}
	public static void cancelTaskAput(View parents){
		String taskKey = TASK_APUT+"_"+parents.hashCode();
		cancelTask(taskKey);
	}
	
	
	public void cancelTaskAcv(){
		String taskKey = getTaskKey(TASK_ACV);
		cancelTask(taskKey);
	}
	
	public void cancelTaskAput(){
		String taskKey = getTaskKey(TASK_APUT);
		cancelTask(taskKey);
	}
	
	
	@SuppressLint("NewApi") 
	private void excute(String command){
		this.task = command;
		String taskKey = getTaskKey(command);
		cancelTask(taskKey);
		Refresh ref = new Refresh(taskKey);
		asyncTaskPool.add(taskKey, ref);
		
		if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ) {
			ref.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		} else {
			ref.execute();
		}
	}

	public void view_pre(){
		if(parentView!=null){
			if(TASK_ACV.equals(task)){
				parentView.removeAllViews();
			}
		}
	}
	public void onProgressUpdate(Integer... values){
	}
	
	private  class Refresh extends AsyncTask<Object, Integer, Object>{
		private String taskKey;
		private boolean isStop;
		
		public Refresh(String taskKey){
			this.taskKey = taskKey;
			this.isStop = false;
		}
		
		public void stop(){
			cancel(true);
			isStop = true;
			asyncTaskPool.remove(taskKey);
			Log.w("JwViewer","Stop AsyncTask : "+taskKey);
		}
		
		@Override
		protected void onPreExecute() {
			if(preload){
				viewerInit();
				parentView.removeAllViews();
				parentView.addView(viewer,parentView.getLayoutParams());	
			}else{
				JwViewer.this.view_pre();
			}
		}
		@Override
		protected void onCancelled() {
			super.onCancelled();
		}
		@Override
		protected void onProgressUpdate(Integer... values) {
			JwViewer.this.onProgressUpdate(values);
		}
		@Override
		protected Object doInBackground(Object... params) {
			loadingParam = new Store<Object>();
			loadingParam.clear();
			return loading(); //데이터 가져오기
		}
		@Override
		protected void onPostExecute(Object result) {
			if (isStop) {
				return;
			}
			boolean isView = (Boolean)result;
			if(isView){
				if(!preload){
					viewerInit();
				}
				if(TASK_ACV.equals(task)){
					parentView.removeAllViews();
					parentView.addView(viewer,parentView.getLayoutParams());	
				}else{
					parentView.addView(viewer,parentView.getLayoutParams());
				}
				JwMemberMapper.injectField(JwViewer.this);
				view_init();
			}else{
				view_fail();
			}
			asyncTaskPool.remove(taskKey);
		}
	}
	
	public void setPreView(int R_layout_id){
		if(parentView!=null){
			parentView.removeAllViews();
		}
		ViewGroup loading = (ViewGroup) Jwc.getViewInfalter( R_layout_id, getParent().getContext());
		loading.setLayoutParams(getParent().getLayoutParams());
		((ViewGroup)getParent()).addView(loading);
	}
//*************************************************** changeViewer *********************************************//
	
	
	/**
	 * Viewer ID를 가져온다.
	 * @return Viewer ID
	 */
	public String getId(){
		return id;
	}
	
	/**
	 * Viewer ID를 설정한다.
	 * @param Viewer ID
	 */
	public void setId(String id){
		this.id =id;
	}
	
	/**
	 * Layout ID를 가져온다.
	 * @return Layout ID
	 */
	public int getLayoutId(){
		return layoutId;
	}
	
	/**
	 * Layout ID를 가져온다.
	 * @param Layout ID
	 */
	public void setLayoutId(int layoutId){
		this.layoutId = layoutId;
	}
	
	/**
	 * Context 객체를 반환한다.
	 * @return Context
	 */
	public Context getContext(){
		return context;
	}
	/**
	 * viewer의 Context 객체를 반환한다.
	 * @return Context
	 */
	public Context getViewerContext(){
		return viewer.getContext();
	}
	
	/**
	 * Resources를 반환한다.
	 * @return Resources
	 */
	public Resources getResources(){
		return context.getResources();
	}
	
	
	/**
	 * id에 해당하는 뷰에 이벤트를  등록한다.
	 * @param methodName 메소드명
	 * @param R_id_view 이벤트를 등록할 id
	 */
	public void setOnClickListener(String methodName, int R_id_view){
//		if(oclReceiver==null){
			oclReceiver = new OnClickListenerReceiver(context);
//		}
		oclReceiver.setOnClickListener(this, methodName, R_id_view, viewer);
	}
	/**
	 * view에 해당하는 이벤트를  등록한다.
	 * @param methodName 메소드명
	 * @param view view 
	 */
	public void setOnClickListener(String methodName, View view){
//		if(oclReceiver==null){
			oclReceiver = new OnClickListenerReceiver(context);
//		}
		oclReceiver.setOnClickListener(this, methodName, view);
	}
	
	/**
	 * parents의 child view중 tag에 해당하는 뷰에 이벤트를  등록한다.
	 * @param methodName 메소드명
	 * @param tag 이벤트를 등록할 tag
	 * @param parents tag를 찾을 parents view 
	 */
	public void setOnClickListener(String methodName, String tag, View parents){
		setOnClickListener(methodName,getViewTag(tag, parents));
	}
	
	
	
	/**
	 * tag에 해당하는 뷰에 이벤트를  등록한다.
	 * @param methodName 메소드명
	 * @param tag 이벤트를 등록할 tag
	 */
	public void setOnClickListener(String methodName, String tag){
		setOnClickListener(methodName, tag, viewer);
	}
	
	/**
	 * tag에 해당하는 뷰에 이벤트를  등록한다.
	 * @param methodName 메소드명
	 * @param tag 이벤트를 등록할 tag
	 */
	public void setOnClickParamListener(String methodName, String tag, Object param){
		setOnClickParamListener(methodName, tag, viewer,param);
	}
	
	/**
	 * id에 해당하는 뷰에 이벤트를  등록한다.
	 * @param methodName 메소드명
	 * @param R_id_view 이벤트를 등록할 id
	 */
	public void setOnClickParamListener(String methodName, int R_id_view, Object param){
//		if(oclReceiver==null){
			oclReceiver = new OnClickListenerReceiver(context);
//		}
		oclReceiver.setOnClickParamListener(this, methodName, R_id_view, viewer,param);
	}
	/**
	 * parents의 child view중 tag에 해당하는 뷰에 이벤트를  등록한다.
	 * @param methodName 메소드명
	 * @param tag 이벤트를 등록할 tag
	 * @param parents tag를 찾을 parents view 
	 */
	public void setOnClickParamListener(String methodName, View view, Object param){
//		if(oclReceiver==null){
			oclReceiver = new OnClickListenerReceiver(context);
//		}
		oclReceiver.setOnClickParamListener(this, methodName, view,param);
	}
	/**
	 * parents의 child view중 tag에 해당하는 뷰에 이벤트를  등록한다.
	 * @param methodName 메소드명
	 * @param tag 이벤트를 등록할 tag
	 * @param parents tag를 찾을 parents view 
	 */
	public void setOnClickParamListener(String methodName, String tag, View parents, Object param){
		setOnClickParamListener(methodName, getViewTag(tag, parents),param);
	}
	

	
	/**
	 * tag명과 동일한 메소드를 찾아 해당하는 뷰에 이벤트를  등록한다.
	 * @param methodName 메소드명
	 * @param tag 이벤트를 등록할 tag
	 */
	public void setOnClickListenerByTag(String tag){
		setOnClickListener(tag, tag, viewer);
	}
	
	/**
	 * 부모의 view를  반환한다.
	 * @return 부모 View
	 */
	public ViewGroup getParent(){
		return this.parentView;
	}
	
	/**
	 * Viewer에해당하는 Layout
	 * @return Viewer에해당하는 Layout
	 */
	public ViewGroup getLayout(){
		return (ViewGroup)viewer;
	}
	
	/**
	 * activity안에 있는 View를 찾는다
	 * @param R_id_view 찾을 View ID
	 * @return View 해당 View
	 */
	public View findViewById(int R_id_view){
		return getViewToContext(R_id_view);
		
	}
	
	/**
	 * 현재 viewer에 속해있는 View를 찾는다
	 * @param R_id_view 찾을 View ID
	 * @return View 해당 View
	 */
	public View getView(int R_id_view){
		return Jwc.getView(R_id_view, viewer);
	}
	/**
	 * parents에 속해있는 View를 찾는다
	 * @param R_id_view 찾을 View ID
	 * @return View  해당 View
	 */
	public View getView(int R_id_view,View parents){
		return Jwc.getView(R_id_view, parents);
	}
	
	/**
	 * 현재 viewer에 속해있는 View를 tag로 찾는다
	 * @param tag 찾을 tag
	 * @param parents parnets View
	 * @return View 해당 View
	 */
	public View getViewTag(String tag){
		return getViewTag(tag, viewer);
	}
	
	/**
	 * parnets에 속해있는 View를 tag로 찾는다
	 * @param tag 찾을 tag
	 * @param parents parnets View
	 * @return View 해당 View
	 */
	public View getViewTag(String tag,View parents){
		return Jwc.getViewTag(tag, parents);
	}
	
	/**
	 * ID에 해당하는 Button을 리턴함
	 * @param R_id_view button ID
	 * @return Button 해당 Button
	 */
	public Button Button(int R_id_view){
		return (Button)getView(R_id_view);
	}
	/**
	 * 해당 parents 아래 ID해당하는 Button을 리턴함
	 * @param R_id_view button ID
	 * @param parents parnets View
	 * @return 해당 Button
	 */
	public Button Button(int R_id_view,View parents){
		return (Button)getView(R_id_view,parents);
	}
	/**
	 * ID에 해당하는 TextView을 리턴함
	 * @param R_id_view TextView ID
	 * @return TextView
	 */
	public TextView TextView(int R_id_view){
		return (TextView)getView(R_id_view);
	}
	/**
	 * 해당 parents 아래 ID해당하는 TextView를 리턴함
	 * @param R_id_view TextView ID
	 * @param parents parnets View
	 * @returnTextView
	 */
	public TextView TextView(int R_id_view,View parents){
		return (TextView)getView(R_id_view,parents);
	}
	
	/**
	 * Viewer를 visible한다.
	 */
	public void visible(){
		viewer.setVisibility(View.VISIBLE);
	}
	
	/**
	 * Viewer를 invisible한다.
	 */
	public void inVisible(){
		viewer.setVisibility(View.INVISIBLE);
	}
	
	/**
	 * Viewer를 gone한다.
	 */
	public void gone(){
		viewer.setVisibility(View.GONE);
	}
	
	/**
	 * Viewer layout을 삭제한다.
	 */
	public void removeLayout(){
		if(parentView!=null){
			if(parentView instanceof ViewGroup){
				((ViewGroup)parentView).removeView(this.getLayout());
			}
		}
	}
	
	AnimationSet outAnimation;
	public void setAnimation(AnimationSet outAnimation){
		this.outAnimation = outAnimation;
	}
}
