/**
 * 商品 API — 支持后端接口 + Mock 数据自动切换
 *
 * 使用方式：
 *   import { productApi, enableMock } from '../api/product'
 *   enableMock(true)  // 开发环境无后端时启用 Mock
 *
 * 分页参数统一：
 *   { page, pageSize, category, keyword }
 */
import request from '../utils/request'
import { mockProductPage, mockProductSearch, mockProductDetail, mockCategories } from '../mock/products'

// Mock 开关
let useMock = false
export function enableMock(v = true) { useMock = v; console.log('[API] Mock ' + (v ? 'ENABLED' : 'DISABLED')) }
export function isMockEnabled() { return useMock }

export const productApi = {

  /**
   * 分页查询商品列表（每页独立请求）
   * @param {number} page     页码
   * @param {number} pageSize 每页条数
   * @param {string} category 分类（可选）
   * @param {string} keyword  关键词（可选）
   */
  async listPage({ page = 1, pageSize = 8, category = '', keyword = '' } = {}) {
    if (useMock) {
      return mockProductPage({ page, pageSize, category, keyword })
    }
    return request.get('/products', {
      params: { page, pageSize, category, keyword }
    })
  },

  /**
   * 搜索商品（支持分页）
   */
  async search({ keyword, page = 1, pageSize = 8 } = {}) {
    if (useMock) {
      return mockProductSearch(keyword, page, pageSize)
    }
    return request.get('/products/search', {
      params: { keyword, page, pageSize }
    })
  },

  /**
   * 商品详情
   */
  async detail(id) {
    if (useMock) {
      return mockProductDetail(id)
    }
    return request.get('/products/' + id)
  },

  /**
   * 按分类获取（支持分页）
   */
  async byCategory({ category, page = 1, pageSize = 8 } = {}) {
    if (useMock) {
      return mockProductPage({ page, pageSize, category })
    }
    return request.get('/products/category/' + category, {
      params: { page, pageSize }
    })
  },

  /**
   * 获取所有分类列表
   */
  async getCategories() {
    if (useMock) {
      return mockCategories()
    }
    return request.get('/products/categories')
  }
}
