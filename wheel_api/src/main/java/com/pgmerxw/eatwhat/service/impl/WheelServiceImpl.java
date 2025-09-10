package com.pgmerxw.eatwhat.service.impl;

import com.pgmerxw.eatwhat.mapper.PrizeMapper;
import com.pgmerxw.eatwhat.model.Prize;
import com.pgmerxw.eatwhat.service.WheelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WheelServiceImpl implements WheelService {
    @Autowired
    PrizeMapper prizeMapper;

    /**
     *
     * @return
     */
    @Override
    public List<Prize> getPrizeList() {
        return prizeMapper.getPrizeList();
    }

    /**
     * @param prize
     * @return 影响行数
     */
    @Override
    public int updatePrizeById(Prize prize) {
        return prizeMapper.updatePrizeById(prize);
    }

    /**
     * @param prize
     * @return 影响行数
     */
    @Override
    public int insertPrize(Prize prize) {
        return prizeMapper.insertPrize(prize);
    }

    /**
     * @param id
     * @return
     */
    @Override
    public int deletePrizeById(Long id) {
        return prizeMapper.deletePrizeById(id);
    }


    /**
     * 保存记录
     * @param prize
     * @return
     */
    @Override
    public int insertRecord(Prize prize) {
        return prizeMapper.insertRecord(prize);
    }
}
