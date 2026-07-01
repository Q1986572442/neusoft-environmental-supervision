package org.nep.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.nep.system.entity.News;
import org.nep.system.mapper.NewsMapper;
import org.nep.system.service.NewsService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 环保新闻公告 Service 实现
 */
@Service
public class NewsServiceImpl extends ServiceImpl<NewsMapper, News> implements NewsService {

    @Override
    public News publish(News news) {
        news.setStatus(1);
        news.setPublishTime(LocalDateTime.now());
        news.setViewCount(0L);
        this.save(news);
        return news;
    }

    @Override
    public void incrementViewCount(Long id) {
        News news = this.getById(id);
        if (news != null) {
            news.setViewCount((news.getViewCount() == null ? 0 : news.getViewCount()) + 1);
            this.updateById(news);
        }
    }
}
