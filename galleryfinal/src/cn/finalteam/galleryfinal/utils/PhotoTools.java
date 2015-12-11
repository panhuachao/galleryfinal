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

package cn.finalteam.galleryfinal.utils;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.util.Log;
import cn.finalteam.galleryfinal.GalleryFinal;
import cn.finalteam.galleryfinal.R;
import cn.finalteam.galleryfinal.model.PhotoFolderInfo;
import cn.finalteam.galleryfinal.model.PhotoInfo;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Desction:
 * Author:pengjianbo
 * Date:15/10/10 下午4:26
 */
public class PhotoTools {

    /**
     * 获取所有图片
     * @param context
     * @return
     */
    public static List<PhotoFolderInfo> getAllPhotoFolder(Context context) {
        List<PhotoFolderInfo> allFolderList = new ArrayList<PhotoFolderInfo>();
        final String[] projectionPhotos = {
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.BUCKET_ID,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.DATE_TAKEN,
                MediaStore.Images.Media.ORIENTATION,
                MediaStore.Images.Thumbnails.DATA
        };
        final ArrayList<PhotoFolderInfo> allPhotoFolderList = new ArrayList<PhotoFolderInfo>();
        HashMap<Integer, PhotoFolderInfo> bucketMap = new HashMap<Integer, PhotoFolderInfo>();
        Cursor cursor = null;
        //所有图片
        PhotoFolderInfo allPhotoFolderInfo = new PhotoFolderInfo();
        allPhotoFolderInfo.setFolderId(0);
        allPhotoFolderInfo.setFolderName(context.getResources().getString(R.string.all_photo));
        allPhotoFolderInfo.setPhotoList(new ArrayList<PhotoInfo>());
        allPhotoFolderList.add(0, allPhotoFolderInfo);
        List<String> filterList = GalleryFinal.getGalleryConfig().getFilterList();
        try {
            cursor = MediaStore.Images.Media.query(context.getContentResolver(), MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    , projectionPhotos, "", null, MediaStore.Images.Media.DATE_TAKEN + " DESC");
            if (cursor != null) {
                int bucketNameColumn = cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
                final int bucketIdColumn = cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_ID);
                while (cursor.moveToNext()) {
                    int bucketId = cursor.getInt(bucketIdColumn);
                    String bucketName = cursor.getString(bucketNameColumn);
                    final int dataColumn = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
                    final int imageIdColumn = cursor.getColumnIndex(MediaStore.Images.Media._ID);
                    //int thumbImageColumn = cursor.getColumnIndex(MediaStore.Images.Thumbnails.DATA);
                    final int imageId = cursor.getInt(imageIdColumn);
                    final String path = cursor.getString(dataColumn);
                    //final String thumb = cursor.getString(thumbImageColumn);
                    final PhotoInfo photoInfo = new PhotoInfo();
                    photoInfo.setPhotoId(imageId);
                    photoInfo.setPhotoPath(path);
                    //photoInfo.setThumbPath(thumb);
                    if (photoInfo.getPhotoPath()=="") {
                        continue;
                    }
                    if ( allPhotoFolderInfo.getCoverPhoto() == null ) {
                        allPhotoFolderInfo.setCoverPhoto(photoInfo);
                    }
                    //添加到所有图片
                    allPhotoFolderInfo.getPhotoList().add(photoInfo);

                    //通过bucketId获取文件夹
                    PhotoFolderInfo photoFolderInfo = bucketMap.get(bucketId);

                    if (photoFolderInfo == null) {
                        photoFolderInfo = new PhotoFolderInfo();
                        photoFolderInfo.setPhotoList(new ArrayList<PhotoInfo>());
                        photoFolderInfo.setFolderId(bucketId);
                        photoFolderInfo.setFolderName(bucketName);
                        photoFolderInfo.setCoverPhoto(photoInfo);
                        bucketMap.put(bucketId, photoFolderInfo);
                        allPhotoFolderList.add(photoFolderInfo);
                    }
                    if ( filterList == null || !filterList.contains(photoInfo.getPhotoPath()) ) {
                        photoFolderInfo.getPhotoList().add(photoInfo);
                    }
                }
            }
        } catch (Exception ex) {
            Log.e("error", ex.toString());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        allFolderList.addAll(allPhotoFolderList);
        return allFolderList;
    }
}
