/*
 * Copyright (C) 2014 pengjianbo(pengjianbosoft@gmail.com), Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package cn.finalteam.galleryfinal;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import cn.finalteam.galleryfinal.adapter.FolderListAdapter;
import cn.finalteam.galleryfinal.adapter.PhotoListAdapter;
import cn.finalteam.galleryfinal.model.PhotoFolderInfo;
import cn.finalteam.galleryfinal.model.PhotoInfo;
import cn.finalteam.galleryfinal.utils.DeviceUtils;
import cn.finalteam.galleryfinal.utils.PhotoTools;
import cn.finalteam.galleryfinal.widget.FloatingActionButton;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Desction:图片选择器
 * Author:pengjianbo
 * Date:15/10/10 下午3:54
 */
public class PhotoSelectActivity extends PhotoBaseActivity implements View.OnClickListener, AdapterView.OnItemClickListener{

    private final int HANLDER_TAKE_PHOTO_EVENT = 1000;
    private final int HANDLER_REFRESH_LIST_EVENT = 1002;

    private GridView mGvPhotoList;
    private ListView mLvFolderList;
    private LinearLayout mLlFolderPanel;
    private ImageView mIvTakePhoto;
    private ImageView mIvBack;
    private ImageView mIvClear;
    private TextView mTvChooseCount;
    private TextView mTvSubTitle;
    private LinearLayout mLlTitle;
    private FloatingActionButton mFabOk;
    private TextView mTvEmptyView;

    private List<PhotoFolderInfo> mAllPhotoFolderList;
    private FolderListAdapter mFolderListAdapter;

    private List<PhotoInfo> mCurPhotoList;
    private PhotoListAdapter mPhotoListAdapter;

    private GalleryConfig mGalleryConfig;

    //是否需要刷新相册
    private boolean mHasRefreshGallery = false;
    private HashMap<String, PhotoInfo> mSelectPhotoMap = new HashMap<String, PhotoInfo>();

    private Handler mHanlder = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if ( msg.what == HANLDER_TAKE_PHOTO_EVENT ) {
                PhotoInfo photoInfo = (PhotoInfo) msg.obj;
                takeRefreshGallery(photoInfo);
                refreshSelectCount();
            } else if ( msg.what == HANDLER_REFRESH_LIST_EVENT ){
                refreshSelectCount();
                mPhotoListAdapter.notifyDataSetChanged();
                mFolderListAdapter.notifyDataSetChanged();
                if (mAllPhotoFolderList.get(0).getPhotoList() == null ||
                        mAllPhotoFolderList.get(0).getPhotoList().size() == 0) {
                    mTvEmptyView.setText(R.string.no_photo);
                }

                mGvPhotoList.setEnabled(true);
                mLlTitle.setEnabled(true);
                mIvTakePhoto.setEnabled(true);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gf_activity_photo_select);

        mGalleryConfig = GalleryFinal.getGalleryConfig();

        mPhotoTargetFolder = null;

        findViews();
        setListener();

        mAllPhotoFolderList = new ArrayList<PhotoFolderInfo>();
        mFolderListAdapter = new FolderListAdapter(this, mAllPhotoFolderList, mGalleryConfig);
        mLvFolderList.setAdapter(mFolderListAdapter);

        mCurPhotoList = new ArrayList<PhotoInfo>();
        mPhotoListAdapter = new PhotoListAdapter(this, mCurPhotoList, mSelectPhotoMap, mScreenWidth, mGalleryConfig);
        mGvPhotoList.setAdapter(mPhotoListAdapter);

        if ( mGalleryConfig.isMutiSelect() ) {
            mTvChooseCount.setVisibility(View.VISIBLE);
            mFabOk.setVisibility(View.VISIBLE);
        }

        mIvBack.setBackgroundDrawable(getTitleStateListDrawable());
        //mIvTakePhoto.setBackgroundDrawable(getTitleStateListDrawable());
        //mIvClear.setBackgroundDrawable(getTitleStateListDrawable());
        mGvPhotoList.setEmptyView(mTvEmptyView);

