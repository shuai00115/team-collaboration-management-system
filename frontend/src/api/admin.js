import request from './index'

export function getAdminUsers(params) {
  return request.get('/admin/users', { params })
}

export function updateUser(userId, data) {
  return request.put(`/admin/users/${userId}`, data)
}

export function getAdminTeams(params) {
  return request.get('/admin/teams', { params })
}

export function forceDeleteTeam(teamId) {
  return request.delete(`/admin/teams/${teamId}`)
}

export function createSkill(data) {
  return request.post('/admin/skills', data)
}

export function deleteSkill(skillId) {
  return request.delete(`/admin/skills/${skillId}`)
}
