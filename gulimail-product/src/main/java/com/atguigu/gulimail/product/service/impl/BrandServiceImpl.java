package com.atguigu.gulimail.product.service.impl;

import com.atguigu.gulimail.product.service.CategoryBrandRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimail.product.dao.BrandDao;
import com.atguigu.gulimail.product.entity.BrandEntity;
import com.atguigu.gulimail.product.service.BrandService;
import org.springframework.util.StringUtils;


@Service("brandService")
public class BrandServiceImpl extends ServiceImpl<BrandDao, BrandEntity> implements BrandService {

    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        String key = (String)params.get("key");
        QueryWrapper<BrandEntity> queryWrapper = new QueryWrapper<>();
        if(!StringUtils.isEmpty(key)){
            queryWrapper.eq("brand_id",key).or().like("name",key);
        }
        IPage<BrandEntity> page = this.page(
                new Query<BrandEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    public void updateDetail(BrandEntity brand) {
        //保证冗余字段一致
        this.updateById(brand);
        if(!StringUtils.isEmpty(brand.getName())){
            //同步其它关联表中的数据
            categoryBrandRelationService.updateBrand(brand.getBrandId(),brand.getName());
            //TODO更新其它关联表
        }
    }

    @Cacheable(value = "brand",key = "'brandinf:'+#root.args[0]")
    @Override
    public List<BrandEntity> getBrandsByIds(List<Long> brandIds) {

        return  baseMapper.selectList(new QueryWrapper<BrandEntity>().in("brand_id", brandIds));
    }
}