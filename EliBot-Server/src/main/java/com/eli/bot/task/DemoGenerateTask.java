package com.eli.bot.task;

import com.eli.bot.constants.VdmConstant;
import com.eli.bot.core.BotCenter;
import com.eli.bot.core.SystemClock;
import com.eli.bot.service.IDemoParserService;
import com.eli.bot.service.IEcsFileService;
import com.eli.bot.utils.SpringContextUtil;
import com.qiniu.http.Response;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.message.data.At;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.springframework.http.HttpMethod;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 * demo制作任务
 *
 * @author Eli
 */
@Data
@Slf4j
public class DemoGenerateTask implements Runnable {

    private static final Object LOCK = new Object();

    private static final String DEMO_PATH = "D://demo//";

    private static final String SPLIT = "/";

    private static final String SPLIT_LEFT = "\\";

    private static final String HLAE_EXE = "C:\\Users\\Eli\\AppData\\Local\\AkiVer\\hlae\\HLAE.exe";

    private static final String CSGO_EXE = "D:\\app\\steam\\steamapps\\common\\Counter-Strike Global Offensive\\csgo.exe";

    private static final Long RECORDING_MAX_WAIT_TIME = 300000L;

    /**
     * 群号
     */
    private Long groupId;

    /**
     * 群成员ID
     */
    private Long memberId;

    /**
     * 被观察者的64位steamId
     */
    private String steam64Id;

    /**
     * demo链接
     */
    private String demoUrl;

    private RestTemplate restTemplate;

    private Bot bot;

    private IDemoParserService demoParserService;

    private IEcsFileService ecsFileService;


    public DemoGenerateTask(Long groupId, Long memberId, String steam64Id, String demoUrl) {
        this.groupId = groupId;
        this.memberId = memberId;
        this.steam64Id = steam64Id;
        this.demoUrl = demoUrl;
        this.restTemplate = SpringContextUtil.getBean(RestTemplate.class);
        this.bot = SpringContextUtil.getBean(BotCenter.class).getBot();
        this.demoParserService = SpringContextUtil.getBean(IDemoParserService.class);
        this.ecsFileService = SpringContextUtil.getBean(IEcsFileService.class);
    }

