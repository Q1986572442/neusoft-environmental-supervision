package org.nep.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.nep.system.entity.News;

/**
 * 环保新闻公告 Service
 */
public interface NewsService extends IService<News> {
    /** 发布新闻（设置发布时间） */
    News publish(News news);
    /** 增加浏览次数 */
    void incrementViewCount(Long id);
}
