package com.eli.bot.service;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;

import java.io.File;

/**
 * 文件存储服务
 *
 * @author Eli
 */
public interface IEcsFileService {


    /**
     * 上传文件
     *
     * @param file 文件
     * @return 上传结果
     * @throws QiniuException 上传文件中抛出的异常
     */
    Response uploadFile(File file) throws QiniuException;

}