    @Override
    public void run() {
        synchronized (LOCK) {
            log.info("demo开始解析");
            long beginTime = SystemClock.now();
            final At at = new At(memberId);
            try {
                // 文件名
                String[] split = demoUrl.split(SPLIT);
                String fileName = split[split.length - 1];
                String dirName = fileName.substring(0, fileName.lastIndexOf("."));
                // 1. 下载demo
                File tempFile = downloadDemoZipFile(dirName);
                if (tempFile == null) {
                    Objects.requireNonNull(bot.getGroup(groupId)).sendMessage(at.plus(String.format("demo文件%s下载异常,请确认下载链接是否正确", demoUrl)));
                    return;
                }
                log.info("demo文件{}下载完毕,存储路径:{}", demoUrl, tempFile.getAbsolutePath());

                // 2. 解压demo
                File demoFile = unzipDemo(tempFile, dirName);
                tempFile.delete();
                if (demoFile == null) {
                    Objects.requireNonNull(bot.getGroup(groupId)).sendMessage(at.plus(String.format("demo文件%s解压异常,请确认demo是否完整", demoUrl)));
                    return;
                }
                log.info("demo文件{}解压完毕,存储路径:{}", demoUrl, demoFile.getAbsolutePath());

                // 3. 解析demo 调用node server接口
                List<Long> lowlightTicks = demoParserService.getLowlightTicks(demoFile.getAbsolutePath(), steam64Id);
                if (CollectionUtils.isEmpty(lowlightTicks)) {
                    Objects.requireNonNull(bot.getGroup(groupId)).sendMessage(at.plus(String.format("demo文件%s不存在%s用户的低光时刻，请检查steamId是否正确", demoUrl, steam64Id)));
                    return;
                }
                log.info("demo文件{}解析完毕", demoFile.getAbsolutePath());

                // 4. 生成并存储低光时刻vdm文件
                File vdmFile = generateLowlightVdm(demoFile.getAbsolutePath().substring(0, demoFile.getAbsolutePath().lastIndexOf(SPLIT_LEFT)), dirName, lowlightTicks, steam64Id);
                if (vdmFile == null) {
                    Objects.requireNonNull(bot.getGroup(groupId)).sendMessage(at.plus(String.format("demo文件%s解析异常", demoUrl)));
                    return;
                }
                log.info("demo文件{} VDM解析完毕,存储路径:{}", demoUrl, vdmFile.getAbsolutePath());

                // 5. 调用hlae录制demo
                if (!recordDemo(demoFile)) {
                    Objects.requireNonNull(bot.getGroup(groupId)).sendMessage(at.plus(String.format("demo文件%s录制异常", demoUrl)));
                    return;
                }
                // 6.线程等待，判断视频是否生成完毕 TODO 其他方法进行优化
                long startRecordingTime = SystemClock.now();
                while (SystemClock.now() - startRecordingTime < RECORDING_MAX_WAIT_TIME) {
                    // 监测是否生成了最终的视频文件
                    String filePath = demoFile.getAbsolutePath().substring(0, demoFile.getAbsolutePath().lastIndexOf(SPLIT_LEFT));
                    File mp4File = new File(filePath + "\\take0000\\raw\\video.mp4");
                    if (mp4File.exists() && !isProcessExist("csgo")) {
                        log.info("demo文件{}录制完毕开始上传", demoFile.getAbsoluteFile());
                        // 修改文件名称并上传
                        String mp4FileName = dirName + steam64Id + "_ll.mp4";
                        File newFile = new File(filePath + "//" + mp4FileName);
                        FileUtils.moveFile(mp4File, newFile);
                        FileUtils.deleteDirectory(new File(filePath + "\\take0000"));
                        // 上传视频
                        ecsFileService.uploadFile(newFile);
                        // 7.callback
                        String url = "http://cdn.sorrelcivet.cn/" + mp4FileName;
                        long duration = SystemClock.now() - beginTime;
                        log.info("demo文件{}上传完毕，共计耗时{}ms", url, duration);
                        Objects.requireNonNull(bot.getGroup(groupId)).sendMessage(at.plus(String.format("点击%s查收您的低光白给时刻,本次耗时%s ms", url, duration)));
                        return;
                    }
                }
                Objects.requireNonNull(bot.getGroup(groupId)).sendMessage(at.plus(String.format("demo %s 解析超时", demoUrl)));
            } catch (Exception e) {
                log.error("demo解析异常", e);
                // 返回异常信息
                Objects.requireNonNull(bot.getGroup(groupId)).sendMessage(at.plus(String.format("demo文件%s解析异常", demoUrl)));
            }
        }
    }

    @Nullable
    private File downloadDemoZipFile(String dirName) {
        return restTemplate.execute(demoUrl, HttpMethod.GET, null, clientHttpResponse -> {
            File temp = File.createTempFile(dirName, "zip");
            InputStream body = clientHttpResponse.getBody();
            FileUtils.copyInputStreamToFile(body, temp);
            return temp;
        });
    }

