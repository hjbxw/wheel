package com.pgmerxw.eatwhat.controller;

import com.pgmerxw.eatwhat.model.Prize;
import com.pgmerxw.eatwhat.service.WheelService;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wheel")
public class WheelController {
    @Autowired
    WheelService wheelService;

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
}
