package org.nep.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.nep.system.entity.Knowledge;

public interface KnowledgeService extends IService<Knowledge> {
    void incrementViewCount(Long id);
    void incrementLikeCount(Long id);
}
