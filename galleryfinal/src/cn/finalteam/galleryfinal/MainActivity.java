package cn.finalteam.galleryfinal;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Toast;
import cn.finalteam.galleryfinal.GalleryConfig;
import cn.finalteam.galleryfinal.GalleryFinal;
import cn.finalteam.galleryfinal.model.PhotoInfo;
import cn.finalteam.galleryfinal.utils.Logger;
import cn.finalteam.galleryfinal.widget.HorizontalListView;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

	RadioButton mRbUil;
	RadioButton mRbGlide;
	RadioButton mRbPicasso;
	RadioButton mRbSingleSelect;
    RadioButton mRbMutiSelect;
    EditText mEtMaxSize;
    Button mBtnOpenGallery;
    HorizontalListView mLvPhoto;
    CheckBox mCbEdit;
    CheckBox mCbCrop;
    CheckBox mCbRotate;
    CheckBox mCbShowCamera;
    LinearLayout mLlMaxSize;
    LinearLayout mLlEdit;
    RadioButton mRbXutils;
    RadioButton mRbXutils3;
    EditText mEtCropWidth;
    EditText mEtCropHeight;
    LinearLayout mLlCropSize;
    CheckBox mCbCropSquare;

    private List<PhotoInfo> mPhotoList;
    private ChoosePhotoListAdapter mChoosePhotoListAdapter;
    private Button mOpenGallery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.init("galleryfinal", true);
        setContentView(R.layout.activity_main);
        mRbUil=(RadioButton) findViewById(R.id.rb_uil);
        mRbGlide=(RadioButton) findViewById(R.id.rb_glide);
        mRbPicasso=(RadioButton) findViewById(R.id.rb_picasso);
        mRbSingleSelect=(RadioButton) findViewById(R.id.rb_single_select);
        mRbMutiSelect=(RadioButton) findViewById(R.id.rb_muti_select);
        mEtMaxSize=(EditText) findViewById(R.id.et_max_size);
        mBtnOpenGallery=(Button) findViewById(R.id.btn_open_gallery);
        mLvPhoto=(HorizontalListView) findViewById(R.id.lv_photo);
        mCbEdit=(CheckBox) findViewById(R.id.cb_edit);
        mCbCrop=(CheckBox) findViewById(R.id.cb_crop);
        mCbRotate=(CheckBox) findViewById(R.id.cb_rotate);
        mCbShowCamera=(CheckBox) findViewById(R.id.cb_show_camera);
        mLlMaxSize=(LinearLayout) findViewById(R.id.ll_max_size);
        mLlEdit=(LinearLayout) findViewById(R.id.ll_edit);
        mRbXutils=(RadioButton) findViewById(R.id.rb_xutils);
        mRbXutils3=(RadioButton) findViewById(R.id.rb_xutils3);
        mEtCropWidth=(EditText) findViewById(R.id.et_crop_width);
        mEtCropHeight=(EditText) findViewById(R.id.et_crop_height);
        mLlCropSize=(LinearLayout) findViewById(R.id.ll_crop_size);
        mCbCropSquare=(CheckBox) findViewById(R.id.cb_crop_square);
        
        
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
        mLvPhoto = (HorizontalListView) findViewById(R.id.lv_photo);
        mPhotoList = new ArrayList<PhotoInfo>();
        mChoosePhotoListAdapter = new ChoosePhotoListAdapter(this, mPhotoList);
        mLvPhoto.setAdapter(mChoosePhotoListAdapter);
        //x.Ext.init(getApplication());
        mOpenGallery = (Button) findViewById(R.id.btn_open_gallery);
        mRbMutiSelect.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mLlMaxSize.setVisibility(View.VISIBLE);
                } else {
                    mLlMaxSize.setVisibility(View.GONE);
                }
            }
        });
        mCbEdit.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mLlEdit.setVisibility(View.VISIBLE);
                } else {
                    mLlEdit.setVisibility(View.GONE);
                }
            }
        });
        mCbCrop.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mLlCropSize.setVisibility(View.VISIBLE);
                } else {
                    mLlCropSize.setVisibility(View.GONE);
                }
            }
        });
        mOpenGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //GalleryConfig config = new GalleryConfig.Builder(MainActivity.this)
                //        .mutiSelect()
                //        .mutiSelectMaxSize(8)
                //        .enableEdit()
                //        .enableCrop()
                //        .enableRotate()
                //        .showCamera()
                //        .imageloader(new UILImageLoader())
                //        .cropSquare()
                //        .cropWidth(50)
                //        .cropHeight(50)
                //        .filter(mPhotoList)
                //        .build();
                //GalleryFinal.open(config);
                GalleryConfig.Builder builder = new GalleryConfig.Builder(MainActivity.this);
                builder.imageloader(new cn.finalteam.galleryfinal.loader.GlideImageLoader());
