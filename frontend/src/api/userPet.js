import request from './request'

/**
 * 获取我的宠物列表
 */
export const getMyPets = () => {
  return request.get('/user-pets/my-pets')
}

/**
 * 添加宠物关联
 */
export const addPetRelation = (petId) => {
  return request.post('/user-pets/add-relation', { petId })
}

/**
 * 移除宠物关联
 */
export const removePetRelation = (petId) => {
  return request.delete('/user-pets/remove-relation', { data: { petId } })
}

/**
 * 批量添加宠物关联
 */
export const batchAddPetRelations = (petIds) => {
  return request.post('/user-pets/batch-add-relations', { petIds })
}
