package com.eli.bot.service.impl;

import com.eli.bot.core.SystemClock;
import com.eli.bot.service.IEcsFileService;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;


/**
 * @author Eli
 */
@Slf4j
@Service
public class EcsFileServiceImpl implements IEcsFileService {

    private final UploadManager uploadManager;

    private final Auth auth;

    @Value("${qiniu.Bucket}")
    private String bucket;

    private static final int RETRY_TIME = 3;

    public EcsFileServiceImpl(UploadManager uploadManager, Auth auth) {
        this.uploadManager = uploadManager;
        this.auth = auth;
    }


    @Override
    public Response uploadFile(File file) throws QiniuException {
        log.info("开始上传文件{}至存储空间...", file.getName());
        long beginTime = SystemClock.now();
        Response response = uploadManager.put(file, null, getUploadToken());
        int retry = 0;
        while (response.needRetry() && retry < RETRY_TIME) {
            response = uploadManager.put(file, null, getUploadToken());
            retry++;
        }
        if (retry == RETRY_TIME) {
            log.info("{}次重试后,上传文件{}失败,耗时{}ms...", RETRY_TIME, file.getName(), SystemClock.now() - beginTime);
            throw new RuntimeException("文件上传失败");
        }else {
            log.info("文件{}上传成功,耗时{}ms...", file.getName(), SystemClock.now() - beginTime);
        }
        return response;
    }

    /**
     * 获取凭证
     *
     * @return 凭证
     */
    private String getUploadToken() {
        return this.auth.uploadToken(bucket);
    }
}
