package com.example.mdtoword.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.mdtoword.pojo.Category;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CategoryMapper extends BaseMapper<Category>{
    // 继承BaseMapper后，已经有基本的CRUD方法，无需额外定义
}
