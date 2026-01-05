import request from './request'

/**
 * 分页查询宠物
 */
export const getPets = (params) => {
  return request.get('/pets', { params })
}

/**
 * 根据ID查询宠物详情
 */
export const getPetById = (petId) => {
  return request.get(`/pets/${petId}`)
}

/**
 * 创建宠物
 */
export const createPet = (data) => {
  return request.post('/pets', data)
}

/**
 * 更新宠物信息
 */
export const updatePet = (petId, data) => {
  return request.put(`/pets/${petId}`, data)
}

/**
 * 删除宠物
 */
export const deletePet = (petId) => {
  return request.delete(`/pets/${petId}`)
}

/**
 * 批量删除宠物
 */
export const batchDeletePets = (petIds) => {
  return request.delete('/pets/batch', { data: petIds })
}

/**
 * 上传宠物图片
 */
export const uploadPetImage = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/pets/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}
