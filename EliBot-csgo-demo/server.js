var fs = require("fs");
var demofile = require("demofile");
var express = require('express');
var app = express();

var bodyParser = require('body-parser')
app.use(bodyParser.json())
app.use(bodyParser.urlencoded({
    extended: false
}))


async function getLowlightTick(demoPath, steam64Id) {
    return new Promise((resolve, reject) => {
        fs.readFile(demoPath, (err, buffer) => {
            const demoFile = new demofile.DemoFile();
            let ticks = []
            demoFile.gameEvents.on("player_death", e => {
                // 受害者
                const victim = demoFile.entities.getByUserId(e.userid);
                if (victim && victim.steam64Id === steam64Id) {
                    ticks.push(demoFile.currentTick);
                }
            });

            demoFile.on("end", e => {
                if (e.error || err) {
                    console.error("解析异常", demoPath);
                    reject("解析异常");
                } else {
                    resolve(ticks);
                }
            });

            demoFile.parse(buffer);
        })
    })
}


app.post('/getLowlightTick', async function (req, res) {
    // demo文件路径
    const demoPath = req.body.demoPath
    // steam64Id
    const steam64Id = req.body.steam64Id
    // 解析
    var ticks = await getLowlightTick(demoPath, steam64Id).then(data => {
        res.send({
            message: '解析成功',
            code: 200,
            ticks: data
        });
    }).catch(err => {
        console.error(err);
        res.send({
            message: '解析失败',
            code: 500
        });
    })

})


var server = app.listen(8082, function () {
    var host = server.address().address
    var port = server.address().port
    console.log("实例已启动,http://%s:%s", host, port)
})

/**
 
mirv_streams record name "D:\test";
mirv_streams add baseFx raw;
mirv_streams edit raw record 1;
mirv_streams edit raw settings afxFfmpegYuv420p;
host_framerate 300;
mirv_streams record start;
mirv_streams record end;


GenerateVdmFile

hlae.exe -noGui -csgoLauncher -autoStart 
-csgoExe "" 
-customLaunchOptions "-applaunch 730 -novid -perfectworld -console +playdemo xxx "



playdemo "d:\app\steam\steamapps\common\counter-strike global offensive\csgo\replays\match730_003459360530727174490_1725346146_233.dem"
playdemo "d:\app\steam\steamapps\common\counter-strike global offensive\csgo\replays\test.dem"

hlae.exe -csgoLauncher -noGui -autoStart -csgoExe "D:\app\steam\steamapps\common\Counter-Strike Global Offensive\csgo.exe" -gfxEnabled true -gfxWidth 1280 -gfxHeight 720 -gfxFull false -customLaunchOptions "-applaunch 730 -novid -perfectworld -insecure -console +playdemo d:\\app\\steam\\steamapps\\common\\counter-strike global offensive\\csgo\\replays\\test.dem"

 */