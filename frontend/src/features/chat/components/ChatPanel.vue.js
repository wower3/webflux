import { ref, onMounted } from 'vue';
import ChatView from './ChatView.vue';
const { defineProps, defineSlots, defineEmits, defineExpose, defineModel, defineOptions, withDefaults, } = await import('vue');
const emit = defineEmits();
const isLoggedIn = ref(!!localStorage.getItem('chat_token'));
const token = () => localStorage.getItem('chat_token') || '';
const usernameDisplay = ref(localStorage.getItem('chat_username') || '');
// 登录表单
const isRegister = ref(false);
const username = ref('');
const password = ref('');
const loading = ref(false);
const error = ref('');
// 会话管理
const showSidebar = ref(false);
const conversations = ref([]);
const currentConversationId = ref(null);
const chatViewRef = ref();
const handleAuth = async () => {
    if (loading.value || !username.value || !password.value)
        return;
    loading.value = true;
    error.value = '';
    try {
        const endpoint = isRegister.value ? '/api/auth/register' : '/api/auth/login';
        const res = await fetch(endpoint, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username: username.value, password: password.value })
        });
        const data = await res.json();
        if (res.ok) {
            localStorage.setItem('chat_token', data.token);
            localStorage.setItem('chat_username', data.username);
            isLoggedIn.value = true;
            usernameDisplay.value = data.username;
            await initChat();
        }
        else {
            error.value = data.message || '操作失败';
        }
    }
    catch (e) {
        error.value = '网络错误';
    }
    finally {
        loading.value = false;
    }
};
const logout = () => {
    localStorage.removeItem('chat_token');
    localStorage.removeItem('chat_username');
    isLoggedIn.value = false;
    usernameDisplay.value = '';
    conversations.value = [];
    currentConversationId.value = null;
    showSidebar.value = false;
};
const initChat = async () => {
    await fetchConversations();
    if (conversations.value.length > 0) {
        currentConversationId.value = conversations.value[0].conversationId;
        selectConversation(conversations.value[0].conversationId);
    }
    else {
        await createConversation();
    }
};
const fetchConversations = async () => {
    try {
        const res = await fetch('/api/conversations', {
            headers: { 'Authorization': `Bearer ${token()}` }
        });
        const data = await res.json();
        conversations.value = data.conversations || [];
    }
    catch {
        // silently fail — will retry on next user action
    }
};
const createConversation = async () => {
    try {
        const res = await fetch('/api/conversation', {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token()}`
            }
        });
        const data = await res.json();
        if (res.ok) {
            await fetchConversations();
            currentConversationId.value = data.conversationId;
            chatViewRef.value?.clearMessages();
            showSidebar.value = false;
        }
    }
    catch {
        // silently fail
    }
};
const selectConversation = async (conversationId) => {
    currentConversationId.value = conversationId;
    try {
        const res = await fetch(`/api/conversation/${conversationId}/messages`, {
            headers: { 'Authorization': `Bearer ${token()}` }
        });
        const data = await res.json();
        const msgs = data.map((m) => ({
            id: m.requestId,
            role: m.role,
            content: m.content,
            timestamp: new Date(m.createdAt).getTime()
        }));
        chatViewRef.value?.loadMessages(msgs);
        showSidebar.value = false;
    }
    catch {
        // silently fail
    }
};
const handleSendFromGuide = (text) => {
    chatViewRef.value?.sendMessage(text);
};
const formatConvName = (dateStr) => {
    if (!dateStr)
        return '新对话';
    const d = new Date(dateStr);
    const pad = (n) => String(n).padStart(2, '0');
    return `${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`;
};
const formatRelativeTime = (dateStr) => {
    if (!dateStr)
        return '';
    const d = new Date(dateStr);
    const now = new Date();
    const diffMs = now.getTime() - d.getTime();
    const diffMin = Math.floor(diffMs / 60000);
    if (diffMin < 1)
        return '刚刚';
    if (diffMin < 60)
        return `${diffMin}分钟前`;
    const diffHour = Math.floor(diffMin / 60);
    if (diffHour < 24)
        return `${diffHour}小时前`;
    return `${d.getMonth() + 1}/${d.getDate()}`;
};
// 面板打开时，如果已登录则初始化
onMounted(async () => {
    if (isLoggedIn.value) {
        await initChat();
    }
});
const __VLS_fnComponent = (await import('vue')).defineComponent({
    emits: {},
});
;
let __VLS_functionalComponentProps;
function __VLS_template() {
    let __VLS_ctx;
    /* Components */
    let __VLS_otherComponents;
    let __VLS_own;
    let __VLS_localComponents;
    let __VLS_components;
    let __VLS_styleScopedClasses;
    // CSS variable injection 
    // CSS variable injection end 
    let __VLS_resolvedLocalAndGlobalComponents;
    __VLS_elementAsFunction(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({ ...{ class: ("chat-panel") }, });
    if (!__VLS_ctx.isLoggedIn) {
        __VLS_elementAsFunction(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({ ...{ class: ("panel-header") }, });
        __VLS_elementAsFunction(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({ ...{ class: ("panel-title") }, });
        // @ts-ignore
        [isLoggedIn,];
        __VLS_elementAsFunction(__VLS_intrinsicElements.button, __VLS_intrinsicElements.button)({ ...{ onClick: (...[$event]) => {
                    if (!((!__VLS_ctx.isLoggedIn)))
                        return;
                    __VLS_ctx.$emit('close');
                    // @ts-ignore
                    [$emit,];
                } }, ...{ class: ("close-btn") }, });
        __VLS_elementAsFunction(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({ ...{ class: ("login-container") }, });
        __VLS_elementAsFunction(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({ ...{ class: ("login-card") }, });
        __VLS_elementAsFunction(__VLS_intrinsicElements.h3, __VLS_intrinsicElements.h3)({});
        (__VLS_ctx.isRegister ? '创建账号' : '登录');
        // @ts-ignore
        [isRegister,];
        __VLS_elementAsFunction(__VLS_intrinsicElements.p, __VLS_intrinsicElements.p)({ ...{ class: ("subtitle") }, });
        (__VLS_ctx.isRegister ? '注册后即可使用' : '请登录以继续');
        // @ts-ignore
        [isRegister,];
        __VLS_elementAsFunction(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({ ...{ class: ("form-group") }, });
        __VLS_elementAsFunction(__VLS_intrinsicElements.label, __VLS_intrinsicElements.label)({});
        __VLS_elementAsFunction(__VLS_intrinsicElements.input)({ ...{ onKeyup: (__VLS_ctx.handleAuth) }, value: ((__VLS_ctx.username)), type: ("text"), placeholder: ((__VLS_ctx.isRegister ? '请输入用户名' : '')), });
        // @ts-ignore
        [isRegister, handleAuth, username,];
        __VLS_elementAsFunction(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({ ...{ class: ("form-group") }, });
        __VLS_elementAsFunction(__VLS_intrinsicElements.label, __VLS_intrinsicElements.label)({});
        __VLS_elementAsFunction(__VLS_intrinsicElements.input)({ ...{ onKeyup: (__VLS_ctx.handleAuth) }, type: ("password"), placeholder: ("请输入密码"), });
        (__VLS_ctx.password);
        // @ts-ignore
        [handleAuth, password,];
        __VLS_elementAsFunction(__VLS_intrinsicElements.button, __VLS_intrinsicElements.button)({ ...{ onClick: (__VLS_ctx.handleAuth) }, ...{ class: ("auth-btn") }, disabled: ((__VLS_ctx.loading || !__VLS_ctx.username || !__VLS_ctx.password)), });
        (__VLS_ctx.loading ? '请求中...' : (__VLS_ctx.isRegister ? '注册' : '登录'));
        // @ts-ignore
        [isRegister, handleAuth, username, password, loading, loading,];
        __VLS_elementAsFunction(__VLS_intrinsicElements.p, __VLS_intrinsicElements.p)({ ...{ onClick: (...[$event]) => {
                    if (!((!__VLS_ctx.isLoggedIn)))
                        return;
                    __VLS_ctx.isRegister = !__VLS_ctx.isRegister;
                    // @ts-ignore
                    [isRegister, isRegister,];
                } }, ...{ class: ("switch-mode") }, });
        (__VLS_ctx.isRegister ? '已有账号？去登录' : '没有账号？去注册');
        // @ts-ignore
        [isRegister,];
        if (__VLS_ctx.error) {
            __VLS_elementAsFunction(__VLS_intrinsicElements.p, __VLS_intrinsicElements.p)({ ...{ class: ("error-msg") }, });
            (__VLS_ctx.error);
            // @ts-ignore
            [error, error,];
        }
    }
    else {
        __VLS_elementAsFunction(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({ ...{ class: ("panel-header") }, });
        __VLS_elementAsFunction(__VLS_intrinsicElements.button, __VLS_intrinsicElements.button)({ ...{ onClick: (...[$event]) => {
                    if (!(!((!__VLS_ctx.isLoggedIn))))
                        return;
                    __VLS_ctx.showSidebar = !__VLS_ctx.showSidebar;
                    // @ts-ignore
                    [showSidebar, showSidebar,];
                } }, ...{ class: ("icon-btn") }, title: ("会话列表"), });
        __VLS_elementAsFunction(__VLS_intrinsicElements.i, __VLS_intrinsicElements.i)({ ...{ class: ("fa fa-bars") }, });
        __VLS_elementAsFunction(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({ ...{ class: ("panel-title") }, });
        __VLS_elementAsFunction(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({ ...{ class: ("username") }, });
        (__VLS_ctx.usernameDisplay);
        // @ts-ignore
        [usernameDisplay,];
        __VLS_elementAsFunction(__VLS_intrinsicElements.button, __VLS_intrinsicElements.button)({ ...{ onClick: (__VLS_ctx.logout) }, ...{ class: ("icon-btn logout-btn") }, title: ("退出"), });
        __VLS_elementAsFunction(__VLS_intrinsicElements.i, __VLS_intrinsicElements.i)({ ...{ class: ("fa fa-sign-out") }, });
        // @ts-ignore
        [logout,];
        __VLS_elementAsFunction(__VLS_intrinsicElements.button, __VLS_intrinsicElements.button)({ ...{ onClick: (...[$event]) => {
                    if (!(!((!__VLS_ctx.isLoggedIn))))
                        return;
                    __VLS_ctx.$emit('close');
                    // @ts-ignore
                    [$emit,];
                } }, ...{ class: ("icon-btn close-btn") }, title: ("关闭"), });
        __VLS_elementAsFunction(__VLS_intrinsicElements.i, __VLS_intrinsicElements.i)({ ...{ class: ("fa fa-times") }, });
        __VLS_elementAsFunction(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({ ...{ class: ("panel-body") }, });
        // @ts-ignore
        const __VLS_0 = {}
            .Transition;
        ({}.Transition);
        ({}.Transition);
        __VLS_components.Transition;
        __VLS_components.Transition;
        // @ts-ignore
        [Transition, Transition,];
        // @ts-ignore
        const __VLS_1 = __VLS_asFunctionalComponent(__VLS_0, new __VLS_0({ name: ("sidebar"), }));
        const __VLS_2 = __VLS_1({ name: ("sidebar"), }, ...__VLS_functionalComponentArgsRest(__VLS_1));
        ({}({ name: ("sidebar"), }));
        const __VLS_5 = __VLS_nonNullable(__VLS_pickFunctionalComponentCtx(__VLS_0, __VLS_2));
        if (__VLS_ctx.showSidebar) {
            __VLS_elementAsFunction(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({ ...{ class: ("panel-sidebar") }, });
            __VLS_elementAsFunction(__VLS_intrinsicElements.button, __VLS_intrinsicElements.button)({ ...{ onClick: (__VLS_ctx.createConversation) }, ...{ class: ("new-chat-btn") }, });
            __VLS_elementAsFunction(__VLS_intrinsicElements.i, __VLS_intrinsicElements.i)({ ...{ class: ("fa fa-plus") }, });
            // @ts-ignore
            [showSidebar, createConversation,];
            __VLS_elementAsFunction(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({ ...{ class: ("conversation-list") }, });
            for (const [conv] of __VLS_getVForSourceType((__VLS_ctx.conversations))) {
                __VLS_elementAsFunction(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({ ...{ onClick: (...[$event]) => {
                            if (!(!((!__VLS_ctx.isLoggedIn))))
                                return;
                            if (!((__VLS_ctx.showSidebar)))
                                return;
                            __VLS_ctx.selectConversation(conv.conversationId);
                            // @ts-ignore
                            [conversations, selectConversation,];
                        } }, key: ((conv.conversationId)), ...{ class: ("conv-item") }, ...{ class: (({ active: conv.conversationId === __VLS_ctx.currentConversationId })) }, });
                __VLS_styleScopedClasses = ({ active: conv.conversationId === currentConversationId });
                __VLS_elementAsFunction(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({ ...{ class: ("conv-title") }, });
                __VLS_elementAsFunction(__VLS_intrinsicElements.i, __VLS_intrinsicElements.i)({ ...{ class: ("fa fa-message") }, });
                // @ts-ignore
                [currentConversationId,];
                __VLS_elementAsFunction(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({});
                (__VLS_ctx.formatConvName(conv.createdAt));
                // @ts-ignore
                [formatConvName,];
                __VLS_elementAsFunction(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({ ...{ class: ("conv-time") }, });
                (__VLS_ctx.formatRelativeTime(conv.createdAt));
                // @ts-ignore
                [formatRelativeTime,];
            }
            if (__VLS_ctx.conversations.length === 0) {
                __VLS_elementAsFunction(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({ ...{ class: ("empty-hint") }, });
                // @ts-ignore
                [conversations,];
            }
        }
        __VLS_nonNullable(__VLS_5.slots).default;
        __VLS_elementAsFunction(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({ ...{ class: ("chat-area") }, });
        // @ts-ignore
        [ChatView,];
        // @ts-ignore
        const __VLS_6 = __VLS_asFunctionalComponent(ChatView, new ChatView({ ...{ 'onSendFromGuide': {} }, ref: ("chatViewRef"), conversationId: ((__VLS_ctx.currentConversationId)), }));
        const __VLS_7 = __VLS_6({ ...{ 'onSendFromGuide': {} }, ref: ("chatViewRef"), conversationId: ((__VLS_ctx.currentConversationId)), }, ...__VLS_functionalComponentArgsRest(__VLS_6));
        ({}({ ...{ 'onSendFromGuide': {} }, ref: ("chatViewRef"), conversationId: ((__VLS_ctx.currentConversationId)), }));
        // @ts-ignore
        (__VLS_ctx.chatViewRef);
        let __VLS_11;
        const __VLS_12 = {
            onSendFromGuide: (__VLS_ctx.handleSendFromGuide)
        };
        const __VLS_10 = __VLS_nonNullable(__VLS_pickFunctionalComponentCtx(ChatView, __VLS_7));
        let __VLS_8;
        let __VLS_9;
        // @ts-ignore
        [currentConversationId, chatViewRef, handleSendFromGuide,];
    }
    if (typeof __VLS_styleScopedClasses === 'object' && !Array.isArray(__VLS_styleScopedClasses)) {
        __VLS_styleScopedClasses['chat-panel'];
        __VLS_styleScopedClasses['panel-header'];
        __VLS_styleScopedClasses['panel-title'];
        __VLS_styleScopedClasses['close-btn'];
        __VLS_styleScopedClasses['login-container'];
        __VLS_styleScopedClasses['login-card'];
        __VLS_styleScopedClasses['subtitle'];
        __VLS_styleScopedClasses['form-group'];
        __VLS_styleScopedClasses['form-group'];
        __VLS_styleScopedClasses['auth-btn'];
        __VLS_styleScopedClasses['switch-mode'];
        __VLS_styleScopedClasses['error-msg'];
        __VLS_styleScopedClasses['panel-header'];
        __VLS_styleScopedClasses['icon-btn'];
        __VLS_styleScopedClasses['fa'];
        __VLS_styleScopedClasses['fa-bars'];
        __VLS_styleScopedClasses['panel-title'];
        __VLS_styleScopedClasses['username'];
        __VLS_styleScopedClasses['icon-btn'];
        __VLS_styleScopedClasses['logout-btn'];
        __VLS_styleScopedClasses['fa'];
        __VLS_styleScopedClasses['fa-sign-out'];
        __VLS_styleScopedClasses['icon-btn'];
        __VLS_styleScopedClasses['close-btn'];
        __VLS_styleScopedClasses['fa'];
        __VLS_styleScopedClasses['fa-times'];
        __VLS_styleScopedClasses['panel-body'];
        __VLS_styleScopedClasses['panel-sidebar'];
        __VLS_styleScopedClasses['new-chat-btn'];
        __VLS_styleScopedClasses['fa'];
        __VLS_styleScopedClasses['fa-plus'];
        __VLS_styleScopedClasses['conversation-list'];
        __VLS_styleScopedClasses['conv-item'];
        __VLS_styleScopedClasses['conv-title'];
        __VLS_styleScopedClasses['fa'];
        __VLS_styleScopedClasses['fa-message'];
        __VLS_styleScopedClasses['conv-time'];
        __VLS_styleScopedClasses['empty-hint'];
        __VLS_styleScopedClasses['chat-area'];
    }
    var __VLS_slots;
    return __VLS_slots;
    const __VLS_componentsOption = {};
    let __VLS_name;
    let __VLS_defineComponent;
    const __VLS_internalComponent = __VLS_defineComponent({
        setup() {
            return {
                ChatView: ChatView,
                isLoggedIn: isLoggedIn,
                usernameDisplay: usernameDisplay,
                isRegister: isRegister,
                username: username,
                password: password,
                loading: loading,
                error: error,
                showSidebar: showSidebar,
                conversations: conversations,
                currentConversationId: currentConversationId,
                chatViewRef: chatViewRef,
                handleAuth: handleAuth,
                logout: logout,
                createConversation: createConversation,
                selectConversation: selectConversation,
                handleSendFromGuide: handleSendFromGuide,
                formatConvName: formatConvName,
                formatRelativeTime: formatRelativeTime,
            };
        },
        emits: {},
    });
}
export default (await import('vue')).defineComponent({
    setup() {
        return {};
    },
    emits: {},
});
;
