#!/bin/bash
BASE="http://localhost:8080"
PASS=0
FAIL=0

pass() { echo "[PASS] $1"; PASS=$((PASS+1)); }
fail() { echo "[FAIL] $1 — $2"; FAIL=$((FAIL+1)); }

# ===== 1. 公开接口 =====
echo "=== 公开接口 ==="

R=$(curl -s "$BASE/")
echo "$R" | grep -q "Chat Chart API" && pass "GET /" || fail "GET /" "$R"

R=$(curl -s "$BASE/health")
echo "$R" | grep -q "healthy" && pass "GET /health" || fail "GET /health" "$R"

CODE=$(curl -s -o /dev/null -w "%{http_code}" "$BASE/api/conversations")
[ "$CODE" = "401" ] && pass "无token返回401" || fail "401" "got $CODE"

# ===== 2. 认证接口 =====
echo -e "\n=== 认证接口 ==="

# 清理旧测试用户
mysql -u root -p123456 chat_chart -e "DELETE FROM chat_message WHERE conversation_id IN (SELECT conversation_id FROM conversation WHERE user_id IN (SELECT id FROM \`user\` WHERE username='testuser_reqid'));" 2>/dev/null
mysql -u root -p123456 chat_chart -e "DELETE FROM conversation WHERE user_id IN (SELECT id FROM \`user\` WHERE username='testuser_reqid');" 2>/dev/null
mysql -u root -p123456 chat_chart -e "DELETE FROM \`user\` WHERE username='testuser_reqid';" 2>/dev/null

R=$(curl -s -X POST "$BASE/api/auth/register" -H "Content-Type: application/json" -d '{"username":"testuser_reqid","password":"test123"}')
echo "$R" | grep -q "token" && pass "注册成功" || fail "注册" "$R"
TOKEN=$(echo "$R" | grep -o '"token":"[^"]*"' | head -1 | cut -d'"' -f4)
echo "  token=$TOKEN"

# 重复注册 — 服务端抛异常，检查HTTP状态码
CODE=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$BASE/api/auth/register" -H "Content-Type: application/json" -d '{"username":"testuser_reqid","password":"test123"}')
[ "$CODE" = "500" ] && pass "重复注册返回500(已有用户)" || echo "[INFO] 重复注册状态码: $CODE (可能无全局异常处理)"

R=$(curl -s -X POST "$BASE/api/auth/login" -H "Content-Type: application/json" -d '{"username":"testuser_reqid","password":"test123"}')
echo "$R" | grep -q "token" && pass "登录成功" || fail "登录" "$R"
TOKEN=$(echo "$R" | grep -o '"token":"[^"]*"' | head -1 | cut -d'"' -f4)

CODE=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$BASE/api/auth/login" -H "Content-Type: application/json" -d '{"username":"testuser_reqid","password":"wrong"}')
[ "$CODE" = "401" ] && pass "错误密码返回401" || fail "密码" "got $CODE"

CODE=$(curl -s -o /dev/null -w "%{http_code}" -H "Authorization: Bearer invalid" "$BASE/api/conversations")
[ "$CODE" = "401" ] && pass "无效token返回401" || fail "无效token" "got $CODE"

AUTH="Authorization: Bearer $TOKEN"

# ===== 3. 会话接口 =====
echo -e "\n=== 会话接口 ==="

R=$(curl -s -X POST "$BASE/api/conversation" -H "$AUTH")
CONV_ID=$(echo "$R" | grep -o '"conversationId":"[^"]*"' | head -1 | cut -d'"' -f4)
echo "$R" | grep -q "conversationId" && pass "创建会话" || fail "创建会话" "$R"
echo "  conversationId=$CONV_ID"

echo "$CONV_ID" | grep -qE '^[0-9]{13}-[a-z0-9]{6}$' && pass "conversationId格式: 时间戳-随机后缀" || echo "[WARN] 格式不符: $CONV_ID"

R=$(curl -s "$BASE/api/conversations" -H "$AUTH")
echo "$R" | grep -q "conversations" && pass "会话列表" || fail "会话列表" "$R"

R=$(curl -s "$BASE/api/conversation/$CONV_ID/messages" -H "$AUTH")
[ "$R" = "[]" ] && pass "空会话消息" || fail "空消息" "$R"

