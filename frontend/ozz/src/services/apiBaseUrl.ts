export const API_BASE_URL = ''

export const getApiUrl = (path: string) => {
  return `${API_BASE_URL}${path.startsWith('/') ? path : `/${path}`}`
}
