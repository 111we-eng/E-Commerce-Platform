import request from '../utils/request'

export const orderApi = {
  list() { return request.get('/orders') },
  detail(id) { return request.get('/orders/' + id) },
  create(data) { return request.post('/orders', data) },
  cancel(id) { return request.put('/orders/' + id + '/cancel') },
  all() { return request.get('/orders/all') },
  updateStatus(id, status) { return request.put('/orders/' + id + '/status', { status }) }
}
