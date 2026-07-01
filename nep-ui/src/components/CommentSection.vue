<template>
  <div class="comment-section">
    <h4 class="section-title">💬 评论 ({{ comments.length }})</h4>

    <!-- 发表评论 -->
    <div class="comment-input-box">
      <el-input
        v-model="newComment"
        type="textarea"
        :rows="3"
        placeholder="写下你的评论..."
        maxlength="500"
        show-word-limit
      />
      <el-button type="primary" size="small" :loading="posting" @click="handlePost"
        style="margin-top:8px">
        发表评论
      </el-button>
    </div>

    <!-- 评论列表 -->
    <div v-if="comments.length > 0" class="comment-list">
      <div v-for="c in topLevelComments" :key="c.id" class="comment-item">
        <div class="comment-avatar">{{ (c.userName || '匿名')[0] }}</div>
        <div class="comment-body">
          <div class="comment-header">
            <span class="c-user">{{ c.userName || '匿名' }}</span>
            <span class="c-time">{{ formatTime(c.createTime) }}</span>
          </div>
          <p class="c-content">
            <span v-if="c.replyToName" class="reply-hint">回复 @{{ c.replyToName }}：</span>
            {{ c.content }}
          </p>
          <div class="c-actions">
            <span class="action-btn" @click="handleLike(c)">
              <el-icon :color="c._liked ? '#D9534F' : '#999'"><StarFilled v-if="c._liked" /><Star v-else /></el-icon>
              {{ c.likeCount || 0 }}
            </span>
            <span class="action-btn" @click="startReply(c)">回复</span>
          </div>

          <!-- 回复输入框 -->
          <div v-if="replyTo === c.id" class="reply-input-box">
            <el-input v-model="replyContent" type="textarea" :rows="2"
              :placeholder="`回复 @${c.userName}...`" maxlength="300" />
            <div style="margin-top:6px;display:flex;gap:8px">
              <el-button size="small" type="primary" :loading="posting" @click="handleReply(c)">回复</el-button>
              <el-button size="small" @click="cancelReply">取消</el-button>
            </div>
          </div>

          <!-- 子回复 -->
          <div v-if="getReplies(c.id).length > 0" class="sub-comments">
            <div v-for="sub in getReplies(c.id)" :key="sub.id" class="sub-comment">
              <span class="sc-user">{{ sub.userName || '匿名' }}</span>
              <span v-if="sub.replyToName" class="reply-hint">回复 @{{ sub.replyToName }}：</span>
              <span class="sc-content">{{ sub.content }}</span>
              <span class="sc-time">{{ formatTime(sub.createTime) }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <el-empty v-else description="暂无评论，来发表第一条吧" :image-size="60" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { postComment, getComments, toggleLike } from '@/api/social'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()

const props = defineProps({
  targetType: { type: String, required: true },  // NEWS / KNOWLEDGE / DETECTION
  targetId: { type: Number, required: true },
  userId: { type: Number, default: () => Number(localStorage.getItem('userId') || 0) }
})

const comments = ref([])
const newComment = ref('')
const posting = ref(false)
const replyTo = ref(null)
const replyContent = ref('')

const topLevelComments = computed(() =>
  comments.value.filter(c => !c.parentId)
)

function getReplies(parentId) {
  return comments.value.filter(c => c.parentId === parentId)
}

function formatTime(t) {
  if (!t) return ''
  return t.replace('T', ' ').substring(0, 16)
}

async function loadComments() {
  try {
    const res = await getComments(props.targetType, props.targetId)
    comments.value = (res.data || []).map(c => ({ ...c, _liked: false }))
  } catch (e) { comments.value = [] }
}

async function handlePost() {
  if (!newComment.value.trim()) return
  posting.value = true
  try {
    await postComment({
      targetType: props.targetType,
      targetId: props.targetId,
      userId: props.userId,
      userName: userStore.userName || '用户',
      content: newComment.value
    })
    ElMessage.success('评论发表成功')
    newComment.value = ''
    await loadComments()
  } catch (e) {} finally { posting.value = false }
}

function startReply(c) {
  replyTo.value = c.id
  replyContent.value = ''
}

function cancelReply() {
  replyTo.value = null
  replyContent.value = ''
}

async function handleReply(parent) {
  if (!replyContent.value.trim()) return
  posting.value = true
  try {
    await postComment({
      targetType: props.targetType,
      targetId: props.targetId,
      userId: props.userId,
      userName: userStore.userName || '用户',
      content: replyContent.value,
      parentId: parent.id,
      replyToUid: parent.userId,
      replyToName: parent.userName
    })
    ElMessage.success('回复成功')
    cancelReply()
    await loadComments()
  } catch (e) {} finally { posting.value = false }
}

async function handleLike(c) {
  try {
    const res = await toggleLike(props.userId, 'COMMENT', c.id)
    c._liked = res.data.liked
    c.likeCount = res.data.likeCount
  } catch (e) {}
}

onMounted(loadComments)
</script>

<style scoped>
.comment-section {
  padding: 12px 0;
}

.section-title {
  font-size: 15px;
  color: #333;
  margin-bottom: 16px;
}

.comment-input-box {
  margin-bottom: 24px;
}

.comment-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.comment-item {
  display: flex;
  gap: 12px;
  padding: 14px 0;
  border-bottom: 1px solid #f0f0f0;
}

.comment-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: linear-gradient(135deg, #409EFF, #66b1ff);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
  flex-shrink: 0;
}

.comment-body {
  flex: 1;
  min-width: 0;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 4px;
}

.c-user {
  font-weight: 600;
  font-size: 13px;
  color: #333;
}

.c-time {
  font-size: 12px;
  color: #bbb;
}

.c-content {
  margin: 0 0 8px;
  font-size: 14px;
  line-height: 1.6;
  color: #444;
}

.reply-hint {
  color: #409EFF;
}

.c-actions {
  display: flex;
  gap: 16px;
}

.action-btn {
  cursor: pointer;
  font-size: 12px;
  color: #999;
  display: flex;
  align-items: center;
  gap: 3px;
  transition: color 0.2s;
}
.action-btn:hover { color: #409EFF; }

.reply-input-box {
  margin-top: 8px;
  padding-left: 0;
}

.sub-comments {
  margin-top: 8px;
  padding: 8px 12px;
  background: #f8f9fb;
  border-radius: 8px;
}

.sub-comment {
  padding: 6px 0;
  font-size: 13px;
  line-height: 1.5;
}
.sub-comment + .sub-comment { border-top: 1px solid #eee; }

.sc-user {
  font-weight: 600;
  color: #409EFF;
  margin-right: 4px;
}

.sc-content {
  color: #555;
}

.sc-time {
  font-size: 11px;
  color: #bbb;
  margin-left: 8px;
}
</style>
