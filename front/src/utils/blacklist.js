// 黑名单工具函数
import request from "../utils/request";

/**
 * 检查用户是否在黑名单
 * @param {number|string} userId 用户ID
 * @returns {Promise<boolean>} true=在黑名单，false=不在
 */
export async function isUserInBlacklist(userId) {
  if (!userId) return false;
  try {
    const res = await request.get(`/blacklist/user/${userId}/details`);
    // 检查是否有逾期记录，如果有则认为在黑名单
    if (res && res.code === 1 && res.data && Array.isArray(res.data) && res.data.length > 0) {
      return true;
    }
    return false;
  } catch (e) {
    return false;
  }
}
