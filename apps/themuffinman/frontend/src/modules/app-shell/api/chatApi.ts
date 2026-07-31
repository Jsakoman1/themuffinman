import {userShellApi} from "./userShellApi.ts"

/** Chat compatibility facade; callers can migrate without changing transport semantics. */
export const chatApi = {
  getChatWorkspace: userShellApi.getChatWorkspace.bind(userShellApi),
  getChatConversations: userShellApi.getChatConversations.bind(userShellApi),
  createChatGroup: userShellApi.createChatGroup.bind(userShellApi),
  checkChatGroupEligibility: userShellApi.checkChatGroupEligibility.bind(userShellApi),
  openChat: userShellApi.openChat.bind(userShellApi),
  getChatConversationSync: userShellApi.getChatConversationSync.bind(userShellApi),
  getChatRefreshHint: userShellApi.getChatRefreshHint.bind(userShellApi),
  getChatMessages: userShellApi.getChatMessages.bind(userShellApi),
  sendChatMessage: userShellApi.sendChatMessage.bind(userShellApi),
  markChatConversationRead: userShellApi.markChatConversationRead.bind(userShellApi),
  deleteChatMessage: userShellApi.deleteChatMessage.bind(userShellApi),
  addChatReaction: userShellApi.addChatReaction.bind(userShellApi),
  removeChatReaction: userShellApi.removeChatReaction.bind(userShellApi)
}
