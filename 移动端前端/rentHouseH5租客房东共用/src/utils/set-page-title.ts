/**
 * 设置页面标题
 */
export default function setPageTitle(title?: string) {
  const defaultTitle = '公寓找房';
  
  if (title) {
    document.title = `${title} - ${defaultTitle}`;
  } else {
    document.title = defaultTitle;
  }
}