package org.nep.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.nep.system.entity.News;

/**
 * 环保新闻公告 Mapper
 */
@Mapper
public interface NewsMapper extends BaseMapper<News> {
}