    private File unzipDemo(File tempFile, String dirName) {
        try (ZipFile zipFile = new ZipFile(tempFile);
             ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(tempFile))) {
            ZipEntry zipEntry;
            File unZipFile;
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                String entryName = zipEntry.getName();
                if (StringUtils.equals(dirName, entryName.substring(0, entryName.lastIndexOf(".")))) {
                    InputStream inputStream = zipFile.getInputStream(zipEntry);
                    unZipFile = new File(DEMO_PATH + dirName + "//" + entryName);
                    FileUtils.copyInputStreamToFile(inputStream, unZipFile);
                    return unZipFile;
                }
            }
            return null;
        } catch (Exception e) {
            log.error("文件{}解压异常", tempFile.getName(), e);
            return null;
        }
    }

    private File generateLowlightVdm(String path, String fileName, List<Long> lowlightTicks, String steam64Id) {
        try {
            int orderCount = 0;
            StringBuilder content = new StringBuilder();
            // 1. 锁定视角
            content.append(String.format(VdmConstant.SPEC_PLAYER, ++orderCount, orderCount, 0, steam64Id));

            // 2. hlae相关命令
            String movieCommand = String.format("sv_cheats 1;cl_draw_only_deathnotices 1;mirv_streams record name \\\"%s\\\" ;", path.replace("\\", "\\\\"));
            long firstKilledTick = lowlightTicks.get(0) - VdmConstant.RECALL_TICK < 0 ? 0 : lowlightTicks.get(0) - VdmConstant.RECALL_TICK;
            content.append(String.format(VdmConstant.EXECUTE_COMMAND, ++orderCount, orderCount, firstKilledTick, movieCommand));

            String recordCommand = "mirv_streams add normal raw;mirv_streams edit raw record 1;mirv_streams edit raw settings afxFfmpegYuv420p;host_framerate 60;mirv_streams record start;";
            content.append(String.format(VdmConstant.EXECUTE_COMMAND, ++orderCount, orderCount, firstKilledTick, recordCommand));

            // 3. 击杀镜头跳转
            long startTick = 0;
            for (Long tick : lowlightTicks) {
                long skipToTick = tick - VdmConstant.RECALL_TICK < 0 ? 0 : tick - VdmConstant.RECALL_TICK;
                content.append(String.format(VdmConstant.SKIP_AHEAD, ++orderCount, orderCount, startTick, skipToTick));
                startTick = tick + VdmConstant.RECALL_TICK;
            }

            // 4. 结束录制;quit;
            String endRecordCommand = "mirv_streams record end;quit;";
            content.append(String.format(VdmConstant.EXECUTE_COMMAND, ++orderCount, orderCount, startTick, endRecordCommand));

            // 5. main
            String res = String.format(VdmConstant.MAIN, content);
            File file = new File(path + "//" + fileName + ".vdm");
            FileUtils.write(file, res, "UTF-8");
            return file;
        } catch (Exception e) {
            log.error("demo文件{}生成VDM异常", demoUrl, e);
            return null;
        }
    }

    private boolean recordDemo(File demoFile) {
        try {
            String command = HLAE_EXE + " -csgoLauncher -noGui -autoStart -csgoExe \"" + CSGO_EXE +
                    "\" -gfxEnabled true -gfxWidth 1280 -gfxHeight 720 -gfxFull false " +
                    "-customLaunchOptions \"-applaunch 730 -novid -perfectworld -insecure -console +playdemo " +
                    demoFile.getAbsolutePath().replace("\\", "\\\\") +
                    "\"";
            log.info("{}", command);
            // TODO check关闭进程

            Process process = Runtime.getRuntime().exec(command);
            int exitCode = process.waitFor();
            log.info("{}HLAE执行结束，返回结果{}", demoFile.getAbsoluteFile(), exitCode);
            return exitCode == 0;
        } catch (Exception e) {
            log.error("录制demo{}出错", demoFile.getAbsoluteFile(), e);
            return false;
        }
    }


    private boolean isProcessExist(String processName) {
        BufferedReader bufferedReader = null;
        try {
            Properties prop = System.getProperties();
            Process proc;
            //获取操作系统名称
            String os = prop.getProperty("os.name");
            if (os != null && os.toLowerCase().startsWith("lin")) {
                proc = Runtime.getRuntime().exec("ps -ef");
            } else {
                proc = Runtime.getRuntime().exec("tasklist");
            }
            bufferedReader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.contains(processName)) {
                    return true;
                }
            }
            return false;
        } catch (Exception ex) {
            log.error("get procedure whetherRunning failed!");
            return false;
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (Exception ex) {
                    log.error("bufferedReader close failed!");
                }
            }
        }
    }

}
