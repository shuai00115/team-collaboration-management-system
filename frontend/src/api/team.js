import request from './index'

export function getTeams(params) {
  return request.get('/teams', { params })
}

export function createTeam(data) {
  return request.post('/teams', data)
}

export function getTeamDetail(teamId) {
  return request.get(`/teams/${teamId}`)
}

export function updateTeam(teamId, data) {
  return request.put(`/teams/${teamId}`, data)
}

export function deleteTeam(teamId, confirm) {
  return request.delete(`/teams/${teamId}`, { params: { confirm } })
}

export function getTeamMembers(teamId) {
  return request.get(`/teams/${teamId}/members`)
}

export function removeMember(teamId, userId) {
  return request.delete(`/teams/${teamId}/members/${userId}`)
}

export function applyToJoin(teamId, data) {
  return request.post(`/teams/${teamId}/applications`, data)
}

export function getApplications(teamId, params) {
  return request.get(`/teams/${teamId}/applications`, { params })
}

export function reviewApplication(teamId, requestId, data) {
  return request.put(`/teams/${teamId}/applications/${requestId}`, data)
}
