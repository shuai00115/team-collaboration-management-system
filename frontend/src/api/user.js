import request from './index'

export function getMyInfo() {
  return request.get('/users/me')
}

export function updateProfile(data) {
  return request.put('/users/me', data)
}

export function changePassword(data) {
  return request.put('/users/me/password', data)
}

export function getMyTeams(params) {
  return request.get('/users/me/teams', { params })
}

export function getMyTasks(params) {
  return request.get('/users/me/tasks', { params })
}

export function getMyApplications(params) {
  return request.get('/users/me/applications', { params })
}

export function addSkill(data) {
  return request.post('/users/me/skills', data)
}

export function removeSkill(skillId) {
  return request.delete(`/users/me/skills/${skillId}`)
}
