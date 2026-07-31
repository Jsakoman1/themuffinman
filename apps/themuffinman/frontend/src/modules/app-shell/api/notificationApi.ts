import {userShellApi} from "./userShellApi.ts"

export const notificationApi = {
  getMyNews: userShellApi.getMyNews.bind(userShellApi),
  markNewsAsRead: userShellApi.markNewsAsRead.bind(userShellApi),
  markNewsItemAsRead: userShellApi.markNewsItemAsRead.bind(userShellApi),
  getNotificationPreferences: userShellApi.getNotificationPreferences.bind(userShellApi),
  updateNotificationPreferences: userShellApi.updateNotificationPreferences.bind(userShellApi),
  getAttentionCenter: userShellApi.getAttentionCenter.bind(userShellApi)
}
