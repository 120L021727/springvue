package com.example.mdtoword.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.mdtoword.pojo.Blog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BlogMapper extends BaseMapper<Blog> {
}
