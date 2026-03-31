# 租房系统流程优化功能测试指南

## 测试准备

### 1. 确保数据库连接
请确保MySQL数据库正在运行，并且已经执行了 `rental_system_enhancements.sql` 脚本。

### 2. 执行测试数据插入
运行 `test_data_insert.sql` 脚本来添加测试数据。

## 功能测试步骤

### 测试1: 房源审核功能
1. **查看待审核房源**
   ```sql
   SELECT r.id, r.room_number, r.publisher_type, rv.review_status, rv.review_score
   FROM room_info r 
   LEFT JOIN room_review rv ON r.id = rv.room_id
   WHERE rv.review_status = 'PENDING' OR rv.review_status IS NULL;
   ```

2. **提交房源审核**
   - 使用API: `POST /admin/room-review/submit`
   - 测试数据:
   ```json
   {
     "roomId": 1,
     "reviewComments": "房源信息完整，符合标准",
     "reviewCriteria": {
       "location": 9,
       "facilities": 8,
       "photos": 9,
       "description": 8,
       "pricing": 8
     }
   }
   ```

3. **查看审核结果**
   ```sql
   SELECT * FROM room_review WHERE room_id = 1;
   ```

### 测试2: 直接入住申请功能
1. **查看申请列表**
   ```sql
   SELECT da.*, r.room_number, r.rent 
   FROM direct_move_in_application da
   JOIN room_info r ON da.room_id = r.id
   ORDER BY da.submitted_at DESC;
   ```

2. **处理申请**
   - 使用API: `PUT /admin/direct-move-in/process/{id}`
   - 测试数据:
   ```json
   {
     "status": "APPROVED",
     "processNotes": "申请理由充分，同意直接入住"
   }
   ```

### 测试3: 房东权限管理
1. **查看房东权限**
   ```sql
   SELECT * FROM landlord_permission WHERE is_active = 1;
   ```

2. **更新权限**
   - 使用API: `PUT /admin/landlord-permission/{id}`
   - 测试数据:
   ```json
   {
     "canPublishRooms": true,
     "canManageReviews": false,
     "canHandleApplications": true,
     "permissionLevel": "LIMITED"
   }
   ```

### 测试4: 发布者类型功能
1. **按发布者类型查询房源**
   ```sql
   SELECT publisher_type, COUNT(*) as count, AVG(rent) as avg_rent
   FROM room_info 
   WHERE publisher_type IS NOT NULL
   GROUP BY publisher_type;
   ```

2. **测试不同发布者的权限**
   - 房东发布的房源应该可以直接审核
   - 中介发布的房源需要额外验证

### 测试5: 看房引导类型
1. **查看不同引导类型的预约**
   ```sql
   SELECT guide_type, COUNT(*) as count
   FROM view_appointment 
   WHERE guide_type IS NOT NULL
   GROUP BY guide_type;
   ```

2. **测试引导类型对预约流程的影响**
   - 自助看房：无需人工干预
   - 中介引导：需要中介确认
   - 虚拟看房：需要提供VR链接

## API测试建议

### 使用Postman或类似工具测试以下接口：

1. **房源审核相关**
   - `GET /admin/room-review/list` - 获取审核列表
   - `POST /admin/room-review/submit` - 提交审核
   - `GET /admin/room-review/{id}` - 获取审核详情

2. **直接入住申请相关**
   - `GET /admin/direct-move-in/list` - 获取申请列表
   - `PUT /admin/direct-move-in/process/{id}` - 处理申请
   - `GET /admin/direct-move-in/{id}` - 获取申请详情

3. **房东权限相关**
   - `GET /admin/landlord-permission/list` - 获取权限列表
   - `PUT /admin/landlord-permission/{id}` - 更新权限
   - `POST /admin/landlord-permission/grant` - 授予权限

## 预期结果验证

### 1. 数据完整性检查
```sql
-- 检查外键约束
SELECT 'room_review外键检查' as check_type, COUNT(*) as count 
FROM room_review rv 
LEFT JOIN room_info r ON rv.room_id = r.id 
WHERE r.id IS NULL;

-- 检查枚举值
SELECT 'publisher_type枚举检查' as check_type, publisher_type, COUNT(*) 
FROM room_info 
WHERE publisher_type NOT IN ('LANDLORD', 'AGENT') AND publisher_type IS NOT NULL
GROUP BY publisher_type;
```

### 2. 业务逻辑验证
```sql
-- 检查审核评分逻辑
SELECT room_id, review_score, 
       JSON_EXTRACT(review_criteria, '$.location') as location_score,
       JSON_EXTRACT(review_criteria, '$.facilities') as facilities_score
FROM room_review 
WHERE review_status = 'APPROVED';
```

## 常见问题排查

1. **如果API返回404**：检查Controller路径映射
2. **如果数据库操作失败**：检查表结构和外键约束
3. **如果枚举值错误**：检查枚举类定义和数据库字段类型
4. **如果权限验证失败**：检查房东权限表数据

## 性能测试建议

1. **批量数据测试**：插入大量测试数据验证查询性能
2. **并发测试**：模拟多用户同时提交审核申请
3. **索引效果验证**：检查查询执行计划

记得在测试完成后清理测试数据！