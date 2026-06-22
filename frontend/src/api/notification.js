import request from './index'

export function getNotifications(params) {
  return request.get('/notifications', { params })
}

export function getUnreadCount() {
  return request.get('/notifications/unread-count')
}

export function markRead(notificationId) {
  return request.put(`/notifications/${notificationId}/read`)
}

export function markAllRead(data) {
  return request.put('/notifications/read-all', data || {})
}
