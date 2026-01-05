import request from './request'

/**
 * 分页查询兽医咨询
 */
export const getVetConsultations = (params) => {
  return request.get('/vet-consultations', { params })
}

/**
 * 根据ID查询兽医咨询详情
 */
export const getVetConsultationById = (consultId) => {
  return request.get(`/vet-consultations/${consultId}`)
}

/**
 * 创建兽医咨询
 */
export const createVetConsultation = (data) => {
  return request.post('/vet-consultations', data)
}

/**
 * 更新兽医咨询
 */
export const updateVetConsultation = (consultId, data) => {
  return request.put(`/vet-consultations/${consultId}`, data)
}

/**
 * 删除兽医咨询
 */
export const deleteVetConsultation = (consultId) => {
  return request.delete(`/vet-consultations/${consultId}`)
}

/**
 * 批量删除兽医咨询
 */
export const batchDeleteVetConsultations = (consultIds) => {
  return request.delete('/vet-consultations/batch', { data: consultIds })
}

/**
 * 兽医回答问题
 */
export const answerConsultation = (consultId, answer) => {
  return request.post(`/vet-consultations/${consultId}/answer`, { answer })
}
