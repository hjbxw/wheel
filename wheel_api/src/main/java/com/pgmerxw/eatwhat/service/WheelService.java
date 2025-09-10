package com.pgmerxw.eatwhat.service;

import com.pgmerxw.eatwhat.model.Prize;

import java.util.List;

public interface WheelService {

    List<Prize> getPrizeList();

    int updatePrizeById(Prize prize);

    int insertPrize(Prize prize);

    int deletePrizeById(Long id);


    int insertRecord(Prize prize);
}
