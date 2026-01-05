import request from './request'

/**
 * 分页查询传感器数据
 */
export const getSensorData = (params) => {
  return request.get('/sensor-data', { params })
}

/**
 * 根据ID查询传感器数据详情
 */
export const getSensorDataById = (dataId) => {
  return request.get(`/sensor-data/${dataId}`)
}

/**
 * 创建传感器数据
 */
export const createSensorData = (data) => {
  return request.post('/sensor-data', data)
}

/**
 * 更新传感器数据
 */
export const updateSensorData = (dataId, data) => {
  return request.put(`/sensor-data/${dataId}`, data)
}

/**
 * 删除传感器数据
 */
export const deleteSensorData = (dataId) => {
  return request.delete(`/sensor-data/${dataId}`)
}

/**
 * 批量删除传感器数据
 */
export const batchDeleteSensorData = (dataIds) => {
  return request.delete('/sensor-data/batch', { data: dataIds })
}
