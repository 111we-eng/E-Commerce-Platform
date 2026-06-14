import request from '../utils/request'

export const productApi = {
  list() { return request.get('/products') },
  search(keyword) { return request.get('/products/search', { params: { keyword } }) },
  detail(id) { return request.get('/products/' + id) },
  byCategory(category) { return request.get('/products/category/' + category) }
}
