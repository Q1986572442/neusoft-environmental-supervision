package org.nep.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.nep.system.entity.FeedbackNote;
import org.nep.system.mapper.FeedbackNoteMapper;
import org.nep.system.service.FeedbackNoteService;
import org.springframework.stereotype.Service;

@Service
public class FeedbackNoteServiceImpl
        extends ServiceImpl<FeedbackNoteMapper, FeedbackNote>
        implements FeedbackNoteService {
}
