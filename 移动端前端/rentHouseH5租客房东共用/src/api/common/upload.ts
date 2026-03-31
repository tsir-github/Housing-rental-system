import http from "@/utils/http";

/**
 * 上传图片到MinIO
 */
export function uploadImage(file: File, landlordId?: number) {
  const formData = new FormData();
  formData.append('file', file);
  
  return http.post<string>('/app/common/file/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  });
}

/**
 * 批量上传图片
 */
export function uploadImages(files: File[]) {
  const uploadPromises = files.map(file => uploadImage(file));
  return Promise.all(uploadPromises);
}