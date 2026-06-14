import request from '../utils/request'

export const cartApi = {
  list() { return request.get('/cart') },
  add(productId, quantity) { return request.post('/cart', { productId, quantity }) },
  updateQuantity(id, quantity) { return request.put('/cart/' + id, { quantity }) },
  remove(id) { return request.delete('/cart/' + id) },
  clear() { return request.delete('/cart') }
}
