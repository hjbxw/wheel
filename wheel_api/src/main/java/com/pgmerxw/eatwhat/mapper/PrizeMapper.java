package com.pgmerxw.eatwhat.mapper;

import com.pgmerxw.eatwhat.model.Prize;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface PrizeMapper {
    List<Prize> getPrizeList();

    int updatePrizeById(Prize prize);

    int insertPrize(Prize prize);

    int deletePrizeById(Long id);

    int insertRecord(Prize prize);
}
