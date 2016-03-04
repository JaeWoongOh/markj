package com.markjmind.uni;

import android.view.View;

import com.markjmind.uni.common.Store;
import com.markjmind.uni.mapper.Mapper;
import com.markjmind.uni.mapper.UniMapper;
import com.markjmind.uni.progress.ProgressBuilder;
import com.markjmind.uni.thread.CancelAdapter;
import com.markjmind.uni.thread.LoadEvent;

/**
 * <br>捲土重來<br>
 *
 * @author 오재웅(JaeWoong-Oh)
 * @email markjmind@gmail.com
 * @since 2016-03-04
 */
public class UniTask implements UniInterface{
    public Mapper mapper;
    public Store<?> param;
    public ProgressBuilder progress;

    private UniView uniView;


    public UniTask(){
        uniView = null;
        mapper = new UniMapper();
        param = new Store<>();
        progress = new ProgressBuilder();
    }

    private Uni.UniBuildInterface uniBuildInterface = new Uni.UniBuildInterface() {
        @Override
        public UniInterface getUniInterface() {
            return UniTask.this;
        }
        @Override
        public UniMapper getMapper() {
            return (UniMapper)mapper;
        }
        @Override
        public Class<? extends UniView> getCustomUniView() {
            return null;
        }
        @Override
        public Store<?> getParam() {
            return param;
        }
        @Override
        public ProgressBuilder getProgress() {
            return progress;
        }
        @Override
        public void reset() {
            param.clear();
        }
    };


    @Override
    public void onBind() {

    }

    @Override
    public void onPre() {

    }

    @Override
    public void onLoad(LoadEvent event, CancelAdapter cancelAdapter) throws Exception {

    }

    @Override
    public void onUpdate(Object value, CancelAdapter cancelAdapter) {

    }

    @Override
    public void onPost() {

    }

    @Override
    public void onPostFail(String message, Object arg) {

    }

    @Override
    public void onException(Exception e) {

    }

    @Override
    public void onCancelled(boolean attached) {

    }

    void init(UniView uniView){
        uniView.setUniInterface(this);
        uniView.mapper.reset(uniView, this);
        uniView.init(uniBuildInterface.getUniInterface(), uniBuildInterface.getParam(), uniBuildInterface.getProgress());
        uniView.injectLayout(uniView);

    }

    public View findViewById(int id){
        return uniView.findViewById(id);
    }


}
