package com.pgmerxw.eatwhat.controller;

import com.pgmerxw.eatwhat.model.Prize;
import com.pgmerxw.eatwhat.service.RedisService;
import com.pgmerxw.eatwhat.service.WheelService;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/wheel")
public class WheelController {
    @Autowired
    WheelService wheelService;

    @Autowired
    RedisService redisService;

    /**
     * 获取转盘选项
     * @return List<Prize>
     */
    @GetMapping("/getPrizeList")
    public List<Prize> getPrizeList() {
        return wheelService.getPrizeList();
    }

    /**
     * 吃了啥
     * @param prize
     * @return s
     */
    @PostMapping("/saveEatWhat")
    public String saveEatWhat(@RequestBody Prize prize){
        int i = wheelService.insertRecord(prize);
        if (i == 1){
            try {
                //每天只吃一次中饭
                Map<String,Object> currentPrize = new HashMap<>();
                LocalDate localDate = LocalDate.now();
                //吃了啥
                currentPrize.put("name",prize.getName());
                //吃的日期
                currentPrize.put("date",localDate.toString());
                redisService.saveMapToValue("todayPrize",currentPrize);
            }catch (Exception e){
                e.printStackTrace();
            }
            return "success";
        }else {
            return "fail";
        }
    }

    /**
     * 根据id删除奖品
     * @param id
     * @return
     */
    @DeleteMapping("/deletePrizeById/{id}")
    public String deletePrizeById(@PathVariable Long id){
        val i = wheelService.deletePrizeById(id);
        if (i == 1){
            return "success";
        }else {
            return "fail";
        }
    }

    /**
     * 今天吃的啥
     * @return Prize
     */
    @GetMapping("/getTodayPrize")
    public String getTodayPrize() {
        String name = "";
        Map<String, Object> currentPrize = redisService.getMapByValue("todayPrize");
        String prizeName = currentPrize.get("name").toString();
        String data = (String) currentPrize.get("date");
        String localDate = LocalDate.now().toString();
        boolean isSameDay = data.equals(localDate);
        if (isSameDay) {
            name = prizeName;
        }
        return name;
    }


}