        if (mGalleryConfig.isShowCamera()) {
            mIvTakePhoto.setVisibility(View.VISIBLE);
        } else {
            mIvTakePhoto.setVisibility(View.GONE);
        }

        refreshSelectCount();
        getPhotos();
    }

    private void findViews() {
        mGvPhotoList = (GridView) findViewById(R.id.gv_photo_list);
        mLvFolderList = (ListView) findViewById(R.id.lv_folder_list);
        mTvSubTitle = (TextView) findViewById(R.id.tv_sub_title);
        mLlFolderPanel = (LinearLayout) findViewById(R.id.ll_folder_panel);
        mIvTakePhoto = (ImageView) findViewById(R.id.iv_take_photo);
        mTvChooseCount = (TextView) findViewById(R.id.tv_choose_count);
        mIvBack = (ImageView) findViewById(R.id.iv_back);
        mFabOk = (FloatingActionButton) findViewById(R.id.fab_ok);
        mTvEmptyView = (TextView) findViewById(R.id.tv_empty_view);
        mLlTitle = (LinearLayout) findViewById(R.id.ll_title);
        mIvClear = (ImageView) findViewById(R.id.iv_clear);
    }

    private void setListener() {
        mLlTitle.setOnClickListener(this);
        mIvTakePhoto.setOnClickListener(this);
        mIvBack.setOnClickListener(this);

        mLvFolderList.setOnItemClickListener(this);
        mGvPhotoList.setOnItemClickListener(this);
        mFabOk.setOnClickListener(this);
        mIvClear.setOnClickListener(this);
    }

    protected void deleteSelect(int photoId) {
        try {
            Iterator<Map.Entry<String, PhotoInfo>> entries = mSelectPhotoMap.entrySet().iterator();
            while (entries.hasNext()) {
                Map.Entry<String, PhotoInfo> entry = entries.next();
                if (entry.getValue() != null && entry.getValue().getPhotoId() == photoId) {
                    entries.remove();
                }
            }
        } catch (Exception e){}

        refreshAdapter();
    }

    private void refreshAdapter() {
        mHanlder.sendEmptyMessageDelayed(HANDLER_REFRESH_LIST_EVENT, 100);
    }

    protected void takeRefreshGallery(PhotoInfo photoInfo, boolean selected) {
        if (isFinishing() || photoInfo == null) {
            return;
        }

        Message message = mHanlder.obtainMessage();
        message.obj = photoInfo;
        message.what = HANLDER_TAKE_PHOTO_EVENT;
        mSelectPhotoMap.put(photoInfo.getPhotoPath(), photoInfo);
        mHanlder.sendMessageDelayed(message, 100);
    }

    /**
     * 解决在5.0手机上刷新Gallery问题，从startActivityForResult回到Activity把数据添加到集合中然后理解跳转到下一个页面，
     * adapter的getCount与list.size不一致，所以我这里用了延迟刷新数据
     * @param photoInfo
     */
    private void takeRefreshGallery(PhotoInfo photoInfo) {
        mCurPhotoList.add(0, photoInfo);
        mPhotoListAdapter.notifyDataSetChanged();

        //添加到集合中
        List<PhotoInfo> photoInfoList = mAllPhotoFolderList.get(0).getPhotoList();
        if (photoInfoList == null) {
            photoInfoList = new ArrayList<PhotoInfo>();
        }
        photoInfoList.add(0, photoInfo);
        mAllPhotoFolderList.get(0).setPhotoList(photoInfoList);

        if ( mFolderListAdapter.getSelectFolder() != null ) {
            PhotoFolderInfo photoFolderInfo = mFolderListAdapter.getSelectFolder();
            List<PhotoInfo> list = photoFolderInfo.getPhotoList();
            if ( list == null ) {
                list = new ArrayList<PhotoInfo>();
            }
            list.add(0, photoInfo);
            if ( list.size() == 1 ) {
                photoFolderInfo.setCoverPhoto(photoInfo);
            }
            mFolderListAdapter.getSelectFolder().setPhotoList(list);
        } else {
            String folderA = new File(photoInfo.getPhotoPath()).getParent();
            for (int i = 1; i < mAllPhotoFolderList.size(); i++) {
                PhotoFolderInfo folderInfo = mAllPhotoFolderList.get(i);
                String folderB = null;
                if (photoInfo.getPhotoPath()!="") {
                    folderB = new File(photoInfo.getPhotoPath()).getParent();
                }
                if (TextUtils.equals(folderA, folderB)) {
                    List<PhotoInfo> list = folderInfo.getPhotoList();
                    if (list == null) {
                        list = new ArrayList<PhotoInfo>();
                    }
                    list.add(0, photoInfo);
                    folderInfo.setPhotoList(list);
                    if ( list.size() == 1 ) {
                        folderInfo.setCoverPhoto(photoInfo);
                    }
                }
            }
        }

        mFolderListAdapter.notifyDataSetChanged();
    }

    @Override
    protected void takeResult(PhotoInfo photoInfo) {

        Message message = mHanlder.obtainMessage();
        message.obj = photoInfo;
        message.what = HANLDER_TAKE_PHOTO_EVENT;

        if ( !mGalleryConfig.isMutiSelect() ) { //单选
            if ( mGalleryConfig.isEditPhoto() ) {//裁剪
                mHasRefreshGallery = true;
                toPhotoEdit();
            } else {
                ArrayList<PhotoInfo> list = new ArrayList<PhotoInfo>();
                list.add(photoInfo);
                resultMuti(list);
            }
            mHanlder.sendMessageDelayed(message, 100);
        } else {//多选
            mSelectPhotoMap.put(photoInfo.getPhotoPath(), photoInfo);
            mHanlder.sendMessageDelayed(message, 1);
        }
    }

    /**
     * 执行裁剪
     */
    protected void toPhotoEdit() {
        Intent intent = new Intent(this, PhotoEditActivity.class);
        intent.putExtra(PhotoEditActivity.SELECT_MAP, mSelectPhotoMap);
        startActivityForResult(intent, GalleryFinal.EDIT_REQUEST_CODE);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if ( id == R.id.ll_title ) {
            if ( mLlFolderPanel.getVisibility() == View.VISIBLE ) {
                mLlFolderPanel.setVisibility(View.GONE);
            } else {
                mLlFolderPanel.setVisibility(View.VISIBLE);
            }
        } else if ( id == R.id.iv_take_photo ) {
            //判断是否达到多选最大数量
            if (mGalleryConfig.isMutiSelect() && mSelectPhotoMap.size() == mGalleryConfig.getMaxSize()) {
                toast(getString(R.string.select_max_tips));
                return;
            }
            
            //判断是否有sd card
            if (!DeviceUtils.existSDCard()) 
            {
                toast(getString(R.string.empty_sdcard));
                return;
            }

            takePhotoAction();
        } else if ( id == R.id.iv_back ) {
            if ( mLlFolderPanel.getVisibility() == View.VISIBLE ) {
                mLlTitle.performClick();
            } else {
                finish();
            }
        } else if ( id == R.id.fab_ok ) {
            ArrayList<PhotoInfo> photoList = new ArrayList<PhotoInfo>(mSelectPhotoMap.values());
            if (mSelectPhotoMap.size() == 0 || !mGalleryConfig.isEditPhoto()) {
                //resultMuti(photoList);
            } else {
                toPhotoEdit();
            }
        } else if ( id == R.id.iv_clear ) {
            mSelectPhotoMap.clear();
            mPhotoListAdapter.notifyDataSetChanged();
            refreshSelectCount();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        int parentId = parent.getId();
        if ( parentId == R.id.lv_folder_list ) {
            folderItemClick(position);
        } else {
            photoItemClick(view, position);
        }
    }
    private void folderItemClick(int position) {
        mLlFolderPanel.setVisibility(View.GONE);
        mCurPhotoList.clear();
        PhotoFolderInfo photoFolderInfo = mAllPhotoFolderList.get(position);
        if ( photoFolderInfo.getPhotoList() != null ) {
            mCurPhotoList.addAll(photoFolderInfo.getPhotoList());
        }
        mPhotoListAdapter.notifyDataSetChanged();

        if (position == 0) {
            mPhotoTargetFolder = null;
        } else {
            PhotoInfo photoInfo = photoFolderInfo.getCoverPhoto();
            if (photoInfo != null && photoInfo.getPhotoPath()!="") {
                mPhotoTargetFolder = new File(photoInfo.getPhotoPath()).getParent();
            } else {
                mPhotoTargetFolder = null;
            }
        }
        mTvSubTitle.setText(photoFolderInfo.getFolderName());
        mFolderListAdapter.setSelectFolder(photoFolderInfo);
        mFolderListAdapter.notifyDataSetChanged();

        if (mCurPhotoList.size() == 0) {
            mTvEmptyView.setText(R.string.no_photo);
        }
    }

    private void photoItemClick(View view, int position) {
        PhotoInfo info = mCurPhotoList.get(position);
        if (!mGalleryConfig.isMutiSelect()) {
            mSelectPhotoMap.clear();
            mSelectPhotoMap.put(info.getPhotoPath(), info);
            if (mGalleryConfig.isEditPhoto()) {
                toPhotoEdit();
            } else {
                ArrayList<PhotoInfo> list = new ArrayList<PhotoInfo>();
                list.add(info);
                resultMuti(list);
            }
            return;
        }
        boolean checked = false;
        if (mSelectPhotoMap.get(info.getPhotoPath()) == null) {
            if (mGalleryConfig.isMutiSelect() && mSelectPhotoMap.size() == mGalleryConfig.getMaxSize()) {
                toast(getString(R.string.select_max_tips));
                return;
            } else {
                mSelectPhotoMap.put(info.getPhotoPath(), info);
                checked = true;
            }
        } else {
            mSelectPhotoMap.remove(info.getPhotoPath());
            checked = false;
        }
        refreshSelectCount();

        PhotoListAdapter.PhotoViewHolder holder = (PhotoListAdapter.PhotoViewHolder) view.getTag();
        if (holder != null) {
            if (checked) {
                holder.mIvCheck.setBackgroundColor(getResources().getColor(R.color.gf_blue_theme_color));
            } else {
                holder.mIvCheck.setBackgroundColor(Color.rgb(0xd2, 0xd2, 0xd7));
            }
        } else {
            mPhotoListAdapter.notifyDataSetChanged();
        }
    }

    public void refreshSelectCount() {
        mTvChooseCount.setText(getString(R.string.selected, mSelectPhotoMap.size(), mGalleryConfig.getMaxSize()));
        if ( mSelectPhotoMap.size() > 0 && mGalleryConfig.isMutiSelect() ) {
            mIvClear.setVisibility(View.VISIBLE);
        } else {
            mIvClear.setVisibility(View.GONE);
        }
    }

    /**
     * 获取所有图片
     */
    private void getPhotos() {
        mTvEmptyView.setText(R.string.waiting);
        mGvPhotoList.setEnabled(false);
        mLlTitle.setEnabled(false);
        mIvTakePhoto.setEnabled(false);
        new Thread() {
            @Override
            public void run() {
                super.run();

                mAllPhotoFolderList.clear();
                List<PhotoFolderInfo> allFolderList = PhotoTools.getAllPhotoFolder(PhotoSelectActivity.this);
                mAllPhotoFolderList.addAll(allFolderList);

                mCurPhotoList.clear();
                if ( allFolderList.size() > 0 ) {
                    if ( allFolderList.get(0).getPhotoList() != null ) {
                        mCurPhotoList.addAll(allFolderList.get(0).getPhotoList());
                    }
                }

                refreshAdapter();
            }
        }.start();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ( keyCode == KeyEvent.KEYCODE_BACK ) {
            if ( mLlFolderPanel.getVisibility() == View.VISIBLE ) {
                mLlTitle.performClick();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if ( mHasRefreshGallery ) {
            mHasRefreshGallery = false;
            getPhotos();
        }
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        if ( mGalleryConfig.getImageLoader() != null ) {
            mGalleryConfig.getImageLoader().clearMemoryCache();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPhotoTargetFolder = null;
        mSelectPhotoMap.clear();
        System.gc();
    }
}