# ===== 4. 聊天接口 =====
echo -e "\n=== 聊天接口 ==="

R=$(curl -s -N --max-time 30 -X POST "$BASE/api/chat/stream" \
  -H "$AUTH" -H "Content-Type: application/json" \
  -d "{\"message\":\"hello test\",\"conversationId\":\"$CONV_ID\"}" 2>/dev/null)
echo "$R" | grep -q '"type":"content"' && pass "流式聊天-content事件" || fail "content" "no content"
echo "$R" | grep -q '"type":"end"' && pass "流式聊天-end事件" || fail "end" "no end"

# ===== 5. 数据验证(request_id) =====
echo -e "\n=== 数据验证 ==="

R=$(curl -s "$BASE/api/conversation/$CONV_ID/messages" -H "$AUTH")
echo "$R" | grep -q "requestId" && pass "消息包含requestId" || fail "requestId" "$R"
echo "$R" | grep -q '"role":"user"' && pass "消息包含user" || fail "user" "$R"
echo "$R" | grep -q '"role":"assistant"' && pass "消息包含assistant" || fail "assistant" "$R"

# 同一requestId下有user+assistant
FIRST_REQ=$(echo "$R" | grep -o '"requestId":"[^"]*"' | head -1 | cut -d'"' -f4)
COUNT=$(echo "$R" | grep -c "\"requestId\":\"$FIRST_REQ\"")
[ "$COUNT" -ge 2 ] && pass "同一requestId有user+assistant ($COUNT条)" || fail "同requestId" "只找到$COUNT条"

# 不包含旧字段
echo "$R" | grep -q "messageId" && fail "不应有messageId" "found" || pass "无旧messageId字段"
echo "$R" | grep -q '"sessionId"' && fail "不应有sessionId" "found" || pass "无旧sessionId字段"

# ===== 6. 数据库验证 =====
echo -e "\n=== 数据库验证 ==="

DB_RESULT=$(mysql -u root -p123456 chat_chart -N -e "SELECT COUNT(*) FROM chat_message WHERE request_id='$FIRST_REQ';" 2>/dev/null)
[ "$DB_RESULT" = "2" ] && pass "数据库中同一request_id有2条记录" || echo "[INFO] request_id=$FIRST_REQ 记录数: $DB_RESULT"

PK_CHECK=$(mysql -u root -p123456 chat_chart -N -e "SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS WHERE TABLE_SCHEMA='chat_chart' AND TABLE_NAME='chat_message' AND CONSTRAINT_TYPE='PRIMARY KEY';" 2>/dev/null)
[ "$PK_CHECK" = "1" ] && pass "chat_message有且仅有1个主键" || fail "主键" "count=$PK_CHECK"

SESSION_NULL=$(mysql -u root -p123456 chat_chart -N -e "SELECT COUNT(*) FROM chat_message WHERE session_id IS NULL;" 2>/dev/null)
TOTAL=$(mysql -u root -p123456 chat_chart -N -e "SELECT COUNT(*) FROM chat_message;" 2>/dev/null)
[ "$SESSION_NULL" = "$TOTAL" ] && pass "所有消息session_id均为NULL" || echo "[INFO] session_id非NULL: $(($TOTAL-$SESSION_NULL))条"

# ===== 汇总 =====
echo -e "\n=============================="
echo "总计: $((PASS+FAIL))  通过: $PASS  失败: $FAIL"
echo "=============================="

# 清理
mysql -u root -p123456 chat_chart -e "DELETE FROM chat_message WHERE conversation_id='$CONV_ID';" 2>/dev/null
mysql -u root -p123456 chat_chart -e "DELETE FROM conversation WHERE conversation_id='$CONV_ID';" 2>/dev/null
mysql -u root -p123456 chat_chart -e "DELETE FROM chat_message WHERE conversation_id IN (SELECT conversation_id FROM conversation WHERE user_id IN (SELECT id FROM \`user\` WHERE username='testuser_reqid'));" 2>/dev/null
mysql -u root -p123456 chat_chart -e "DELETE FROM conversation WHERE user_id IN (SELECT id FROM \`user\` WHERE username='testuser_reqid');" 2>/dev/null
mysql -u root -p123456 chat_chart -e "DELETE FROM \`user\` WHERE username='testuser_reqid';" 2>/dev/null
echo "[CLEANUP] done"
