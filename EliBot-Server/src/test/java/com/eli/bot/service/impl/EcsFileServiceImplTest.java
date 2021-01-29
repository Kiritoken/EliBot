package com.eli.bot.service.impl;

import com.eli.bot.service.IEcsFileService;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
public class EcsFileServiceImplTest {

    @Autowired
    private IEcsFileService ecsFileService;


    @Test
    public void testUploadFile() throws QiniuException {
        File file = new File("C:\\Users\\Eli\\Videos\\Captures\\testgg.mp4");
        Response response = ecsFileService.uploadFile(file);
        log.info("{}", response);
    }

}