//                if (mRbUil.isChecked()) {
//                    builder.imageloader(new UILImageLoader());
//                } else if (mRbXutils.isChecked()) {
//                    builder.imageloader(new XUtilsImageLoader(MainActivity.this));
//                } else if (mRbXutils3.isChecked()) {
//                    builder.imageloader(new XUtils3ImageLoader());
//                } else if (mRbGlide.isChecked()) {
//                    builder.imageloader(new GlideImageLoader());
//                } else {
//                    builder.imageloader(new PicassoImageLoader());
//                }

                if (mRbSingleSelect.isChecked()) {
                    builder.singleSelect();
                } else {
                    builder.mutiSelect();
                    if (TextUtils.isEmpty(mEtMaxSize.getText().toString())) {
                        Toast.makeText(getApplicationContext(), "请输入MaxSize", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    int maxSize = Integer.parseInt(mEtMaxSize.getText().toString());
                    builder.mutiSelectMaxSize(maxSize);
                }

                if (mCbEdit.isChecked()) {
                    builder.enableEdit();
                }

                if (mCbRotate.isChecked()) {
                    builder.enableRotate();
                }

                if (mCbCrop.isChecked()) {
                    builder.enableCrop();
                    if (!TextUtils.isEmpty(mEtCropWidth.getText().toString())) {
                        int width = Integer.parseInt(mEtCropWidth.getText().toString());
                        builder.cropWidth(width);
                    }

                    if (!TextUtils.isEmpty(mEtCropHeight.getText().toString())) {
                        int height = Integer.parseInt(mEtCropHeight.getText().toString());
                        builder.cropHeight(height);
                    }

                    if (mCbCropSquare.isChecked()) {
                        builder.cropSquare();
                    }
                }

                if (mCbShowCamera.isChecked()) {
                    builder.showCamera();
                }

                builder.filter(mPhotoList);//添加过滤集合

                GalleryConfig config = builder.build();
                GalleryFinal.open(config);
            }
        });
        initImageLoader(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GalleryFinal.GALLERY_REQUEST_CODE) {
            if (resultCode == GalleryFinal.GALLERY_RESULT_SUCCESS) {
                List<PhotoInfo> photoInfoList = (List<PhotoInfo>) data.getSerializableExtra(GalleryFinal.GALLERY_RESULT_LIST_DATA);
                if (photoInfoList != null) {
                    mPhotoList.addAll(photoInfoList);
                    mChoosePhotoListAdapter.notifyDataSetChanged();
                }
            }
        }
    }

    public static void initImageLoader(Context context) {
        // This configuration tuning is custom. You can tune every option, you may tune some of them,
        // or you can create default configuration by
        //  ImageLoaderConfiguration.createDefault(this);
        // method.
        ImageLoaderConfiguration.Builder config = new ImageLoaderConfiguration.Builder(context);
        config.threadPriority(Thread.NORM_PRIORITY - 2);
        config.denyCacheImageMultipleSizesInMemory();
        config.diskCacheFileNameGenerator(new Md5FileNameGenerator());
        config.diskCacheSize(50 * 1024 * 1024); // 50 MiB
        config.tasksProcessingOrder(QueueProcessingType.LIFO);
        config.writeDebugLogs(); // Remove for release app

        // Initialize ImageLoader with configuration.
        ImageLoader.getInstance().init(config.build());
    }
}
