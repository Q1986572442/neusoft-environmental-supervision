package org.nep.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.nep.system.entity.Knowledge;
import org.nep.system.mapper.KnowledgeMapper;
import org.nep.system.service.KnowledgeService;
import org.springframework.stereotype.Service;

@Service
public class KnowledgeServiceImpl extends ServiceImpl<KnowledgeMapper, Knowledge> implements KnowledgeService {

    @Override
    public void incrementViewCount(Long id) {
        Knowledge k = this.getById(id);
        if (k != null) {
            k.setViewCount((k.getViewCount() == null ? 0 : k.getViewCount()) + 1);
            this.updateById(k);
        }
    }

    @Override
    public void incrementLikeCount(Long id) {
        Knowledge k = this.getById(id);
        if (k != null) {
            k.setLikeCount((k.getLikeCount() == null ? 0 : k.getLikeCount()) + 1);
            this.updateById(k);
        }
    }
}
