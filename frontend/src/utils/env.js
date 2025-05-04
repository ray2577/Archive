/**
 * 环境变量工具函数
 * 用于安全地获取环境变量，无论在什么上下文中执行
 */

/**
 * 获取环境变量值
 * 优先使用import.meta.env，如果不可用则回退到process.env
 * 
 * @param {string} key - 环境变量名
 * @param {string} defaultValue - 默认值
 * @returns {string} 环境变量值或默认值
 */
export const getEnvValue = (key, defaultValue) => {
  try {
    // 检查import.meta是否可用
    if (typeof import.meta !== 'undefined' && import.meta.env) {
      return import.meta.env[key] || defaultValue;
    }
  } catch (e) {
    console.warn(`无法使用import.meta.env获取${key}，回退到process.env`);
  }
  
  // 回退到process.env
  return process.env[key] || defaultValue;
};

/**
 * 获取当前环境模式
 * @returns {string} 环境模式 (development, production, test等)
 */
export const getMode = () => {
  try {
    if (typeof import.meta !== 'undefined' && import.meta.env) {
      return import.meta.env.MODE || 'development';
    }
  } catch (e) {
    console.warn('无法使用import.meta.env获取MODE，回退到process.env');
  }
  
  return process.env.NODE_ENV || 'development';
};

/**
 * 检查是否为开发环境
 * @returns {boolean} 是否为开发环境
 */
export const isDevelopment = () => getMode() === 'development';

/**
 * 检查是否为生产环境
 * @returns {boolean} 是否为生产环境
 */
export const isProduction = () => getMode() === 'production';

/**
 * 检查是否为测试环境
 * @returns {boolean} 是否为测试环境
 */
export const isTest = () => getMode() === 'test';

export default {
  getEnvValue,
  getMode,
  isDevelopment,
  isProduction,
  isTest
}; 