package com.jyx.healthsys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jyx.healthsys.entity.Detail;
import com.jyx.healthsys.mapper.DetailMapper;
import com.jyx.healthsys.service.IDetailService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.util.List;


@Service
public class DetailServiceImpl extends ServiceImpl<DetailMapper, Detail> implements IDetailService {


    @Resource
    private DetailMapper detailMapper;


    @Override
    public List<Detail> getDetailInfo(String sportName) {

        return detailMapper.getDetailInfo(sportName);
    }




    @Override
    @Transactional
    public boolean addDetail(Detail detail) {
        // 1. 检查运动类型是否已存在
        QueryWrapper<Detail> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("sport_type", detail.getSportType());
        Long count = this.baseMapper.selectCount(queryWrapper);

        if (count > 0) {
            throw new RuntimeException("运动类型 '" + detail.getSportType() + "' 已存在，不能重复添加");
        }

        // 2. 插入新记录
        return this.baseMapper.insert(detail) > 0;
    }


    @Override
    public void updateDetail(Detail detail) {
        // 更新用户表中的数据
        this.baseMapper.updateById(detail);
    }

    @Override
    public Detail getDetailById(Integer id) {
        System.out.println(id);
        Detail detail = this.baseMapper.selectById(id);
        return detail;
    }

    @Override
    public void deletDetailById(Integer id) {
        this.baseMapper.deleteById(id);
    }
}


