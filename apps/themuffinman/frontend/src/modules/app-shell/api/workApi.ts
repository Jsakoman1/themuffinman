import {userShellApi} from "./userShellApi.ts"

/** Work-only compatibility facade while legacy callers migrate away from userShellApi. */
export const workApi = {
  searchQuests: userShellApi.searchQuests.bind(userShellApi),
  getQuestDetail: userShellApi.getQuestDetail.bind(userShellApi),
  getQuestPreview: userShellApi.getQuestPreview.bind(userShellApi),
  getQuestApplications: userShellApi.getQuestApplications.bind(userShellApi),
  decideQuestApplication: userShellApi.decideQuestApplication.bind(userShellApi),
  releaseQuestWorker: userShellApi.releaseQuestWorker.bind(userShellApi),
  replaceQuestWorker: userShellApi.replaceQuestWorker.bind(userShellApi),
  applyForQuest: userShellApi.applyForQuest.bind(userShellApi),
  createQuest: userShellApi.createQuest.bind(userShellApi),
  updateQuest: userShellApi.updateQuest.bind(userShellApi),
  executeQuestAction: userShellApi.executeQuestAction.bind(userShellApi),
  updateMyApplication: userShellApi.updateMyApplication.bind(userShellApi),
  withdrawMyApplication: userShellApi.withdrawMyApplication.bind(userShellApi),
  getMyApplications: userShellApi.getMyApplications.bind(userShellApi),
  getApplicationDetail: userShellApi.getApplicationDetail.bind(userShellApi)
}
