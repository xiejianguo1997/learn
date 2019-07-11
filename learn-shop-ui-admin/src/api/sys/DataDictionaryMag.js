import request from '@/utils/request'

const baseUrl = 'admin-system/dataDictionaryApi';

/**
 * 查询 system 系统指定的 FieldType 的字典
 * @param roleFilter
 * @constructor
 */
export function LoadSysDataDictionary(fieldType) {
  return request({
    url: baseUrl + '/findDataDictionary/adminSystem/' + fieldType,
    method: 'get'
  })
}

/**
 * 保存权限信息
 * @param data
 * @constructor
 */
export function SavePermission(data) {
  return request({
    url: baseUrl + '/savePermission',
    method: 'post',
    data: Object.assign(data)
  });
}

/**
 * 更新权限信息
 * @param data
 * @constructor
 */
export function UpdatePermission(data) {
  return request({
    url: baseUrl + '/updatePermission',
    method: 'put',
    data: Object.assign(data)
  });
}

/**
 * 根据id删除权限信息
 * @param id
 * @constructor
 */
export function DeletePermissionById(id) {
  return request({
    url: baseUrl + '/deletePermissionById/' + id,
    method: 'delete'
  });
}
